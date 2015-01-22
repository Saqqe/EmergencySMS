package com.saqib.lab4;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;


public class HeartBeatMode extends ActionBarActivity {

    Button btnSave;
    EditText edtInterval;

    long interval = -1;
    long hour = 3600000;
    long minute = 60000;
    long sec = 1000;

    String intervalFile = "interval.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_beat_mode);
        readFromFile(this.intervalFile);

        btnSave = (Button) findViewById(R.id.btnSaveSettingHeartBeatMode);
        edtInterval = (EditText) findViewById(R.id.editTextInterval);

        if (this.interval > 0){
            edtInterval.setText(String.valueOf(this.interval));
            Editable text = edtInterval.getText();
            Selection.setSelection(text, edtInterval.length());
        }


    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSaveSettingHeartBeatMode:
                String edtText = edtInterval.getText().toString();
                if(edtText.isEmpty()){
                    Toast.makeText(this, "Enter how often you want to send SMS in minutes", Toast.LENGTH_SHORT).show();
                }
                else{
                    saveToFile(edtText, this.intervalFile);
                    this.interval = Long.parseLong(edtText) * minute; //Change here for interval
                    Intent intent = new Intent();
                    intent.putExtra("Interval", this.interval);
                    setResult(Activity.RESULT_OK, intent);
                    Toast.makeText(this, "Heart Beat Mode is activated!", Toast.LENGTH_SHORT);
                    finish();
                }
                break;
            case R.id.btnHeartBeatClear:
                this.edtInterval.setText("");
                Intent myIntent = new Intent(this, MyLarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                File fileInterval = getBaseContext().getFileStreamPath(this.intervalFile);
                if(fileInterval.exists()){
                    fileInterval.delete();
                }
                finish();
                Toast.makeText(this, "Heart Beat Mode is deactivated!", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    public void saveToFile(String data, String filename) {
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(openFileOutput(filename, Context.MODE_PRIVATE));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            if (outputStreamWriter != null) {
                outputStreamWriter.write(data);
                outputStreamWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//End of saveToFile

    public void readFromFile(String filename){
        try {
            InputStream inputStream = openFileInput(filename);
            if(inputStream != null){
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String fileData;

                if(filename.equals(this.intervalFile)){
                    while((fileData = bufferedReader.readLine()) != null){
                        interval = Long.parseLong(fileData);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//End of readFromFile
}
