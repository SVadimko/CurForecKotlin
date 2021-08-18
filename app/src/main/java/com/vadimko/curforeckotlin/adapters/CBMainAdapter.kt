package com.vadimko.curforeckotlin.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.cbjsonApi.CurrencyCBjs
import com.vadimko.curforeckotlin.databinding.CbrfMainRecycleBinding
import com.vadimko.curforeckotlin.tcsApi.TCSRepository
import com.vadimko.curforeckotlin.ui.now.NowFragment
import com.vadimko.curforeckotlin.ui.now.NowViewModel
import com.vadimko.curforeckotlin.utils.NowPreference
import java.util.*

/**
 * Adapter class for RecycleView for CentralBank data on [NowFragment]
 *
 * @param curCB take list of [CurrencyCBjs] that got from
 * [NowViewModel] through
 * [TCSRepository] Retrofit
 *
 */

class CBMainAdapter(private val curCB: List<CurrencyCBjs>) :
    RecyclerView.Adapter<CBMainAdapter.CurrCBHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrCBHolder {
        val binding = CbrfMainRecycleBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return CurrCBHolder(binding)

    }

    override fun onBindViewHolder(holder: CurrCBHolder, position: Int) {
        val valute = curCB[position]
        holder.bindActivity(valute)
    }

    override fun getItemCount(): Int {
        return curCB.size
    }

    /**
     * inner class ViewHolder
     *
     */

    inner class CurrCBHolder(binding: CbrfMainRecycleBinding) :
        RecyclerView.ViewHolder(binding.cardView),
        View.OnClickListener {
        private val cardView = binding.cardView

        private var imageView = binding.flag

        private var arrow = binding.arrow

        private var currTv = binding.curr

        private var valTv = binding.value

        private var valWasTv = binding.valWas

        init {
            cardView.setOnClickListener(this)
        }

        private lateinit var valute: CurrencyCBjs

        @SuppressLint("ResourceAsColor", "SetTextI18n")
        fun bindActivity(valute: CurrencyCBjs) {
            val textParams = NowPreference.getTextParams()
            val typefaceTv = textParams[0] as Typeface
            val textSizeInt = textParams[1] as Float

            valTv.apply {
                typeface = typefaceTv
                textSize = textSizeInt
            }
            valWasTv.apply {
                typeface = typefaceTv
                textSize = textSizeInt
            }

            this.valute = valute
            currTv.text = valute.curr

            // Grivna and Lira are returned in a ratio of 1 to 10 to rubles, so for them we increase
            // number of decimal places
            if ((valute.curr == "UAH") || (valute.curr == "TRY")) {
                valTv.text = String.format(Locale.US, "%.3f", valute.value)
            } else {
                valTv.text = String.format(Locale.US, "%.2f", valute.value)
            }
            if (valute.valueWas != 0.0) {
                arrow.visibility = View.VISIBLE
                // change the color of the current course and set the corresponding arrow depending on
                // direction of rate growth
                if (valute.value > valute.valueWas) {
                    valTv.setTextColor(Color.RED)
                    valWasTv.setTextColor(Color.RED)
                    arrow.setImageResource(R.drawable.arrup)
                }
                if (valute.value < valute.valueWas) {
                    valTv.setTextColor(Color.rgb(60, 220, 78))
                    valWasTv.setTextColor(Color.rgb(60, 220, 78))
                    arrow.setImageResource(R.drawable.arrdown)
                }

                // Grivna and Lira are returned in a ratio of 1 to 10 to rubles, so for them we increase
                // number of decimal places
                if ((valute.curr == "UAH") || (valute.curr == "TRY")) {
                    valWasTv.text = String.format(
                        Locale.US, "%+.3f",
                        -valute.valueWas + valute.value
                    )
                } else {
                    valWasTv.text = String.format(
                        Locale.US, "%+.2f",
                        -valute.valueWas + valute.value
                    )
                }
                imageView.setImageResource(valute.flag)
            } else {
                //in case the previous value of the course is undefined
                valTv.setTextColor(R.color.colorRed)
                arrow.visibility = View.INVISIBLE
                valWasTv.setText(R.string.na_string)
                imageView.setImageResource(valute.flag)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TextViewCompat.setAutoSizeTextTypeWithDefaults(
                    valTv,
                    TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
                )
                TextViewCompat.setAutoSizeTextTypeWithDefaults(
                    valWasTv,
                    TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
                )
                TextViewCompat.setAutoSizeTextTypeWithDefaults(
                    currTv,
                    TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
                )
            }
        }

        override fun onClick(v: View?) {
        }
    }
}
