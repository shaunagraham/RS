package com.rap.sheet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rap.sheet.R
import com.rap.sheet.databinding.SearchContactRowViewBinding
import com.rap.sheet.extenstion.click
import com.rap.sheet.model.MyContact.ContactModel
import com.rap.sheet.utilitys.Utility

class MyContactAdapter(private val contactModelList: MutableList<ContactModel>,
                       private val onItemClick: (Int) -> Unit = { _ -> })
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    var filterContacts: List<ContactModel?>?

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): RecyclerView.ViewHolder {
        val binding: SearchContactRowViewBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.search_contact_row_view,
                parent,
                false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return contactModelList.size
    }

    inner class ViewHolder(var binding: SearchContactRowViewBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ContactModel) {
            binding.textViewContactName.text = Utility.capitalize(data.firstName.plus(" ").plus(data.lastName))
            //ratingBarContactRating.setRating(Float.valueOf(contactModelList.get(position).getAvgRate()));
            val stringBuilderPhone = StringBuilder(data.number.toString().replace("-".toRegex(), ""))
            if (stringBuilderPhone.length > 3) {

                stringBuilderPhone.insert(3, "-")
            }
            if (stringBuilderPhone.length > 7) {
                stringBuilderPhone.insert(7, "-")
            }
            binding.textViewContactNumber.text = stringBuilderPhone.toString()
            binding.imageViewPopUp.visibility = View.GONE

            binding.linearLayoutContactRow.click {
                onItemClick(adapterPosition)
            }
        }

    }

//    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
//        val textViewContactName: TextView
//        val textViewContactNumber: TextView
//        val imageViewPopUp: ImageView
//        var linearLayoutContactRow: LinearLayout
//        override fun onClick(v: View) {
//            if (v.id == linearLayoutContactRow.id) {
//                if (recyclerViewItemClick != null) {
//                    recyclerViewItemClick!!.onItemClick(adapterPosition, v)
//                }
//            }
//            //            if (v.getId() == delete_icon.getId()) {
////                if (recyclerViewItemDeleteClickListener != null) {
////                    recyclerViewItemDeleteClickListener.onItemClick(getAdapterPosition(), swipeLayout);
////                }
////            }
//        }
//
//        init {
//            imageViewPopUp = itemView.findViewById(R.id.imageViewPopUp)
//            linearLayoutContactRow = itemView.findViewById(R.id.linearLayoutContactRow)
//            textViewContactName = itemView.findViewById(R.id.textViewContactName)
//            textViewContactNumber = itemView.findViewById(R.id.textViewContactNumber)
//            //    ratingBarContactRating = itemView.findViewById(R.id.ratingBarContactRating);
//            linearLayoutContactRow.setOnClickListener(this)
//        }
//    }
//
//    override fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(charSequence: CharSequence): FilterResults {
//                val charString = charSequence.toString()
//                if (charString.isEmpty()) {
//                    filterContacts = contactModelList
//                } else {
//                    val filteredList: MutableList<ContactModel?> = ArrayList()
//                    for (row in contactModelList!!) {
//
//                        // name match condition. this might differ depending on your requirement
//                        // here we are looking for name or phone number match
//                        if (row.getFirstName().toLowerCase().contains(charString.toLowerCase()) || row.getLastName().contains(charSequence) || row.getNumber().contains(charSequence)) {
//                            filteredList.add(row)
//                        }
//                    }
//                    filterContacts = filteredList
//                }
//                val filterResults = FilterResults()
//                filterResults.values = filterContacts
//                return filterResults
//            }
//
//            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
//                filterContacts = filterResults.values as List<ContactModel?>
//                notifyDataSetChanged()
//            }
//        }
//    }
//
//    init {
//        filterContacts = contactModelList
//    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = contactModelList[position]
        (holder as ViewHolder).bind(data)
    }

}