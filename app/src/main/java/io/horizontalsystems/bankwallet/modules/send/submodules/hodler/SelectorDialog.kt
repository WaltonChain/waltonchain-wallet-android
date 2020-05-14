package io.horizontalsystems.bankwallet.modules.send.submodules.hodler

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.bankwallet.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_holder_item_selector.*

class SelectorDialog : DialogFragment(), SelectorAdapter.Listener {

    interface Listener {
        fun onSelectItem(position: Int)
    }

    private var listener: Listener? = null
    private var items = listOf<SelectorItem>()
    private var title: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.alert_background_themed)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)

        val view = inflater.inflate(R.layout.fragment_alert_dialog_single_select, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.dialogRecyclerView)
        val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)

        recyclerView.adapter = SelectorAdapter(items, this)
        recyclerView.layoutManager = LinearLayoutManager(context)

        dialogTitle.visibility = if (title == null) View.GONE else View.VISIBLE
        dialogTitle.text = title

        hideKeyBoard()

        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        showKeyBoard()
    }

    override fun onClick(position: Int) {
        listener?.onSelectItem(position)
        dismiss()
    }

    private fun showKeyBoard() {
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyBoard() {
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    companion object {
        fun newInstance(listener: Listener? = null, items: List<SelectorItem>, title: String?): SelectorDialog {
            val dialog = SelectorDialog()
            dialog.listener = listener
            dialog.items = items
            dialog.title = title
            return dialog
        }
    }

}

class SelectorAdapter(private val list: List<SelectorItem>,
                      private val listener: Listener) : RecyclerView.Adapter<SelectorOptionViewHolder>() {

    interface Listener {
        fun onClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SelectorOptionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_item_selector, parent, false), listener)

    override fun onBindViewHolder(holder: SelectorOptionViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size
}

class SelectorOptionViewHolder(override val containerView: View, private val listener: SelectorAdapter.Listener) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    init {
        containerView.setOnClickListener { listener.onClick(adapterPosition) }
    }

    fun bind(item: SelectorItem) {
        itemTitle.text = item.caption
        itemTitle.isSelected = item.selected
    }
}

data class SelectorItem(val caption: String, val selected: Boolean)

