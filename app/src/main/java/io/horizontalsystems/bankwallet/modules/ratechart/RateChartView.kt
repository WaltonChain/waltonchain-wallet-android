package io.horizontalsystems.bankwallet.modules.ratechart

import io.horizontalsystems.core.SingleLiveEvent
import io.horizontalsystems.bankwallet.entities.CurrencyValue
import io.horizontalsystems.xrateskit.entities.ChartType

class RateChartView : RateChartModule.View {
    val showSpinner = SingleLiveEvent<Unit>()
    val hideSpinner = SingleLiveEvent<Unit>()
    val setDefaultMode = SingleLiveEvent<ChartType>()
    val setSelectedPoint = SingleLiveEvent<ChartPointViewItem>()
    val showChartInfo = SingleLiveEvent<ChartInfoViewItem>()
    val showMarketInfo = SingleLiveEvent<MarketInfoViewItem>()
    val showError = SingleLiveEvent<Throwable>()

    override fun showSpinner() {
        showSpinner.call()
    }

    override fun hideSpinner() {
        hideSpinner.call()
    }

    override fun setChartType(type: ChartType) {
        setDefaultMode.postValue(type)
    }

    override fun showChartInfo(viewItem: ChartInfoViewItem) {
        showChartInfo.postValue(viewItem)
    }

    override fun showMarketInfo(viewItem: MarketInfoViewItem) {
        showMarketInfo.postValue(viewItem)
    }

    override fun showSelectedPoint(item: ChartPointViewItem) {
        setSelectedPoint.postValue(item)
    }

    override fun showError(ex: Throwable) {
        showError.postValue(ex)
    }
}
