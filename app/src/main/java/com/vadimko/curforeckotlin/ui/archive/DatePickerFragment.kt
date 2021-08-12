package com.vadimko.curforeckotlin.ui.archive

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import java.util.*

/**
 * @property ARG_DATE key of bundle for receive to [DatePickerFragment] date that was
 * already choosen before
 */
private const val ARG_DATE = "date"

/**
 * DatePickerFragment for choosing dates on archive fragment
 */

open class DatePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val dateListener =
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, month)
                c.set(Calendar.DAY_OF_MONTH, day)
                val resultDate: Date = c.time
                val requestKey: String = if (this.tag.equals(FROM_DATE_PICKER)) "fromDate"
                else "tillDate"
                parentFragmentManager.setFragmentResult(
                    requestKey,
                    bundleOf("bundleKey" to resultDate)
                )
            }
        val date = arguments?.getSerializable(ARG_DATE) as Date
        c.time = date
        val initialYear = c.get(Calendar.YEAR)
        val initialMonth = c.get(Calendar.MONTH)
        val initialDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.datePicker.minDate = 1323388800000
        return datePickerDialog
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}