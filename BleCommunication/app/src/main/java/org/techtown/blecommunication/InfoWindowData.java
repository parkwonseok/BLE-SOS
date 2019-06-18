package org.techtown.blecommunication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InfoWindowData {
    private String fire_id; // fire_id
    private String sos_num; // 조난인원
    private String name; // 조난자 이름
    private String phone; // 조난자 핸드폰 번호
    private String disease; // 질병
    private String battery;
    private String date_sos;
    private double latitude;


    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private DatabaseReference mUserReference;

    public InfoWindowData(String fire_id, String sos_num, String name, String phone, String disease, String battery, String date_sos, double latitude, double longitude, String sos_condition) {

        mFirebaseInstance = FirebaseDatabase.getInstance();
        //referencce 초기 설정("DataUsers/")
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");
        mUserReference = mFirebaseDatabase.child("Users");
        this.fire_id = fire_id;
        this.sos_num = sos_num;
        this.name = name;
        this.phone = phone;
        this.disease = disease;
        this.battery = battery;
        this.date_sos = date_sos;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sos_condition = sos_condition;
    }

    private double longitude;
    private String sos_condition;


    public String getFire_id() {
        return fire_id;
    }

    public void setFire_id(String fire_id) {
        this.fire_id = fire_id;
    }

    public String getSos_num() {
        return sos_num;
    }

    public void setSos_num(String sos_num) {
        this.sos_num = sos_num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getDate_sos() {
        return date_sos;
    }

    public void setDate_sos(String date_sos) {
        this.date_sos = date_sos;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSos_condition() {
        return sos_condition;
    }

    public void setSos_condition(String sos_condition) {
        this.sos_condition = sos_condition;
    }


}
