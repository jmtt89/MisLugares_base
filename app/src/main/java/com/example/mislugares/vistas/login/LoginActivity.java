package com.example.mislugares.vistas.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mislugares.R;
import com.example.mislugares.almacenamiento.Usuarios;
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
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 35;
    private static final int EMAIL_SIGN_IN = 40;
    private static final int PHONE_SIGN_IN = 45;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;


    private View wrapper;
    private LoginButton facebookLoginButton;
    private TwitterLoginButton twitterLoginButton;

    private Button emailLogin;
    private Button phoneLogin;
    private TextView skipLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        wrapper = findViewById(R.id.layout_wrapper);

        // Email
        emailLogin = findViewById(R.id.email_login_button);
        emailLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailSignIn();
            }
        });


        // Phone
        phoneLogin = findViewById(R.id.phone_login_button);
        phoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneSignIn();
            }
        });

        // Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("989762078439-usigj1rhnlis5jpirmfou5m9g6sevhf5.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.google_login_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });


        findViewById(R.id.aux_google_login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

        // Facebook
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
                failureAuth("Facebook Authentication Canceled");
            }

            @Override
            public void onError(FacebookException exception) {
                failureAuth("Facebook Authentication Failure", exception);
            }
        });

        findViewById(R.id.aux_facebook_login_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        facebookLoginButton.performClick();
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
                failureAuth("Twitter Authentication Failure", exception);
            }
        });

        findViewById(R.id.aux_twitter_login_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        twitterLoginButton.performClick();
                    }
                });

        //Anonymous
        skipLogin = findViewById(R.id.skip_authentication);
        skipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAnonymouslyAuth();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && !currentUser.isAnonymous()){
            successAuth("User Already Signed");
        }
    }

    private void emailSignIn(){
        Intent signInIntent = new Intent(this, EmailAuthActivity.class);
        startActivityForResult(signInIntent, EMAIL_SIGN_IN);
    }

    private void phoneSignIn(){
        Intent signInIntent = new Intent(this, PhoneAuthActivity.class);
        startActivityForResult(signInIntent, PHONE_SIGN_IN);
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
        }else if( requestCode == EMAIL_SIGN_IN){
            handleEmailUserCreated();
        }else if(requestCode == PHONE_SIGN_IN){
            PhoneAuthCredential credential = data.getParcelableExtra(PhoneValidateActivity.KEY_AUTH_CREDENTIAL);
            signInWithPhoneAuthCredential(credential);
        }else{
            // Facebook
            callbackManager.onActivityResult(requestCode, resultCode, data);

            // Twitter
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }



    private void handleEmailUserCreated(){
        successAuth("Email Authentication Success");
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            successAuth("Phone Authentication Success");
                        } else {
                            failureAuth("Phone Authentication Failed", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        signInOrLink("Google", credential);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        signInOrLink("Facebook", credential);
    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        signInOrLink("Twitter", credential);
    }

    private void handleAnonymouslyAuth(){
        if(mAuth.getCurrentUser() != null){
            successAuth("Anonymous Success");
            return;
        }
        mAuth.signInAnonymously()
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        successAuth("Anonymous Success");
                    } else {
                        failureAuth("Anonymous Authentication Failed.", task.getException());
                    }
                }
            });
    }


    private void signInOrLink(final String provider, AuthCredential credential){
        if(mAuth.getCurrentUser() != null){
            mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            successAuth("Link " + provider + "Success");
                        } else {
                            failureAuth("Link " + provider + "Failed.", task.getException());
                        }
                    }
                });
        } else {
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            successAuth(provider + "Authentication  Success");
                        } else {
                            failureAuth(provider + "Authentication Failed.", task.getException());
                        }
                    }
                });
        }
    }


    private void successAuth(String msg){
        Log.d(TAG, msg);
        Usuarios.guardarUsuario(getApplicationContext(), FirebaseAuth.getInstance().getCurrentUser());
        setResult(RESULT_OK);
        finish();
    }

    private void failureAuth(String msg, Exception _e){
        Log.e(TAG, msg, _e);
        Snackbar.make(wrapper, _e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
    }

    private void failureAuth(String msg) {
        Snackbar.make(wrapper, msg, Snackbar.LENGTH_LONG).show();
    }

}
