package com.e.sonic_attendance_proto.util;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Util.schedulerJob(context, 2);
    }
}