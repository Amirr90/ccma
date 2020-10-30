package com.ccma.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccma.AccountDetailScreen;
import com.ccma.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.ccma.Utility.Util.ACCOUNT_ID;
import static com.ccma.Utility.Util.ACCOUNT_NUMBER;
import static com.ccma.Utility.Util.ACCOUNT_QUERY;
import static com.ccma.Utility.Util.DEFAULT;
import static com.ccma.Utility.Util.DEFAULT2;
import static com.ccma.Utility.Util.EXTRA_COLUMN_1;
import static com.ccma.Utility.Util.FAMILY_GROUPS_QUERY;
import static com.ccma.Utility.Util.IMAGE;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.SELF_GROUPS_QUERY;
import static com.ccma.Utility.Util.hideKeyboard;

public class AllAccountHolderAdapter extends RecyclerView.Adapter<AllAccountHolderAdapter.ViewHolder> {

    List<DocumentSnapshot> accountList;
    Activity context;
    ProgressDialog progressDialog;

    public AllAccountHolderAdapter(List<DocumentSnapshot> accountList, Activity context) {
        this.accountList = accountList;
        this.context = context;
    }

    @NonNull
    @Override
    public AllAccountHolderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.home_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting account,Please wait...");
        progressDialog.setCancelable(false);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AllAccountHolderAdapter.ViewHolder holder, final int position) {

        final DocumentSnapshot user = accountList.get(position);

        try {
            // long acc_number = user.getLong(ACCOUNT_NUMBER);
            if (null != user.getLong(ACCOUNT_NUMBER) && user.getLong(ACCOUNT_NUMBER) != 0) {
                holder.account_number.setText("" + user.getLong(ACCOUNT_NUMBER));
            } else {
                holder.account_number.setText(user.getString(EXTRA_COLUMN_1));
            }

            //holder.account_number.setText("" + acc_number);
            holder.name.setText(user.getString(NAME));


            final String imageUrl = user.getString(IMAGE);
            if (null != imageUrl && !imageUrl.equalsIgnoreCase(DEFAULT)
                    && !imageUrl.equalsIgnoreCase(DEFAULT2)) {
                Picasso.with(context).load(imageUrl)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.logooo)
                        .into(holder.profileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(context).load(imageUrl)
                                        .placeholder(R.drawable.logooo).into(holder.profileImage);
                            }
                        });
            } else {
                holder.profileImage.setImageResource(R.drawable.logooo);
            }
        } catch (Exception e) {
            Toast.makeText(context, "error in home rec\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String acc_id = user.getId();
                context.startActivity(new Intent(context, AccountDetailScreen.class)
                        .putExtra(ACCOUNT_ID, acc_id));
            }
        });

        holder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context).setTitle("Delete Account??")
                        .setMessage("This Account will delete permanently and won't be re-store again!!!")
                        .setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                hideKeyboard(context);
                                progressDialog.show();
                                final String id = user.getId();
                                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                firebaseFirestore.collection(ACCOUNT_QUERY)
                                        .document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                        accountList.remove(position);
                                        notifyDataSetChanged();
                                        removeFromGroups(id);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "try again", Toast.LENGTH_SHORT).show();
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

    private void removeFromGroups(final String id) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        //Removing from Family Groups
        reference.child(FAMILY_GROUPS_QUERY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.child(id).exists()) {
                        String grpKey = snapshot1.getKey();
                        String acc_key = snapshot1.child(id).getKey();
                        reference.child(FAMILY_GROUPS_QUERY).child(grpKey).child(acc_key).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Removing from SELF Groups
        reference.child(SELF_GROUPS_QUERY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.child(id).exists()) {
                        String grpKey = snapshot1.getKey();
                        String acc_key = snapshot1.child(id).getKey();
                        reference.child(SELF_GROUPS_QUERY).child(grpKey).child(acc_key).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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