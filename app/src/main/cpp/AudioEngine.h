#pragma once

#include <aaudio/AAudio.h>

class AudioEngine {
public:
    AudioEngine();
    ~AudioEngine();

    // Avvia la cattura e riproduzione audio
    bool start();

    // Ferma gli stream
    void stop();

private:
    AAudioStream* txStream = nullptr;
    AAudioStream* rxStream = nullptr;

    // Callback invocata da AAudio per ogni buffer
    static aaudio_data_callback_result_t
    onAudioCallback(AAudioStream* stream,
                    void* userData,
                    void* audioData,
                    int32_t numFrames);
};
