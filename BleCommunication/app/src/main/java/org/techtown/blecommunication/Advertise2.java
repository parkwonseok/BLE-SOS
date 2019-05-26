package org.techtown.blecommunication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;

public class Advertise2 extends AppCompatActivity {
    TextView textView;
    Set pairDevices1;
    Set pairDevices2;
    int size;
    String serviceData;   // 패킷에 넣을 데이터(조난자 정보)
    String bleUuid = "CDB7950D-73F1-4D4D-8E47-C090502DBD63";   // 패킷 id
    public static final int REQUEST_CODE_ADVERTISE1 = 102;   // Advertise1 액티비티 요청 상수
    public static final int REQUEST_CODE_ADVERTISE3 = 103;   // Advertise1 액티비티 요청 상수

    // Advertise 함수들
    BluetoothManager bleManager;
    BluetoothAdapter bleAdapter;
    BluetoothLeAdvertiser bleAdvertiser;
    AdvertiseSettings bleAdvertiseSettings;   // 주기, 송신신호세기, 연결플래그 설정
    AdvertiseData bleAdvertiseData;   // 패킷에 넣을 정보(uuid, 조난자정보, 송신신호세기, 디바이스이름)
    AdvertiseCallback bleAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {   // Advertise 성공 시
            super.onStartSuccess(settingsInEffect);
            Log.v("Tag","Success to start advertise: " + settingsInEffect.toString());
        }

        @Override
        public void onStartFailure(int errorCode) {   // Advertise 실패 시
            super.onStartFailure(errorCode);
            textView.setText("송신에 실패하였습니다.\n조난자 정보를 예시에 맞게 다시 작성해주세요.");
            textView.invalidate();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise2);

        textView = findViewById(R.id.textView);
        bleManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bleAdapter = bleManager.getAdapter();
        bleAdvertiser = bleAdapter.getBluetoothLeAdvertiser();

        pairDevices1 = bleAdapter.getBondedDevices();
        size = pairDevices1.size();

        // 이전 액티비티에서 조난자 정보 가져오기
        Intent intent = getIntent();
        getIntentData(intent);

        bleAdapter.setName(serviceData);
        // Advertising 시작
        startAdvertising();

        // 조난신호 중지 버튼 클릭시
        Button stopBtn = (Button) findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleAdvertiser.stopAdvertising(bleAdvertiseCallback);
                Intent intent = new Intent(getApplicationContext(), Advertise1.class);
                startActivityForResult(intent, REQUEST_CODE_ADVERTISE1);
            }
        });

        // 페어링되면 바로 채팅창 연결
        // 백그라운드 스레드로 페어링 됬는지 계속 검사 -> 페어링되면 채팅창 연결 -> 스레드 종료
        BackThread thread = new BackThread();
        thread.setDaemon(true);
        thread.start();
    }

    class BackThread extends Thread{
        @Override
        public void run() {
            while(true){
                handler.sendEmptyMessage(0);
                pairDevices2 = bleAdapter.getBondedDevices();
                if (pairDevices2.size() > size) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){
                pairDevices2 = bleAdapter.getBondedDevices();
                if (pairDevices2.size() > size) {
                    bleAdvertiser.stopAdvertising(bleAdvertiseCallback);
                    Intent intent1 = new Intent(getApplicationContext(), Advertise3.class);
                    startActivityForResult(intent1, REQUEST_CODE_ADVERTISE3);
                }
            }
        }
    };

    // 이전 액티비티에서 조난자 정보 받아오는 함수
    private void getIntentData(Intent intent){
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            AdvertiseInfo advertiseInfo = bundle.getParcelable("Data");   // 이전 액티비티에서의 조난자 정보를 합쳐 문자열 변수에 저장
            if (intent != null) {
                final String str = "ID0000" + advertiseInfo.num + advertiseInfo.time + advertiseInfo.sick + advertiseInfo.battery;
                serviceData = str;
            }
        }
    }

    // Advertising
    public void startAdvertising(){
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(bleUuid));   // 패킷 id

        // Advertising 신호 주기, 세기 설정
        bleAdvertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)   // 신호 주기 설정
                // ADVERTISE_MODE_LOW_POWER : 1초에 1번
                // ADVERTISE_MODE_BALANCED : 1초에 3번
                // ADVERTISE_MODE_LOW_LATENCY : 1초에 10번
                .setConnectable(true)   // 블루투스 페어링 필요시 True
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)   // 신호 세기 설정
                // ADVERTISE_TX_POWER_ULTRA_LOW
                // ADVERTISE_TX_POWER_LOW
                // ADVERTISE_TX_POWER_MEDIUM
                // ADVERTISE_TX_POWER_HIGH
                .build();

        // Advertising 패킷에 넣을 데이터
        bleAdvertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)   // 디바이스 이름 넣을 경우 : true
                .setIncludeTxPowerLevel(false)   // 신호 세기 넣을 경우 : true
                //.addServiceUuid(pUuid)   // id
                //.addServiceData(pUuid, serviceData.getBytes(Charset.forName("UTF-8")))   // 조난자 정보 넣기
                .build();

        bleAdvertiser.startAdvertising(bleAdvertiseSettings, bleAdvertiseData, bleAdvertiseCallback);   // advertise 시작
    }
}