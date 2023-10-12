package com.example.homework1part0;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ColorSpace;
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

    private float speed = 0;
    private String spinnerVal = "Meters per second";

    /*
    Used following source to implement a spinner for selecting different speed
    options: https://www.geeksforgeeks.org/spinner-in-android-using-java-with-example/#
     */

    String[] speedChoices = { "Meters per Second", "Miles per Hour",
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

        Button locationButton = findViewById(R.id.getLocation);
        Button speedButton = findViewById(R.id.getSpeed);

        locationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startLocationUpdates();
            }
        });

        /*
        speedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startSpeedUpdates();
            }
        });
        */
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

                    double temp = speedUnitsCalc(location.getSpeed(), spinnerVal);
                    speedValue.setText(String.valueOf(temp));

                    speedColors(speed);

                }
            }
        }
    };

    /*Copied code from startLocationUpdates, with some fields
    adjusted to account for different TextView.
    Not sure if this is correct to use, as the requestLocationUpdates is giving errors,
    although the app still runs
     */

    /*
    private void startSpeedUpdates() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                speedValue.setText("Getting Speed...");
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(1000); // 1 seconds
                locationRequest.setFastestInterval(500); // 0.5 seconds
                mFusedLocationClient.requestLocationUpdates(locationRequest, speedLocationCallback, Looper.getMainLooper());
            } else {
                Toast.makeText(this, "Please enable location.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }
     */

    /*
    Also copies code for mLocationCallback, with proper adjustments for speedValue instead
     */

    /*
    private LocationCallback speedLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    //speedValue.setText(String.valueOf(location.getSpeed()));

                    //float speedMeters = location.getSpeed();

                    //float speedMeters = 2;  //for testing colors
                    float speedMeters = 6;  //for testing colors
                    //float speedMeters = 11;  //for testing colors

                    speedValue.setText("6.00"); //test for setting speedValue text

                    speedColors(speedMeters);

                }
            }
        }
    };
*/

    /*
    Change the speedValue fields color based on the speed calculated
    Color int values obtained from here:
    https://developer.android.com/reference/android/graphics/Color
     */
    private void speedColors(float speedMeters) {
        if(speedMeters <= 5) {
            speedValue.setTextColor(-16711936); //int color for green

        } else if (speedMeters >5 && speedMeters <= 10) {
            speedValue.setTextColor(-256); //int color for yellow
        } else {
            speedValue.setTextColor(-65536); //int color for red
        }
    }



    //Change the Units based on the Spinner item selected

    /*
    Commented out for time being while troubleshooting other things
    */
    private double speedUnitsCalc(float speedMeters, String spinner_choice) {

        double speedCalc = 0;

        if(spinner_choice == "Miles per Hour") {
            speedCalc = speedMeters * 2.23694;

        } else if (spinner_choice == "Kilometers per Hour") {
            speedCalc = speedMeters * 3.60000;

        } else if (spinner_choice == "Feet per Second") {
            speedCalc = speedMeters * 3.28084;

        } else {
            speedCalc = speedMeters;
        }

        return speedCalc;
    }


    private void stopLocationUpdates() {
        if (mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mLocationCallback = null;
        }
    }

    /*
    onItemSelected updated to call speedUnitsCalc function whenever object is selected in spinner
    Note that a dummy variable is fed in for speedMeters for now
    onNothingSelected Methods for spinners is unfilled
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner test = (Spinner) parent;
        String test2 = test.getSelectedItem().toString();
        spinnerVal = test2;

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