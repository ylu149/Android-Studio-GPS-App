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
import android.os.Handler;
import android.os.SystemClock;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private final int PERMISSION_ID = 238947;
    private FusedLocationProviderClient mFusedLocationClient;
    TextView locationView, speedValue, elapsed_time, totDist;
    ;
    boolean pauseTest = true, godModeFlag = false;

    private double longSpoof = 100, latSpoof = 40, heightSpoof = 1;
    private float speed = 0;

    private String speedUnitsValue = "Meters per second";

    private String distUnitsValue = "Meters";

    private String timeUnitsValue = "Seconds";
    private Handler handler = new Handler();
    private long startTime = 0;
    private Location previousLocation;
    private double totalDistance = 0.0;

    /*Used following source to implement a spinner for selecting different speed
    options: https://www.geeksforgeeks.org/spinner-in-android-using-java-with-example/#

    Created String Arrays to hold options for each spinner (Speed Units, Time Units, Distance Units)
     */

    String[] speedChoices = { "Meters per Second", "Miles per Hour",
            "Kilometers per Hour", "Feet per Second", "Minutes per Mile"};

    String[] timeChoices = { "Seconds", "Minutes", "Hours", "Days"};

    String[] distChoices = { "Meters", "Kilometers", "Miles", "Feet"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationView = findViewById(R.id.location);
        speedValue = findViewById(R.id.speedValue);
        elapsed_time = findViewById(R.id.time);
        totDist = findViewById(R.id.distance);
        /*
        Source for adding spinner: https://developer.android.com/develop/ui/views/components/spinner
        Creates Spinners for each toggle-able value, and adds an OnItemSelectedListener to each
         */

        Spinner speedUnits = findViewById(R.id.speeds_spinner);
        Spinner timeUnits = findViewById(R.id.times_spinner);
        Spinner distUnits = findViewById(R.id.distance_spinner);

        speedUnits.setOnItemSelectedListener(this);
        timeUnits.setOnItemSelectedListener(this);
        distUnits.setOnItemSelectedListener(this);

        /* Performs the following for the speed spinner:
        - Creates the array containing the list of speed choices
        - Sets simple layout resource file for each item of speed spinner
        - Sets the ArrayAdapter data on the Spinner which binds data to speed spinner
         */

        ArrayAdapter speedDropdown
                = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                speedChoices);

        speedDropdown.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);

        speedUnits.setAdapter(speedDropdown);

        /* Performs the following for the time spinner:
        - Creates the array containing the list of time choices
        - Sets simple layout resource file for each item of time spinner
        - Sets the ArrayAdapter data on the Spinner which binds data to time spinner
         */

        ArrayAdapter timeDropdown
                = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                timeChoices);

        timeDropdown.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);

        timeUnits.setAdapter(timeDropdown);

        /* Performs the following for the distance spinner:
        - Creates the array containing the list of distance choices
        - Sets simple layout resource file for each item of distance spinner
        - Sets the ArrayAdapter data on the Spinner which binds data to distance spinner
         */

        ArrayAdapter distDropdown
                = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                distChoices);

        distDropdown.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);

        distUnits.setAdapter(distDropdown);

        //End of Spinner section

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!checkPermissions()) {
            requestPermissions();
        }

        startTime = SystemClock.elapsedRealtime();
        handler.post(updateTime);

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
                    pauseText.setText("Updates are now paused.");
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

        Button godButton = findViewById(R.id.godMode);
        godButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                spoofLocLooper(40, 100);
                godModeFlag = !godModeFlag;
                if (godModeFlag){
                    godButton.setText("Alt Mode Off");
                }
                else {
                    godButton.setText("Alt Mode On");
                }
            }
        });

    }

    private Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            long elapsedTime = SystemClock.elapsedRealtime() - startTime;
            double seconds = (double) (elapsedTime / 1000);

            //Change the displayed time units depending on the Time Spinner value selected

            switch (timeUnitsValue){
                case "Seconds" :
                    elapsed_time.setText("Elapsed Time: " + seconds);
                    break;
                case "Minutes" :
                    double minutes = (double) (seconds / 60);
                    elapsed_time.setText("Elapsed Time: " + minutes);
                    break;
                case "Hours" :
                    double hours = (double) (seconds / 3600);
                    elapsed_time.setText("Elapsed Time: " + hours);
                    break;
                case "Days" :
                    double days = (double) (seconds / 86400);
                    elapsed_time.setText("Elapsed Time: " + days);
                    break;
            }

            // Update the time every second
            handler.postDelayed(this, 1000);
        }
    };

    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String text = "Pause Updates: Pauses getting the users current location and speed.\n" +
                "\nGet Location, Speed, and Height: Gets the user's location, speed, and the height of the device every second.\n" +
                "\nAlt Mode: Spoofs the phones location and speed. Developer mode must be on. Also, get location and speed must be running as well.\n";

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
                totDist.setText("Getting Elapsed Distance...");
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
                    if(godModeFlag){
                        double intLong = 0.0000000076, intLat = 0;
                        longSpoof += intLong;
                        latSpoof += intLat;
                        location.setLatitude(longSpoof);
                        location.setLongitude(latSpoof);
                        location.setAltitude(heightSpoof);
                        location.setSpeed(4.4704f);
                    }
                    locationView.setText("Longitude: " + location.getLongitude() + " degrees" + "\nLatitude: "
                            + location.getLatitude() + " degrees" + "\nHeight: " + location.getAltitude() + " meters");
                    float temp2 = location.getSpeed();
                    double temp = speedUnitsCalc(temp2, speedUnitsValue);
                    speedValue.setText(String.valueOf(temp));
                    speedColors(temp2);

                    if (previousLocation != null && previousLocation != location) {
                        float distance = location.distanceTo(previousLocation);
                        totalDistance += distance;

                        switch (distUnitsValue){
                            case "Meters" :
                                totDist.setText("Distance Traveled: " + totalDistance);
                                break;
                            case "Kilometers" :
                                double totalKilometers = (double) (totalDistance/ 1000);
                                totDist.setText("Distance Traveled: " + totalKilometers);
                                break;
                            case "Miles" :
                                double totalMiles = (double) (totalDistance / 1609);
                                totDist.setText("Distance Traveled: " + totalMiles);
                                break;
                            case "Feet" :
                                double totalFeet = (double) (totalDistance * 3.281);
                                totDist.setText("Distance Traveled: " + totalFeet);
                                break;
                        }

                    }
                    previousLocation = location;

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
            speedValue.setTextColor(-16776961); //int color for blue
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

        /*Using 1609 meters/1 mile rather than more precise 1609.34 for calculation
        calculation value is 1609/60 to 2 decimal places
         */

        } else if (spinner_choice == "Minutes per Mile") {
            speedCalc = speedMeters * 26.82;

        }else {
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

    /*
    Updated to have multiple spinners, reference:
    https://stackoverflow.com/questions/13716251/how-to-implements-multiple-spinner-with-different-item-list-and-different-action
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(parent.getId() == R.id.speeds_spinner){
            Spinner test = (Spinner) parent;
            String test2 = test.getSelectedItem().toString();
            speedUnitsValue = test2;
        }

        if(parent.getId() == R.id.times_spinner){
            Spinner test = (Spinner) parent;
            String test2 = test.getSelectedItem().toString();
            timeUnitsValue = test2;
        }

        if(parent.getId() == R.id.distance_spinner){
            Spinner test = (Spinner) parent;
            String test2 = test.getSelectedItem().toString();
            distUnitsValue = test2;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
