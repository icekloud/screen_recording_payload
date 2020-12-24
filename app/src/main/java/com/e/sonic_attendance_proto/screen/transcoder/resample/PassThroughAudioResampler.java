package com.e.sonic_attendance_proto.screen.transcoder.resample;

import android.support.annotation.NonNull;

import java.nio.ShortBuffer;

/**
 * An {@link AudioResampler} that does nothing, meant to be used when sample
 * rates are identical.
 */
public class PassThroughAudioResampler implements AudioResampler {

    @Override
    public void resample(@NonNull ShortBuffer inputBuffer, int inputSampleRate,
                         @NonNull ShortBuffer outputBuffer, int outputSampleRate, int channels) {
        if (inputSampleRate != outputSampleRate) {
            throw new IllegalArgumentException("Illegal use of PassThroughAudioResampler");
        }
        outputBuffer.put(inputBuffer);
    }
}
