package org.techtown.blecommunication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Scan2 extends AppCompatActivity {
    TextView textView;
    String scanData;
    String batLe;
    String batLen[] = new String[10];
    String battery;
    String id;
    String num;
    String time;
    String sick;

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

        if(android.os.Build.VERSION.SDK_INT >= 26) {
            String data = scanData.substring(16);
            id = data.substring(0, 4);
            num = data.substring(4, 5);
            time = data.substring(5, 16);
            sick = data.substring(16, 17);
            batLen = data.substring(17).split(",");
            battery = batLen[0];
        }
        else {
            String data = scanData.substring(48);
            id = data.substring(0, 4);
            num = data.substring(4, 5);
            time = data.substring(5, 16);
            sick = data.substring(16, 17);
            batLe = data.substring(17);
            Log.d("data", data);
            if (batLe.length() == 2) {
                battery = batLe.substring(0, 1);
            } else if (batLe.length() == 3) {
                battery = batLe.substring(0, 2);
            } else {
                battery = "100";
            }
        }
        textView.append("   -  회원 ID : " + id);
        textView.append("\n   -  조난인원 : " + num);
        textView.append("\n   -  조난일시 : " + time);
        if(sick.equals("0")){
            textView.append("\n   -  현재상태 : 이상 없음");
        }
        else if(sick.equals("1")){
            textView.append("\n   -  현재상태 : 골절");
        }
        else if(sick.equals("2")){
            textView.append("\n   -  현재상태 : 출혈");
        }
        else if(sick.equals("3")){
            textView.append("\n   -  현재상태 : 골절, 출혈");
        }
        else if(sick.equals("4")){
            textView.append("\n   -  현재상태 : 염좌(삠)");
        }
        else if(sick.equals("5")){
            textView.append("\n   -  현재상태 : 골절, 염좌(삠)");
        }
        else if(sick.equals("6")){
            textView.append("\n   -  현재상태 : 출혈, 염좌(삠)");
        }
        else if(sick.equals("7")){
            textView.append("\n   -  현재상태 : 골절, 출혈, 염좌(삠)");
        }
        textView.append("\n   -  배터리 : " + battery + " %");


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
