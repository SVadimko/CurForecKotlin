package com.vadimko.curforeckotlin.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.cbjsonapi.CurrencyCBjs
import java.util.*


//адаптер для ресайклвью данных ЦБ на фрагменте Today
class CBmainAdapter(private val curCB: List<CurrencyCBjs>) :
    RecyclerView.Adapter<CBmainAdapter.CurrCBHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrCBHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.cbrf_main_recycle, parent, false)
        return CurrCBHolder(view)

    }

    override fun onBindViewHolder(holder: CurrCBHolder, position: Int) {
        val valute = curCB[position]
        holder.bindActivity(valute)
    }

    override fun getItemCount(): Int {
        return curCB.size
    }

    inner class CurrCBHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val cardView: CardView = itemView.findViewById(R.id.card_view)
        var imageView = cardView.findViewById<View>(R.id.flag) as ImageView
        private var arrow = cardView.findViewById<View>(R.id.arrow) as ImageView
        private var currTv = cardView.findViewById<View>(R.id.curr) as TextView
        private var valTv = cardView.findViewById<View>(R.id.value) as TextView
        private var valWasTv = cardView.findViewById<View>(R.id.val_was) as TextView

        init {
            cardView.setOnClickListener(this)
        }

        private lateinit var valute: CurrencyCBjs

        @SuppressLint("ResourceAsColor", "SetTextI18n")
        fun bindActivity(valute: CurrencyCBjs) {
            this.valute = valute

            currTv.text = valute.curr
            valTv.text = String.format(Locale.US, "%.2f", valute.value)
            if (valute.value_was != 0.0) {
                arrow.visibility = View.VISIBLE
                if (valute.value > valute.value_was) {
                    valTv.setTextColor(Color.RED)
                    valWasTv.setTextColor(Color.RED)
                    arrow.setImageResource(R.drawable.arrup)
                }
                if (valute.value < valute.value_was) {
                    valTv.setTextColor(Color.rgb(60, 220, 78))
                    valWasTv.setTextColor(Color.rgb(60, 220, 78))
                    arrow.setImageResource(R.drawable.arrdown)
                }
                valWasTv.text = " (" + String.format(
                    Locale.US, "%+.2f",
                    -valute.value_was + valute.value
                ) + ")"
                imageView.setImageResource(valute.flag)
            } else {
                valTv.setTextColor(R.color.colorRed)
                arrow.visibility = View.INVISIBLE
                valWasTv.setText(R.string.na_string)
                imageView.setImageResource(valute.flag)
            }
        }

        override fun onClick(v: View?) {
        }

    }
}