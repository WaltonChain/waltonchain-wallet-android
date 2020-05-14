package io.horizontalsystems.bankwallet.modules.restore.words

import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.InvalidMnemonicWordsCountException

class RestoreWordsPresenter(
        private val wordsCount: Int,
        private val interactor: RestoreWordsModule.Interactor,
        private val router: RestoreWordsModule.Router)
    : RestoreWordsModule.ViewDelegate, RestoreWordsModule.InteractorDelegate {

    var view: RestoreWordsModule.View? = null

    override var privateKey: String = ""
    //  IView Delegate

    override val words = mutableListOf<String>()

    override fun onDone(wordsString: String?) {
        val wordList = wordsString?.split(" ")
        if (wordList != null && wordList.size == wordsCount) {
            words.clear()
            words.addAll(wordList)
            validate()
        } else {
            wordsString?.let {
                privateKey = wordsString
                interactor.validatePrivateKey(privateKey)
            }
        }
    }

    //  Interactor Delegate

    override fun didValidate() {
        router.notifyRestored()
    }

    override fun didFailToValidate(exception: Exception) {
        view?.showError(R.string.Restore_ValidationFailed)
    }

    private fun validate(){
        if (words.size != wordsCount) {
            didFailToValidate(InvalidMnemonicWordsCountException())
        } else {
            interactor.validate(words)
        }
    }
}
