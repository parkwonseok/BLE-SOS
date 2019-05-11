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
import android.widget.RadioButton;

public class Advertise1 extends AppCompatActivity {
    // 조난자 정보 입력 ui
    EditText editNum;
    RadioButton timeRadio1;
    RadioButton timeRadio2;
    RadioButton timeRadio3;
    RadioButton timeRadio4;
    RadioButton timeRadio5;
    RadioButton timeRadio6;
    RadioButton timeRadio7;
    RadioButton timeRadio8;
    CheckBox sickRadio1;
    CheckBox sickRadio2;
    CheckBox sickRadio3;
    CheckBox sickRadio4;

    // 입력 받은 조난자 정보 저장할 문자열 변수
    String num;
    String time;
    String sick;
    int batteryPct;
    String battery;

    public static final int REQUEST_ADVERTISE2_ACTIVITY = 102;   // Advertise2 액티비티 요청 상수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise1);

        editNum = findViewById(R.id.editNum);
        timeRadio1 = findViewById(R.id.timeRadio1);
        timeRadio2 = findViewById(R.id.timeRadio2);
        timeRadio3 = findViewById(R.id.timeRadio3);
        timeRadio4 = findViewById(R.id.timeRadio4);
        timeRadio5 = findViewById(R.id.timeRadio5);
        timeRadio6 = findViewById(R.id.timeRadio6);
        timeRadio7 = findViewById(R.id.timeRadio7);
        timeRadio8 = findViewById(R.id.timeRadio8);
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
                batteryPct = getBatteryRemain(getApplicationContext());
                if(batteryPct == 100){
                    battery = "00";
                }
                else{
                    battery = String.valueOf(batteryPct);
                }

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
}
