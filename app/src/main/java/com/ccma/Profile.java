package com.ccma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.List;

import static com.ccma.Utility.Util.BANK_NAME;
import static com.ccma.Utility.Util.BRANCH_NAME;
import static com.ccma.Utility.Util.EMAIL;
import static com.ccma.Utility.Util.LOGIN_CREDENTIALS;
import static com.ccma.Utility.Util.MOBILE;
import static com.ccma.Utility.Util.MOB_NUM_1;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.PASSWORD;
import static com.ccma.Utility.Util.USERNAME;
import static com.ccma.Utility.Util.getEmail;
import static com.ccma.Utility.Util.getUsername;
import static com.ccma.Utility.Util.saveBankName;
import static com.ccma.Utility.Util.saveBranchName;
import static com.ccma.Utility.Util.saveUserName;

public class Profile extends AppCompatActivity {
    TextView email, branchName, bankName, mobileNumber, username;
    FirebaseFirestore firestore;
    Query query;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);


        email = findViewById(R.id.textView14);
        branchName = findViewById(R.id.textView15);
        bankName = findViewById(R.id.textView16);
        mobileNumber = findViewById(R.id.textView17);
        username = findViewById(R.id.username_tv);

        firestore = FirebaseFirestore.getInstance();
        query = firestore.collection(LOGIN_CREDENTIALS)
                .whereEqualTo(EMAIL, getEmail(this));

        email.setText("Institution/Firm email\n" + getEmail(this));
        username.setText("Institution/Firm\nUsername: " + getUsername(this));


        setPersonalData();
    }

    private void setPersonalData() {
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots == null && queryDocumentSnapshots.isEmpty()) {
                    return;
                }
                String doc_id = queryDocumentSnapshots.getDocuments().get(0).getId();
                firestore.collection(LOGIN_CREDENTIALS).document(doc_id)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.contains(BRANCH_NAME)) {
                                    branchName.setText("Branch: " + documentSnapshot.getString(BRANCH_NAME));
                                    saveBranchName(documentSnapshot.getString(BRANCH_NAME), Profile.this);
                                }
                                if (documentSnapshot.contains(BANK_NAME)) {
                                    bankName.setText("Institution/firm name: " + documentSnapshot.getString(BANK_NAME));
                                    saveBankName(documentSnapshot.getString(BANK_NAME), Profile.this);

                                }
                                if (documentSnapshot.contains(MOBILE)) {
                                    mobileNumber.setText("Mobile: " + documentSnapshot.getString(MOBILE));
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Profile.this, "some thing went wrong,try again", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }


    public void update(View view) {
        int id = view.getId();
        TextView textView = findViewById(id);
        switch (id) {

            case R.id.username_tv: {
                updateToDatabase(USERNAME, view, textView);
            }
            break;
            case R.id.textView15: {
                updateToDatabase(BRANCH_NAME, view, textView);
            }
            break;
            case R.id.textView16: {
                updateToDatabase(BANK_NAME, view, textView);

            }
            break;
            case R.id.textView17: {
                updateToDatabase(MOBILE, view, textView);
            }
            break;
        }
    }

    private void updateToDatabase(final String field, final View view, final TextView textView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        LayoutInflater inflater = Profile.this.getLayoutInflater();
        View view1 = inflater.inflate(R.layout.update_lay, null);
        final TextInputEditText inputEditText = view1.findViewById(R.id.update_et);
        inputEditText.setHint(field);
        builder.setView(view1)
                .setMessage("Update " + field)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final String text = inputEditText.getText().toString();
                        if (text.isEmpty()) {
                            return;
                        }
                        Toast.makeText(Profile.this, "Updating....", Toast.LENGTH_SHORT).show();
                        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                        firebaseFirestore.collection(LOGIN_CREDENTIALS)
                                .whereEqualTo(EMAIL, getEmail(Profile.this))
                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.isEmpty()) {
                                    Toast.makeText(Profile.this, "No user found to update", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String doc_id = queryDocumentSnapshots.getDocuments().get(0).getId();
                                firebaseFirestore.collection(LOGIN_CREDENTIALS).document(doc_id)
                                        .update(field, text).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        textView.setText(field + ": " + text);
                                        if (field.equalsIgnoreCase(USERNAME)) {
                                            saveUserName(text, Profile.this);
                                        } else if (field.equalsIgnoreCase(BANK_NAME)) {
                                            saveBankName(text, Profile.this);
                                        } else if (field.equalsIgnoreCase(BRANCH_NAME)) {
                                            saveBranchName(text, Profile.this);
                                        }

                                        Snackbar.make(view, "Uploaded successfully", Snackbar.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(view, "could'nt update", Snackbar.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(view, "no user found", Snackbar.LENGTH_SHORT).show();

                            }
                        });


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // remove the dialog from the screen
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void changePassword(View view) {
        if (view.getId() == R.id.button3) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);

            LayoutInflater inflater = Profile.this.getLayoutInflater();
            View view1 = inflater.inflate(R.layout.update_password, null);
            final EditText oldPassword, newPassword;
            oldPassword = view1.findViewById(R.id.old_password);
            newPassword = view1.findViewById(R.id.new_password);
            builder.setView(view1)
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String old_password = oldPassword.getText().toString();
                            String new_password = newPassword.getText().toString();
                            if (!old_password.isEmpty() && !new_password.isEmpty()) {
                                dialog.dismiss();
                                progressDialog.setMessage("Updating password,Please wait....");
                                progressDialog.show();
                                updatePassword(old_password, new_password);
                            }
                            // your sign in code here
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void updatePassword(final String old_password, final String new_password) {
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);
                            String password = snapshot.getString(PASSWORD);
                            String doc_id = snapshot.getId();
                            if (password.equalsIgnoreCase(old_password)) {
                                firestore.collection(LOGIN_CREDENTIALS).document(doc_id)
                                        .update(PASSWORD, new_password).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(Profile.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(Profile.this, "failed to update password,try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(Profile.this, "password do not matched", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}