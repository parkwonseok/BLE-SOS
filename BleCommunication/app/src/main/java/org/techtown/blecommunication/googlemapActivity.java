package org.techtown.blecommunication;

import android.app.FragmentManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Random;

public class googlemapActivity extends AppCompatActivity implements OnMapReadyCallback {

    //구글맵
    private static GoogleMap googleMap;
//    private static PolylineOptions polylineOptions;
//    private static ArrayList<LatLng> arraySOS;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private DatabaseReference mSOSRef;

    int helper_id;
    int sos_id;
    double distance;
    double longitude;
    double latitude;
    ArrayList<SOSInfo> sosInfos = new ArrayList<SOSInfo>();
    SOSInfo[] finder = new SOSInfo[3];
    SOSInfo sosInfo;
    Button find_btn;
    int counter;

    public static String[] mColors = {
            "#3079ab", // dark blue
            "#c25975", // mauve
            "#e15258", // red
            "#f9845b", // orange
            "#838cc7", // lavender
            "#7d669e", // purple
            "#53bbb4", // aqua
            "#51b46d", // green
            "#e0ab18", // mustard
            "#637a91", // dark gray
            "#f092b0", // pink
            "#b7c0c7"  // light gray
    };
    static String color = "";
    static int colorAsInt;
    @Override
    protected void onStart() {

        super.onStart();
        sos_checking();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlemap);

