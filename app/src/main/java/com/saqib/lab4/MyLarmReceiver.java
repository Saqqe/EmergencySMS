package com.saqib.lab4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Saqib on 2015-01-10.
 */
public class MyLarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent LarmIntet = new Intent(context, MessageSender.class);
        LarmIntet.putExtra("HeartBeat", true);
        LarmIntet.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(LarmIntet);
    }
}
