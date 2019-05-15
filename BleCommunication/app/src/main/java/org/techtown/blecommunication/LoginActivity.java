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
    String name;
    String birth;
    String contact;
    String disease;
    int id_;
    UserInfo users;


    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String UserId;

    public static final int REQUEST_CODE_LOGINCODE = 18;
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


        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");

        mDbOpenHelper = new DbOpenHelper(this);

        //내부 데이터베이스 만들기
        try {
            mDbOpenHelper.open();
            mCursor = null;
            //DB에 있는 모든 컬럼을 가져옴
            mCursor = mDbOpenHelper.getAllColumns();
            //컬럼의 갯수 확인
            Log.i(TAG, "Count = " + mCursor.getCount());
            if(mCursor.getCount()>0){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        try{
            Intent intent = getIntent();
            if(intent.getExtras().getBoolean("editing")){
                name = intent.getExtras().getString("name");
                gender = intent.getExtras().getString("gender");
                birth = intent.getExtras().getString("birth");
                contact = intent.getExtras().getString("contact");
                disease = intent.getExtras().getString("disease");
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

        } catch(Exception error){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            error.printStackTrace();
        }
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                mDbOpenHelper.insertColumn(
                        name,
                        gender,
                        birth,
                        contact,
                        disease
                );
                mDbOpenHelper.close();
                addUser(id_, name, gender, birth, contact, disease);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

//firebase 연동
    public void addUser(int id_, String username, String usergender, String userbirth, String usercontact, String userdisease){
        DatabaseReference newRef = mFirebaseDatabase.child("User_id");
        newRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(
                    DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                System.out.println("Transaction completed");
            }
        });
        users = new UserInfo(id_, username, usergender, userbirth, usercontact, userdisease);
        mFirebaseDatabase.child("Users").push().setValue(users);
    }

//
//
//    public void updateUser(String username, String email, String phone_number, int age, int disease, double longitude, double latitude, double altitude){
//        Calendar calendar = Calendar.getInstance();
//        java.util.Date date = calendar.getTime();
//        String update_at = (new SimpleDateFormat("yyyyMMddHHmmss").format(date));
//        DatabaseReference myUser = mFirebaseDatabase.child("Users").child(UserId);
//        myUser.child("username").setValue(username);
//        myUser.child("email").setValue(email);
//        myUser.child("age").setValue(age);
//        myUser.child("phone_number").setValue(phone_number);
//        myUser.child("disease").setValue(disease);
//        myUser.child("longitude").setValue(longitude);
//        myUser.child("latitude").setValue(latitude);
//        myUser.child("altitude").setValue(altitude);
//        myUser.child("updated_at").setValue(update_at);
//
//    }
//
//    public void insertData(View view){
//        addUser(user.getText().toString().trim(),
//                email.getText().toString().trim(),
//                phone_number.getText().toString().trim(),
//                Integer.parseInt(age.getText().toString()),
//                1,
//                longitude,
//                latitude,
//                altitude);
//    }
//
//    public void updateData(){
//        updateUser(user.getText().toString().trim(),
//                email.getText().toString().trim(),
//                phone_number.getText().toString().trim(),
//                Integer.parseInt(age.getText().toString()),
//                1,
//                longitude,
//                latitude,
//                altitude);
//    }
//
//    public void readDate(View view){
//        mFirebaseDatabase.addValueEventListener(new ValueEventListener(){
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
//                for(DataSnapshot ds: dataSnapshot.getChildren()){
//                    try{
//                        id_ = ds.child("User_id").getValue(Integer.class);
//                    }catch (NullPointerException e){
//                        e.printStackTrace();
//                    }
//
////                    String dbuser = ds.child("username").getValue(String.class);
////                    String dbmail = ds.child("email").getValue(String.class);
//                    Log.d("TAG", String.valueOf(id_)+"/");
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError){
//
//            }
//
//        });
//    }



}
