package com.example.chatapp;

import android.Manifest;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment {


    ImageButton changeName,changeEmail;
    ImageView imageProfile;
    FloatingActionButton changePic;
    TextView userName,email;
//    AdView adView1;

    ProgressDialog pd;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);


        imageProfile = view.findViewById(R.id.image_profile);
        changePic = view.findViewById(R.id.fab_camera);
        userName =view.findViewById(R.id.tv_username);
        changeName = view.findViewById(R.id.changeName);

        //        init progress dialog
        pd = new ProgressDialog(getContext());

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();//firebase storage reference

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Query query = databaseReference.orderByChild("userid").equalTo(user.getUid());

        query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot ds: snapshot.getChildren()){
                            // get data
                            Log.d("ds", "onDataChange: "+ds);
                            String name = "" + ds.child("name").getValue();
                            String userImage = "" + ds.child("image").getValue();


                            //set data
                            Log.d("name:", "onDataChange: "+name);
                            userName.setText(name);

                            // for userImage
                            try{
                                Picasso.get().load(userImage).into(imageProfile);
                            }catch (Exception e){
                                Picasso.get().load(R.drawable.people).into(imageProfile);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.setMessage("Updating Profile Picture...");
                showImagePicDialog();
            }
        });

        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.setMessage("Updating Name...");
                showNameUpdateDialog("name");
            }
        });


        return view;
    }


    private void showNameUpdateDialog(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Update "+ key);
        //        Set layout for dialog
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
//        Add edit text
        final EditText editText = new EditText(getContext());
        editText.setHint("Enter "+ key);

        linearLayout.addView(editText);
        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final String value = editText.getText().toString().trim();

                if(!TextUtils.isEmpty(value)) {
                    HashMap<String,Object> result = new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getContext(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getContext(),"Failed "+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    editText.setError("Please enter the name");
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create().show();
    }


    private void showImagePicDialog() {
        //        Show edit profile options
        String options[] = {"Camera","Gallery"};
//        alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        boolean result = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return  result;
    }

    public void requestStoragePermission(){
        //request runtime request camera permission
        ActivityCompat.requestPermissions(getActivity(),storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //Check if camera permission is enabled or not
        //return true if enabled false if not enabled getActivity() instead of this
        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return  result && result1;

    }

    public void requestCameraPermission(){
        //request runtime request camera permission
        ActivityCompat.requestPermissions(getActivity(),cameraPermission,CAMERA_REQUEST_CODE);

    }

    public void pickFromCamera(){

        //Intent to pick image from camera
        ContentValues cv= new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_rui = getActivity().getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

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
                        Toast.makeText(getContext(),"Camera and Storage permission are necessary",Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getContext(),"Storage permission is necessary",Toast.LENGTH_LONG);
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
//                imagePostIv.setImageURI(image_rui);
                uploadUserPhoto(image_rui);
            }else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                // Add code later on
                uploadUserPhoto(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadUserPhoto(final Uri uri) {

        pd.setMessage("Updating profile...");
        pd.show();
        String filePathAndName = "Profile/"+"profile_"+user.getUid();
        StorageReference storageReference1 = storageReference.child(filePathAndName);

        storageReference1.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                final Uri downLoadUri = uriTask.getResult();

                if(uriTask.isSuccessful()){

                    HashMap<String,Object> results = new HashMap<>();
                    results.put("image",downLoadUri.toString());
                    databaseReference.child(user.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getContext(),"Profile Pic Updated",
                                            Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getContext(),"Error updating image",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }else{
                    pd.dismiss();
                    Toast.makeText(getContext(), "Oops! Something went wrong...", Toast.LENGTH_SHORT).show();
                }


// end
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getContext(), "No..."+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


}
