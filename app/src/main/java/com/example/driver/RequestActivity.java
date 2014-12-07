package com.example.driver;


import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;

import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.dd.CircularProgressButton;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardViewNative;

/**
 * Created by Nishanth on 02-12-2014.
 */
public class RequestActivity extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
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
    Double current_lat, current_lng;
    LocationClient mLocationClient;
    Location mCurrentLocation;
    GoogleMap map;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Typeface regular, bold, light;
    Context context;
    float numberXOffset;
    TextView client_name, client_address;
    Dialog dialog;
    Boolean status_internet;
    InternetUtils check;
    List<Marker> markers;
    Handler handler;
    Runnable runnable;
    CircularProgressBar progress;
    CountDownTimer decline;
    Marker user,car;
    String cid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request);
        check = new InternetUtils();
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        Intent intent = getIntent();
        context = RequestActivity.this;
        bold = Typeface.createFromAsset(this.getAssets(), "fonts/bold.otf");
        regular = Typeface.createFromAsset(this.getAssets(),
                "fonts/regular.otf");
        light = Typeface.createFromAsset(this.getAssets(), "fonts/light.otf");
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
        progress = (CircularProgressBar) findViewById(R.id.progress);


        decline = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {


            }

            public void onFinish() {


                startActivity(new Intent(RequestActivity.this,
                        MainActivity.class));

                finish();


            }
        }.start();


        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

//        ImageView pin = (ImageView) findViewById(R.id.pin);

        View overlay = (View) findViewById(R.id.overlay);
        Bitmap bitmap = Bitmap.createBitmap((int) getWindowManager()
                .getDefaultDisplay().getWidth(), (int) getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        float width = (getWindowManager()
                .getDefaultDisplay().getWidth()) / 2;
        float heigth = (getWindowManager()
                .getDefaultDisplay().getHeight()) / 2;
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setAlpha(125);
        canvas.drawPaint(paint);
//        paint.setColor(getResources().getColor(R.color.light_blue));
//        canvas.drawCircle(width, heigth, 210, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));


        canvas.drawCircle(width, heigth, 200, paint);


        overlay.setBackground(new BitmapDrawable(bitmap));


        map = ((MapFragment) getFragmentManager().findFragmentById(
                R.id.map)).getMap();
        map.getUiSettings().setZoomControlsEnabled(false);

        map.getUiSettings().setAllGesturesEnabled(false);


        if (intent.getExtras() != null) {

            String clientname = intent.getStringExtra("name").toUpperCase();
            String address = intent.getStringExtra("address").toUpperCase();
            cid = intent.getStringExtra("id").toUpperCase();
            final String phonenumber = intent.getStringExtra("phone");

            Double client_lat = Double.parseDouble(intent.getStringExtra("lat"));
            Double client_lng = Double.parseDouble(intent.getStringExtra("lng"));
            String duration = intent.getStringExtra("eta");
            String[] separated = duration.split(" ");
            LatLng myLocation = new LatLng(client_lat,
                    client_lng);

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                    17));
            client_name = (TextView) findViewById(R.id.clientname);
            client_address = (TextView) findViewById(R.id.StreetName);
//            client_number = (TextView) findViewById(R.id.clientnumber);

            client_name.setText(clientname);
            client_address.setText(address);

            client_name.setTypeface(regular);
            client_address.setTypeface(regular);
//            client_number.setText(phonenumber);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.pin_pickup_green);
            Bitmap mutableBitmap = icon.copy(Bitmap.Config.ARGB_8888, true);
            canvas = new Canvas(mutableBitmap);
            Paint mPictoPaint = new Paint();
            mPictoPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            mPictoPaint.setColor(Color.WHITE);
            mPictoPaint.setTypeface(bold);
            Resources r = getResources();
            mPictoPaint.setTextSize(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 9, r.getDisplayMetrics()));

            float textYOffset = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics());
            float textXOffset = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
            float numberYOffset = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 22, r.getDisplayMetrics());
            if (separated[0].length() == 2) {
                numberXOffset = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 17, r.getDisplayMetrics());
            } else {
                numberXOffset = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
            }

            canvas.drawText(separated[1].toUpperCase(), textXOffset, textYOffset,
                    mPictoPaint);
            canvas.drawText(separated[0].toUpperCase(), numberXOffset,
                    numberYOffset, mPictoPaint);
            markers = new ArrayList<Marker>();

            user = map.addMarker(new MarkerOptions().position(
                    new LatLng(client_lat, client_lng)).icon(
                    BitmapDescriptorFactory.fromBitmap(mutableBitmap)));


            markers.add(user);
            findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelDialog(context);
                }
            });
            findViewById(R.id.call).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phonenumber));
                    startActivity(intent);

                }
            });
            //Create a Card
            Card card = new Card(context);


            //Set card in the cardView
            CardViewNative cardView = (CardViewNative) findViewById(R.id.carddemo);
            cardView.setCard(card);


        }


