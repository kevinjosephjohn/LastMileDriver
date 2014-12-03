package com.example.driver;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;

import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

        ImageView pin = (ImageView) findViewById(R.id.pin);

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

        LatLng myLocation = new LatLng(13.055587,
                80.243687);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                17));


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


}
