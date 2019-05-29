package org.techtown.blecommunication;

import android.util.Log;

public class SOSInfo {
    //구조자 id
    public int helper_id;
    //조난자 id
    public int sos_id;
    //조난자와 구조자 거리
    public double distance;
    //경도
    public double longitude;
    //위도
    public double latitude;

    SOSInfo(int helper_id, int sos_id, double distance, double longitude, double latitude){
        this.helper_id = helper_id;
        this.sos_id = sos_id;
        this.distance = distance;
        this.longitude = longitude;
        this.latitude = latitude;
        Log.d("블루투스로 확인된 내용", "구조자 블루투스 검색 목록 추가 완료");
    }

    SOSInfo(int sos_id, double longitude, double latitude){
        this.sos_id = sos_id;
        this.longitude = longitude;
        this.latitude = latitude;
        Log.d("추가된 내용", "조난자 삼각측량 완료!");
    }

    public int getHelper_id() {
        return helper_id;
    }

    public void setHelper_id(int helper_id) {
        this.helper_id = helper_id;
    }

    public int getSos_id() {
        return sos_id;
    }

    public void setSos_id(int sos_id) {
        this.sos_id = sos_id;
    }

    public double getDistance() {
        return distance/1000;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
