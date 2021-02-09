package com.ccma.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
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
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
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

import java.util.Arrays;
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

    private static final String TAG = "AllAccountHolderAdapter";
    List<DocumentSnapshot> accountList;
    Activity context;
    ProgressDialog progressDialog;

    RewardedAd mRewardedAd;
    //Date 09-02-2021

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


                setRewardAdd(user);

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

    private void setRewardAdd(final DocumentSnapshot user) {

        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("EAE61BEA656088A6BE28C25EDB1A5A2F")).build();
        MobileAds.setRequestConfiguration(configuration);

        AdRequest adRequest = new AdRequest.Builder().build();
        Log.d(TAG, "setRewardAdd: Mode " + adRequest.isTestDevice(context));
        RewardedAd.load(context, "ca-app-pub-8024475397859122/2083215585",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;

                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "onAdFailedToLoad " + rewardedAd.getRewardItem());
                    }
                });

        if (null != mRewardedAd) {
            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d(TAG, "Ad was shown.");
                    mRewardedAd = null;
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {

                    Log.d(TAG, "Ad failed to show.");
                    String acc_id = user.getId();
                    context.startActivity(new Intent(context, AccountDetailScreen.class)
                            .putExtra(ACCOUNT_ID, acc_id));
                }

                @Override
                public void onAdDismissedFullScreenContent() {

                    Log.d(TAG, "Ad was dismissed.");
                    String acc_id = user.getId();
                    context.startActivity(new Intent(context, AccountDetailScreen.class)
                            .putExtra(ACCOUNT_ID, acc_id));
                }
            });
        }


        if (mRewardedAd != null) {
            Activity activityContext = context;
            mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d("TAG", "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    Log.d(TAG, "onUserEarnedReward: " + rewardAmount);
                    Log.d(TAG, "onUserEarnedRewardType: " + rewardType);
                }
            });
        } else {
            String acc_id = user.getId();
            context.startActivity(new Intent(context, AccountDetailScreen.class)
                    .putExtra(ACCOUNT_ID, acc_id));
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
        }

    }
}
