package com.ccma.Utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static com.google.common.net.HttpHeaders.FROM;

public class Util {

    public static final String MyPREFERENCES = "My_Prefs";
    public static final String IS_LOGIN = "Is_login";
    public static final String TAG2 = "AccountDetailScreen";
    public static final String CREATED_AT = "created_at";
    public static final String TIMESTAMP = "timestamp";
    public static final String USERNAME = "username";
    public static final String MESSAGE = "msg";
    public static final String IS_SEEN = "is_seen";
    public static final String ADDRESS = "Address";
    public static final String USER = "user";
    public static final String LAT2 = "lat2";
    public static final String LONG2 = "lng2";
    private static final int PERMISSION_ID = 10012;
    public int REQ_CODE_ADD_NEW_ACCOUNT = 101;
    public static final String RESPONSE = "response";
    public static final String ACCOUNT_NUMBER = "AC_No";
    public static final String PROJECT = "Project";
    public static final String NAME = "Name";
    public static final String ADDRESS_1 = "Address_1";
    public static final String ADDRESS_2 = "Address_2";
    public static final String MOB_NUM_1 = "Mob_No_1";
    public static final String MOB_NUM_2 = "Mob_No_2";
    public static final String SAN_DT = "San_Dt";
    public static final String DOC_ID = "doc_id";
    public static final String SAN_AMOUNT = "San_Amt";
    public static final String EXTRA_COLUMN_1 = "EXTRA_COLUMN_1";
    public static final String EXTRA_COLUMN_2 = "EXTRA_COLUMN_2";
    public static final String BANK_NAME = "Bank_name";
    public static final String MOBILE = "mobile";
    public static final String BRANCH_NAME = "Branch_name";
    public static final String IMAGE = "image";
    public static final String TAG = "tag";
    public static final String LAT = "lat";
    public static final String LONG = "long";
    public static final String IS_ACTIVE = "is_active";
    public static final String IS_ACTIVE2 = "isActive";
    public static final String DEFAULT = "default";
    public static final String DEFAULT2 = "NOT_AVAILABLE";
    public static final String SELF = "self";
    public static final String ACCOUNT_TYPE = "Account_type";
    public static final String SELF_ACCOUNT_QUERY = "Self_Accounts";
    public static final String OTHER_ACCOUNT_QUERY = "Other_Account";
    public static final String GROUP_ID = "group_id";
    public static final String ADDED_TO = "added_To";
    public static final String ADDED_BY = "added_by";
    public static final String ACCOUNT_QUERY = "Account_Holders";
    public static final String QUERY_PERSONAL_CONTACT = "Personal_Contact";
    public static final String ACCOUNT_ID = "account_id";
    public static final String EXCEL_FILE = "file";
    public static final int REQ_CODE_UPLOAD_IMAGE = 10;
    public static final String LOGIN_CREDENTIALS = "Login_credentials";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String COMMENTS_QUERY = "Comments";
    public static final String COMMENT = "comment";
    public static final String DATA = "data";
    public static final String CLASSIFICATION = "classification";
    public static final String BALANCE_DUE = "balance_due";
    public static final String ACCOUNT_STATUS_QUERY = "Account_Status";
    public static final String TYPE = "type";
    public static final String QUERY_PHONE_CONTACT = "Phone_Contacts";
    public static final String QUERY_CHAT = "Chat";
    public static final String DAILY_VISIT_DATA_QUERY = "DailyVisitData";
    public static final String DEMO_QUERY = "Demo_Data";
    public static final String VISIT_TYPE = "visitType";
    public static final String PERSONAL_CONTACT = "personalContact";
    public static final String PHONE_CONTACT = "phoneContact";
    public static final String COMMENTS = "comments";
    public static final String FAMILY_GROUPS_QUERY = "Family_Group";
    public static final String SELF_GROUPS_QUERY = "Self_Group";
    public static final String GROUPS_QUERY = "Groups";
    public static final String NOT_AVAILABLE = "NOT_AVAILABLE";


