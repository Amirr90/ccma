package com.ccma;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ccma.Utility.Util.ACCOUNT_ID;
import static com.ccma.Utility.Util.ACCOUNT_NUMBER;
import static com.ccma.Utility.Util.ACCOUNT_QUERY;
import static com.ccma.Utility.Util.ADDED_BY;
import static com.ccma.Utility.Util.ADDED_TO;
import static com.ccma.Utility.Util.CREATED_AT;
import static com.ccma.Utility.Util.DEFAULT;
import static com.ccma.Utility.Util.EMAIL;
import static com.ccma.Utility.Util.FAMILY_GROUPS_QUERY;
import static com.ccma.Utility.Util.GROUPS_QUERY;
import static com.ccma.Utility.Util.GROUP_ID;
import static com.ccma.Utility.Util.IMAGE;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.OTHER_ACCOUNT_QUERY;
import static com.ccma.Utility.Util.SAN_AMOUNT;
import static com.ccma.Utility.Util.SAN_DT;
import static com.ccma.Utility.Util.SELF_ACCOUNT_QUERY;
import static com.ccma.Utility.Util.SELF_GROUPS_QUERY;
import static com.ccma.Utility.Util.TIMESTAMP;
import static com.ccma.Utility.Util.TYPE;
import static com.ccma.Utility.Util.USERNAME;
import static com.ccma.Utility.Util.getEmail;
import static com.ccma.Utility.Util.hideKeyboard;

public class PickAccountScreen extends AppCompatActivity {

