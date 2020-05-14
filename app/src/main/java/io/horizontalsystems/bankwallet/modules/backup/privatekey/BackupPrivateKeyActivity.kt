package io.horizontalsystems.bankwallet.modules.backup.privatekey

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import io.horizontalsystems.bankwallet.BaseActivity
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.utils.ModuleCode
import io.horizontalsystems.core.setOnSingleClickListener
import kotlinx.android.synthetic.main.activity_backup_private_key.*

class BackupPrivateKeyActivity : BaseActivity() {

    val viewModel by viewModels<BackupPrivateKeyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_private_key)

        val backedUp = intent.getBooleanExtra(ACCOUNT_BACKEDUP, false)
        val backupPrivateKey = intent.getStringExtra(PRIVATE_KEY) ?: ""

        viewModel.init(backupPrivateKey, backedUp)

        if (savedInstanceState == null) {
            viewModel.delegate.viewDidLoad()
        }

        buttonBack.setOnSingleClickListener { viewModel.delegate.onBackClick() }
        buttonNext.setOnSingleClickListener { viewModel.delegate.onNextClick() }

        viewModel.loadPageLiveEvent.observe(this, Observer {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer, BackupPrivateKeyFragment())
                addToBackStack(null)
                commit()
            }
        })

        viewModel.notifyBackedUpEvent.observe(this, Observer {
            setResult(BackupPrivateKeyModule.RESULT_BACKUP)
            finish()
        })

        viewModel.notifyClosedEvent.observe(this, Observer {
            setResult(BackupPrivateKeyModule.RESULT_SHOW)
            finish()
        })

        viewModel.closeLiveEvent.observe(this, Observer {
            finish()
        })
    }

    override fun onBackPressed() {
        viewModel.delegate.onBackClick()
    }

    companion object {
        const val ACCOUNT_BACKEDUP = "account_backedup"
        const val PRIVATE_KEY = "PrivateKey"

        fun start(context: AppCompatActivity, PrivateKey: String, backedUp: Boolean) {
            val intent = Intent(context, BackupPrivateKeyActivity::class.java).apply {
                putExtra(PRIVATE_KEY, PrivateKey)
                putExtra(ACCOUNT_BACKEDUP, backedUp)
            }

            context.startActivityForResult(intent, ModuleCode.BACKUP_PRIVATE)
        }
    }
}
