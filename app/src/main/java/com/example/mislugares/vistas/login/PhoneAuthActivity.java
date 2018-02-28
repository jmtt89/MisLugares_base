package com.example.mislugares.vistas.login;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mislugares.R;
import com.hbb20.CountryCodePicker;

public class PhoneAuthActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int VALIDATE_REQUEST = 100;
    private static final String TAG = "PhoneAuthActivity";

    private View wrapper;
    private CountryCodePicker ccp;
    private EditText carrierNumber;
    private Button verifyPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        wrapper = findViewById(R.id.layout_wrapper);

        ccp = findViewById(R.id.ccp);
        carrierNumber = findViewById(R.id.edt_carrierNumber);

        ccp.registerCarrierNumberEditText(carrierNumber);

        verifyPhone = findViewById(R.id.btn_verify_phone);
        verifyPhone.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(ccp.isValidFullNumber()){
            Intent intent = new Intent(this, PhoneValidateActivity.class);
            intent.putExtra(PhoneValidateActivity.KEY_FULL_NUMBER, ccp.getFullNumberWithPlus());
            startActivityForResult(intent, VALIDATE_REQUEST);
        }else{
            Snackbar.make(wrapper, "El numero introducido no es valido", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == VALIDATE_REQUEST){
            if(resultCode == RESULT_OK){
                setResult(RESULT_OK, data);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }


}
