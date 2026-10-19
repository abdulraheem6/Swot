package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

object AudioTonePlayer {

    private var activeJob: Job? = null
    private var activeAudioTrack: AudioTrack? = null
    private var activeMediaPlayer: android.media.MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun isPlaying(): Boolean = activeJob?.isActive == true || activeMediaPlayer?.isPlaying == true

    fun stop() {
        activeJob?.cancel()
        activeJob = null
        try {
            activeAudioTrack?.stop()
            activeAudioTrack?.release()
        } catch (_: Exception) {}
        activeAudioTrack = null

        try {
            activeMediaPlayer?.stop()
            activeMediaPlayer?.release()
        } catch (_: Exception) {}
        activeMediaPlayer = null
    }

    fun playCustomAudio(
        context: android.content.Context,
        pathOrUri: String,
        volumePercent: Int = 80,
        onCompletion: () -> Unit = {}
    ) {
        stop()
        try {
            val mp = android.media.MediaPlayer()
            activeMediaPlayer = mp
            val vol = (volumePercent.coerceIn(0, 100) / 100f)
            mp.setVolume(vol, vol)
            val file = java.io.File(pathOrUri)
            if (file.exists()) {
                mp.setDataSource(file.absolutePath)
            } else {
                mp.setDataSource(context, android.net.Uri.parse(pathOrUri))
            }
            mp.setOnCompletionListener {
                stop()
                onCompletion()
            }
            mp.setOnErrorListener { _, _, _ ->
                stop()
                onCompletion()
                true
            }
            mp.prepare()
            mp.start()
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
            // Fallback to gentle bell tone
            playTone("gentle", volumePercent, onCompletion)
        }
    }

    fun playContinuousAlarm(
        context: android.content.Context,
        toneName: String = "alarm",
        customUri: String? = null,
        volumePercent: Int = 85
    ) {
        stop()
        if (!customUri.isNullOrBlank()) {
            try {
                val mp = android.media.MediaPlayer()
                activeMediaPlayer = mp
                val vol = (volumePercent.coerceIn(0, 100) / 100f)
                mp.setVolume(vol, vol)
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                val file = java.io.File(customUri)
                if (file.exists()) {
                    mp.setDataSource(file.absolutePath)
                } else {
                    mp.setDataSource(context, android.net.Uri.parse(customUri))
                }
                mp.isLooping = true
                mp.prepare()
                mp.start()
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        activeJob = scope.launch {
            val sampleRate = 44100
            val volume = (volumePercent.coerceIn(0, 100) / 100f) * 0.9f
            val pcmData = when (toneName.lowercase()) {
                "radar" -> generateRadarTone(sampleRate, volume)
                "gentle" -> generateGentleBellTone(sampleRate, volume)
                "digital" -> generateDigitalBeepTone(sampleRate, volume)
                "chime" -> generateChimeTone(sampleRate, volume)
                else -> generateAlarmTone(sampleRate, volume)
            }
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, pcmData.size * 2)

            while (isActive) {
                try {
                    val audioTrack = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()

                    activeAudioTrack = audioTrack
                    audioTrack.write(pcmData, 0, pcmData.size)
                    audioTrack.play()

                    val durationMs = (pcmData.size.toDouble() / sampleRate * 1000).toLong()
                    delay(durationMs + 300)
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {
                    break
                }
            }
        }
    }

    fun playTone(
        toneName: String,
        volumePercent: Int = 80,
        onCompletion: () -> Unit = {}
    ) {
        stop()

        activeJob = scope.launch {
            try {
                val sampleRate = 44100
                val volume = (volumePercent.coerceIn(0, 100) / 100f) * 0.8f

                val pcmData = when (toneName.lowercase()) {
                    "radar" -> generateRadarTone(sampleRate, volume)
                    "gentle" -> generateGentleBellTone(sampleRate, volume)
                    "alarm" -> generateAlarmTone(sampleRate, volume)
                    "digital" -> generateDigitalBeepTone(sampleRate, volume)
                    else -> generateChimeTone(sampleRate, volume) // default "chime"
                }

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufferSize, pcmData.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                activeAudioTrack = audioTrack

                audioTrack.write(pcmData, 0, pcmData.size)
                audioTrack.play()

                // Wait for the duration of the audio plus small buffer
                val durationMs = (pcmData.size.toDouble() / sampleRate * 1000).toLong()
                delay(durationMs + 100)
            } catch (_: Exception) {
            } finally {
                stop()
                onCompletion()
            }
        }
    }

    private fun generateChimeTone(sampleRate: Int, volume: Float): ShortArray {
        val durationSeconds = 1.2
        val numSamples = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val freq1 = 523.25 // C5
        val freq2 = 783.99 // G5

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = kotlin.math.exp(-3.0 * t) // natural decay
            val sample = (sin(2.0 * Math.PI * freq1 * t) * 0.6 + sin(2.0 * Math.PI * freq2 * t) * 0.4) * envelope * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateGentleBellTone(sampleRate: Int, volume: Float): ShortArray {
        val durationSeconds = 1.5
        val numSamples = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val freqs = listOf(659.25, 830.61, 987.77, 1318.51) // E5, G#5, B5, E6

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var sum = 0.0
            freqs.forEachIndexed { index, freq ->
                val noteStart = index * 0.12
                if (t >= noteStart) {
                    val noteTime = t - noteStart
                    val env = kotlin.math.exp(-4.0 * noteTime)
                    sum += sin(2.0 * Math.PI * freq * noteTime) * env * 0.25
                }
            }
            val sample = sum * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateRadarTone(sampleRate: Int, volume: Float): ShortArray {
        val durationSeconds = 1.4
        val numSamples = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val freq = 1046.50 // C6

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // 2 pings
            val pingIndex = (t / 0.6).toInt()
            val pingT = t % 0.6
            val env = if (pingT < 0.3) kotlin.math.exp(-12.0 * pingT) else 0.0
            val sample = sin(2.0 * Math.PI * freq * pingT) * env * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateAlarmTone(sampleRate: Int, volume: Float): ShortArray {
        val durationSeconds = 1.5
        val numSamples = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val isHigh = ((t * 4).toInt() % 2) == 0
            val freq = if (isHigh) 880.0 else 587.33
            val sample = sin(2.0 * Math.PI * freq * t) * 0.8 * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateDigitalBeepTone(sampleRate: Int, volume: Float): ShortArray {
        val durationSeconds = 0.5
        val numSamples = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val freq = 1760.0 // A6

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val env = kotlin.math.exp(-6.0 * t)
            val sample = sin(2.0 * Math.PI * freq * t) * env * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }
}
