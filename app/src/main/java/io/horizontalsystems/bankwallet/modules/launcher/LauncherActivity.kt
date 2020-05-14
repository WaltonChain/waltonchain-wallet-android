package io.horizontalsystems.bankwallet.modules.launcher

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.modules.keystore.KeyStoreActivity
import io.horizontalsystems.bankwallet.modules.lockscreen.LockScreenModule
import io.horizontalsystems.bankwallet.modules.main.MainModule
import io.horizontalsystems.bankwallet.modules.welcome.WelcomeModule
import io.horizontalsystems.pin.PinModule
import java.util.logging.Level
import java.util.logging.Logger

class LauncherActivity : AppCompatActivity() {

    private lateinit var viewModel: LaunchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        viewModel = ViewModelProvider(this).get(LaunchViewModel::class.java)
        viewModel.init()

        viewModel.openWelcomeModule.observe(this, Observer {
            WelcomeModule.start(this)
            finish()
        })

        Logger.getLogger("LauncherActivity").log(Level.INFO, "time:${SystemClock.elapsedRealtime()}")
        viewModel.openMainModule.observe(this, Observer {
            Handler().postDelayed({
                Logger.getLogger("LauncherActivity").log(Level.INFO, "start main time:${SystemClock.elapsedRealtime()}")

                MainModule.start(this)
                finish()
            }, 1000)
        })

        viewModel.openUnlockModule.observe(this, Observer {
            LockScreenModule.startForUnlock(this, REQUEST_CODE_UNLOCK_PIN)
        })

        viewModel.openNoSystemLockModule.observe(this, Observer {
            KeyStoreActivity.startForNoSystemLock(this)
        })

        viewModel.openKeyInvalidatedModule.observe(this, Observer {
            KeyStoreActivity.startForInvalidKey(this)
        })

        viewModel.openUserAuthenticationModule.observe(this, Observer {
            KeyStoreActivity.startForUserAuthentication(this)
        })

        viewModel.closeApplication.observe(this, Observer {
            finishAffinity()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UNLOCK_PIN) {
            when (resultCode) {
                PinModule.RESULT_OK -> viewModel.delegate.didUnlock()
                PinModule.RESULT_CANCELLED -> viewModel.delegate.didCancelUnlock()
            }
        }
    }

    companion object {
        const val REQUEST_CODE_UNLOCK_PIN = 1
    }
}
