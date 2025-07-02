package com.malikhain.kuripot_app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticUtils {
    
    fun performHapticFeedback(context: Context, type: HapticType) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (!vibrator.hasVibrator()) return
        
        val effect = when (type) {
            HapticType.LIGHT_CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    @Suppress("DEPRECATION")
                    VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                }
            }
            HapticType.MEDIUM_CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    @Suppress("DEPRECATION")
                    VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                }
            }
            HapticType.HEAVY_CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    @Suppress("DEPRECATION")
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                }
            }
            HapticType.ERROR -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 100, 50, 100),
                        intArrayOf(0, 255, 0, 255),
                        -1
                    )
                } else {
                    @Suppress("DEPRECATION")
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 100, 50, 100),
                        intArrayOf(0, 255, 0, 255),
                        -1
                    )
                }
            }
            HapticType.SUCCESS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 50, 50, 50),
                        intArrayOf(0, 128, 0, 255),
                        -1
                    )
                } else {
                    @Suppress("DEPRECATION")
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 50, 50, 50),
                        intArrayOf(0, 128, 0, 255),
                        -1
                    )
                }
            }
        }
        
        vibrator.vibrate(effect)
    }
    
    enum class HapticType {
        LIGHT_CLICK,
        MEDIUM_CLICK,
        HEAVY_CLICK,
        ERROR,
        SUCCESS
    }
} 