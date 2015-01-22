package com.saqib.lab4;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;


/**
 * Created by Saqib on 2015-01-10.
 */
public class onBootReceiver extends BroadcastReceiver {
    String intervalFile = "interval.txt";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            File fileInterval = context.getFileStreamPath(intervalFile);
            if(fileInterval.exists()) {
                fileInterval.delete();
            }
        }
    }
}
