package com.vadimko.curforeckotlin.ui.now

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.SettingsActivity
import com.vadimko.curforeckotlin.adapters.CBMainAdapter
import com.vadimko.curforeckotlin.adapters.TCSMainAdapter
import com.vadimko.curforeckotlin.cbjsonApi.CurrencyCBjs
import com.vadimko.curforeckotlin.databinding.FragmentNowBinding
import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.utils.CoinsAnimator
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Now fragment representing current courses of Tinkov and Central Bank
 * @property nowViewModel [NowViewModel] for [NowFragment]
 * @property mScale Display params, used for [CoinsAnimator]. Send it through [NowViewModel]
 * @property rect Display params, used for [CoinsAnimator]. Send it through [NowViewModel]
 * @property displayRight Display params, used for [CoinsAnimator]. Send it through [NowViewModel]
 * @property displayBottom Display params, used for [CoinsAnimator]. Send it through [NowViewModel]
 */

class NowFragment : Fragment() {

    private val nowViewModel by viewModel<NowViewModel>()

    private var _binding: FragmentNowBinding? = null
    private val binding get() = _binding!!

    private var mScale: Float = 0f
    private lateinit var rect: Rect
    private var displayRight = 0
    private var displayBottom = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNowBinding.inflate(inflater, container, false)
        val root = binding.root
        binding.recycletinkoff.layoutManager = LinearLayoutManager(context)
        binding.recyclecbrf.layoutManager = LinearLayoutManager(context)
        binding.swipe.setOnRefreshListener {
            nowViewModel.prepareAnimations(mScale, rect, binding.root)
            nowViewModel.startRefresh()
        }
        getDisplayParams()
        nowViewModel.prepareAnimations(mScale, rect, binding.root)
        return root
    }

    /**
     * Observe data receiving from Tinkov bank bank through [NowViewModel]
     * Launching [CoinsAnimator] through [NowViewModel]
     * Observe data receiving from Central Bank through [NowViewModel]
     */
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        getDisplayParams()
//        nowViewModel.prepareAnimations(mScale, rect, binding.root)
        /*nowViewModel.getDataTCs().observe(viewLifecycleOwner, { forecTCS ->
            forecTCS?.let {
                setupAdapterTCS(forecTCS)
                binding.lastchk.text =
                    "${getString(R.string.lastupdateTCS)} ${forecTCS[0].curr} ${getString(R.string.NOWFRAGsource)} tinkoff.ru"
            }
        })*/
        lifecycleScope.launchWhenStarted {
            nowViewModel.getDataTCs().collect {
                setupAdapterTCS(it)
                binding.lastchk.text =
                    "${getString(R.string.lastupdateTCS)} ${it[0].curr} ${getString(R.string.NOWFRAGsource)} tinkoff.ru"
                binding.swipe.isRefreshing = false
            }
        }

      /*  nowViewModel.getDataCD().observe(viewLifecycleOwner, { forecCB ->
            forecCB?.let {
                setupAdapterCB(forecCB)
                binding.lastchkcbrf.text =
                    "${getString(R.string.lastupdateTCS)} ${forecCB[0].dateTime} ${getString(R.string.NOWFRAGsource)} cbr-xml-daily.ru"
                binding.swipe.isRefreshing = false
            }
        })*/

        lifecycleScope.launchWhenStarted {
            nowViewModel.getDataCD().collect {
                setupAdapterCB(it)
                binding.lastchkcbrf.text =
                    "${getString(R.string.lastupdateTCS)} ${it[0].dateTime} ${getString(R.string.NOWFRAGsource)} cbr-xml-daily.ru"

            }
        }

    }

    override fun onDestroyView() {
        nowViewModel.stopAnimation()
        _binding = null
        super.onDestroyView()

    }

    /**
     * Setup [TCSMainAdapter] for [RecyclerView] Tinkov bank
     */
    private fun setupAdapterTCS(tcsForec: List<CurrencyTCS>) {
        binding.recycletinkoff.adapter = TCSMainAdapter(tcsForec)
    }

    /**
     * Setup [CBMainAdapter] for [RecyclerView] Central bank
     */
    private fun setupAdapterCB(cbForec: List<CurrencyCBjs>) {
        binding.recyclecbrf.adapter = CBMainAdapter(cbForec)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val settings = Intent(activity, SettingsActivity::class.java)
                startActivity(settings)
                return true
            }
            R.id.refresh -> {
                nowViewModel.prepareAnimations(mScale, rect, binding.root)
                nowViewModel.startRefresh()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Get display params for launching [CoinsAnimator] through [NowViewModel]
     */
    private fun getDisplayParams() {
        rect = Rect()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity?.windowManager!!.currentWindowMetrics
            rect = windowMetrics.bounds
            /*val windowInsets = windowMetrics.windowInsets
            val insets: Insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars()
                        or WindowInsets.Type.displayCutout()
            )
            val insetsWidth: Int = insets.right + insets.left
            val insetsHeight: Int = insets.top + insets.bottom

            val legacySize = Size(
                rect.width() - insetsWidth,
                rect.height() - insetsHeight
            )*/
        } else {
            val display = requireActivity().windowManager.defaultDisplay
            display?.getRectSize(rect)
        }
        val displayMetrics = context?.resources?.displayMetrics
        if (displayMetrics != null) {
            mScale = displayMetrics.density
            displayBottom = displayMetrics.heightPixels
            displayRight = displayMetrics.widthPixels
        }
    }
}