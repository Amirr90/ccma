package com.ccma;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccma.Modals.SelectedItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ccma.Utility.Util.ACCOUNT_ID;
import static com.ccma.Utility.Util.ACCOUNT_NUMBER;
import static com.ccma.Utility.Util.ACCOUNT_QUERY;
import static com.ccma.Utility.Util.ADDRESS;
import static com.ccma.Utility.Util.DATA;
import static com.ccma.Utility.Util.EMAIL;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.QUERY_PERSONAL_CONTACT;
import static com.ccma.Utility.Util.QUERY_PHONE_CONTACT;
import static com.ccma.Utility.Util.RESPONSE;
import static com.ccma.Utility.Util.SAN_AMOUNT;
import static com.ccma.Utility.Util.SAN_DT;
import static com.ccma.Utility.Util.TIMESTAMP;
import static com.ccma.Utility.Util.USERNAME;
import static com.ccma.Utility.Util.getCurrencyBalance;
import static com.ccma.Utility.Util.getEmail;

public class SuggestionScreen extends AppCompatActivity {

    ArrayList<String> list;
    String AccountId;

    RecyclerView recyclerView;
    SmartSuggestionAdapter adapter;
    Toolbar toolbar;
    ArrayList<SelectedItem> selectedItems = new ArrayList<>();
    TextView titleTv;
    ImageView next;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    CollectionReference reference;

