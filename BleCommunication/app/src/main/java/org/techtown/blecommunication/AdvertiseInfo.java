package org.techtown.blecommunication;

import android.bluetooth.le.AdvertiseData;
import android.os.Parcel;
import android.os.Parcelable;

public class AdvertiseInfo implements Parcelable {
    String num;
    String time;
    String sick;
    String battery;

    public AdvertiseInfo(String num_, String time_, String sick_, String battery_){
        num = num_;
        time = time_;
        sick = sick_;
        battery = battery_;
    }

    public AdvertiseInfo(Parcel src) {
        num = src.readString();
        time = src.readString();
        sick = src.readString();
        battery = src.readString();
    }

    public static final Creator<AdvertiseInfo> CREATOR = new Creator<AdvertiseInfo>() {
        @Override
        public AdvertiseInfo createFromParcel(Parcel in) {
            return new AdvertiseInfo(in);
        }

        @Override
        public AdvertiseInfo[] newArray(int size) {
            return new AdvertiseInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(num);
        dest.writeString(time);
        dest.writeString(sick);
        dest.writeString(battery);
    }
}
