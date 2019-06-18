package org.techtown.blecommunication;

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    ArrayList<HashMap<String, String>> listDevice;
    HashMap<String, String> inputData;
    ListView listView;
    SimpleAdapter simpleAdapter;

    // Map < 조난자id, 패킷데이터 >
    HashMap<String, String> sosData = new HashMap<>();
    // Map < 조난자id, rssi값 20개 >
    HashMap<String, Object> sosRssi = new HashMap<>();
    // Map < 조난자id, 거리 >
    HashMap<String, Double> sosDistance = new HashMap<>();

    int check = 0;
    String isSOS;

    ArrayList<String> SOS_id = new ArrayList<>();
    ArrayList<Double> SOS_distance = new ArrayList<>();


    Button senddt_bt;
    Button show_bt;

    //위치
    private GpsInfo gps;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    double latitude;
    double longitude;



    //firebase 전송을 위한 세팅
    private DbOpenHelper mDbOpenHelper; //내부 DB 관리
    private Cursor mCursor; // DB 관련
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    int fire_id;
    LocationManager lm;
    @Override
    protected void onStart() {
        super.onStart();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //firebase 연결
        mFirebaseInstance = FirebaseDatabase.getInstance();
        //referencce 초기 설정("DataUsers/")
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan1);

        senddt_bt = findViewById(R.id.sendDt);
        show_bt = findViewById(R.id.show);

        bleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bleManager.getAdapter();
        bleScanner = bleAdapter.getBluetoothLeScanner();

        listView = (ListView) findViewById(R.id.listDevice);
        listDevice = new ArrayList<HashMap<String, String>>();
        bleDevices = new ArrayList<>();

        // 심플 리스트뷰 어댑터
        simpleAdapter = new SimpleAdapter(
                getApplicationContext(),
                listDevice,
                android.R.layout.simple_list_item_2,
                new String[]{"data", "distance"},
                new int[]{android.R.id.text1, android.R.id.text2});
        listView.setAdapter(simpleAdapter);

        gps = new GpsInfo(Scan1.this);
        gps.getLocation();


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

        //조난자 전송버튼
        senddt_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                System.out.println(latitude + longitude);
                for (String key : sosDistance.keySet()) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    addSOSList(Integer.parseInt(key), sosDistance.get(key), latitude, longitude, sosData.get(key));
                }

            }
        });

        //조난자 위치버튼
        show_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), map_findActivity.class);
                startActivity(intent1);

            }
        });

        //위치 관련
        if (!isPermission) {
            callPermission();
            return;
        }


        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();


            Toast.makeText(
                    getApplicationContext(),
                    "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude,
                    Toast.LENGTH_LONG).show();
        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }



    }

    // Scan 결과 반환 함수
    private ScanCallback bleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            // 패킷에 담긴 조난자 정보
            int rssi = result.getRssi();
            int listDeviceSize = listDevice.size();
            String advData = result.getScanRecord().getDeviceName();
            Log.d("result", result.getScanRecord().toString());

            if (advData != null) {
                isSOS = advData.substring(0, 2);
            } else {
                isSOS = "no";
            }
            if (isSOS.equals("ID")) {
                // 삼각측량 부분
                String key = advData.substring(2, 6);
                ArrayList<Double> rssiList;

                if (sosRssi.containsKey(key)) {
                    rssiList = (ArrayList<Double>) sosRssi.get(key);
                    inputData = new HashMap<String, String>();
                    inputData.put("data", "패킷데이터 : " + advData);
                    inputData.put("distance", "<조난신호>     " + "rssi : " + String.valueOf(rssi) + "                  " + String.valueOf(rssiList.size())  + "  / 20");
                }
                else {
                    rssiList = new ArrayList<>();
                    inputData = new HashMap<String, String>();
                    inputData.put("data", "패킷데이터 : " + advData);
                    inputData.put("distance", "<조난신호>     " + "rssi : " + String.valueOf(rssi) + "                  " + " 1 / 20");
                }
                if (rssiList.size() < 20) {
                    rssiList.add((double) rssi);
                    sosRssi.put(key, rssiList);
                } else if (rssiList.size() == 20) {
                    sosDistance.put(key, getDistance(1.55, -56, kalman(rssiList, 50.0, 0.008)));
                    sosData.put(key, advData);
                }

            } else {
                if(isSOS.contains("n")){
//                    inputData = new HashMap<String, String>();
//                    inputData.put("data", "패킷데이터 : " + advData);
//                    inputData.put("distance", "<알 수 없는 신호>     " + "rssi : " + String.valueOf(rssi));
                }
                else{
                    inputData = new HashMap<String, String>();
                    inputData.put("data", "패킷데이터 : " + advData);
                    inputData.put("distance", "<알 수 없는 신호>     " + "rssi : " + String.valueOf(rssi));
                }

            }

            for (int i = 0; i < listDeviceSize; i++) {

                if (listDevice.get(i).get("data").equals(inputData.get("data"))) {

                    listDevice.set(i, inputData);
                    check = 1;
                    break;
                }
            }
            if (check == 0) {
                try{
                    if (inputData.get("data").equals("null")) {

                    } else {
                        bleDevices.add(result.getDevice());
                        listDevice.add(inputData);
                    }
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
//                if (inputData.get("data").equals("null")) {
//
//                } else {
//                    bleDevices.add(result.getDevice());
//                    listDevice.add(inputData);
//                }
            }
            check = 0;
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
                //bleScanner.startScan(scanFilters, scanSettings, bleScanCallback);
                bleScanner.startScan(bleScanCallback);
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
    public static boolean isNumeric(String s) {
        return s.matches("-?\\d+(\\.\\d+)?");
    }

    // rssi -> 거리
    protected static double getDistance(double n, int txPower, double rssi) {
        double d = Math.pow(10, (txPower - rssi) / (Math.pow(10, n)));

        return d;
    }

    /* Complete calculation of Kalman Filter */
    public static double kalman(ArrayList<Double> inputValues, double initialVariance, double noise) {
        return calculate(inputValues, initialVariance, noise);
    }

    /* Calculation of Kalman Filter using default values for wireless Access Points data acquisition */
    public static double kalman(ArrayList<Double> inputValues) {
        return calculate(inputValues, 50.0, 0.008);
    }

    /* Calculation of arithmetic mean */
    public static double mean(ArrayList<Double> inputValues) {
        return utilsMean(inputValues);
    }


    /*This method is the responsible for calculating the value refined with Kalman Filter */
    private static double calculate(ArrayList<Double> inputValues, double initialVariance, double noise) {
        double kalmanGain;
        double variance = initialVariance;
        double processNoise = noise;
        double measurementNoise = utilsVariance(inputValues);
        double mean = inputValues.get(0);

        for (double value : inputValues) {
            variance = variance + processNoise;
            kalmanGain = variance / ((variance + measurementNoise));
            mean = mean + kalmanGain * (value - mean);
            variance = variance - (kalmanGain * variance);
        }
        return mean;
    }

    public static double utilsVariance(ArrayList<Double> values) {
        double sum1 = 0.0;
        double mean = utilsMean(values);
        for (double num : values) {
            sum1 += Math.pow(num - mean, 2);
        }
        return sum1 / (values.size() - 1);
    }

    public static double utilsMean(ArrayList<Double> values) {
        return utilsSum(values) / values.size();
    }

    private static double utilsSum(ArrayList<Double> values) {
        double sum1 = 0.0;
        for (double num : values) {
            sum1 += num;
        }
        return sum1;
    }

    public void addSOSList(int SOS_id, double distance, double latitude, double longitude, String sosData) {
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mCursor = null;
        //DB에 있는 모든 컬럼을 가져옴
        mCursor = mDbOpenHelper.getAllColumns();
        mCursor.moveToLast();
        fire_id = mCursor.getInt(mCursor.getColumnIndex("fire_id"));
        Log.d("data_value", fire_id + "/" + distance + "/" + latitude + "/" + longitude);

        mFirebaseDatabase.child("sos_info").child(String.valueOf(SOS_id)).child(String.valueOf(fire_id)).child("distance").setValue(distance);

        mFirebaseDatabase.child("sos_info").child(String.valueOf(SOS_id)).child(String.valueOf(fire_id)).child("latitude").setValue(latitude);

        mFirebaseDatabase.child("sos_info").child(String.valueOf(SOS_id)).child(String.valueOf(fire_id)).child("longitude").setValue(longitude);

        mFirebaseDatabase.child("sos_info").child(String.valueOf(SOS_id)).child(String.valueOf(fire_id)).child("sos_content").setValue(sosData);
    }


    /*public void progress_bar(int value){
        try {

            // 변환된 값을 프로그레스바에 적용.
            ProgressBar progress = (ProgressBar) findViewById(R.id.progress) ;
            progress.setProgress(value) ;

        } catch (Exception e) {
            // 토스트(Toast) 메시지 표시.
            Toast toast = Toast.makeText(Scan1.this, "Invalid number format",
                    Toast.LENGTH_SHORT);
            toast.show();
        }

    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    // 전화번호 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }





}
