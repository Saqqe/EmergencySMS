package com.saqib.lab4;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;


public class SmsSettingsActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener, RemoveContactDialogFragment.NoticeDialogListener {

    private static final int REQUEST_CODE_ContactPicker = 1;

    EditText msg;
    Button btnContactPicker;
    MySpinner spinner;

    private final String phoneNumbersFile = "phoneNumbers.txt";
    private final String msgFile = "msg.txt";
    private String toRemove = null;

    private ArrayList<String> phoneNumbers;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_settings);

        this.phoneNumbers = new ArrayList<>();

        initUi();
        setSpinner();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("PhoneNumbers", this.phoneNumbers);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.phoneNumbers.clear();
        this.phoneNumbers.addAll(savedInstanceState.getStringArrayList("PhoneNumbers"));
        this.adapter.notifyDataSetChanged();
    }

    private void initUi() {
        btnContactPicker = (Button) findViewById(R.id.btnContactPicker);
        msg = (EditText) findViewById(R.id.editTextMSG);
        spinner = (MySpinner) findViewById(R.id.spinner);
    }

    private void setSpinner() {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, this.phoneNumbers);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        File filePhone = getBaseContext().getFileStreamPath(this.phoneNumbersFile);
        File fileMsg = getBaseContext().getFileStreamPath(this.msgFile);

        if (filePhone.exists() && fileMsg.exists()) {
            readFromFile(this.phoneNumbersFile);
            readFromFile(this.msgFile);
        } else if (filePhone.exists()) {
            readFromFile(this.phoneNumbersFile);
        }
    }

    //Button listener "Click here to add phone number"
    public void getPhoneNo(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        startActivityForResult(intent, REQUEST_CODE_ContactPicker);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ContactPicker && resultCode == RESULT_OK) {
            Uri contactData = data.getData();
            Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
            cursor.moveToFirst();

            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

            String newInfo = name + ": " + number;

            if (this.phoneNumbers.isEmpty()) {
                this.phoneNumbers.add(newInfo);
                adapter.notifyDataSetChanged();
            } else {
                for (int n = 0; n < this.phoneNumbers.size(); n++) {
                    if (this.phoneNumbers.contains(newInfo)) {
                        break;
                    } else {
                        this.phoneNumbers.add(newInfo);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    private void showDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("You have not chosen anyone to send to!" + "\r\n"
                + " Click Okey if you want to turn off this faction")
                .setCancelable(false)
                .setPositiveButton("Okey",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File filePhone = getBaseContext().getFileStreamPath(phoneNumbersFile);
                                if (filePhone.exists()) {
                                    filePhone.delete();
                                }
                                finish();
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
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

                if (filename.equals(this.phoneNumbersFile)) {
                    while ((fileData = bufferedReader.readLine()) != null) {
                        this.phoneNumbers.add(fileData);
                    }
                    adapter.notifyDataSetChanged();
                } else if (filename.equals(this.msgFile)) {
                    while ((fileData = bufferedReader.readLine()) != null) {
                        if (msg.getText().toString().isEmpty()) {
                            msg.setText(fileData);
                        } else {
                            msg.setText(msg.getText().toString() + "\r\n" + fileData);
                        }
                    }
                    Editable text = msg.getText();
                    Selection.setSelection(text, msg.length());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//End of readFromFile

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        this.phoneNumbers.remove(toRemove);
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        toRemove = this.phoneNumbers.get(position);
        DialogFragment newFragment = new RemoveContactDialogFragment();
        newFragment.show(getFragmentManager(), "contacts");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    //Button listener "Save Setting"
    public void saveSettings(View view) {
        String phoneNo = "";
        String msg = this.msg.getText().toString();

        if (this.phoneNumbers.isEmpty()) {
            showDialog();
        } else {
            for (int n = 0; n < this.phoneNumbers.size(); n++) {
                phoneNo += this.phoneNumbers.get(n) + "\r\n";
            }
            if (msg.isEmpty()) {
                msg = "My mobile is DYING!";
                Toast.makeText(this, "You did not enter a message. We did it for you: \r\n " + msg, Toast.LENGTH_LONG).show();
            }
            saveToFile(phoneNo, this.phoneNumbersFile);
            saveToFile(msg, this.msgFile);
            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
            finish();
        }
    }//End of saveSettings

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSMSClear:
                this.phoneNumbers.clear();
                adapter.notifyDataSetChanged();
                this.msg.setText("");
                File fileMsg = getBaseContext().getFileStreamPath(this.msgFile);
                File filePhoneNr = getBaseContext().getFileStreamPath(this.phoneNumbersFile);
                if(fileMsg.exists()){
                    fileMsg.delete();
                }
                if(filePhoneNr.exists()) {
                    filePhoneNr.delete();
                }
                Toast.makeText(this, "Nothing is saved and you can't send SMS to anyone!", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
