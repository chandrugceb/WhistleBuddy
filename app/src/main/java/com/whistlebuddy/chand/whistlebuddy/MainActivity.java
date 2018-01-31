package com.whistlebuddy.chand.whistlebuddy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.whistlebuddy.chand.whistlebuddy.util.HttpPush;
import com.whistlebuddy.chand.whistlebuddy.util.User;

import java.util.Date;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, TextWatcher, LocationListener {

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "MainActivityTAG";
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private Button signOutButton;
    private TextView tvLoggedUser;
    private ConstraintLayout clHomeLayout;
    private FloatingActionButton fabGreen, fabAmber, fabRed;
    private ImageView ivPhoto;
    private EditText etTimerText;
    private SeekBar sbTimerAdjust;

    FirebaseDatabase myDB;
    DatabaseReference myRef, myContactRef, myCountRef, myLocation, myLocationHistory;
    GoogleSignInAccount account;
    User myuser;
    String Alert;
    String DisplayName;
    CountDownTimer currentTimer;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }


        // Set the dimensions of the sign-in button.
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);
        signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(this);
        tvLoggedUser = findViewById(R.id.tvLoggedUser);
        tvLoggedUser.setOnClickListener(this);
        clHomeLayout = findViewById(R.id.clHomeLayout);
        clHomeLayout.setOnClickListener(this);
        fabGreen = findViewById(R.id.fabGreen);
        fabGreen.setOnClickListener(this);
        fabAmber = findViewById(R.id.fabAmber);
        fabAmber.setOnClickListener(this);
        fabRed = findViewById(R.id.fabRed);
        fabRed.setOnClickListener(this);
        ivPhoto = findViewById(R.id.ivPhoto);
        etTimerText = findViewById(R.id.etTimerText);
        etTimerText.addTextChangedListener(this);
        sbTimerAdjust = findViewById(R.id.sbTimerAdjust);
        sbTimerAdjust.setOnSeekBarChangeListener(this);

        //Database related initializations
        myDB = FirebaseDatabase.getInstance();
        myRef = myDB.getReference("users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            saveLocation(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, MainActivity.this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 50, MainActivity.this);
        }
    }

    private void updateUI(final GoogleSignInAccount account) {
        if (account != null) {
            signInButton.setVisibility(View.INVISIBLE);
            tvLoggedUser.setVisibility(View.VISIBLE);
            etTimerText.setVisibility(View.VISIBLE);
            sbTimerAdjust.setVisibility(View.VISIBLE);
            tvLoggedUser.setText(account.getDisplayName());
            //ivPhoto.setImageBitmap(getbmpfromURL(account.getPhotoUrl().toString()));
            myuser = new User(account.getDisplayName(), account.getEmail());
            DisplayName = account.getDisplayName();
            myRef.child(account.getId()).child("displayname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, dataSnapshot.getKey().toString());
                    if (dataSnapshot.getValue() != null) {
                        Log.i(TAG, dataSnapshot.getValue().toString());
                        Log.i(TAG, FirebaseInstanceId.getInstance().getToken());
                        myRef.child(account.getId()).child("token").setValue(FirebaseInstanceId.getInstance().getToken());
                    } else {
                        Log.i(TAG, "dataSnapshot.getValue() is null, so adding user");
                        myRef.child(account.getId()).setValue(myuser);
                        myRef.child(account.getId()).child("token").setValue(FirebaseInstanceId.getInstance().getToken());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            signInButton.setVisibility(View.VISIBLE);
            tvLoggedUser.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.tvLoggedUser:
                tvLoggedUser.setVisibility(View.INVISIBLE);
                signOutButton.setVisibility(View.VISIBLE);
                break;
            case R.id.clHomeLayout:
                tvLoggedUser.setVisibility(View.VISIBLE);
                signOutButton.setVisibility(View.INVISIBLE);
                break;
            case R.id.fabGreen:
                onAlert("Green");
                if (currentTimer != null) {
                    currentTimer.cancel();
                }
                break;
            case R.id.fabAmber:
                onAlert("Amber");
                CountDown(Integer.valueOf(etTimerText.getText().toString()));
                setCountDownToCloud(etTimerText.getText().toString());
                break;
            case R.id.fabRed:
                onAlert("Red");
                if (currentTimer != null) {
                    currentTimer.cancel();
                }
                etTimerText.setText("0");
                goMap();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        signInButton.setVisibility(View.VISIBLE);
                        tvLoggedUser.setVisibility(View.INVISIBLE);
                        signOutButton.setVisibility(View.INVISIBLE);
                        etTimerText.setVisibility(View.INVISIBLE);
                        sbTimerAdjust.setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    public void postMessage(String Id) throws Exception {

        myContactRef = myDB.getReference("usercontact");
        myContactRef.child(Id).child("primarycontactreg").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HttpPush httpmessagepush = new HttpPush();
                try {
                    httpmessagepush.sendPost(DisplayName + " has raised " + Alert + " Alert", dataSnapshot.getValue().toString(), account.getId().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Implement the behavior for Each Alert Colors
    public void onAlert(String alert) {
        switch (alert) {
            case "Red":
                myRef.child(account.getId()).child("status").setValue("Red");
                try {
                    Alert = "Red";
                    postMessage(account.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                myRef.child(account.getId()).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, dataSnapshot.getKey().toString());
                        if (dataSnapshot.getValue() != null) {
                            Log.i(TAG, dataSnapshot.getValue().toString());
                            if (dataSnapshot.getValue().equals("Red")) {
                                clHomeLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case "Amber":
                myRef.child(account.getId()).child("status").setValue("Amber");
                try {
                    Alert = "Amber";
                    postMessage(account.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                myRef.child(account.getId()).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, dataSnapshot.getKey().toString());
                        if (dataSnapshot.getValue() != null) {
                            Log.i(TAG, dataSnapshot.getValue().toString());
                            if (dataSnapshot.getValue().equals("Amber")) {
                                clHomeLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case "Green":
                Alert = "Green";
                myRef.child(account.getId()).child("status").setValue("Green");
                myRef.child(account.getId()).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, dataSnapshot.getKey().toString());
                        if (dataSnapshot.getValue() != null) {
                            Log.i(TAG, dataSnapshot.getValue().toString());
                            if (dataSnapshot.getValue().equals("Green")) {
                                clHomeLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        etTimerText.setText(String.valueOf(sbTimerAdjust.getProgress()));

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Integer currentMax = sbTimerAdjust.getMax();
        Integer currentProgress = sbTimerAdjust.getProgress();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    public void setCountDownToCloud(String Seconds) {
        myCountRef = myDB.getReference("users");
        myCountRef.child(account.getId()).child("seconds").setValue(Seconds);
    }

    public void CountDown(Integer Seconds) {
        currentTimer = new CountDownTimer(Seconds * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                etTimerText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                onAlert("Red");
            }
        }.start();
    }

    public void goMap() {
        Intent mapIntent = new Intent(this, MapActivity.class);
        mapIntent.putExtra("origin","app");
        startActivity(mapIntent);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(Alert != "Green"){
            saveLocation(location);
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, MainActivity.this);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 50, MainActivity.this);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void saveLocation(Location location){
        if(account != null)
            {
                Date mydate = new Date();
                myLocation = myRef.child(account.getId()).child("location");
                myLocation.child("lat").setValue(String.valueOf(location.getLatitude()));
                myLocation.child("long").setValue(String.valueOf(location.getLongitude()));
                myLocation.child("updatetime").setValue(mydate.getTime());
                myLocationHistory = myRef.child(account.getId()).child("location").child("locationhistory");
                myLocationHistory.child(String.valueOf(mydate.getTime())).child("lat").setValue(location.getLatitude());
                myLocationHistory.child(String.valueOf(mydate.getTime())).child("long").setValue(location.getLongitude());
            }
    }
}
