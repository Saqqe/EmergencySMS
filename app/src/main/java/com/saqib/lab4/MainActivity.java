package com.saqib.lab4;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;


public class MainActivity extends ActionBarActivity{

    private int REQUEST_CODE_HEARTBEAT = 10;
    long hour = 3600000;

    private long interval = -1;

    String phoneNumbersFile = "phoneNumbers.txt";
    String intervalFile = "interval.txt";
    String mailTo = "mailTo.txt";

    Button btnSMS, btnHeartBeat, btnEmail;
    TextView txtOut;

    File filePhone;
    File fileInterval;
    File fileMailTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSMS = (Button) findViewById(R.id.btnSmsSettings);
        btnHeartBeat = (Button) findViewById(R.id.btnHeartBeatMode);
        btnEmail = (Button) findViewById(R.id.btnMailSettings);
        txtOut = (TextView) findViewById(R.id.txtOut);

    }

    @Override
    protected void onResume() {
        super.onResume();

        filePhone = getBaseContext().getFileStreamPath(phoneNumbersFile);
        fileInterval = getBaseContext().getFileStreamPath(intervalFile);
        fileMailTo = getBaseContext().getFileStreamPath(mailTo);

        if(filePhone.exists() && !btnSMS.getText().toString().contains("Done")){
            btnSMS.setText("SMS Settings - Set");
        }
        if(fileInterval.exists() && !btnHeartBeat.getText().toString().contains("Done")){
            btnHeartBeat.setText("Heart beat mode- Set");
        }
        else if(!fileInterval.exists()){
            btnHeartBeat.setText("Heart beat mode- Not Set");
        }
        if(fileMailTo.exists() && !btnEmail.getText().toString().contains("Done")){
            btnEmail.setText("Email settings - Set");
        }
    }

    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btnSmsSettings:
                Intent intent = new Intent(this, SmsSettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.btnStartService:
                final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
                if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if(fileMailTo.exists() || filePhone.exists()) {
                        confirmDialog();
                    }
                    else{
                        Toast.makeText(this, "You need to set SMS settings OR E-mail settings!", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    showGPSDisabledAlertToUser();
                }
                break;
            case R.id.btnMailSettings:
                startActivity(new Intent(this, Email.class));
                break;
            case R.id.btnHeartBeatMode:
                startActivityForResult(new Intent(this, HeartBeatMode.class), REQUEST_CODE_HEARTBEAT);
                break;
            case R.id.btnStop:
                Intent myIntent = new Intent(this, MyLarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                if (filePhone.exists()) {
                    filePhone.delete();
                }
                if(fileInterval.exists()){
                    fileInterval.delete();
                }
                if(fileMailTo.exists()){
                    fileMailTo.delete();
                }
                Toast.makeText(this, "No more sms or e-mail will be sent to anyone!", Toast.LENGTH_LONG).show();
                finish();
            default:
                break;
        }
    }

    public void startApp(){
        if(interval > 0){
            Intent myIntent = new Intent(this, MyLarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    interval, interval, pendingIntent);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == this.REQUEST_CODE_HEARTBEAT){
            if(resultCode == Activity.RESULT_OK){
                interval = data.getLongExtra("Interval", 0);
            }
        }
    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. You need to enable it for this application to work!")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void confirmDialog(){
        String msg = "";
        if(filePhone.exists() && fileMailTo.exists()){
            msg = "SMS and Email will be sent to chosen contact in SMS Settings and Email Settings at low battery.";
        }
        else if(filePhone.exists()){
            msg = "SMS will be sent to chosen contact in SMS Settings at low battery.";
        }
        else if(fileMailTo.exists()){
            msg = "Email will be sent to chosen contact in Email Settings at low battery.";
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if(this.interval > 0){
            msg += "\r\nHeartBeat Mode is on and will send sms/email every: " + this.interval/hour + " hour!";
        }
        alertDialogBuilder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Okey",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                startApp();
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
