package io.horizontalsystems.bankwallet.modules.send.submodules.amount

import io.horizontalsystems.bankwallet.core.IAppNumberFormatter
import io.horizontalsystems.bankwallet.entities.Coin
import io.horizontalsystems.bankwallet.entities.CoinValue
import io.horizontalsystems.bankwallet.entities.CurrencyValue
import io.horizontalsystems.bankwallet.modules.send.SendModule
import io.horizontalsystems.core.entities.Currency
import java.math.BigDecimal
import java.math.RoundingMode

class SendAmountPresenterHelper(
        private val numberFormatter: IAppNumberFormatter,
        private val coin: Coin,
        private val baseCurrency: Currency,
        private val coinDecimal: Int,
        private val currencyDecimal: Int) {

    fun getAmount(coinAmount: BigDecimal?, inputType: SendModule.InputType, rate: BigDecimal?): String {
        val amount = when (inputType) {
            SendModule.InputType.COIN -> {
                coinAmount?.setScale(coinDecimal, RoundingMode.DOWN)
            }
            SendModule.InputType.CURRENCY -> {
                val currencyAmount = rate?.let { coinAmount?.times(it) }
                currencyAmount?.setScale(currencyDecimal, RoundingMode.DOWN)
            }
        } ?: BigDecimal.ZERO

        return if (amount > BigDecimal.ZERO) amount.stripTrailingZeros().toPlainString() else ""
    }


    fun getHint(coinAmount: BigDecimal? = null, inputType: SendModule.InputType, rate: BigDecimal?): String? {
        return when (inputType) {
            SendModule.InputType.CURRENCY -> {
                numberFormatter.format(CoinValue(coin, coinAmount ?: BigDecimal.ZERO), realNumber = true)
            }
            SendModule.InputType.COIN -> {
                rate?.let {
                    numberFormatter.format(CurrencyValue(baseCurrency, coinAmount?.times(it) ?: BigDecimal.ZERO))
                }
            }
        }
    }

    fun getAvailableBalance(coinAmount: BigDecimal? = null, inputType: SendModule.InputType, rate: BigDecimal?): String? {
        return when (inputType) {
            SendModule.InputType.CURRENCY -> {
                rate?.let {
                    numberFormatter.format(CurrencyValue(baseCurrency, coinAmount?.times(it) ?: BigDecimal.ZERO))
                }
            }
            SendModule.InputType.COIN -> {
                numberFormatter.format(CoinValue(coin, coinAmount ?: BigDecimal.ZERO), realNumber = true)
            }
        }
    }

    fun getAmountPrefix(inputType: SendModule.InputType, rate: BigDecimal?): String? {
        return when {
            inputType == SendModule.InputType.COIN -> coin.code
            rate == null -> null
            else -> baseCurrency.symbol
        }
    }

    fun getCoinAmount(amount: BigDecimal?, inputType: SendModule.InputType, rate: BigDecimal?): BigDecimal? {
        return when (inputType) {
            SendModule.InputType.CURRENCY -> rate?.let { amount?.divide(it, coinDecimal, RoundingMode.CEILING) }
            else -> amount
        }
    }

    fun decimal(inputType: SendModule.InputType) = if (inputType == SendModule.InputType.COIN) coinDecimal else currencyDecimal

}
