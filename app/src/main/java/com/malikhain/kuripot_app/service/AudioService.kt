package com.malikhain.kuripot_app.service

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AudioService(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var exoPlayer: ExoPlayer? = null
    private var currentRecordingPath: String? = null
    private var isRecording = false
    private var isPlaying = false
    
    suspend fun startRecording(): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (isRecording) {
                return@withContext Result.failure(Exception("Already recording"))
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "voice_note_$timestamp.m4a"
            val file = File(context.filesDir, fileName)
            currentRecordingPath = file.absolutePath
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            
            isRecording = true
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun stopRecording(): Result<String?> = withContext(Dispatchers.IO) {
        try {
            if (!isRecording) {
                return@withContext Result.failure(Exception("Not recording"))
            }
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            Result.success(currentRecordingPath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun startPlayback(audioPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (isPlaying) {
                stopPlayback()
            }
            
            val file = File(audioPath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Audio file not found"))
            }
            
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
                setMediaItem(mediaItem)
                prepare()
                play()
            }
            
            isPlaying = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun stopPlayback(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            exoPlayer?.apply {
                stop()
                release()
            }
            exoPlayer = null
            isPlaying = false
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isRecording(): Boolean = isRecording
    
    fun isPlaying(): Boolean = isPlaying
    
    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0L
    
    fun getDuration(): Long = exoPlayer?.duration ?: 0L
    
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }
    
    fun deleteAudioFile(audioPath: String): Boolean {
        return try {
            val file = File(audioPath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
} 