package com.ccma;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.ccma.Utility.Util.ACCOUNT_ID;
import static com.ccma.Utility.Util.ACCOUNT_NUMBER;
import static com.ccma.Utility.Util.ACCOUNT_QUERY;
import static com.ccma.Utility.Util.ADDED_BY;
import static com.ccma.Utility.Util.ADDRESS;
import static com.ccma.Utility.Util.ADDRESS_1;
import static com.ccma.Utility.Util.ADDRESS_2;
import static com.ccma.Utility.Util.COMMENT;
import static com.ccma.Utility.Util.COMMENTS_QUERY;
import static com.ccma.Utility.Util.DAILY_VISIT_DATA_QUERY;
import static com.ccma.Utility.Util.DEFAULT;
import static com.ccma.Utility.Util.FAMILY_GROUPS_QUERY;
import static com.ccma.Utility.Util.GROUP_ID;
import static com.ccma.Utility.Util.IMAGE;
import static com.ccma.Utility.Util.IS_ACTIVE;
import static com.ccma.Utility.Util.LAT;
import static com.ccma.Utility.Util.LAT2;
import static com.ccma.Utility.Util.LONG;
import static com.ccma.Utility.Util.LONG2;
import static com.ccma.Utility.Util.MOB_NUM_1;
import static com.ccma.Utility.Util.MOB_NUM_2;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.OTHER_ACCOUNT_QUERY;
import static com.ccma.Utility.Util.PERSONAL_CONTACT;
import static com.ccma.Utility.Util.PHONE_CONTACT;
import static com.ccma.Utility.Util.PROJECT;
import static com.ccma.Utility.Util.SAN_AMOUNT;
import static com.ccma.Utility.Util.SAN_DT;
import static com.ccma.Utility.Util.SELF_ACCOUNT_QUERY;
import static com.ccma.Utility.Util.SELF_GROUPS_QUERY;
import static com.ccma.Utility.Util.TAG2;
import static com.ccma.Utility.Util.TIMESTAMP;
import static com.ccma.Utility.Util.TYPE;
import static com.ccma.Utility.Util.USERNAME;
import static com.ccma.Utility.Util.VISIT_TYPE;
import static com.ccma.Utility.Util.checkPermissions;
import static com.ccma.Utility.Util.checkStoragePermissions;
import static com.ccma.Utility.Util.getAccountNumber;
import static com.ccma.Utility.Util.getBankName;
import static com.ccma.Utility.Util.getBranchName;
import static com.ccma.Utility.Util.getCurrencyBalance;
import static com.ccma.Utility.Util.getDate;
import static com.ccma.Utility.Util.getFullDate;
import static com.ccma.Utility.Util.requestLocationPermissions;
import static com.ccma.Utility.Util.requestStoragePermissions;


public class AccountDetailScreen extends AppCompatActivity {
    String AccountId;
    AppLocationService appLocationService;
    FusedLocationProviderClient mFusedLocationClient;
    public static double user_lat;
    public static double user_long;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference reference;
    private int PERMISSION_ID = 1;
    TextInputEditText addressField;
    ProgressDialog progressDialog;
    double lat, lng;
    ProgressBar progressBar;

    ImageView profileImage;
    String imageUrl;
    RelativeLayout selfAccountLay, otherAccountLay;
    Query selfRef, otherRef;
    TextView accountNumber, accountName, sn_date, snAmount, address, account_status;
    String pdfName;
    List<String> familyGrpIds = new ArrayList<>();
    List<String> SelfGrpIds = new ArrayList<>();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.account_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressBar = findViewById(R.id.progressBar9);
        selfAccountLay = (RelativeLayout) findViewById(R.id.self_account_lay);
        otherAccountLay = (RelativeLayout) findViewById(R.id.other_account_lay);


        profileImage = findViewById(R.id.imageView5);
        AccountId = getIntent().getStringExtra(ACCOUNT_ID);
        reference = firestore.collection(ACCOUNT_QUERY).document(AccountId);

