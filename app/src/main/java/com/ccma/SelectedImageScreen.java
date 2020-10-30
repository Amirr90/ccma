package com.ccma;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.ccma.Utility.Util.ACCOUNT_ID;
import static com.ccma.Utility.Util.ACCOUNT_QUERY;
import static com.ccma.Utility.Util.IMAGE;
import static com.ccma.Utility.Util.TAG;

public class SelectedImageScreen extends AppCompatActivity {

    ArrayList<String> imageList;
    ProgressDialog progressDialog;
    String AccountID;
    PickedImageAdapter imageAdapter;
    RecyclerView recyclerView;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    int a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_image_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.selected_image_toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("uploading image,Please wait...");

        imageList = getIntent().getStringArrayListExtra("data");
        AccountID = getIntent().getStringExtra(ACCOUNT_ID);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(imageList.size() + " images selected");


        recyclerView = (RecyclerView) findViewById(R.id.picked_image_rec);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        if (imageList == null) {
            imageList = new ArrayList<>();
        }
        imageAdapter = new PickedImageAdapter(imageList);
        recyclerView.setAdapter(imageAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        return super.onSupportNavigateUp();
    }

    public void uploadImage(View view) {
        if (view.getId() == R.id.uploadImageBtn) {
            if (!imageList.isEmpty()) {
                progressDialog.setCancelable(false);
                progressDialog.show();
                try {
                    uploadImageToFirebase();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(this, "failed to Upload Image,try again", Toast.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(view, "Image list is empty", Snackbar.LENGTH_SHORT).show();
            }

        }
    }

    private void uploadImageToFirebase() throws FileNotFoundException {

        final CollectionReference uploadImageUriRef = firestore.collection(ACCOUNT_QUERY).document(AccountID)
                .collection(IMAGE);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();

        for (a = 0; a < imageList.size(); a++) {
            final String STORAGE_PATH = "images/" + AccountID + "/" + System.currentTimeMillis() + ".jpg";
            StorageReference spaceRef = storageRef.child(STORAGE_PATH);

            InputStream stream = new FileInputStream(new File(imageList.get(a)));
            UploadTask uploadTask = spaceRef.putStream(stream);

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
                            imageMap.put(TAG, "");
                            uploadImageUriRef.document("" + System.currentTimeMillis())
                                    .set(imageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SelectedImageScreen.this, "" + (a) + " Image Uploaded", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    Intent intent = new Intent();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            });
                        }
                    });

                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    progressDialog.dismiss();
                    Toast.makeText(SelectedImageScreen.this, "Upload cancelled, try again", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    private class PickedImageAdapter extends RecyclerView.Adapter<PickedImageAdapter.MyViewHolder> {
        ArrayList<String> imagesEncodedList;

        public PickedImageAdapter(ArrayList<String> imagesEncodedList) {
            this.imagesEncodedList = imagesEncodedList;
        }


        @NonNull
        @Override
        public PickedImageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picked_image_view, parent, false);
            return new PickedImageAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PickedImageAdapter.MyViewHolder holder, final int position) {


            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagesEncodedList.get(position));
                holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(SelectedImageScreen.this, "failed to set image " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }


            holder.remove_image_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = position;
                    imagesEncodedList.remove(pos);
                    imageAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return imagesEncodedList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView, remove_image_btn;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.picked_imageView);
                remove_image_btn = (ImageView) itemView.findViewById(R.id.picked_imageView_close);
            }
        }
    }

}