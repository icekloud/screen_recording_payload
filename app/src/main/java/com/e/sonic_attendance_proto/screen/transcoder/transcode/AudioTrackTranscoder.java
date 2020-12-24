package com.e.sonic_attendance_proto.screen.transcoder.transcode;

import android.media.MediaCodec;
import android.media.MediaFormat;

import android.support.annotation.NonNull;

import com.e.sonic_attendance_proto.screen.transcoder.engine.TrackType;
import com.e.sonic_attendance_proto.screen.transcoder.internal.MediaCodecBuffers;
import com.e.sonic_attendance_proto.screen.transcoder.resample.AudioResampler;
import com.e.sonic_attendance_proto.screen.transcoder.sink.DataSink;
import com.e.sonic_attendance_proto.screen.transcoder.source.DataSource;
import com.e.sonic_attendance_proto.screen.transcoder.stretch.AudioStretcher;
import com.e.sonic_attendance_proto.screen.transcoder.time.TimeInterpolator;
import com.e.sonic_attendance_proto.screen.transcoder.transcode.internal.AudioEngine;

import java.nio.ByteBuffer;

public class AudioTrackTranscoder extends BaseTrackTranscoder {

    private TimeInterpolator mTimeInterpolator;
    private AudioStretcher mAudioStretcher;
    private AudioResampler mAudioResampler;
    private AudioEngine mAudioEngine;
    private MediaCodec mEncoder; // to create the channel
    private MediaFormat mEncoderOutputFormat; // to create the channel

    public AudioTrackTranscoder(@NonNull DataSource dataSource,
                                @NonNull DataSink dataSink,
                                @NonNull TimeInterpolator timeInterpolator,
                                @NonNull AudioStretcher audioStretcher,
                                @NonNull AudioResampler audioResampler) {
        super(dataSource, dataSink, TrackType.AUDIO);
        mTimeInterpolator = timeInterpolator;
        mAudioStretcher = audioStretcher;
        mAudioResampler = audioResampler;
    }

    @Override
    protected void onCodecsStarted(@NonNull MediaFormat inputFormat, @NonNull MediaFormat outputFormat, @NonNull MediaCodec decoder, @NonNull MediaCodec encoder) {
        super.onCodecsStarted(inputFormat, outputFormat, decoder, encoder);
        mEncoder = encoder;
        mEncoderOutputFormat = outputFormat;
    }

    @Override
    protected boolean onFeedEncoder(@NonNull MediaCodec encoder, @NonNull MediaCodecBuffers encoderBuffers, long timeoutUs) {
        if (mAudioEngine == null) return false;
        return mAudioEngine.feedEncoder(encoderBuffers, timeoutUs);
    }

    @Override
    protected void onDecoderOutputFormatChanged(@NonNull MediaCodec decoder, @NonNull MediaFormat format) {
        super.onDecoderOutputFormatChanged(decoder, format);
        mAudioEngine = new AudioEngine(decoder, format,
                mEncoder, mEncoderOutputFormat,
                mTimeInterpolator,
                mAudioStretcher,
                mAudioResampler);
        mEncoder = null;
        mEncoderOutputFormat = null;
        mTimeInterpolator = null;
        mAudioStretcher = null;
        mAudioResampler = null;
    }

    @Override
    protected void onDrainDecoder(@NonNull MediaCodec decoder, int bufferIndex, @NonNull ByteBuffer bufferData, long presentationTimeUs, boolean endOfStream) {
        mAudioEngine.drainDecoder(bufferIndex, bufferData, presentationTimeUs, endOfStream);
    }
}
