package com.example.christian.distancetracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements OnClickListener, LocationListener {
    private double latA;
    private double longA;
    private double latB;
    private double longB;
    private double locA;
    private double locB;

    private Location startLocation;
    private Location stopLocation;
    private Location tempLocation;
    private LocationManager locationManager;
    private MainActivity instance;
    private boolean isRunning;

    DecimalFormat df = new DecimalFormat("#.##");

    TextView textMilesValue;
    TextView textMetersValue;
    TextView textYardsValue;
    Button btnStart;
    Button btnStop;
    Button btnClear;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        //initialize buttons
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnClear = (Button) findViewById(R.id.btnClear);
        isRunning = false;

        //initialize locationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //request permissions for location function of the phone from the user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
            return;
        }
        //get updates every 1 second and 0.1 meters using the locationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.1f, this);

        //make the stop and clear buttons not usable when the app is opened
        btnStop.setEnabled(false);
        btnClear.setEnabled(false);

        //btnStart method
        btnStart.setOnClickListener(new View.OnClickListener() {


            //onClick for btnStart
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(instance, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(instance, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                isRunning = true;
                btnStart.setEnabled(false);
                TextView message = (TextView) findViewById(R.id.textMetersValue);
                message.setText(" ");
                System.out.println(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));

                //10 second timer for the app to find the GPS satellite of the app
                for(int i = 0; i < 10; i++){
                    System.out.println(i);
                    if(null == tempLocation){
                        try {
                            message.setText("Acquiring GPS..." + i);
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            isRunning = false;
                            message.setText("error: " + e.getMessage());
                            btnClear.setEnabled(true);
                            return;
                        }
                    }
                    else{
                        break;
                    }
                }
                //if there is not a tempLocation acquired from GPS, show error message
                if(null == tempLocation){
                    isRunning = false;
                    tempLocation = null;
                    System.out.println("Unable to Acquire GPS");
                    message.setText("Unable to Acquire GPS. Move to a better location and try again.");
                    btnClear.setEnabled(true);
                    return;
                }
                startLocation = new Location(tempLocation);
                //get latitude and longitude of point A
                latA = startLocation.getLatitude();
                longA = startLocation.getLongitude();
                System.out.println(latA);
                System.out.println(longA);

                Location currLocation = new Location (tempLocation);
                float distance = currLocation.distanceTo(startLocation);

                for(int i = 0; i < 10; i++){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    currLocation = new Location (tempLocation);
                    distance = currLocation.distanceTo(startLocation);
                    startLocation = new Location(currLocation);
                    if (distance > 1.0) {
                        System.out.println(distance);
                        continue;
                    }
                    else{
                        break;
                    }
                }
                //if moving a meter per "step", accurate location will not be retrieved until user stops moving
                if (distance > 1.0) {
                    message.setText("Please stop moving to acquire accurate location.");
                    btnClear.setEnabled(true);
                    return;
                }
                //fills the text areas with the latitude and longitude point found
                TextView meters = (TextView) findViewById(R.id.textMetersValue);
                meters.setText("latA: " + latA);

                TextView yards = (TextView) findViewById(R.id.textYardsValue);
                yards.setText("longA: " + longA);

                TextView miles = (TextView) findViewById(R.id.textMilesValue);
                miles.setText("0.0");

                //enables the stop button and disables the clear button
                btnStop.setEnabled(true);
                btnClear.setEnabled(false);
            }
        });
        //stop button method
        btnStop.setOnClickListener(new View.OnClickListener() {
            //onClick for the stop button
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(instance, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(instance, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                isRunning = false;
                if(null == tempLocation){
                    stopLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }else{
                    stopLocation = new Location(tempLocation);
                }
                //finds the latitude and longitude of point B
                latB = stopLocation.getLatitude();
                longB = stopLocation.getLongitude();
                System.out.println(latB);
                System.out.println(longB);

                //test to show the difference between satellite and system times
                System.out.println("stopLocation Sat time: " + stopLocation.getTime());
                System.out.println("stopLocation Sys time: " + System.currentTimeMillis());
                System.out.println((System.currentTimeMillis() - stopLocation.getTime()));

                TextView meters = (TextView) findViewById(R.id.textMetersValue);
                TextView yards = (TextView) findViewById(R.id.textYardsValue);
                TextView miles = (TextView) findViewById(R.id.textMilesValue);

                //if block that displays error message for when GPS connection is lost
                long stale = (System.currentTimeMillis() - stopLocation.getTime());
                if(stale > 20000){
                    System.out.println("stale" + stale);
                    meters.setText("Lost Satellite connection. Try moving a few feet over and press stop again.");
                    yards.setText(" ");
                    miles.setText(" ");
                    isRunning = true;
                    btnClear.setEnabled(true);
                    return;
                }
                //calculations and displaying of measurements in their TextViews
                meters.setText(String.valueOf(df.format(startLocation.distanceTo(stopLocation))));
                yards.setText(String.valueOf(df.format(startLocation.distanceTo(stopLocation)*1.0936)));
                miles.setText(String.valueOf(df.format(startLocation.distanceTo(stopLocation)*0.00062137)));

                tempLocation = null;
                //sets clear and start buttons to enabled and stop to false
                btnClear.setEnabled(true);
                btnStop.setEnabled(false);
                btnStart.setEnabled(true);
            }
        });
        //clear button method
        btnClear.setOnClickListener(new View.OnClickListener() {
            //onClick for the clear button
            @Override
            public void onClick(View v) {
                isRunning = false;
                startLocation = null;
                stopLocation = null;
                tempLocation = null;

                TextView meters = (TextView) findViewById(R.id.textMetersValue);
                meters.setText(" ");

                TextView yards = (TextView) findViewById(R.id.textYardsValue);
                yards.setText(" ");

                TextView miles = (TextView) findViewById(R.id.textMilesValue);
                miles.setText(" ");

                //disables the clear button and enables the start button
                btnStart.setEnabled(true);
                btnClear.setEnabled(false);
            }
        });
    }
    //onClick view method
    @Override
    public void onClick(View v) {

    }
    //onRequestPermissionsResult method
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        System.out.println("onRequestPermissionsResult");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }
    //onLocationChanged method
    @Override
    public void onLocationChanged(Location location) {
        tempLocation = location;
        if(!isRunning){
            startLocation = new Location(location);
            TextView meters = (TextView) findViewById(R.id.textMetersValue);
            meters.setText(String.valueOf(location.getTime()));
            TextView yards = (TextView) findViewById(R.id.textYardsValue);
            yards.setText(String.valueOf(location.getSpeed()));
        }
        if (isRunning) {
            System.out.println("onLocationChanged");
            if (ActivityCompat.checkSelfPermission(instance, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(instance, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
//            stopLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            //gets the latitude and longitude of the point that is found upon each update
            latB = location.getLatitude();
            longB = location.getLongitude();
            System.out.println(latB);
            System.out.println(longB);

            //does the calculation of measurements in real time
            if(startLocation != null) {
                System.out.println("Sat time: " + tempLocation.getTime());
                System.out.println("Sys time: " + System.currentTimeMillis());
                System.out.println((System.currentTimeMillis() - tempLocation.getTime()));

                TextView meters = (TextView) findViewById(R.id.textMetersValue);
                meters.setText(String.valueOf(df.format(startLocation.distanceTo(location))));

                TextView yards = (TextView) findViewById(R.id.textYardsValue);
                yards.setText(String.valueOf(df.format(startLocation.distanceTo(location) * 1.0936)));

                TextView miles = (TextView) findViewById(R.id.textMilesValue);
                miles.setText(String.valueOf(df.format(startLocation.distanceTo(location) * 0.00062137)));
            }else{
                System.out.println("startLocation was null");
            }
        }
    }
    //onStatusChanged method
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){

    }
    //onProviderEnabled method
    @Override
    public void onProviderEnabled(String provider) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    @Override
    public void onProviderDisabled(String provider){

    }
}

