package com.e.sonic_attendance_proto.screen;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class ScreenRecordService extends Service {

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
    private Intent reuse;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service onCreate() is called");



        //addNotification();
        //removeNotification();


    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand() is called");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            String channelId = "001";
//            String channelName = "FOREGROUND";
//            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
//            channel.setLightColor(Color.TRANSPARENT);
//            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            //channel.setImportance(NotificationManager.IMPORTANCE_MAX);
            //channel.setImportance(NotificationManager.IMPORTANCE_LOW);
            //channel.setShowBadge(false);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
              //  manager.createNotificationChannel(channel);
                Notification notification;

//                notification = new Notification.
//                        Builder(getApplicationContext(), channelId).setOngoing(true).setSmallIcon(R.mipmap.ic_launcher).setCategory(Notification.CATEGORY_SERVICE).build();

                notification = new Notification();
                startForeground(101, notification);


               // manager.cancel(001);


            }
        } else {
            //startForeground(101, new Notification());
        }
        reuse = intent;


        mResultCode = intent.getIntExtra("code", -1);
        mResultData = intent.getParcelableExtra("data");
        mScreenWidth = intent.getIntExtra("width", 720);
        mScreenHeight = intent.getIntExtra("height", 1280);
        mScreenDensity = intent.getIntExtra("density", 1);
        isVideoSd = intent.getBooleanExtra("quality", true);
        isAudio = intent.getBooleanExtra("audio", true);
        //isAudio = true;

        mMediaProjection = createMediaProjection();
        mMediaRecorder = createMediaRecorder();
        // 必须在mediaRecorder.prepare() 之后调用，否则报错"fail to get surface"
        mVirtualDisplay = createVirtualDisplay();


        Toast.makeText(getApplicationContext(), "START Recording", Toast.LENGTH_LONG).show();


        mMediaRecorder.start();





        return Service.START_STICKY;
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
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/tmp");
        file.mkdirs();

        mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/tmp/tmp1.sys");

        if(mScreenWidth>mScreenHeight)
        {
            int buf = mScreenHeight;
            mScreenHeight = mScreenWidth;
            mScreenWidth = buf;

        }

        mediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);  //after setVideoSource(), setOutFormat()


        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);  //after setOutputFormat()
        if (isAudio) {
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);  //after setOutputFormat()
        }
        int bitRate;
        if (isVideoSd) {
            mediaRecorder.setVideoEncodingBitRate(mScreenWidth * mScreenHeight/10);
            mediaRecorder.setVideoFrameRate(5);
            mediaRecorder.setCaptureRate(5);
            bitRate = mScreenWidth * mScreenHeight / 100000;
        } else {
//            mediaRecorder.setVideoEncodingBitRate(5 * mScreenWidth * mScreenHeight);
//            mediaRecorder.setVideoFrameRate(10); //after setVideoSource(), setOutFormat()
//            bitRate = 5 * mScreenWidth * mScreenHeight / 1000;
            mediaRecorder.setVideoEncodingBitRate(mScreenWidth * mScreenHeight/10);
            mediaRecorder.setVideoFrameRate(5);
            mediaRecorder.setCaptureRate(5);

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
        //stopForeground(true);
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

        Toast.makeText(getApplicationContext(), "Finish Service", Toast.LENGTH_LONG).show();

        setFileName("tmp");

//        Intent service = new Intent(this, GlueService.class);
//        //Intent service = new Intent(this, SimpleJobIntentService.class);
//        service.putExtra("code", mResultCode);
//        service.putExtra("data", mResultData);
//        service.putExtra("audio", isAudio);
//        service.putExtra("width", mScreenWidth);
//        service.putExtra("height", mScreenHeight);
//        service.putExtra("density", mScreenDensity);
//        service.putExtra("quality", isVideoSd);
//        Log.d("d","NEED FOR GLUE : reattempt start service");
//        startService(service);

    }
    private void setFileName(String text) {
        String currentFileName = "/tmp1.sys";
        Log.i("Current file name", currentFileName);

        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/tmp");
        File from      = new File(directory, currentFileName);
        File to        = new File(directory, text.trim() + ".sys");
        from.renameTo(to);
        Log.i("Directory is", directory.toString());
//        Log.i("Default path is", videoURI.toString());
        Log.i("From path is", from.toString());
        Log.i("To path is", to.toString());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
