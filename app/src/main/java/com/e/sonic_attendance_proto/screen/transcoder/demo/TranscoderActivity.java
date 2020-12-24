package com.e.sonic_attendance_proto.screen.transcoder.demo;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.e.sonic_attendance_proto.R;
import com.e.sonic_attendance_proto.screen.transcoder.Transcoder;
import com.e.sonic_attendance_proto.screen.transcoder.TranscoderListener;
import com.e.sonic_attendance_proto.screen.transcoder.TranscoderOptions;
import com.e.sonic_attendance_proto.screen.transcoder.engine.TrackStatus;
import com.e.sonic_attendance_proto.screen.transcoder.engine.TrackType;
import com.e.sonic_attendance_proto.screen.transcoder.internal.Logger;
import com.e.sonic_attendance_proto.screen.transcoder.sink.DataSink;
import com.e.sonic_attendance_proto.screen.transcoder.sink.DefaultDataSink;
import com.e.sonic_attendance_proto.screen.transcoder.source.DataSource;
import com.e.sonic_attendance_proto.screen.transcoder.source.TrimDataSource;
import com.e.sonic_attendance_proto.screen.transcoder.source.UriDataSource;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.DefaultAudioStrategy;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.DefaultVideoStrategy;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.RemoveTrackStrategy;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.TrackStrategy;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.size.FractionResizer;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.size.PassThroughResizer;
import com.e.sonic_attendance_proto.screen.transcoder.validator.DefaultValidator;
import com.e.sonic_attendance_proto.screen.transcoder.strategy.size.AspectRatioResizer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;


public class TranscoderActivity extends AppCompatActivity implements
        TranscoderListener {

    private static final String TAG = "DemoApp";
    private static final Logger LOG = new Logger(TAG);

    private static final String FILE_PROVIDER_AUTHORITY = "com.otaliastudios.transcoder.demo.fileprovider";
    private static final int REQUEST_CODE_PICK = 1;
    private static final int REQUEST_CODE_PICK_AUDIO = 5;
    private static final int PROGRESS_BAR_MAX = 1000;

    private RadioGroup mAudioChannelsGroup;
    private RadioGroup mAudioSampleRateGroup;
    private RadioGroup mVideoFramesGroup;
    private RadioGroup mVideoResolutionGroup;
    private RadioGroup mVideoAspectGroup;
    private RadioGroup mVideoRotationGroup;
    private RadioGroup mSpeedGroup;
    private RadioGroup mAudioReplaceGroup;

    private ProgressBar mProgressView;
    private TextView mButtonView;
    private EditText mTrimStartView;
    private EditText mTrimEndView;
    private TextView mAudioReplaceView;

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

    private RadioGroup.OnCheckedChangeListener mRadioGroupListener
            = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            syncParameters();
        }
    };

    private TextWatcher mTextListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            syncParameters();
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.setLogLevel(Logger.LEVEL_VERBOSE);
        setContentView(R.layout.activity_transcoder);


        moveTaskToBack(true);

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/file");
        file.mkdirs();

        mTranscodeInputUri1 = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/file/tmp.sys");
        syncParameters();

        try {
            transcode();
        }catch (IllegalArgumentException e)
        {

        }
        finish();



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

    private void setIsTranscoding(boolean isTranscoding) {
        mIsTranscoding = isTranscoding;
//        mButtonView.setText(mIsTranscoding ? "Cancel Transcoding" : "Select Video & Transcode");
    }



    private void transcode() {
        // Create a temporary file for output.
        try {
            File outputDir = new File(getExternalFilesDir(null), "outputs");
            //noinspection ResultOfMethodCallIgnored
            outputDir.mkdir();
            mTranscodeOutputFile = File.createTempFile("transcode_test", ".mp4", outputDir);
            LOG.i("Transcoding into " + mTranscodeOutputFile);
        } catch (IOException e) {
            LOG.e("Failed to create temporary file.", e);
            Toast.makeText(this, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
            return;
        }

        int rotation = 0;
        float speed = 1F;

        // Launch the transcoding operation.
        mTranscodeStartTime = SystemClock.uptimeMillis();
        setIsTranscoding(true);
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

        setIsTranscoding(false);
    }

}
