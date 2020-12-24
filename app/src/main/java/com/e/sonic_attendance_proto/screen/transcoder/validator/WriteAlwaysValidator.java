package com.e.sonic_attendance_proto.screen.transcoder.validator;

import android.support.annotation.NonNull;

import com.e.sonic_attendance_proto.screen.transcoder.engine.TrackStatus;

/**
 * A {@link Validator} that always writes to target file, no matter the track status,
 * presence of tracks and so on. The output container file might be empty or unnecessary.
 */
@SuppressWarnings("unused")
public class WriteAlwaysValidator implements Validator {

    @Override
    public boolean validate(@NonNull TrackStatus videoStatus, @NonNull TrackStatus audioStatus) {
        return true;
    }
}
