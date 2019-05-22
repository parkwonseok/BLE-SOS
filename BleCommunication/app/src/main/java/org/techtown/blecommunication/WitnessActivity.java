package org.techtown.blecommunication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    String counter_set;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private int UserId;
    private String fire_id;

    private DbOpenHelper mDbOpenHelper; //내부 DB 관리
    private Cursor mCursor; // DB 관련
    private UserInfo mUserInfo;
    private ArrayList<UserInfo> mInfoArr;
    String TAG = "database";



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
        locationBtn = findViewById(R.id.locationBtn);
        locationTv = findViewById(R.id.locationTv);

        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mInfoArr = new ArrayList<UserInfo>();
        getUserInfo();

        //counter



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
                addWitness(counter, time, content, longitude, latitude);

            }
        });

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference counterRef = mFirebaseDatabase.child("Witness").child("counter");
                counterRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(currentValue + 1);
                            counter = currentValue;
                        }

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(
                            DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                        System.out.println("Transaction completed");
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



//                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                            1000,
//                            1,
//                            gpsLocationListener);

                }

            }
        });

    }

    public String getUserInfo(){
        mCursor = null;
        //DB에 있는 모든 컬럼을 가져옴
        mCursor = mDbOpenHelper.getAllColumns();

        String User_name;
        //컬럼의 갯수 확인
        while(mCursor.moveToNext()){
            mUserInfo = new UserInfo(
                    mCursor.getInt(mCursor.getColumnIndex("_id")),
                    mCursor.getInt(mCursor.getColumnIndex("fire_id")),
                    mCursor.getString(mCursor.getColumnIndex("name")),
                    mCursor.getString(mCursor.getColumnIndex("gender")),
                    mCursor.getString(mCursor.getColumnIndex("birth")),
                    mCursor.getString(mCursor.getColumnIndex("contact")),
                    mCursor.getString(mCursor.getColumnIndex("disease"))
            );
            mInfoArr.add(mUserInfo);
        }

        //Cursor 닫기
        mCursor.close();
        User_name = mUserInfo.getName();
        UserId = mUserInfo.getFire_id();
        Log.d(TAG, User_name +" ///"+ UserId);
        return User_name;
    }

    public void addWitness(int counter, String time, String content, double longitude, double latitude){
        WitnessInfo witnessInfo = new WitnessInfo(counter, time, content, longitude, latitude, counter);
        counter_set = String.valueOf(counter);
        Log.d(TAG, witnessInfo.getUser_id() + "/" +
                witnessInfo.getContent()+ "/" +
                witnessInfo.getLongitude()+ "/" +
                witnessInfo.getLatitude()+ "/" +
                witnessInfo.getCounter());
        mFirebaseDatabase.child("Witness").child(counter_set).setValue(witnessInfo);
    }

    public void readDate(View view){
        mFirebaseDatabase.child("Witness").addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    counter =  ds.child("counter").getValue(Integer.class);
                    Log.d("TAG", "" + counter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){

            }

        });
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
