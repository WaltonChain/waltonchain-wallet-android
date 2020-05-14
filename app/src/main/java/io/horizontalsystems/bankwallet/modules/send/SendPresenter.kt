package io.horizontalsystems.bankwallet.modules.send

import androidx.lifecycle.ViewModel
import io.horizontalsystems.bankwallet.modules.send.submodules.address.SendAddressModule
import io.horizontalsystems.bankwallet.modules.send.submodules.amount.SendAmountModule
import io.horizontalsystems.bankwallet.modules.send.submodules.fee.SendFeeModule
import io.horizontalsystems.bankwallet.modules.send.submodules.hodler.SendHodlerModule

class SendPresenter(
        private val interactor: SendModule.ISendInteractor,
        val router: SendModule.IRouter)
    : ViewModel(), SendModule.IViewDelegate, SendModule.ISendInteractorDelegate, SendModule.ISendHandlerDelegate {

    var amountModuleDelegate: SendAmountModule.IAmountModuleDelegate? = null
    var addressModuleDelegate: SendAddressModule.IAddressModuleDelegate? = null
    var feeModuleDelegate: SendFeeModule.IFeeModuleDelegate? = null
    var hodlerModuleDelegate: SendHodlerModule.IHodlerModuleDelegate? = null

    override lateinit var view: SendModule.IView
    override lateinit var handler: SendModule.ISendHandler

    // SendModule.IViewDelegate

    override fun onViewDidLoad() {
        view.loadInputItems(handler.inputItems)
    }

    override fun onModulesDidLoad() {
        handler.onModulesDidLoad()
    }

    override fun onAddressScan(address: String) {
        handler.onAddressScan(address)
    }

    override fun onProceedClicked() {
        view.showConfirmation(handler.confirmationViewItems())
    }

    override fun onSendConfirmed() {
        interactor.send(handler.sendSingle())
    }

    override fun onClear() {
        interactor.clear()

    }

    // ViewModel

    override fun onCleared() {
        interactor.clear()
        handler.onClear()
    }

    // SendModule.ISendInteractorDelegate

    override fun sync() {
        handler.sync()
    }

    override fun didSend() {
        router.closeWithSuccess()
    }

    override fun didFailToSend(error: Throwable) {
        view.showErrorInToast(error)
    }

    // SendModule.ISendHandlerDelegate

    override fun onChange(isValid: Boolean) {
        view.setSendButtonEnabled(isValid)
    }
}
