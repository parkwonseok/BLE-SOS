package org.techtown.blecommunication;

import android.provider.BaseColumns;

public class DataBases {
    //데이터베이스 호출 시 사용될 생성자
    public static final class CreateDB implements BaseColumns {
        public static final String NAME = "name";
        public static final String GENDER = "gender";
        public static final String BIRTH = "birth";
        public static final String CONTACT = "contact";
        public static final String DISEASE = "disease";
        public static final String FIRE_ID = "fire_id";
        public static final String _TABLENAME = "address";
        public static final String _CREATE =
                "create table " + _TABLENAME + "("
                        + _ID + " integer primary key autoincrement, "
                        + FIRE_ID + " text not null, "
                        + NAME + " text not null , "
                        + GENDER + " text not null , "
                        + BIRTH + " text not null , "
                        + CONTACT + " text not null ,"
                        + DISEASE + " text not null);";
    }

}
