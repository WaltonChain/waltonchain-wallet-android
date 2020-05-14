package io.horizontalsystems.bankwallet.modules.managecoins.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.BaseActivity
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.utils.ModuleCode
import io.horizontalsystems.bankwallet.core.utils.ModuleField
import io.horizontalsystems.bankwallet.entities.AccountType
import io.horizontalsystems.bankwallet.entities.Coin
import io.horizontalsystems.bankwallet.entities.PredefinedAccountType
import io.horizontalsystems.bankwallet.modules.blockchainsettings.CoinSettingsModule
import io.horizontalsystems.bankwallet.modules.blockchainsettings.SettingsMode
import io.horizontalsystems.bankwallet.modules.createwallet.view.CoinItemsAdapter
import io.horizontalsystems.bankwallet.modules.managecoins.ManageWalletsModule
import io.horizontalsystems.bankwallet.modules.managecoins.ManageWalletsPresenter
import io.horizontalsystems.bankwallet.modules.managecoins.ManageWalletsRouter
import io.horizontalsystems.bankwallet.modules.managecoins.ManageWalletsView
import io.horizontalsystems.bankwallet.modules.restore.RestoreModule
import io.horizontalsystems.core.helpers.HudHelper
import kotlinx.android.synthetic.main.activity_manage_coins.*

class ManageWalletsActivity : BaseActivity(), ManageWalletsDialog.Listener, CoinItemsAdapter.Listener {

    private lateinit var presenter: ManageWalletsPresenter
    private lateinit var adapter: CoinItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_coins)

        val showCloseButton = intent?.extras?.getBoolean(ModuleField.SHOW_CLOSE_BUTTON, false) ?: false
        val isColdStart = savedInstanceState != null

        presenter = ViewModelProvider(this, ManageWalletsModule.Factory(showCloseButton, isColdStart ))
                .get(ManageWalletsPresenter::class.java)

        presenter.onLoad()

        setSupportActionBar(toolbar)
        if (!presenter.showCloseButton) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        adapter = CoinItemsAdapter(this)
        recyclerView.adapter = adapter

        observe(presenter.view as ManageWalletsView)
        observe(presenter.router as ManageWalletsRouter)
    }

    private fun observe(view: ManageWalletsView) {
        view.coinsLiveData.observe(this, Observer { viewItems ->
            adapter.viewItems = viewItems
            adapter.notifyDataSetChanged()
        })

        view.showManageKeysDialog.observe(this, Observer { (coin, predefinedAccountType) ->
            ManageWalletsDialog.show(this, this, coin, predefinedAccountType)
        })

        view.showErrorEvent.observe(this, Observer {
            onCancel() // will uncheck coin
        })

        view.showSuccessEvent.observe(this, Observer {
            HudHelper.showSuccessMessage(R.string.Hud_Text_Done)
        })

    }

    private fun observe(router: ManageWalletsRouter) {
        router.openRestoreModule.observe(this, Observer { predefinedAccountType ->
            RestoreModule.startForResult(this, predefinedAccountType, ModuleCode.RESTORE_KEY_INPUT)
        })

        router.closeLiveDate.observe(this, Observer {
            finish()
        })

        router.showCoinSettings.observe(this, Observer {
            CoinSettingsModule.startForResult(this, SettingsMode.InsideRestore)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.manage_coins_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menuClose)?.apply {
            isVisible = presenter.showCloseButton
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuClose ->  {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ModuleCode.COIN_SETTINGS -> {
                presenter.onCoinSettingsClose()
            }
            ModuleCode.RESTORE_KEY_INPUT-> {
                val accountType = data?.getParcelableExtra<AccountType>(ModuleField.ACCOUNT_TYPE) ?: return
                presenter.didRestore(accountType)
            }
        }

    }

    // ManageWalletsDialog.Listener

    override fun onClickCreateKey(predefinedAccountType: PredefinedAccountType) {
        presenter.onSelectNewAccount(predefinedAccountType)
    }

    override fun onClickRestoreKey(predefinedAccountType: PredefinedAccountType) {
        presenter.onSelectRestoreAccount(predefinedAccountType)
    }

    override fun onCancel() {
        presenter.onClickCancel()
    }

    // CoinItemsAdapter listener

    override fun enable(coin: Coin) {
        presenter.onEnable(coin)
    }

    override fun disable(coin: Coin) {
        presenter.onDisable(coin)
    }

    override fun select(coin: Coin) {
        presenter.onSelect(coin)
    }
}
