package io.horizontalsystems.bankwallet.modules.backup.privatekey

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.core.SingleLiveEvent

class BackupPrivateKeyViewModel : ViewModel(), BackupPrivateKeyModule.IView, BackupPrivateKeyModule.IRouter {

    lateinit var delegate: BackupPrivateKeyModule.IViewDelegate

    val loadPageLiveEvent = SingleLiveEvent<Unit>()
    val wordsLiveData = MutableLiveData<String>()
    val validateWordsLiveEvent = SingleLiveEvent<Unit>()
    val notifyBackedUpEvent = SingleLiveEvent<Unit>()
    val notifyClosedEvent = SingleLiveEvent<Unit>()
    val closeLiveEvent = SingleLiveEvent<Unit>()

    fun init(words: String, backedUp: Boolean) {
        BackupPrivateKeyModule.init(this, this, words)
    }

    override fun showWords(words: String) {
        wordsLiveData.value = words
    }

    // view

    override fun loadPage() {
        loadPageLiveEvent.call()
    }

    override fun validateWords() {
        validateWordsLiveEvent.call()
    }

    // router

    override fun notifyBackedUp() {
        notifyBackedUpEvent.call()
    }

    override fun notifyClosed() {
        notifyClosedEvent.call()
    }

    override fun close() {
        closeLiveEvent.call()
    }

}
