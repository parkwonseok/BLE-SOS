package org.techtown.blecommunication;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Advertise1 extends AppCompatActivity {
    // 조난자 정보 입력 ui
    EditText editNum;
    CheckBox sickRadio1;
    CheckBox sickRadio2;
    CheckBox sickRadio3;
    CheckBox sickRadio4;

    // 현재시간
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    // 패킷에 들어가는 정보
    String num;
    String time;
    String sick;
    String battery;

    public static final int REQUEST_ADVERTISE2_ACTIVITY = 102;   // Advertise2 액티비티 요청 상수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise1);

        editNum = findViewById(R.id.editNum);
        sickRadio1 = findViewById(R.id.sickCheck1);
        sickRadio2 = findViewById(R.id.sickCheck2);
        sickRadio3 = findViewById(R.id.sickCheck3);
        sickRadio4 = findViewById(R.id.sickCheck4);

        sickRadio1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sickRadio4.setChecked(false);
            }
        });

        sickRadio2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sickRadio4.setChecked(false);
            }
        });

        sickRadio3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sickRadio4.setChecked(false);
            }
        });

        sickRadio4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sickRadio1.setChecked(false);
                sickRadio2.setChecked(false);
                sickRadio3.setChecked(false);
            }
        });

        // 조난신호 송신 버튼 클릭시
        Button advBtn = (Button) findViewById(R.id.advBtn);
        advBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ui에서 입력 받은 정보를 문자열 변수에 저장
                // 조난인원
                num = editNum.getText().toString();
                if(num.equals("")){
                    num = "0";
                }
                // 조난일시
                time = getTime().substring(5, 16);
                // 현재상태
                int sickNum = 0;
                if(sickRadio1.isChecked()){
                    sickNum += 1;
                }
                if(sickRadio2.isChecked()){
                    sickNum += 2;
                }
                if(sickRadio3.isChecked()){
                    sickNum += 4;
                }
                if(sickRadio4.isChecked()){
                    sickNum = 0;
                }
                sick = String.valueOf(sickNum);
                // 배터리 잔량
                battery = String.valueOf(getBatteryRemain(getApplicationContext()));

                Intent intent = new Intent(getApplicationContext(), Advertise2.class);
                AdvertiseInfo advertiseInfo = new AdvertiseInfo(num, time, sick, battery);
                intent.putExtra("Data", advertiseInfo);
                startActivityForResult(intent, REQUEST_ADVERTISE2_ACTIVITY);
            }
        });
    }

    // 스마트폰 배터리 잔량 함수
    public static int getBatteryRemain(Context context) {
        Intent intentBattery = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = intentBattery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intentBattery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;

        return (int)(batteryPct * 100);
    }
    // 현재시간 함수
    public String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
}
