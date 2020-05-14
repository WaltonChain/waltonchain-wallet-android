package io.horizontalsystems.bankwallet.modules.send.submodules.amount

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.entities.Wallet
import io.horizontalsystems.bankwallet.modules.send.SendModule
import io.horizontalsystems.bankwallet.modules.send.submodules.SendSubmoduleFragment
import kotlinx.android.synthetic.main.view_amount_input.*

class SendAmountFragment(
        private val wallet: Wallet,
        private val amountModuleDelegate: SendAmountModule.IAmountModuleDelegate,
        private val sendHandler: SendModule.ISendHandler)
    : SendSubmoduleFragment() {

    private lateinit var presenter: SendAmountPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.view_amount_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        presenter = ViewModelProvider(this, SendAmountModule.Factory(wallet, sendHandler))
                .get(SendAmountPresenter::class.java)
        val presenterView = presenter.view as SendAmountView
        presenter.moduleDelegate = amountModuleDelegate
        editTxtAmount.requestFocus()

        btnMax.setOnClickListener { presenter.onMaxClick() }
        btnSwitch.setOnClickListener { presenter.onSwitchClick() }

        presenterView.amountInputPrefix.observe(viewLifecycleOwner, Observer { prefix ->
            setPrefix(prefix)
        })

        presenterView.amount.observe(viewLifecycleOwner, Observer { amount ->
            setAmount(amount)
        })

        presenterView.availableBalance.observe(viewLifecycleOwner, Observer { amount ->
            setAvailableBalance(amount)
        })

        presenterView.hint.observe(viewLifecycleOwner, Observer { hint ->
            setHint(hint)
        })

        presenterView.maxButtonVisibleValue.observe(viewLifecycleOwner, Observer { visible ->
            setMaxButtonVisibility(visible)
        })

        presenterView.addTextChangeListener.observe(viewLifecycleOwner, Observer {
            enableAmountChangeListener()
        })

        presenterView.removeTextChangeListener.observe(viewLifecycleOwner, Observer {
            removeAmountChangeListener()
        })

        presenterView.revertAmount.observe(viewLifecycleOwner, Observer { amount ->
            revertAmount(amount)
        })

        presenterView.validationError.observe(viewLifecycleOwner, Observer {
            setValidationError(it)
        })

        presenterView.switchButtonEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            enableCurrencySwitch(enabled)
        })

        presenterView.setLoading.observe(viewLifecycleOwner, Observer { loading ->
            setLoading(loading)
        })
    }

    override fun init() {
        presenter.onViewDidLoad()
    }

    private fun setPrefix(prefix: String?) {
        topAmountPrefix.text = prefix
    }

    private fun setLoading(loading: Boolean) {
        availableBalanceValue.visibility = if (!loading) View.VISIBLE else View.INVISIBLE
        processSpinner.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setAmount(amount: String) {
        editTxtAmount.setText(amount)
        editTxtAmount.setSelection(editTxtAmount.text.length)
    }

    private fun setAvailableBalance(availableBalance: String) {
        availableBalanceValue.setText(availableBalance)
    }

    private fun setHint(hint: String?) {
        txtHintInfo.text = hint
    }

    private fun setMaxButtonVisibility(visible: Boolean) {
        // since the max button used to align amount field title it may be "invisible" not "gone"
        btnMax.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun enableAmountChangeListener() {
        editTxtAmount.addTextChangedListener(textChangeListener)
    }

    private fun removeAmountChangeListener() {
        editTxtAmount.removeTextChangedListener(textChangeListener)
    }

    private fun revertAmount(amount: String) {
        editTxtAmount.setText(amount)
        editTxtAmount.setSelection(amount.length)
        val shake = AnimationUtils.loadAnimation(context, R.anim.shake_edittext)
        editTxtAmount.startAnimation(shake)
    }

    private fun setValidationError(error: SendAmountModule.ValidationError?) {

        processSpinner.visibility = View.INVISIBLE
        txtHintError.visibility = if (error == null) View.GONE else View.VISIBLE
        txtHintInfo.visibility = if (error == null) View.VISIBLE else View.GONE

        txtHintError.text = when (error) {
            is SendAmountModule.ValidationError.InsufficientBalance -> {
                error.availableBalance?.let {
                    context?.getString(R.string.Send_Error_BalanceAmount, it.getFormatted())
                }
            }
            is SendAmountModule.ValidationError.TooFewAmount -> {
                error.minimumAmount?.let {
                    context?.getString(R.string.Send_Error_MinimumAmount, it.getFormatted())
                }
            }
            is SendAmountModule.ValidationError.MaxAmountLimit -> {
                error.maximumAmount?.let {
                    context?.getString(R.string.Send_Error_MaximumAmount, it.getFormatted())
                }
            }
            is SendAmountModule.ValidationError.NotEnoughForMinimumRequiredBalance -> {
                context?.getString(R.string.Send_Error_MinRequiredBalance,
                                   App.numberFormatter.format(error.minimumRequiredBalance))
            }
            else -> null
        }
    }

    private fun enableCurrencySwitch(enabled: Boolean) {
        btnSwitch.isEnabled = enabled
    }

    private val textChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val amountText = s?.toString() ?: ""
            presenter.onAmountChange(amountText)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}
