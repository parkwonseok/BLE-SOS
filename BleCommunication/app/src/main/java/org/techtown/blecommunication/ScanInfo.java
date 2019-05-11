package org.techtown.blecommunication;

import android.os.Parcel;
import android.os.Parcelable;

public class ScanInfo implements Parcelable {
    String data;

    public ScanInfo(String data_){
        data = data_;
    }

    public ScanInfo(Parcel src) {
        data = src.readString();
    }

    public static final Creator<ScanInfo> CREATOR = new Creator<ScanInfo>() {
        @Override
        public ScanInfo createFromParcel(Parcel in) {
            return new ScanInfo(in);
        }

        @Override
        public ScanInfo[] newArray(int size) {
            return new ScanInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data);
    }
}
