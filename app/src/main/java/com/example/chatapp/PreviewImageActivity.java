package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class PreviewImageActivity extends AppCompatActivity {


    ZoomageView picture;
    FloatingActionButton send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        picture = findViewById(R.id.imageView);
        send = findViewById(R.id.btn_send);

        final Uri image = Uri.parse(getIntent().getStringExtra("image"));
        final String sender = getIntent().getStringExtra("sender");
        final String receiver = getIntent().getStringExtra("receiver");

        Picasso.get().load(image).placeholder(R.drawable.ic_image_black_24dp)
                .into(picture);

        //        mActionBar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(new Intent(PreviewImageActivity.this,MessageActivity.class));
                intent.putExtra("userId",receiver);
                startActivity(intent);
                finish();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send Image Message
                try {
                    sendImageMessage(image,sender,receiver);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private void sendImageMessage(Uri image_rui, final String sender, final String receiver) throws IOException {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending Image...");
        progressDialog.show();

        final String timeStamp = ""+System.currentTimeMillis();
        String filePath = "ChatImages/"+"post_"+timeStamp;

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                image_rui);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data = baos.toByteArray();//image into byte
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if(uriTask.isSuccessful()){
                            //upload to db
                            DatabaseReference databaseReference=
                                    FirebaseDatabase.getInstance().getReference();
                            HashMap<String,Object> hashMap = new HashMap<>();

                            hashMap.put("sender",sender);
                            hashMap.put("reciever",receiver);
                            hashMap.put("message",downloadUri);
                            hashMap.put("type","image");
                            hashMap.put("timeStamp",timeStamp);
                            hashMap.put("isseen",false);

                            databaseReference.child("Chat").push().setValue(hashMap);
                            progressDialog.dismiss();

                            Intent intent = new Intent(new Intent(PreviewImageActivity.this,MessageActivity.class));
                            intent.putExtra("userId",receiver);
                            startActivity(intent);
                            finish();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });
    }

}
