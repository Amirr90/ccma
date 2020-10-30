package com.ccma;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class SingleImageViewScreen extends AppCompatActivity {
    String image;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image_view_screen);


        progressBar = findViewById(R.id.progressBar3);
        Toolbar toolbar = (Toolbar) findViewById(R.id.single_image_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("View Images");

        image = getIntent().getStringExtra("image");
        final ImageView imageViewBackground = findViewById(R.id.iv_auto_image_slider);
        Picasso.with(this).load(image).placeholder(R.drawable.ic_launcher_foreground)
                .networkPolicy(NetworkPolicy.OFFLINE).into(imageViewBackground, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onError() {
                progressBar.setVisibility(View.GONE);
                Picasso.with(SingleImageViewScreen.this).load(image).into(imageViewBackground);
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}