package com.rap.sheet.utilitys

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.rap.sheet.adapter.MyContactAdapter

/**
 * Created by mvayak on 19-07-2018.
 */

class RecyclerItemTouchHelperContact constructor(dragDirs: Int, swipeDirs: Int, private val listener: RecyclerItemTouchHelperListener) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            val foregroundView: View? = (viewHolder as MyContactAdapter.ViewHolder).binding.linearLayoutContactRow
            ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(foregroundView)
        }
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView,
                                        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                                        actionState: Int, isCurrentlyActive: Boolean) {
        val foregroundView: View? = (viewHolder as MyContactAdapter.ViewHolder).binding.linearLayoutContactRow
        ItemTouchHelper.Callback.getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val foregroundView: View? = (viewHolder as MyContactAdapter.ViewHolder).binding.linearLayoutContactRow
        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(foregroundView)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                                    viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                                    actionState: Int, isCurrentlyActive: Boolean) {
        val foregroundView: View? = (viewHolder as MyContactAdapter.ViewHolder).binding.linearLayoutContactRow
        ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
    }

    interface RecyclerItemTouchHelperListener {
        fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int)
    }

}