package io.horizontalsystems.bankwallet.modules.send.submodules.address

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.entities.Coin
import io.horizontalsystems.bankwallet.modules.send.SendModule
import io.horizontalsystems.bankwallet.modules.send.submodules.SendSubmoduleFragment
import io.horizontalsystems.hodler.HodlerPlugin
import kotlinx.android.synthetic.main.view_address_input.*
import java.util.*

class SendAddressFragment(
        private val coin: Coin,
        private val editable: Boolean,
        private val addressModuleDelegate: SendAddressModule.IAddressModuleDelegate,
        private val sendHandler: SendModule.ISendHandler
) : SendSubmoduleFragment() {

    private lateinit var presenter: SendAddressPresenter

    private val addressChangeListener = object : TextWatcher {
        private var timer = Timer()
        private val DELAY: Long = 500 // milliseconds

        override fun afterTextChanged(s: Editable?) {
            timer.cancel()
            timer = Timer()
            timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            presenter.onManualAddressEnter(s?.toString() ?: "")
                        }
                    },
                    DELAY
            )
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.view_address_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        btnBarcodeScan.visibility = View.VISIBLE
        btnPaste.visibility = View.VISIBLE
        btnDeleteAddress.visibility = View.GONE

        presenter = ViewModelProvider(this, SendAddressModule.Factory(coin, editable, sendHandler))
                .get(SendAddressPresenter::class.java)
        val presenterView = presenter.view as SendAddressView
        presenter.moduleDelegate = addressModuleDelegate

        btnBarcodeScan.setOnClickListener { presenter.onAddressScanClicked() }
        btnPaste?.setOnClickListener { presenter.onAddressPasteClicked() }
        btnDeleteAddress?.setOnClickListener { presenter.onAddressDeleteClicked() }

        addAddressChangeListener()

        presenterView.addressText.observe(viewLifecycleOwner, Observer { address ->
            removeAddressChangeListener()
            txtAddressInput.setText(address)
            txtAddressInput.setSelection(txtAddressInput.text.count())
            addAddressChangeListener()

            val empty = address?.isEmpty() ?: true
            btnBarcodeScan.visibility = if (empty) View.VISIBLE else View.GONE
            btnPaste.visibility = if (empty) View.VISIBLE else View.GONE
            btnDeleteAddress.visibility = if (empty) View.GONE else View.VISIBLE
        })

        presenterView.error.observe(viewLifecycleOwner, Observer { error ->
            when (error) {
                null -> txtAddressError.visibility = View.GONE
                else -> {
                    txtAddressError.text = when (error) {
                        is HodlerPlugin.UnsupportedAddressType -> getString(R.string.Send_Error_UnsupportedAddress)
                        else -> getString(R.string.Send_Error_IncorrectAddress)
                    }
                    txtAddressError.visibility = View.VISIBLE
                }
            }
        })

        presenterView.addressInputEditable.observe(viewLifecycleOwner, Observer { editable ->
            txtAddressInput.focusable = if (editable) View.FOCUSABLE else View.NOT_FOCUSABLE
        })
    }

    private fun addAddressChangeListener() {
        txtAddressInput.addTextChangedListener(addressChangeListener)
    }

    private fun removeAddressChangeListener() {
        txtAddressInput.removeTextChangedListener(addressChangeListener)
    }

    override fun init() {
        presenter.onViewDidLoad()
    }

}
