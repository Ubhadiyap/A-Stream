LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := auth-jni.c
LOCAL_MODULE    := auth-jni
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := ffmpeg-jni.c
LOCAL_MODULE    := ffmpeg-jni
LOCAL_LDLIBS    := -llog -L$(LOCAL_PATH)/../jniLibs/$(TARGET_ARCH_ABI) -lavcodec-57 -lavdevice-57 -lavfilter-6 -lavformat-57 -lavutil-55 -lcrypto -lssl -lrtmp-1
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
include $(BUILD_SHARED_LIBRARY)