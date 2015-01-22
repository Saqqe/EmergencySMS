package com.saqib.lab4;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class Email extends ActionBarActivity {

    EditText sender, password, to, msgBox;
    private String msg = "";
    private final String mailSender = "mailSender.txt";
    private final String mailPass = "mailPass.txt";
    private final String mailTo = "mailTo.txt";
    private final String msgFile = "msg.txt";
    private boolean wifiConnected = false;
    private boolean mobileConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        sender = (EditText) findViewById(R.id.mailSender);
        password = (EditText) findViewById(R.id.password);
        to = (EditText) findViewById(R.id.mailTo);
        msgBox = (EditText) findViewById(R.id.editTextMsgEmail);

        File fileSender = getBaseContext().getFileStreamPath(this.mailSender);
        File filePass = getBaseContext().getFileStreamPath(this.mailPass);
        File fileTo = getBaseContext().getFileStreamPath(this.mailTo);

        if (fileSender.exists()) {
            readFromFile(this.mailSender);
        }
        if (filePass.exists()) {
            readFromFile(this.mailPass);
        }
        if (fileTo.exists()) {
            readFromFile(this.mailTo);
        }
    }


    public void onClick(View view) {
        String sender = this.sender.getText().toString();
        String password = this.password.getText().toString();
        String to = this.to.getText().toString();
        switch (view.getId()){
            case R.id.btnMailSaveSetting:
                if (!sender.isEmpty() && !password.isEmpty() && !to.isEmpty()) {
                    checkNetworkConnection();
                    if (wifiConnected || mobileConnected) {
                        testSend(sender, password);
                    }
                    else{
                        Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
                    }
                } else if (sender.isEmpty()) {
                    Toast.makeText(this, "You need to enter your gmail account!", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(this, "You need to enter your gmail account password!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You need to enter someone to send to!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnEmailClear:
                this.sender.setText("");
                this.password.setText("");
                this.to.setText("");
                File fileSender = getBaseContext().getFileStreamPath(this.mailSender);
                File filePass = getBaseContext().getFileStreamPath(this.mailPass);
                File fileTo = getBaseContext().getFileStreamPath(this.mailTo);
                if (fileSender.exists()) {
                    fileSender.delete();
                }
                if (filePass.exists()) {
                    filePass.delete();
                }
                if (fileTo.exists()) {
                    fileTo.delete();
                }
                Toast.makeText(this, "Nothing is saved and you can't send Email to anyone!", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    public void saveAndClose(){
        String sender = this.sender.getText().toString();
        String password = this.password.getText().toString();
        String to = this.to.getText().toString();

        this.msg = this.msgBox.getText().toString();
        if (msg.isEmpty()) {
            msg = "My mobile is DYING!";
            Toast.makeText(this, "You did not enter a message. We did it for you: \r\n " + msg, Toast.LENGTH_LONG).show();
        }

        saveToFile(sender, mailSender);
        saveToFile(password, mailPass);
        saveToFile(to, mailTo);
        saveToFile(this.msg, this.msgFile);
        finish();
    }

    public void wrongMsg(){
        Toast.makeText(this, "Something is wrong, check your Email and password", Toast.LENGTH_SHORT).show();
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

    public void readFromFile(String filename) {

        try {
            InputStream inputStream = openFileInput(filename);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String fileData;

                if (filename.equals(this.mailSender)) {
                    while ((fileData = bufferedReader.readLine()) != null) {
                        sender.setText(fileData);
                    }
                } else if (filename.equals(this.mailPass)) {
                    while ((fileData = bufferedReader.readLine()) != null) {
                        password.setText(fileData);
                    }
                } else if (filename.equals(this.mailTo)) {
                    while ((fileData = bufferedReader.readLine()) != null) {
                        to.setText(fileData);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//End of readFromFile

    private void testSend(String sender, String pass) {
        try {
            GMailSender gMailSender = new GMailSender(sender, pass, this);
            gMailSender.setMail("From Location App", "Sending email to confirm everything is setup!", "saqib.sarker@gmail.com");
            gMailSender.execute("test");
        } catch (Exception e) {
            Log.d("SendMail", e.getMessage(), e);
        }
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
