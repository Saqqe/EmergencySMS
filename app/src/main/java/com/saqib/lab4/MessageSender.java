package com.saqib.lab4;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Saqib on 2015-01-04.
 */
public class MessageSender extends Service implements LocationListener {

    private boolean heartbeat = false;

    private ArrayList<String> phoneNoTo;
    private String msg = "";
    private String sender = "";
    private String pass = "";
    private String to = "";

    private final String phoneNumbersFile = "phoneNumbers.txt";
    private final String msgFile = "msg.txt";
    private final String mailSender = "mailSender.txt";
    private final String mailPass = "mailPass.txt";
    private final String mailTo = "mailTo.txt";


    private boolean wifiConnected = false;
    private boolean mobileConnected = false;

    LocationManager locationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.phoneNoTo = new ArrayList<>();
        readFromFile(this.phoneNumbersFile);
        readFromFile(this.msgFile);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra("HeartBeat")) {
            this.heartbeat = intent.getBooleanExtra("HeartBeat", false);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void readFromFile(String filename) {
        try {
            InputStream inputStream = openFileInput(filename);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String fileData;

                if (filename.equals(this.phoneNumbersFile)) {
                    while ((fileData = bufferedReader.readLine()) != null) {
                        String[] newSplit = fileData.split(": ");
                        this.phoneNoTo.add(newSplit[1]);
                    }
                } else if (filename.equals(this.msgFile)) {
                    while ((fileData = bufferedReader.readLine()) != null) {
                        if (this.msg.isEmpty()) {
                            this.msg = fileData;
                        } else {
                            this.msg = this.msg + "\r\n" + fileData;
                        }
                    }
                } else if (filename.equals(this.mailSender)) {
                    while ((fileData = bufferedReader.readLine()) != null) {
                        this.sender = fileData;
                    }
                } else if (filename.equals(this.mailPass)) {
                    while ((fileData = bufferedReader.readLine()) != null) {
                        this.pass = fileData;
                    }
                } else if (filename.equals(this.mailTo)) {
                    while ((fileData = bufferedReader.readLine()) != null) {
                        this.to = fileData;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//End of readFromFile

    private String getLocation(double lat, double lng) {
        String lati = Double.toString(lat);
        String longi = Double.toString(lng);

        String mapUrl = "https://maps.google.com/maps?q=" + lati + "," + longi;

        return mapUrl +
                "\r\n" + "Latitude: " + lati +
                "\r\n" + "Longitude: " + longi;
    }

    private void sendSMS(double lat, double lng) {

        SmsManager smsManager = SmsManager.getDefault();
        if (heartbeat) {
            this.msg = "I'am here: " + getLocation(lat, lng);
        } else {
            this.msg += "\r\n" + getLocation(lat, lng);
        }

        for (int n = 0; n < this.phoneNoTo.size(); n++) {
            //Log.d("onStart", "Sending: " + this.msg + " to: " + this.phoneNoTo.get(n));
            smsManager.sendTextMessage(this.phoneNoTo.get(n), null, this.msg, null, null);
        }
    }

    //Call after sendSMS()
    private void sendMail() {
        checkNetworkConnection();
        if(wifiConnected || mobileConnected) {
            File fileMailTo = getBaseContext().getFileStreamPath(mailTo);
            if (fileMailTo.exists()) {
                readFromFile(this.mailTo);
                readFromFile(this.mailPass);
                readFromFile(this.mailSender);

                try {
                    GMailSender sender = new GMailSender(this.sender, this.pass);
                    sender.setMail("From Location App", this.msg, this.to);
                    sender.execute();
                } catch (Exception e) {
                    Log.d("SendMail", e.getMessage(), e);
                }

            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            locationManager.removeUpdates(this);
            sendSMS(lat, lng);
            sendMail();
            stopSelf();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    /**
     * Check whether the device is connected, and if so, whether the connection
     * is wifi or mobile (it could be something else).
     */
    private void checkNetworkConnection() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }
    }

}
