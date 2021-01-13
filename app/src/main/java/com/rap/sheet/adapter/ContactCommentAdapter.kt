package com.rap.sheet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rap.sheet.application.BaseApplication
import com.rap.sheet.model.ContactDetail.ContactDetailCommentModel
import com.rap.sheet.R
import com.rap.sheet.databinding.CommentRowViewBinding
import com.rap.sheet.extenstion.beGone
import com.rap.sheet.extenstion.beVisible
import com.rap.sheet.extenstion.click
import com.rap.sheet.utilitys.BindingHolder
import com.rap.sheet.utilitys.Utility

class ContactCommentAdapter(private val commentModelList: List<ContactDetailCommentModel>?,
                            private val isComment: Boolean,
                            private val userId: String,
                            private val onDeleteClick: (Int) -> Unit = { _ -> }
) : RecyclerView.Adapter<BindingHolder<CommentRowViewBinding>>() {

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): BindingHolder<CommentRowViewBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: CommentRowViewBinding =
                DataBindingUtil.inflate(inflater, R.layout.comment_row_view, parent, false)
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return commentModelList!!.size
    }

//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
//        val textViewComment: TextView
//        val textViewCommentDate: TextView
//        val imageViewDelete: ImageView
//        override fun onClick(v: View) {
//            if (v.id == imageViewDelete.id) {
//                if (recyclerViewItemClick != null) {
//                    recyclerViewItemClick!!.onItemClick(adapterPosition, v)
//                }
//            }
//        }
//
//        init {
//            imageViewDelete = itemView.findViewById(R.id.imageViewDelete)
//            textViewComment = itemView.findViewById(R.id.textViewComment)
//            textViewCommentDate = itemView.findViewById(R.id.textViewCommentDate)
//            imageViewDelete.setOnClickListener(this)
//        }
//    }

    override fun onBindViewHolder(holder: BindingHolder<CommentRowViewBinding>, position: Int) {
        holder.binding.textViewComment.text = commentModelList!![position].message?.trim { it <= ' ' }
        holder.binding.textViewCommentDate.text = Utility.getLeftTime(commentModelList[position].createdAt, "yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy")
        holder.binding.imageViewDelete.click {
            onDeleteClick(position)
        }
        if (!isComment) {
            if (commentModelList[position].userId == userId) {
                holder.binding.imageViewDelete.beVisible()
            } else {
                holder.binding.imageViewDelete.beGone()
            }
        }
    }

}