package com.e.sonic_attendance_proto.screen.transcoder.time;

import android.support.annotation.NonNull;

import com.e.sonic_attendance_proto.screen.transcoder.engine.TrackType;

/**
 * An interface to redefine the time between video or audio frames.
 */
public interface TimeInterpolator {

    /**
     * Given the track type (audio or video) and the frame timestamp in microseconds,
     * should return the corrected timestamp.
     *
     * @param type track type
     * @param time frame timestamp in microseconds
     * @return the new frame timestamp
     */
    long interpolate(@NonNull TrackType type, long time);
}
