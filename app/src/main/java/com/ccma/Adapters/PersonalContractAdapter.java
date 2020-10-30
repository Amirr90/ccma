package com.ccma.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccma.Add_View_CommentsScreen;
import com.ccma.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static com.ccma.Utility.Util.ACCOUNT_QUERY;
import static com.ccma.Utility.Util.COMMENTS_QUERY;
import static com.ccma.Utility.Util.TIMESTAMP;
import static com.ccma.Utility.Util.createDate;
import static com.ccma.Utility.Util.getDate;
import static com.ccma.Utility.Util.getDay;

public class PersonalContractAdapter extends RecyclerView.Adapter<PersonalContractAdapter.ViewHolder> {
    private static final String TAG = "PersonalContractAdapter";
    List<DocumentSnapshot> snapshots;
    Context context;
    String Query;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    CollectionReference commentRef;
    String AccountId;

    public PersonalContractAdapter(List<DocumentSnapshot> snapshots, Context context, String query, String AccountId) {
        this.snapshots = snapshots;
        this.context = context;
        Query = query;
        this.AccountId = AccountId;
        commentRef = firestore.collection(ACCOUNT_QUERY)
                .document(AccountId).collection(Query);
    }

    @NonNull
    @Override
    public PersonalContractAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.personal_contact_view, parent, false);
        PersonalContractAdapter.ViewHolder viewHolder = new PersonalContractAdapter.ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PersonalContractAdapter.ViewHolder holder, final int position) {

        final DocumentSnapshot chatData = snapshots.get(position);
        try {


            long timestamp = Long.parseLong(chatData.getString(TIMESTAMP));
            ArrayList<String> suggestions = (ArrayList<String>) chatData.getData().get("data");
            StringBuilder builder = new StringBuilder();
            if (suggestions == null) {
                suggestions = new ArrayList<>();
            }
            for (int a = 0; a < suggestions.size(); a++) {
                if (suggestions.size() == 1) {
                    builder.append(suggestions.get(a));
                } else {
                    builder.append("" + (a + 1) + ". " + suggestions.get(a));
                    if (a != suggestions.size() - 1)
                        builder.append("\n");
                }
            }
            holder.text.setText(builder.toString());
            holder.timestamp.setText(createDate(timestamp));
            if (position < snapshots.size()) {
                if (position == snapshots.size() - 1) {
                    holder.date.setText(getDate(timestamp));
                    holder.date.setVisibility(View.VISIBLE);
                } else {
                    int day1 = getDay(timestamp);
                    int day2 = getDay(Long.parseLong(snapshots.get(position + 1).getString(TIMESTAMP)));
                    if (day1 != day2) {
                        holder.date.setText(getDate(timestamp));
                        holder.date.setVisibility(View.VISIBLE);
                    } else {
                        holder.date.setVisibility(View.GONE);

                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "error " + e.getLocalizedMessage());
        }


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final String commentId = chatData.getId();
                final CharSequence[] items = {"Un-send comments"};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Make your selection");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        unSendComment(commentId, position);


                    }
                }).show();
                return true;
            }
        });
    }

    private void unSendComment(String commentId, final int position) {
        commentRef.document(commentId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                snapshots.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "un-send successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "error on deleting comment,try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return snapshots.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView text, timestamp, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.textView9);
            timestamp = itemView.findViewById(R.id.textView10);
            date = itemView.findViewById(R.id.textView8);
        }
    }
}

