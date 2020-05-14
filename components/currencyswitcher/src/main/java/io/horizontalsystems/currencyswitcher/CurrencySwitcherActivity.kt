package io.horizontalsystems.currencyswitcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.core.CoreActivity
import io.horizontalsystems.core.setOnSingleClickListener
import io.horizontalsystems.views.LayoutHelper
import io.horizontalsystems.views.TopMenuItem
import io.horizontalsystems.views.ViewHolderProgressbar
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_currency_switcher.*

class CurrencySwitcherActivity : CoreActivity(), CurrencySwitcherAdapter.Listener {

    private lateinit var presenter: CurrencySwitcherPresenter
    private lateinit var presenterView: CurrencySwitcherView
    private lateinit var presenterRouter: CurrencySwitcherRouter
    private var adapter: CurrencySwitcherAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = ViewModelProvider(this, CurrencySwitcherModule.Factory()).get(CurrencySwitcherPresenter::class.java)
        presenterView = presenter.view as CurrencySwitcherView
        presenterRouter = presenter.router as CurrencySwitcherRouter

        setContentView(R.layout.activity_currency_switcher)

        shadowlessToolbar.bind(
                title = getString(R.string.SettingsCurrency_Title),
                leftBtnItem = TopMenuItem(R.drawable.ic_back, onClick = { onBackPressed() })
        )

        adapter = CurrencySwitcherAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        presenterView.currencyItems.observe(this, Observer { items ->
            adapter?.items = items
            adapter?.notifyDataSetChanged()
        })

        presenterRouter.closeLiveEvent.observe(this, Observer {
            finish()
        })

        presenter.viewDidLoad()
    }

    override fun onItemClick(position: Int) {
        presenter.didSelect(position)
    }
}

class CurrencySwitcherAdapter(private var listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ITEM = 1
    private val VIEW_TYPE_LOADING = 2

    interface Listener {
        fun onItemClick(position: Int)
    }

    var items = listOf<CurrencyViewItem>()

    override fun getItemCount() = if (items.isEmpty()) 1 else items.size

    override fun getItemViewType(position: Int): Int = if (items.isEmpty()) {
        VIEW_TYPE_LOADING
    } else {
        VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ITEM -> ViewHolderCurrency(inflater.inflate(ViewHolderCurrency.layoutResourceId, parent, false))
            else -> ViewHolderProgressbar(inflater.inflate(ViewHolderProgressbar.layoutResourceId, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderCurrency -> holder.bind(items[position]) { listener.onItemClick(position) }
        }
    }
}

class ViewHolderCurrency(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(item: CurrencyViewItem, onClick: () -> (Unit)) {
        val image = containerView.findViewById<ImageView>(R.id.image)
        val title = containerView.findViewById<TextView>(R.id.title)
        val subtitle = containerView.findViewById<TextView>(R.id.subtitle)
        val checkmarkIcon = containerView.findViewById<ImageView>(R.id.checkmarkIcon)

        containerView.setOnSingleClickListener { onClick.invoke() }
        image.setImageResource(LayoutHelper.getCurrencyDrawableResource(containerView.context, item.code.toLowerCase()))
        title.text = item.code
        subtitle.text = item.symbol
        checkmarkIcon.visibility = if (item.selected) View.VISIBLE else View.GONE
    }

    companion object {
        val layoutResourceId: Int
            get() = R.layout.view_holder_item_with_checkmark
    }
}
