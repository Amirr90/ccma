package com.ccma;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.ccma.Utility.Util.ACCOUNT_NUMBER;
import static com.ccma.Utility.Util.ACCOUNT_QUERY;
import static com.ccma.Utility.Util.ADDRESS;
import static com.ccma.Utility.Util.ADDRESS_1;
import static com.ccma.Utility.Util.ADDRESS_2;
import static com.ccma.Utility.Util.BANK_NAME;
import static com.ccma.Utility.Util.BRANCH_NAME;
import static com.ccma.Utility.Util.DEFAULT;
import static com.ccma.Utility.Util.DOC_ID;
import static com.ccma.Utility.Util.EMAIL;
import static com.ccma.Utility.Util.EXTRA_COLUMN_1;
import static com.ccma.Utility.Util.EXTRA_COLUMN_2;
import static com.ccma.Utility.Util.IMAGE;
import static com.ccma.Utility.Util.IS_ACTIVE;
import static com.ccma.Utility.Util.LAT;
import static com.ccma.Utility.Util.LAT2;
import static com.ccma.Utility.Util.LONG;
import static com.ccma.Utility.Util.LONG2;
import static com.ccma.Utility.Util.MOB_NUM_1;
import static com.ccma.Utility.Util.MOB_NUM_2;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.NOT_AVAILABLE;
import static com.ccma.Utility.Util.PROJECT;
import static com.ccma.Utility.Util.RESPONSE;
import static com.ccma.Utility.Util.SAN_AMOUNT;
import static com.ccma.Utility.Util.SAN_DT;
import static com.ccma.Utility.Util.TIMESTAMP;
import static com.ccma.Utility.Util.USERNAME;
import static com.ccma.Utility.Util.getEmail;
import static com.ccma.Utility.Util.getUsername;
import static com.ccma.Utility.Util.hideKeyboard;


public class AddNewAccountScreen extends AppCompatActivity {


    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String ac_number, ac_name, ac_address, ac_amount, ac_date, ac_mobile1, ac_mobile2, ac_project;
    TextInputEditText et_accountNumber, et_accountName, et_address, et_mobile1, et_mobile2, et_amount, et_date, et_project;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_account_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_new_account_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add New Account");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        findViewById();


    }

    private void findViewById() {
        et_accountNumber = findViewById(R.id.et_account_number);
        et_accountName = findViewById(R.id.et_account_name);
        et_address = findViewById(R.id.et_account_address);
        et_mobile1 = findViewById(R.id.et_account_mobile1);
        et_mobile2 = findViewById(R.id.et_account_mobile2);
        et_amount = findViewById(R.id.et_account_amount);
        et_date = findViewById(R.id.et_account_date);
        et_project = findViewById(R.id.et_account_project);
        et_accountName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
    }

    public void addNewAccount(View view) {

        ac_number = et_accountNumber.getText().toString();
        ac_name = et_accountName.getText().toString();
        ac_address = et_address.getText().toString();
        ac_amount = et_amount.getText().toString();
        ac_date = et_date.getText().toString();
        ac_mobile1 = et_mobile1.getText().toString();
        ac_mobile2 = et_mobile2.getText().toString();
        ac_project = et_project.getText().toString();
        if (ac_number.isEmpty()) {
            et_accountNumber.setError("required");
        } else if (ac_name.isEmpty()) {
            et_accountName.setError("required");
        } else if (ac_address.isEmpty()) {
            et_address.setError("required");
        } else if (ac_amount.isEmpty()) {
            et_amount.setError("required");
        } else if (ac_date.isEmpty()) {
            et_date.setError("required");
        } else if (ac_project.isEmpty()) {
            et_project.setError("required");
        } else {
            progressDialog.setMessage("Adding Account,Please wait....");
            progressDialog.show();
            hideKeyboard(AddNewAccountScreen.this);

            DocumentReference ref = firestore.collection(ACCOUNT_QUERY).document();
            String doc_id = ref.getId();

            firestore.collection(ACCOUNT_QUERY)
                    .document(doc_id)
                    .set(getAddNewAccountMap(doc_id))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Intent intent = new Intent();
                            intent.putExtra(RESPONSE, "Added Successfully");
                            setResult(RESULT_OK, intent);
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra(RESPONSE, "failed to add " + e.getLocalizedMessage());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

        }

    }

    public Map<String, Object> getAddNewAccountMap(String doc_id) {
        Map<String, Object> map = new HashMap<>();

        map.put(ACCOUNT_NUMBER, 0);
        map.put(EXTRA_COLUMN_1, et_accountNumber.getText().toString());
        map.put(PROJECT, ac_project);
        map.put(NAME, ac_name);
        map.put(EMAIL, getEmail(AddNewAccountScreen.this));
        map.put(USERNAME, getUsername(AddNewAccountScreen.this));
        map.put(ADDRESS, ac_address);
        map.put(ADDRESS_1, DEFAULT);
        map.put(ADDRESS_2, DEFAULT);
        map.put(MOB_NUM_1, ac_mobile1);
        map.put(DOC_ID, doc_id);
        map.put(MOB_NUM_2, ac_mobile2);
        map.put(SAN_DT, ac_date);
        map.put(TIMESTAMP, System.currentTimeMillis());
        map.put(SAN_AMOUNT, Long.parseLong(ac_amount));
        map.put(BANK_NAME, DEFAULT);
        map.put(BRANCH_NAME, DEFAULT);
        map.put(IMAGE, DEFAULT);
        map.put(IS_ACTIVE, true);
        map.put(LAT, 0);
        map.put(LONG, 0);
        map.put(LAT2, 0);
        map.put(LONG2, 0);
        map.put(EXTRA_COLUMN_2, DEFAULT);

        return map;
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        return super.onSupportNavigateUp();

    }
}