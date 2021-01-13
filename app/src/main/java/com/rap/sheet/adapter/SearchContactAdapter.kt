package com.rap.sheet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rap.sheet.model.SearchConatct.SearchContactDataModel
import com.rap.sheet.R
import com.rap.sheet.databinding.SearchContactRowViewBinding
import com.rap.sheet.extenstion.beGone
import com.rap.sheet.extenstion.click
import com.rap.sheet.utilitys.BindingHolder
import com.rap.sheet.utilitys.Utility

class SearchContactAdapter(private val searchContactDataModelsList: MutableList<SearchContactDataModel>,
                           private val onItemClick: (Int) -> Unit = { _ -> }, private val onPopUpClick: (Int,View) -> Unit = { _,_ -> })
    : RecyclerView.Adapter<BindingHolder<SearchContactRowViewBinding>>() {
//    var filterContacts: List<ContactModel?>?

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): BindingHolder<SearchContactRowViewBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: SearchContactRowViewBinding =
                DataBindingUtil.inflate(inflater, R.layout.search_contact_row_view, parent, false)
        return BindingHolder(binding)
    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.textViewContactName.text = Utility.capitalize(searchContactDataModelsList!![position].firstName + " " + searchContactDataModelsList[position].lastName)
//        // holder.ratingBarContactRating.setRating(Float.valueOf(searchContactDataModelsList.get(position).getAvgRate()));
//        val stringBuilderPhone = StringBuilder(searchContactDataModelsList[position].number.replace("-".toRegex(), ""))
//        if (stringBuilderPhone.length > 3) {
//            stringBuilderPhone.insert(3, "-")
//        }
//        if (stringBuilderPhone.length > 7) {
//            stringBuilderPhone.insert(7, "-")
//        }
//        holder.textViewContactNumber.text = stringBuilderPhone.toString()
//    }

//    fun itemClickListener(recyclerViewItemClick: RecyclerViewItemClickInterface?) {
//        this.recyclerViewItemClick = recyclerViewItemClick
//    }
//
//    fun itemPopUpClickListener(recyclerViewItemClick: RecyclerViewItemClickInterface?) {
//        recyclerViewItemPopUpClickListener = recyclerViewItemClick
//    }

    override fun getItemCount(): Int {
        return searchContactDataModelsList.size
    }

//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
//        private val imageViewPopUp: ImageView
//        val textViewContactName: TextView
//        val textViewContactNumber: TextView
//
//        //  private RatingBar ratingBarContactRating;
//        var linearLayoutContactRow: LinearLayout
//        override fun onClick(v: View) {
//            if (v.id == linearLayoutContactRow.id) {
//                if (recyclerViewItemClick != null) {
//                    recyclerViewItemClick!!.onItemClick(adapterPosition, v)
//                }
//            }
//            if (v.id == imageViewPopUp.id) {
//                if (recyclerViewItemPopUpClickListener != null) {
//                    recyclerViewItemPopUpClickListener!!.onItemClick(adapterPosition, v)
//                }
//            }
//        }
//
//        //        private SwipeLayout swipeLayout;
//        //        private ImageView delete_icon;
//        init {
//            //            delete_icon=itemView.findViewById(R.id.delete_icon);
////            swipeLayout=itemView.findViewById(R.id.swipeLayout);
//            imageViewPopUp = itemView.findViewById(R.id.imageViewPopUp)
//            linearLayoutContactRow = itemView.findViewById(R.id.linearLayoutContactRow)
//            textViewContactName = itemView.findViewById(R.id.textViewContactName)
//            textViewContactNumber = itemView.findViewById(R.id.textViewContactNumber)
//            //    ratingBarContactRating = itemView.findViewById(R.id.ratingBarContactRating);
//            linearLayoutContactRow.setOnClickListener(this)
//            imageViewPopUp.setOnClickListener(this)
//            //  delete_icon.setOnClickListener(this);
//        }
//    }

    fun removeItem(position: Int) {
        searchContactDataModelsList.removeAt(position)
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position)
    }

    fun restoreItem(item: SearchContactDataModel, position: Int) {
        searchContactDataModelsList.add(position, item)
        // notify item added by position
        notifyItemInserted(position)
    }

    override fun onBindViewHolder(holder: BindingHolder<SearchContactRowViewBinding>, position: Int) {
        val data = searchContactDataModelsList[position]
        data.apply {
            holder.binding.textViewContactName.text = Utility.capitalize(this.firstName.plus(" ").plus(this.lastName))
            //holder.ratingBarContactRating.setRating(Float.valueOf(contactModelList.get(position).getAvgRate()));
            val stringBuilderPhone = StringBuilder(this.number.toString().replace("-".toRegex(), ""))
            if (stringBuilderPhone.length > 3) {
                stringBuilderPhone.insert(3, "-")
            }
            if (stringBuilderPhone.length > 7) {
                stringBuilderPhone.insert(7, "-")
            }
            holder.binding.textViewContactNumber.text = stringBuilderPhone.toString()
            holder.binding.imageViewPopUp.beGone()

            holder.binding.linearLayoutContactRow.click {
                onItemClick(holder.adapterPosition)
            }
            holder.binding.imageViewPopUp.click {
                onPopUpClick(holder.adapterPosition,it)
            }
        }
    }

}