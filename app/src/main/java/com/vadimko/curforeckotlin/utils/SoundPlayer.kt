package com.vadimko.curforeckotlin.utils

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.SoundPool
import com.vadimko.curforeckotlin.utils.SoundPlayer.mAssetManager
import com.vadimko.curforeckotlin.utils.SoundPlayer.s1
import com.vadimko.curforeckotlin.utils.SoundPlayer.s2
import com.vadimko.curforeckotlin.utils.SoundPlayer.s3
import com.vadimko.curforeckotlin.utils.SoundPlayer.s4
import com.vadimko.curforeckotlin.utils.SoundPlayer.stopFlag
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import kotlin.random.Random

/**
 * Utility class to play sound while coins falling on animate
 * @property mAssetManager get assets from asset folder
 * @property [s1] Id of playing sound
 * @property [s2] Id of playing sound
 * @property [s3] Id of playing sound
 * @property [s4] Id of playing sound
 * @property [stopFlag] Flag that prevent for playing sound
 */
object SoundPlayer : KoinComponent {
    private val context: Context by inject()
    private var mAssetManager: AssetManager = context.assets
    private var s1: Int = 0
    private var s2: Int = 0
    private var s3: Int = 0
    private var s4: Int = 0
    private lateinit var mSoundPool: SoundPool
    private var stream = 0
    private var stopFlag = false

    fun onInit() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        mSoundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .build()
        s1 = loadSound("4.mp3")
        s2 = loadSound("1.mp3")
        s3 = loadSound("2.mp3")
        s4 = loadSound("3.mp3")
    }

    /**
     * Function plays random sound from loaded assets via [playSound]
     */
    fun playRandomSound() {
        when (Random.nextInt(4)) {
            0 -> playSound(s1)
            1 -> playSound(s2)
            2 -> playSound(s3)
            3 -> playSound(s4)
        }
    }

    /**
     * Plays final sound when last two coins are falling via [playSound]
     */
    fun playFinalSound() {
        playSound(s4)
    }

    /**
     * Sets all sounds to pause when NowFragment changed and sets [stopFlag]
     */
    fun onStop() {
        mSoundPool.autoPause()
        stopFlag = true
    }

    /**
     * Sets all sounds to resume when animation runs again and drops [stopFlag]
     */
    fun onResume() {
        mSoundPool.autoResume()
        stopFlag = false
    }

    /**
     * Release [SoundPool] resources on activity destroy
     */
    fun onDestroy() {
        mSoundPool.release()
    }

    /**
     * Loads files on [onInit] functions
     * @param fileName File name sound file to load
     */
    private fun loadSound(fileName: String): Int {
        val afd: AssetFileDescriptor = try {
            mAssetManager.openFd(fileName)
        } catch (e: IOException) {
            e.printStackTrace()
            return -1
        }
        return mSoundPool.load(afd, 1)
    }

    /**
     * Plays sound with set Id
     * @param sound Id of chosen sound file
     */
    private fun playSound(sound: Int): Int {
        if (!stopFlag)
            stream = mSoundPool.play(sound, 1f, 1f, 1, 0, 1f)
        return stream
    }
}