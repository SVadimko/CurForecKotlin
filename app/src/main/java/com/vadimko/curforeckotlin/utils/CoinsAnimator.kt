package com.vadimko.curforeckotlin.utils

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.preference.PreferenceManager
import com.vadimko.curforeckotlin.R
import com.vadimko.curforeckotlin.utils.CoinsAnimator.Companion.stop
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference
import java.util.*

/**
 * Class for creating animation after courses updated
 * @constructor takes display params, [WeakReference] on NowFragment main layout
 * @property stop flag that stops animation timer
 * @property count number of playing animations
 * @property mAllImageViews arraylist of Views which animates and add on NowFragment main layout
 * @property mHandler Handler prepared image, add it to arraylist and layout and begin [startAnimation]
 * @property enableSound Settings from SharedPreference allowing to play sound
 * @property anotherCount Counter to monitor count of falling coins to play on last two final sound
 */

class CoinsAnimator(
    val mScale: Float,
    val mDisplaySize: Rect,
    val layout: WeakReference<FrameLayout>
) : KoinComponent {

    private var enableSound = false
    private val context: Context by inject()
    private var count = 0
    private var anotherCount = 0
    private val coinsArrayImage = intArrayOf(
        R.drawable.coin,
        R.drawable.coin2,
        R.drawable.coin3,
        R.drawable.coin4
    )

    private var mAllImageViews = arrayListOf<View>()
    private val timer = Timer()

    /**
     * Start timer that start [mHandler] for playing animation
     */
    fun coinsAnimate() {
        stop = false
        timer.schedule(ExeTimerTask(), 0, 100)
        enableSound = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("onAnimationSound", false)
    }

    /**
     * Takes ImageView and use [ValueAnimator] to animate it
     */
    private fun startAnimation(aniView: View) {
        var alreadyPlayed = false
        aniView.pivotX = (aniView.width / 2).toFloat()
        aniView.pivotY = (aniView.height / 2).toFloat()
        val delay = Random().nextInt(1000).toLong()
        var set = 0
        when (Random().nextInt(5)) {
            0 -> set = 900
            1 -> set = 2000
            2 -> set = 1700
            3 -> set = 1200
            4 -> set = 1300
        }
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = set.toLong()
        animator.interpolator = AccelerateInterpolator()
        animator.startDelay = delay
        animator.addUpdateListener(object : AnimatorUpdateListener {
            var angle = 90 + (Math.random() * 501).toInt()
            var moveX = Random().nextInt(mDisplaySize.right)
            override fun onAnimationUpdate(animation: ValueAnimator) {
                val value = animation.animatedValue as Float
                aniView.rotation = angle * value
                aniView.translationX = (moveX - 40) * value
                aniView.translationY = (mDisplaySize.bottom + 150 * mScale) * value
                if ((mDisplaySize.height() < aniView.translationY) && !alreadyPlayed && !stop) {
                    if (enableSound) {
                        anotherCount++
                        if (anotherCount < 18) {
                            SoundPlayer.playRandomSound()
                        } else {
                            SoundPlayer.playFinalSound()
                        }
                        alreadyPlayed = true
                    }
                }
            }
        })
        animator.start()
    }


    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("InflateParams", "UseCompatLoadingForDrawables")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val viewId = Random().nextInt(coinsArrayImage.size)
            val inflate = LayoutInflater.from(context)
            val view = inflate.inflate(R.layout.ani_image_view, null)
            view.setBackgroundResource(coinsArrayImage[viewId])
            view.requestLayout()
            layout.get()?.addView(view)
            var animationLayout = view.layoutParams as FrameLayout.LayoutParams
            mAllImageViews.add(view)
            view.requestLayout()
            when (Random().nextInt(5)) {
                0 -> {
                    view.requestLayout()
                    animationLayout = view.layoutParams as FrameLayout.LayoutParams
                    animationLayout.setMargins(0, (-150 * mScale).toInt(), 700, 0)
                    view.requestLayout()
                }
                1 -> {
                    view.requestLayout()
                    animationLayout = view.layoutParams as FrameLayout.LayoutParams
                    animationLayout.setMargins(700, (-150 * mScale).toInt(), 0, 0)
                    view.requestLayout()
                }
                2 -> {
                    view.requestLayout()
                    animationLayout = view.layoutParams as FrameLayout.LayoutParams
                    animationLayout.setMargins(350, (-150 * mScale).toInt(), 0, 0)
                    view.requestLayout()
                }
                3 -> {
                    view.requestLayout()
                    animationLayout = view.layoutParams as FrameLayout.LayoutParams
                    animationLayout.setMargins(0, (-150 * mScale).toInt(), 350, 0)
                    view.requestLayout()
                }
                4 -> {
                    view.requestLayout()
                    animationLayout = view.layoutParams as FrameLayout.LayoutParams
                    animationLayout.setMargins(0, (-150 * mScale).toInt(), 0, 0)
                    view.requestLayout()
                }
            }
            view.requestLayout()
            val place2 = Random().nextInt(10) + 20
            animationLayout.width = (place2 * mScale).toInt()
            animationLayout.height = (place2 * mScale).toInt()
            startAnimation(view)
        }
    }

    /**
     * Control number of animations and stops animation through [stop]
     * after animation stops, remove all imageview from parent FrameLayout
     * @property count sets the number of flying coins
     */
    inner class ExeTimerTask : TimerTask() {
        override fun run() {
            mHandler.sendEmptyMessage(0x001)
            count++
            if (count == 20 || stop) {
                timer.cancel()
                timer.purge()
                val handler = Handler(Looper.getMainLooper())
                handler.post { mAllImageViews.forEach { layout.get()?.removeView(it) } }
                mAllImageViews.clear()
            }
            stop = false
        }
    }

    /**
     * Companion object for stop playing animation
     * @property stop Flag stops animation timer
     */
    companion object {
        private var stop: Boolean = false

        /**
         * Sets flag [stop] which stops play animation
         */
        fun stopAnimation() {
            stop = true
        }
    }
}