package com.example.abp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    public static final int forinterval = 30;
    public static final int forFastInterval = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private Handler handler =new Handler();

    TextToSpeech textToSpeech ;


    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address;
    Switch sw_locationsupdates, sw_gps;

    LocationRequest locationRequest;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationsupdates = findViewById(R.id.sw_locationsupdates);

        handler.postDelayed(m,1000);
        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000 * forinterval);

        locationRequest.setFastestInterval(1000 * forFastInterval);

        locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                updateUIvalues(location);
            }
        };
        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    textToSpeech.speak(tv_address.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                    locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("USING GPS");
                } else {
                    locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("USING TOWER/WIFI");
                }
            }
        });

        sw_locationsupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_locationsupdates.isChecked()) {
                    startLocationUpdate();
                } else {
                    stopLocationUpdate();
                }
            }
        });

        textToSpeech =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int i){
                if(i!=TextToSpeech.ERROR){
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
        updateGPS();

    }
    private void startLocationUpdate() {
        tv_updates.setText("Tracking your position");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);

    }

    private void  stopLocationUpdate(){
        tv_updates.setText("Not tracking you");
        tv_lat.setText("Not traking you");
        tv_lon.setText("Not tracking you");
        tv_speed.setText("Not tracking you");
        tv_address.setText("Not tracking you");
        tv_accuracy.setText("Not tracking you");
        tv_altitude.setText("Not tracking you");
        tv_sensor.setText("Not tracking you");
    }

    private Runnable m=new Runnable() {
        @Override
        public void run() {
            startLocationUpdate();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case PERMISSIONS_FINE_LOCATION:
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                updateGPS();
            }
            else
            {
                Toast.makeText(this,"This APP requires Permission ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        }
    }

    private  void updateGPS(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                      updateUIvalues(location);
                }
            });
        }
        else
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateUIvalues(Location location) {
         tv_lat.setText(String.valueOf(location.getLatitude()));
         tv_lon.setText(String.valueOf(location.getLongitude()));
         tv_accuracy.setText(String.valueOf(location.getAccuracy()));

         if(location.hasAltitude())
         {
             tv_altitude.setText(String.valueOf(location.getAltitude()));
         }
         else
         {
             tv_altitude.setText(String.valueOf("NOT AVAILABLE, buy a better phone "));
         }

         if(location.hasSpeed())
         {
             tv_speed.setText(String.valueOf(location.getSpeed()));
         }
         else
         {
             tv_speed.setText(String.valueOf("SPEED is unavailable in your phone buy a better one "));
         }

        Geocoder geocoder = new Geocoder(MainActivity.this);

         try{
             List<Address> addresses= geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
             tv_address.setText(addresses.get(0).getAddressLine(0));
         }catch ( IOException e){
             tv_address.setText("CAnt access the address ");

         }
    }

}