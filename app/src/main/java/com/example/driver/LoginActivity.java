package com.example.driver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.dd.CircularProgressButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
    String uname, pass;
    EditText email, password;
    SharedPreferences pref;
    Editor editor;
    Context context = this;
    boolean status_internet;
    InternetUtils check;
    CircularProgressButton login_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        login_button = (CircularProgressButton) findViewById(R.id.login_button);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        password = (EditText) findViewById(R.id.password_login);
        email = (EditText) findViewById(R.id.email_login);
        check = new InternetUtils();
        status_internet = check.isConnected(context);
        if (!status_internet)
            showDialog(context);

    }

    public void login(View v) {
        int flag_email = 0, flag_pass = 0;

        if (email.getText().length() == 0) {
            email.setError("Email Address Cannot Be Empty");
            flag_email = 1;
        }
        if (password.getText().length() < 8) {
            password.setError("Password Cannot Be Empty");
            flag_pass = 1;
        }

        if (flag_email == 0 && flag_pass == 0)

        {

            uname = email.getText().toString();
            pass = password.getText().toString();
            status_internet = check.isConnected(context);
            if (!status_internet)
                showDialog(context);
            else {
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute(uname, pass);

            }

        }

    }

    public void showDialog(final Context context) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.internetdialog);
        dialog.setCancelable(false);
        final CircularProgressButton dialogButton = (CircularProgressButton) dialog
                .findViewById(R.id.tryagain);
        dialogButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialogButton.setIndeterminateProgressMode(true);
                dialogButton.setProgress(50);

                new CountDownTimer(2000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        dialogButton.setClickable(false);
                    }

                    public void onFinish() {

                        if (!check.isConnected(context)) {

                            dialogButton.setProgress(0);
                            dialog.show();
                        } else {
                            dialog.dismiss();
                            recreate();
                        }

                    }
                }.start();

            }

        });
        dialog.show();

    }

    @Override
    public void recreate() {

        super.recreate();

    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(
                    "http://128.199.134.210/api/auth/index.php");
            String responseBody = null;

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("tag", "login"));

                nameValuePairs.add(new BasicNameValuePair("email", params[0]));
                nameValuePairs
                        .add(new BasicNameValuePair("password", params[1]));
                nameValuePairs.add(new BasicNameValuePair("gcm_regid", pref
                        .getString("registration_id", "")));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                responseBody = EntityUtils.toString(entity);
                Log.i("Response", responseBody);
                // Log.i("Parameters", params[0]);

            } catch (ClientProtocolException e) {
                showDialog(context);
                // TODO Auto-generated catch block
            } catch (IOException e) {
                showDialog(context);
                // TODO Auto-generated catch block
            }
            return responseBody;

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject data = new JSONObject(result);

                String error = data.getString("error");

                if (error.equals("0")) {
                    JSONObject user_details = data.getJSONObject("user");
                    String fname = user_details.getString("fname");
                    String lname = user_details.getString("lname");
                    String email = user_details.getString("email");
                    String phone = user_details.getString("phone");
                    String uid = user_details.getString("uid");

                    editor.putString("is_login", "true");
                    editor.putString("first_name", fname);
                    editor.putString("last_name", lname);
                    editor.putString("email", email);
                    editor.putString("phone", phone);
                    editor.putString("uid", uid);

                    editor.commit();
                    login_button.setProgress(100);

                    new CountDownTimer(2000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            login_button.setClickable(false);
                        }

                        public void onFinish() {

                            Intent intent = new Intent(LoginActivity.this,
                                    MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        }
                    }.start();
                } else {
                    String error_msg = data.getString("error_msg");
                    if (error.equals("1")) {
                        email.setError(error_msg);
                        password.setError(error_msg);
                        login_button.setProgress(-1);
                        new CountDownTimer(2000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                login_button.setClickable(false);

                            }

                            public void onFinish() {

                                login_button.setProgress(0);
                                login_button.setClickable(true);

                            }
                        }.start();

                    }

                }
            } catch (JSONException e) {
                showDialog(context);
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {

            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            password = (EditText) findViewById(R.id.password_login);
            email = (EditText) findViewById(R.id.email_login);
            login_button = (CircularProgressButton) findViewById(R.id.login_button);
            login_button.setIndeterminateProgressMode(true);
            login_button.setProgress(50);

            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(String... text) {

            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }
    }

}
