package io.horizontalsystems.bankwallet.modules.backup

import androidx.lifecycle.ViewModel
import io.horizontalsystems.core.SingleLiveEvent
import io.horizontalsystems.bankwallet.entities.Account

class BackupViewModel : ViewModel(), BackupModule.View, BackupModule.Router {

    lateinit var delegate: BackupModule.ViewDelegate

    val startPinModule = SingleLiveEvent<Void>()
    val startBackupWordsModule = SingleLiveEvent<List<String>>()
    val startBackupPrivateKeyModule = SingleLiveEvent<String>()
    val startBackupEosModule = SingleLiveEvent<Pair<String, String>>()
    val closeLiveEvent = SingleLiveEvent<Void>()
    val showSuccessAndFinishEvent = SingleLiveEvent<Void>()

    fun init(account: Account) {
        BackupModule.init(this, this, account)
    }

    // router

    override fun showSuccessAndFinish() {
        showSuccessAndFinishEvent.call()
    }

    override fun startUnlockPinModule() {
        startPinModule.call()
    }

    override fun startBackupWordsModule(words: List<String>) {
        startBackupWordsModule.postValue(words)
    }

    override fun startBackupPrivateKeyModule(privateKey: String) {
        startBackupPrivateKeyModule.postValue(privateKey)
    }

    override fun startBackupEosModule(account: String, activePrivateKey: String) {
        startBackupEosModule.postValue(Pair(account, activePrivateKey))
    }

    override fun close() {
        closeLiveEvent.call()
    }
}
