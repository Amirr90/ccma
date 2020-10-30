package com.ccma;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import static com.ccma.Utility.Util.*;

public class LoginScreen extends AppCompatActivity {
    TextInputEditText email, password;
    ProgressDialog progressDialog;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        email = findViewById(R.id.email_edit);
        password = findViewById(R.id.password_edit);


        progressDialog = new ProgressDialog(this);
    }

    public void login(final View view) {
        int id = view.getId();
        if (id == R.id.button_login) {

            final String Email, Password;
            Email = email.getText().toString();
            Password = password.getText().toString();
            if (Email.isEmpty()) {
                Snackbar.make(view, "email required", Snackbar.LENGTH_SHORT).show();
            } else if (Password.isEmpty()) {
                Snackbar.make(view, "password could not be empty", Snackbar.LENGTH_SHORT).show();
            } else {
                progressDialog.setMessage("Please wait.....");
                progressDialog.setCancelable(false);
                progressDialog.show();
                hideKeyboard(LoginScreen.this);
                firestore.collection(LOGIN_CREDENTIALS)
                        .whereEqualTo(EMAIL, Email)
                        .whereEqualTo(PASSWORD, Password)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    firestore.collection(LOGIN_CREDENTIALS)
                                            .document(queryDocumentSnapshots.getDocuments().get(0).getId()).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    String userName = documentSnapshot.getString(USERNAME);
                                                    saveLoginSession(true, LoginScreen.this, Email, userName);
                                                    progressDialog.dismiss();
                                                    Toast.makeText(LoginScreen.this, "Login successfully", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(LoginScreen.this, HomeScreen.class));
                                                    finish();
                                                }
                                            });

                                } else {
                                    progressDialog.dismiss();
                                    Snackbar.make(view, "INVALID CREDENTIALS", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Snackbar.make(view, "failed to login,try again", Snackbar.LENGTH_SHORT).show();

                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        progressDialog.dismiss();
                        Snackbar.make(view, "login cancelled", Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
        }
    }


    public void SignUp(View view) {


        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginScreen.this);

        LayoutInflater inflater = LoginScreen.this.getLayoutInflater();

        View view2 = inflater.inflate(R.layout.signup, null);
        final TextInputEditText sign_up_username_et = view2.findViewById(R.id.sign_up_username);
        final TextInputEditText sign_up_email_et = view2.findViewById(R.id.sign_up_email);
        final TextInputEditText sign_up_password_et = view2.findViewById(R.id.sign_up_password);
        Button btn_sign_up = view2.findViewById(R.id.button_sign_up);


        builder.setView(view2)
                .show();

        btn_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = sign_up_email_et.getText().toString();
                final String username = sign_up_username_et.getText().toString();
                final String password = sign_up_password_et.getText().toString();

                if (username.isEmpty()) {
                    sign_up_username_et.setError("required");
                } else if (email.isEmpty()) {
                    sign_up_email_et.setError("required");
                } else if (password.isEmpty()) {
                    sign_up_password_et.setError("required");
                } else {
                    hideKeyboard(LoginScreen.this);
                    progressDialog.setMessage("Signing up,Please wait....");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    firestore.collection(LOGIN_CREDENTIALS)
                            .whereEqualTo(EMAIL, email)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (queryDocumentSnapshots.isEmpty()) {
                                        firestore.collection(LOGIN_CREDENTIALS).add(getSignUpMap(email, password, username)).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference reference) {
                                                progressDialog.dismiss();
                                                saveLoginSession(true, LoginScreen.this, email, username);
                                                Toast.makeText(LoginScreen.this, "Sign up successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(LoginScreen.this, HomeScreen.class));
                                                finish();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(LoginScreen.this, "try again", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginScreen.this, "email already register", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginScreen.this, "try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }


}