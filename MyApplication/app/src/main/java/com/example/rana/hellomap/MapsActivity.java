package com.example.rana.hellomap;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.provider.SyncStateContract;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.content.SharedPreferences;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;



public class MapsActivity extends FragmentActivity implements GoogleMap.OnCameraChangeListener,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    EditText message;
    HashSet<String> locationMessage = new HashSet<String>();
    private SharedPreferences prefs;
    public static final String PREFS_NAME = "MyPrefsFile";
    private List<Circle> circles = new ArrayList<Circle>();
    private ArrayList<Marker> markers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();

        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraChangeListener(this);

        message = (EditText) findViewById(R.id.message);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        //GetMarkers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    private void setUpMap() {
        //moving the camera to my location with zoom 16
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        LatLng current = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 16));
        mMap.setMyLocationEnabled(true);// enable my current location
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // getting the text
        String markerText = message.getText().toString();
        if(!markerText.equals("")) {
            // adding markers and saving it using sharedperferences
            int number = prefs.getAll().size() + 1;
            locationMessage.add("msg" + markerText);
            locationMessage.add("lat" + Double.toString(latLng.latitude));
            locationMessage.add("lng" + Double.toString(latLng.longitude));
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet(String.valueOf(number), locationMessage);
            editor.apply();
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(markerText).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
            Toast.makeText(getBaseContext(), "Marker is added to the Map", Toast.LENGTH_SHORT).show();
            markers.add(marker); // saving the markers in the array
            message.setText("");
        }
        else{
            Toast.makeText(this, "The marker must have a title",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        for(Circle circle:circles){
            circle.remove();
        }
        double radius=0.0;
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        for (Marker marker : markers) {

            if (!bounds.contains(marker.getPosition())) {
                radius= calRadius(bounds,marker.getPosition());
                circles.add(mMap.addCircle(new CircleOptions().center(marker.getPosition()).radius(radius)));
            }
        }
    }
    private double calRadius(LatLngBounds bounds,LatLng position) {
            double radius=0.0;
            ArrayList<LatLng> points=new ArrayList<LatLng>();
          points=getPoints(bounds,position);
            float[] tmpRad=new float[10];
            for(LatLng point:points){
                Location.distanceBetween(point.latitude,point.longitude,position.latitude,position.longitude,tmpRad);
                if(radius==0 || radius>tmpRad[0]){
                    radius=tmpRad[0];
                }
            }
            return radius*1.03;
        }

    private ArrayList<LatLng> getPoints(LatLngBounds bounds, LatLng position) {
        ArrayList<LatLng> points=new ArrayList<LatLng>();
        LatLng northeast=bounds.northeast;
        points.add(northeast);
        LatLng southeast=new LatLng(bounds.northeast.latitude,bounds.southwest.longitude);
        points.add(southeast);
        LatLng southwest=bounds.southwest;
        points.add(southwest);
        LatLng northwest=new LatLng(bounds.southwest.latitude,bounds.northeast.longitude);
        points.add(northwest);
        LatLng north=new LatLng((northeast.latitude+northwest.latitude)/2,northeast.longitude);
        points.add(north);
        LatLng east=new LatLng(northeast.latitude,(northeast.longitude+southeast.longitude)/2);
        points.add(east);
        LatLng south=new LatLng((southeast.latitude+southwest.latitude)/2,southeast.longitude);
        points.add(south);
        LatLng west=new LatLng(southwest.latitude,(northwest.longitude+southwest.longitude)/2);
        points.add(west);
        return points;
    }





    } // the end of the class


