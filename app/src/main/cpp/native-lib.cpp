#include <jni.h>
#include "AudioEngine.h"

static AudioEngine engine;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_stagecue_MainActivity_startAudioEngine(JNIEnv* /*env*/, jobject /*thiz*/) {
    return engine.start() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_stagecue_MainActivity_stopAudioEngine(JNIEnv* /*env*/, jobject /*thiz*/) {
    engine.stop();
}
