package com.saqib.lab4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

public class ReceiverBatteryLow extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
            Intent serviceIntent = new Intent(context, MessageSender.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            serviceIntent.putExtra("LowBattery", true);
            context.startService(serviceIntent);
        }
    }
}
