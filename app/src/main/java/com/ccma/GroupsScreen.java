package com.ccma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccma.Modals.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.ccma.Utility.Util.ACCOUNT_ID;
import static com.ccma.Utility.Util.DEFAULT;
import static com.ccma.Utility.Util.EMAIL;
import static com.ccma.Utility.Util.GROUPS_QUERY;
import static com.ccma.Utility.Util.GROUP_ID;
import static com.ccma.Utility.Util.TIMESTAMP;
import static com.ccma.Utility.Util.TYPE;
import static com.ccma.Utility.Util.getEmail;

public class GroupsScreen extends AppCompatActivity {
    String type;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<DocumentSnapshot> fullUserList = new ArrayList<>();
    UserInGroupAdapter adapter;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_screen);


        Toolbar toolbar = (Toolbar) findViewById(R.id._toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Groups");

        type = getIntent().getStringExtra(TYPE);


        recyclerView = findViewById(R.id.rec3);
        progressBar = findViewById(R.id.progressBar112);

        setRec();
    }

    private void setRec() {
        adapter = new UserInGroupAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        fullUserList.clear();
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(GROUPS_QUERY)
                .whereEqualTo(EMAIL, getEmail(GroupsScreen.this))
                .whereEqualTo(TYPE, type)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        progressBar.setVisibility(View.GONE);
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            fullUserList.addAll(queryDocumentSnapshots.getDocuments());
                            adapter.notifyDataSetChanged();

                        } else {
                            Toast.makeText(GroupsScreen.this, "No data found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: " + e.getLocalizedMessage());
                Toast.makeText(GroupsScreen.this, "try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private class UserInGroupAdapter extends RecyclerView.Adapter<UserInGroupAdapter.ViewHolder> {
        @NonNull
        @Override
        public UserInGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.home_view, parent, false);
            UserInGroupAdapter.ViewHolder viewHolder = new UserInGroupAdapter.ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final UserInGroupAdapter.ViewHolder holder, final int position) {

            final DocumentSnapshot snapshot = fullUserList.get(position);
            try {
                String accNo = snapshot.getId();
                holder.account_number.setText(accNo);
            } catch (Exception e) {
            }

            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id = snapshot.getId();
                    startActivity(new Intent(GroupsScreen.this, UserInGroupScreen.class)
                            .putExtra(TYPE, type)
                            .putExtra(GROUP_ID, id));
                }
            });


        }


        @Override
        public int getItemCount() {
            return fullUserList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView account_number, name;
            public RelativeLayout relativeLayout;
            ImageView profileImage;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.account_number = (TextView) itemView.findViewById(R.id.acc_number);
                this.name = (TextView) itemView.findViewById(R.id.name);
                name.setText("Group Id");
                profileImage = itemView.findViewById(R.id.imageView);
                relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            }
        }
    }


}
