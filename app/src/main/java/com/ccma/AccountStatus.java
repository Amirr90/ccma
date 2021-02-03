package com.ccma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dinuscxj.refresh.RecyclerRefreshLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ccma.Utility.Util.ACCOUNT_ID;
import static com.ccma.Utility.Util.ACCOUNT_NUMBER;
import static com.ccma.Utility.Util.ACCOUNT_STATUS_QUERY;
import static com.ccma.Utility.Util.BALANCE_DUE;
import static com.ccma.Utility.Util.CLASSIFICATION;
import static com.ccma.Utility.Util.DATA;
import static com.ccma.Utility.Util.TIMESTAMP;
import static com.ccma.Utility.Util.getCurrencyBalance;

public class AccountStatus extends AppCompatActivity {


    RecyclerView rec;
    List<DocumentSnapshot> account_status_list = new ArrayList<>();
    AccountStatusAdapter adapter;
    ProgressBar progressBar;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String AccountId;
    private RecyclerRefreshLayout swipe_refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_status);

        AccountId = getIntent().getStringExtra(ACCOUNT_ID);
        Toolbar toolbar = (Toolbar) findViewById(R.id.AccountStatus_toolbar);
        swipe_refresh = findViewById(R.id.AccountStatus_refresh_layout);

        setToolbar(toolbar, "");
        setRec();
        TextView acc_no = findViewById(R.id.textView21);
        acc_no.setText(getResources().getString(R.string.for_acc) + getIntent().getStringExtra(ACCOUNT_NUMBER));

        swipe_refresh.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //loadExaminationData();
                addData();
            }
        });
    }

    private void setRec() {
        progressBar = findViewById(R.id.AccountStatus_progressBar);
        rec = findViewById(R.id.AccountStatus_rec);
        rec.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AccountStatusAdapter(account_status_list);
        rec.setAdapter(adapter);
        progressBar.setVisibility(View.VISIBLE);
        addData();

    }

    private void addData() {
        firestore.collection(ACCOUNT_STATUS_QUERY)
                .whereEqualTo(ACCOUNT_NUMBER, AccountId)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                progressBar.setVisibility(View.GONE);
                if (queryDocumentSnapshots.isEmpty()) {
                    Toast.makeText(AccountStatus.this, "No data found", Toast.LENGTH_SHORT).show();
                    return;
                }
                account_status_list.clear();
                if (queryDocumentSnapshots != null)
                    account_status_list.addAll(queryDocumentSnapshots.getDocuments());
                adapter.notifyDataSetChanged();
                swipe_refresh.setRefreshing(false);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "error " + e.getLocalizedMessage());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AccountStatus.this, "try again", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void setToolbar(Toolbar toolbar, String id) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account Status");

    }

    private class AccountStatusAdapter extends RecyclerView.Adapter<AccountStatusAdapter.ViewHolder> {
        List<DocumentSnapshot> snapshots;

        public AccountStatusAdapter(List<DocumentSnapshot> snapshots) {
            this.snapshots = snapshots;
        }

        @NonNull
        @Override
        public AccountStatusAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_status_view, parent, false);
            return new AccountStatusAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AccountStatusAdapter.ViewHolder holder, final int position) {
            final DocumentSnapshot snapshot = snapshots.get(position);
            try {
                long balance = snapshot.getLong(BALANCE_DUE);
                String date = snapshot.getString(DATA);
                String classification = snapshot.getString(CLASSIFICATION);

                holder.balance_due.setText(getCurrencyBalance(balance));
                holder.date.setText(date);
                holder.classification.setText(classification);
            } catch (Exception e) {
                e.printStackTrace();
            }


            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final String doc_id = snapshot.getId();
                    new AlertDialog.Builder(AccountStatus.this)
                            .setMessage("Remove this entry??")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    progressBar.setVisibility(View.VISIBLE);
                                    firestore.collection(ACCOUNT_STATUS_QUERY)
                                            .document(doc_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(AccountStatus.this, "removed", Toast.LENGTH_SHORT).show();
                                            snapshots.remove(position);
                                            notifyDataSetChanged();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(AccountStatus.this, "try again", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                    return true;
                }
            });

        }

        @Override
        public int getItemCount() {
            return snapshots.size();

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView date, balance_due, classification;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.textView25);
                balance_due = itemView.findViewById(R.id.textView24);
                classification = itemView.findViewById(R.id.textView23);

            }
        }
    }
}