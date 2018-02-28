package com.example.mislugares.vistas.profile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.mislugares.R;
import com.example.mislugares.vistas.login.PhoneValidateActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int RC_SIGN_IN = 36;
    private static final int REQUEST_UPDATE_PHONE = 69;
    private FirebaseAuth firebaseAuth;
    private Drawable transparent = new ColorDrawable(Color.parseColor("#00ffffff"));
    private Drawable undelineDrawable;

    private boolean editOn = false;

    private View wrapper;

    private ImageView imgPicture;
    private TextInputEditText edtDisplayName;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPhoneNumber;
    private Button btnChangePassword;

    private Button btnGoogleConnect;
    private Button btnFacebookConnect;
    private Button btnTwitterConnect;

    private FloatingActionButton fab;


    private CallbackManager callbackManager;
    private LoginButton facebookLoginButton;
    private TwitterLoginButton twitterLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scroll_activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();

        wrapper = findViewById(R.id.layout_wrapper);

        imgPicture = findViewById(R.id.profile_picture);
        edtDisplayName = findViewById(R.id.profile_displayName);
        edtEmail = findViewById(R.id.profile_email);
        edtPhoneNumber = findViewById(R.id.profile_phoneNumber);

        btnGoogleConnect = findViewById(R.id.connect_google);
        btnFacebookConnect = findViewById(R.id.connect_facebook);
        btnTwitterConnect = findViewById(R.id.connect_twitter);

        undelineDrawable = edtEmail.getBackground();


        btnChangePassword = findViewById(R.id.change_password);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user.getEmail() != null){
                    FirebaseAuth
                            .getInstance()
                            .sendPasswordResetEmail(user.getEmail())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "onComplete password reset");
                                    if(!task.isSuccessful() && task.getException() != null){
                                        Log.e(TAG, "onComplete: ", task.getException());
                                        Snackbar.make(wrapper, task.getException().getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                }else{
                    Snackbar.make(wrapper, "Necesitas un Email para cambiar tu Password", Snackbar.LENGTH_LONG).show();
                }
            }
        });


        btnGoogleConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String providerId = getProviderLink("google");
                if(providerId != null){
                    firebaseAuth.getCurrentUser().unlink(providerId);
                    btnGoogleConnect.setText(R.string.btn_label_connect);
                }else{
                    // Google
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build();
                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            }
        });


        callbackManager = CallbackManager.Factory.create();
        facebookLoginButton = findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions("email");
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "Facebook Authentication Canceled");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "Facebook Authentication Failure", exception);
            }
        });
        btnFacebookConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String providerId = getProviderLink("google");
                if(providerId != null){
                    firebaseAuth.getCurrentUser().unlink(providerId);
                    btnFacebookConnect.setText(R.string.btn_label_connect);
                }else{
                    facebookLoginButton.performClick();
                }
            }
        });


        // Twitter
        twitterLoginButton = findViewById(R.id.twitter_login_button);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.e(TAG, "failure: twitter", exception);
            }
        });
        btnTwitterConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String providerId = getProviderLink("google");
                if(providerId != null){
                    firebaseAuth.getCurrentUser().unlink(providerId);
                    btnTwitterConnect.setText(R.string.btn_label_connect);
                }else{
                    twitterLoginButton.performClick();
                }
            }
        });

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editOn = !editOn;
                toggleEditProfile(editOn);
                if(editOn){
                    fab.setImageResource(R.drawable.ic_check_white_24dp);
                }else{
                    fab.setImageResource(R.drawable.ic_mode_edit_24dp);
                    saveUser();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toggleEditProfile(true);
        setupUserProfile();
        toggleEditProfile(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK){
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        // Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed", e);
            }
        }else if(requestCode == REQUEST_UPDATE_PHONE){
            PhoneAuthCredential credential = data.getParcelableExtra(PhoneValidateActivity.KEY_AUTH_CREDENTIAL);
            addOrUpdatePhoneAuthCredential(credential);
        }else{
            // Facebook
            callbackManager.onActivityResult(requestCode, resultCode, data);

            // Twitter
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseAuth.getCurrentUser() == null || firebaseAuth.getCurrentUser().isAnonymous()){
            Log.e(TAG, "User is null or Anonymous", new Exception("User is null or Anonymous"));
            finish();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        linkAccount("Google", credential);
    }

    private void addOrUpdatePhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "addOrUpdatePhoneAuthCredential");
        firebaseAuth
                .getCurrentUser()
                .updatePhoneNumber(credential)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful() && task.getException() != null){
                            Log.e(TAG, "onComplete: ", task.getException());
                            edtPhoneNumber.setError(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        linkAccount("Facebook", credential);
    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        linkAccount("Twitter", credential);
    }

    private void linkAccount(final String provider, AuthCredential credential){
        firebaseAuth.getCurrentUser().linkWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        switch (provider){
                            case "Google":
                                btnGoogleConnect.setText(R.string.btn_label_disconnect);
                                break;
                            case "Facebook":
                                btnFacebookConnect.setText(R.string.btn_label_disconnect);
                                break;
                            case "Twitter":
                                btnTwitterConnect.setText(R.string.btn_label_disconnect);
                                break;
                        }
                    }
                }
            });
    }

    private String getProviderLink(String str) {
        try {
            List<String> providers = firebaseAuth.getCurrentUser().getProviders();
            for (String provider: providers) {
                if(provider.toLowerCase().contains(str)){
                    return provider;
                }
            }
        }catch (Exception ignore){}
        return  null;
    }

    private void setupUserProfile(){
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user.getPhotoUrl() != null){
            Picasso.with(getApplicationContext()).load(user.getPhotoUrl()).into(imgPicture);
        }

        edtDisplayName.setText(user.getDisplayName());
        edtEmail.setText(user.getEmail());
        if(user.isEmailVerified()){
            edtEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_black_24dp, 0);
        }
        edtPhoneNumber.setText(user.getPhoneNumber());
        List<String> providers = user.getProviders() != null ? user.getProviders() : new ArrayList<String>();
        for (String provider : providers) {
            String tmp = provider.toLowerCase();
            if (tmp.contains("google")) {
                btnGoogleConnect.setText(R.string.btn_label_disconnect);
            } else if (tmp.contains("facebook")) {
                btnFacebookConnect.setText(R.string.btn_label_disconnect);
            } else if (tmp.contains("twitter")) {
                btnTwitterConnect.setText(R.string.btn_label_disconnect);
            }
        }
    }

    private void toggleEditProfile(boolean edit){
        edtDisplayName.setEnabled(edit);
        edtEmail.setEnabled(edit);
        edtPhoneNumber.setEnabled(edit);

        btnGoogleConnect.setEnabled(edit);
        btnFacebookConnect.setEnabled(edit);
        btnTwitterConnect.setEnabled(edit);
        btnChangePassword.setEnabled(edit);

        if(edit){
            edtDisplayName.setBackground(undelineDrawable);
            edtEmail.setBackground(undelineDrawable);
            edtPhoneNumber.setBackground(undelineDrawable);
            btnChangePassword.setVisibility(View.VISIBLE);
        }else{
            edtDisplayName.setBackground(transparent);
            edtEmail.setBackground(transparent);
            edtPhoneNumber.setBackground(transparent);
            btnChangePassword.setVisibility(View.GONE);
        }

    }

    private void saveUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String newDisplayName = edtDisplayName.getText().toString();
        String newEmail = edtEmail.getText().toString();
        String newPhone = edtPhoneNumber.getText().toString();


        if(!newDisplayName.equals(user.getDisplayName())){
            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(newDisplayName).build())
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful() && task.getException() != null){
                            Log.e(TAG, "Display Name Error: ", task.getException());
                            edtDisplayName.setError(task.getException().getLocalizedMessage());
                        }
                    }
                });
        }

        if(!newEmail.equals(user.getEmail())){
            user.updateEmail(newEmail)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful() && task.getException() != null){
                            Log.e(TAG, "Email Update Error: ", task.getException());
                            edtEmail.setError(task.getException().getLocalizedMessage());
                        }
                    }
                });
        }

        if(!newPhone.equals(user.getPhoneNumber())){
            Intent intent = new Intent(getApplicationContext(), PhoneValidateActivity.class);
            intent.putExtra(PhoneValidateActivity.KEY_FULL_NUMBER, newPhone);
            startActivityForResult(intent, REQUEST_UPDATE_PHONE);
        }


    }

}
