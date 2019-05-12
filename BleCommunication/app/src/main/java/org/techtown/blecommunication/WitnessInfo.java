package org.techtown.blecommunication;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WitnessInfo {
    int user_id;
    String content;
    String created_at;
    String updated_at;

    public WitnessInfo(int user_id, String content){
        Calendar calendar = Calendar.getInstance();
        java.util.Date date = calendar.getTime();
        this.user_id = user_id;
        this.content = content;
        created_at = (new SimpleDateFormat("yyyyMMddHHmmss").format(date));
        updated_at = (new SimpleDateFormat("yyyyMMddHHmmss").format(date));
    }
}
