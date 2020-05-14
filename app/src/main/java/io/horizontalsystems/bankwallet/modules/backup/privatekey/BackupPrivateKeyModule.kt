package io.horizontalsystems.bankwallet.modules.backup.privatekey

import androidx.appcompat.app.AppCompatActivity
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.modules.backup.words.BackupWordsActivity
import io.horizontalsystems.bankwallet.modules.backup.words.BackupWordsInteractor
import io.horizontalsystems.bankwallet.modules.backup.words.BackupWordsPresenter
import io.horizontalsystems.bankwallet.modules.backup.words.BackupWordsViewModel

object BackupPrivateKeyModule {
    const val RESULT_BACKUP = 1
    const val RESULT_SHOW = 2

    interface IView {
        fun showWords(words: String)

        fun loadPage()
        fun validateWords()
    }

    interface IPresenter : IInteractorDelegate, IViewDelegate {
        var view: IView?
    }

    interface IViewDelegate {
        fun viewDidLoad()
        fun onNextClick()
        fun onBackClick()
    }

    interface IInteractor {
    }

    interface IInteractorDelegate {
        fun onValidateSuccess()
        fun onValidateFailure()
    }

    interface IRouter {
        fun notifyBackedUp()
        fun notifyClosed()
        fun close()
    }

    //  helpers

    fun start(context: AppCompatActivity, words: String, backedUp: Boolean) {
        BackupPrivateKeyActivity.start(context, words, backedUp)
    }

    fun init(view: BackupPrivateKeyViewModel, router: IRouter, words: String) {
        val interactor = BackupPrivateKeyInteractor(words)
        val presenter = BackupPrivateKeyPresenter(interactor, router, words)

        view.delegate = presenter
        presenter.view = view
        interactor.delegate = presenter
    }
}
