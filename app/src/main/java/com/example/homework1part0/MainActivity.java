package com.example.homework1part0;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ColorSpace;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
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

class ChangeVariableHolder {

    double latChange = 0;
    double longChange = 0;
    double heightChange = 0;
    double speedChange = 0;
    double distanceChange = 0;

    ChangeVariableHolder() {

    }

    public void setHeightChange(double heightChange) {
        this.heightChange = heightChange;
    }

    public void setLatChange(double latChange) {
        this.latChange = latChange;
    }

    public void setLongChange(double longChange) {
        this.longChange = longChange;
    }

    public void setSpeedChange(double speedChange) {
        this.speedChange = speedChange;
    }

    public void setDistanceChange(double distanceChange) {
        this.distanceChange = distanceChange;
    }
}

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private final int PERMISSION_ID = 238947;
    private FusedLocationProviderClient mFusedLocationClient;
    TextView locationView, speedValue, elapsed_time, totDist, pauseText;
    Button pauser;
    boolean pauseTest = true, godModeFlag = false;

    private double longSpoof = 100, latSpoof = 40, heightSpoof = 1;
    private float speed = 0;

    ChangeVariableHolder changes = new ChangeVariableHolder();

    private String speedUnitsValue = "Meters per second";
    private String distUnitsValue = "Meters";
    private String timeUnitsValue = "Seconds";

    private Handler handler = new Handler();
    private long startTime = 0;
    private long initTime = 0;
    private long pausedTime = 0;
    private long addTime = 0;
    private long totalStopped = 0;
    private Location previousLocation;
    private double totalDistance = 0.0;
    private HandlerThread locationThread;
    private Handler locationHandler;

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
        pauseText = findViewById(R.id.title);
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
        pauser = findViewById(R.id.pause);
        initTime = System.currentTimeMillis();
        getSaveData();
        handler.post(updateTime);

        locationThread = new HandlerThread("LocationThread");
        locationThread.start();
        locationHandler = new Handler(locationThread.getLooper());

        pauser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!pauseTest){
                    startLocationUpdates();
                    pauseText.setText("Location App");
                    pauser.setText("Pause Updates");
                }
                else{
                    onClickPause();
                    pauseText.setText("Location App (Paused)");
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
                    pauseText.setText("Location App");
                }
                startLocationUpdates();
            }
        });

        Button reset = findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                totalDistance = 0;
                totalStopped = 0;
                startTime = SystemClock.elapsedRealtime();
                initTime = System.currentTimeMillis();
                totDist.setText("Distance Traveled: 0.0");
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

    /*
    Trying to implement time while moving, using the following as reference:
    https://stackoverflow.com/questions/58983710/adding-pause-and-resume-methods-to-stopwatch-class
    Basing the updates on distance changes since location.getSpeed always gives a positive value
     */

    private Runnable updateTime = new Runnable() {

        @Override
        public void run() {

            long elapsedTime = System.currentTimeMillis() - initTime;
            double seconds = (double) (elapsedTime / 1000);

            if(changes.distanceChange == 0) {

                if (pausedTime == 0) {
                    pausedTime = SystemClock.elapsedRealtime();
                }

                if (pausedTime != 0) {
                    long previousPause = pausedTime;
                    addTime = (SystemClock.elapsedRealtime() - previousPause);
                    totalStopped += addTime;
                    pausedTime = SystemClock.elapsedRealtime();
                }

            }

            if(changes.distanceChange != 0){

                if(pausedTime != 0){
                    long previousPause = pausedTime;
                    addTime = (SystemClock.elapsedRealtime() - previousPause);
                    totalStopped += addTime;
                    pausedTime = 0;
                }

                if(pausedTime == 0){
                    addTime = 0;
                }

            }

            long moveTime = ((SystemClock.elapsedRealtime() - startTime) - totalStopped);
            double secondsMove = (double) (moveTime / 1000);

            /*
            Change the displayed time units depending on the Time Spinner value selected
            Added IF statement to control for Moving Time, currently based on if change in distance,
            since the location.getSpeed() always gives a number not equal to 0, indicating moving objects
             */

            switch (timeUnitsValue){
                case "Seconds" :
                    elapsed_time.setText("Elapsed Time: " + seconds +"\nMoving Time: " + secondsMove);
                    break;
                case "Minutes" :
                    double minutes = (double) (seconds / 60);
                    double minutesMove = (double) (secondsMove / 60);
                    elapsed_time.setText("Elapsed Time: " + String.format("%.2f", minutes)
                            +"\nMoving Time: " + String.format("%.2f", minutesMove));
                    break;
                case "Hours" :
                    double hours = (double) (seconds / 3600);
                    double hoursMove = (double) (secondsMove / 3600);
                    elapsed_time.setText("Elapsed Time: " + String.format("%.4f", hours)
                            +"\nMoving Time: " + String.format("%.4f", hoursMove));
                    break;
                case "Days" :
                    double days = (double) (seconds / 86400);
                    double daysMove = (double) (secondsMove / 86400);
                    elapsed_time.setText("Elapsed Time: " + String.format("%.5f", days)
                            +"\nMoving Time: " + String.format("%.5f", daysMove));
                    break;
            }

            // Update the time every second
            handler.postDelayed(this, 1000);
        }
    };
    private void getSaveData(){
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        totalDistance = sharedPreferences.getFloat("totalDistance", 0);
        String lat = sharedPreferences.getString("Latitude", "0");
        String longi = sharedPreferences.getString("Longitude", "0");
        String alt = sharedPreferences.getString("Altitude", "0");
        initTime = sharedPreferences.getLong("InitTime", System.currentTimeMillis());
        totDist.setText("Distance Traveled: " + String.format("%.2f", totalDistance));
        locationView.setText("Longitude: " + longi
                +"\nLatitude: " + lat
                +"\nHeight: " + alt);
        pauseText.setText("Location App (Paused)");
        pauser.setText("Resume Updates");
        pauseTest = !pauseTest;
    }
    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String text = "Pause Updates: Pauses getting the users current location and speed.\n" +
                "\nGet Location, Speed, and Height: Gets the user's location, speed, and the height of the device every second.\n" +
                "\nAlt Mode: Spoofs the phones location and speed. Developer mode must be on. Also, get location and speed must be running as well.\n" +
                "\nThere are two times: Elapsed Time and Moving Time. Moving time only updates when the app is getting location information";

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
            if (mLocationCallback != null) {
                locationView.setText("Getting Location...");
                speedValue.setText("Getting Speed...");
                totDist.setText("Getting Elapsed Distance...");

                //creating function to multithread
                locationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequest.setInterval(1000); // 1 second
                        locationRequest.setFastestInterval(500); // 0.5 seconds
                        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());
                    }
                });
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

                    //Setting values for when Alt Mode is On

                    if(godModeFlag){
                        double intLong = 0.0000000076, intLat = 0;
                        longSpoof += intLong;
                        latSpoof += intLat;
                        location.setLatitude(longSpoof);
                        location.setLongitude(latSpoof);
                        location.setAltitude(heightSpoof);
                        location.setSpeed(4.4704f);
                    }

                    if(previousLocation == null) {

                        locationView.setText("Longitude: " + String.format("%.4f", location.getLongitude()) + " (" + String.format("%.4f", changes.latChange) +")"
                                + "\nLatitude: " + String.format("%.4f", location.getLatitude()) + " (" + String.format("%.4f",changes.longChange) +")"
                                + "\nHeight: " + String.format("%.2f", location.getAltitude()) + " meters " + " (" + String.format("%.2f",changes.heightChange) +")");
                        float temp2 = location.getSpeed();
                        double temp = speedUnitsCalc(temp2, speedUnitsValue);
                        speedValue.setText(String.format("%.2f", temp) + " ("  + String.format("%.2f",changes.speedChange) + ")");
                        speedColors(temp2);

                    }

                    else {

                        double deltaLat = location.getLatitude() - previousLocation.getLatitude();
                        changes.setLatChange(deltaLat);

                        double deltaLong = location.getLongitude() - previousLocation.getLongitude();
                        changes.setLongChange(deltaLong);

                        double deltaHeight = location.getAltitude() - previousLocation.getAltitude();
                        changes.setHeightChange(deltaHeight);

                        double deltaSpeed = location.getSpeed() - previousLocation.getSpeed();
                        double deltaSpeedAdj = speedUnitsCalc((float) deltaSpeed, speedUnitsValue);
                        changes.setSpeedChange(deltaSpeedAdj);

                        locationView.setText("Longitude: " + String.format("%.4f", location.getLongitude()) + " (" + String.format("%.4f", changes.latChange) +")"
                                + "\nLatitude: " + String.format("%.4f", location.getLatitude()) + " (" + String.format("%.4f",changes.longChange) +")"
                                + "\nHeight: " + String.format("%.2f", location.getAltitude()) + " meters " + " (" + String.format("%.2f",changes.heightChange) +")");
                        float temp2 = location.getSpeed();
                        double temp = speedUnitsCalc(temp2, speedUnitsValue);
                        speedValue.setText(String.format("%.2f", temp) + " ("  + String.format("%.2f",changes.speedChange) + ")");
                        speedColors(temp2);

                        if (previousLocation != location) {
                            float distance = location.distanceTo(previousLocation);
                            changes.setDistanceChange(distance);
                            totalDistance += distance;

                            switch (distUnitsValue){
                                case "Meters" :
                                    totDist.setText("Distance Traveled: " + String.format("%.2f", totalDistance)
                                            + " (" + String.format("%.2f", distance) + ")");
                                    break;
                                case "Kilometers" :
                                    double totalKilometers = (double) (totalDistance/ 1000);
                                    double deltaKil = (double) (distance /1000);
                                    totDist.setText("Distance Traveled: " + String.format("%.2f", totalKilometers)
                                            + " (" + String.format("%.2f", deltaKil) + ")");
                                    break;
                                case "Miles" :
                                    double totalMiles = (double) (totalDistance / 1609);
                                    double deltaMiles = (double) (distance /1609);
                                    totDist.setText("Distance Traveled: " + String.format("%.2f", totalMiles)
                                            + " (" + String.format("%.2f", deltaMiles) + ")");
                                    break;
                                case "Feet" :
                                    double totalFeet = (double) (totalDistance * 3.281);
                                    double deltaFeet = (double) (distance * 3.281);
                                    totDist.setText("Distance Traveled: " + String.format("%.2f", totalFeet)
                                            + " (" + String.format("%.2f", deltaFeet) + ")");
                                    break;
                            }

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
        changes.setDistanceChange(0);
        changes.setSpeedChange(0);
        changes.setHeightChange(0);
        changes.setLatChange(0);
        changes.setLongChange(0);
    }

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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("totalDistance", (float) totalDistance);
        editor.putString("Longitude", String.format("%.4f", previousLocation.getLongitude()));
        editor.putString("Latitude", String.format("%.4f", previousLocation.getLatitude()));
        editor.putString("Altitude", String.format("%.4f", previousLocation.getAltitude()));
        editor.putLong("InitTime", initTime);
        editor.apply();
        locationThread.quit();
    }
}