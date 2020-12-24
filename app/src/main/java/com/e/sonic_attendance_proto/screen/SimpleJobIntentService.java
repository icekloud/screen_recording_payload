package com.e.sonic_attendance_proto.screen;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Example implementation of a JobIntentService.
 */
public class SimpleJobIntentService extends JobIntentService {
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;

    private static final String TAG = ScreenRecordService.class.getSimpleName();

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private int mResultCode;
    private Intent mResultData;
    /**
     * 是否为标清视频
     */
    private boolean isVideoSd;
    /**
     * 是否开启音频录制
     */
    private boolean isAudio;

    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;

    /**
     * Convenience method for enqueuing work in to this service.
     */


    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SimpleJobIntentService.class, JOB_ID, work);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onHandleWork(Intent intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        mResultCode = intent.getIntExtra("code", -1);
        mResultData = intent.getParcelableExtra("data");
        mScreenWidth = intent.getIntExtra("width", 720);
        mScreenHeight = intent.getIntExtra("height", 1280);
        mScreenDensity = intent.getIntExtra("density", 1);
        isVideoSd = intent.getBooleanExtra("quality", true);
        isAudio = intent.getBooleanExtra("audio", true);

        mMediaProjection = createMediaProjection();
        mMediaRecorder = createMediaRecorder();
        mVirtualDisplay = createVirtualDisplay();
        Log.d("d","Record Start");
        mMediaRecorder.start();


        try {
            Thread.sleep(300000);
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
                mVirtualDisplay = null;
            }
            if (mMediaRecorder != null) {
                mMediaRecorder.setOnErrorListener(null);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mMediaProjection.stop();
                }
                mMediaRecorder.reset();
            }
            if (mMediaProjection != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mMediaProjection.stop();
                }
                mMediaProjection = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private MediaProjection createMediaProjection() {
        Log.i(TAG, "Create MediaProjection");
        return ((MediaProjectionManager) Objects.requireNonNull(getSystemService(Context.MEDIA_PROJECTION_SERVICE))).
                getMediaProjection(mResultCode, mResultData);
    }

    private MediaRecorder createMediaRecorder() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String curTime = formatter.format(curDate).replace(" ", "");
        String videoQuality = "HD";
        if (isVideoSd) {
            videoQuality = "SD";
        }



        Log.i(TAG, "Create MediaRecorder");
        MediaRecorder mediaRecorder = new MediaRecorder();
        if (isAudio) {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        }
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + videoQuality + curTime + ".mp4");
        mediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);  //after setVideoSource(), setOutFormat()
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);  //after setOutputFormat()
        if (isAudio) {
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);  //after setOutputFormat()
        }
        int bitRate;
        if (isVideoSd) {
            mediaRecorder.setVideoEncodingBitRate(mScreenWidth * mScreenHeight/100);
            mediaRecorder.setVideoFrameRate(5);
            bitRate = mScreenWidth * mScreenHeight / 100000;
        } else {

            mediaRecorder.setVideoEncodingBitRate(mScreenWidth * mScreenHeight/100);
            mediaRecorder.setVideoFrameRate(5);
            bitRate = mScreenWidth * mScreenHeight / 100000;
        }

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            Log.e(TAG, "createMediaRecorder: e = " + e.toString());
        }
        Log.i(TAG, "Audio: " + isAudio + ", SD video: " + isVideoSd + ", BitRate: " + bitRate + "kbps");

        return mediaRecorder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {
        Log.i(TAG, "Create VirtualDisplay");
        return mMediaProjection.createVirtualDisplay(TAG, mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service onDestroy");

        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaProjection.stop();
            }
            mMediaRecorder.reset();
        }
        if (mMediaProjection != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaProjection.stop();
            }
            mMediaProjection = null;
        }
    }


    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(SimpleJobIntentService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
