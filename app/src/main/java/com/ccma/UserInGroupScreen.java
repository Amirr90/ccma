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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.ccma.Utility.Util.ACCOUNT_ID;
import static com.ccma.Utility.Util.DEFAULT;
import static com.ccma.Utility.Util.GROUPS_QUERY;
import static com.ccma.Utility.Util.GROUP_ID;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.TYPE;

public class UserInGroupScreen extends AppCompatActivity {

    String groupId, type;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<UserModel> fullUserList = new ArrayList<>();
    UserInGroupAdapter adapter;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_in_group_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id._toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("GroupId " + getIntent().getStringExtra(GROUP_ID));

        btnDelete = findViewById(R.id.btnDeleteGrp);
        groupId = getIntent().getStringExtra(GROUP_ID);
        type = getIntent().getStringExtra(TYPE);


        recyclerView = findViewById(R.id.rec2);
        progressBar = findViewById(R.id.progressBar11);

        setRec();


    }


    public void deleteGroup(View view) {
        new AlertDialog.Builder(this).setMessage("Do you really want to delete this group??")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressBar.setVisibility(View.VISIBLE);
                        dialogInterface.dismiss();
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection(GROUPS_QUERY).document(groupId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(UserInGroupScreen.this, "Group Deleted Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(UserInGroupScreen.this, "unable to delete This group, try again", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();

    }

    public void addAccountHolder(View view) {
        if (view.getId() == R.id.button2) {
            startActivity(new Intent(this, AddUserInGroupScreen.class)
                    .putExtra(GROUP_ID, groupId)
                    .putExtra(TYPE, type));
        }
    }

    private void setRec() {
        adapter = new UserInGroupAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        reference.child(type).child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        fullUserList.clear();
                        progressBar.setVisibility(View.GONE);
                        if (snapshot.getChildrenCount() > 0) {
                            btnDelete.setVisibility(View.GONE);
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                                fullUserList.add(new UserModel(dataSnapshot.getKey(), userModel.getImage()));
                            }
                            adapter.notifyDataSetChanged();


                        } else {
                            btnDelete.setVisibility(View.VISIBLE);
                            Toast.makeText(UserInGroupScreen.this, "No account found", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        progressBar.setVisibility(View.GONE);
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
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final UserInGroupAdapter.ViewHolder holder, final int position) {

            final UserModel snapshot = fullUserList.get(position);
            try {
                String accNo = snapshot.getKey();
                final String imageUrl = snapshot.getImage();
                holder.account_number.setText(accNo);

                if (null != imageUrl && !imageUrl.equalsIgnoreCase(DEFAULT)) {
                    Picasso.with(UserInGroupScreen.this).load(imageUrl)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(holder.profileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(UserInGroupScreen.this).load(imageUrl)
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
                    String id = snapshot.getKey();
                    Toast.makeText(UserInGroupScreen.this, "" + id, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UserInGroupScreen.this, AccountDetailScreen.class)
                            .putExtra(ACCOUNT_ID, id));
                }
            });

            holder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    final String id = snapshot.getKey();
                    new AlertDialog.Builder(UserInGroupScreen.this)
                            .setMessage("Remove account no: '" + id + "' from group??")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    progressBar.setVisibility(View.VISIBLE);
                                    removeUserFromGroup(id, v, position);
                                }
                            }).setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                    return true;
                }
            });

        }

        private void removeUserFromGroup(String id, final View v, final int position) {
            reference.child(type).child(groupId).child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(v, "Removed ", Snackbar.LENGTH_SHORT).show();
                    fullUserList.remove(position);
                    notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UserInGroupScreen.this, "try again", Toast.LENGTH_SHORT).show();
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
                name.setText("Account Number");
                profileImage = itemView.findViewById(R.id.imageView);
                relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            }
        }
    }
}