    private static final String TAG = "PickAccountScreen";
    int type;
    TextInputEditText searchEditText;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    AllAccountHolderAdapter2 adapter;
    List<DocumentSnapshot> fullUserList = new ArrayList<>();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    RelativeLayout uploadExcelLayout;
    String AccountId, AccountNumber, Username, SanDate, SanAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_account_screen);


        Toolbar toolbar = (Toolbar) findViewById(R.id.pick_account_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("TITLE"));

        searchEditText = findViewById(R.id.searche_edit_text);
        recyclerView = findViewById(R.id.search_rec);
        progressBar = findViewById(R.id.progressBar10);
        uploadExcelLayout = findViewById(R.id.uploadExcelLayout2);

        type = getIntent().getIntExtra(TYPE, 0);
        AccountId = getIntent().getStringExtra(ACCOUNT_ID);
        AccountNumber = getIntent().getStringExtra(ACCOUNT_NUMBER);
        Username = getIntent().getStringExtra(USERNAME);
        SanAmount = getIntent().getStringExtra(SAN_AMOUNT);
        SanDate = getIntent().getStringExtra(SAN_DT);

        setRec();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (!text.isEmpty()) {
                    loadData(text);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void setRec() {
        adapter = new AllAccountHolderAdapter2(fullUserList, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void loadData(CharSequence charSequence) {
        long acc_num;
        try {
            acc_num = Long.parseLong(charSequence.toString());
            progressBar.setVisibility(View.VISIBLE);
            Query query;
            if (type == 0) {
                query = firestore.collection(GROUPS_QUERY)
                        .whereEqualTo(EMAIL, getEmail(PickAccountScreen.this))
                        .whereEqualTo(TYPE, SELF_GROUPS_QUERY)
                        .orderBy(CREATED_AT)
                        .startAt(acc_num)
                        .endAt(acc_num + "\uf8ff")
                        .limit(5);
            } else {
                query = firestore.collection(GROUPS_QUERY)
                        .whereEqualTo(EMAIL, getEmail(PickAccountScreen.this))
                        .whereEqualTo(TYPE, FAMILY_GROUPS_QUERY)
                        .orderBy(CREATED_AT)
                        .startAt(acc_num)
                        .endAt(acc_num + "\uf8ff")
                        .limit(5);
            }


            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    progressBar.setVisibility(View.GONE);
                    if (e != null) {
                        Log.d(TAG, "error " + e.getLocalizedMessage());
                        Toast.makeText(PickAccountScreen.this, "failed to get User " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fullUserList.clear();
                    fullUserList.addAll(queryDocumentSnapshots.getDocuments());
                    Collections.reverse(fullUserList);
                    adapter.notifyDataSetChanged();

                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.d("PickAccount", "PickAccount" + e.getLocalizedMessage());
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public class AllAccountHolderAdapter2 extends RecyclerView.Adapter<AllAccountHolderAdapter2.ViewHolder> {

        List<DocumentSnapshot> accountList;
        Context context;

        public AllAccountHolderAdapter2(List<DocumentSnapshot> accountList, Context context) {
            this.accountList = accountList;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.home_view, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final AllAccountHolderAdapter2.ViewHolder holder, int position) {

            final DocumentSnapshot user = accountList.get(position);

            try {
                long acc_number = user.getLong(CREATED_AT);
                holder.account_number.setText("" + acc_number);
                holder.name.setText(user.getString(TYPE));


                final String imageUrl = user.getString(IMAGE);
                if (!imageUrl.equalsIgnoreCase(DEFAULT)) {
                    Picasso.with(context).load(imageUrl)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(holder.profileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(context).load(imageUrl)
                                            .placeholder(R.drawable.ic_launcher_foreground).into(holder.profileImage);
                                }
                            });
                } else {
                    holder.profileImage.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } catch (Exception e) {
            }

            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(context).setCancelable(false)
                            .setMessage("Add Account to Group " + user.getLong(CREATED_AT))
                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                    addAccount(user);
                                }
                            }).setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

                }
            });

        }

        private void addAccount(final DocumentSnapshot user) {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            hideKeyboard(PickAccountScreen.this);
            final DatabaseReference databaseReference;
            if (type == 0)
                databaseReference = reference.child(SELF_GROUPS_QUERY).child(user.getId());
            else
                databaseReference = reference.child(FAMILY_GROUPS_QUERY).child(user.getId());


            firestore.collection(ACCOUNT_QUERY).document(AccountId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                databaseReference
                                        .child(documentSnapshot.getLong(ACCOUNT_NUMBER) + "")
                                        .setValue(documentSnapshot.getData())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(context, "failed to add " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                            } else {
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, "No user Found To Add", Toast.LENGTH_SHORT).show();
                }
            });


        }

        @Override
        public int getItemCount() {
            return accountList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView account_number, name;
            public RelativeLayout relativeLayout;
            ImageView profileImage;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.account_number = (TextView) itemView.findViewById(R.id.acc_number);
                this.name = (TextView) itemView.findViewById(R.id.name);
                profileImage = itemView.findViewById(R.id.imageView);
                relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            }
        }
    }

    private void addAccountToDatabase(final String acc_id, final String userName, final String account_number, final String accountId, final String san_amount, final String san_date) {
        final CollectionReference query;
        if (type == 0) {
            firestore.collection(SELF_ACCOUNT_QUERY)
                    .whereEqualTo(ADDED_BY, accountId)
                    .whereEqualTo(ADDED_TO, acc_id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.isEmpty()) {
                                firestore.collection(SELF_ACCOUNT_QUERY)
                                        .add(getSelfAccountMap(acc_id, userName, account_number, accountId, san_amount, san_date))
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference reference) {
                                                updateMyAccountData(acc_id);
                                                Toast.makeText(PickAccountScreen.this, "Account Added", Toast.LENGTH_SHORT).show();
                                                uploadExcelLayout.setVisibility(View.GONE);
                                                finish();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        uploadExcelLayout.setVisibility(View.GONE);
                                        Toast.makeText(PickAccountScreen.this, "", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                uploadExcelLayout.setVisibility(View.GONE);
                                Toast.makeText(PickAccountScreen.this, "Account Already Added", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    uploadExcelLayout.setVisibility(View.GONE);
                    Toast.makeText(PickAccountScreen.this, "Failed to add,try again", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            firestore.collection(OTHER_ACCOUNT_QUERY)
                    .whereEqualTo(ADDED_BY, accountId)
                    .whereEqualTo(ADDED_TO, acc_id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.isEmpty()) {
                                firestore.collection(OTHER_ACCOUNT_QUERY)
                                        .add(getSelfAccountMap(acc_id, userName, account_number, accountId, san_amount, san_date))
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference reference) {
                                                updateMyAccountData(acc_id);
                                                Toast.makeText(PickAccountScreen.this, "Account Added", Toast.LENGTH_SHORT).show();
                                                uploadExcelLayout.setVisibility(View.GONE);
                                                finish();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        uploadExcelLayout.setVisibility(View.GONE);
                                        Toast.makeText(PickAccountScreen.this, "", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                uploadExcelLayout.setVisibility(View.GONE);
                                Toast.makeText(PickAccountScreen.this, "Account Already Added", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    uploadExcelLayout.setVisibility(View.GONE);
                    Toast.makeText(PickAccountScreen.this, "Failed to add,try again", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    private void updateMyAccountData(String accountId) {
        if (type == 0) {
            firestore.collection(SELF_ACCOUNT_QUERY)
                    .add(getSelfAccountMap(AccountId, Username, AccountNumber, accountId, SanAmount, SanDate));

        } else {
            firestore.collection(OTHER_ACCOUNT_QUERY)
                    .add(getSelfAccountMap(AccountId, Username, AccountNumber, accountId, SanAmount, SanDate));
        }
    }

    private Object getSelfAccountMap(String acc_id, String userName, String account_number, String accountId, String san_amount, String san_date) {
        Map<String, Object> map = new HashMap<>();
        map.put(ADDED_TO, acc_id);
        map.put(GROUP_ID, accountId);
        map.put(ADDED_BY, accountId);
        map.put(NAME, userName);
        map.put(SAN_AMOUNT, san_amount);
        map.put(SAN_DT, san_date);
        map.put(EMAIL, getEmail(PickAccountScreen.this));
        map.put(ACCOUNT_NUMBER, account_number);
        map.put(TIMESTAMP, System.currentTimeMillis());
        return map;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_add_account, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final long gId = System.currentTimeMillis();
        final DocumentReference query;
        query = firestore.collection(GROUPS_QUERY).document(gId + "");
        switch (item.getItemId()) {
            case R.id.menuCreateGroup: {
                new AlertDialog.Builder(PickAccountScreen.this)
                        .setMessage("Create New Group??")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                progressBar.setVisibility(View.VISIBLE);
                                query.set(getNewGroupMap(gId, PickAccountScreen.this))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressBar.setVisibility(View.GONE);
                                                String text = "A group is created with id " + gId;
                                                showDialogToAddAccount(text, gId);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(PickAccountScreen.this, "try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialogToAddAccount(String text, final long gId) {
        new AlertDialog.Builder(PickAccountScreen.this)
                .setMessage(text)
                .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String ty;
                        if (type == 0)
                            ty = SELF_ACCOUNT_QUERY;
                        else
                            ty = FAMILY_GROUPS_QUERY;

                        startActivity(new Intent(PickAccountScreen.this, AddUserInGroupScreen.class)
                                .putExtra(GROUP_ID, gId + "")
                                .putExtra(TYPE, ty));
                    }
                }).setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private Object getNewGroupMap(long gId, Context context) {
        Map<String, Object> map = new HashMap<>();
        map.put(CREATED_AT, gId);
        map.put(EMAIL, getEmail(context));
        map.put(TIMESTAMP, System.currentTimeMillis());
        map.put(TYPE, type == 0 ? SELF_GROUPS_QUERY : FAMILY_GROUPS_QUERY);
        return map;
    }


}