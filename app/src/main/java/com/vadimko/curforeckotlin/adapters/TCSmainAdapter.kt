package com.vadimko.curforeckotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.tcsapi.CurrencyTCS
import java.util.*


//адаптер для ресайклвью данных ТКС на фрагменте Today
class TCSmainAdapter(private val curTCS: List<CurrencyTCS>) :
    RecyclerView.Adapter<TCSmainAdapter.CurrTCSHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrTCSHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.tinkoff_main_recycle, parent, false)
        return CurrTCSHolder(view)

    }

    override fun onBindViewHolder(holder: CurrTCSHolder, position: Int) {
        val valute = curTCS[position]
        holder.bindActivity(valute)
    }

    override fun getItemCount(): Int {
        return curTCS.size
    }

    inner class CurrTCSHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val cardView: CardView = itemView.findViewById(R.id.card_view)
        var imageView = cardView.findViewById<View>(R.id.flag) as ImageView
        private var currTv = cardView.findViewById<View>(R.id.curr) as TextView
        private var buyTv = cardView.findViewById<View>(R.id.buy) as TextView
        private var cellTv = cardView.findViewById<View>(R.id.cell) as TextView

        init {
            cardView.setOnClickListener(this)
        }

        private lateinit var valute: CurrencyTCS

        fun bindActivity(valute: CurrencyTCS) {
            this.valute = valute
            currTv.text = valute.name
            buyTv.text = String.format(Locale.US, "%.2f", valute.buy)
            cellTv.text = String.format(Locale.US, "%.2f", valute.sell)
            imageView.setImageResource(valute.flag)
        }

        override fun onClick(v: View?) {
        }

    }
}