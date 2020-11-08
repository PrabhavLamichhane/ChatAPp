package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class FirstActivity extends AppCompatActivity {


    Button next;

    ImageView profile;
    EditText name;
    FloatingActionButton changePic;

//    Firebase auth
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


//    FirebaseDatabase
    DatabaseReference reference;

    ProgressDialog progressDialog;
    Dialog myDialog;

    ProgressDialog pd;

    StorageReference storageReference;

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
        setContentView(R.layout.activity_first);

        next = findViewById(R.id.next);
        profile = findViewById(R.id.image_profile);
        name = findViewById(R.id.name);
        changePic = findViewById(R.id.fab_camera);


        //        Progress Dialog
        myDialog = new Dialog(this);

        progressDialog = new ProgressDialog(this);
        pd = new ProgressDialog(this);
        progressDialog.setMessage("Wait...");

//        firebase auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
//        firebase database
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        storageReference = FirebaseStorage.getInstance().getReference();//firebase storage reference

        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.setMessage("Updating Profile Picture...");
                showImagePicDialog();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(currentUser == null){
//                    save name and profile pic to db
//                     sign in anonymously
                    progressDialog.show();
                    signInAnonymously();
                }else {
                    //may be change profile pic and name
                    sendToMain();
                }


            }
        });
    }

    private void showImagePicDialog() {
        //        Show edit profile options
        String options[] = {"Camera","Gallery"};
//        alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(FirstActivity.this);
//        Set title
        builder.setTitle("Pick Image From...");
//        Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle dialog item click
                if(i==0){
                    //Camera click
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }

                }
                else if (i==1){
                    //Gallery click
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else{
                        pickFromGallery();
                    }

                }


            }
        });
        builder.create().show();
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
                        Toast.makeText(this,"Storage permission is necessary",Toast.LENGTH_LONG).show();
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
                //Set to image view
                profile.setImageURI(image_rui);
            }else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                // Add code later on
                profile.setImageURI(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void uploadUserPhoto(final Uri uri) {



        String filePathAndName = "Profile/"+"profile_"+currentUser.getUid();
        StorageReference storageReference1 = storageReference.child(filePathAndName);

        Log.d("path--", "uploadUserPhoto: "+filePathAndName);
        Log.d("ref--", "uploadUserPhoto: "+storageReference1);


        storageReference1.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                final Uri downLoadUri = uriTask.getResult();

                if(uriTask.isSuccessful()){

                    HashMap<String,Object> results = new HashMap<>();
                    results.put("image",downLoadUri.toString());
                    reference.child(currentUser.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    sendToMain();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(FirstActivity.this,"Error updating image",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }else{
                    Toast.makeText(FirstActivity.this, "Oops! Something went wrong...", Toast.LENGTH_SHORT).show();
                }


// end
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(FirstActivity.this, "No..."+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }



    public void sendToMain(){
        Intent intent = new Intent(FirstActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void signInAnonymously(){

        final String uName = name.getText().toString().trim();

        // Deal with image later on
        if(TextUtils.isEmpty(uName)){
            name.setError("Your name cannot be empty...");
            progressDialog.dismiss();
        }else {
            final String userName = Character.toUpperCase(uName.charAt(0)) + uName.substring(1);;
            mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //store in db
                        //Also deal with profile pic later on
                        storeInDb(userName);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(FirstActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FirstActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("", "onFailure: " + e.getMessage());
                }
            });
        }
    }

    public void storeInDb(String userName){

        currentUser = mAuth.getCurrentUser();
        User user = new User(userName,"",currentUser.getUid(),"offline");

        reference.child(currentUser.getUid())
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()) {
                    if(image_rui!=null) {
                        uploadUserPhoto(image_rui);
                    }else {
                        progressDialog.dismiss();
                        sendToMain();
                    }
                }else{
                    Toast.makeText(FirstActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FirstActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("", "onFailure: "+e.getMessage());
            }
        });

    }

}
