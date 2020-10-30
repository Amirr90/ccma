package com.ccma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccma.Adapters.AllAccountHolderAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.List;

import static com.ccma.Utility.Util.ACCOUNT_NUMBER;
import static com.ccma.Utility.Util.ACCOUNT_QUERY;
import static com.ccma.Utility.Util.DEFAULT;
import static com.ccma.Utility.Util.EMAIL;
import static com.ccma.Utility.Util.GROUP_ID;
import static com.ccma.Utility.Util.IMAGE;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.TAG2;
import static com.ccma.Utility.Util.TYPE;
import static com.ccma.Utility.Util.getEmail;
import static com.ccma.Utility.Util.hideKeyboard;
import static com.ccma.Utility.Util.setTotalAccountCount;

public class AddUserInGroupScreen extends AppCompatActivity {
    public static TextInputEditText searchEditText;
    int a = 0;
    ProgressBar progressBar;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    List<DocumentSnapshot> fullUserList = new ArrayList<>();
    RecyclerView recyclerView;
    AddUserInGroupAdapter adapter;
    String gId, type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_in_group_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_user_account_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Account To Group");


        gId = getIntent().getStringExtra(GROUP_ID);
        type = getIntent().getStringExtra(TYPE);
        searchEditText = findViewById(R.id.searche_edit_text2);
        recyclerView = findViewById(R.id.home_rec2);
        progressBar = findViewById(R.id.progressBar22);

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

        setTotalAccountCount(searchEditText, this);


    }

    private void setRec() {
        adapter = new AddUserInGroupAdapter(fullUserList, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void loadData(CharSequence charSequence) {
        long acc_num;
        try {
            acc_num = Long.parseLong(charSequence.toString());
            progressBar.setVisibility(View.VISIBLE);
            final Query query = firestore.collection(ACCOUNT_QUERY)
                    .whereEqualTo(EMAIL, getEmail(AddUserInGroupScreen.this))
                    .orderBy(ACCOUNT_NUMBER)
                    .startAt(acc_num)
                    .endAt(acc_num + "\uf8ff")
                    .limit(5);


            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    progressBar.setVisibility(View.GONE);
                    if (e != null && null != queryDocumentSnapshots && queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(AddUserInGroupScreen.this, "failed to get User " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        fullUserList.clear();
                        fullUserList.addAll(queryDocumentSnapshots.getDocuments());
                    }
                    adapter.notifyDataSetChanged();

                }
            });
        } catch (Exception e) {
            Log.d(TAG2, "error " + e.getLocalizedMessage());
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private class AddUserInGroupAdapter extends RecyclerView.Adapter<AddUserInGroupAdapter.ViewHolder> {
        List<DocumentSnapshot> accountList;
        Activity context;
        ProgressDialog progressDialog;

        public AddUserInGroupAdapter(List<DocumentSnapshot> accountList, Activity context) {
            this.accountList = accountList;
            this.context = context;
        }

        @NonNull
        @Override
        public AddUserInGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.home_view, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Deleting account,Please wait...");
            progressDialog.setCancelable(false);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final AddUserInGroupAdapter.ViewHolder holder, int position) {

            final DocumentSnapshot user = accountList.get(position);

            try {
                long acc_number = user.getLong(ACCOUNT_NUMBER);
                holder.account_number.setText("" + acc_number);
                holder.name.setText(user.getString(NAME));


                final String imageUrl = user.getString(IMAGE);
                if (null != imageUrl && !imageUrl.equalsIgnoreCase(DEFAULT)) {
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
                Toast.makeText(context, "error in loading  data\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    hideKeyboard(AddUserInGroupScreen.this);
                    final String id = user.getId();
                    String userName = user.getString(NAME);
                    new AlertDialog.Builder(AddUserInGroupScreen.this)
                            .setMessage("Add " + userName + " to group " + gId)
                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    progressDialog.setMessage("Adding Account, Please wait...");
                                    progressDialog.show();
                                    addAccountToGroup(id, user, v);
                                }
                            }).setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            });
        }

        private void addAccountToGroup(final String id, final DocumentSnapshot user, final View v) {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(type).child(gId);

            //check Already added Status
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(id)) {
                        progressDialog.dismiss();
                        Snackbar.make(v, "Already Added", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    reference.child(id).setValue(user.getData()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Snackbar.make(v, "Added Successfully", Snackbar.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(context, "try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Toast.makeText(context, "try again", Toast.LENGTH_SHORT).show();
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
}