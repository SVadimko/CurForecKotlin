package com.vadimko.curforeckotlin

import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*

/**
 * SettingsActivity class
 */

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val updateSwitch = findPreference<SwitchPreferenceCompat>("updateon")
            val updatePeriod = findPreference<ListPreference>("update_per")
            val editTextBuy = findPreference<EditTextPreference>("buymore")
            val editTextSell = findPreference<EditTextPreference>("sellmore")

            editTextBuy!!.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }
            editTextSell!!.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            updateSwitch?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    val some = newValue.toString().toBoolean()
                    updatePeriod?.isEnabled = !some
                    true
                }
        }
    }
}