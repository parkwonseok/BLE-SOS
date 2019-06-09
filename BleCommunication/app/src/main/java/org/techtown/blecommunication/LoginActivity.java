package org.techtown.blecommunication;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class  LoginActivity extends AppCompatActivity {
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
    //서버로부터 받아오는 fire base id값
    int fire_init;
    UserInfo users;


    //내정보 수정하기 버튼 눌러올때(초기값 false)
    boolean checkingdata;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private DatabaseReference mCounterReference;
    @Override
    protected void onStart() {
        //firebase 연결
        super.onStart();
        mFirebaseInstance = FirebaseDatabase.getInstance();

        //referencce 초기 설정("DataUsers/")
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");

        mCounterReference = mFirebaseDatabase.child("Users");

        mCounterReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //sos_id
                if(fire_id == 0){
                    fire_init = Integer.parseInt(String.valueOf(dataSnapshot.getChildrenCount()));
                    Log.d("fire_init", String.valueOf(fire_init));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });




    }
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


        mDbOpenHelper = new DbOpenHelper(this);


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
                Intent intent = getIntent();
                //만약 editing을 하지 않았다면 null값으로 넘어옴 ==> 에러발생
                try {

                    checkingdata = intent.getExtras().getBoolean("editing");
                    Log.d("checkingdata", String.valueOf(checkingdata));

                    if(checkingdata){
                        name = intent.getExtras().getString("name");
                        gender = intent.getExtras().getString("gender");
                        birth = intent.getExtras().getString("birth");
                        contact = intent.getExtras().getString("contact");
                        disease = intent.getExtras().getString("disease");
                        fire_id = intent.getExtras().getInt("fire_id");
                        Log.d(TAG, "기존 fire_id 값 : "+ fire_id);

                        //counter값에 기존 데이터 저장하기

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
                    else{
                        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent1);
                    }
                }
                catch(NullPointerException e){
                    Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent1);
                }
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
                if(fire_id == 0){
                    Log.d(TAG, "내정보 수정했져욤");
                    addUser(fire_init+1, name, gender, birth, contact, disease);
                    mDbOpenHelper.insertColumn(
                            String.valueOf(fire_init+1),
                            name,
                            gender,
                            birth,
                            contact,
                            disease
                    );

                }
                else{
                    Log.d(TAG, "처음 가입했져욤");
                    addUser(fire_id, name, gender, birth, contact, disease);
                    //              내장 DB 저장
                    mDbOpenHelper.insertColumn(
                            String.valueOf(fire_id),
                            name,
                            gender,
                            birth,
                            contact,
                            disease
                    );
                }


                mDbOpenHelper.close();
                //firebase 등록
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

//firebase 연동

    //신규회원가입할때
    public void addUser(int fire_id, String username, String usergender, String userbirth, String usercontact, String userdisease){
        users = new UserInfo(fire_id, username, usergender, userbirth, usercontact, userdisease);
        mFirebaseDatabase.child("Users").child(String.valueOf(fire_id)).setValue(users);
    }





}
