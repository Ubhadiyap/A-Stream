cmake_minimum_required(VERSION 3.4.1)
set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

#FFmpeg include

include_directories(src/main/cpp)
link_directories(src/main/jniLibs/${ANDROID_ABI})

add_library(
            auth-jni
            SHARED
            src/main/cpp/auth-jni.c )
target_link_libraries(
                       auth-jni
                       log )

add_library(
            ffmpeg-jni
            SHARED
            src/main/cpp/ffmpeg-jni.c )
target_link_libraries(
                       ffmpeg-jni
                       log avcodec-57 avdevice-57 avfilter-6 avformat-57 avutil-55 swresample-2 swscale-4 crypto rtmp-1)