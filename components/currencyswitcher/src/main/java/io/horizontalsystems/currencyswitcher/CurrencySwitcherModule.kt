package io.horizontalsystems.currencyswitcher

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.core.CoreApp
import io.horizontalsystems.core.entities.Currency

object CurrencySwitcherModule {

    interface IView {
        fun show(items: List<CurrencyViewItem>)
    }

    interface IRouter {
        fun close()
    }

    interface IViewDelegate {
        fun viewDidLoad()
        fun didSelect(position: Int)
    }

    interface IInteractor {
        val currencies: List<Currency>
        var baseCurrency: Currency
    }

    fun start(context: Context) {
        val intent = Intent(context, CurrencySwitcherActivity::class.java)
        context.startActivity(intent)
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val view = CurrencySwitcherView()
            val router = CurrencySwitcherRouter()
            val interactor = CurrencySwitcherInteractor(CoreApp.currencyManager)
            val presenter = CurrencySwitcherPresenter(view, router, interactor)

            return presenter as T
        }
    }
}

data class CurrencyViewItem(val code: String, val symbol: String, val selected: Boolean) {

    override fun equals(other: Any?): Boolean {
        if (other is CurrencyViewItem) {
            return code == other.code
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + symbol.hashCode()
        result = 31 * result + selected.hashCode()
        return result
    }
}
