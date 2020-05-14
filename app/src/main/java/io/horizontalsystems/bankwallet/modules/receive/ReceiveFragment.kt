package io.horizontalsystems.bankwallet.modules.receive

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.entities.Wallet
import io.horizontalsystems.bankwallet.ui.extensions.BaseBottomSheetDialogFragment
import io.horizontalsystems.bankwallet.ui.helpers.TextHelper
import io.horizontalsystems.core.helpers.HudHelper
import io.horizontalsystems.views.LayoutHelper
import kotlinx.android.synthetic.main.view_bottom_sheet_receive.*

class ReceiveFragment: BaseBottomSheetDialogFragment() {

    private var listener: Listener? = null

    interface Listener {
        fun shareReceiveAddress(address: String)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            //close fragment in case it's restoring
            dismiss()
        }

        setContentView(R.layout.view_bottom_sheet_receive)

        val wallet = arguments?.getParcelable<Wallet>(keyWallet) ?: run { dismiss(); return }

        setTitle(activity?.getString(R.string.Deposit_Title, wallet.coin.code))
        setSubtitle(wallet.coin.title)
        context?.let { setHeaderIcon(LayoutHelper.getCoinDrawableResource(it, wallet.coin.code)) }

        val presenter = ViewModelProvider(this, ReceiveModule.Factory(wallet)).get(ReceivePresenter::class.java)
        observeView(presenter.view as ReceiveView)
        observeRouter(presenter.router as ReceiveRouter)
        presenter.viewDidLoad()

        btnShare.setOnClickListener { presenter.onShareClick() }
        receiveAddressView.setOnClickListener { presenter.onAddressClick() }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    private fun observeRouter(receiveRouter: ReceiveRouter) {
        receiveRouter.shareAddress.observe(viewLifecycleOwner, Observer {
            listener?.shareReceiveAddress(it)
        })
    }

    private fun observeView(view: ReceiveView) {
        view.setHintText.observe(viewLifecycleOwner, Observer {
            receiverHint.setText(it)

            view.hintDetails?.let {
                receiverHint.text = "${receiverHint.text} (${it})"
            }
        })

        view.showAddress.observe(viewLifecycleOwner, Observer {
            receiveAddressView.text = it.address
            imgQrCode.setImageBitmap(TextHelper.getQrCodeBitmap(it.address))
        })

        view.showError.observe(viewLifecycleOwner, Observer { error ->
            error?.let { HudHelper.showErrorMessage(it) }
            dismiss()
        })

        view.showCopied.observe(viewLifecycleOwner, Observer {
            HudHelper.showSuccessMessage(R.string.Hud_Text_Copied)
        })
    }

    companion object {
        private const val keyWallet = "wallet_key"
        fun newInstance(wallet: Wallet) = ReceiveFragment().apply {
            arguments = Bundle(1).apply {
                putParcelable(keyWallet, wallet)
            }
        }
    }

}
