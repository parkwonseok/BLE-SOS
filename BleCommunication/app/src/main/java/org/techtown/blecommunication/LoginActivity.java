package org.techtown.blecommunication;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "TestDataBase";
    private DbOpenHelper mDbOpenHelper;
    public String gender;
    private Cursor mCursor;
    //유저정보
    String name;
    String birth;
    String contact;
    String disease;
    //사용자 firebase id
    int fire_id;

    UserInfo users;

    //firebase key값
    int counter = 0;
    String counter_set;

    //내정보 수정하기 버튼 눌러올때(초기값 false)
    boolean checkingdata = false;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private DatabaseReference mCounterReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText etName = findViewById(R.id.etName);
        final RadioButton radioMan = findViewById(R.id.radioMan);
        final RadioButton radioWoman = findViewById(R.id.radioWoman);
        final EditText etBirth = findViewById(R.id.etBirth);
        final EditText etContact = findViewById(R.id.etContact);
        final EditText etDisease = findViewById(R.id.etDisease);
        Button btSave = findViewById(R.id.btSave);
        Button btfire = findViewById(R.id.btfire);
        final TextView serverTv = findViewById(R.id.server_Tv);

        //firebase 연결
        mFirebaseInstance = FirebaseDatabase.getInstance();

        //referencce 초기 설정("DataUsers/")
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");
//        UserId = mFirebaseDatabase.child("Users").push().getKey();

        mDbOpenHelper = new DbOpenHelper(this);

        mCounterReference = mFirebaseDatabase.child("Users").child("counter");


        //내부 데이터베이스가 있을때
        try {
            mDbOpenHelper.open();
            mCursor = null;
            //DB에 있는 모든 컬럼을 가져옴
            mCursor = mDbOpenHelper.getAllColumns();
            //컬럼의 갯수 확인
            Log.i(TAG, "Count = " + mCursor.getCount());

            //내장에 데이터가 있을때
            if(mCursor.getCount()>0){

                //editing 초기값은 false
                Intent intent = getIntent();

                //만약 editing을 하지 않았다면 null값으로 넘어옴 ==> 에러발생
                checkingdata = intent.getExtras().getBoolean("editing");
                if(checkingdata){
                    name = intent.getExtras().getString("name");
                    gender = intent.getExtras().getString("gender");
                    birth = intent.getExtras().getString("birth");
                    contact = intent.getExtras().getString("contact");
                    disease = intent.getExtras().getString("disease");
                    fire_id = intent.getExtras().getInt("fire_id");
                    Log.d(TAG, "기존 fire_id 값 : "+ fire_id);

                    //counter값에 기존 데이터 저장하기
                    counter = fire_id;

                    etName.setText(name);
                    if(gender == "남자"){
                        radioMan.setChecked(true);
                    }
                    if(gender == "여자"){
                        radioWoman.setChecked(true);
                    }
                    etBirth.setText(birth);
                    etContact.setText(contact);
                    etDisease.setText(disease);
                }

                //

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //저장하기 버튼을 눌렀을때
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // edit 데이터 값 받아오기
                name = etName.getText().toString().trim();
                birth = etBirth.getText().toString().trim();
                contact = etContact.getText().toString().trim();
                disease = etDisease.getText().toString().trim();
                if(radioMan.isChecked()){
                    gender = "남자";
                }
                if(radioWoman.isChecked()){
                    gender = "여자";
                }

                //내 정보 수정하기 눌렀을 때
                if(checkingdata){
                    Log.d(TAG, "내정보 수정했져욤");
                    addUser(name, gender, birth, contact, disease);

                }
                //그냥 처음 저장하기 눌렀을때
                else{
                    Log.d(TAG, "처음 회원가입했어욤");
                    onCounter(mCounterReference);
                    try{
                        addUser(counter, name, gender, birth, contact, disease);
                    }
                    catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Counter is "+counter);
                    addUser(counter, name, gender, birth, contact, disease);
                    fire_id = counter;

                }
//                 내장 DB 저장
                mDbOpenHelper.insertColumn(
                        counter_set,
                        name,
                        gender,
                        birth,
                        contact,
                        disease
                );

                mDbOpenHelper.close();

                //firebase 등록
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        btfire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCounter(mCounterReference);
                serverTv.setText(""+counter);
            }
        });
    }

//firebase 연동

    //신규회원가입할때
    public void addUser(int fire_id, String username, String usergender, String userbirth, String usercontact, String userdisease){

        counter_set = String.valueOf(counter);
        users = new UserInfo(fire_id, username, usergender, userbirth, usercontact, userdisease);
        mCounterReference.setValue(counter+1);
        //예: "User/1/유저정보"
        mFirebaseDatabase.child("Users").child(counter_set).setValue(users);
    }

    //신규회원 저장할때
    public void addUser(String username, String usergender, String userbirth, String usercontact, String userdisease){

        counter_set = String.valueOf(counter);
        users = new UserInfo(counter, username, usergender, userbirth, usercontact, userdisease);
        mFirebaseDatabase.child("Users").child(counter_set).setValue(users);

    }

    private void onCounter(DatabaseReference counterRef) {
        counterRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                try{
                    if (currentValue == null) {
                        mutableData.setValue(1);
                    }
                    else{
                        Log.d(TAG, "counter 저장됨");
                        counter = currentValue;
                        mCounterReference.setValue(currentValue+1);
                    }

                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }

                return Transaction.success(mutableData);
            }
            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }





}
