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
import com.vadimko.curforeckotlin.databinding.CbrfMainRecycleBinding

//адаптер для ресайклвью данных ЦБ на фрагменте Today
class CBMainAdapter(private val curCB: List<CurrencyCBjs>) :
    RecyclerView.Adapter<CBMainAdapter.CurrCBHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrCBHolder {
        //val view: View = LayoutInflater.from(parent.context)
            //.inflate(R.layout.cbrf_main_recycle, parent, false)
        val binding = CbrfMainRecycleBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        //return CurrCBHolder(view)
        return CurrCBHolder(binding)

    }

    override fun onBindViewHolder(holder: CurrCBHolder, position: Int) {
        val valute = curCB[position]
        holder.bindActivity(valute)
    }

    override fun getItemCount(): Int {
        return curCB.size
    }


    //inner class CurrCBHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    inner class CurrCBHolder(binding: CbrfMainRecycleBinding) : RecyclerView.ViewHolder(binding.cardView),
        View.OnClickListener {
        private val cardView= binding.cardView
        //var imageView = cardView.findViewById<View>(R.id.flag) as ImageView
        private var imageView = binding.flag//cardView.findViewById<View>(R.id.flag) as ImageView
        //private var arrow = cardView.findViewById<View>(R.id.arrow) as ImageView
        private var arrow = binding.arrow
        //private var currTv = cardView.findViewById<View>(R.id.curr) as TextView
        private var currTv = binding.curr
        //private var valTv = cardView.findViewById<View>(R.id.value) as TextView
        private var valTv = binding.value
        //private var valWasTv = cardView.findViewById<View>(R.id.val_was) as TextView
        private var valWasTv = binding.valWas

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
