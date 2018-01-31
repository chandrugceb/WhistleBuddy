package com.whistlebuddy.chand.whistlebuddy;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    GoogleMap map;
    LocationManager locationManager;
    String SenderId;
    FirebaseDatabase myDB;
    DatabaseReference myRef, myFriendLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapView);
        String origin = getIntent().getExtras().getString("origin");
        myDB = FirebaseDatabase.getInstance();
        myRef = myDB.getReference("users");
        if(origin.contains("notification")){
            SenderId = getIntent().getExtras().getString("senderid");
            //plot sender location
            plotSenderLocation(SenderId);
        }

        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d("MapTag", "Permission Check!");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MapTag", "No Permission");
            return;
        }
        Log.d("MapTag", "Permission is available!");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, MapActivity.this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 50, MapActivity.this);
    }

    private void plotSenderLocation(String senderId) {
        if(map != null) {
            map.clear();
        }
        myFriendLocation = myRef.child(senderId).child("location");
        myFriendLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LatLng currentLocation = new LatLng(Double.parseDouble(dataSnapshot.child("lat").getValue().toString()), Double.parseDouble(dataSnapshot.child("long").getValue().toString()));
                Log.d("MapTagInside", currentLocation.toString());


                MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_little_girl));
                markerOptions.position(currentLocation);
                markerOptions.title("Your Friend!");

                map.addMarker(markerOptions);

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.0f));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    @Override
    public void onLocationChanged(Location location) {
        //map.clear();
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d("MapTag", location.toString());


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLocation);
        markerOptions.title("i'm here");

        map.addMarker(markerOptions);

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.0f));
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

