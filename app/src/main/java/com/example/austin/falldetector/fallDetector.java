package com.example.austin.falldetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public abstract class fallDetector implements SensorEventListener {
    private final double LOWER_THRESHOLD = 6;
    private final double HIGHER_THRESHOLD = 15;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    public DataManager dataManager = new DataManager();
    Queue<Double> magnitudes = new LinkedList<Double>();

    public FusedLocationProviderClient mFusedLocationClient;

    Context activity;

    public fallDetector(Context activity) {
        this.activity = activity;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private boolean boolLower = false;
    private boolean boolHigher = false;

    public static boolean fell = false;

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        double magnitude = Math.sqrt(x * x + y * y + z * z);
        if (magnitudes.size() < 15)
            magnitudes.add(magnitude);
        else
        {
            double magnitudeAverage = 0;
            for(Double magn : magnitudes){
                magnitudeAverage += magn;
            }
            magnitudeAverage = magnitudeAverage/magnitudes.size();

            if(magnitude/magnitudeAverage > 3){
                onFall();
            }
            else{
                magnitudes.remove();
                magnitudes.add(magnitude);
            }
        }
    }

    private void getLastLocation() {
        Log.d("Fetch", "Loc");
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    HashMap<String, Double> loc = new HashMap<String, Double>();
                    loc.put("latitude", location.getLatitude());
                    loc.put("longitude", location.getLongitude());
                    Log.d("Got Location", Double.toString(loc.get("latitude")));
                } else {
                    Log.d("Got Nothing", "Lol");
                    return;
                    // Do not send error on events otherwise it will produce an error
                }
            }
        });
    }

    public void onFall(){
        Log.d("Detected Fall", "A fall was detected");
        dataManager.Write("fakeUserId");
        magnitudes.clear();
        getLastLocation();
    };
    
    //not necessary, just to satisfy interface for now
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}



