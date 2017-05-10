package com.chrishonwyllie.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, LocationListener {
// This is the original class declaration. Causes crash with backbutton
//public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // initialize to negative 1 to symbolize null value
    int passedInLocation = -1;

    LocationManager locationManager;
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setupBackButton();
        setupLocationManager();

        Intent i = getIntent();
        Log.i("locationInfo", Integer.toString(i.getIntExtra("locationInfo", -1)));
        passedInLocation = i.getIntExtra("locationInfo", -1);

    }

    ///*
    @Override
    protected void onResume() {
        super.onResume();


        setupMapIfNeeded();

        if (passedInLocation == 1 || passedInLocation == 0) {
            requestUserLocation();
        }
    }

    private void requestUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            System.out.println("User location permission was NOT granted. Requesting access");

            String permission = Manifest.permission.ACCESS_FINE_LOCATION;
            final Integer requestCode = 0x1;

            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{permission}, requestCode);

            return;
        } else {
            System.out.println("User location permission was granted");
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    private void setupMapIfNeeded() {

        if (mMap == null) {

            Log.i("Info", "mMap was null");
            System.out.println("mMap was null");


        } else {
            Log.i("Info", "mMap was not null");
            System.out.println("mMap was not null");
            setupMap();
        }
    }

    private void setupMap() {

        // -1 = error getting point, 0 = user attempting to add new point
        if (passedInLocation != -1 && passedInLocation != 0) {

            System.out.println("passedInLocation was NEITHER -1 nor 0: " + passedInLocation);

            locationManager.removeUpdates(this);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.locations.get(passedInLocation), 10));
            mMap.addMarker(new MarkerOptions().position(MainActivity.locations.get(passedInLocation)).title(MainActivity.places.get(passedInLocation)));
        } else {

            System.out.println("passedInLocation was either -1 or 0: " + passedInLocation + ". Requesting user location");

            requestUserLocation();
        }
    }




    private void setupLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
    }


    // For top NavBar

    private void setupBackButton() {
        ActionBar actionbar = getSupportActionBar();
        //ActionBar actionBar = getActionBar();

        if (actionbar == null) {
            Log.e("Info", "NULL action bar");
        } else {
            Log.e("Info", "action bar is not NULL");
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                // This effectively returns to the parentActivity
                locationManager.removeUpdates(this);
                Log.i("Info", "Going back to main activity");
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

        setupMap();
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder  = new Geocoder(getApplicationContext(), Locale.getDefault());

        // If no address can be retrieved from longPress location, set the title of the address to today's date as a default
        String address = new Date().toString();

        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (listAddresses != null && listAddresses.size() > 0) {

                // Get the first address that comes from this long press
                address = listAddresses.get(0).getAddressLine(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged(); // ReloadData()...

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(address)
                .snippet("Your marker snippet"));
    }









    // User Location Listener methods

    @Override
    public void onLocationChanged(Location location) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
