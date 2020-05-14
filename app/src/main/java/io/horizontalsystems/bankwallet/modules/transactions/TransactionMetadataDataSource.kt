package io.horizontalsystems.bankwallet.modules.transactions

import io.horizontalsystems.bankwallet.entities.Coin
import io.horizontalsystems.bankwallet.entities.CurrencyValue
import io.horizontalsystems.bankwallet.entities.LastBlockInfo
import io.horizontalsystems.bankwallet.entities.Wallet
import io.horizontalsystems.core.entities.Currency
import java.math.BigDecimal

class TransactionMetadataDataSource {

    private val lastBlockInfos = mutableMapOf<Wallet, LastBlockInfo>()
    private val thresholds = mutableMapOf<Wallet, Int>()
    private val rates = mutableMapOf<Coin, MutableMap<Long, CurrencyValue>>()

    fun setLastBlockInfo(lastBlockInfo: LastBlockInfo, wallet: Wallet) {
        lastBlockInfos[wallet] = lastBlockInfo
    }

    fun getLastBlockInfo(wallet: Wallet): LastBlockInfo? {
        return lastBlockInfos[wallet]
    }

    fun setConfirmationThreshold(confirmationThreshold: Int, wallet: Wallet) {
        thresholds[wallet] = confirmationThreshold
    }

    fun getConfirmationThreshold(wallet: Wallet): Int =
            thresholds[wallet] ?: 1

    fun setRate(rateValue: BigDecimal, coin: Coin, currency: Currency, timestamp: Long) {
        if (!rates.containsKey(coin)) {
            rates[coin] = mutableMapOf()
        }

        rates[coin]?.set(timestamp, CurrencyValue(currency, rateValue))
    }

    fun getRate(coin: Coin, timestamp: Long): CurrencyValue? {
        return rates[coin]?.get(timestamp)
    }

    fun clearRates() {
        rates.clear()
    }

}
