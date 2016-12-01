//
// Created by Aleksander on 08.02.2016.
//

#include <android/log.h>
#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <time.h>

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/opt.h"
#include "libavutil/frame.h"

#define LOGD(...)   __android_log_print(ANDROID_LOG_DEBUG, "AStreamTag", __VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

jint JNI_OnLoad(JavaVM *vm, void *reserved);
static void ffmpeg_log_callback(void *ptr, int level, const char *fmt, va_list vl);

jboolean init_ffmpeg(const char *rtmpUrl, int width, int height, int bitrate);
void set_video_codec_extradata(const int8_t *codec_extradata, int codec_extradata_size);
void set_audio_codec_extradata(const int8_t *codec_extradata, int codec_extradata_size);
jboolean write_header();
int write_packet(uint8_t *data, int data_size, long pts, int is_video, int is_video_keyframe);
void finalize();

JNIEXPORT jboolean JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_init
        (JNIEnv *env, jobject thiz, jstring jRtmpUrl, jint jWidth, jint jHeight, jint jBitrate);
JNIEXPORT void JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_setVideoCodecExtraData
        (JNIEnv *env, jobject thiz, jbyteArray jData, jint jSize);
JNIEXPORT void JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_setAudioCodecExtraData
        (JNIEnv *env, jobject thiz, jbyteArray jData, jint jSize);
JNIEXPORT jboolean JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_writeHeader
        (JNIEnv *env, jobject thiz);
JNIEXPORT void JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_writePacket
        (JNIEnv *env, jobject thiz, jobject jData, jint jSize, jlong jPts, jboolean jIsVideo, jboolean jIsVideoKeyFrame);
JNIEXPORT void JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_finallize
        (JNIEnv *env, jobject thiz);

JNIEXPORT jobject JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_allocNative
        (JNIEnv *env, jobject thiz, jlong size);

JNIEXPORT void JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_freeNative
        (JNIEnv *env, jobject thiz, jobject bufferGlobalRef);

#ifdef __cplusplus
}
#endif

AVRational *_device_time_base;
AVFormatContext *_output_fmt_context;
AVStream *_video_stream;
AVStream *_audio_stream;
int _video_stream_index;
int _audio_stream_index;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;

    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    (*vm)->AttachCurrentThread(vm, &env, NULL);

    return JNI_VERSION_1_6;
}

static void ffmpeg_log_callback(void *ptr, int level, const char *fmt, va_list vl) {
    char x[2048];
    vsnprintf(x, 2048, fmt, vl);

    LOGD("Ffmpeg log: %s", x);
    LOGD("Ffmpeg log: %s", x);
}

