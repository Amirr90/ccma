package com.ccma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccma.Modals.ChatModel;
import com.ccma.Modals.ChatModel2;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ccma.Utility.Util.MESSAGE;
import static com.ccma.Utility.Util.QUERY_CHAT;
import static com.ccma.Utility.Util.TIMESTAMP;
import static com.ccma.Utility.Util.USER;
import static com.ccma.Utility.Util.createDate;
import static com.ccma.Utility.Util.getChatMap;
import static com.ccma.Utility.Util.getDate;
import static com.ccma.Utility.Util.getDay;
import static com.ccma.Utility.Util.getEmail;
import static com.ccma.Utility.Util.getRecentChatMap;
import static com.google.common.net.HttpHeaders.FROM;

public class ChatWithUs extends AppCompatActivity {
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference chatRef;

    RecyclerView recyclerView;
    ChatAdapter chatAdapter;
    List<ChatModel2> chatList = new ArrayList<>();
    CollectionReference chatRef2;
    ProgressBar progressBar;
    TextView textView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_us);


        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_us_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Chat with us");

        chatRef = firestore.collection(QUERY_CHAT).document(getEmail(this));
        chatRef2 = chatRef.collection(MESSAGE);


        progressBar = findViewById(R.id.chat_us_progressbar);
        textView = findViewById(R.id.chat_us_tv);

        recyclerView = findViewById(R.id.chat_us_rec);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        chatAdapter = new ChatAdapter(chatList);
        recyclerView.setAdapter(chatAdapter);
        loadChatDataOnce();
        loadChatData();
    }

    private void loadChatDataOnce() {
        chatRef2.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                String comment = snapshot.getString(MESSAGE);
                                String from = snapshot.getString(FROM);
                                long timestamp = snapshot.getLong(TIMESTAMP);
                                chatList.add(new ChatModel2(comment, timestamp, from));
                            }
                            textView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            chatAdapter.notifyDataSetChanged();

                        } else {
                            textView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                textView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void loadChatData() {

        chatRef2.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener(ChatWithUs.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e != null && null != queryDocumentSnapshots.getDocumentChanges()
                                && !queryDocumentSnapshots.getDocumentChanges().isEmpty()) {
                            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();
                            for (DocumentChange change : documentChanges) {
                                DocumentChange.Type type = change.getType();
                                if (type == DocumentChange.Type.ADDED) {
                                    try {
                                        String from = change.getDocument().getString(FROM);
                                        String msg = change.getDocument().getString(MESSAGE);
                                        long timestamp = change.getDocument().getLong(MESSAGE);
                                        chatList.add(0, new ChatModel2(msg, timestamp, from));
                                    } catch (Exception e1) {
                                    }
                                }
                            }
                            chatAdapter.notifyDataSetChanged();
                        }

                    }
                });
    }


    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        List<ChatModel2> commentList;

        public ChatAdapter(List<ChatModel2> commentList) {
            this.commentList = commentList;
        }

        @NonNull
        @Override
        public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.comment_view, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, final int position) {

            final ChatModel2 chatData = commentList.get(position);
            try {
                long timestamp = chatData.getTimestamp();
                holder.text.setText(chatData.getComment());
                holder.timestamp.setText(createDate(timestamp));

                if (position <= commentList.size()) {

                    int day1 = getDay(timestamp);
                    int day2 = getDay(commentList.get(position + 1).getTimestamp());
                    if (day1 != day2) {
                        holder.date.setText(getDate(timestamp));
                        holder.date.setVisibility(View.VISIBLE);
                    } else {
                        holder.date.setVisibility(View.GONE);

                    }
                }

            } catch (Exception e) {
            }

            String from = chatData.getFrom();
            if (USER.equalsIgnoreCase(from)) {
                //msg from user
                holder.text.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_outline));
            } else {
                holder.text.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_outline_align_right));
            }


            holder.text.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final String commentId = String.valueOf(chatData.getTimestamp());
                    final CharSequence[] items = {"Un-send comments", "Delete All comments"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatWithUs.this);
                    builder.setTitle("Make your selection");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            dialog.dismiss();
                            if (item == 0) {
                                unSendComment(commentId, position);
                            } else if (item == 1) {
                                new AlertDialog.Builder(ChatWithUs.this)
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
            Toast.makeText(ChatWithUs.this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }

        private void unSendComment(String commentId, final int position) {
            chatRef2.document(commentId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    commentList.remove(position);
                    chatAdapter.notifyDataSetChanged();
                    Toast.makeText(ChatWithUs.this, "Comment un-send successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatWithUs.this, "error on deleting comment,try again", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return commentList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView text, timestamp, date;
            RelativeLayout layout;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(R.id.textView9);
                timestamp = itemView.findViewById(R.id.textView10);
                date = itemView.findViewById(R.id.textView8);
                layout = itemView.findViewById(R.id.my_chat_lay);

            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void chatUsScreenOnClick(View view) {

        int id = view.getId();
        switch (id) {
            case R.id.chat_us_send_btn: {

                EditText et_text = findViewById(R.id.chat_us__text);
                String text = et_text.getText().toString();
                if (!text.isEmpty()) {
                    et_text.setText("");
                    sendMessage(text, System.currentTimeMillis());

                }
            }
            break;
        }

    }

    private void sendMessage(final String text, long currentTimeMillis) {

        chatList.add(0, new ChatModel2(text, currentTimeMillis, USER));
        chatAdapter.notifyDataSetChanged();

        chatRef.set(getRecentChatMap(text, currentTimeMillis, ChatWithUs.this))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String doc_id = String.valueOf(System.currentTimeMillis());
                        chatRef.collection(MESSAGE).document(doc_id).set(getChatMap(text, doc_id)).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatWithUs.this, "msg not sent", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });
    }

}