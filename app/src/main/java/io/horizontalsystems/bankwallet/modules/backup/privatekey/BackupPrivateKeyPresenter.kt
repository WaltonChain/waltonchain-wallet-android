package io.horizontalsystems.bankwallet.modules.backup.privatekey

import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.core.helpers.HudHelper


class BackupPrivateKeyPresenter(val interactor: BackupPrivateKeyInteractor, private val router: BackupPrivateKeyModule.IRouter, val words: String)
    : BackupPrivateKeyModule.IPresenter, BackupPrivateKeyModule.IViewDelegate, BackupPrivateKeyModule.IInteractorDelegate {

    override var view: BackupPrivateKeyModule.IView? = null

    override fun viewDidLoad() {
        view?.showWords(words)
        view?.loadPage()
    }

    override fun onNextClick() {
        HudHelper.showSuccessMessage(R.string.Hud_Text_Private_Key_Copied)
    }

    override fun onBackClick() {
        router.close()
    }

    override fun onValidateSuccess() {

    }

    override fun onValidateFailure() {

    }

}
