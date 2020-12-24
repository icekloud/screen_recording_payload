package com.e.sonic_attendance_proto.screen.transcoder.source;

import android.media.MediaExtractor;
import android.media.MediaMetadataRetriever;

import android.support.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * A {@link DataSource} backed by a file descriptor.
 */
public class FileDescriptorDataSource extends DefaultDataSource {

    @NonNull
    private FileDescriptor descriptor;

    public FileDescriptorDataSource(@NonNull FileDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    protected void applyExtractor(@NonNull MediaExtractor extractor) throws IOException  {
        extractor.setDataSource(descriptor);
    }

    @Override
    protected void applyRetriever(@NonNull MediaMetadataRetriever retriever) {
        retriever.setDataSource(descriptor);
    }
}
