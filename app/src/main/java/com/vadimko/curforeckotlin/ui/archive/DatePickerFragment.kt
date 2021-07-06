package com.vadimko.curforeckotlin.ui.archive

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"
private const val ARG_FROMTILL = "fromtill"
open class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {


    interface Callbacks {
        fun onDateSelected(date: Date, frommtill: Boolean)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val frtl = arguments?.getSerializable(ARG_FROMTILL) as Boolean
        val dateListener = DatePickerDialog.OnDateSetListener {
                _: DatePicker, year: Int, month: Int, day: Int ->
            c.set(Calendar.YEAR,year)
            c.set(Calendar.MONTH,month)
            c.set(Calendar.DAY_OF_MONTH,day)
            val resultDate : Date = c.time
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onDateSelected(resultDate, frtl)
            }
        }

        val date = arguments?.getSerializable(ARG_DATE) as Date
        c.time=date
        val initialYear = c.get(Calendar.YEAR)
        val initialMonth = c.get(Calendar.MONTH)
        val initialDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.datePicker.minDate = 1323388800000
        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
    }

    companion object {
        fun newInstance(date: Date, frommtill:Boolean): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
                putSerializable(ARG_FROMTILL, frommtill)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }


}