package com.vadimko.curforeckotlin.ui.now

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Insets
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vadimko.curforeckotlin.*
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.adapters.CBMainAdapter
import com.vadimko.curforeckotlin.adapters.TCSMainAdapter
import com.vadimko.curforeckotlin.cbjsonapi.CurrencyCBjs
import com.vadimko.curforeckotlin.databinding.FragmentNowBinding
import com.vadimko.curforeckotlin.tcsapi.CurrencyTCS


class NowFragment : Fragment() {

    private val nowViewModel: NowViewModel by lazy {
        ViewModelProvider(this).get(NowViewModel::class.java)
    }
    private var _binding: FragmentNowBinding? = null

    private val binding get() = _binding!!
    private lateinit var root: View
    private lateinit var tcsRecycle: RecyclerView
    private lateinit var cbRecycle: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var mScale: Float = 0f
    private lateinit var rect: Rect
    private var displayRight = 0
    private var displayBottom = 0
    private lateinit var mContext: Context
    private lateinit var frameLayout: FrameLayout

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
        root = binding.root

        tcsRecycle = binding.recycletinkoff
        //tcsRecycle = root.findViewById(R.id.recycletinkoff)
        tcsRecycle.layoutManager = LinearLayoutManager(context)




        cbRecycle = binding.recyclecbrf
        //cbRecycle = root.findViewById(R.id.recyclecbrf)
        cbRecycle.layoutManager = LinearLayoutManager(context)

        frameLayout = binding.framelay
        //frameLayout = root.findViewById(R.id.framelay)


        /*swipeRefreshLayout = root.findViewById<SwipeRefreshLayout>(R.id.swipe)
        swipeRefreshLayout.apply {
            setOnRefreshListener {
                startWorker()
                val coinsAnimator = CoinsAnimator(mScale, rect, frameLayout, mContext)
                coinsAnimator.weatherAnimationSnow()
            }
        }*/
        swipeRefreshLayout = binding.swipe
        swipeRefreshLayout.setOnRefreshListener{
            //startWorker()
            nowViewModel.startWorker()
            ////nowViewModel.startAnimations(mScale, rect, frameLayout)
            //val coinsAnimator = CoinsAnimator(mScale, rect, frameLayout, requireContext())
            //coinsAnimator.weatherAnimationSnow()
        }
        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDisplayParams()
        //подписка на данные получаемые с сервера ЦБ
        nowViewModel.getData().observe(viewLifecycleOwner, { forecTCS ->
            forecTCS?.let {
                setupAdapterTCS(forecTCS)
                //val lastChk = root.findViewById<TextView>(R.id.lastchk)
                binding.lastchk.text =
                //lastChk.text =
                    "${getString(R.string.lastupdateTCS)} ${forecTCS[0].curr} ${getString(R.string.NOWFRAGsource)} tinkoff.ru"
                //fillBundle(forecTCS)
                //CalcViewModel.data.postValue(forecTCS)
                nowViewModel.startAnimations(mScale, rect, frameLayout)
            }
        })
        //подписка на данные получаемые с сервера Тиньков
        nowViewModel.getData2().observe(viewLifecycleOwner, { forecCB ->
            forecCB?.let {
                setupAdapterCB(forecCB)
                //val lastChck = root.findViewById<TextView>(R.id.lastchkcbrf)
                binding.lastchkcbrf.text =
                //lastChck.text =
                    "${getString(R.string.lastupdateTCS)} ${forecCB[0].datetime} ${getString(R.string.NOWFRAGsource)} cbr-xml-daily.ru"
                swipeRefreshLayout.isRefreshing = false
            }
        })

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()

    }

    //конфигурирование и запуск воркера для обновления данных о курсах
   /* private fun startWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            //.setRequiresCharging(true)
            .build()

        //val datastring: String = "inputDATA once"
        //val randomData = workDataOf("dt" to datastring)
        val workManager = context?.let { WorkManager.getInstance(it) }
        val myWorkRequest = OneTimeWorkRequest.Builder(
            NowWorker::class.java
        )
            .setConstraints(constraints)
            //.setInputData(randomData)
            .build()
        workManager?.enqueue(myWorkRequest)
    }*/

    private fun setupAdapterTCS(tcsForec: List<CurrencyTCS>) {
        tcsRecycle.adapter = TCSMainAdapter(tcsForec)
    }

    private fun setupAdapterCB(cbForec: List<CurrencyCBjs>) {
        cbRecycle.adapter = CBMainAdapter(cbForec)
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
                //startWorker()
                nowViewModel.startWorker()
                ////nowViewModel.startAnimations(mScale, rect, frameLayout)
                //val coinsAnimator = CoinsAnimator(mScale, rect, frameLayout, requireContext())
                //coinsAnimator.weatherAnimationSnow()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    private fun getDisplayParams() {
        rect = Rect()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity?.windowManager!!.currentWindowMetrics
            val windowInsets = windowMetrics.windowInsets
            val insets: Insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars()
                        or WindowInsets.Type.displayCutout()
            )
            val insetsWidth: Int = insets.right + insets.left
            val insetsHeight: Int = insets.top + insets.bottom
            rect = windowMetrics.bounds
            val legacySize = Size(
                rect.width() - insetsWidth,
                rect.height() - insetsHeight
            )
        } else {
            val display = requireActivity().windowManager.defaultDisplay
            display?.getRectSize(rect)
            //val metrics = DisplayMetrics()
            //display?.getMetrics(metrics)
            //val some = context?.resources?.displayMetrics
            //mScale = metrics.density
        }
        val displayMetrics = context?.resources?.displayMetrics
        if (displayMetrics != null) {
            mScale = displayMetrics.density
            displayBottom = displayMetrics.heightPixels
            displayRight = displayMetrics.widthPixels
        }
    }
}