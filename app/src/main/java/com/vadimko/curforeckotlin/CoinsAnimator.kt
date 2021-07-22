package com.vadimko.curforeckotlin

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import java.util.*

class CoinsAnimator(val mScale: Float, val mDisplaySize: Rect, val mRootLayout: FrameLayout, val context: Context) {
    var count = 0
    private val coinsArrayImage = intArrayOf(
        R.drawable.coin,
        R.drawable.coin2,
        R.drawable.coin3,
        R.drawable.coin4
    )

    private val mAllImageViews = ArrayList<View>()
    private val timer = Timer()

    fun weatherAnimationSnow(){
        timer.schedule(ExeTimerTask(), 0, 100)
    }


    fun startAnimation(aniView: ImageView) {

        aniView.pivotX = (aniView.width / 2).toFloat()
        aniView.pivotY = (aniView.height / 2).toFloat()
        val delay = Random().nextInt(1000).toLong()
        var set = 0
        when (Random().nextInt(5)) {
            0 -> set = 900
            1 -> set = 2500
            2 -> set = 2000
            3 -> set = 1200
            4 -> set = 1500
        }
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = set.toLong()
        animator.interpolator = AccelerateInterpolator()
        animator.startDelay = delay
        animator.addUpdateListener(object : AnimatorUpdateListener {
            var angle = 90 + (Math.random() * 501).toInt()
            var moveX = Random().nextInt(mDisplaySize.right)
            override fun onAnimationUpdate(animation: ValueAnimator) {
                val value = (animation.animatedValue as Float).toFloat()
                aniView.rotation = angle * value
                aniView.translationX = (moveX - 40) * value
                aniView.translationY = (mDisplaySize.bottom + 150 * mScale) * value
            }
        })
        animator.start()
    }

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("InflateParams", "UseCompatLoadingForDrawables")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val viewId = Random().nextInt(coinsArrayImage.size)
            val d: Drawable? =
                context.getDrawable(coinsArrayImage[viewId])
            val inflate = LayoutInflater.from(context)
            val imageView = inflate.inflate(R.layout.ani_image_view, null) as ImageView
            imageView.setImageDrawable(d)
            imageView.requestLayout()
            mRootLayout.addView(imageView)
            var animationLayout = imageView.layoutParams as FrameLayout.LayoutParams
            mAllImageViews.add(imageView)
            imageView.requestLayout()
            when (Random().nextInt(5)) {
                0 -> {
                    imageView.requestLayout()
                    animationLayout = imageView.layoutParams as FrameLayout.LayoutParams
                    animationLayout.setMargins(0, (-150 * mScale).toInt(), 700, 0)
                    imageView.requestLayout()
                }
                1 -> {
                    imageView.requestLayout()
                    animationLayout = imageView.layoutParams as FrameLayout.LayoutParams
                    animationLayout.setMargins(700, (-150 * mScale).toInt(), 0, 0)
                    imageView.requestLayout()
                }
                2 -> {
                    imageView.requestLayout()
                    animationLayout = imageView.layoutParams as FrameLayout.LayoutParams
                    animationLayout.setMargins(350, (-150 * mScale).toInt(), 0, 0)
                    imageView.requestLayout()
                }
                3 -> {
                    imageView.requestLayout()
                    animationLayout = imageView.layoutParams as FrameLayout.LayoutParams
                    animationLayout.setMargins(0, (-150 * mScale).toInt(), 350, 0)
                    imageView.requestLayout()
                }
                4 -> {
                    imageView.requestLayout()
                    animationLayout = imageView.layoutParams as FrameLayout.LayoutParams
                    animationLayout.setMargins(0, (-150 * mScale).toInt(), 0, 0)
                    imageView.requestLayout()
                }

            }
            imageView.requestLayout()
            val place2 = Random().nextInt(10) + 20
            animationLayout.width = (place2 * mScale).toInt()
            animationLayout.height = (place2 * mScale).toInt()
            startAnimation(imageView)
        }
    }

    inner class ExeTimerTask : TimerTask() {
        override fun run() {
            mHandler.sendEmptyMessage(0x001)
            count++
            if (count == 50) {
                timer.cancel()
                timer.purge()
            }
        }
    }
}