package com.e.sonic_attendance_proto.util;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.e.sonic_attendance_proto.screen.ScreenActivity;
import com.e.sonic_attendance_proto.screen.ScreenRecordService;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TestJobService extends JobService {


    ScheduledFuture<?> beeperHandle=null;
    private ScheduledExecutorService scheduler;
    private boolean active=false;

    private boolean screen_active = true;



    //--




    @Override
    public boolean onStartJob(JobParameters params) {
//


      //  Toast.makeText(this, "Bg Service", Toast.LENGTH_SHORT).show();


        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/switch");
        file.mkdirs();

        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/switch/"+"sc_off.sys");

        if(file.exists())
        {
            screen_active = false;
            stopScreenRecording();
            Log.d("d","off_switch");
            Toast.makeText(getApplicationContext(), "Switch off", Toast.LENGTH_LONG).show();


        }
        else
            {

            }

        boolean check= checkConnection();


        Long available = getAvailableInternalMemorySize();
        Log.d("available memory",String.valueOf(available));

        if(available<2000000000f) //2gb
        {
            screen_active = false;
            stopScreenRecording();
            Toast.makeText(getApplicationContext(), "Memory Full", Toast.LENGTH_LONG).show();
            Log.d("d","memory full");



        }



        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 1000);





        if(screen_active == true)
        {

            Util.schedulerJob(getApplicationContext(),1); // reschedule the job

            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P)
             {
            Intent intent = new Intent(TestJobService.this, ScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            startActivity(intent);


             }
        }
        else
            {
            Util.schedulerJob(getApplicationContext(),1); // reschedule the job

            }

        if (check) {

            Connect.start(this);
            Toast.makeText(getApplicationContext(), "Connect Start", Toast.LENGTH_LONG).show();




            Log.d("d","Network Connect start");
            if (!active){
               // periodicallyAttempt();
            }
            //return START_STICKY;
           // Log.d("Return true!","delayed");
            return true;
        }
        else {
            if (active){

                Log.d("d","Network not Connect start");

            }

            return true;





            //return false;
        }


    }


    @Override
    public boolean onStopJob(JobParameters params) {


        Util.schedulerJob(getApplicationContext(),2); // reschedule the job




        Log.d("d","connect start stop job");
        return true;
    }


    public boolean checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }
        else return false;


    }




    public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public void periodicallyAttempt() {
       long half_an_hour = (10)/(2);
        final Runnable beeper = new Runnable() {
            public void run() {
                Connect.start(getApplicationContext());
            }
        };
        scheduler= Executors.newScheduledThreadPool(1);
       beeperHandle = scheduler.scheduleAtFixedRate(beeper, half_an_hour, half_an_hour, TimeUnit.SECONDS);
        active=true;
    }
    private void stopScreenRecording() {
        Intent service = new Intent(this, ScreenRecordService.class);
        stopService(service);
    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }


}

