package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Picasso;

public class PicPreviewActivity extends AppCompatActivity {

    ZoomageView picture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_preview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(new Intent(PicPreviewActivity.this,MainActivity.class));
                startActivity(intent);
                finish();
            }
        });


        picture = findViewById(R.id.imageView);
        final Uri image = Uri.parse(getIntent().getStringExtra("image"));
        Picasso.get().load(image).placeholder(R.drawable.ic_image_black_24dp)
                .into(picture);
    }
}
