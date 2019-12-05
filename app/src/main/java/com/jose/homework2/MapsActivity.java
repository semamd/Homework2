package com.jose.homework2;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {
    private final String TASKS_JSON_FILE = "tasks.json";
    List<Marker> markerList;
    private static final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 101;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    Marker gpsMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        markerList = restoreFromJson();

        if (!markerList.isEmpty() && markerList.size() > 0){
            markerList = restoreFromJson();
        }
        else {
            markerList = new ArrayList<>();
        }
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
                    if(markerList.isEmpty() || markerList == null)
                         mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(getString(R.string.last_known_loc_msg)));
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
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        ImageButton xbutton = findViewById(R.id.xbutton);
        ImageButton dotbutton = findViewById(R.id.dotbutton);
        xbutton.setVisibility(View.VISIBLE);
        dotbutton.setVisibility(View.VISIBLE);
        return false;

    }

    public void dotButton(View view){
        TextView info = findViewById(R.id.sensor_text);
        info.setVisibility(View.VISIBLE);
    }

    public void xButton(View view){
        ImageButton xbutton = findViewById(R.id.xbutton);
        ImageButton dotbutton = findViewById(R.id.dotbutton);
        TextView sensor_info = findViewById(R.id.sensor_text);
        xbutton.setVisibility(View.INVISIBLE);
        dotbutton.setVisibility(View.INVISIBLE);
        sensor_info.setVisibility(View.INVISIBLE);
    }

    public void clearButton(View view){
        mMap.clear();
        markerList.clear();
        Toast.makeText(getApplicationContext(), "Locations have been deleted...", Toast.LENGTH_SHORT).show();

    }

    private void saveToJson(){
        Gson gson = new Gson();
        String listJson = gson.toJson(markerList);
        FileOutputStream outputStream;
        try{
            outputStream = openFileOutput(TASKS_JSON_FILE,MODE_PRIVATE);
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

    public List<Marker> restoreFromJson(){
        FileInputStream inputStream;
        int DEFAULT_BUFFER_SIZE = 10000;
        Gson gson = new Gson();
        String readJson;

        try{
            inputStream = openFileInput(TASKS_JSON_FILE);
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
            Type collectionType = new TypeToken<ArrayList<Marker>>(){
            }.getType();
            List<Marker> o = gson.fromJson(readJson, collectionType);
            if(o != null){
                markerList.clear();
                for(Marker marker: o){
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
        return markerList;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        saveToJson();
    }

}
