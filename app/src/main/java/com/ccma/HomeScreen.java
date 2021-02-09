package com.ccma;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccma.Adapters.AllAccountHolderAdapter;
import com.ccma.Modals.Demomodel;
import com.ccma.Modals.ExcelModel;
import com.ccma.Utility.Api;
import com.ccma.Utility.ApiUtils;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.ccma.Utility.Util.ACCOUNT_ID;
import static com.ccma.Utility.Util.ACCOUNT_NUMBER;
import static com.ccma.Utility.Util.ACCOUNT_QUERY;
import static com.ccma.Utility.Util.ADDRESS;
import static com.ccma.Utility.Util.ADDRESS_1;
import static com.ccma.Utility.Util.ADDRESS_2;
import static com.ccma.Utility.Util.BANK_NAME;
import static com.ccma.Utility.Util.BRANCH_NAME;
import static com.ccma.Utility.Util.DEFAULT;
import static com.ccma.Utility.Util.DEMO_QUERY;
import static com.ccma.Utility.Util.EMAIL;
import static com.ccma.Utility.Util.EXTRA_COLUMN_1;
import static com.ccma.Utility.Util.EXTRA_COLUMN_2;
import static com.ccma.Utility.Util.FAMILY_GROUPS_QUERY;
import static com.ccma.Utility.Util.IMAGE;
import static com.ccma.Utility.Util.IS_ACTIVE;
import static com.ccma.Utility.Util.LAT;
import static com.ccma.Utility.Util.LAT2;
import static com.ccma.Utility.Util.LONG;
import static com.ccma.Utility.Util.LONG2;
import static com.ccma.Utility.Util.MESSAGE;
import static com.ccma.Utility.Util.MOB_NUM_1;
import static com.ccma.Utility.Util.MOB_NUM_2;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.PROJECT;
import static com.ccma.Utility.Util.QUERY_CHAT;
import static com.ccma.Utility.Util.RESPONSE;
import static com.ccma.Utility.Util.SAN_AMOUNT;
import static com.ccma.Utility.Util.SAN_DT;
import static com.ccma.Utility.Util.SELF_GROUPS_QUERY;
import static com.ccma.Utility.Util.TAG2;
import static com.ccma.Utility.Util.TIMESTAMP;
import static com.ccma.Utility.Util.TYPE;
import static com.ccma.Utility.Util.getEmail;
import static com.ccma.Utility.Util.getSHA512;
import static com.ccma.Utility.Util.hideKeyboard;
import static com.ccma.Utility.Util.logout;
import static com.ccma.Utility.Util.setTotalAccountCount;
import static com.ccma.Utility.Util.showToast;
import static com.google.firebase.firestore.DocumentChange.Type.MODIFIED;
import static com.otaliastudios.cameraview.CameraView.PERMISSION_REQUEST_CODE;

public class HomeScreen extends AppCompatActivity {


    private static final int PICK_EXCEL_FILE_REQ_CODE = 1001;
    private static final int REQ_CODE_ADD_NEW_ACCOUNT = 1002;
    private static final String TAG = "HomeScreen";
    public static TextInputEditText searchEditText;
    private RecyclerView recyclerView;
    int a = 0;
    ProgressBar progressBar;
    AdView mAdView;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    AllAccountHolderAdapter adapter;
    List<DocumentSnapshot> fullUserList = new ArrayList<>();
    CollectionReference excelRef;

