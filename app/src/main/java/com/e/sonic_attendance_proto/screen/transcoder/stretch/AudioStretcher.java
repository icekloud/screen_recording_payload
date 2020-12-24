package com.e.sonic_attendance_proto.screen.transcoder.stretch;

import android.support.annotation.NonNull;

import com.e.sonic_attendance_proto.screen.transcoder.time.TimeInterpolator;

import java.nio.Buffer;
import java.nio.ShortBuffer;

/**
 * An AudioStretcher will change audio samples duration, in response to a
 * {@link TimeInterpolator} that altered the sample timestamp.
 *
 * This can mean either shrink the sample (in case of video speed up) or elongate it (in case of
 * video slow down) so that it matches the output size.
 */
public interface AudioStretcher {

    /**
     * Stretches the input into the output, based on the {@link Buffer#remaining()} value of both.
     * At the end of this method, the {@link Buffer#position()} of both should be equal to their
     * respective {@link Buffer#limit()}.
     *
     * And of course, both {@link Buffer#limit()}s should remain unchanged.
     *
     * @param input input buffer
     * @param output output buffer
     * @param channels audio channels
     */
    void stretch(@NonNull ShortBuffer input, @NonNull ShortBuffer output, int channels);

    AudioStretcher PASSTHROUGH = new PassThroughAudioStretcher();

    AudioStretcher CUT = new CutAudioStretcher();

    AudioStretcher INSERT = new InsertAudioStretcher();
}
