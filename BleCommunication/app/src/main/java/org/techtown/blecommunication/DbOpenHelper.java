package org.techtown.blecommunication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper {

    private static final String DATABASE_NAME = "addressbook.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DataBaseHelper mDBHelper;
    private Context mCtx;

    private class DataBaseHelper extends SQLiteOpenHelper {

        /**
         * 데이터베이스 헬퍼 생성자
         * @param context   context
         * @param name      Db Name
         * @param factory   CursorFactory
         * @param version   Db Version
         */
        public DataBaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        //최초 DB를 만들 때 한번만 호출
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DataBases.CreateDB._CREATE);
        }

        //버전이 업데이트 되었을 경우 DB를 다시 만들어주는 메소드
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //업데이트를 했는데 DB가 존재할 경우 onCreate를 다시 불러온다
            db.execSQL("DROP TABLE IF EXISTS " + DataBases.CreateDB._TABLENAME);
            onCreate(db);
        }
    }

    //DbOpenHelper 생성자
    public DbOpenHelper(Context context) {
        this.mCtx = context;
    }

    //Db를 여는 메소드
    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DataBaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    //Db를 다 사용한 후 닫는 메소드
    public void close() {
        mDB.close();
    }

    /**
     *  데이터베이스에 사용자가 입력한 값을 insert하는 메소드
     * @param name          이름
     * @param contact       전화번호
     * @param gender
     * @param birth
     * @param disease
     * @return              SQLiteDataBase에 입력한 값을 insert
     */
    public long insertColumn(String name, String gender, String birth, String contact, String disease) {
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.GENDER, gender);
        values.put(DataBases.CreateDB.BIRTH, birth);
        values.put(DataBases.CreateDB.CONTACT, contact);
        values.put(DataBases.CreateDB.DISEASE, disease);
        return mDB.insert(DataBases.CreateDB._TABLENAME, null, values);
    }

    /**
     * 기존 데이터베이스에 사용자가 변경할 값을 입력하면 값이 변경됨(업데이트)
     * @param id            데이터베이스 아이디
     * @param name          이름
     * @param contact       전화번호
     * @param birth
     * @param disease
     * @param gender
     * @return              SQLiteDataBase에 입력한 값을 update
     */
    public boolean updateColumn(long id, String name, String gender, String birth, String contact, String disease) {
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.GENDER, gender);
        values.put(DataBases.CreateDB.BIRTH, birth);
        values.put(DataBases.CreateDB.CONTACT, contact);
        values.put(DataBases.CreateDB.DISEASE, disease);
        return mDB.update(DataBases.CreateDB._TABLENAME, values, "_id="+id, null) > 0;
    }

    //입력한 id값을 가진 DB를 지우는 메소드
    public boolean deleteColumn(long id) {
        return mDB.delete(DataBases.CreateDB._TABLENAME, "_id=" + id, null) > 0;
    }

    //입력한 전화번호 값을 가진 DB를 지우는 메소드
    public boolean deleteColumn(String number) {
        return mDB.delete(DataBases.CreateDB._TABLENAME, "contact="+number, null) > 0;
    }

    //커서 전체를 선택하는 메소드
    public Cursor getAllColumns() {
        return mDB.query(DataBases.CreateDB._TABLENAME, null, null, null, null, null, null);
    }

    //ID 컬럼 얻어오기
    public Cursor getColumn(long id) {
        Cursor c = mDB.query(DataBases.CreateDB._TABLENAME, null,
                "_id="+id, null, null, null, null);
        //받아온 컬럼이 null이 아니고 0번째가 아닐경우 제일 처음으로 보냄
        if (c != null && c.getCount() != 0)
            c.moveToFirst();
        return c;
    }

    //이름으로 검색하기 (rawQuery)
    public Cursor getMatchName(String name) {
        Cursor c = mDB.rawQuery( "Select * from address where name" + "'" + name + "'", null);
        return c;
    }
}
