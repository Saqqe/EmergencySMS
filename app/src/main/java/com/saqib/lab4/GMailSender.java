package com.saqib.lab4;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class GMailSender extends AsyncTask<String, Void, String> {
    private String user = "";
    private String password = "";
    private Session session;
    private String rec;
    private String subject;
    private String textMessage;
    private Email mailActivity;
    private int result = -1;

    public GMailSender(final String user, final String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        //Log.d("GmailSender", "Pass: " + password + " this.pass: " + this.password);
        session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
    }

    public GMailSender(final String user, final String password, Email mailActivity) {
        this.user = user;
        this.password = password;
        this.mailActivity = mailActivity;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        //Log.d("GmailSender", "Pass: " + password + " this.pass: " + this.password);
        session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
    }

    public void setMail(String subject, String msg, String recipients) {
        this.subject = subject;
        this.textMessage = msg;
        this.rec = recipients;
    }

    public void sendMail(String sendTo){
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sendTo));
            message.setSubject(subject);
            message.setContent(textMessage, "text/html; charset=utf-8");
            Transport.send(message);
        }
        catch (AuthenticationFailedException e){
            Log.d("sendMail", "Auth problem");
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.d("sendMail", "Couldn't send mail");
        }
    }

    public int testSend(String sendTo){
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sendTo));
            message.setSubject(subject);
            message.setContent(textMessage, "text/html; charset=utf-8");
            Transport.send(message);
            return 0;
        }
        catch (AuthenticationFailedException e){
            Log.d("sendMail", "Auth problem");
            return 1;
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.d("sendMail", "Couldn't send mail");
            return 1;
        }
    }

    @Override
    protected String doInBackground(String... params) {
        if(params.length > 0){
            if(params[0].equals("test")){
                result = testSend(this.rec);
            }
        }
        else {
            if (this.rec.contains(";")) {
                String[] sendTo = this.rec.split(";");
                for (String aSendTo : sendTo) {
                    sendMail(aSendTo);
                }
            } else {
                sendMail(this.rec);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(this.mailActivity != null){
            if(this.result == 0){
                this.mailActivity.saveAndClose();
            }
            else{
                this.mailActivity.wrongMsg();
            }
        }

    }
}