        selfRef = firestore.collection(SELF_ACCOUNT_QUERY)
                .whereEqualTo(ADDED_BY, AccountId)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING);

        otherRef = firestore.collection(OTHER_ACCOUNT_QUERY)
                .whereEqualTo(ADDED_BY, AccountId);

        appLocationService = new AppLocationService(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        setAccountDetail();
        setLastComment();
        setSelfAccount();
        setOtherAccount();
    }

    private void setOtherAccount() {

        Log.d("TAG", "setOtherAccount: "+AccountId);
        databaseReference.child(FAMILY_GROUPS_QUERY)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Iterable> selfList = new ArrayList<>();
                        int groupCounter = 0;
                        StringBuilder text = new StringBuilder();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            if (snapshot1.child(AccountId).exists()) {
                                long accountCounter = ((snapshot1.getChildrenCount()) - 1);
                                text.append("GroupID: " + snapshot1.getKey() + "\n");
                                text.append("Account(s) Added: " + accountCounter + "\n\n");

                                familyGrpIds.add(snapshot1.getKey());
                                selfList.add(snapshot1.child(snapshot1.getKey()).getChildren());
                                groupCounter++;
                            }
                        }
                        text.append("----------");
                        otherAccountLay.setVisibility(View.VISIBLE);
                        TextView textViewCount = findViewById(R.id.textView111);
                        textViewCount.setText(groupCounter + " Family Groups\n" + text.toString());
                        if (!selfList.isEmpty())
                            otherAccountLay.setVisibility(View.VISIBLE);
                        else otherAccountLay.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setSelfAccount() {
        databaseReference.child(SELF_GROUPS_QUERY)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Iterable> selfList = new ArrayList<>();
                        int groupCounter = 0;
                        StringBuilder text = new StringBuilder();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            if (snapshot1.child(AccountId).exists()) {
                                long accountCounter = ((snapshot1.getChildrenCount()) - 1);
                                text.append("GroupID: " + snapshot1.getKey() + "\n");
                                text.append("Account(s) Added: " + accountCounter + "\n\n");
                                SelfGrpIds.add(snapshot1.getKey());
                                selfList.add(snapshot1.child(snapshot1.getKey()).getChildren());
                                groupCounter++;
                            }
                        }

                        text.append("----------");
                        TextView textViewCount = findViewById(R.id.textView11);
                        textViewCount.setText(groupCounter + " Self Groups\n" + text.toString());
                        if (!selfList.isEmpty())
                            selfAccountLay.setVisibility(View.VISIBLE);
                        else selfAccountLay.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void setAccountDetail() {
        final TextView address1, address2, mobile1, mobile2, projectName;
        accountNumber = findViewById(R.id.textView2);
        accountName = findViewById(R.id.acc_name);
        address = findViewById(R.id.address);
        address1 = findViewById(R.id.add1);
        account_status = findViewById(R.id.active_status_btn);
        address2 = findViewById(R.id.add2);
        mobile1 = findViewById(R.id.mobile1);
        mobile2 = findViewById(R.id.mobile2);
        sn_date = findViewById(R.id.sn_date);
        snAmount = findViewById(R.id.acc_sn_amount);
        projectName = findViewById(R.id.project_name);
        firestore.collection(ACCOUNT_QUERY)
                .document(AccountId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            try {
                                String acc_num = String.valueOf(documentSnapshot.getLong(ACCOUNT_NUMBER));
                                imageUrl = documentSnapshot.getString(IMAGE);
                                if (!imageUrl.equalsIgnoreCase(DEFAULT)) {
                                    Picasso.with(AccountDetailScreen.this).load(imageUrl)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .placeholder(R.drawable.logooo)
                                            .into(profileImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(AccountDetailScreen.this).load(imageUrl).placeholder(R.drawable.logooo).into(profileImage);
                                                }
                                            });
                                }

                                Boolean isActive = documentSnapshot.getBoolean(IS_ACTIVE);

                                accountNumber.setText(getAccountNumber(documentSnapshot));
                                accountName.setText(documentSnapshot.getString(NAME));
                                getSupportActionBar().setTitle(documentSnapshot.getString(NAME));
                                address.setText("Address: " + documentSnapshot.getString(ADDRESS));
                                address1.setText(documentSnapshot.getString(ADDRESS_1));
                                address2.setText(documentSnapshot.getString(ADDRESS_2));
                                mobile1.setText(documentSnapshot.getString(MOB_NUM_1));
                                mobile2.setText(documentSnapshot.getString(MOB_NUM_2));
                                sn_date.setText(documentSnapshot.getString(SAN_DT));
                                account_status.setText(isActive ? "Active" : "Closed");
                                snAmount.setText("Sn Amount: " + getCurrencyBalance(documentSnapshot.getLong(SAN_AMOUNT)));
                                projectName.setText(documentSnapshot.getString(PROJECT));

                            } catch (Exception e) {
                                Toast.makeText(AccountDetailScreen.this, "error found in account detail" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void setLastComment() {
        CollectionReference commentRef = firestore.collection(ACCOUNT_QUERY)
                .document(AccountId)
                .collection(COMMENTS_QUERY);

        final TextView tv_comment, tv_comment_date;
        tv_comment = findViewById(R.id.last_comment_text);
        tv_comment_date = findViewById(R.id.last_comment_date);
        commentRef.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            tv_comment.setVisibility(View.VISIBLE);
                            tv_comment_date.setVisibility(View.VISIBLE);
                            DocumentSnapshot comment = queryDocumentSnapshots.getDocuments().get(0);
                            try {
                                tv_comment.setText(comment.getString(COMMENT));
                                tv_comment_date.setText(getDate(comment.getLong(TIMESTAMP)));
                            } catch (Exception e) {
                            }
                        } else {
                            tv_comment.setVisibility(View.GONE);
                            tv_comment_date.setVisibility(View.GONE);
                            //Toast.makeText(AccountDetailScreen.this, "no comment found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void accountDetailFunction(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.mobile1: {
                TextView textView = findViewById(id);
                String number = textView.getText().toString();
                showMobileDialog(number, textView, MOB_NUM_1);
            }
            break;
            case R.id.mobile2: {
                TextView textView = findViewById(id);
                String number = textView.getText().toString();
                showMobileDialog(number, textView, MOB_NUM_2);
            }
            break;
            case R.id.address_nav1:
                getlatlng(R.id.address_nav1);
                break;
            case R.id.address_nav2: {
                getlatlng(R.id.address_nav2);
            }

            break;
            case R.id.add_location1: {

                if (checkPermissions(AccountDetailScreen.this)) {
                    showAddLocationDialog(id, ADDRESS_1);
                } else {
                    requestLocationPermissions(AccountDetailScreen.this);
                }

            }
            break;
            case R.id.add_location2: {

                if (checkPermissions(AccountDetailScreen.this)) {
                    showAddLocationDialog(id, ADDRESS_2);
                } else {
                    requestLocationPermissions(AccountDetailScreen.this);
                }

            }
            break;
            case R.id.add_view_images: {
                startActivity(new Intent(AccountDetailScreen.this, Add_View_ImagesScreen.class)
                        .putExtra(ACCOUNT_ID, AccountId));
            }
            break;
            case R.id.add_view_comments: {
                startActivity(new Intent(AccountDetailScreen.this, Add_View_CommentsScreen.class)
                        .putExtra(ACCOUNT_ID, AccountId)
                        .putExtra(ADDRESS, address.getText().toString())
                        .putExtra(SAN_AMOUNT, snAmount.getText().toString())
                        .putExtra(SAN_DT, sn_date.getText().toString())
                        .putExtra(ACCOUNT_NUMBER, accountNumber.getText().toString())
                        .putExtra(USERNAME, accountName.getText().toString()));
            }
            break;

            case R.id.personal_contact: {
                startActivity(new Intent(AccountDetailScreen.this, PersonalContactScreen.class)
                        .putExtra(ACCOUNT_ID, AccountId)
                        .putExtra(ADDRESS, address.getText().toString())
                        .putExtra(SAN_AMOUNT, snAmount.getText().toString())
                        .putExtra(SAN_DT, sn_date.getText().toString())
                        .putExtra(ACCOUNT_NUMBER, accountNumber.getText().toString())
                        .putExtra(USERNAME, accountName.getText().toString()));
            }
            break;
            case R.id.phone_contact: {
                startActivity(new Intent(AccountDetailScreen.this, PhoneContactScreen.class)
                        .putExtra(ACCOUNT_ID, AccountId)
                        .putExtra(ADDRESS, address.getText().toString())
                        .putExtra(SAN_AMOUNT, snAmount.getText().toString())
                        .putExtra(SAN_DT, sn_date.getText().toString())
                        .putExtra(ACCOUNT_NUMBER, accountNumber.getText().toString())
                        .putExtra(USERNAME, accountName.getText().toString()));
            }
            break;
            case R.id.addAccount: {
                showDialog(AccountId);
            }
            break;
            case R.id.download_account_data: {
                progressDialog.setMessage("Downloading Account details, please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                firestore.collection(DAILY_VISIT_DATA_QUERY)
                        .whereEqualTo(ACCOUNT_NUMBER, accountNumber.getText().toString())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                    progressDialog.dismiss();
                                    List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                                    if (checkStoragePermissions(AccountDetailScreen.this)) {
                                        progressDialog.show();
                                        createPdf(snapshots);
                                    } else {
                                        requestStoragePermissions(AccountDetailScreen.this);
                                    }
                                } else {
                                    Toast.makeText(AccountDetailScreen.this, "No data found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AccountDetailScreen.this, "try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            break;

            case R.id.self_account_text:
                showSelfAccountGroupIdsDialog(SelfGrpIds, SELF_GROUPS_QUERY);
                break;
            case R.id.family_account_text:
                showSelfAccountGroupIdsDialog(familyGrpIds, FAMILY_GROUPS_QUERY);
                break;

            case R.id.acc_name: {
                TextView textView = findViewById(id);
                String text = textView.getText().toString();
                showUpdateDialog(text, textView, NAME);
            }
            break;
            case R.id.address: {
                TextView textView = findViewById(id);
                String text = textView.getText().toString();
                showUpdateDialog(text, textView, ADDRESS);
            }
            break;

            case R.id.sn_date: {
                TextView textView = findViewById(id);
                String text = textView.getText().toString();
                showUpdateDialog(text, textView, SAN_DT);
            }
            break;

            case R.id.project_name: {
                TextView textView = findViewById(id);
                String text = textView.getText().toString();
                showUpdateDialog(text, textView, PROJECT);
            }
            break;
            case R.id.acc_sn_amount: {
                TextView textView = findViewById(id);
                String text = textView.getText().toString();
                showUpdateDialog(text, textView, SAN_AMOUNT);
            }
            break;
            case R.id.account_status: {
                startActivity(new Intent(AccountDetailScreen.this, AccountStatus.class)
                        .putExtra(ACCOUNT_ID, AccountId)
                        .putExtra(ACCOUNT_NUMBER, accountNumber.getText().toString()));
            }
            break;
            case R.id.active_status_btn: {
                final TextView textView = findViewById(id);
                String text = textView.getText().toString();
                if (text.isEmpty())
                    return;
                progressDialog.show();
                boolean isActive = false;
                if (text.equalsIgnoreCase("Active"))
                    isActive = false;
                else
                    isActive = true;
                final boolean finalIsActive = isActive;
                firestore.collection(ACCOUNT_QUERY).document(AccountId).update(IS_ACTIVE, isActive).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        if (finalIsActive) {
                            Toast.makeText(AccountDetailScreen.this, "Account Active", Toast.LENGTH_SHORT).show();
                            textView.setText("Active");
                        } else {
                            Toast.makeText(AccountDetailScreen.this, "Account Close", Toast.LENGTH_SHORT).show();
                            textView.setText("Closed");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AccountDetailScreen.this, "try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            break;
            default:
                Snackbar.make(view, "Coming soon", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showUpdateDialog(String oldText, final TextView textView, final String address) {
        LayoutInflater inflater = AccountDetailScreen.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.update_lay, null);
        final TextInputEditText inputEditText = view.findViewById(R.id.update_et);
        inputEditText.setHint(address);
        if (!address.equalsIgnoreCase(SAN_AMOUNT))
            inputEditText.setText(oldText);
        new AlertDialog.Builder(AccountDetailScreen.this)
                .setMessage("Update " + address)
                .setView(view)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String text = inputEditText.getText().toString();
                        if (text.isEmpty())
                            return;
                        progressDialog.setMessage("Updating, Please wait....");
                        progressDialog.show();
                        if (address.equalsIgnoreCase(SAN_AMOUNT)) {
                            try {
                                final Long amount = Long.parseLong(text);
                                firestore.collection(ACCOUNT_QUERY).document(AccountId)
                                        .update(address, amount)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(AccountDetailScreen.this, "Updated", Toast.LENGTH_SHORT).show();
                                                textView.setText("Sn Amount: " + getCurrencyBalance(amount));
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AccountDetailScreen.this, "try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                Toast.makeText(AccountDetailScreen.this, "please, enter valid amount", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            firestore.collection(ACCOUNT_QUERY).document(AccountId)
                                    .update(address, text)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AccountDetailScreen.this, "Updated", Toast.LENGTH_SHORT).show();
                                            textView.setText("Address: " + text);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AccountDetailScreen.this, "try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }


                    }
                }).setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void showSelfAccountGroupIdsDialog(List<String> selfGrpIds, final String selfGroupsQuery) {
        final String[] items = new String[selfGrpIds.size()];
        for (int a = 0; a < selfGrpIds.size(); a++) {
            items[a] = selfGrpIds.get(a);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailScreen.this);
        builder.setTitle("Select GroupId to view linked Accounts");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String text = (items[item]);
                dialog.dismiss();
                startActivity(new Intent(AccountDetailScreen.this, UserInGroupScreen.class)
                        .putExtra(GROUP_ID, text)
                        .putExtra(TYPE, selfGroupsQuery));
            }
        }).show();
    }

    private void showDialog(String accountId) {
        final CharSequence[] items = {"Add To Self Group", "Add To Family Group"};

        AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailScreen.this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                startActivity(new Intent(AccountDetailScreen.this, PickAccountScreen.class)
                        .putExtra(TYPE, item)
                        .putExtra("TITLE", items[item])
                        .putExtra(USERNAME, accountName.getText().toString())
                        .putExtra(ACCOUNT_NUMBER, accountNumber.getText().toString())
                        .putExtra(SAN_AMOUNT, snAmount.getText().toString())
                        .putExtra(SAN_DT, sn_date.getText().toString())
                        .putExtra(ACCOUNT_ID, AccountId));

            }
        }).show();
    }

    private void createPdf(List<DocumentSnapshot> snapshots) {
        Document doc = new Document(PageSize.A4, 10, 10
                , 10, 10);
        pdfName = (System.currentTimeMillis()) + ".pdf";
        Font fontSize_16 = new Font(Font.FontFamily.TIMES_ROMAN, 16.0f, Font.BOLD);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CCMA/";

        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, pdfName);
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            PdfWriter.getInstance(doc, fOut);
            doc.open();

            StringBuilder header = new StringBuilder();
            header.append("Report Generated For " + accountName.getText().toString());
            header.append(" on " + getDate(System.currentTimeMillis()) + "\n");
            header.append(getBankName(AccountDetailScreen.this) + "\n");
            header.append(getBranchName(AccountDetailScreen.this) + "\n");
            header.append("Account number: " + accountNumber.getText().toString() + "\n");
            header.append(snAmount.getText().toString() + "\n");
            header.append("Sn Date: " + sn_date.getText().toString() + "\n");
            header.append(address.getText().toString() + "\n\n");


            Paragraph letterNo = new Paragraph(header.toString(), fontSize_16);
            letterNo.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(letterNo);
            float[] columnWidths = {1, 5, 7, 15};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);
            table.getDefaultCell().setUseAscender(true);
            table.getDefaultCell().setUseDescender(true);
            Font f = new Font(Font.FontFamily.HELVETICA, 13, Font.NORMAL, GrayColor.GRAYWHITE);
            PdfPCell cell = new PdfPCell();
            cell.setPadding(10);
            cell.setBackgroundColor(GrayColor.GRAYBLACK);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setColspan(8);
            table.addCell(cell);
            table.getDefaultCell().setBackgroundColor(new GrayColor(0.75f));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            for (int i = 0; i < 2; i++) {
                table.addCell("S.No");
                table.addCell("Contact Type");
                table.addCell("Date");
                table.addCell("Contact Details");
            }
            table.setHeaderRows(3);
            table.setFooterRows(1);
            table.getDefaultCell().setBackgroundColor(GrayColor.GRAYWHITE);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
            int counter = 1;
            for (DocumentSnapshot snapshot : snapshots) {
                Map<String, Object> map = (Map<String, Object>) snapshot.get("items");
                StringBuilder builder1 = new StringBuilder();
                if (snapshot.getString(VISIT_TYPE).equalsIgnoreCase(PERSONAL_CONTACT)
                        || snapshot.getString(VISIT_TYPE).equalsIgnoreCase(PHONE_CONTACT)) {

                    ArrayList<String> data = (ArrayList<String>) map.get("data");
                    if (null != data) {
                        for (int a = 0; a < data.size(); a++) {
                            builder1.append("" + (a + 1) + ". " + data.get(a) + "\n");

                        }
                    }

                } else {
                    builder1.append(map.get("comment") + "\n");

                }
                table.addCell(String.valueOf(counter));
                table.addCell(snapshot.getString(VISIT_TYPE));
                table.addCell(getFullDate(snapshot.getLong(TIMESTAMP)).toString());
                table.addCell(builder1.toString());
                counter++;

            }
            doc.add(table);
            openPdf(file);
        } catch (FileNotFoundException | DocumentException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "file not created " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            doc.close();
        }


    }

    private void openPdf(File myFile) {

        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", myFile);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

        } else {
            String myFilePath = Environment.getExternalStorageDirectory().getPath() + "/CCMA/" + pdfName;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(myFilePath), "application/pdf");
            intent = Intent.createChooser(intent, "Open File");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        progressDialog.dismiss();
    }

    private void getlatlng(final int address_nav1) {

        progressDialog.setMessage("loading.....");
        progressDialog.show();
        firestore.collection(ACCOUNT_QUERY).document(AccountId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        progressDialog.dismiss();

                        if (documentSnapshot.exists()) {
                            if (R.id.address_nav1 == address_nav1) {
                                try {
                                    lat = documentSnapshot.getDouble(LAT);
                                    lng = documentSnapshot.getDouble(LONG);
                                } catch (Exception e) {
                                }
                            } else {
                                try {
                                    lat = documentSnapshot.getDouble(LAT2);
                                    lng = documentSnapshot.getDouble(LONG2);
                                } catch (Exception e) {
                                }
                            }
                            String strUri = "http://maps.google.com/maps?q=loc:" + lat + "," + lng + " (" + "Target location" + ")";
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));

                            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

                            startActivity(intent);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(appLocationService, "try again", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AccountDetailScreen.this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddLocationDialog(int id, final String field) {
        final TextView textView;
        if (field.equalsIgnoreCase(ADDRESS_1)) {
            textView = findViewById(R.id.add1);
        } else {
            textView = findViewById(R.id.add2);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailScreen.this);

        // get the layout inflater
        LayoutInflater inflater = AccountDetailScreen.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_location_lay, null);

        addressField = view.findViewById(R.id.address_field);

        getLastLocation();
        builder.setView(view)

                // action buttons
                .setMessage("Add Location")
                .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final String text = addressField.getText().toString();
                        if (!text.isEmpty()) {
                            progressDialog.setMessage("Uploading Address,Please wait...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            reference.update(field, text).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    updateLatLangToDatabase(user_lat, user_long);
                                    textView.setText(text);
                                    Toast.makeText(AccountDetailScreen.this, "Address updated successfully", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                }
                            });

                        }

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // remove the dialog from the screen
                    }
                })
                .show();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (isLocationEnabled()) {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocationData();
                            } else {
                                user_lat = location.getLatitude();
                                user_long = location.getLongitude();
                                //updateLatLangToDatabase(user_lat, user_long);
                                getAddress(AccountDetailScreen.this, location.getLatitude(), location.getLongitude());

                            }
                        }
                    }
            );
        } else {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    public boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    private void updateLatLangToDatabase(double user_lat, double user_long) {
        Map<String, Object> latLongMap = new HashMap<>();
        latLongMap.put(LAT, user_lat);
        latLongMap.put(LONG, user_long);
        reference.update(latLongMap);
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ((FusedLocationProviderClient) mFusedLocationClient).requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            user_lat = mLastLocation.getLatitude();
            user_long = mLastLocation.getLongitude();
            getAddress(AccountDetailScreen.this, mLastLocation.getLatitude(), mLastLocation.getLongitude());


        }
    };


    public void updateProfileImage(final View view) {

        if (view.getId() == R.id.profile_cardView) {
            final CharSequence[] items = {"View Profile Image", "Change Profile Image"};

            AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailScreen.this);
            builder.setTitle("Make your selection");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    dialog.dismiss();
                    if (item == 0) {
                        if (!imageUrl.equalsIgnoreCase(DEFAULT)) {
                            startActivity(new Intent(AccountDetailScreen.this, SingleImageViewScreen.class)
                                    .putExtra("image", imageUrl));
                        } else {
                            Snackbar.make(view, "Upload image first", Snackbar.LENGTH_SHORT).setAction("UPLOAD", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ImagePicker.Companion.with(AccountDetailScreen.this)
                                            .crop(4f, 4f)                    //Crop image(Optional), Check Customization for more option
                                            .compress(512)            //Final image size will be less than 1 MB(Optional)
                                            .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                                            .start();

                                }
                            }).show();
                        }
                    } else {
                        ImagePicker.Companion.with(AccountDetailScreen.this)
                                .crop(4f, 4f)                    //Crop image(Optional), Check Customization for more option
                                .compress(512)            //Final image size will be less than 1 MB(Optional)
                                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                                .start();
                    }

                }
            }).show();

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (null != data) {
                Uri uri = data.getData();
                profileImage.setImageURI(uri);
                try {
                    uploadImageToFirebase();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(AccountDetailScreen.this, "failed " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void uploadImageToFirebase() throws FileNotFoundException {

        progressDialog.show();
        progressDialog.setMessage("Updating profile image,please wait...");
        final DocumentReference uploadImageUriRef = firestore.collection(ACCOUNT_QUERY).document(AccountId);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();


        final String STORAGE_PATH = "profile_image/" + AccountId + "/" + System.currentTimeMillis() + ".jpg";
        StorageReference spaceRef = storageRef.child(STORAGE_PATH);

        Bitmap bitmap2 = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] compressData = baos.toByteArray();
        UploadTask uploadTask = spaceRef.putBytes(compressData);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setProgress((int) progress);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.child(STORAGE_PATH).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map<String, Object> imageMap = new HashMap<>();
                        imageMap.put(IMAGE, uri.toString());
                        uploadImageUriRef.update(imageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(AccountDetailScreen.this, " Image Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                progressDialog.dismiss();
                Toast.makeText(AccountDetailScreen.this, "Upload cancelled, try again", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();

                Toast.makeText(AccountDetailScreen.this, "failed to upload Image " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    public void getAddress(Context context, double LATITUDE, double LONGITUDE) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String area = addresses.get(0).getAdminArea();
                String sub_area = addresses.get(0).getSubAdminArea();
                String locality = addresses.get(0).getLocality();
                String sub_locality = addresses.get(0).getSubLocality();
                String postalCode = addresses.get(0).getPostalCode();
                String admin_area = addresses.get(0).getAddressLine(1);
                String sub_admin_area = addresses.get(0).getSubAdminArea();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL


                Log.d(TAG2, "getAddress:  address " + address);
                Log.d(TAG2, "getAddress:  area " + area);
                Log.d(TAG2, "getAddress:  sub_area " + sub_area);
                Log.d(TAG2, "getAddress:  locality " + locality);
                Log.d(TAG2, "getAddress:  sub_locality " + sub_locality);
                Log.d(TAG2, "getAddress:  city " + city);
                Log.d(TAG2, "getAddress:  state " + state);
                Log.d(TAG2, "getAddress:  postalCode " + postalCode);
                Log.d(TAG2, "getAddress:  address 1 " + admin_area);
                Log.d(TAG2, "getAddress:  sub_admin_area " + sub_admin_area);
                Log.d(TAG2, "getAddress:  knownName " + knownName);

                addressField.setText(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    private void showMobileDialog(final String number, final TextView textView, final String field) {
        final CharSequence[] items = {"EDIT MOBILE NUMBER", "CALL"};

        AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailScreen.this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                if (item == 0) {
                    editMobileNumber(number, textView, field);
                } else {
                    String phone = number.trim();
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                    startActivity(intent);
                }
            }
        }).show();
    }

    private void editMobileNumber(String number, final TextView textView, final String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailScreen.this);

        // get the layout inflater
        LayoutInflater inflater = AccountDetailScreen.this.getLayoutInflater();

        View view = inflater.inflate(R.layout.add_location_lay, null);
        final TextInputEditText inputEditText = view.findViewById(R.id.address_field);
        inputEditText.setHint("mobile Number");
        builder.setView(view)

                // action buttons
                .setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        final String text = inputEditText.getText().toString();
                        if (!text.isEmpty()) {
                            progressDialog.setMessage("Updating Mobile number,Please wait...");
                            progressDialog.show();
                            reference.update(field, "+91" + text).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AccountDetailScreen.this, "Phone Number updated successfully", Toast.LENGTH_SHORT).show();
                                    textView.setText("+91" + text);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AccountDetailScreen.this, "try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(AccountDetailScreen.this, "field is empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // remove the dialog from the screen
                    }
                })
                .show();
    }
}