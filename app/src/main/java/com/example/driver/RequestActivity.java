package com.example.driver;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

/**
 * Created by Nishanth on 02-12-2014.
 */
public class RequestActivity extends ActionBarActivity {
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
        map = ((MapFragment) getFragmentManager().findFragmentById(
                R.id.map)).getMap();
        map.getUiSettings().setZoomControlsEnabled(false);

        map.getUiSettings().setAllGesturesEnabled(false);

        ImageView pin = (ImageView) findViewById(R.id.pin);

        LatLng myLocation = new LatLng(13.055587,
                80.243687);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                17));

        ShowcaseView mShowcaseView = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(pin))
                .setContentTitle("Dai Dai Dai")
                .setContentText("This is highlighting the Home button")

                .build();
        mShowcaseView.setButtonText("ACCEPT");


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
}
