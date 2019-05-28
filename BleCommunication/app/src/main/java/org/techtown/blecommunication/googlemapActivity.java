package org.techtown.blecommunication;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class googlemapActivity extends AppCompatActivity {

    private static ArrayList<SOSInfo> sosLocations = new ArrayList<SOSInfo>();
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
                find_sos(sosInfos);
            }
        });
    }

    public static Location getLocationWithTrilateration(SOSInfo A, SOSInfo B, SOSInfo C){

        SOSInfo find_location;
        double bAlat = A.getLatitude();
        double bAlong = A.getLongitude();
        double bBlat = B.getLatitude();
        double bBlong = B.getLongitude();
        double bClat = C.getLatitude();
        double bClong = C.getLongitude();
        double distanceA = Math.round(A.getDistance()*100)/100.0;
        double distanceB = Math.round(B.getDistance()*100)/100.0;
        double distanceC = Math.round(C.getDistance()*100)/100.0;

        //절대 좌표
        double Xlat = 0;
        double Xlong = 0;
        double Ylat = (Math.round((bAlat-bBlat)*1000000)*1.1*100000)/1000000.0;
        double Ylong = (Math.round((bAlong-bBlong)*1000000)*0.9*100000)/1000000.0;
        double Zlat = (Math.round((bAlat-bClat)*10000000)*1.1*100000)/1000000.0;
        double Zlong = (Math.round((bAlong-bClong)*1000000)*0.9*100000)/1000000.0;
        Log.d("절대 좌표값", Xlat+"/"+Xlong+"/"+Ylat+"/"+Ylong+"/"+Zlat+"/"+Zlong);

        bAlat = 0;
        bAlong = 0;
        bBlat = Ylat;
        bBlong = Ylong;
        bClat = Zlat;
        bClong = Zlong;
        double W, Z, foundBeaconLat, foundBeaconLong, foundBeaconLongFilter;
        W = distanceA * distanceA - distanceB * distanceB - bAlat * bAlat - bAlong * bAlong + bBlat * bBlat + bBlong * bBlong;
        Z = distanceB * distanceB - distanceC * distanceC - bBlat * bBlat - bBlong * bBlong + bClat * bClat + bClong * bClong;

        foundBeaconLat = (W * (bClong - bBlong) - Z * (bBlong - bAlong)) / (2 * ((bBlat - bAlat) * (bClong - bBlong) - (bClat - bBlat) * (bBlong - bAlong)));
        foundBeaconLong = (W - 2 * foundBeaconLat * (bBlat - bAlat)) / (2 * (bBlong - bAlong));
        //`foundBeaconLongFilter` is a second measure of `foundBeaconLong` to mitigate errors
        foundBeaconLongFilter = (Z - 2 * foundBeaconLat * (bClat - bBlat)) / (2 * (bClong - bBlong));

        foundBeaconLong = (foundBeaconLong + foundBeaconLongFilter) / 2;

        Location foundLocation = new Location("Location");
        foundLocation.setLatitude(foundBeaconLat);
        foundLocation.setLongitude(foundBeaconLong);
        Log.d("잘 찾았나요?","위도"+foundBeaconLat+"경도"+foundBeaconLong);
        find_location = new SOSInfo(0, A.sos_id, 0,foundBeaconLong, foundBeaconLat);
        sosLocations.add(find_location);
        return foundLocation;
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
                getLocationWithTrilateration(finder[0],finder[1],finder[2]);
                check_id = -1;
                counter =0;
            }
        }
    }

}
