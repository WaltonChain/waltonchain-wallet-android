package io.horizontalsystems.bankwallet.modules.createwallet.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.BaseActivity
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.core.utils.ModuleCode
import io.horizontalsystems.bankwallet.core.utils.ModuleField
import io.horizontalsystems.bankwallet.entities.Coin
import io.horizontalsystems.bankwallet.entities.PredefinedAccountType
import io.horizontalsystems.bankwallet.entities.PresentationMode
import io.horizontalsystems.bankwallet.modules.backup.BackupInteractor
import io.horizontalsystems.bankwallet.modules.createwallet.CreateWalletModule
import io.horizontalsystems.bankwallet.modules.createwallet.CreateWalletPresenter
import io.horizontalsystems.bankwallet.modules.createwallet.CreateWalletRouter
import io.horizontalsystems.bankwallet.modules.createwallet.CreateWalletView
import io.horizontalsystems.bankwallet.modules.main.MainModule
import io.horizontalsystems.core.helpers.HudHelper
import io.horizontalsystems.pin.PinActivity
import io.horizontalsystems.pin.PinInteractionType
import io.horizontalsystems.pin.PinModule
import io.horizontalsystems.views.AlertDialogFragment
import kotlinx.android.synthetic.main.activity_create_wallet.*

class CreateWalletActivity : BaseActivity(), CoinItemsAdapter.Listener {
    private lateinit var presenter: CreateWalletPresenter
    private lateinit var coinItemsAdapter: CoinItemsAdapter
    private var buttonEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wallet)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val presentationMode: PresentationMode = intent.getParcelableExtra(ModuleField.PRESENTATION_MODE)
                ?: PresentationMode.Initial
        val predefinedAccountType: PredefinedAccountType? = intent.getParcelableExtra(ModuleField.PREDEFINED_ACCOUNT_TYPE)

        presenter = ViewModelProvider(this, CreateWalletModule.Factory(presentationMode, predefinedAccountType)).get(CreateWalletPresenter::class.java)

        observeView(presenter.view as CreateWalletView)
        observeRouter(presenter.router as CreateWalletRouter)

        coinItemsAdapter = CoinItemsAdapter(this)
        coins.adapter = coinItemsAdapter

        presenter.onLoad()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menuCreate)?.apply {
            isEnabled = buttonEnabled
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuCreate -> {
                val interactor = BackupInteractor(App.backupManager, App.pinManager)
                if (!interactor.isPinSet) {
                    PinActivity.startForResult(this, PinInteractionType.SET_PIN, ModuleCode.UNLOCK_PIN, false)
                } else {
                    presenter.onCreateButtonClick()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ModuleCode.BACKUP_WORDS -> {
            }
            ModuleCode.BACKUP_PRIVATE -> {
                // 备份私钥
            }
            ModuleCode.BACKUP_EOS -> {
            }
            ModuleCode.UNLOCK_PIN -> {
                when (resultCode) {
                    PinModule.RESULT_OK -> {
                        presenter.onCreateButtonClick()
                    }
                }
            }
        }
    }
    // CoinItemsAdapter.Listener

    override fun enable(coin: Coin) {
        presenter.onEnable(coin)
    }

    override fun disable(coin: Coin) {
        presenter.onDisable(coin)
    }

    override fun select(coin: Coin) {
        presenter.onSelect(coin)
    }

    private fun observeView(view: CreateWalletView) {
        view.coinsLiveData.observe(this, Observer { coins ->
            coinItemsAdapter.viewItems = coins
            coinItemsAdapter.notifyDataSetChanged()
        })

        view.createButtonEnabled.observe(this, Observer { enabled ->
            buttonEnabled = enabled
            invalidateOptionsMenu()
        })

        view.showNotSupported.observe(this, Observer { predefinedAccountType ->
            AlertDialogFragment.newInstance(
                    getString(R.string.ManageCoins_Alert_CantCreateTitle, getString(predefinedAccountType.title)),
                    getString(R.string.ManageCoins_Alert_CantCreateDescription, getString(predefinedAccountType.title)),
                    R.string.Alert_Ok
            ).show(supportFragmentManager, "alert_dialog")
        })
    }

    private fun observeRouter(router: CreateWalletRouter) {
        router.startMainModuleLiveEvent.observe(this, Observer {
            MainModule.startAsNewTask(this)
            finish()
        })
        router.showSuccessAndClose.observe(this, Observer {
            HudHelper.showSuccessMessage(R.string.Hud_Text_Done, HudHelper.ToastDuration.LONG)
            finish()
        })
    }
}
