package org.techtown.blecommunication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Scan2 extends AppCompatActivity {
    TextView textView;
    String scanData;
    String num;
    String time;
    String sick;
    String battery;

    public static final int REQUEST_CODE_SCAN3 = 203;   // Scan3 액티비티 요청 상수
    public static final int REQUEST_CODE_SCAN1 = 202;   // Scan1 액티비티 요청 상수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan2);

        textView = findViewById(R.id.textView);

        // 이전 액티비티에서 조난자 정보 가져오기
        Intent intent = getIntent();
        getIntentData(intent);

        String data = scanData.substring(34);
        String dataLenCheck = scanData.substring(39, 40);
        if(!isNumeric(dataLenCheck)){
            num = scanData.substring(34, 35);   // 조난인원
            time = scanData.substring(35, 36);   // 조난시간
            sick = scanData.substring(36, 37);   // 현재상태
            battery = scanData.substring(37, 39);   // 배터리 잔량
        }
        else{
            num = scanData.substring(34, 36);   // 조난인원
            time = scanData.substring(36, 37);   // 조난시간
            sick = scanData.substring(37, 38);   // 현재상태
            battery = scanData.substring(38, 40);   // 배터리 잔량
        }
        textView.append("조난인원 : " + num);
        if(time.equals("0")){
            textView.append("\n조난시간 : 1시간");
        }
        else if(time.equals("1")){
            textView.append("\n조난시간 : 2시간");
        }
        else if(time.equals("2")){
            textView.append("\n조난시간 : 4시간");
        }
        else if(time.equals("3")){
            textView.append("\n조난시간 : 8시간");
        }
        else if(time.equals("4")){
            textView.append("\n조난시간 : 12시간");
        }
        else if(time.equals("5")){
            textView.append("\n조난시간 : 24시간");
        }
        else if(time.equals("6")){
            textView.append("\n조난시간 : 48시간");
        }
        else if(time.equals("7")){
            textView.append("\n조난시간 : 48시간 이상");
        }
        if(sick.equals("0")){
            textView.append("\n현재상태 : 이상 없음");
        }
        else if(sick.equals("1")){
            textView.append("\n현재상태 : 골절");
        }
        else if(sick.equals("2")){
            textView.append("\n현재상태 : 출혈");
        }
        else if(sick.equals("3")){
            textView.append("\n현재상태 : 골절, 출혈");
        }
        else if(sick.equals("4")){
            textView.append("\n현재상태 : 염좌(삠)");
        }
        else if(sick.equals("5")){
            textView.append("\n현재상태 : 골절, 염좌(삠)");
        }
        else if(sick.equals("6")){
            textView.append("\n현재상태 : 출혈, 염좌(삠)");
        }
        else if(sick.equals("7")){
            textView.append("\n현재상태 : 골절, 출혈, 염좌(삠)");
        }
        if(battery.equals("00")){
            textView.append("\n배터리 : 100%");
        }
        else{
            textView.append("\n배터리 : " + battery + "%");
        }

        // 재탐색 버튼 클릭시
        Button scanBtn = (Button) findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Scan1.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN1);
            }
        });

        // 채팅 버튼 클릭시
        Button chatBtn = (Button) findViewById(R.id.chatBtn);
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Scan3.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN3);
            }
        });
    }

    // 이전 액티비티에서 조난자 정보 받아오는 함수
    private void getIntentData(Intent intent){
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            ScanInfo scanInfo = bundle.getParcelable("scan");
            if (intent != null) {
                final String str = scanInfo.data;
                scanData = str;
            }
        }
    }

    // 문자열이 숫자인지 판별
    public static boolean isNumeric(String s)
    {
        return s.matches("-?\\d+(\\.\\d+)?");
    }
}
