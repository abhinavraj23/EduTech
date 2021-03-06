package com.example.smit.edutech;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class signUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private SignInButton mGoogleBtn;
    private DatabaseReference mDataBase;
    private GoogleApiClient mGoogleApi;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private ProgressBar progressBar;
    private static final String EMAIL = "email";
    private static int RC_SIGN_IN = 1;
    private static String TAG = "TAG";
    public Button create;
    EditText user,Email,passWord;
    String User,eMail,PassWord;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        user = findViewById(R.id.userName);
        Email = findViewById(R.id.email2);
        passWord = findViewById(R.id.password2);



        mAuth = FirebaseAuth.getInstance();

        create = findViewById(R.id.signup2);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User = user.getText().toString();
                eMail = Email.getText().toString();
                PassWord = passWord.getText().toString();
                if(eMail.length()>0 && PassWord.length()>0 && User.length()>0) {
                    createAccount(eMail, PassWord, User);
                }
                else{
                    Toast.makeText(signUpActivity.this, "Enter E-Mail or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mGoogleBtn = findViewById(R.id.googleBtn);
        progressBar = findViewById(R.id.progress_bar);
        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                signIn();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(signUpActivity.this, homeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        mGoogleApi = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(signUpActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();



    }



    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListner);
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void createAccount(final String email, final String password, final String User1) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            final boolean[] exists = {false};

                            mDataBase = FirebaseDatabase.getInstance().getReference();

                            final DatabaseReference mRef = mDataBase.child("Users").child(User1);
                            mRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot data : dataSnapshot.getChildren()){
                                        if((data.child("Email").exists()) && (data.child("Email").getValue().equals(email))){
                                            exists[0]=true;
                                            Toast.makeText(signUpActivity.this, "Account already exists!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    if(!exists[0]) {

                                        User User = new User(User1,email);
                                        mRef.setValue(User).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(!task.isSuccessful()){
                                                    Toast.makeText(signUpActivity.this,"Error occured", Toast.LENGTH_SHORT).show();
                                                }
                                                if(task.isSuccessful()){
                                                    startActivity(new Intent(signUpActivity.this,homeActivity.class));
                                                }
                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }

                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(signUpActivity.this, "Invalid E-Mail or Password",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApi);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressBar.setVisibility(View.INVISIBLE);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {

                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                //final GoogleSignInAccount acct = result.getSignInAccount();
                //    new GetGendersTask().execute(acct);
                firebaseAuthWithGoogle(account);
                handleSignInResult(result);

            } else {
                // Google Sign In failed, update UI appropriately

                // ...
            }
        }
    }

    private void handleSignInResult (GoogleSignInResult result){
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(getApplicationContext(), "" + acct.getDisplayName(), Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            final boolean[] exists = {false};

                            mDataBase = FirebaseDatabase.getInstance().getReference();

                            final DatabaseReference mRef = mDataBase.child("Users").child(user.getDisplayName());
                            mRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot data : dataSnapshot.getChildren()){
                                        if((data.child("Email").exists()) && (data.child("Email").getValue().equals(user.getEmail()))){
                                            exists[0]=true;
                                        }
                                    }
                                    if(!exists[0]) {
                                        User User = new User(user.getDisplayName(),user.getEmail());
                                        mRef.setValue(User).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(!task.isSuccessful()){
                                                    Toast.makeText(signUpActivity.this,"Error occured", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(signUpActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}
