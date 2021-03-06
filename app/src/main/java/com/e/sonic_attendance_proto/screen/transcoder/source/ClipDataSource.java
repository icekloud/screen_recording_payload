package com.e.sonic_attendance_proto.screen.transcoder.source;

import android.support.annotation.NonNull;

/**
 * A {@link DataSource} that clips the inner source within the given interval.
 */
@SuppressWarnings("unused")
public class ClipDataSource extends DataSourceWrapper {

    public ClipDataSource(@NonNull DataSource source, long clipStartUs) {
        super(new TrimDataSource(source, clipStartUs));
    }

    public ClipDataSource(@NonNull DataSource source, long clipStartUs, long clipEndUs) {
        super(new TrimDataSource(source,
                clipStartUs,
                source.getDurationUs() - clipEndUs));
    }
}