    String FILE_NAME = "account_sample.xls";
    TextView textCartItemCount;
    int mCartItemCount = 0;
    RelativeLayout uploadExcelLayout;
    ProgressDialog loading;
    public TextView textView;
    String msg;
    ConstraintLayout addLayout;
    FloatingActionMenu floatingActionMenu;
    FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;
    RewardedAd mRewardedAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);


        msg = "Drop a mail to sigmasoftsolutions1@gmail.com for excel/bulk uploads of customer data";
        Toolbar toolbar = (Toolbar) findViewById(R.id.home_screen_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("CCNA");

        searchEditText = findViewById(R.id.searche_edit_text);
        recyclerView = findViewById(R.id.home_rec);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView20);
        floatingActionMenu = findViewById(R.id.menu);
        uploadExcelLayout = findViewById(R.id.uploadExcelLayout);
        addLayout = findViewById(R.id.constraintLay);
        excelRef = firestore.collection(DEMO_QUERY);

        String email = "cpkhatriiitd07@gmail.com";
        //updateAccount(email);

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

        floatingActionButton1 = findViewById(R.id.menu_item1);
        floatingActionButton2 = findViewById(R.id.menu_item2);
        floatingActionButton3 = findViewById(R.id.menu_item3);

        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);
                setRewardAdd();

            }
        });

        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                floatingActionMenu.close(true);
                final CharSequence[] items = {"Download Sample file"};
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
                builder.setTitle("Make your selection");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        if (item == 0) {
                            downloadAddAccountFile();
                            Snackbar.make(v, msg, Snackbar.LENGTH_SHORT)
                                    .setAction("CHAT WITH US", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            startActivity(new Intent(HomeScreen.this, ChatWithUs.class));
                                        }
                                    }).setActionTextColor(getResources().getColor(R.color.white))
                                    .show();
                        } else downloadAddAccountStatusFile();

                    }
                }).show();


            }
        });

        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                floatingActionMenu.close(true);
                final CharSequence[] items = {"Download Sample file"};
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
                builder.setTitle("Make your selection");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        if (item == 0) {
                            downloadAddAccountFile();
                            Snackbar.make(v, msg, Snackbar.LENGTH_SHORT)
                                    .setAction("CHAT WITH US", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            startActivity(new Intent(HomeScreen.this, ChatWithUs.class));
                                        }
                                    }).setActionTextColor(getResources().getColor(R.color.white))
                                    .show();
                        } else downloadAddAccountStatusFile();

                    }
                }).show();
            }
        });


        //updateData();

        // delete();


    }


    private void setRewardAdd() {

        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("EAE61BEA656088A6BE28C25EDB1A5A2F")).build();
        MobileAds.setRequestConfiguration(configuration);

        AdRequest adRequest = new AdRequest.Builder().build();
        Log.d(TAG, "setRewardAdd: Mode " + adRequest.isTestDevice(HomeScreen.this));
        RewardedAd.load(HomeScreen.this, "ca-app-pub-8024475397859122/2083215585",
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
                    Intent intent = new Intent(HomeScreen.this, AddNewAccountScreen.class);
                    startActivityForResult(intent, REQ_CODE_ADD_NEW_ACCOUNT);
                }

                @Override
                public void onAdDismissedFullScreenContent() {

                    Log.d(TAG, "Ad was dismissed.");
                    Intent intent = new Intent(HomeScreen.this, AddNewAccountScreen.class);
                    startActivityForResult(intent, REQ_CODE_ADD_NEW_ACCOUNT);
                }
            });
        }


        if (mRewardedAd != null) {
            Activity activityContext = HomeScreen.this;
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
            Intent intent = new Intent(HomeScreen.this, AddNewAccountScreen.class);
            startActivityForResult(intent, REQ_CODE_ADD_NEW_ACCOUNT);
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
        }

    }

    private void updateAccount(String email) {
        firestore.collection("Account_Holders")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (null == queryDocumentSnapshots)
                            return;
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("EXTRA_COLUMN_1", "" + snapshot.getLong("AC_No"));
                            if (snapshot.getLong("AC_No") != 0)
                                firestore.collection("Account_Holders")
                                        .document(snapshot.getId())
                                        .update(map);
                        }

                    }
                });
    }


    private void delete() {
        firestore.collection("Account_Holders")
                .whereEqualTo("email", "cpkhatriiitd07@gmail.com")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot snapshot : queryDocumentSnapshots)
                    firestore.collection("Account_Holders").document(snapshot.getId()).delete();
                Toast.makeText(HomeScreen.this, "Deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateData() {
        byte[] email = getEmail(HomeScreen.this).getBytes();
        byte[] hash;
        try {
            hash = getSHA512("SHA-512", email);
            BigInteger shaData = new BigInteger(1, hash);
            Api api = ApiUtils.getAPIService();
            Call<Demomodel> call = api.getAccounts(getEmail(HomeScreen.this), shaData.toString(16));
            call.enqueue(new Callback<Demomodel>() {
                @Override
                public void onResponse(Call<Demomodel> call, Response<Demomodel> response) {
                    Toast.makeText(HomeScreen.this, "" + response.body().getResponseMsg(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Demomodel> call, Throwable t) {

                    Toast.makeText(HomeScreen.this, "Api failed to hit \n" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Toast.makeText(this, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void setRec() {
        adapter = new AllAccountHolderAdapter(fullUserList, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        firestore.collection(ACCOUNT_QUERY)
                .whereEqualTo(EMAIL, getEmail(this))
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        progressBar.setVisibility(View.GONE);

                        if (queryDocumentSnapshots != null) {
                            fullUserList.clear();
                            fullUserList.addAll(queryDocumentSnapshots.getDocuments());
                        }
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "" + e.getLocalizedMessage());
            }
        });
    }

    private void loadData(CharSequence charSequence) {
        String acc_num;
        try {
            acc_num = charSequence.toString();
            progressBar.setVisibility(View.VISIBLE);
            Query query = firestore.collection(ACCOUNT_QUERY)
                    .whereEqualTo(EMAIL, getEmail(this))
                    .orderBy(EXTRA_COLUMN_1)
                    .startAt(acc_num)
                    .endAt(acc_num + "\uf8ff")
                    .limit(30);


            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    progressBar.setVisibility(View.GONE);
                    if (e != null && null != queryDocumentSnapshots && queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(HomeScreen.this, "failed to get User " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    textView.setVisibility(View.GONE);

                    if (queryDocumentSnapshots != null) {
                        fullUserList.clear();
                        fullUserList.addAll(queryDocumentSnapshots.getDocuments());
                        if (null != adapter)
                            adapter.notifyDataSetChanged();
                    }


                }
            });
        } catch (Exception e) {
            Log.d(TAG2, "error " + e.getLocalizedMessage());
            Toast.makeText(this, "error " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public void homeScreenClickEvents(final View view) {
        int id = view.getId();
        switch (id) {
            case R.id.floatingActionButton: {

                final CharSequence[] items = {"Add Account manually", "Add Account via excel file"};
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
                builder.setTitle("Make your selection");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        if (item == 0) {
                            Intent intent = new Intent(HomeScreen.this, AddNewAccountScreen.class);
                            startActivityForResult(intent, REQ_CODE_ADD_NEW_ACCOUNT);
                        } else {
                            getExcelFile(view);
                        }

                    }
                }).show();

            }
            break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        addLayout.setVisibility(View.GONE);
    }

    public void getExcelFile(final View view) {
        final CharSequence[] items = {"Download Sample file"};
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                if (item == 0) {
                    if (checkPermission()) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                        startActivityForResult(intent, PICK_EXCEL_FILE_REQ_CODE);


                    } else
                        Snackbar.make(view, "Permission required", Snackbar.LENGTH_SHORT).setAction("ENABLE PERMISSION", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestPermission();
                            }
                        }).show();

                } else downloadAddAccountStatusFile();

            }
        }).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        final MenuItem menuItem = menu.findItem(R.id.menuChatWithUs);
        View actionView = menuItem.getActionView();
        textCartItemCount = (TextView) actionView.findViewById(R.id.cart_badge);

        setupBadge();

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void setupBadge() {

        if (textCartItemCount != null) {
            firestore.collection(QUERY_CHAT).document(getEmail(HomeScreen.this)).collection(MESSAGE)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e == null) {
                                if (null != queryDocumentSnapshots.getDocumentChanges() && !queryDocumentSnapshots.isEmpty()) {
                                    List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();
                                    for (DocumentChange change : documentChanges) {
                                        DocumentChange.Type type = change.getType();
                                        if (type == MODIFIED) {
                                            mCartItemCount = mCartItemCount + 1;
                                        }
                                    }
                                    if (mCartItemCount == 0) {
                                        if (textCartItemCount.getVisibility() != View.GONE) {
                                            textCartItemCount.setVisibility(View.GONE);
                                        }
                                    } else {
                                        textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount, 99)));
                                        if (textCartItemCount.getVisibility() != View.VISIBLE) {
                                            textCartItemCount.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }
                        }
                    });

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuPrintDaily: {
                startActivity(new Intent(HomeScreen.this, PrintScreen.class));
            }
            break;
            case R.id.menuProfile: {
                mCartItemCount = 0;
                textCartItemCount.setText("0");
                textCartItemCount.setVisibility(View.GONE);
                startActivity(new Intent(HomeScreen.this, Profile.class));

            }
            break;
            case R.id.menuChatWithUs:
                startActivity(new Intent(HomeScreen.this, ChatWithUs.class));
                break;

            case R.id.menuRefresh:
                uploadExcelLayout.setVisibility(View.VISIBLE);
                // refreshData2();
                break;

            case R.id.menuLogout:
                logout(false, HomeScreen.this);
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeScreen.this, LoginScreen.class));
                finish();
                break;
            case R.id.menuFilter:
                filterDialog();
                break;

            case R.id.menuSearchByPhone:
                searchByPhoneDialog();
                break;
            case R.id.menuItemDownloadFile: {
                final CharSequence[] items = {"Download Sample file"};
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
                builder.setTitle("Make your selection");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        if (item == 0) {
                            downloadAddAccountFile();
                        } else downloadAddAccountStatusFile();

                    }
                }).show();
                //downloadAddAccountFile();
            }

            break;

            case R.id.menuSearchByName:
                searchByNameDialog();
                break;
            case R.id.menuSelfGroups:
                startActivity(new Intent(HomeScreen.this, GroupsScreen.class)
                        .putExtra(TYPE, SELF_GROUPS_QUERY));
                break;
            case R.id.menuFamilyGroups:
                startActivity(new Intent(HomeScreen.this, GroupsScreen.class)
                        .putExtra(TYPE, FAMILY_GROUPS_QUERY));
                break;
            case R.id.menuContactUs:
                startActivity(new Intent(HomeScreen.this, ContactUs.class));
                break;
            default: {
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();

            }
        }
        return true;
    }

    private void searchByNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);

        // get the layout inflater
        LayoutInflater inflater = HomeScreen.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.update_lay, null);
        final TextInputEditText inputEditText = view.findViewById(R.id.update_et);
        builder.setMessage("Enter Borrower's name");
        builder.setView(view)
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        final String text = inputEditText.getText().toString();
                        if (!text.isEmpty()) {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText("");
                            uploadExcelLayout.setVisibility(View.VISIBLE);
                            firestore.collection(ACCOUNT_QUERY)
                                    .whereEqualTo(EMAIL, getEmail(HomeScreen.this))
                                    .orderBy(NAME)
                                    .startAt(text)
                                    .endAt(text + "\uf8ff")
                                    .limit(5)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            uploadExcelLayout.setVisibility(View.GONE);
                                            if (null != queryDocumentSnapshots && !queryDocumentSnapshots.isEmpty()) {
                                                fullUserList.clear();
                                                fullUserList.addAll(queryDocumentSnapshots.getDocuments());
                                                adapter.notifyDataSetChanged();
                                                setFilterCountToTextView(fullUserList.size());
                                            } else
                                                Toast.makeText(HomeScreen.this, "no user found", Toast.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    uploadExcelLayout.setVisibility(View.GONE);
                                    Log.d(TAG, "error " + e.getLocalizedMessage());
                                    showToast("try again" + e.getLocalizedMessage(), HomeScreen.this);
                                }
                            });
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // remove the dialog from the screen
                    }
                })
                .show();
    }

    private void searchByPhoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);

        // get the layout inflater
        LayoutInflater inflater = HomeScreen.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.update_lay, null);
        final TextInputEditText inputEditText = view.findViewById(R.id.update_et);
        builder.setMessage("Enter mobile number");
        builder.setView(view)
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        final String text = inputEditText.getText().toString();
                        if (!text.isEmpty()) {
                            hideKeyboard(HomeScreen.this);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText("");
                            uploadExcelLayout.setVisibility(View.VISIBLE);
                            firestore.collection(ACCOUNT_QUERY)
                                    .whereEqualTo(EMAIL, getEmail(HomeScreen.this))
                                    .whereEqualTo(MOB_NUM_1, "+91" + text)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            uploadExcelLayout.setVisibility(View.GONE);
                                            if (null != queryDocumentSnapshots && !queryDocumentSnapshots.isEmpty()) {
                                                fullUserList.clear();
                                                fullUserList.addAll(queryDocumentSnapshots.getDocuments());
                                                adapter.notifyDataSetChanged();
                                                setFilterCountToTextView(fullUserList.size());
                                            } else {
                                                firestore.collection(ACCOUNT_QUERY)
                                                        .whereEqualTo(EMAIL, getEmail(HomeScreen.this))
                                                        .whereEqualTo(MOB_NUM_2, "+91" + text)
                                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        if (null != queryDocumentSnapshots && !queryDocumentSnapshots.isEmpty()) {
                                                            fullUserList.clear();
                                                            fullUserList.addAll(queryDocumentSnapshots.getDocuments());
                                                            adapter.notifyDataSetChanged();
                                                            setFilterCountToTextView(fullUserList.size());
                                                        } else {
                                                            showToast("No User Found", HomeScreen.this);
                                                            setFilterCountToTextView(0);
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        showToast("No User Found", HomeScreen.this);
                                                        setFilterCountToTextView(0);
                                                    }
                                                });

                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    uploadExcelLayout.setVisibility(View.GONE);
                                    showToast("try again", HomeScreen.this);
                                }
                            });
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // remove the dialog from the screen
                    }
                })
                .show();
    }

    public void filterDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);

        builder.setTitle("Choose One")
                .setSingleChoiceItems(R.array.choices, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }

                })

                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        uploadExcelLayout.setVisibility(View.VISIBLE);
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        filterAccounts(selectedPosition);
                        textView.setVisibility(View.VISIBLE);
                        textView.setText("");
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })

                .show();

    }

    private void filterAccounts(final int selectedPosition) {
        hideKeyboard(HomeScreen.this);
        firestore.collection(ACCOUNT_QUERY)
                .whereEqualTo(EMAIL, getEmail(HomeScreen.this))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (null != queryDocumentSnapshots && !queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                            fullUserList.clear();
                            switch (selectedPosition) {
                                case 0:
                                    for (DocumentSnapshot snapshot : snapshotList) {
                                        try {
                                            if (snapshot.contains(ADDRESS_1) && snapshot.getString(ADDRESS_1) != null) {

                                                if (!snapshot.getString(ADDRESS_1).equalsIgnoreCase("NOT_AVAILABLE")
                                                        && !snapshot.getString(ADDRESS_2).equalsIgnoreCase("NOT_AVAILABLE")
                                                        && !snapshot.getString(ADDRESS_1).equalsIgnoreCase(DEFAULT)
                                                        && !snapshot.getString(ADDRESS_2).equalsIgnoreCase(DEFAULT)) {
                                                    if (!snapshot.getString(ADDRESS_1).isEmpty() || !snapshot.getString(ADDRESS_2).isEmpty() && !snapshot.getString(ADDRESS_2).equalsIgnoreCase("NOT_AVAILABLE")) {
                                                        fullUserList.add(snapshot);
                                                    }
                                                }

                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                    uploadExcelLayout.setVisibility(View.GONE);
                                    adapter.notifyDataSetChanged();
                                    showToast(fullUserList.size() + " Accounts Found", HomeScreen.this);

                                    break;
                                case 1:
                                    for (DocumentSnapshot snapshot : snapshotList) {
                                        try {
                                            if (null != snapshot.getString(MOB_NUM_2) || snapshot.getString(MOB_NUM_1) != null) {
                                                if (!snapshot.getString(MOB_NUM_1).equalsIgnoreCase("NOT_AVAILABLE")
                                                        && !snapshot.getString(MOB_NUM_2).equalsIgnoreCase("NOT_AVAILABLE")
                                                        && !snapshot.getString(MOB_NUM_1).equalsIgnoreCase(DEFAULT)
                                                        && !snapshot.getString(MOB_NUM_2).equalsIgnoreCase(DEFAULT)) {
                                                    if (!snapshot.getString(MOB_NUM_1).isEmpty() || !snapshot.getString(MOB_NUM_2).isEmpty()) {
                                                        fullUserList.add(snapshot);
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                    uploadExcelLayout.setVisibility(View.GONE);
                                    adapter.notifyDataSetChanged();
                                    showToast(fullUserList.size() + " Accounts Found", HomeScreen.this);
                                    break;
                                case 2:
                                    for (DocumentSnapshot snapshot : snapshotList) {
                                        try {
                                            if (snapshot.contains(IMAGE) && snapshot.getString(IMAGE) != null) {

                                                if (!snapshot.getString(IMAGE).equalsIgnoreCase("NOT_AVAILABLE")
                                                        && !snapshot.getString(IMAGE).equalsIgnoreCase(DEFAULT)) {
                                                    if (!snapshot.getString(IMAGE).equalsIgnoreCase(DEFAULT)) {
                                                        fullUserList.add(snapshot);
                                                    }
                                                }

                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                    uploadExcelLayout.setVisibility(View.GONE);
                                    adapter.notifyDataSetChanged();
                                    showToast(fullUserList.size() + " Accounts Found", HomeScreen.this);

                                    break;
                            }
                            setFilterCountToTextView(fullUserList.size());
                        } else {
                            showToast("No data Found", HomeScreen.this);
                            setFilterCountToTextView(0);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HomeScreen.this, "try again", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uploadExcelLayout.setVisibility(View.GONE);
                showToast("try again", HomeScreen.this);
            }
        });
    }

    private void refreshData2() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference().child("items");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if (dataSnapshot.getChildrenCount() == 0) {
                    uploadExcelLayout.setVisibility(View.GONE);
                    Toast.makeText(HomeScreen.this, "Nothing to refresh ", Toast.LENGTH_SHORT).show();
                } else {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        try {
                            final ExcelModel excelModel = postSnapshot.getValue(ExcelModel.class);
                            if (null != excelModel) {
                                final Map<String, Object> map = new HashMap<>();
                                map.put(ACCOUNT_NUMBER, excelModel.getAc_no());
                                map.put(PROJECT, excelModel.getProject());
                                map.put(NAME, excelModel.getName());
                                map.put(EMAIL, excelModel.getEmail());
                                map.put(ADDRESS, DEFAULT);
                                map.put(ADDRESS_1, excelModel.getAddress_1());
                                map.put(ADDRESS_2, excelModel.getAddress_2());
                                map.put(MOB_NUM_1, excelModel.getMob_num_1());
                                map.put(MOB_NUM_2, excelModel.getMob_num_2());
                                map.put(SAN_DT, excelModel.getSan_date());
                                map.put(SAN_AMOUNT, excelModel.getSan_amount());
                                map.put(BANK_NAME, DEFAULT);
                                map.put(BRANCH_NAME, DEFAULT);
                                map.put(IMAGE, DEFAULT);
                                map.put(IS_ACTIVE, true);
                                map.put(LAT, 0);
                                map.put(LONG, 0);
                                map.put(LAT2, 0);
                                map.put(LONG2, 0);
                                map.put(EXTRA_COLUMN_1, DEFAULT);
                                map.put(EXTRA_COLUMN_2, DEFAULT);


                                firestore.collection(ACCOUNT_QUERY).document("" + excelModel.getAc_no())
                                        .update(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                myRef.child("" + a).removeValue();
                                                a++;
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                firestore.collection(ACCOUNT_QUERY).document("" + excelModel.getAc_no())
                                                        .set(map);
                                                myRef.child("" + a).removeValue();
                                                a++;
                                            }
                                        });
                            }
                        } catch (Exception e) {
                            uploadExcelLayout.setVisibility(View.GONE);
                            Toast.makeText(HomeScreen.this, "Not in proper format " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                    uploadExcelLayout.setVisibility(View.GONE);
                    Toast.makeText(HomeScreen.this, "Account Uploaded ", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                uploadExcelLayout.setVisibility(View.GONE);
                Toast.makeText(HomeScreen.this, "try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setFilterCountToTextView(int count) {
        textView.setText(count + " Accounts Found");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_ADD_NEW_ACCOUNT: {
                if (resultCode == RESULT_OK) {
                    if (null != data) {
                        String res = data.getStringExtra(RESPONSE);
                        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
                        loadData();
                    } else Toast.makeText(this, "failed to add", Toast.LENGTH_SHORT).show();

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "cancelled add new Account", Toast.LENGTH_SHORT).show();
                }

            }
            break;
            case PICK_EXCEL_FILE_REQ_CODE: {
                if (resultCode == RESULT_OK) {
                    if (null != data) {

                    }
                    break;

                }
            }
            break;
        }
    }

    public void downloadAddAccountStatusFile() {

        progressBar.setVisibility(View.VISIBLE);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference()
                .child("add_account_file")
                .child("add_account_status_sample.xls");
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                progressBar.setVisibility(View.GONE);

                Toast.makeText(HomeScreen.this, "File Downloaded Successfully", Toast.LENGTH_SHORT).show();
                downloadFile(HomeScreen.this, uri, "add_account_status_sample.xls");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeScreen.this, "failed To download " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void downloadAddAccountFile() {


        progressBar.setVisibility(View.VISIBLE);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference()
                .child("add_account_file")
                .child("add_account_sample.xls");
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                progressBar.setVisibility(View.GONE);

                Toast.makeText(HomeScreen.this, "File Downloaded Successfully", Toast.LENGTH_SHORT).show();
                downloadFile(HomeScreen.this, uri, "add_account_sample.xls");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeScreen.this, "failed To download " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void downloadFile(Context context, Uri uri, String fileName) {
        DownloadManager downloadManager = (DownloadManager)
                context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, fileName);
        downloadManager.enqueue(request);

    }


    public void closeAdd(View view) {
        addLayout.setVisibility(View.GONE);
    }
}

