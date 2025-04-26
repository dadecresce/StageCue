#include <jni.h>               // ← necessario per JNIEXPORT, jboolean, JNICALL
#include <aaudio/AAudio.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "AudioEngine", __VA_ARGS__)
#define AAUDIO_CHECK(call)                                    \
    do {                                                      \
        aaudio_result_t r = call;                             \
        if (r != AAUDIO_OK) {                                 \
            LOGI("AAudio error %d at " #call, r);             \
            return JNI_FALSE;                                 \
        }                                                     \
    } while (0)

static AAudioStream *inputStream = nullptr;
static AAudioStream *outputStream = nullptr;

static aaudio_data_callback_result_t
onAudioCallback(AAudioStream*, void*, void* audioData, int32_t numFrames) {
    int64_t timeoutNanos = 0;
    aaudio_result_t r = AAudioStream_read(inputStream,
                                          audioData,
                                          numFrames,
                                          timeoutNanos);
    if (r < 0) {
        return AAUDIO_CALLBACK_RESULT_STOP;
    }
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_stagecue_MainActivity_startAudioEngine(JNIEnv*, jobject) {
    AAudioStreamBuilder *builder = nullptr;
    AAUDIO_CHECK( AAudio_createStreamBuilder(&builder) );

    // 1) input (microfono)
    AAudioStreamBuilder_setDirection(builder, AAUDIO_DIRECTION_INPUT);
    AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_FLOAT);
    AAudioStreamBuilder_setSampleRate(builder, 48000);
    AAudioStreamBuilder_setChannelCount(builder, 1);
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAUDIO_CHECK( AAudioStreamBuilder_openStream(builder, &inputStream) );
    AAUDIO_CHECK( AAudioStream_requestStart(inputStream) );

    // 2) output (speaker) con callback
    AAudioStreamBuilder_setDirection(builder, AAUDIO_DIRECTION_OUTPUT);
    AAudioStreamBuilder_setUsage(builder, AAUDIO_USAGE_VOICE_COMMUNICATION);
    AAudioStreamBuilder_setContentType(builder, AAUDIO_CONTENT_TYPE_SPEECH);
    AAudioStreamBuilder_setDataCallback(builder, onAudioCallback, nullptr);
    AAUDIO_CHECK( AAudioStreamBuilder_openStream(builder, &outputStream) );
    AAUDIO_CHECK( AAudioStream_requestStart(outputStream) );

    AAudioStreamBuilder_delete(builder);
    LOGI("AudioEngine started");
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_stagecue_MainActivity_stopAudioEngine(JNIEnv*, jobject) {
    if (inputStream) {
        AAudioStream_requestStop(inputStream);
        AAudioStream_close(inputStream);
        inputStream = nullptr;
    }
    if (outputStream) {
        AAudioStream_requestStop(outputStream);
        AAudioStream_close(outputStream);
        outputStream = nullptr;
    }
    LOGI("AudioEngine stopped");
}
