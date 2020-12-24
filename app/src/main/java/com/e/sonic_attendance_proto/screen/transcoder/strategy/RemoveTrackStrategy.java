package com.e.sonic_attendance_proto.screen.transcoder.strategy;

import android.media.MediaFormat;

import android.support.annotation.NonNull;

import com.e.sonic_attendance_proto.screen.transcoder.engine.TrackStatus;

import java.util.List;

/**
 * An {@link TrackStrategy} that removes this track from output.
 */
@SuppressWarnings("unused")
public class RemoveTrackStrategy implements TrackStrategy {

    @NonNull
    @Override
    public TrackStatus createOutputFormat(@NonNull List<MediaFormat> inputFormats, @NonNull MediaFormat outputFormat) {
        return TrackStatus.REMOVING;
    }
}