//        ShowcaseView mShowcaseView = new ShowcaseView.Builder(this)
//                .setTarget(new ViewTarget(pin))
//                .setContentTitle("Dai Dai Dai")
//                .setContentText("This is highlighting the Home button")
//
//                .build();
//        mShowcaseView.setButtonText("ACCEPT");


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
        map.clear();


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

    public void acceptRequest(View v) {
        decline.cancel();
        v.setVisibility(View.GONE);

        progress.setVisibility(View.GONE);
        RelativeLayout user_details = (RelativeLayout) findViewById(R.id.user_details);
        RelativeLayout floating_button = (RelativeLayout) findViewById(R.id.floating_button);

        user_details.setVisibility(View.VISIBLE);
        floating_button.setVisibility(View.VISIBLE);
        new AcceptRequest().execute();


    }

    private class AcceptRequest extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(
                    "http://128.199.134.210/api/driver/status/");
            String responseBody = null;

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("type", "accept"));
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

    @Override
    public void onBackPressed() {
        cancelDialog(context);
    }

    public void cancelDialog(final Context context) {

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.cancel_dialog);
        dialog.setCancelable(false);
        Button yesButton = (Button) dialog.findViewById(R.id.yes);
        Button noButton = (Button) dialog.findViewById(R.id.no);
        yesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {


                new CancelRide().execute();


            }

        });
        noButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();

            }

        });
        dialog.show();

    }

    private class CancelRide extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {


        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            // Create a new HttpClient and Post Header

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(
                    "http://128.199.134.210/api/request/");
            String responseBody = null;


            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("type", "drivercancel"));
                nameValuePairs.add(new BasicNameValuePair("uid", cid));


                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                responseBody = EntityUtils.toString(entity);
                Log.i("Response", responseBody);
                // Log.i("Parameters", params[0]);

            } catch (ClientProtocolException e) {


                // TODO Auto-generated catch block
            } catch (IOException e) {

                // TODO Auto-generated catch block
            }
            return responseBody;

        }


        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            Intent intent = new Intent(RequestActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);


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
            current_lat = mCurrentLocation.getLatitude();
            current_lng = mCurrentLocation.getLongitude();
            mLocationClient.requestLocationUpdates(mLocationRequest, RequestActivity.this);
            car = map.addMarker(new MarkerOptions().position(
                    myLocation).icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.car)));



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
        Log.i(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        current_lat = location.getLatitude();
        current_lng = location.getLongitude();
        status_internet = check.isConnected(context);
        if (!status_internet) {


        } else {
            new PollingLocation().execute();
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
                JSONObject data = new JSONObject(result);
                lat = Double.valueOf(data.getString("lat"));
                lng = Double.valueOf(data.getString("lng"));
                LatLng myLocation = new LatLng(lat,
                        lng);

//                markers.add(car);
                car.remove();
                car = map.addMarker(new MarkerOptions().position(
                        myLocation).icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.car)));
                map.getUiSettings().setAllGesturesEnabled(true);
//                LatLngBounds.Builder builder = new LatLngBounds.Builder();
//
//                for (Marker marker : markers) {
//                    builder.include(marker.getPosition());
//                }
//                final LatLngBounds bounds = builder.build();
//
//                int padding = 200; // offset from edges of the map in pixels
//                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
//                        padding);
//                map.moveCamera(cu);


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

