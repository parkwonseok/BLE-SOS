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

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "TestDataBase";
    private DbOpenHelper mDbOpenHelper;
    public String gender;
    private Cursor mCursor;
    String name;
    String birth;
    String contact;
    String disease;
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


        mDbOpenHelper = new DbOpenHelper(this);

        //내부 데이터베이스 만들기
        try {
            mDbOpenHelper.open();
            mCursor = null;
            //DB에 있는 모든 컬럼을 가져옴
            mCursor = mDbOpenHelper.getAllColumns();
            //컬럼의 갯수 확인
            Log.i(TAG, "Count = " + mCursor.getCount());

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
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

//테스트// 두번째 // 세번째
    }
}