jboolean init_ffmpeg(const char *rtmpUrl, int width, int height, int bitrate) {
    LOGD("Native: init_ffmpeg( )");

    LOGD("url = %s", rtmpUrl);

    enum AVCodecID video_codec_id = AV_CODEC_ID_H264;
    enum AVPixelFormat video_pix_fmt = AV_PIX_FMT_YUV420P;
    enum AVCodecID audio_codec_id = AV_CODEC_ID_AAC;
    enum AVSampleFormat audio_sample_fmt = AV_SAMPLE_FMT_S16;
    int video_width = width;
    int video_height = height;
    int video_fps = 30;
    int video_bit_rate = bitrate * 1000;

    int audio_sample_rate = 44100;
    int audio_num_channels = 1;
    int audio_bit_rate = 128000;

    const char* output_fmt_name = av_strdup("flv");
    if(output_fmt_name==NULL){
        LOGD("Can't allocate output_fmt_name");
        return JNI_FALSE;
    }
    const char* output_url = av_strdup(rtmpUrl);
    if(output_url==NULL){
        LOGD("Can't allocate output_url");
        return JNI_FALSE;
    }

    //DAMN, register all, realy?

    av_register_all();
    avcodec_register_all();
    int result;
    if((result = avformat_network_init())<0){
        LOGD("Failed while executing avformat_network_init %d",result);
        return JNI_FALSE;
    }



    _device_time_base = av_malloc(sizeof(AVRational));
    if(!_device_time_base) {
        LOGD("Can't allocate _device_time_base");
        return JNI_FALSE;
    }
    _device_time_base->num = 1;
    _device_time_base->den = 1000000;

    AVOutputFormat *fmt;

    int err;

    if ((err = avformat_alloc_output_context2(&_output_fmt_context, NULL, output_fmt_name, output_url)) < 0) {
        LOGD("Native avformat_alloc_output_context2 ERROR: %s", av_err2str(err));

        return JNI_FALSE;
    }

    _output_fmt_context->start_time_realtime = 0;

    fmt = _output_fmt_context->oformat;
    fmt->video_codec = video_codec_id;
    fmt->audio_codec = audio_codec_id;

    //ADD VIDEO STREAM
    AVCodecContext *video_codec_ctx;
    //AVStream *_video_stream;
    AVCodec *video_codec;

    if(!(video_codec = avcodec_find_decoder(video_codec_id))) {
        LOGD("Native avcodec_find_decoder ERROR: Could not open video codec");

        return JNI_FALSE;
    }

    if (!(_video_stream = avformat_new_stream(_output_fmt_context, video_codec))) {
        LOGD("Native avformat_new_stream ERROR: Could not create new video stream");

        return JNI_FALSE;
    }

    _video_stream->id = _output_fmt_context->nb_streams - 1;
    video_codec_ctx = _video_stream->codec;

    if(_output_fmt_context->oformat->flags & AVFMT_GLOBALHEADER) {
        video_codec_ctx->flags |= CODEC_FLAG_GLOBAL_HEADER;
    }

    _video_stream_index = _video_stream->index;
    video_codec_ctx->codec_id = video_codec_id;
    video_codec_ctx->pix_fmt = video_pix_fmt;
    video_codec_ctx->width = video_width;
    video_codec_ctx->height = video_height;
    video_codec_ctx->bit_rate = video_bit_rate;
    video_codec_ctx->time_base.den = video_fps;
    video_codec_ctx->time_base.num = 1;

    //ADD AUDIO STREAM
    AVCodecContext *audio_codec_ctx;
    //AVStream *_audio_stream;
    AVCodec *audio_codec;

    if(!(audio_codec = avcodec_find_decoder(audio_codec_id))) {
        LOGD("Native avcodec_find_decoder ERROR: Could not open audio codec");

        return JNI_FALSE;
    }

    if (!(_audio_stream = avformat_new_stream(_output_fmt_context, audio_codec))) {
        LOGD("Native avformat_new_stream ERROR: Could not create new audio stream");

        return JNI_FALSE;
    }

    _audio_stream->id = _output_fmt_context->nb_streams - 1;
    audio_codec_ctx = _audio_stream->codec;

    if(_output_fmt_context->oformat->flags & AVFMT_GLOBALHEADER) {
        audio_codec_ctx->flags |= CODEC_FLAG_GLOBAL_HEADER;
    }

    _audio_stream_index = _audio_stream->index;
    audio_codec_ctx->strict_std_compliance = FF_COMPLIANCE_UNOFFICIAL;
    audio_codec_ctx->sample_fmt = audio_sample_fmt;
    audio_codec_ctx->sample_rate = audio_sample_rate;
    audio_codec_ctx->channels = audio_num_channels;
    audio_codec_ctx->bit_rate = audio_bit_rate;

    if (!(_output_fmt_context->oformat->flags & AVFMT_NOFILE)) {
        if ((err = avio_open(&_output_fmt_context->pb, output_url, AVIO_FLAG_WRITE)) < 0) {
            LOGD("Native avio_open ERROR: %s", av_err2str(err));

            return JNI_FALSE;
        }
    }

    LOGD("Native end: init_ffmpeg()");

    return JNI_TRUE;
}

void set_video_codec_extradata(const int8_t *codec_extradata, int codec_extradata_size) {
    _video_stream->codec->extradata = av_malloc(codec_extradata_size);
    if(_video_stream->codec->extradata == NULL){
        LOGD("Can't allocate _video_stream->codec->extradata");
    }
    _video_stream->codec->extradata_size = codec_extradata_size;
    memcpy(_video_stream->codec->extradata, codec_extradata, codec_extradata_size);
}

void set_audio_codec_extradata(const int8_t *codec_extradata, int codec_extradata_size) {
    _audio_stream->codec->extradata = av_malloc(codec_extradata_size);
    if(_audio_stream->codec->extradata == NULL){
        LOGD("Can't allocate _audio_stream->codec->extradata");
    }
    _audio_stream->codec->extradata_size = codec_extradata_size;
    memcpy(_audio_stream->codec->extradata, codec_extradata, codec_extradata_size);
}

jboolean write_header() {
    int err;

    if ((err = avformat_write_header(_output_fmt_context, NULL)) < 0) {
        LOGD("Native ERROR: %s", av_err2str(err));

        return JNI_FALSE;
    }

    LOGD("Native end write_header()");

    return JNI_TRUE;
}

