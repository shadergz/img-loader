#include <cassert>
#include <string_view>

#include <jni.h>
#include <android/log.h>

[[maybe_unused]] constexpr std::string_view gLoadImageTag("libimage:Load Image");

extern "C"
JNIEXPORT void JNICALL
Java_com_beloncode_imgloader_MainActivity_loadImageDocument(JNIEnv* env,
                                                            jobject
                                                            javaContext, jstring uriPath) {
    assert(env != nullptr && javaContext != nullptr && uriPath != nullptr);
}