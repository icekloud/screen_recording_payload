package com.e.sonic_attendance_proto.screen.transcoder.strategy;

import android.media.MediaFormat;

import android.support.annotation.NonNull;

import com.e.sonic_attendance_proto.screen.transcoder.engine.TrackStatus;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.size.Resizer;

import java.util.List;

/**
 * Base class for video/audio format strategy.
 * Video strategies should use a {@link Resizer} instance to compute the output
 * video size.
 */
public interface TrackStrategy {

    /**
     * Create the output format for this track (either audio or video).
     * Implementors should fill the outputFormat object and return a non-null {@link TrackStatus}:
     * - {@link TrackStatus#COMPRESSING}: we want to compress this track. Output format will be used
     * - {@link TrackStatus#PASS_THROUGH}: we want to use the input format. Output format will be ignored
     * - {@link TrackStatus#REMOVING}: we want to remove this track. Output format will be ignored
     *
     * Subclasses can also throw to abort the whole transcoding operation.
     *
     * @param inputFormats the input formats
     * @param outputFormat the output format to be filled
     * @return the track status
     */
    @NonNull
    TrackStatus createOutputFormat(@NonNull List<MediaFormat> inputFormats, @NonNull MediaFormat outputFormat);
}