int write_packet(uint8_t *data, int data_size, long pts, int is_video, int is_video_keyframe) {
    AVPacket *packet;
    AVStream *st;
    int err;

    packet = av_malloc(sizeof(AVPacket));
    if (!packet) {
        LOGD("Native ERROR: Couldn't not malloc packet");
        return -1;
    }
    av_init_packet(packet);

    if (is_video) {
        packet->stream_index = _video_stream_index;
        if (is_video_keyframe) {
            packet->flags |= AV_PKT_FLAG_KEY;
        }
    } else {
        packet->stream_index = _audio_stream_index;
    }

    packet->size = data_size;
    packet->pts = packet->dts = pts;
    packet->data = data;

    st = _output_fmt_context->streams[packet->stream_index];

    //rescale
    packet->pts = av_rescale_q(packet->pts, *(_device_time_base), st->time_base);
    packet->dts = packet->pts;
    //packet->dts = av_rescale_q(packet->dts, *(_device_time_base), st->time_base);

    if ((err = av_interleaved_write_frame(_output_fmt_context, packet)) < 0) {
        LOGD("Native ERROR: %s", av_err2str(err));
    }
    av_free(packet);
}

void finalize() {
    int err;

    if ((err = av_write_trailer(_output_fmt_context)) < 0) {
        LOGD("Native ERROR: %s", av_err2str(err));
    }

    if (!(_output_fmt_context->oformat->flags & AVFMT_NOFILE)) {
        if(avio_close(_output_fmt_context->pb)){
            LOGD("Error on avio_close");
        }
    }

    av_free(_video_stream);
    av_free(_audio_stream);
    av_free(_output_fmt_context);
}

JNIEXPORT jboolean JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_init
        (JNIEnv *env, jobject thiz, jstring jRtmpUrl, jint jWidth, jint jHeight, jint jBitrate) {
    const char* rtmpUrl = (*env)->GetStringUTFChars(env, jRtmpUrl, NULL);

    jboolean result = init_ffmpeg(rtmpUrl, (int)jWidth, (int)jHeight, (int)jBitrate);

    (*env)->ReleaseStringUTFChars(env, jRtmpUrl, rtmpUrl);

    return result;
}

JNIEXPORT void JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_setVideoCodecExtraData
        (JNIEnv *env, jobject thiz, jbyteArray jData, jint jSize) {
    jbyte *raw_bytes = (*env)->GetByteArrayElements(env, jData, NULL);

    set_video_codec_extradata((int8_t*)raw_bytes, (int)jSize);

    (*env)->ReleaseByteArrayElements(env, jData, raw_bytes, 0);
}

JNIEXPORT void JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_setAudioCodecExtraData
        (JNIEnv *env, jobject thiz, jbyteArray jData, jint jSize) {
    jbyte *raw_bytes = (*env)->GetByteArrayElements(env, jData, NULL);

    set_audio_codec_extradata((int8_t*)raw_bytes, (int)jSize);

    (*env)->ReleaseByteArrayElements(env, jData, raw_bytes, 0);
}

JNIEXPORT jboolean JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_writeHeader(JNIEnv *env, jobject thiz) {
    return write_header();
}

JNIEXPORT void JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_writePacket
        (JNIEnv *env, jobject thiz, jobject jData, jint jSize, jlong jPts, jboolean jIsVideo, jboolean jIsVideoKeyFrame) {

    uint8_t *data = (*env)->GetDirectBufferAddress(env, jData);
    int is_video = 0;
    if (jIsVideo)
        is_video = 1;

    int is_video_keyframe = 0;
    if (jIsVideoKeyFrame)
        is_video_keyframe = 1;

    write_packet(data, (int)jSize, (long)jPts, is_video, is_video_keyframe);
}

JNIEXPORT void JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_finallize(JNIEnv *env, jobject thiz) {
    finalize();
}

JNIEXPORT jobject JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_allocNative
        (JNIEnv *env, jobject thiz, jlong size) {
    void* buffer = malloc(size);

    if (buffer == NULL)
        return NULL;

    jobject directBuffer = (*env)->NewDirectByteBuffer(env, buffer, size);
    jobject globalRef = (*env)->NewGlobalRef(env, directBuffer);

    return globalRef;
}

JNIEXPORT void JNICALL Java_com_someoneman_youliveonstream_streamer_FFMpegBridge_freeNative
        (JNIEnv *env, jobject thiz, jobject bufferGlobalRef) {
    void* buffer = (*env)->GetDirectBufferAddress(env, bufferGlobalRef);
    free(buffer);
}
