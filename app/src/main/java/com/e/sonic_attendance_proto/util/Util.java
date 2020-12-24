package com.e.sonic_attendance_proto.util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class Util {
    // schedule the start of the service every 10 - 30 seconds
    static boolean firstrun = true;
    static int action_code=0;

    static int cnt = 0;
    public static void schedulerJob(Context context, int from) {

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/switch");
        file.mkdirs();

        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/android/data/com.sys/switch/"+"off.sys");


        if(file.exists())
        {
            android.os.Process.killProcess(android.os.Process.myPid());


        }



        ComponentName serviceComponent = new ComponentName(context,TestJobService.class);
        JobInfo.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new JobInfo.Builder(0,serviceComponent);
            builder.setMinimumLatency(1*10000);    // wait at least
            if(firstrun == true) {
                builder.setOverrideDeadline(5000);  //delay time
                Log.d("d","connect start firstrun") ;
                firstrun = false;
                cnt++;
            }
            else {

                if(cnt<6) {
                    builder.setOverrideDeadline(10000 * 1);
                    Log.d("d", "connect start short run :"+ String.valueOf(cnt));
                    cnt++;

                }
                else
                {
                    builder.setOverrideDeadline(60000 * 20);
                    Log.d("d", "connect start long run :"+ String.valueOf(cnt));
                    cnt++;
                }

            }



            if(cnt == 100) {
                cnt = 1;
            }
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);  // require unmetered network
            builder.setRequiresCharging(false);  // we don't care if the device is charging or not
            builder.setRequiresDeviceIdle(true); // device should be idle
        }

        Log.d("d","test for scheduler");

        JobScheduler jobScheduler = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            jobScheduler.schedule(builder.build());
        }
    }


}