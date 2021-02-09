package com.ccma;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.archit.calendardaterangepicker.customviews.CalendarListener;
import com.archit.calendardaterangepicker.customviews.DateRangeCalendarView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.ccma.Utility.Util.ACCOUNT_NUMBER;
import static com.ccma.Utility.Util.ADDRESS;
import static com.ccma.Utility.Util.COMMENTS;
import static com.ccma.Utility.Util.DAILY_VISIT_DATA_QUERY;
import static com.ccma.Utility.Util.EMAIL;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.PERSONAL_CONTACT;
import static com.ccma.Utility.Util.PHONE_CONTACT;
import static com.ccma.Utility.Util.SAN_AMOUNT;
import static com.ccma.Utility.Util.SAN_DT;
import static com.ccma.Utility.Util.TIMESTAMP;
import static com.ccma.Utility.Util.VISIT_TYPE;
import static com.ccma.Utility.Util.checkStoragePermissions;
import static com.ccma.Utility.Util.getBankName;
import static com.ccma.Utility.Util.getBranchName;
import static com.ccma.Utility.Util.getDate;
import static com.ccma.Utility.Util.getEmail;
import static com.ccma.Utility.Util.requestStoragePermissions;

public class PrintScreen extends AppCompatActivity {
    private static final String TAG = "PrintScreen";

    String pdfName;
    ProgressDialog progressDialog;
    String visitType;

    DateRangeCalendarView calendar;

    String choices[] = {"Personal Contact Data", "Phone Contact Data", "Comments Data"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.print_screen_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Select Date");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading data, please wait...");
        progressDialog.setCancelable(false);

        setDate();

        calendar = (DateRangeCalendarView) findViewById(R.id.calendar);

        calendar.setCalendarListener(new CalendarListener() {
            @Override
            public void onFirstDateSelected(@NotNull Calendar calendar) {

            }

            @Override
            public void onDateRangeSelected(@NotNull Calendar startDate, @NotNull Calendar endDate) {
                Log.d(TAG, "onDateRangeSelected: " + "Start Date: " + startDate.getTimeInMillis() + " End date: " + endDate.getTimeInMillis());
                showOptionDialog(startDate, endDate);
            }
        });

    }

    private void showOptionDialog(final Calendar startTime, final Calendar endTime) {

        AlertDialog.Builder builder = new AlertDialog.Builder(PrintScreen.this);
        builder.setTitle("Make your selection");
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                if (item == 0)
                    visitType = PERSONAL_CONTACT;
                else if (item == 1)
                    visitType = PHONE_CONTACT;
                else
                    visitType = COMMENTS;
                if (checkStoragePermissions(PrintScreen.this)) {
                    progressDialog.show();
                    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                    firebaseFirestore.collection(DAILY_VISIT_DATA_QUERY)
                            .whereEqualTo(VISIT_TYPE, visitType)
                            .whereEqualTo(EMAIL, getEmail(PrintScreen.this))
                            .whereGreaterThanOrEqualTo("timestamp", startTime.getTimeInMillis())
                            .whereLessThanOrEqualTo("timestamp", endTime.getTimeInMillis())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    progressDialog.dismiss();
                                    if (queryDocumentSnapshots.isEmpty()) {
                                        Toast.makeText(PrintScreen.this, "No data found", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                                    createPdf(snapshots, startTime.getTime().toString(), startTime, endTime);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(PrintScreen.this, "try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else
                    requestStoragePermissions(PrintScreen.this);

            }
        }).show();


    }

    private void createPdf(List<DocumentSnapshot> snapshots, String date, Calendar startTime, Calendar endTime) {
        Document doc = new Document(PageSize.A4.rotate(), 10, 10
                , 10, 10);
        pdfName = (System.currentTimeMillis()) + ".pdf";
        Font fontSize_16 = new Font(Font.FontFamily.TIMES_ROMAN, 16.0f, Font.BOLD);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CCMA/";

        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, pdfName);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            PdfWriter.getInstance(doc, fOut);
            doc.open();

            String header = getBankName(PrintScreen.this) + "\n" +
                    getBranchName(PrintScreen.this) + "\n" +
                    "Report of " + visitType + " made on: " + date
                    + "\nReport Print Date: " + getDate(System.currentTimeMillis()) + "\n"
                    + "From: " + getDate(startTime.getTimeInMillis()) + "         To: " + getDate(endTime.getTimeInMillis()) + "\n\n";
            Paragraph letterNo = new Paragraph(header, fontSize_16);
            letterNo.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(letterNo);
            float[] columnWidths = {1, 1.3f, 3, 4, 4, 2, 2, 4, 2};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);
            table.getDefaultCell().setUseAscender(true);
            table.getDefaultCell().setUseDescender(true);
            Font f = new Font(Font.FontFamily.HELVETICA, 13, Font.NORMAL, GrayColor.GRAYWHITE);
            PdfPCell cell = new PdfPCell();
            cell.setPadding(10);
            cell.setBackgroundColor(GrayColor.GRAYBLACK);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setColspan(9);
            table.addCell(cell);
            table.getDefaultCell().setBackgroundColor(new GrayColor(0.75f));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            for (int i = 0; i < 2; i++) {
                table.addCell("S.N");
                table.addCell("Project");
                table.addCell("A/C No");
                table.addCell("Borrower's Name");
                table.addCell("Address");
                table.addCell("San Amount");
                table.addCell("San Date");
                table.addCell(visitType + " Details");
                table.addCell("Date");
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
                String amount = (String) map.get(SAN_AMOUNT);
                String address = (String) map.get(ADDRESS);
                String[] am = new String[2];
                String[] ad = new String[2];
                try {
                    am = amount.split("\\:");
                    ad = address.split("\\:");
                    table.addCell(String.valueOf(counter));
                    table.addCell("CCA");
                    table.addCell((String) map.get(ACCOUNT_NUMBER) + "");
                    table.addCell((String) map.get(NAME));
                    table.addCell(ad.length > 1 ? ad[1] : ad[0]);
                    table.addCell(am.length > 1 ? am[1] : am[0]);
                    table.addCell((String) map.get(SAN_DT));
                    table.addCell(builder1.toString());
                    table.addCell(getDate(snapshot.getLong(TIMESTAMP)).toString());
                } catch (Exception e) {
                    Log.d(TAG, "createPdfDateError: " + e.getLocalizedMessage());

                }

                counter++;
            }
            doc.add(table);
            openPdf(file);
        } catch (FileNotFoundException | DocumentException e) {
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
        } else {
            String myFilePath = Environment.getExternalStorageDirectory().getPath() + "/CCMA/" + pdfName;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(myFilePath), "application/pdf");
            intent = Intent.createChooser(intent, "Open File");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
    }


    private void setDate() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}