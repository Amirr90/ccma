package com.ccma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccma.Utility.Util;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.ccma.Utility.Util.ACCOUNT_ID;
import static com.ccma.Utility.Util.ACCOUNT_QUERY;
import static com.ccma.Utility.Util.IMAGE;
import static com.ccma.Utility.Util.NAME;
import static com.ccma.Utility.Util.REQ_CODE_UPLOAD_IMAGE;
import static com.otaliastudios.cameraview.CameraView.PERMISSION_REQUEST_CODE;

public class Add_View_ImagesScreen extends AppCompatActivity {


    String AccountId;
    private int ADD_IMAGE_REQ_CODE = 1001;
    ArrayList<String> returnValue = new ArrayList<>();

    ImageAdapter imageAdapter;
    RecyclerView recyclerView;
    List<DocumentSnapshot> imageList = new ArrayList<>();

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    TextView textView;

    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__view__images_scree);
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_view_image_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add/View Images");

        AccountId = getIntent().getStringExtra(ACCOUNT_ID);

        textView = findViewById(R.id.textView3);
        progressBar = findViewById(R.id.progressBar2);
        recyclerView = (RecyclerView) findViewById(R.id.image_rec);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        imageAdapter = new ImageAdapter(imageList, this);
        recyclerView.setAdapter(imageAdapter);
        loadImages();

    }

    private void loadImages() {

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection(ACCOUNT_QUERY)
                .document(AccountId).collection(IMAGE)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                progressBar.setVisibility(View.GONE);
                if (!queryDocumentSnapshots.isEmpty()) {
                    imageList.clear();
                    imageList.addAll(queryDocumentSnapshots.getDocuments());
                    imageAdapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);

                } else {
                    recyclerView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void addImage(View view) {
        int id = view.getId();
        if (id == R.id.add_image) {
            addImage();
        }
    }

    private void addImage() {
        Options options = Options.init()
                .setRequestCode(ADD_IMAGE_REQ_CODE)                                           //Request code for activity results
                .setCount(5)                                                 //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                //.setPreSelectedUrls(null)                               //Pre selected Image Urls
                .setExcludeVideos(true)//Option to exclude videos
                //Duration for video recording
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/CCMA/images");                                       //Custom Path For media Storage

        Pix.start(Add_View_ImagesScreen.this, options);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(Add_View_ImagesScreen.this, Options.init().setRequestCode(ADD_IMAGE_REQ_CODE));
                } else {
                    Toast.makeText(Add_View_ImagesScreen.this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }

            }
            break;

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == ADD_IMAGE_REQ_CODE && data != null) {
            returnValue.addAll(data.getStringArrayListExtra(Pix.IMAGE_RESULTS));
            Intent intent = new Intent(Add_View_ImagesScreen.this, SelectedImageScreen.class);
            intent.putExtra("data", returnValue);
            intent.putExtra(ACCOUNT_ID, AccountId);
            startActivityForResult(intent, REQ_CODE_UPLOAD_IMAGE);
        }

        if (requestCode == REQ_CODE_UPLOAD_IMAGE) {
            if (resultCode == RESULT_OK) {
                loadImages();
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Uploaded image cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        List<DocumentSnapshot> images;
        Context context;

        public ImageAdapter(List<DocumentSnapshot> images, Context context) {
            this.images = images;
            this.context = context;
        }

        @NonNull
        @Override
        public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ImageAdapter.ViewHolder holder, final int position) {

            final String imageUrl = images.get(position).getString(IMAGE);
            final String TAG = images.get(position).getString(Util.TAG);
            holder.tagTv.setText(TAG);
            if (null != imageUrl && !imageUrl.isEmpty()) {
                Picasso.with(context).load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(context).load(imageUrl).placeholder(R.drawable.ic_launcher_foreground).into(holder.imageView);
                            }
                        });
            }

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, SingleImageViewScreen.class)
                            .putExtra("image", imageUrl));
                }
            });

            holder.tagTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id = images.get(position).getId();
                    showUpdateTagDialog(holder, TAG, id);
                }
            });

            holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    new AlertDialog.Builder(context).setMessage("Want to delete this image permanently??")
                            .setPositiveButton("Yes, Delete it", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    progressBar.setVisibility(View.VISIBLE);
                                    String id = images.get(position).getId();
                                    deleteImage(id, v, position, images);
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            });
        }

        private void deleteImage(String id, final View v, final int position, final List<DocumentSnapshot> images) {
            firestore.collection(ACCOUNT_QUERY).document(AccountId).collection(IMAGE)
                    .document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(v, "Image Removed", Snackbar.LENGTH_SHORT).show();
                    images.remove(position);
                    notifyDataSetChanged();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Add_View_ImagesScreen.this, "try again", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }


        @Override
        public int getItemCount() {
            return images.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView, deleteIcon;
            TextView tagTv;
            RelativeLayout layout;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.imageView = itemView.findViewById(R.id.imageView2);
                this.deleteIcon = itemView.findViewById(R.id.imageView6);
                this.tagTv = itemView.findViewById(R.id.textView19);
                this.layout = itemView.findViewById(R.id.bottomLay);

            }
        }
    }


    private void showUpdateTagDialog(final ImageAdapter.ViewHolder holder, String tag, final String image_id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Add_View_ImagesScreen.this);

        // get the layout inflater
        LayoutInflater inflater = Add_View_ImagesScreen.this.getLayoutInflater();

        View view = inflater.inflate(R.layout.update_lay, null);
        final TextInputEditText inputEditText = view.findViewById(R.id.update_et);
        inputEditText.setHint(Util.TAG);
        builder.setView(view)

                // action buttons
                .setMessage("Update TAG")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final String text = inputEditText.getText().toString();
                        if (!text.isEmpty()) {
                            progressBar.setVisibility(View.VISIBLE);
                            firestore.collection(ACCOUNT_QUERY).document(AccountId).collection(IMAGE)
                                    .document(image_id)
                                    .update(Util.TAG, text).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressBar.setVisibility(View.GONE);
                                    holder.tagTv.setText(text);
                                    Toast.makeText(Add_View_ImagesScreen.this, "TAG updated successfully", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Add_View_ImagesScreen.this, "try again", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}