package com.e.sonic_attendance_proto.screen.transcoder.validator;

import android.support.annotation.NonNull;

import com.e.sonic_attendance_proto.screen.transcoder.engine.TrackStatus;

/**
 * A {@link Validator} that gives priority to the video track.
 * Transcoding will not happen if the video track does not need it, even if the
 * audio track might need it.
 */
@SuppressWarnings("unused")
public class WriteVideoValidator implements Validator {

    @Override
    public boolean validate(@NonNull TrackStatus videoStatus, @NonNull TrackStatus audioStatus) {
        switch (videoStatus) {
            case ABSENT: return false;
            case REMOVING: return true;
            case COMPRESSING: return true;
            case PASS_THROUGH: return false;
        }
        return true;
    }
}
