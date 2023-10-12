package com.example.homework1part0;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    boolean pauseTest = true;

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

        if (!checkPermissions()) {
            requestPermissions();
        }


        TextView pauseText = findViewById(R.id.pauseFlag);
        Button pauser = findViewById(R.id.pause);
        pauser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!pauseTest){
                    startLocationUpdates();
                    pauseText.setText("");
                    pauser.setText("Pause Updates");
                }
                else{
                    onClickPause();
                    pauseText.setText("You are now in pause mode. Nothing will be updated. Press resume to continue.");
                    pauser.setText("Resume Updates");
                }
                pauseTest = !pauseTest;
            }
        });

        Button locButton = findViewById(R.id.getLocation);
        locButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!pauseTest){
                    pauseText.setText("");
                    pauser.setText("Pause Updates");
                    pauseTest = !pauseTest;
                }
                startLocationUpdates();
            }
        });

        Button helpButtoon = findViewById(R.id.help);
        helpButtoon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showHelp();
            }
        });
    }

    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String text = "Pause Updates: Pauses getting the users current location and speed.\n" +
                "\nGet Location and Speed: Gets the user's location and speed every second.\n";

        builder.setMessage(text);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (isLocationEnabled()) {
            if(mLocationCallback != null){
                locationView.setText("Getting Location...");
                speedValue.setText("Getting Speed...");
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(1000); // 1 seconds
                locationRequest.setFastestInterval(500); // 0.5 seconds
                mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());
            }
        } else {
            locationPermHelper();
            }
    }

    private void locationPermHelper(){
        Toast.makeText(this, "Please enable location.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
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

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    locationView.setText("Longitude: " + location.getLongitude() + "\nLatitude: " + location.getLatitude());
                    speedValue.setText(String.valueOf(location.getSpeed()));
                }
            }
        }
    };


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
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    protected void onClickPause() {
        stopLocationUpdates();
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

}