        find_btn = findViewById(R.id.find_btn);
        find_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(SOSInfo sosinfo : sosInfos){
                    Log.d("배열에 저장된값들: ", ""+sosinfo.helper_id+sosinfo.sos_id);
                }
                googleMap.clear();
                find_sos(sosInfos);
            }
        });

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void sos_checking(){

        //firebase 연결
        mFirebaseInstance = FirebaseDatabase.getInstance();

        //referencce 초기 설정("DataUsers/")
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");

        //참조값
        mSOSRef = mFirebaseDatabase.child("sos_info");
        mSOSRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot sos : dataSnapshot.getChildren()) {
                    //sos_id
                    sos_id = Integer.parseInt(sos.getKey());
                    Log.d("sos_id", sos.getKey());
                    get_helper_id(sos.getKey(), mSOSRef);
                    Log.d("count", String.valueOf(sos.getChildrenCount()));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

    }

    public void get_helper_id(final String sos_id1, DatabaseReference Ref){

        DatabaseReference Helper_id_Ref = Ref.child(sos_id1);
        Helper_id_Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot sos : dataSnapshot.getChildren()){
                    helper_id = Integer.parseInt(sos.getKey());
                    Log.d("sos_id/helper_id", sos_id1 +"/"+sos.getKey());
                    Log.d("해당하는 조난자 갯수", String.valueOf(sos.getChildrenCount()));
                    distance = sos.child("distance").getValue(double.class);
                    latitude = sos.child("latitude").getValue(double.class);
                    longitude = sos.child("longitude").getValue(double.class);
                    Log.d("values : ", "distanc:"+ distance+"latitude:"+latitude+"longitude"+longitude);
                    sosInfo = new SOSInfo(helper_id, Integer.parseInt(sos_id1), distance, longitude, latitude);
                    sosInfos.add(sosInfo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void find_sos(ArrayList<SOSInfo> sosInfos){
        counter = 0;
        int check_id = -1;
        for(SOSInfo sosinfo: sosInfos){
            //처음 시작부분
            if(check_id == -1){
                check_id = sosinfo.sos_id;
                Log.d("check_id", check_id+"");
                finder[counter] = sosinfo;
                counter++;
            }

            else if(check_id == sosinfo.sos_id){
                finder[counter] = sosinfo;
                Log.d("check_id", check_id+"");
                counter++;
            }
            else{
                Log.d("check_id", check_id+"");
                counter = 1;
                check_id = sosinfo.sos_id;
                finder[0] = sosinfo;
            }
            if(counter==3){
                Log.d("finder[0~2]",finder[0].helper_id+"/"+finder[1].helper_id+"/"+finder[2].helper_id);
                MarkerOptions makerOptions = new MarkerOptions();
                Compute(finder[0],finder[1],finder[2]);
                check_id = -1;
                counter =0;
            }
        }
    }
    public static double[] Compute(SOSInfo p1,SOSInfo p2,SOSInfo p3){
        double[] a=new double[3];
        double[] b=new double[3];
        double c,d,f,g,h;
        double[] i=new double[2];
        double k;

        c=p2.getLatitude()-p1.getLatitude();
        d=p2.getLongitude()-p1.getLongitude();
        f=(180/Math.PI)*Math.acos(Math.abs(c)/Math.abs(Math.sqrt(Math.pow(c,2)+Math.pow(d,2))));

        if((c>0&&d>0))
        {
            f=360-f;
        }

        else if((c<0&&d>0))
        {
            f=180+f;
        }

        else if((c<0&&d<0))
        {
            f=180-f;
        }

        a=C(c,d,B(A(D(p2.getDistance()))),f);
        b=C(p3.getLatitude()-p1.getLatitude(),p3.getLongitude()-p1.getLongitude(),B(A(D(p3.getDistance()))),f);
        g=(Math.pow(B(A(D(p1.getDistance()))),2)-Math.pow(a[2],2)+Math.pow(a[0],2))/(2*a[0]);
        h=(Math.pow(B(A(D(p1.getDistance()))),2)-Math.pow(b[2],2)-Math.pow(g,2)+Math.pow(g-b[0],2)+Math.pow(b[1],2))/(2*b[1]);
        i=C(g,h,0,-f);
        i[0]=i[0]+p1.getLatitude();
        i[1]=i[1]+p1.getLongitude();
        k=E(i[0],i[1],p1.getLatitude(),p1.getLongitude());
        Log.d("latitude/longitude", i[0]+"/"+i[1]);
        //오차에 대한 생략
//        if(k>p1.getDistance()*2){i=null;}else{
//            if(i[0]<-90||i[0]>90||i[1]<-180||i[1]>180){i=null;}}
        Log.d("k/p1.getDistance()*2", k+"/"+p1.getDistance()*2);


        //조난신호 마커생성
        MarkerOptions makerOptions = new MarkerOptions();
        makerOptions // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                .position(new LatLng(i[0], i[1]))
                .title("조난 신호");// 타이틀.
        googleMap.addMarker(makerOptions);

        Random randomGenerator = new Random(); // Construct a new Random number generator
        int randomNumber = randomGenerator.nextInt(mColors.length);

        color = mColors[randomNumber];
        colorAsInt = getColorWithAlpha(Color.parseColor(color), 0.2f);
        addHelper_marker(p1);
        addHelper_marker(p2);
        addHelper_marker(p3);
        // 2. 마커 생성 (마커를 나타냄)

        return i;
    }
    private static double A(double a){return a*7.2;}
    private static double B(double a){return a/900000;}
    private static double[] C(double a,double b,double c,double d){return new double[]{a*Math.cos((Math.PI/180)*d)-b*Math.sin((Math.PI/180)*d),a*Math.sin((Math.PI/180)*d)+b*Math.cos((Math.PI/180)*d),c};}
    private static double D(double a){return 730.24198315+52.33325511*a+1.35152407*Math.pow(a,2)+0.01481265*Math.pow(a,3)+0.00005900*Math.pow(a,4)+0.00541703*180;}
    private static double E(double a,double b,double c,double d){double e=Math.PI,f=e*a/180,g=e*c/180,h=b-d,i=e*h/180,j=Math.sin(f)*Math.sin(g)+Math.cos(f)*Math.cos(g)*Math.cos(i);if(j>1){j=1;}j=Math.acos(j);j=j*180/e;j=j*60*1.1515;j=j*1.609344;return j;}

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng Dongguk = new LatLng(37.558384, 127.000139);
        googleMap = map;
        map.moveCamera(CameraUpdateFactory.newLatLng(Dongguk));
        map.animateCamera(CameraUpdateFactory.zoomTo(16));
    }

    public static void addHelper_marker(SOSInfo p){

        googleMap.addCircle(new CircleOptions()
                .center(new LatLng(p.getLatitude(), p.getLongitude()))
                .radius(p.getDistance()*2000)
                .strokeColor(Color.GREEN)
                .strokeWidth(0)
                .fillColor(getColorWithAlpha(colorAsInt, 0.7f))
                .strokeColor(5)
        );
    }
    public static int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }

    public static void fake(SOSInfo p1,SOSInfo p2,SOSInfo p3){
        double[] i=new double[2];
        double sum = p1.distance+p2.distance+p3.distance;



        MarkerOptions makerOptions = new MarkerOptions();
        makerOptions // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                .position(new LatLng(i[0], i[1]))
                .title("조난 신호");// 타이틀.
        googleMap.addMarker(makerOptions);

        Random randomGenerator = new Random(); // Construct a new Random number generator
        int randomNumber = randomGenerator.nextInt(mColors.length);

        color = mColors[randomNumber];
        colorAsInt = getColorWithAlpha(Color.parseColor(color), 0.2f);
        addHelper_marker(p1);
        addHelper_marker(p2);
        addHelper_marker(p3);
    }


}
