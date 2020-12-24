package com.e.sonic_attendance_proto.screen.transcoder.time;

import android.support.annotation.NonNull;

import com.e.sonic_attendance_proto.screen.transcoder.engine.TrackType;

/**
 * A {@link TimeInterpolator} that does no time interpolation or correction -
 * it just returns the input time.
 */
public class DefaultTimeInterpolator implements TimeInterpolator {

    @Override
    public long interpolate(@NonNull TrackType type, long time) {
        return time;
    }
}
