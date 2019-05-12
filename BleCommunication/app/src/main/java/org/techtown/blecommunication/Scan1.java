package org.techtown.blecommunication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Scan1 extends AppCompatActivity {
    public static final int REQUEST_SCAN2_ACTIVITY = 202;   // Scan2 액티비티 요청 상수
    // Scan 준비
    BluetoothManager bleManager;
    BluetoothAdapter bleAdapter;
    BluetoothLeScanner bleScanner;
    BluetoothDevice device;
    ArrayList<BluetoothDevice> bleDevices;
    ScanSettings.Builder mScanSettings;
    List<ScanFilter> scanFilters;

    // 리스트뷰 준비
    ArrayList<HashMap<String,String>> listDevice;
    HashMap<String,String> inputData;
    ListView listView;
    SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan1);

        bleManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bleManager.getAdapter();
        bleScanner = bleAdapter.getBluetoothLeScanner();

        listView = (ListView)findViewById(R.id.listDevice);
        listDevice = new ArrayList<HashMap<String, String>>();
        bleDevices = new ArrayList<>();

        // 심플 리스트뷰 어댑터
        simpleAdapter = new SimpleAdapter(
                getApplicationContext(),
                listDevice,
                android.R.layout.simple_list_item_2,
                new String[]{"data", "distance"},
                new int[]{android.R.id.text1,android.R.id.text2});
        listView.setAdapter(simpleAdapter);

        // Scan 시작
        startScanning();

        // 리스트뷰 목록에서 탐색된 장치 클릭시
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                device = bleDevices.get(position);
                device.createBond();   // 조난신호 보낸 장치와 페어링
                stopScaning();   // Scan 중지

                // Scan2 액티비티로 전환
                Intent intent = new Intent(getApplicationContext(), Scan2.class);
                // Scan2 액티비티로 조난자 정보 넘겨줌
                String scanData = listDevice.get(position).toString();
                ScanInfo scanInfo = new ScanInfo(scanData);
                intent.putExtra("scan", scanInfo);
                startActivityForResult(intent, REQUEST_SCAN2_ACTIVITY);
            }
        });
    }

    // Scan 결과 반환 함수
    private ScanCallback bleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            // 패킷에 담긴 조난자 정보
            String str = new String(result.getScanRecord().getBytes(), Charset.forName("UTF-8"));
            String serviceData = str.substring(25);
            int rssi = result.getRssi();
            double distance = getDistance(-56, rssi);
            //device = result.getDevice();   // 조난신호를 보낸 장치

            inputData = new HashMap<String, String>();
            inputData.put("data", "패킷데이터 : " + serviceData);
            inputData.put("distance", "거리 : " + Double.toString(distance).substring(0, 4));

            if(listDevice.size() == 0){
                bleDevices.add(result.getDevice());
                listDevice.add(inputData);
            }
            for(int i= 0 ; i<listDevice.size(); i++){

                if(listDevice.get(i).get("data").equals(inputData.get("data"))){
                    listDevice.set(i, inputData);
                    break;
                }
                else{
                    bleDevices.add(result.getDevice());
                    listDevice.add(inputData);
                }
            }
            // 리스트뷰 갱신
            simpleAdapter.notifyDataSetChanged();
        }
    };

    public void startScanning() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mScanSettings = new ScanSettings.Builder();
                mScanSettings.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
                ScanSettings scanSettings = mScanSettings.build();

                scanFilters = new Vector<>();
                ScanFilter.Builder scanFilter = new ScanFilter.Builder();
                scanFilter.setServiceUuid(ParcelUuid.fromString("CDB7950D-73F1-4D4D-8E47-C090502DBD63"));
                ScanFilter scan = scanFilter.build();
                scanFilters.add(scan);
                bleScanner.startScan(scanFilters, scanSettings, bleScanCallback);
            }
        });
    }

    public void stopScaning() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bleScanner.stopScan(bleScanCallback);
            }
        });
    }

    // 문자열이 숫자인지 판별
    public static boolean isNumeric(String s)
    {
        return s.matches("-?\\d+(\\.\\d+)?");
    }

    // rssi -> 거리
    protected static double getDistance(int txPower, double rssi){
        double d = Math.pow(10, (txPower-rssi)/(Math.pow(10, 1.6)));

        return d;
    }
}
