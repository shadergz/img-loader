package com.tool.imgloader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

enum class OnSelectedType {
    PRESSED,
    SELECTED,
    UNSELECTED
}

class AdapterItems(
    private var models: ArrayList<ItemModel>,
    val onSelected: (ItemModel, OnSelectedType) -> Unit
) : RecyclerView.Adapter<AdapterItems.ItemsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.dff_view, parent, false)
        return ItemsViewHolder(item)
    }

    override fun getItemCount() = models.size

    private var inSelectionMode: Boolean = false
    private lateinit var filter: ArrayList<ItemModel>

    private var blackList = ArrayList<ItemModel>()

    fun removeFilter() {
        blackList.forEach {
            models.add(it)
            notifyItemInserted(models.indexOf(it))
        }
        blackList.clear()
    }
    private fun applyFilter(filtered: ArrayList<ItemModel>) {
        filter = models.filterNot { filtered.contains(it) } as ArrayList<ItemModel>
        filter.forEach {
            notifyItemRemoved(models.indexOf(it))
            blackList.add(it)
            models.remove(it)
        }
    }

    fun filterList(filter: String) {
        val filterBy = filter.uppercase()
        val filtered = models.filter {
            it.dffName.uppercase().contains(filterBy)
        } as ArrayList<ItemModel>

        if (filtered.isNotEmpty()) {
            applyFilter(filtered)
        }
    }

    private fun enterInSelectionMode() {
        models.forEach {
            it.marked = 1
            notifyItemChanged(models.indexOf(it))
        }
        blackList.forEach {
            it.marked = 1
            notifyItemChanged(models.indexOf(it))
        }
        inSelectionMode = !inSelectionMode
    }

    private fun goOutFromSelectionMode() {
        models.forEach {
            it.marked = 0
            notifyItemChanged(models.indexOf(it))
        }
        blackList.forEach {
            it.marked = 0
            notifyItemChanged(models.indexOf(it))
        }
        inSelectionMode = !inSelectionMode
    }

    fun selectAll() {
        models.forEach {
            if (it.marked != 2) {
                it.marked = 2
                notifyItemChanged(models.indexOf(it))
            }
        }
        inSelectionMode = !inSelectionMode
    }

    fun unselectAll() {
        if (!inSelectionMode) return

        models.forEach {
            it.marked = 0
            notifyItemChanged(models.indexOf(it))
        }
        inSelectionMode = !inSelectionMode
    }

    private val selectedCount: Int
        get() {
            var countMarked = 0
            models.forEach {
                if (it.marked == 2)
                    countMarked++
            }
            blackList.forEach {
                if (it.marked == 2)
                    countMarked++
            }
            return countMarked
        }


    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {

        val modelItem = models[position]
        holder.dffName.text = modelItem.dffName
        holder.dffCheck.visibility = if (modelItem.marked >= 1) View.VISIBLE else View.INVISIBLE
        holder.dffCheck.isChecked = modelItem.marked > 1

        // Setting up the On Click event for all view!
        holder.dffCheck.apply {
            setOnClickListener {
                if (isChecked) {
                    modelItem.marked++
                    onSelected(modelItem, OnSelectedType.SELECTED)
                } else {
                    modelItem.marked--
                    onSelected(modelItem, OnSelectedType.UNSELECTED)
                    if (selectedCount == 0 && inSelectionMode)
                        goOutFromSelectionMode()
                }
            }
        }

        holder.itemView.setOnClickListener {
            onSelected(modelItem, OnSelectedType.PRESSED)
        }

        holder.itemView.setOnLongClickListener {
            // Enter in selection mode
            if (selectedCount == 0 && !inSelectionMode)
                enterInSelectionMode()
            if (modelItem.marked == 1) {
                modelItem.marked = 2
            } else if (modelItem.marked == 2) {
                modelItem.marked = 1
            }
            if (selectedCount == 0)
                goOutFromSelectionMode()
            notifyItemChanged(models.indexOf(modelItem))
            onSelected(modelItem, OnSelectedType.SELECTED)
            true
        }
    }

    class ItemsViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val dffName: TextView = item.findViewById(R.id.dffItemName)
        val dffCheck: CheckBox = item.findViewById(R.id.dffCheck)

    }
}