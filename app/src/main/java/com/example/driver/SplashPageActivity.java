package com.example.driver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.dd.CircularProgressButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class SplashPageActivity extends Activity {
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    static final String TAG = "GCM ID";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String DEBUG_TAG = "Gestures";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    Context context = this;
    String status;
    InternetUtils check;
    String SENDER_ID = "1099007182911";
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    String regid;
    SharedPreferences pref;
    Editor editor;

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_activity);

        check = new InternetUtils();


        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        status = pref.getString("is_login", "false");

        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
            new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {

                    if (status.equalsIgnoreCase("true")) {

                        startActivity(new Intent(SplashPageActivity.this,
                                MainActivity.class));

                        finish();

                    } else {

                        startActivity(new Intent(SplashPageActivity.this,
                                LoginActivity.class));
                        finish();

                    }

                }
            }.start();

        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
            PlayServicesDialog(context);

        }


    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("Google Play Services", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void registerInBackground() {
        new registerGCM().execute();

    }

    private void storeRegistrationId(Context context, String regId) {

        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        Editor editor = pref.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private String getRegistrationId(Context context) {

        String registrationId = pref.getString(PROPERTY_REG_ID, "");

        Log.i("ID", registrationId);
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");

            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = pref.getInt(PROPERTY_APP_VERSION,
                Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    public void PlayServicesDialog(Context context) {

        TextView line1, line2;
        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.internetdialog);

        dialog.setCancelable(false);
        final CircularProgressButton dialogButton = (CircularProgressButton) dialog
                .findViewById(R.id.tryagain);
        line1 = (TextView) dialog.findViewById(R.id.textView1);
        line2 = (TextView) dialog.findViewById(R.id.textView2);

        line1.setText("GOOGLE PLAY SERVICES");
        line2.setText("THIS APP WON'T RUN WITHOUT GOOGLE PLAY SERVICES, WHICH ARE MISSING FROM YOUR PHONE");
        line2.setGravity(Gravity.CENTER);

        dialogButton.setText("DOWNLOAD");

        dialogButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                String url = "https://play.google.com/store/apps/details?id=com.lukaville.mental.age";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                dialog.dismiss();
            }

        });
        dialog.show();
    }

    class registerGCM extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                regid = gcm.register(SENDER_ID);

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your
                // app
                // is using accounts.
                // sendRegistrationIdToBackend(regid);

                // For this demo: we don't need to send it because the device
                // will send upstream messages to a server that echo back the
                // message using the 'from' address in the message.

                // Persist the regID - no need to register again.
                storeRegistrationId(context, regid);
            } catch (IOException ex) {

                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
            return regid;
        }

        @Override
        protected void onPostExecute(String msg) {

            Log.i("ID", msg);
        }


    }
}
