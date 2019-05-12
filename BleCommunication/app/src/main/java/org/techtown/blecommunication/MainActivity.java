package org.techtown.blecommunication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.app.AlertDialog;
import android.widget.TextView;

import java.util.ArrayList;
//내용바꾸기
//스캐닝하는중
// 완료
public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_ADVERTISE1_ACTIVITY = 101;   // Advertise1 액티비티 요청 상수
    public static final int REQUEST_SCAN1_ACTIVITY = 201;   // Scan1 액티비티 요청 상수


    private DbOpenHelper mDbOpenHelper; //내부 DB 관리
    private Cursor mCursor; // DB 관련
    private UserInfo mUserInfo;
    private ArrayList<UserInfo> mInfoArr;
    private static final String TAG = "TestDataBase";

    BluetoothManager bleManager;   // 블루투스 관리
    BluetoothAdapter bleAdapter;   // 블루투스 검색, 연결

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //내장 DB 실행
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mInfoArr = new ArrayList<UserInfo>();


        bleActivation();   // 블루투스 활성화 하기

        getLocationAccess();   // 위치권한 얻기

        // 조난자 버튼 클릭시
        Button advBtn = (Button) findViewById(R.id.advBtn);
        advBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Advertise1 액티비티로 전환
                Intent intent = new Intent(getApplicationContext(), Advertise1.class);
                startActivityForResult(intent, REQUEST_ADVERTISE1_ACTIVITY);
            }
        });

        // 구조자 버튼 클릭시
        Button scanBtn = (Button) findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Scan1 액티비티로 전환
                Intent intent = new Intent(getApplicationContext(), Scan1.class);
                startActivityForResult(intent, REQUEST_SCAN1_ACTIVITY);
            }
        });

        //유저정보 표시
        TextView userInfoTv = findViewById(R.id.UserInfoTV);
        userInfoTv.setText(getUserInfo()+"님 반갑습니다.");

        //내 정보 바꾸기
        Button editUserInfoBtn = findViewById(R.id.bt_revise_myInfo);
        editUserInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra("name", mUserInfo.getName()); /*송신*/
                intent.putExtra("gender",mUserInfo.getGender());
                intent.putExtra("birth", mUserInfo.getBirth());
                intent.putExtra("contact", mUserInfo.getContact());
                intent.putExtra("disease", mUserInfo.getDisease());
                intent.putExtra("editing", true);
                startActivity(intent);
            }
        });
    }

    // 블루투스 활성화 함수
    public void bleActivation(){
        bleManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bleManager.getAdapter();
        if (bleAdapter != null && !bleAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
    }

    // 위치권한 요청 함수
    public void getLocationAccess(){
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("사용자 위치 권한 필요");
            builder.setMessage("블루투스 신호 탐색을 위해서 사용자의 위치 권한이 필요합니다.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
            });
            builder.show();
        }
    }

    public String getUserInfo(){
        mCursor = null;
        //DB에 있는 모든 컬럼을 가져옴
        mCursor = mDbOpenHelper.getAllColumns();

        String Tvcontent;
        //컬럼의 갯수 확인
        while(mCursor.moveToNext()){
            mUserInfo = new UserInfo(
                    mCursor.getInt(mCursor.getColumnIndex("_id")),
                    mCursor.getString(mCursor.getColumnIndex("name")),
                    mCursor.getString(mCursor.getColumnIndex("gender")),
                    mCursor.getString(mCursor.getColumnIndex("birth")),
                    mCursor.getString(mCursor.getColumnIndex("contact")),
                    mCursor.getString(mCursor.getColumnIndex("disease"))
            );
            mInfoArr.add(mUserInfo);
            Log.i(TAG, "ID = " + mUserInfo._id);
        }

        //Cursor 닫기
        mCursor.close();

        Tvcontent = mUserInfo.getName();
        return Tvcontent;
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }
}
