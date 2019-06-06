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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    ArrayList<HashMap<String, String>> listDevice;
    HashMap<String, String> inputData;
    ListView listView;
    SimpleAdapter simpleAdapter;

    // Map < 조난자id, rssi값 20개 >
    HashMap<String, Object> sosRssi = new HashMap<>();
    // Map < 조난자id, 거리 >
    HashMap<String, Double> sosDistance = new HashMap<>();
    TextView testView;

    int check = 0;
    String isSOS;

    //location을 위한
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 5000;  // 5초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 2000; // 2초
    Location mCurrentLocatiion;
    LatLng currentPosition;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;
    String TAG = "testing";

    ArrayList<String> SOS_id = new ArrayList<>();
    ArrayList<Double> SOS_distance = new ArrayList<>();

    double longitude;
    double latitude;

    Button senddt_bt;
    Button show_bt;

    //firebase 전송을 위한 세팅
    private DbOpenHelper mDbOpenHelper; //내부 DB 관리
    private Cursor mCursor; // DB 관련
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    int fire_id;

    @Override
    protected void onStart() {
        super.onStart();
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //firebase 연결
        mFirebaseInstance = FirebaseDatabase.getInstance();
        //referencce 초기 설정("DataUsers/")
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");
//        if (Build.VERSION.SDK_INT >= 23 &&
//                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(Scan1.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    0);
//        } else {
//            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                    1000,
//                    1,
//                    gpsLocationListener);
//            String provider = location.getProvider();
//            longitude = location.getLongitude();
//            latitude = location.getLatitude();
//            Log.d("그치만,,,,", "위치정보 : " + provider + "\n" +
//                    "위도 : " + longitude + "\n" +
//                    "경도 : " + latitude + "\n");
//        }

        //위치 탐색

        locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
        testView = findViewById(R.id.testView);

        //조난자 전송버튼
        senddt_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Scan1.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            0);
                } else {
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            1000,
                            1,
                            gpsLocationListener);
                    String provider = location.getProvider();
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    Log.d("그치만,,,,", "위치정보 : " + provider + "\n" +
                            "위도 : " + longitude + "\n" +
                            "경도 : " + latitude + "\n");
                }

                for (String key : sosDistance.keySet()) {
                    addSOSList(Integer.parseInt(key), sosDistance.get(key), latitude, longitude);
                }

            }
        });

        //조난자 위치버튼
        show_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), googlemapActivity.class);
                startActivity(intent1);

            }
        });


        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                List<Location> locationList = locationResult.getLocations();

                if (locationList.size() > 0) {
                    location = locationList.get(locationList.size() - 1);
                    //location = locationList.get(0);

                    currentPosition
                            = new LatLng(location.getLatitude(), location.getLongitude());

                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
//                    String markerTitle = getCurrentAddress(currentPosition);
//                    String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
//                            + " 경도:" + String.valueOf(location.getLongitude();
//                    Log.d("happy", "onLocationResult : " + markerSnippet);


//                    //현재 위치에 마커 생성하고 이동
//                    setCurrentLocation(location, markerTitle, markerSnippet);

                    mCurrentLocatiion = location;
                }
            }
        };

    }

    // Scan 결과 반환 함수
    private ScanCallback bleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            // 패킷에 담긴 조난자 정보
            int rssi = result.getRssi();
            int listDeviceSize = listDevice.size();
            String advData = result.getScanRecord().getDeviceName();

            if (advData != null) {
                isSOS = advData.substring(0, 2);
            } else {
                isSOS = "no";
            }
            if (isSOS.equals("ID")) {
                inputData = new HashMap<String, String>();
                inputData.put("data", "패킷데이터 : " + advData);
                inputData.put("distance", "<조난신호>     " + "rssi : " + String.valueOf(rssi));

                // 삼각측량 부분
                String key = advData.substring(2, 6);
                ArrayList<Double> rssiList;

                if (sosRssi.containsKey(key))
                    rssiList = (ArrayList<Double>) sosRssi.get(key);
                else
                    rssiList = new ArrayList<>();
                if (rssiList.size() < 20) {
                    rssiList.add((double) rssi);
                    sosRssi.put(key, rssiList);
                } else if (rssiList.size() == 20) {
                    sosDistance.put(key, getDistance(1.55, -56, kalman(rssiList, 50.0, 0.008)));
                }

                testView.setText("RSSI 개수 : " + String.valueOf(rssiList.size()));

            } else {
                inputData = new HashMap<String, String>();
                inputData.put("data", "패킷데이터 : " + advData);
                inputData.put("distance", "<알 수 없는 신호>     " + "rssi : " + String.valueOf(rssi));
            }

            for (int i = 0; i < listDeviceSize; i++) {

                if (listDevice.get(i).get("data").equals(inputData.get("data"))) {

                    listDevice.set(i, inputData);
                    check = 1;
                    break;
                }
            }
            if (check == 0) {
                if (inputData.get("data").equals("null")) {

                } else {
                    bleDevices.add(result.getDevice());
                    listDevice.add(inputData);
                }
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

    public void addSOSList(int SOS_id, double distance, double latitude, double longitude) {
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

//        mFirebaseDatabase.child("sos_info").child(String.valueOf(SOS_id)).child(String.valueOf(fire_id)).child("sos_content").setValue(sos_content);
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            String provider = location.getProvider();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };


    //광규코드
    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        //LatLng surLocation = new LatLng();//조난자 위치(삼각층량으로 얻어야함)

//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(currentLatLng);
//        markerOptions.title(markerTitle);
//        markerOptions.snippet(markerSnippet);
//        markerOptions.draggable(true);
//
//        CircleOptions circle100m = new CircleOptions().center(currentLatLng)
//                .radius(100)//100m
//                .strokeWidth(0f)//선너비 0f : 선없음
//                .fillColor(Color.parseColor("#100000FF"));//배경색
//        mMap.addCircle(circle100m);
//
//
//        currentMarker = mMap.addMarker(markerOptions);
//
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
//        mMap.moveCamera(cameraUpdate);
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

    }

    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료.2 가지 경우

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용가능
//                Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
//                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View view) {
//
//                        finish();
//                    }
//                }).show();

                } else {


//                // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용가능
//                Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
//                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View view) {
//
//                        finish();
//                    }
//                }).show();
                }
            }

        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("조까", "onActivityResult : GPS 활성화 되있음");

                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }

    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);


            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        }

    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());


                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);


                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);

                mCurrentLocatiion = location;
            }


        }
    };
}
