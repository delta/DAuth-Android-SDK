package edu.nitt.delta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//class AccountsDialogAdapter(
//    private val mDataset: Array<String>,
//    internal var recyclerViewItemClickListener: RecyclerViewItemClickListener
//) : RecyclerView.Adapter<AccountsDialogAdapter.AccountViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, i: Int): AccountViewHolder {
//
////        val v = LayoutInflater.from(parent.context).inflate(R.layout.fruit_item, parent, false)
//
////        return AccountViewHolder(v)
//
//    }
//
//    override fun onBindViewHolder(fruitViewHolder: AccountViewHolder, i: Int) {
////        fruitViewHolder.mTextView.text = mDataset[i]
//
//
//    }
//
//    override fun getItemCount(): Int {
//        return mDataset.size
//    }
//
//
//    inner class AccountViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
//
////        var mTextView: TextView
//
//        init {
////            mTextView = v.textView
//            v.setOnClickListener(this)
//        }
//
//        override fun onClick(v: View) {
//            recyclerViewItemClickListener.clickOnItem(mDataset[this.adapterPosition])
//
//        }
//    }
//
//    interface RecyclerViewItemClickListener {
//        fun clickOnItem(data: String)
//    }
//}