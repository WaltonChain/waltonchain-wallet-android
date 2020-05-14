package io.horizontalsystems.bankwallet.modules.settings.managekeys.views

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.BaseActivity
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.core.utils.ModuleCode
import io.horizontalsystems.bankwallet.core.utils.ModuleField
import io.horizontalsystems.bankwallet.entities.AccountType
import io.horizontalsystems.bankwallet.entities.PresentationMode
import io.horizontalsystems.bankwallet.modules.backup.BackupInteractor
import io.horizontalsystems.bankwallet.modules.backup.BackupModule
import io.horizontalsystems.bankwallet.modules.blockchainsettings.CoinSettingsModule
import io.horizontalsystems.bankwallet.modules.blockchainsettings.SettingsMode
import io.horizontalsystems.bankwallet.modules.createwallet.CreateWalletModule
import io.horizontalsystems.bankwallet.modules.restore.RestoreModule
import io.horizontalsystems.bankwallet.modules.restore.restorecoins.RestoreCoinsModule
import io.horizontalsystems.bankwallet.modules.send.submodules.confirmation.ConfirmationFragment
import io.horizontalsystems.bankwallet.modules.settings.managekeys.*
import io.horizontalsystems.bankwallet.modules.settings.managekeys.views.ManageKeysDialog.ManageAction
import io.horizontalsystems.pin.PinActivity
import io.horizontalsystems.pin.PinInteractionType
import io.horizontalsystems.pin.PinModule
import kotlinx.android.synthetic.main.activity_manage_keys.*

class ManageKeysActivity : BaseActivity(), ManageKeysDialog.Listener, ManageKeysAdapter.Listener {

    private lateinit var presenter: ManageKeysPresenter
    private lateinit var adapter: ManageKeysAdapter
    private var mUnlinkItem: ManageAccountItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = ViewModelProvider(this, ManageKeysModule.Factory()).get(ManageKeysPresenter::class.java)

        setContentView(R.layout.activity_manage_keys)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = ManageKeysAdapter(this)
        recyclerView.adapter = adapter

        observeView(presenter.view as ManageKeysView)
        observeRouter(presenter.router as ManageKeysRouter)

        (presenter.view as ManageKeysView).confirmUnlinkEvent.observe(this, Observer { item ->
            item.account?.let { account ->

                val confirmationList = listOf(
                        getString(R.string.ManageKeys_Delete_ConfirmationRemove, getString(item.predefinedAccountType.title)),
                        getString(R.string.ManageKeys_Delete_ConfirmationDisable, getString(item.predefinedAccountType.coinCodes)),
                        getString(R.string.ManageKeys_Delete_ConfirmationLose)
                )

                val confirmListener = object : ManageKeysDeleteAlert.Listener {
                    override fun onConfirmationSuccess() {
                        presenter.onConfirmUnlink(account.id)
                    }
                }

                ManageKeysDeleteAlert.show(this, getString(item.predefinedAccountType.title), confirmationList, confirmListener)
            }
        })

        (presenter.view as ManageKeysView).confirmBackupEvent.observe(this, Observer {
            val title = getString(R.string.ManageKeys_Delete_Alert_Title)
            val subtitle = getString(it.predefinedAccountType.title)
            val description = getString(R.string.ManageKeys_Delete_Alert)
            ManageKeysDialog.show(title, subtitle, description, this, this, ManageAction.BACKUP)
        })

        presenter.onLoad()
    }

    private fun observeView(view: ManageKeysView) {
        view.showItemsEvent.observe(this, Observer { list ->
//            adapter.items = list
            // 只显示默认钱包
            adapter.items = mutableListOf(list[0])
            adapter.notifyDataSetChanged()
        })
    }

    private fun observeRouter(router: ManageKeysRouter) {
        router.showRestoreKeyInput.observe(this, Observer { predefinedAccountType ->
            RestoreModule.startForResult(this, predefinedAccountType, ModuleCode.RESTORE_KEY_INPUT)
        })

        router.showCoinSettingsEvent.observe(this, Observer {
            CoinSettingsModule.startForResult(this, SettingsMode.InsideRestore)
        })

        router.showCoinManager.observe(this, Observer { (predefinedAccountType, accountType) ->
            RestoreCoinsModule.start(this, predefinedAccountType, accountType, PresentationMode.InApp)
        })

        router.showCreateWalletLiveEvent.observe(this, Observer { predefinedAccountType ->
            CreateWalletModule.startInApp(this, predefinedAccountType)
        })

        router.showBackupModule.observe(this, Observer { (account, predefinedAccountType) ->
            BackupModule.start(this, account, getString(predefinedAccountType.coinCodes))
        })

        router.closeEvent.observe(this, Observer {
            finish()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ModuleCode.RESTORE_KEY_INPUT -> {
                val accountType = data?.getParcelableExtra<AccountType>(ModuleField.ACCOUNT_TYPE)
                        ?: return
                presenter.didEnterValidAccount(accountType)
            }
            ModuleCode.COIN_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    presenter.didReturnFromCoinSettings()
                }
            }
            ModuleCode.UNLOCK_PIN -> {
                when (resultCode) {
                    PinModule.RESULT_OK -> {
                        mUnlinkItem?.let { item ->
                            presenter.onClickUnlink(item)
                        }
                    }
                    PinModule.RESULT_CANCELLED -> {}
                }
            }
        }
    }

    //  ManageKeysAdapter Listener

    override fun onClickCreate(item: ManageAccountItem) {
        presenter.onClickCreate(item)
    }

    override fun onClickRestore(item: ManageAccountItem) {
        presenter.onClickRestore(item)
    }

    override fun onClickBackup(item: ManageAccountItem) {
        presenter.onClickBackup(item)
    }

    override fun onClickUnlink(item: ManageAccountItem) {
        mUnlinkItem = item
        val interactor = BackupInteractor(App.backupManager, App.pinManager)

        if (interactor.isPinSet) {
            PinActivity.startForResult(this, PinInteractionType.UNLOCK, ModuleCode.UNLOCK_PIN, true)
        } else {
            PinActivity.startForResult(this, PinInteractionType.SET_PIN, ModuleCode.UNLOCK_PIN_SEND, false)
        }
    }

    //  ManageKeysDialog Listener

    override fun onClickBackupKey() {
        presenter.onConfirmBackup()
    }
}