    ProgressDialog progressDialog;
    String AccountNumber, Username, SanAmount, SanDate, Address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion_screen);

        toolbar = (Toolbar) findViewById(R.id.suggestion_toolbar);
        setToolbar(toolbar, "Select 1 or more");
        titleTv = (TextView) toolbar.findViewById(R.id.title_bar);
        next = (ImageView) toolbar.findViewById(R.id.next_icon);

        AccountId = getIntent().getStringExtra(ACCOUNT_ID);
        AccountNumber = getIntent().getStringExtra(ACCOUNT_NUMBER);
        Username = getIntent().getStringExtra(USERNAME);
        SanAmount = getIntent().getStringExtra(SAN_AMOUNT);
        SanDate = getIntent().getStringExtra(SAN_DT);
        Address = getIntent().getStringExtra(ADDRESS);

        progressDialog = new ProgressDialog(this);
        list = getIntent().getStringArrayListExtra("data");
        reference = firestore.collection(ACCOUNT_QUERY)
                .document(AccountId).collection(QUERY_PERSONAL_CONTACT);

        recyclerView = findViewById(R.id.suggestion_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        if (null == list) {
            list = new ArrayList<>();
        }
        for (String s : list) {
            selectedItems.add(new SelectedItem(s, false));
        }
        adapter = new SmartSuggestionAdapter(selectedItems);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


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
        getSupportActionBar().setTitle(id);
    }

    private class SmartSuggestionAdapter extends RecyclerView.Adapter<SmartSuggestionAdapter.ViewHolder> {
        ArrayList<SelectedItem> list;

        public SmartSuggestionAdapter(ArrayList<SelectedItem> list) {
            this.list = list;

        }

        @NonNull
        @Override
        public SmartSuggestionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.suggestion_view, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull SmartSuggestionAdapter.ViewHolder holder, final int position) {

            final int po = position + 1;
            final SelectedItem item = list.get(position);
            holder.textView.setText("" + po + ". " + list.get(position).getText());

            boolean isSelected = item.isSelected();

            if (isSelected) {
                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.light_green));
            } else {
                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.white));

            }
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = item.getText();
                    if (position == 11 || position == 10) {
                        if (item.isSelected()) {
                            list.remove(position);
                            list.add(position, new SelectedItem(text, false));
                            updateCheckedCount();
                        } else {
                            showEditSuggestionDialog(position);
                        }
                    } else {
                        if (item.isSelected()) {
                            list.remove(position);
                            list.add(position, new SelectedItem(text, false));
                            updateCheckedCount();
                        } else {
                            list.remove(position);
                            list.add(position, new SelectedItem(text, true));
                            updateCheckedCount();
                        }
                    }
                    notifyDataSetChanged();
                }
            });
        }

        private void showEditSuggestionDialog(final int position) {

            if (position == 10) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SuggestionScreen.this);
                LayoutInflater inflater = SuggestionScreen.this.getLayoutInflater();
                View view = inflater.inflate(R.layout.suggestion_edit_view, null);
                final TextInputEditText amount = view.findViewById(R.id.suggestion_et_amount);
                final TextInputEditText days = view.findViewById(R.id.suggestion_et_day);
                days.setVisibility(View.VISIBLE);

                builder.setView(view)
                        .setTitle("Add recovery amount and days")
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String Amount = amount.getText().toString();
                                String Days = days.getText().toString();
                                String text = "Promise to pay " + getCurrencyBalance(Double.parseDouble(Amount)) + " within " + Days + " days";
                                if (!Amount.isEmpty() && !Days.isEmpty()) {
                                    list.remove(position);
                                    list.add(position, new SelectedItem(text, true));
                                    updateCheckedCount();
                                    notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(SuggestionScreen.this);
                LayoutInflater inflater = SuggestionScreen.this.getLayoutInflater();
                View view = inflater.inflate(R.layout.suggestion_edit_view, null);
                final TextInputEditText amount = view.findViewById(R.id.suggestion_et_amount);
                builder.setView(view)
                        .setTitle("Add recovery amount")
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String Amount = amount.getText().toString();
                                String text = "" + getCurrencyBalance(Double.parseDouble(Amount)) + " is received as recovery";
                                if (!Amount.isEmpty()) {
                                    list.remove(position);
                                    list.add(position, new SelectedItem(text, true));
                                    updateCheckedCount();
                                    notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .show();
            }
        }


        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            LinearLayout linearLayout;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textView12);
                linearLayout = itemView.findViewById(R.id.suggestion_lay);
            }
        }

        private void updateCheckedCount() {
            int counter = 0;
            for (SelectedItem student : list) {
                if (student.isSelected()) {
                    counter++;
                }
            }
            if (counter == 0) {
                next.setVisibility(View.GONE);
            } else {
                next.setVisibility(View.VISIBLE);
            }
            titleTv.setText("Selected " + counter);
        }
    }

    public void updateSelectedSuggestion(View view) {
        final List<String> listToUpload = new ArrayList<>();

        if (view.getId() == R.id.next_icon) {
            for (SelectedItem item : selectedItems) {
                if (item.isSelected()) {
                    listToUpload.add(item.getText());
                }
            }

            final CharSequence[] items = new CharSequence[listToUpload.size()];
            for (int a = 0; a < listToUpload.size(); a++) {
                items[a] = listToUpload.get(a);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(SuggestionScreen.this);
            builder.setTitle(items.length + " Smart Suggestion Selected");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {

                }
            }).setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    progressDialog.setMessage("Uploading, Please wait...");
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    Map<String, Object> map = new HashMap<>();
                    map.put(DATA, listToUpload);
                    map.put(TIMESTAMP, timestamp);
                    map.put(EMAIL, getEmail(SuggestionScreen.this));
                    map.put(ACCOUNT_NUMBER, AccountNumber);
                    map.put(NAME, Username);
                    map.put(SAN_AMOUNT, SanAmount);
                    map.put(ADDRESS, Address);
                    map.put(SAN_DT, SanDate);


                    reference.document(timestamp).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "onClick: Uploaded");
                            progressDialog.dismiss();
                            Intent intent = new Intent();
                            intent.putExtra(RESPONSE, "Updated successfully");
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Intent intent = new Intent();
                            intent.putExtra(RESPONSE, "failed to Update,try again");
                            setResult(RESULT_OK, intent);
                            finish();
                            Log.d("TAG", "onClick: Error");
                        }
                    });
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();


        }
    }

}