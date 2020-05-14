package io.horizontalsystems.bankwallet.modules.balance

import io.horizontalsystems.bankwallet.core.*
import io.horizontalsystems.bankwallet.entities.PredefinedAccountType
import io.horizontalsystems.bankwallet.entities.Wallet
import io.horizontalsystems.core.ICurrencyManager
import io.horizontalsystems.core.entities.Currency
import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.ChartType
import io.horizontalsystems.xrateskit.entities.MarketInfo
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

class BalanceInteractor(
        private val walletManager: IWalletManager,
        private val adapterManager: IAdapterManager,
        private val currencyManager: ICurrencyManager,
        private val localStorage: ILocalStorage,
        private val rateManager: IRateManager,
        private val predefinedAccountTypeManager: IPredefinedAccountTypeManager)
    : BalanceModule.IInteractor {

    var delegate: BalanceModule.IInteractorDelegate? = null

    private var disposables = CompositeDisposable()
    private var adapterDisposables = CompositeDisposable()
    private var marketInfoDisposables = CompositeDisposable()

    override val wallets: List<Wallet>
        get() = walletManager.wallets

    override val baseCurrency: Currency
        get() = currencyManager.baseCurrency

    override val sortType: BalanceSortType
        get() = localStorage.sortType

    override fun marketInfo(coinCode: String, currencyCode: String): MarketInfo? {
        return rateManager.marketInfo(coinCode, currencyCode)
    }

    override fun chartInfo(coinCode: String, currencyCode: String): ChartInfo? {
        return rateManager.chartInfo(coinCode, currencyCode, ChartType.DAILY)
    }

    override fun balance(wallet: Wallet): BigDecimal? {
        return adapterManager.getBalanceAdapterForWallet(wallet)?.balance
    }

    override fun balanceLocked(wallet: Wallet): BigDecimal? {
        return adapterManager.getBalanceAdapterForWallet(wallet)?.balanceLocked
    }

    override fun state(wallet: Wallet): AdapterState? {
        return adapterManager.getBalanceAdapterForWallet(wallet)?.state
    }

    override fun subscribeToWallets() {
        walletManager.walletsUpdatedObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { wallets ->
                    onUpdateWallets(wallets)
                }.let {
                    disposables.add(it)
                }

        adapterManager.adaptersReadyObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    onAdaptersReady()
                }.let {
                    disposables.add(it)
                }
    }

    override fun subscribeToBaseCurrency() {
        currencyManager.baseCurrencyUpdatedSignal
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { onUpdateCurrency() }
                .let { disposables.add(it) }
    }

    override fun subscribeToAdapters(wallets: List<Wallet>) {
        adapterDisposables.clear()

        for (wallet in wallets) {
            val adapter = adapterManager.getBalanceAdapterForWallet(wallet) ?: continue

            adapter.balanceUpdatedFlowable
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe {
                        delegate?.didUpdateBalance(wallet, adapter.balance, adapter.balanceLocked)
                    }.let {
                        adapterDisposables.add(it)
                    }

            adapter.stateUpdatedFlowable
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe {
                        delegate?.didUpdateState(wallet, adapter.state)
                    }.let {
                        adapterDisposables.add(it)
                    }
        }
    }

    override fun subscribeToMarketInfo(currencyCode: String) {
        marketInfoDisposables.clear()

        rateManager.marketInfoObservable(currencyCode)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    delegate?.didUpdateMarketInfo(it)
                }.let {
                    marketInfoDisposables.add(it)
                }
    }

    override fun refresh() {
        adapterManager.refresh()
        rateManager.refresh()

        delegate?.didRefresh()
    }

    override fun predefinedAccountType(wallet: Wallet): PredefinedAccountType? {
        return predefinedAccountTypeManager.predefinedAccountType(wallet.account.type)
    }

    override fun saveSortType(sortType: BalanceSortType) {
        localStorage.sortType = sortType
    }

    override fun clear() {
        disposables.clear()
        adapterDisposables.clear()
        marketInfoDisposables.clear()
    }

    private fun onUpdateCurrency() {
        delegate?.didUpdateCurrency(currencyManager.baseCurrency)
    }

    private fun onUpdateWallets(wallets: List<Wallet>) {
        delegate?.didUpdateWallets(wallets)
    }

    private fun onAdaptersReady() {
        delegate?.didPrepareAdapters()
    }

}
