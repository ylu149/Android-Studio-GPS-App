package com.example.homework1part0;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private final int PERMISSION_ID = 238947;
    private FusedLocationProviderClient mFusedLocationClient;
    TextView locationView, speedValue;

    /*
    Used following source to implement a spinner for selecting different speed
    options: https://www.geeksforgeeks.org/spinner-in-android-using-java-with-example/#
     */

    String[] speedChoices = { "Miles per Hour",
            "Kilometers per Hour", "Feet per Second"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationView = findViewById(R.id.location);
        speedValue = findViewById(R.id.speedValue);

        /*
        source for adding spinner: https://developer.android.com/develop/ui/views/components/spinner
        In addition to source above
         */

        Spinner speedUnits = findViewById(R.id.speeds_spinner);
        speedUnits.setOnItemSelectedListener(this);

        // Create the array containing the list of choices
        ArrayAdapter ad
                = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                speedChoices);

        // set simple layout resource file for each item of spinner
        ad.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);

        // Set the ArrayAdapter data on the Spinner which binds data to spinner
        speedUnits.setAdapter(ad);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button button = findViewById(R.id.getLocation);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startLocationUpdates();
            }
        });

    }

    //Pulled from online source: https://www.geeksforgeeks.org/how-to-get-user-location-in-android/
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    //end of copied section

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                locationView.setText("Getting Location...");
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(1000); // 1 seconds
                locationRequest.setFastestInterval(500); // 0.5 seconds
                mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());
            } else {
                Toast.makeText(this, "Please enable location.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }


    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    locationView.setText("Longitude: " + location.getLongitude() + "\nLatitude: " + location.getLatitude());

                    float speedMeters = location.getSpeed();

                    speedColors(speedMeters);
                    //speedUnits(speedMeters);

                }
            }
        }
    };



    //Change the speedValue fields color based on the speed calculated
    private void speedColors(float speedMeters) {
        if(speedMeters <= 5) {
            speedValue.setTextColor(Integer.parseInt("green"));
        } else if (speedMeters >5 && speedMeters <= 10) {
            speedValue.setTextColor(Integer.parseInt("yellow"));
        } else {
            speedValue.setTextColor(Integer.parseInt("red"));
        }
    }

    //Change the Units based on the Spinner item selected

    /*
    Commented out for time being while troubleshooting other things

    private void speedUnits(float speedMeters) {

        double speedCalc;

        if(spinner_choice = "Miles per Hour") {
            speedCalc = speedMeters * 2.23694;
            speedValue.setText(String.valueOf(speedCalc));
        } else if (spinner_choice = "Kilometers per Hour") {
            speedCalc = speedMeters * 3.60000;
            speedValue.setText(String.valueOf(speedCalc));
        } else if (spinner_choice = "Feet per Second") {
            speedCalc = speedMeters * 3.28084;
            speedValue.setText(String.valueOf(speedCalc));
        } else {
            speedValue.setText(String.valueOf(speedMeters));
        }

    }

     */

    private void stopLocationUpdates() {
        if (mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mLocationCallback = null;
        }
    }

    /*
    Unfilled onItemSelected and onNothingSelected Methods for spinners
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /*
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }
    */
}