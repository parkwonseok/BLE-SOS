package org.techtown.blecommunication;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RssiSearchInfo {
    int user_id;
    double distance;
    int rescue_id;
    double longitude, latitude;
    String created_at;
    String updated_at;

    public RssiSearchInfo(int user_id, double distance, int rescue_id, double longitude, double latitude){
        Calendar calendar = Calendar.getInstance();
        java.util.Date date = calendar.getTime();
        this.user_id = user_id;
        this.distance = distance;
        this.rescue_id = rescue_id;
        this.longitude = longitude;
        this.latitude = latitude;
        created_at = (new SimpleDateFormat("yyyyMMddHHmmss").format(date));
        updated_at = (new SimpleDateFormat("yyyyMMddHHmmss").format(date));

    }
}