    public static void saveLoginSession(Boolean value, Context context, String email, String username) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(IS_LOGIN, value);
        editor.putString(EMAIL, email);
        editor.putString(USERNAME, username);
        editor.commit();
    }

    public static void saveUserName(String username, Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(USERNAME, username);
        editor.commit();
    }

    public static void saveBankName(String username, Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(BANK_NAME, username);
        editor.commit();
    }

    public static void saveBranchName(String username, Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(BRANCH_NAME, username);
        editor.commit();
    }

    public static void logout(Boolean value, Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(IS_LOGIN, value);
        editor.putString(EMAIL, "");
        editor.putString(USERNAME, "");
        editor.putString(BANK_NAME, "");
        editor.putString(BRANCH_NAME, "");
        editor.commit();
    }

    public static boolean getLoginSessionStatus(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.contains(IS_LOGIN)) {
            return sharedpreferences.getBoolean(IS_LOGIN, false);
        } else return false;
    }

    public static String getUsername(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.contains(USERNAME)) {
            return sharedpreferences.getString(USERNAME, "");
        } else return "";
    }

    public static String getBankName(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.contains(BANK_NAME)) {
            return sharedpreferences.getString(BANK_NAME, "");
        } else return "";
    }

    public static String getBranchName(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.contains(BRANCH_NAME)) {
            return sharedpreferences.getString(BRANCH_NAME, "");
        } else return "";
    }

    public static String getEmail(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.contains(EMAIL)) {
            return sharedpreferences.getString(EMAIL, "");
        } else return "";
    }

    public static Map<String, Object> getAddNewAccountMap(Context context) {

        Map<String, Object> map = new HashMap<>();
        map.put(ACCOUNT_NUMBER, System.currentTimeMillis());
        map.put(PROJECT, DEFAULT);
        map.put(NAME, DEFAULT);
        map.put(EMAIL, getEmail(context));
        map.put(ADDRESS, DEFAULT);
        map.put(ADDRESS_1, DEFAULT);
        map.put(ADDRESS_2, DEFAULT);
        map.put(MOB_NUM_1, DEFAULT);
        map.put(MOB_NUM_2, DEFAULT);
        map.put(SAN_DT, new Random().nextInt(10000) + 20);
        map.put(SAN_AMOUNT, new Random().nextInt(10000) + 20);
        map.put(BANK_NAME, DEFAULT);
        map.put(BRANCH_NAME, DEFAULT);
        map.put(IMAGE, DEFAULT);
        map.put(IS_ACTIVE, true);
        map.put(LAT, new Random().nextInt(61) + 20);
        map.put(LONG, new Random().nextInt(61) + 20);
        map.put(LAT2, new Random().nextInt(61) + 20);
        map.put(LONG2, new Random().nextInt(61) + 20);
        map.put(EXTRA_COLUMN_1, DEFAULT);
        map.put(EXTRA_COLUMN_2, DEFAULT);
        return map;

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static Map<String, Object> getSignUpMap(String email, String password, String username) {
        Map<String, Object> map = new HashMap<>();
        map.put(EMAIL, email);
        map.put(PASSWORD, password);
        map.put(USERNAME, username);
        map.put(IS_ACTIVE2, true);
        map.put(CREATED_AT, System.currentTimeMillis());
        return map;

    }

    public static void requestStoragePermissions(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_ID
        );
    }

    public static Map<String, Object> getCommentMap(String comment_text, Long timestamp, String AccountNumber, String Username, Activity activity, String SanAmount, String SanDate, String address) {
        Map<String, Object> map = new HashMap<>();
        map.put(COMMENT, comment_text);
        map.put(TIMESTAMP, timestamp);
        map.put(EMAIL, getEmail(activity));
        map.put(SAN_AMOUNT, SanAmount);
        map.put(SAN_DT, SanDate);
        map.put(ADDRESS, address);
        map.put(ACCOUNT_NUMBER, AccountNumber);
        map.put(NAME, Username);
        return map;

    }

    public static Map<String, Object> getRecentChatMap(String msg, Long timestamp, Context context) {
        Map<String, Object> map = new HashMap<>();
        map.put(MESSAGE, msg);
        map.put(TIMESTAMP, timestamp);
        map.put(EMAIL, getEmail(context));
        map.put(IS_SEEN, false);
        map.put(USERNAME, getUsername(context));
        return map;

    }

    public static Map<String, Object> getChatMap(String msg, String timestamp) {
        long timeStamp = Long.parseLong(timestamp);
        Map<String, Object> map = new HashMap<>();
        map.put(MESSAGE, msg);
        map.put(FROM, USER);
        map.put(TIMESTAMP, timeStamp);
        return map;

    }

    public static CharSequence createDate(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(d);
    }

    public static CharSequence getFullDate(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        return sdf.format(d);
    }

    public static CharSequence getDate(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(d);
    }

    public static int getDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        Date date = cal.getTime();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static ArrayList<String> getPersonalContactData() {
        ArrayList<String> list = new ArrayList<>();
        list.add("Borrower Not available at the shop/house");
        list.add("Borrower is sick");
        list.add("family member is sick");
        list.add("No proper response/assurance was given");
        list.add("No one is available at the shop/house");
        list.add("talks made with borrower");
        list.add("B.C letter obtained");
        list.add("talks made with family member");
        list.add("Borrower is out of station");
        list.add("Borrower is out of station with family");
        list.add("Promise to pay Rs(x) within (y) days");
        list.add("Rs (x) is received as recovery");

        return list;

    }

    public static ArrayList<String> getPhoneContactData() {
        ArrayList<String> list = new ArrayList<>();
        list.add("Mobile number is not reachable/out of service");
        list.add("Mobile number does not exists");
        list.add("Call not answered");
        list.add("Call received but number belongs to someone else");
        list.add("Call received by family members who is advised to tell the borrower(not present)");
        list.add("Borrower is sick");
        list.add("Family member is sick");
        list.add("No proper response/assurance received");
        list.add("Promise to pay Rs(x) within (y) days");

        return list;

    }

    public static String getCurrencyBalance(double payment) {
        Locale indiaLocale = new Locale("en", "IN");

        NumberFormat india = NumberFormat.getCurrencyInstance(indiaLocale);

        return "" + india.format(payment);
    }

    public static String getCurrencyBalance(String pay) {
        double payment = Double.parseDouble(pay);
        Locale indiaLocale = new Locale("en", "IN");

        NumberFormat india = NumberFormat.getCurrencyInstance(indiaLocale);

        return "" + india.format(payment);
    }

    public static void setTotalAccountCount(final TextInputEditText inputEditText, final Activity activity) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(ACCOUNT_QUERY)
                .whereEqualTo(EMAIL, getEmail(activity))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            inputEditText.setHint("Search among " + queryDocumentSnapshots.size() + " account holders");
                        } else {
                            inputEditText.setHint("0 accounts");

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                inputEditText.setHint("0 accounts");
                Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showToast(String s, Activity activity) {
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
    }


    public static boolean checkStoragePermissions(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public static boolean checkPermissions(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public static void requestLocationPermissions(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    public static byte[] getSHA512(String key, byte[] dat) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance(key);
        sha.update(dat);
        return sha.digest();
    }


    public static String getAccountNumber(DocumentSnapshot documentSnapshot) {
        if (null != documentSnapshot.getLong(ACCOUNT_NUMBER) && documentSnapshot.getLong(ACCOUNT_NUMBER) != 0) {
            return "" + documentSnapshot.getLong(ACCOUNT_NUMBER);
        } else {
            return documentSnapshot.getString(EXTRA_COLUMN_1);
        }
    }
}
