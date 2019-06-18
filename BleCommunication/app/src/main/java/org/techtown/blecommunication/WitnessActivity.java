package org.techtown.blecommunication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WitnessActivity extends AppCompatActivity {

    RadioButton timeRadio1;
    RadioButton timeRadio2;
    RadioButton timeRadio3;
    RadioButton timeRadio4;
    RadioButton timeRadio5;
    RadioButton timeRadio6;
    RadioButton timeRadio7;
    RadioButton timeRadio8;

    EditText editContent;
    Button submitBtn;
    Button locationBtn;
    TextView locationTv;

    String time;
    String content;
    double longitude;
    double latitude;
    int counter;
    int fire_id;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private DatabaseReference counterRef;


    private DbOpenHelper mDbOpenHelper; //내부 DB 관리
    private Cursor mCursor; // DB 관련
    private UserInfo mUserInfo;
    String TAG = "database";

    @Override
    protected void onStart() {
        super.onStart();

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mFirebaseInstance = FirebaseDatabase.getInstance();
        //referencce 초기 설정("DataUsers/")
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");
        counterRef = mFirebaseDatabase.child("Witness").child("counter");
        counterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //sos_id
                counter= Integer.parseInt(String.valueOf(dataSnapshot.getValue(Integer.class)));
                Log.d("counter", String.valueOf(counter));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( WitnessActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    0 );
        }
        else{
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000,
                    1,
                    gpsLocationListener);
            String provider = location.getProvider();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d(TAG, "위치정보 : " + provider + "\n" +
                    "위도 : " + longitude + "\n" +
                    "경도 : " + latitude + "\n");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_witness);

        timeRadio1 = findViewById(R.id.timeRadio1);
        timeRadio2 = findViewById(R.id.timeRadio2);
        timeRadio3 = findViewById(R.id.timeRadio3);
        timeRadio4 = findViewById(R.id.timeRadio4);
        timeRadio5 = findViewById(R.id.timeRadio5);
        timeRadio6 = findViewById(R.id.timeRadio6);
        timeRadio7 = findViewById(R.id.timeRadio7);
        timeRadio8 = findViewById(R.id.timeRadio8);

        editContent = findViewById(R.id.edit_content);
        submitBtn = findViewById(R.id.submitBtn);
        locationTv = findViewById(R.id.locationTv);

        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        getUserInfo();



        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ui에서 입력 받은 정보를 문자열 변수에 저장
                // 조난시간
                if(timeRadio1.isChecked()){
                    time = "0";
                }
                else if(timeRadio2.isChecked()){
                    time = "1";
                }
                else if(timeRadio3.isChecked()){
                    time = "2";
                }
                else if(timeRadio4.isChecked()){
                    time = "3";
                }
                else if(timeRadio5.isChecked()){
                    time = "4";
                }
                else if(timeRadio6.isChecked()){
                    time = "5";
                }
                else if(timeRadio7.isChecked()){
                    time = "6";
                }
                else if(timeRadio8.isChecked()){
                    time = "7";
                }
                else{
                    time = "0";
                }


                content = editContent.getText().toString();
                addWitness(fire_id, time, content, longitude, latitude);
                Toast.makeText(getApplicationContext(), "제보가 완료되었습니다.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            }
        });

    }

    public void getUserInfo() {
        mCursor = null;
        //DB에 있는 모든 컬럼을 가져옴
        mCursor = mDbOpenHelper.getAllColumns();
        mCursor.moveToLast();
        mUserInfo = new UserInfo(
                mCursor.getInt(mCursor.getColumnIndex("_id")),
                mCursor.getInt(mCursor.getColumnIndex("fire_id")),
                mCursor.getString(mCursor.getColumnIndex("name")),
                mCursor.getString(mCursor.getColumnIndex("gender")),
                mCursor.getString(mCursor.getColumnIndex("birth")),
                mCursor.getString(mCursor.getColumnIndex("contact")),
                mCursor.getString(mCursor.getColumnIndex("disease"))
        );
        mCursor.close();
        fire_id = mUserInfo.getFire_id();
    }

    public void addWitness(int fire_id, String time, String content, double longitude, double latitude){
        WitnessInfo witnessInfo = new WitnessInfo(fire_id, time, content, longitude, latitude);
        Log.d(TAG, witnessInfo.getUser_id() + "/" +
                witnessInfo.getContent()+ "/" +
                witnessInfo.getLongitude()+ "/" +
                witnessInfo.getLatitude()+ "/" +
                witnessInfo.getUser_id());
        mFirebaseDatabase.child("Witness").child(String.valueOf(counter+1)).setValue(witnessInfo);
        mFirebaseDatabase.child("Witness").child("counter").setValue(counter+1);
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            String provider = location.getProvider();
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            locationTv.setText("위치정보 : " + provider + "\n" +
                    "위도 : " + longitude + "\n" +
                    "경도 : " + latitude + "\n" );
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };


}
