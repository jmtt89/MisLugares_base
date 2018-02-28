package com.example.mislugares.vistas.login;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mislugares.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneValidateActivity extends AppCompatActivity {
    public static final String KEY_FULL_NUMBER = "fullNumber";
    public static final String KEY_AUTH_CREDENTIAL = "AuthCredential";
    private static final String TAG = "PhoneValidateActivity";
    private View wrapper;

    private TextView lblPhone;
    private EditText edtCode;
    private Button btnValidate;

    private Button btnResend;
    private String fullPhone;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_validate);

        if(getIntent() == null || getIntent().getExtras() == null || !getIntent().getExtras().containsKey(KEY_FULL_NUMBER)){
            Exception _e = new NullPointerException("This Activity require "+ KEY_FULL_NUMBER +" Extra key");
            Log.e(TAG, _e.getLocalizedMessage(), _e);
            Intent data = new Intent();
            data.putExtra("exception", _e);
            setResult(5);
            finish();
            return;
        }

        fullPhone = getIntent().getExtras().getString(KEY_FULL_NUMBER);

        mCallbacks = new PhoneAuthCallback();

        wrapper = findViewById(R.id.layout_wrapper);

        lblPhone = findViewById(R.id.lbl_phone_number);
        lblPhone.setText(fullPhone);

        edtCode = findViewById(R.id.edt_code);
        edtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 6){
                    btnValidate.setEnabled(true);
                }else{
                    btnValidate.setEnabled(false);
                }
            }
        });

        btnValidate = findViewById(R.id.btn_validate);
        btnValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = edtCode.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                Intent result = new Intent();
                result.putExtra(KEY_AUTH_CREDENTIAL, credential);
                setResult(RESULT_OK, result);
                finish();
            }
        });

        btnResend = findViewById(R.id.btn_resend);
        btnResend.postDelayed(new Runnable() {
            @Override
            public void run() {
                btnResend.setEnabled(true);
            }
        }, 60000);
        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode(resendingToken);
            }
        });

        sendVerificationCode(null);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void sendVerificationCode(PhoneAuthProvider.ForceResendingToken resendingToken){
        PhoneAuthProvider provider = PhoneAuthProvider.getInstance();
        if(resendingToken == null){
            provider.verifyPhoneNumber(fullPhone,60, TimeUnit.SECONDS,this, mCallbacks);
        }else{
            provider.verifyPhoneNumber(fullPhone,60, TimeUnit.SECONDS,this, mCallbacks, resendingToken);
        }
    }

    class PhoneAuthCallback extends PhoneAuthProvider.OnVerificationStateChangedCallbacks{
        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            Log.d(TAG, "onVerificationCompleted:" + credential);
            Intent intent = new Intent();
            intent.putExtra(KEY_AUTH_CREDENTIAL, credential);
            setResult(RESULT_OK, intent);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            if (e instanceof FirebaseTooManyRequestsException) {
                Log.e(TAG, "onVerificationFailed: The SMS quota for the project has been exceeded", e);
            }else{
                Log.e(TAG, "onVerificationFailed: "+ e.getLocalizedMessage(), e);
            }
            Snackbar.make(wrapper, e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String vId, PhoneAuthProvider.ForceResendingToken token) {
            Log.d(TAG, "onCodeSent:" + verificationId);
            verificationId = vId;
            resendingToken = token;
        }
    }
}
