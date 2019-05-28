package org.techtown.blecommunication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WitnessInfo {
    int user_id;
    String content;
    String time;
    String created_at;
    String updated_at;
    String longitude;
    String latitude;

    public WitnessInfo(int user_id, String time, String content, double longitude, double latitude){
        Calendar calendar = Calendar.getInstance();
        java.util.Date date = calendar.getTime();
        this.user_id = user_id;
        this.content = content;
        this.time = time;
        this.longitude = String.valueOf(longitude);
        this.latitude = String.valueOf(latitude);
        created_at = (new SimpleDateFormat("yyyyMMddHHmmss").format(date));
        updated_at = (new SimpleDateFormat("yyyyMMddHHmmss").format(date));

    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
