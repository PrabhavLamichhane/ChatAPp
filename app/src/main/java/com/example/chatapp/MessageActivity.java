package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.chatapp.adapters.MessageAdapter;
import com.example.chatapp.adapters.UsersAdapter;
import com.example.chatapp.models.Chat;
import com.example.chatapp.models.User;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    ImageView userProfile;
    TextView userName;

    ImageView send,cameraBtn,attachBtn;
    EditText msg;

    String userId;

    RecyclerView recyclerView;
    List<Chat> chats;

    MessageAdapter adapter;

    FirebaseUser currentUser;
    DatabaseReference reference;

    ValueEventListener seenListner;

    //    Requesting camera
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;

    //Reqeusting gallery
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //Permission array
    String[] storagePermission;
    String[] cameraPermission;

    Uri image_rui = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        userProfile = findViewById(R.id.userProfile);
        userName = findViewById(R.id.username);

        send = findViewById(R.id.btnSend);
        msg = findViewById(R.id.msg);
        cameraBtn = findViewById(R.id.cameraBtn);
        attachBtn = findViewById(R.id.attachBtn);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);



        // toolbar code later on
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

//        mActionBar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this,MainActivity.class));
                finish();
            }
        });


        Intent intent = getIntent();

        userId = intent.getStringExtra("userId");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!checkCameraPermission()){
                    requestCameraPermission();
                }else{
                    pickFromCamera();
                }

            }
        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkStoragePermission()){
                    requestStoragePermission();
                }else{
                    pickFromGallery();
                }
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = msg.getText().toString();
                if(!message.equals("")){
                    sendMessage(currentUser.getUid(),userId,message);
                }else{
                    Log.d("TAG", "onClick: empty message");
                }


                msg.setText("");
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                userName.setText(user.getName());

//                Deal with image later on

                try{
                    Picasso.get().load(user.getImage()).into(userProfile);
                }catch (Exception e){
                    Picasso.get().load(R.drawable.people).into(userProfile);
                }

//                Image url later on
                readMessage(currentUser.getUid(),user.getUserid(),user.getImage());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        seenMessage(userId);

    }

    public void sendMessage(String sender, final String receiver, String message){
        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String,Object> hashMap = new HashMap<>();

        hashMap.put("sender",sender);
        hashMap.put("reciever",receiver);
        hashMap.put("message",message);
        hashMap.put("type","text");
        hashMap.put("timeStamp",timeStamp);
        hashMap.put("isseen",false);

        databaseReference.child("Chat").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(currentUser.getUid())
                .child(userId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userId);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(receiver)
                .child(currentUser.getUid());

        chatRefReceiver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatRefReceiver.child("id").setValue(currentUser.getUid());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void readMessage(final String myId, final String userId, final String imageUrl){
        chats = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chat");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);

                    Log.d("val", "onDataChange: "+
                            "receiverId"+chat.getReciever()+".... senderId"+chat.getSender()+"\n"+
                            "myId"+myId+"...userId"+userId);
                    if(chat.getReciever().equals(myId)  && chat.getSender().equals(userId)
                            || chat.getReciever().equals(userId) && chat.getSender().equals(myId)){
                        Log.d("", "onDataChange: chat read");
                        chats.add(chat);
                    }
                }

                adapter = new MessageAdapter(MessageActivity.this,chats, imageUrl);
                adapter.notifyDataSetChanged();
                Log.d("adapter---", "onDataChange: "+adapter);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void seenMessage(final String userId){
        reference = FirebaseDatabase.getInstance().getReference("Chat");

        seenListner = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    if(chat.getReciever().equals(currentUser.getUid()) && chat.getSender().equals(userId)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);

        reference.updateChildren(hashMap);
    }


    @Override
    protected void onResume() {
        super.onResume();
        status("online");

    }

    @Override
    protected void onStop() {
        super.onStop();
        status("offline");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListner);
        status("online");
    }

    @Override
    protected void onStart() {
        super.onStart();
        status("online");
    }

    private boolean checkStoragePermission(){
        //Check if storage permission is enabled or not
        //return true if enabled false if not enabled getActivity() instead of this
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return  result;
    }

    public void requestStoragePermission(){
        //request runtime request camera permission
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //Check if camera permission is enabled or not
        //return true if enabled false if not enabled getActivity() instead of this
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return  result && result1;

    }

    public void requestCameraPermission(){
        //request runtime request camera permission
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);

    }

    public void pickFromCamera(){

        //Intent to pick image from camera
        ContentValues cv= new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_rui);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    public void pickFromGallery(){
//        Intent to pick image from gallery later discard .gif image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);

    }

    //Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){

            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted  && storageAccepted ){
                        //Both permission are granted
                        pickFromCamera();
                    }else{
                        //Permision denied
                        Toast.makeText(this,"Camera and Storage permission are necessary",Toast.LENGTH_LONG).show();
                    }
                }

                break;

            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0){

                    //        boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED; change this
                    // if it doesnot 1 instead of 0 work
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //Storage permission are granted
                        pickFromGallery();
                    }else{
                        //Permision denied
                        Toast.makeText(this,"Storage permission is necessary",Toast.LENGTH_LONG);
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //It is called after picking img from camera or gallery
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //Img is picked from gallery get uri of an image
                image_rui = data.getData();

                try {
                    Intent intent = new Intent(MessageActivity.this, PreviewImageActivity.class);
                    intent.putExtra("image", image_rui.toString());
                    intent.putExtra("sender", currentUser.getUid());
                    intent.putExtra("receiver", userId);
                    startActivity(intent);
                    finish();
                }catch (Exception e){
                    e.printStackTrace();
                }



            }else if(requestCode == IMAGE_PICK_CAMERA_CODE){
//                image_rui = data.getData();
                try {
                    Intent intent = new Intent(MessageActivity.this, PreviewImageActivity.class);
                    intent.putExtra("image", image_rui.toString());
                    intent.putExtra("sender", currentUser.getUid());
                    intent.putExtra("receiver", userId);
                    startActivity(intent);
                    finish();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
