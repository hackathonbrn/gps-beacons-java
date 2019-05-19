package com.example.gps_beacons_java;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private Handler pollBeaconsHandler = new Handler();
    private ArrayList<MarkerOptions> gpsData = new ArrayList<MarkerOptions>();

    Runnable pollGpsBeacons = new Runnable() {
        @Override
        public void run() {

            // collect GPS data
//            while (true) {
                try {
                    URL url = new URL("http://192.168.0.99/");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        String response = readStream(in);
                        JSONObject jsonObject = new JSONObject(response);

                        double lat = jsonObject.getDouble("lat");
                        double lon = jsonObject.getDouble("lon");
                        String deviceId = jsonObject.getString("device_id");

                        LatLng pos = new LatLng(lat, lon);

                        if (gpsData != null) {
                            gpsData.clear();
                        }

                        gpsData.add(new MarkerOptions().position(pos).title(deviceId));

                        Log.i("REQUEST DATA", "lat" + lat);
                        Log.i("REQUEST DATA", "lon" + lon);
                        Log.i("REQUEST DATA", "deviceId" + deviceId);

                        pollBeaconsHandler.post(drawBeacons);
//
//                        // sleep for 5 sec
//                        wait(5000);

                    } catch (JSONException e1) {
                        Log.e("REQUEST ERROR", "" + e1.getMessage());
//                    } catch (InterruptedException e){
//                        return;
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    Log.e("REQUEST ERROR", "" + e.getMessage());
                }
            }
//        }
    };

    Runnable drawBeacons = new Runnable() {
        @Override
        public void run() {

            // draw beacons
            if (markers != null) {
                for (Marker marker : markers) {
                    marker.remove();
                }
            }

            if (gpsData != null) {
                for (MarkerOptions markerOption : gpsData) {
                    Log.i("MAIN THREAD", "Adding new marker");

                    Marker newMarker = mMap.addMarker(markerOption);
                    markers.add(newMarker);
                }
            }
        }
    };


    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add ArrayListeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng chaika = new LatLng(53.2803362, 83.6371467);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chaika, 16));

        AsyncTask.execute(pollGpsBeacons);
    }
}
