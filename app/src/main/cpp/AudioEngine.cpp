#include "AudioEngine.h"
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "AudioEngine", __VA_ARGS__)

// Costruttore e distruttore
AudioEngine::AudioEngine() = default;
AudioEngine::~AudioEngine() { stop(); }

// Avvia RX e TX stream
bool AudioEngine::start() {
    AAudioStreamBuilder* builder = nullptr;
    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    if (result != AAUDIO_OK) {
        LOGI("Errore createStreamBuilder: %d", result);
        return false;
    }

    // Configurazione comune: mono, 16 kHz, PCM 16-bit, low latency
    AAudioStreamBuilder_setChannelCount(builder, 1);
    AAudioStreamBuilder_setSampleRate(builder, 16000);
    AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_I16);
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setDataCallback(builder, onAudioCallback, this);

    // Crea stream di ricezione
    result = AAudioStreamBuilder_openStream(builder, &rxStream);
    if (result != AAUDIO_OK) {
        LOGI("Errore openStream RX: %d", result);
        return false;
    }
    // Crea stream di trasmissione
    result = AAudioStreamBuilder_openStream(builder, &txStream);
    if (result != AAUDIO_OK) {
        LOGI("Errore openStream TX: %d", result);
        return false;
    }

    AAudioStream_requestStart(rxStream);
    AAudioStream_requestStart(txStream);
    AAudioStreamBuilder_delete(builder);
    LOGI("AudioEngine started");
    return true;
}

// Ferma e chiude gli stream
void AudioEngine::stop() {
    if (txStream) {
        AAudioStream_requestStop(txStream);
        AAudioStream_close(txStream);
        txStream = nullptr;
    }
    if (rxStream) {
        AAudioStream_requestStop(rxStream);
        AAudioStream_close(rxStream);
        rxStream = nullptr;
    }
    LOGI("AudioEngine stopped");
}

// Callback per ciascun buffer audio
aaudio_data_callback_result_t
AudioEngine::onAudioCallback(AAudioStream* /*stream*/, void* userData,
                             void* audioData, int32_t numFrames) {
    // Per ora facciamo loopback: il buffer in ingresso viene riprodotto
    int16_t* buffer = static_cast<int16_t*>(audioData);
    for (int i = 0; i < numFrames; ++i) {
        buffer[i] = buffer[i];
    }
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}
