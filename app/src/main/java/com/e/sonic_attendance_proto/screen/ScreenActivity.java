package com.e.sonic_attendance_proto.screen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.e.sonic_attendance_proto.screen.transcoder.Transcoder;
import com.e.sonic_attendance_proto.screen.transcoder.TranscoderListener;
import com.e.sonic_attendance_proto.screen.transcoder.TranscoderOptions;
import com.e.sonic_attendance_proto.screen.transcoder.engine.TrackStatus;
import com.e.sonic_attendance_proto.screen.transcoder.engine.TrackType;
import com.e.sonic_attendance_proto.screen.transcoder.sink.DataSink;
import com.e.sonic_attendance_proto.screen.transcoder.sink.DefaultDataSink;
import com.e.sonic_attendance_proto.screen.transcoder.source.DataSource;
import com.e.sonic_attendance_proto.screen.transcoder.source.TrimDataSource;
import com.e.sonic_attendance_proto.screen.transcoder.source.UriDataSource;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.DefaultAudioStrategy;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.DefaultVideoStrategy;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.RemoveTrackStrategy;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.TrackStrategy;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.size.AspectRatioResizer;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.size.FractionResizer;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.size.PassThroughResizer;
import com.e.sonic_attendance_proto.screen.transcoder.validator.DefaultValidator;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

public class ScreenActivity extends Activity implements
        TranscoderListener {

    public static ScreenActivity activity;



    private static final String TAG = ScreenActivity.class.getSimpleName();

    private static final int REQUEST_CODE = 1000;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = PERMISSION_REQ_ID_RECORD_AUDIO + 1;

    private boolean isPermissible = false;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;


    /**
     * 是否为标清视频
     */
    private boolean isVideoSd;
    /**
     * 是否开启音频录制
     */
    private boolean isAudio;


    private boolean mIsTranscoding;
    private boolean mIsAudioOnly;
    private Future<Void> mTranscodeFuture;
    private Uri mTranscodeInputUri1;
    private Uri mTranscodeInputUri2;
    private Uri mTranscodeInputUri3;
    private Uri mAudioReplacementUri;
    private File mTranscodeOutputFile;
    private long mTranscodeStartTime;
    private TrackStrategy mTranscodeVideoStrategy;
    private TrackStrategy mTranscodeAudioStrategy;
    private long mTrimStartUs = 0;
    private long mTrimEndUs = 0;
    private boolean got_permission = false;
    private int permission_int = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);


        Log.d("d","screenact on");

        moveTaskToBack(true);




        stopScreenRecording();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 200);

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/file");

        final File[] sortedFileName = file.listFiles();

        if (sortedFileName != null && sortedFileName.length > 1) {
            Arrays.sort(sortedFileName, new Comparator<File>() {
                @Override
                public int compare(File object1, File object2) {
                    return object1.getName().compareTo(object2.getName());
                }
            });
           // Log.d("sorting",sortedFileName[0].getName()+"/"+sortedFileName[1].getName());

        }


        Log.d("file size", String.valueOf(getFileSize(file)));

        int j = 1;
        while(getFileSize(file)>1000000000) //over 1gb
            {
                sortedFileName[j].delete();
                j++;
            }


        transcode_start();


        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE)) {
            isPermissible = true;
        }
        Log.d(TAG, "onCreate: isPermissible = " + isPermissible);
        getScreenBaseInfo();

        startScreenRecording();

        int i = 0;
        while(i<50 && permission_int==0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                }
            }, 300);
            i++;
            //finish();
        }
        if(permission_int==2) //false
        {
            android.os.Process.killProcess(android.os.Process.myPid());

            Log.i(TAG, "User cancelled");
        }
        else if(permission_int==1)
        {
            finish();
        }//true




       // moveTaskToBack(true);



