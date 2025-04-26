package com.example.stagecue

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.stagecue.ui.theme.StageCueTheme

class MainActivity : ComponentActivity() {
    // Funzioni native
    private external fun startAudioEngine(): Boolean
    private external fun stopAudioEngine()

    // Permesso RECORD_AUDIO
    private val requestAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Log.i("Loopback", "native permission granted, starting AudioEngine")
            val ok = startAudioEngine()
            if (!ok) Toast.makeText(this, "Impossibile avviare AudioEngine", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permesso microfono negato", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        init { System.loadLibrary("audio_module") }
    }

    // Variabili per il Test Tone
    private var toneTrack: AudioTrack? = null
    private var toneThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Speakerphone in comunicazione
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        am.mode = AudioManager.MODE_IN_COMMUNICATION
        am.isSpeakerphoneOn = true
        am.setStreamVolume(
            AudioManager.STREAM_VOICE_CALL,
            am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
            0
        )

        setContent {
            StageCueTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    var started by remember { mutableStateOf(false) }
                    Button(
                        onClick = {
                            Log.i("Loopback", "button clicked, started=$started")
                            if (!started) {
                                // se non ho permesso, lo richiedo
                                if (ContextCompat.checkSelfPermission(
                                        this@MainActivity,
                                        Manifest.permission.RECORD_AUDIO
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    if (isEmulator()) {
                                        startTestTone()
                                    } else {
                                        val ok = startAudioEngine()
                                        if (!ok) Toast.makeText(
                                            this@MainActivity,
                                            "Impossibile avviare AudioEngine",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                // stop
                                if (isEmulator()) {
                                    stopTestTone()
                                } else {
                                    stopAudioEngine()
                                }
                            }
                            started = !started
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    ) {
                        Text(if (!started) "Start Audio Engine" else "Stop Audio Engine")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (isEmulator()) stopTestTone() else stopAudioEngine()
        super.onDestroy()
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.lowercase().contains("virtual")
                || Build.MODEL.contains("Emulator")
                || Build.MANUFACTURER.contains("Google")
                || Build.BRAND.startsWith("generic"))
    }

    // --- Test Tone per emulatori ---
    private fun startTestTone(): Boolean {
        if (toneTrack != null) return false
        Log.i("TestTone", "startTestTone()")
        Toast.makeText(this, "Test Tone ON", Toast.LENGTH_SHORT).show()

        val sampleRate = 48000
        // dimensione minima buffer
        val bufSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        toneTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufSize,
            AudioTrack.MODE_STREAM
        ).apply { play() }

        // genera un buffer di sinusoide 440Hz
        val freq = 440.0
        val samples = ShortArray(bufSize)
        for (i in samples.indices) {
            val angle = 2.0 * Math.PI * i.toDouble() * freq / sampleRate
            samples[i] = (Math.sin(angle) * Short.MAX_VALUE).toInt().toShort()
        }

        toneThread = Thread {
            Log.i("TestTone", "tone thread running")
            while (toneTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                toneTrack?.write(samples, 0, samples.size)
            }
            Log.i("TestTone", "tone thread ending")
        }.also { it.start() }

        return true
    }

    private fun stopTestTone() {
        Log.i("TestTone", "stopTestTone()")
        toneTrack?.let {
            it.stop()
            it.release()
        }
        toneTrack = null
        toneThread = null
        Toast.makeText(this, "Test Tone OFF", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StageCueTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Button(
                onClick = { /* preview */ },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Text("Start Audio Engine")
            }
        }
    }
}
