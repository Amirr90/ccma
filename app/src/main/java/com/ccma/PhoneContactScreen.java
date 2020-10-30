package com.ccma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ccma.Adapters.PersonalContractAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ccma.Utility.Util.ACCOUNT_ID;
import static com.ccma.Utility.Util.ACCOUNT_NUMBER;
import static com.ccma.Utility.Util.ACCOUNT_QUERY;
import static com.ccma.Utility.Util.ADDRESS;
import static com.ccma.Utility.Util.DATA;
import static com.ccma.Utility.Util.EMAIL;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.PHONE_CONTACT;
import static com.ccma.Utility.Util.QUERY_PHONE_CONTACT;
import static com.ccma.Utility.Util.RESPONSE;
import static com.ccma.Utility.Util.SAN_AMOUNT;
import static com.ccma.Utility.Util.SAN_DT;
import static com.ccma.Utility.Util.TIMESTAMP;
import static com.ccma.Utility.Util.USERNAME;
import static com.ccma.Utility.Util.getEmail;
import static com.ccma.Utility.Util.getPhoneContactData;

public class PhoneContactScreen extends AppCompatActivity {

    private static final int REQ_CODE_PHONE_CONTACT = 1022;
    ArrayList<String> phoneContactData;
    String AccountId;
    PersonalContractAdapter adapter;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    CollectionReference reference;
    RecyclerView recyclerView;
    List<DocumentSnapshot> phoneContactList = new ArrayList<>();
    ProgressBar progressBar;
    TextView textView;
    String AccountNumber, Username, SanAmount, SanDate, Address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_contact_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.phone_contact_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Phone Contact");

        AccountId = getIntent().getStringExtra(ACCOUNT_ID);
        AccountNumber = getIntent().getStringExtra(ACCOUNT_NUMBER);
        Username = getIntent().getStringExtra(USERNAME);
        SanAmount = getIntent().getStringExtra(SAN_AMOUNT);
        SanDate = getIntent().getStringExtra(SAN_DT);
        Address = getIntent().getStringExtra(ADDRESS);

        phoneContactData = getPhoneContactData();

        reference = firestore.collection(ACCOUNT_QUERY).document(AccountId)
                .collection(QUERY_PHONE_CONTACT);

        progressBar = findViewById(R.id.progressBar7);
        textView = findViewById(R.id.textView133);
        recyclerView = findViewById(R.id.phone_contact_rec);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new PersonalContractAdapter(phoneContactList, this, QUERY_PHONE_CONTACT, AccountId);
        recyclerView.setAdapter(adapter);
        loadCommentData();
    }

    private void loadCommentData() {
        reference.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(PhoneContactScreen.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e == null && null != queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                phoneContactList.clear();
                                phoneContactList.addAll(queryDocumentSnapshots.getDocuments());
                                textView.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            } else {
                                textView.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        } else {
                            textView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void phoneContactOnClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.smart_suggestion_phone_contact: {
                startActivityForResult(new Intent(PhoneContactScreen.this, SuggestionScreen2.class)
                        .putExtra("data", phoneContactData)
                        .putExtra(ACCOUNT_NUMBER, AccountNumber)
                        .putExtra(USERNAME, Username)
                        .putExtra(SAN_AMOUNT, SanAmount)
                        .putExtra(SAN_DT, SanDate)
                        .putExtra(ADDRESS, Address)
                        .putExtra(ACCOUNT_ID, AccountId), REQ_CODE_PHONE_CONTACT);
            }
            break;

            case R.id.phone_contact_send_btn: {
                final EditText et = findViewById(R.id.phone_contact_text);
                String text = et.getText().toString();
                if (!text.isEmpty()) {
                    et.setText("");
                    final List<String> listToUpload = new ArrayList<>();
                    listToUpload.add(text);
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    Map<String, Object> map = new HashMap<>();
                    map.put(DATA, listToUpload);
                    map.put(TIMESTAMP, timestamp);
                    map.put(ACCOUNT_NUMBER, AccountNumber);
                    map.put(NAME, Username);
                    map.put(SAN_AMOUNT, SanAmount);
                    map.put(SAN_DT, SanDate);
                    map.put(ADDRESS, Address);
                    map.put(EMAIL, getEmail(PhoneContactScreen.this));
                    reference.document(timestamp).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //adapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PhoneContactScreen.this, "failed to update data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            break;
            default:
                Snackbar.make(view, "Coming Soon", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PHONE_CONTACT) {
            if (resultCode == RESULT_OK) {
                if (null != data) {
                    String res = data.getStringExtra(RESPONSE);
                    Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}