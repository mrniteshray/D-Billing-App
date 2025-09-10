package com.niteshray.xapps.billingpro.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class ScannerSoundHelper {
    companion object {
        private var soundPool: SoundPool? = null
        private var beepSoundId: Int = 0

        fun playBeepSound(context: Context) {
            try {
                // Method 1: ToneGenerator (Most reliable)
                playToneBeep()

                // Method 2: Also add vibration for feedback
                playVibration(context)

            } catch (e: Exception) {
                // Fallback: Just vibration
                playVibration(context)
            }
        }

        private fun playToneBeep() {
            try {
                val toneGenerator = ToneGenerator(
                    AudioManager.STREAM_MUSIC, // Changed to MUSIC stream
                    80 // Volume (0-100)
                )
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)

                // Release after delay
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    toneGenerator.release()
                }, 300)
            } catch (e: Exception) {
                // Silent fail
            }
        }

        private fun playVibration(context: Context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    val vibrator = vibratorManager.defaultVibrator
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(100)
                    }
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }

        // Alternative method using MediaPlayer
        fun playBeepSoundAlternative(context: Context) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                // Check if sound is enabled
                when (audioManager.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> {
                        // Play beep sound
                        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)

                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            toneGen.release()
                        }, 200)
                    }
                    AudioManager.RINGER_MODE_VIBRATE -> {
                        // Only vibrate
                        playVibration(context)
                    }
                    AudioManager.RINGER_MODE_SILENT -> {
                        // Do nothing
                    }
                }
            } catch (e: Exception) {
                playVibration(context)
            }
        }
    }
}

