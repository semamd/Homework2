package com.jose.homework2;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener, SensorEventListener {
    private final String LOCATIONS_JSON_FILE = "coordinates.json";
    private static List<Marker> markerList = null;
    private static final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 101;
    private static GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker gpsMarker = null;
    private ArrayList<LatLng> positionList = new ArrayList<LatLng>();
    public SensorManager mSensorManager;
    static List<Sensor> SensorList;
    static final public String SENSOR_TYPE = "sensorType";
    long lastUpdate = -1;
    Sensor mSensor;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        SensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
        restoreFromJson();

    }



    public void zoomInClick(View v){
        mMap.moveCamera(CameraUpdateFactory.zoomIn());
    }

    public void zoomOutClick(View v){
        mMap.moveCamera(CameraUpdateFactory.zoomOut());
    }

    @Override
    public void onMapLoaded() {
        Log.i(MapsActivity.class.getSimpleName(), "MapLoaded");
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        Task<Location> lastLocation = fusedLocationClient.getLastLocation();

        lastLocation.addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null && mMap != null){
                    if(markerList.isEmpty()) {
                        markerList = new ArrayList<Marker>();
                        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(getString(R.string.last_known_loc_msg)));
                        positionList.add(marker.getPosition());
                    }
                    else{
                        for(Marker mark: markerList){
                            mMap.addMarker(new MarkerOptions().position(mark.getPosition()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(mark.getTitle()));
                        }
                    }
                }
            }
        });
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).alpha(0.8f).title(String.format("Position:(%.2f, %.2f)",latLng.latitude,latLng.longitude)));
        markerList.add(marker);
        positionList.add(marker.getPosition());
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ImageButton xbutton = findViewById(R.id.xbutton);
        ImageButton dotbutton = findViewById(R.id.dotbutton);
        xbutton.startAnimation(fade_in);
        dotbutton.startAnimation(fade_in);
        xbutton.setVisibility(View.VISIBLE);
        dotbutton.setVisibility(View.VISIBLE);
        return false;

    }

    public void dotButton(View view){
        Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        TextView info = findViewById(R.id.sensor_text);
        info.startAnimation(fade_in);
        onResume();
        info.setVisibility(View.VISIBLE);



    }

    public void xButton(View view){
        Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        ImageButton xbutton = findViewById(R.id.xbutton);
        ImageButton dotbutton = findViewById(R.id.dotbutton);
        TextView sensor_info = findViewById(R.id.sensor_text);
        view.startAnimation(fade_out);
        dotbutton.startAnimation(fade_out);
        sensor_info.startAnimation(fade_out);
        xbutton.setVisibility(View.INVISIBLE);
        dotbutton.setVisibility(View.INVISIBLE);
        sensor_info.setVisibility(View.INVISIBLE);
        onPause();
    }

    public void clearButton(View view){
        mMap.clear();
        markerList.clear();
        Toast.makeText(getApplicationContext(), "Locations have been deleted...", Toast.LENGTH_SHORT).show();

    }

    private void saveToJson(List<LatLng> positionList){
        Gson gson = new Gson();
        String listJson = gson.toJson(positionList);
        FileOutputStream outputStream;
        try{
            outputStream = openFileOutput(LOCATIONS_JSON_FILE,MODE_PRIVATE);
            FileWriter writer = new FileWriter(outputStream.getFD());
            writer.write(listJson);
            writer.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void restoreFromJson(){
        FileInputStream inputStream;
        int DEFAULT_BUFFER_SIZE = 10000;
        Gson gson = new Gson();
        String readJson;
        try{
            inputStream = openFileInput(LOCATIONS_JSON_FILE);
            FileReader reader = new FileReader(inputStream.getFD());
            char[] buf = new char[DEFAULT_BUFFER_SIZE];
            int n;
            StringBuilder builder = new StringBuilder();
            while((n = reader.read(buf)) >= 0){
                String tmp = String.valueOf(buf);
                String substring = (n<DEFAULT_BUFFER_SIZE) ? tmp.substring(0, n) : tmp;
                builder.append(substring);
            }
            reader.close();
            readJson = builder.toString();
            Type collectionType = new TypeToken<List<LatLng>>(){
            }.getType();
            List<LatLng> o = gson.fromJson(readJson, collectionType);
            if(o != null){
                markerList = new ArrayList<Marker>();
                for(LatLng coordinates: o){
                    MarkerOptions mapMarker = new MarkerOptions().position(coordinates).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).alpha(0.8f).title(String.format("Position:(%.2f, %.2f)",coordinates.latitude,coordinates.longitude));
                    Marker marker = mMap.addMarker(mapMarker);
                    markerList.add(marker);
                }
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        saveToJson(positionList);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timeMicro;
        if(lastUpdate == -1){
            lastUpdate = event.timestamp;
            timeMicro = 0;
        }
        else{
            timeMicro = (event.timestamp - lastUpdate)/1000L;
            lastUpdate = event.timestamp;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Time difference: ").append(timeMicro).append(" \u03bcs\n");
        for (int i = 0; i<event.values.length; i++){
            stringBuilder.append(String.format("Val[%d]=%.4f\n",i,event.values[i]));
        }
        TextView info = findViewById(R.id.sensor_text);

        info.setText(mSensor.getName()+"\n"+stringBuilder.toString());
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(mSensor != null)
            mSensorManager.registerListener(this,mSensor,100000);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mSensor != null)
            mSensorManager.unregisterListener(this,mSensor);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
