package com.rap.sheet.utilitys

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.rap.sheet.model.ContactDetail.ContactDetailCommentModel
import java.util.*


abstract class SwipeHelperDelete @SuppressLint("ClickableViewAccessibility") protected constructor(private val context: Context?, private val recyclerView: RecyclerView?, private val contactDetailCommentModelList: MutableList<ContactDetailCommentModel>, private val userID: String) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    private var buttons: MutableList<SwipeHelperDelete.UnderlayButton> = mutableListOf()
    private val gestureDetector: GestureDetector
    private var swipedPos: Int = -1
    private var swipeThreshold: Float = 0.5f
    private val buttonsBuffer: MutableMap<Int, MutableList<SwipeHelperDelete.UnderlayButton>> = mutableMapOf()
    private var recoverQueue: Queue<Int>? = null
    private var clickRegion: RectF? = null
    private var posDelete: Int = -1

    private val gestureListener: GestureDetector.SimpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            Log.i("TAG", "onSingleTapConfirmed: " + buttons.size)
            for (button: UnderlayButton in buttons) {
                if (button.onClick(e.x, e.y)) return true
            }
            return false
        }
        //        @Override
        //        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //            for (UnderlayButton button : buttons) {
        //                if (button.onClick(e1.getX(), e2.getY()))
        //                    break;
        //            }
        //            return super.onScroll(e1, e2, distanceX, distanceY);
        //        }
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val pos: Int = viewHolder.adapterPosition
        Log.i("TAG", "onSwiped: " + pos)
        if (swipedPos != pos) recoverQueue!!.add(swipedPos)
        swipedPos = pos
        if (buttonsBuffer.containsKey(swipedPos)) buttons = buttonsBuffer.get(swipedPos)!! else buttons.clear()
        buttonsBuffer.clear()
        swipeThreshold = 0.5f * buttons.size * BUTTON_WIDTH
        recoverSwipedItem()
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        var pos: Int = viewHolder.adapterPosition
        var translationX: Float = dX
        val itemView: View = viewHolder.itemView
        posDelete = pos
        if (pos < 0) {
            swipedPos = posDelete
            return
        }
        if (contactDetailCommentModelList[pos].userId == userID) {
            Log.i("TAG", "initView: " + contactDetailCommentModelList.get(pos).userId)
            Log.i("TAG", "onChildDraw:33 " + pos)
            posDelete = pos

            Log.i("TAG", "onChildDraw:55 " + isCurrentlyActive)
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                if (dX < 0) {
                    var buffer: MutableList<UnderlayButton>? = ArrayList()

//                if (pos == 1) {
//                    instantiateUnderlayButton(viewHolder, buffer)
//                    buttonsBuffer.put(pos, buffer!!)
//                }

                    if (!buttonsBuffer.containsKey(posDelete)) {
                        instantiateUnderlayButton(viewHolder, buffer)
                        buttonsBuffer.put(posDelete, buffer!!)
                        Log.i("TAG", "onChildDraw: ")
                    } else {
                        buffer = buttonsBuffer.get(posDelete)
                        Log.i("TAG", "onChildDraw:11 ")
                    }

                    translationX = dX * buffer!!.size * BUTTON_WIDTH / itemView.width
                    drawButtons(c, itemView, buffer, posDelete, translationX)
                }
            }
        } else {
            swipedPos = posDelete
            return
        }
        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive)
    }

    @Synchronized
    private fun recoverSwipedItem() {
        while (!recoverQueue!!.isEmpty()) {
            val pos: Int = recoverQueue!!.poll()
            if (pos > -1) {
                recyclerView?.adapter!!.notifyItemChanged(pos)
            }
        }
    }

    private fun drawButtons(c: Canvas, itemView: View, buffer: List<UnderlayButton>?, pos: Int, dX: Float) {
        var right: Float = itemView.right.toFloat()
        val dButtonWidth: Float = (-1) * dX / buffer!!.size
        for (button: UnderlayButton in buffer) {
            val left: Float = right - dButtonWidth
            button.onDraw(
                    c,
                    RectF(
                            left,
                            itemView.top.toFloat(),
                            right,
                            itemView.bottom.toFloat()
                    ),
                    pos
            )
            right = left
        }
    }

    fun attachSwipe() {
        val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    abstract fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder?, underlayButtons: MutableList<UnderlayButton>?)
    inner class UnderlayButton constructor(private val text: String, private val imageResId: Int, private val color: Int, private val visible1: Boolean, private val clickListener: UnderlayButtonClickListener) {
        var icon = BitmapFactory.decodeResource(context!!.resources,
                imageResId)

        private var pos: Int = 0
        var visible = visible1
        fun onClick(x: Float, y: Float): Boolean {
            if (clickRegion != null && clickRegion!!.contains(x, y)) {
                clickListener.onClick(pos)
                return true
            }
            return false
        }


        fun onDraw(c: Canvas, rect: RectF, pos: Int) {
            val p: Paint = Paint()

            // Draw background
            p.color = color
            c.drawRect(rect, p)
//            p.typeface = ResourcesCompat.getFont((context)!!, R.font.avenir_medium)
//            // Draw Text
//            p.color = Color.WHITE
//            p.textSize = 30f
            val r: Rect = Rect()
            val cHeight: Float = rect.height()
            val cWidth: Float = rect.width()
            p.textAlign = Paint.Align.LEFT
            p.getTextBounds(text, 0, text.length, r)
            val x: Float = (cWidth / 2f) - (r.width() / 2f) - r.left
            val y: Float = cHeight / 2f + r.height() / 2f - r.bottom
            val centreX = (cWidth  - icon.width) /2
            val centreY = (cHeight - icon.height) /2
//            Log.i("TAG", "onDraw: " + rect.left + x)
//            Log.i("TAG", "onDraw: " + rect.top + y)
//            Log.i("TAG", "onDraw: " + centreX)
//            Log.i("TAG", "onDraw: " + centreY)
////            c.drawText(text, rect.left + x, rect.top + y, p)
//            Log.i("TAG", "button: " + icon)
            c.drawBitmap(icon, rect.left+centreX, rect.top+centreY , p)

            clickRegion = rect
            this.pos = pos
        }

    }

    open interface UnderlayButtonClickListener {
        fun onClick(pos: Int)
    }

    companion object {
        val BUTTON_WIDTH: Int = 250
    }

    init {
        buttons = ArrayList()
        gestureDetector = GestureDetector(gestureListener)
        recyclerView!!.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (swipedPos < 0) return false
                val point: Point = Point(e.rawX.toInt(), e.rawY.toInt())
                val swipedViewHolder: RecyclerView.ViewHolder? = recyclerView.findViewHolderForAdapterPosition(swipedPos)
                if (swipedViewHolder != null) {
                    val swipedItem: View = swipedViewHolder.itemView
                    val rect: Rect = Rect()
                    swipedItem.getGlobalVisibleRect(rect)
                    Log.i("TAG", "onInterceptTouchEvent: " + swipedViewHolder.position)
                    if ((e.action == MotionEvent.ACTION_DOWN) || (e.action == MotionEvent.ACTION_UP) || (e.action == MotionEvent.ACTION_MOVE)) {
                        if (rect.top < point.y && rect.bottom > point.y) gestureDetector.onTouchEvent(e) else {
                            recoverQueue!!.add(swipedPos)
                            swipedPos = -1
                            clickRegion = null
                            recoverSwipedItem()
                        }
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })

        recoverQueue = object : LinkedList<Int>() {
            override fun add(o: Int): Boolean {
                return if (contains(o)) false else super.add(o)
            }
        }
//        attachSwipe();
    }

}