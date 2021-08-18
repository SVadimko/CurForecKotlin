package com.vadimko.curforeckotlin.adapters

import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.vadimko.curforeckotlin.databinding.TinkoffMainRecycleBinding
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.tcsApi.TCSRepository
import com.vadimko.curforeckotlin.ui.now.NowFragment
import com.vadimko.curforeckotlin.ui.now.NowViewModel
import com.vadimko.curforeckotlin.utils.NowPreference
import java.util.*

/**
 * Adapter for RecycleView for Tinkov bank data on [NowFragment]
 *
 * @constructor take list of [CurrencyTCS] that getted from
 * [NowViewModel] through
 * [TCSRepository] Retrofit
 * @param curTCS list of [CurrencyTCS]
 *
 */

//in the comments below - code version without ViewBinding
class TCSMainAdapter(private val curTCS: List<CurrencyTCS>) :
    RecyclerView.Adapter<TCSMainAdapter.CurrTCSHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrTCSHolder {
        //val view: View = LayoutInflater.from(parent.context)
        //  .inflate(R.layout.tinkoff_main_recycle, parent, false)
        val binding = TinkoffMainRecycleBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        //return CurrTCSHolder(view)
        return CurrTCSHolder(binding)

    }

    override fun onBindViewHolder(holder: CurrTCSHolder, position: Int) {
        val valute = curTCS[position]
        holder.bindActivity(valute)
    }

    override fun getItemCount(): Int {
        return curTCS.size
    }

    /**
     * inner class ViewHolder
     *
     */

    //inner class CurrTCSHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    inner class CurrTCSHolder(binding: TinkoffMainRecycleBinding) :
        RecyclerView.ViewHolder(binding.cardView),
        View.OnClickListener {

        //private val cardView: CardView = itemView.findViewById(R.id.card_view)
        private val cardView = binding.cardView

        //private var imageView = cardView.findViewById<View>(R.id.flag) as ImageView
        private var imageView = binding.flag

        //private var currTv = cardView.findViewById<View>(R.id.curr) as TextView
        private var currTv = binding.curr

        //private var buyTv = cardView.findViewById<View>(R.id.buy) as TextView
        private var buyTv = binding.buy

        //private var cellTv = cardView.findViewById<View>(R.id.cell) as TextView
        private var cellTv = binding.cell

        init {
            cardView.setOnClickListener(this)
        }

        private lateinit var valute: CurrencyTCS

        fun bindActivity(valute: CurrencyTCS) {
            val textParams = NowPreference.getTextParams()
            val typefaceTv = textParams[0] as Typeface
            val textSizeInt = textParams[1] as Float

            this.valute = valute
            currTv.text = valute.name
            buyTv.apply {
                text = String.format(Locale.US, "%.2f", valute.buy)
                typeface = typefaceTv
                textSize = textSizeInt
            }
            cellTv.apply {
                text = String.format(Locale.US, "%.2f", valute.sell)
                typeface = typefaceTv
                textSize = textSizeInt

            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TextViewCompat.setAutoSizeTextTypeWithDefaults(
                    buyTv,
                    TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
                )
                TextViewCompat.setAutoSizeTextTypeWithDefaults(
                    cellTv,
                    TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
                )
                TextViewCompat.setAutoSizeTextTypeWithDefaults(
                    currTv,
                    TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
                )
            }
            imageView.setImageResource(valute.flag)
        }

        override fun onClick(v: View?) {
        }

    }
}