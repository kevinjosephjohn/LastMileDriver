package com.example.driver;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory.Options;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.CountDownTimer;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 5;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
    boolean mUpdatesRequested;
    // Global constants
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Context context;
    LocationClient mLocationClient;
    Location mCurrentLocation;
    GoogleMap map;
    SwitchCompat driver_status;
    TextView driver_status_text;
    String update_status;
    String current_lat, current_lng;
    Handler handler;
    Runnable runnable;
    boolean status_internet;
    InternetUtils check;
    Dialog dialog;
    Marker car;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        check = new InternetUtils();
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

        context = MainActivity.this;
        mLocationClient = new LocationClient(this, this, this);
        // Start with updates turned off
        mUpdatesRequested = false;
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        map = ((MapFragment) getFragmentManager().findFragmentById(
                R.id.map)).getMap();
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        driver_status = (SwitchCompat) findViewById(R.id.driver_status);
        driver_status_text = (TextView) findViewById(R.id.driver_status_text);
        handler = new Handler();


        driver_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    driver_status_text.setText("YOU ARE ONLINE");
                    RelativeLayout lLayout = (RelativeLayout) findViewById(R.id.bottom_bar);
                    lLayout.setBackgroundColor(Color.parseColor("#2196F3"));
                    update_status = "online";
                    mLocationClient.requestLocationUpdates(mLocationRequest, MainActivity.this);
                    new UpdateStatus().execute();
                    editor.putString("status", update_status);
                    editor.commit();


                } else {

                    driver_status_text.setText("PRESS THE BUTTON TO GO ONLINE");
                    RelativeLayout lLayout = (RelativeLayout) findViewById(R.id.bottom_bar);
                    lLayout.setBackgroundColor(Color.parseColor("#fff41921"));

                    update_status = "offline";
                    mLocationClient.removeLocationUpdates(MainActivity.this);
                    new UpdateStatus().execute();
                    editor.putString("status", update_status);
                    editor.commit();
//                    handler.removeCallbacks(runnable);


                }
            }
        });



    }

    /*
   * Called when the Activity becomes visible.
   */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.

        // If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            mLocationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        mLocationClient.disconnect();
        super.onStop();
        update_status = "offline";
        new UpdateStatus().execute();
        map.clear();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            return rootView;
        }
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
//        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        mCurrentLocation = mLocationClient.getLastLocation();
        if (mCurrentLocation != null) {
            LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude());
//        mLocationClient.requestLocationUpdates(mLocationRequest, this);
            current_lat = Double.toString(mCurrentLocation.getLatitude());
            current_lng = Double.toString(mCurrentLocation.getLongitude());
            car = map.addMarker(new MarkerOptions().position(myLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_dot)));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                    19));
            if ((pref.getString("status", null)).equalsIgnoreCase("online")) {
                driver_status.setChecked(true);
//                handler = new Handler();
//                runnable = new Runnable() {
//                    public void run() {
//                        new PollingLocation().execute();
//
//                        handler.postDelayed(this, 5000);
//                    }
//                };
//                runnable.run();
            }
            else {
                driver_status.setChecked(false);
            }

        } else {

            LocationDialog(context);
        }

    }

    /*
    * Called by Location Services if the connection to the
    * location client drops because of an error.
    */
    @Override
    public void onDisconnected() {
        // Display the connection status
//        Toast.makeText(this, "Disconnected. Please re-connect.",
//                Toast.LENGTH_SHORT).show();
    }

    /*
   * Called by Location Services if the attempt to
   * Location Services fails.
   */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        current_lat = Double.toString(location.getLatitude());
        current_lng = Double.toString(location.getLongitude());

        Log.i(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
        status_internet = check.isConnected(context);
        if (!status_internet) {


        } else {
            new PollingLocation().execute();
        }

    }

    private class UpdateStatus extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(
                    "http://128.199.134.210/api/driver/status/");
            String responseBody = null;

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("type", update_status));
                nameValuePairs.add(new BasicNameValuePair("did", pref
                        .getString("did", "")));


//                nameValuePairs.add(new BasicNameValuePair("gcm_regid", pref
//                        .getString("registration_id", "")));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                responseBody = EntityUtils.toString(entity);
                Log.i("Response", responseBody);
                // Log.i("Parameters", params[0]);

            } catch (ClientProtocolException e) {

            } catch (IOException e) {

            }
            return responseBody;

        }


        @Override
        protected void onPostExecute(String result) {


        }


    }

    private class PollingLocation extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(
                    "http://128.199.134.210/api/driver/status/");
            String responseBody = null;

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("type", "update"));
                nameValuePairs.add(new BasicNameValuePair("location", current_lat + "," + current_lng));

                nameValuePairs.add(new BasicNameValuePair("did", pref
                        .getString("did", "")));


//                nameValuePairs.add(new BasicNameValuePair("gcm_regid", pref
//                        .getString("registration_id", "")));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                responseBody = EntityUtils.toString(entity);
                Log.i("Response", responseBody);
                // Log.i("Parameters", params[0]);

            } catch (ClientProtocolException e) {

            } catch (IOException e) {

            }
            return responseBody;

        }


        @Override
        protected void onPostExecute(String result) {

            Double lat, lng;
            try {
                car.remove();
                JSONObject data = new JSONObject(result);
                lat = Double.valueOf(data.getString("lat"));
                lng = Double.valueOf(data.getString("lng"));
                LatLng myLocation = new LatLng(lat,
                        lng);
                car = map.addMarker(new MarkerOptions().position(myLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_dot)));

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                        19));
            } catch (JSONException e) {


            }


        }


    }

    public void showDialog(final Context context) {

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.internetdialog);
        dialog.setCancelable(false);
        final CircularProgressButton dialogButton = (CircularProgressButton) dialog
                .findViewById(R.id.tryagain);

        dialogButton.setOnClickListener(new View.OnClickListener() {

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

                        }

                    }
                }.start();

            }

        });
        dialog.show();

    }

    public void LocationDialog(final Context context) {

        TextView line1, line2;
        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.internetdialog);

        dialog.setCancelable(false);
        final CircularProgressButton dialogButton = (CircularProgressButton) dialog
                .findViewById(R.id.tryagain);
        line1 = (TextView) dialog.findViewById(R.id.textView1);
        line2 = (TextView) dialog.findViewById(R.id.textView2);

        line1.setText("LOCATION SERVICES");
        line2.setText("LOCATION SERVICES ALLOWS APP TO GATHER AND USE DATA INDICATING YOUR APPROXIMATE LOCATION.");
        line2.setGravity(Gravity.CENTER);

        dialogButton.setText("ENABLE");

        dialogButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // dialogButton.setIndeterminateProgressMode(true);
                // dialogButton.setProgress(50);

                // new CountDownTimer(1000, 1000) {
                //
                // public void onTick(long millisUntilFinished) {
                //
                // }
                //
                // public void onFinish() {

                Intent gpsOptionsIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(gpsOptionsIntent);
                dialog.dismiss();

                // }
                // }.start();

            }

        });
        dialog.show();
    }

}