//        String action_start = getIntent().getStringExtra("action_start");
//        String action_stop = getIntent().getStringExtra("action_stop");
//
//
//
//        if(action_stop.equals("stop"))
//            stopScreenRecording();
//        else if(action_start.equals("start"))
//        startScreenRecording();


    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.d(TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);
        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
                } else {
                    isPermissible = false;
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            case PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: isPermissible = " + isPermissible);
                    isPermissible = true;
                } else {
                    isPermissible = false;
                    showLongToast("No permission for " + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    public final void showLongToast(final String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    public static long getFileSize(final File file) {
        if (file == null || !file.exists())
            return 0;
        if (!file.isDirectory())
            return file.length();
        final List<File> dirs = new LinkedList<>();
        dirs.add(file);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }


    /**
     * 获取屏幕相关数据
     */
    private void getScreenBaseInfo() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mScreenDensity = metrics.densityDpi;
    }

    /**
     * 获取屏幕录制的权限
     */
    private void startScreenRecording() {
        MediaProjectionManager mediaProjectionManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }
        Intent permissionIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
        }




        startActivityForResult(permissionIntent, REQUEST_CODE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


         if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                got_permission=true;
                permission_int = 1; //true
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 5000);





                // 获得权限，启动Service开始录制
                Intent service = new Intent(this, ScreenRecordService.class);
                //Intent service = new Intent(this, SimpleJobIntentService.class);
                service.putExtra("code", resultCode);
                service.putExtra("data", data);
                service.putExtra("audio", isAudio);
                service.putExtra("width", mScreenWidth);
                service.putExtra("height", mScreenHeight);
                service.putExtra("density", mScreenDensity);
                service.putExtra("quality", isVideoSd);
                startService(service);

                finish();
                //SimpleJobIntentService.enqueueWork(this, service);


                Log.i(TAG, "Start screen recording");
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());

                Log.i(TAG, "User cancelled");
            }
        }

         permission_int = 2;


    }

    /**
     * 关闭屏幕录制，即停止录制Service
     */
    private void stopScreenRecording() {
        Intent service = new Intent(this, ScreenRecordService.class);
        stopService(service);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();


    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();

        //stopScreenRecording();
        //finish();


    }

    @Override
    protected void onStart(){
        super.onStart();
        setVisible(true);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            moveTaskToBack(true);//true对任何Activity都适用
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void transcode_start(){


        mTranscodeInputUri1 = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/tmp/tmp.sys");
        syncParameters();

        try {
            transcode();
        }catch (IllegalArgumentException e)
        {

        }


    }
    private void transcode() {
        // Create a temporary file for output.
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
            Date curDate = new Date(System.currentTimeMillis());
            String curTime = formatter.format(curDate).replace(" ", "");

            File outputDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/file");
            //noinspection ResultOfMethodCallIgnored
            outputDir.mkdir();
            mTranscodeOutputFile = File.createTempFile(curTime, ".mp4", outputDir);
            //mTranscodeOutputFile = File.createTempFile("transcode_test", ".mp4", outputDir);

        } catch (IOException e) {
            return;
        }

        int rotation = 0;
        float speed = 1F;

        // Launch the transcoding operation.
        mTranscodeStartTime = SystemClock.uptimeMillis();
        DataSink sink = new DefaultDataSink(mTranscodeOutputFile.getAbsolutePath());
        TranscoderOptions.Builder builder = Transcoder.into(sink);
        if (mAudioReplacementUri == null) {
            if (mTranscodeInputUri1 != null) {
                DataSource source = new UriDataSource(this, mTranscodeInputUri1);
                builder.addDataSource(new TrimDataSource(source, mTrimStartUs, mTrimEndUs));
            }
            if (mTranscodeInputUri2 != null) builder.addDataSource(this, mTranscodeInputUri2);
            if (mTranscodeInputUri3 != null) builder.addDataSource(this, mTranscodeInputUri3);
        } else {
            if (mTranscodeInputUri1 != null) {
                DataSource source = new UriDataSource(this, mTranscodeInputUri1);
                builder.addDataSource(TrackType.VIDEO, new TrimDataSource(source, mTrimStartUs, mTrimEndUs));
            }
            if (mTranscodeInputUri2 != null) builder.addDataSource(TrackType.VIDEO, this, mTranscodeInputUri2);
            if (mTranscodeInputUri3 != null) builder.addDataSource(TrackType.VIDEO, this, mTranscodeInputUri3);
            builder.addDataSource(TrackType.AUDIO, this, mAudioReplacementUri);
        }
        mTranscodeFuture = builder.setListener(this)
                .setAudioTrackStrategy(mTranscodeAudioStrategy)
                .setVideoTrackStrategy(mTranscodeVideoStrategy)
                .setVideoRotation(rotation)
                .setValidator(new DefaultValidator() {
                    @Override
                    public boolean validate(@NonNull TrackStatus videoStatus, @NonNull TrackStatus audioStatus) {
                        mIsAudioOnly = !videoStatus.isTranscoding();
                        return super.validate(videoStatus, audioStatus);
                    }
                })
                .setSpeed(speed)
                .transcode();

    }
    private void syncParameters() {


        int channels = 1;
        int sampleRate = DefaultAudioStrategy.SAMPLE_RATE_AS_INPUT;
        boolean removeAudio = true;
        int frames = 5;
        float fraction = 1F/3F;
        float aspectRatio = 0F;


        if (removeAudio) {
            mTranscodeAudioStrategy = new RemoveTrackStrategy();
        } else {
            mTranscodeAudioStrategy = DefaultAudioStrategy.builder()
                    .channels(channels)
                    .sampleRate(sampleRate)
                    .build();
        }

        mTranscodeVideoStrategy = new DefaultVideoStrategy.Builder()
                .addResizer(aspectRatio > 0 ? new AspectRatioResizer(aspectRatio) : new PassThroughResizer())
                .addResizer(new FractionResizer(fraction))
                .frameRate(frames)
                .build();

    }
    @Override
    public void onTranscodeProgress(double progress) {

    }

    @Override
    public void onTranscodeCompleted(int successCode) {
    }

    @Override
    public void onTranscodeCanceled() {
        onTranscodeFinished(false, "Transcoder canceled.");
    }

    @Override
    public void onTranscodeFailed(@NonNull Throwable exception) {
        onTranscodeFinished(false, "Transcoder error occurred. " + exception.getMessage());
    }

    private void onTranscodeFinished(boolean isSuccess, String toastMessage) {

        //setIsTranscoding(false);
    }


}
