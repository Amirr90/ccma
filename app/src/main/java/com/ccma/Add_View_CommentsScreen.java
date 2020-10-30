package com.ccma;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccma.Modals.ChatModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.kcode.bottomlib.BottomDialog;

import java.util.ArrayList;
import java.util.List;

import static com.ccma.Utility.Util.*;


public class Add_View_CommentsScreen extends AppCompatActivity {

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String AccountId;

    RecyclerView recyclerView;
    CommentAdapter commentAdapter;
    List<ChatModel> commentList = new ArrayList<>();
    CollectionReference commentRef;
    ProgressBar progressBar;
    TextView textView;
    String AccountNumber, Username, SanAmount, SanDate, Address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__view__comments_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_view_comment_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add/View Comments");

        progressBar = findViewById(R.id.progressBar5);
        textView = findViewById(R.id.textView5);
        AccountId = getIntent().getStringExtra(ACCOUNT_ID);
        AccountNumber = getIntent().getStringExtra(ACCOUNT_NUMBER);
        Username = getIntent().getStringExtra(USERNAME);
        SanAmount = getIntent().getStringExtra(SAN_AMOUNT);
        SanDate = getIntent().getStringExtra(SAN_DT);
        Address = getIntent().getStringExtra(ADDRESS);


        commentRef = firestore.collection(ACCOUNT_QUERY)
                .document(AccountId).collection(COMMENTS_QUERY);
        recyclerView = findViewById(R.id.comment_rec);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        commentAdapter = new CommentAdapter(commentList);
        recyclerView.setAdapter(commentAdapter);
        loadCommentData();
    }

    private void loadCommentData() {
        commentRef.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(Add_View_CommentsScreen.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e == null && null != queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                commentList.clear();
                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                    try {
                                        String comment = snapshot.getString(COMMENT);
                                        long timestamp = snapshot.getLong(TIMESTAMP);
                                        String email = snapshot.getString(EMAIL);
                                        String san_amount = snapshot.getString(SAN_AMOUNT);
                                        String san_date = snapshot.getString(SAN_DT);
                                        String address = snapshot.getString(SAN_DT);
                                        commentList.add(new ChatModel(comment, timestamp, email, san_amount, san_date, address));
                                    } catch (Exception e1) {
                                    }
                                }
                                textView.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                commentAdapter.notifyDataSetChanged();
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

    public void onClickFunctions(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.imageView4: {

                EditText et_text = findViewById(R.id.editTextTextPersonName2);
                String text = et_text.getText().toString();
                if (!text.isEmpty()) {
                    et_text.setText("");
                    sendComment(text, System.currentTimeMillis());

                }
            }
            break;
            case R.id.smart_suggestion: {
                final String items[] = new String[]{"Not responding",
                        "Address not found", "User Died", "Address Changed", "Switched off", "Mobile Number Changed"};
                BottomDialog dialog = BottomDialog.newInstance("Select Smart suggestions", "DISMISS",
                        items);
                dialog.show(getSupportFragmentManager(), "CMA");
                dialog.setListener(new BottomDialog.OnClickListener() {
                    @Override
                    public void click(int i) {
                        String text = items[i];
                        sendComment(text, System.currentTimeMillis());
                    }
                });

            }
            break;
        }
    }

    private void sendComment(String text, Long timestamp) {
        commentList.add(new ChatModel(text, System.currentTimeMillis(), getEmail(Add_View_CommentsScreen.this), SanAmount, SanDate, Address));
        commentAdapter.notifyDataSetChanged();
        commentRef.document("" + timestamp)
                .set(getCommentMap(text, timestamp, AccountNumber, Username, Add_View_CommentsScreen.this, SanAmount, SanDate, Address)).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Add_View_CommentsScreen.this, "comment not sent", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
        List<ChatModel> commentList;

        public CommentAdapter(List<ChatModel> commentList) {
            this.commentList = commentList;
        }

        @NonNull
        @Override
        public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.comment_view, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, final int position) {

            final ChatModel chatData = commentList.get(position);
            try {
                long timestamp = chatData.getTimestamp();
                holder.text.setText(chatData.getComment());
                holder.timestamp.setText(createDate(timestamp));

                if (position < commentList.size()) {

                    if (position == commentList.size() - 1) {
                        holder.date.setText(getDate(timestamp));
                        holder.date.setVisibility(View.VISIBLE);
                    } else {
                        int day1 = getDay(timestamp);
                        int day2 = getDay(commentList.get(position + 1).getTimestamp());
                        if (day1 != day2) {
                            holder.date.setText(getDate(timestamp));
                            holder.date.setVisibility(View.VISIBLE);
                        } else {
                            holder.date.setVisibility(View.GONE);

                        }
                    }
                }
            } catch (Exception e) {
            }

            holder.text.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final String commentId = String.valueOf(chatData.getTimestamp());
                    final CharSequence[] items = {"Un-send comments", "Delete All comments"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(Add_View_CommentsScreen.this);
                    builder.setTitle("Make your selection");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            dialog.dismiss();
                            if (item == 0) {
                                unSendComment(commentId, position);
                            } else if (item == 1) {
                                new AlertDialog.Builder(Add_View_CommentsScreen.this)
                                        .setTitle("Delete All comments!!!")
                                        .setMessage("All the comments will deleted permanently and can not be re-store again ")
                                        .setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                                deleteWholeComments(commentId);
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();

                            }

                        }
                    }).show();
                    return true;
                }
            });
        }

        private void deleteWholeComments(String commentId) {
            Toast.makeText(Add_View_CommentsScreen.this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }

        private void unSendComment(String commentId, final int position) {
            commentRef.document(commentId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    commentList.remove(position);
                    commentAdapter.notifyDataSetChanged();
                    Toast.makeText(Add_View_CommentsScreen.this, "Comment un-send successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Add_View_CommentsScreen.this, "error on deleting comment,try again", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return commentList.size();
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
}