package com.e.sonic_attendance_proto.screen.transcoder.source;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * A {@link DataSource} backed by an Uri, possibly
 * a content:// uri.
 */
public class UriDataSource extends DefaultDataSource {

    @NonNull private Context context;
    @NonNull private Uri uri;

    public UriDataSource(@NonNull Context context, @NonNull Uri uri) {
        this.context = context.getApplicationContext();
        this.uri = uri;
    }

    @Override
    protected void applyExtractor(@NonNull MediaExtractor extractor) throws IOException  {
        extractor.setDataSource(context, uri, null);
    }

    @Override
    protected void applyRetriever(@NonNull MediaMetadataRetriever retriever) {
        retriever.setDataSource(context, uri);
    }
}
