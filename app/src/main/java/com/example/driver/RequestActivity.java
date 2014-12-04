package com.example.driver;


import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
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


import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by Nishanth on 02-12-2014.
 */
public class RequestActivity extends ActionBarActivity {
    GoogleMap map;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Typeface regular, bold, light;
    Context context;
    float numberXOffset;
    TextView client_name , client_number , client_address ;
    Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        Intent intent = getIntent();
        context = RequestActivity.this;
        bold = Typeface.createFromAsset(this.getAssets(), "fonts/bold.otf");
        regular = Typeface.createFromAsset(this.getAssets(),
                "fonts/regular.otf");
        light = Typeface.createFromAsset(this.getAssets(), "fonts/light.otf");



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
        if(intent.getExtras()!= null) {

            String clientname = intent.getStringExtra("name");
            final String phonenumber = intent.getStringExtra("phone");

            Double client_lat = Double.parseDouble(intent.getStringExtra("lat"));
            Double client_lng = Double.parseDouble(intent.getStringExtra("lng"));
            String duration = intent.getStringExtra("eta");
            String[] separated = duration.split(" ");
            LatLng myLocation = new LatLng(client_lat,
                    client_lng);
//            map.addMarker(new MarkerOptions().position(myLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_green)));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                    17));
            client_name = (TextView) findViewById(R.id.clientname);
            client_number = (TextView) findViewById(R.id.clientnumber);
            client_address = (TextView) findViewById(R.id.clientaddress);
            client_name.setText(clientname);
            client_number.setText(phonenumber);
            client_address.setText(clientname);
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
            List<Marker> markers = new ArrayList<Marker>();
//            Marker car = map.addMarker(new MarkerOptions().position(
//                    new LatLng(driver_lat, driver_lng)).icon(
//                    BitmapDescriptorFactory.fromResource(R.drawable.car)));
            Marker user = map.addMarker(new MarkerOptions().position(
                    new LatLng(client_lat, client_lng)).icon(
                    BitmapDescriptorFactory.fromBitmap(mutableBitmap)));
//            markers.add(car);
            markers.add(user);
            findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(context);
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

        }


//        ShowcaseView mShowcaseView = new ShowcaseView.Builder(this)
//                .setTarget(new ViewTarget(pin))
//                .setContentTitle("Dai Dai Dai")
//                .setContentText("This is highlighting the Home button")
//
//                .build();
//        mShowcaseView.setButtonText("ACCEPT");


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
    public void acceptRequest(View v)
    {
        v.setVisibility(View.GONE);
        CircularProgressBar progress = (CircularProgressBar) findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        RelativeLayout user_details = (RelativeLayout) findViewById(R.id.user_details);
        RelativeLayout floating_button = (RelativeLayout) findViewById(R.id.floating_button);
        user_details.setVisibility(View.VISIBLE);
        floating_button.setVisibility(View.VISIBLE);

        Toast.makeText(this,"accepted",Toast.LENGTH_SHORT);
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
    }

    public void showDialog(final Context context) {

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
                nameValuePairs.add(new BasicNameValuePair("type", "cancel"));




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


}
