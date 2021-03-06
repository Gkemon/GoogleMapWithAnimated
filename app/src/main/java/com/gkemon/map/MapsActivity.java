package com.gkemon.map;

import android.Manifest;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public
class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private Marker currentLocationMarker;
    ValueAnimator valueAnimator = new ValueAnimator();
    private boolean firstTimeFlag = true;
    private GoogleMap mMap;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GroundOverlay circle;
    @Override
    protected
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        //মাস্ট এইটা করতে হবে --- https://stackoverflow.com/questions/33701865/release-apk-file-not-showing-google-maps
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
    public
    void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public
            void onCameraIdle() {

                if(currentLocationMarker!=null){
                    currentLocationMarker.setIcon(bitmapDescriptorFromVector(MapsActivity.this,R.drawable.ic_parking_markup));
                }




            }
        });


        //TODO: This is for actually Scroll listen
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason ==REASON_GESTURE) {
                    //"The user gestured on the map."



                    if(currentLocationMarker!=null){
                        currentLocationMarker.setIcon(bitmapDescriptorFromVector(MapsActivity.this,R.drawable.ic_my_location_black_24dp));
                    }




                } else if (reason ==REASON_API_ANIMATION) {



                    //"The user tapped something on the marker."

                } else if (reason ==REASON_DEVELOPER_ANIMATION) {
                   // "The app moved the camera."

                }
            }


        });


        if (!GPShelper.canAccessLocation(this)) {
            Log.d("GK", "can not AccessLocation");

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    1);
        } else {

        }

    }



    private void startCurrentLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                return;
            }
        }


        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());



      LocationManager  locationManager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);

     LocationListener locationListener = new LocationListener() {
            @Override
            public
            void onLocationChanged(Location location) {


                LatLng toLocation = new LatLng(location.getLatitude(), location.getLongitude());


                if(currentLocationMarker!=null){
                    currentLocationMarker.setPosition(toLocation);
                }
                else {

                    currentLocationMarker = mMap.addMarker(new MarkerOptions().icon(bitmapDescriptorFromVector(MapsActivity.this,R.drawable.ic_parking_markup)).position(toLocation));

                }


                if(circle!=null)
                {
                    circle.setPosition(toLocation);
                }
                else {
                    getCircle(toLocation);
                }




            }

            @Override
            public
            void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("GK"," onStatusChanged "+provider);
            }


            @Override
            public
            void onProviderEnabled(String provider) {
                Log.d("GK"," onProviderEnabled ");
            }

         @Override
         public
         void onProviderDisabled(String provider) {

         }

     };


        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);




    }

    void setUpValueAnimator(){
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setIntValues(0, 40);
        valueAnimator.setDuration(2000);
        valueAnimator.setEvaluator(new IntEvaluator());
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();

                if(circle!=null)circle.setDimensions(animatedFraction * 40 * 2);

            }
        });

        valueAnimator.start();
    }

    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null)
                return;
            currentLocation = locationResult.getLastLocation();
            if (firstTimeFlag && mMap != null) {
                animateCamera(currentLocation);
                firstTimeFlag = false;
                showMarker(currentLocation);
            }

        }
    };

    private void animateCamera(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
    }

    @NonNull
    private
    CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(16).build();
    }

    public void showMarker(@NonNull Location currentLocation) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (currentLocationMarker == null)
        {
            currentLocationMarker = mMap.addMarker(new MarkerOptions().icon(bitmapDescriptorFromVector(MapsActivity.this,R.drawable.ic_parking_markup)).position(latLng));


        }
        else {
                currentLocationMarker.setPosition(latLng);
             }

Log.d("GK","SHOW MARKER");
        getCircle(latLng);
        setUpValueAnimator();

    }




    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }



    void getCircle(LatLng latLng) {

        // The drawable to use for the circle
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setSize(500, 500);
        d.setColor(0x555751FF);
        d.setStroke(5, Color.TRANSPARENT);

        Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth()
                , d.getIntrinsicHeight()
                , Bitmap.Config.ARGB_8888);

        // Convert the drawable to bitmap
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);

        // Radius of the circle
        final int radius = 40;

        // Add the circle to the


        if(circle==null)
         circle = mMap.addGroundOverlay(new GroundOverlayOptions()
                .position(latLng, 2 * radius).image(BitmapDescriptorFactory.fromBitmap(bitmap)));



    }

    @Override
    protected void onResume() {
        super.onResume();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            startCurrentLocationUpdates();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient = null;
        mMap = null;
    }





    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startCurrentLocationUpdates();
                    Toast.makeText(MapsActivity.this, "Permission Access permitted", Toast.LENGTH_SHORT).show();

                 } else {
                    Toast.makeText(MapsActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
