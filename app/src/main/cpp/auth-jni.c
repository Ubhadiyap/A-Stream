//
// Created by ultra on 13.04.2016.
//

#include <android/log.h>
#include <jni.h>
#include <string.h>

#define LOGD(...)   __android_log_print(ANDROID_LOG_DEBUG, "YouLiveOnStreamTag", __VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jstring JNICALL Java_com_someoneman_youliveonstream_AppClientInfo_getCID
    (JNIEnv *env, jobject thiz);

JNIEXPORT jstring JNICALL Java_com_someoneman_youliveonstream_AppClientInfo_getCS
    (JNIEnv *env, jobject thiz);

#ifdef __cplusplus
}
#endif

JNIEXPORT jstring JNICALL Java_com_someoneman_youliveonstream_AppClientInfo_getCID
    (JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "apps.googleusercontent.com");
}

JNIEXPORT jstring JNICALL Java_com_someoneman_youliveonstream_AppClientInfo_getCS
        (JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "");
}