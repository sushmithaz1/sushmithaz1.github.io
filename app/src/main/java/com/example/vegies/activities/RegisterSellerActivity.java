package com.example.vegies.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.vegies.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.*;

public class RegisterSellerActivity extends AppCompatActivity implements LocationListener{
    private ImageButton backBtn,gpsBtn;
    private ImageView profileTv;
    private EditText NameEt,ShopNameEt,PhoneEt,deliveryFeeEt,countryEt,stateEt,cityEt,adressEt,emailEt,passwordEt
            ,cPasswordEt;
    private Button registerBtn;
    private double latitude =0.0,longitude =0.0;

    private static final int LOCATION_REQUEST_CODE=100;
    private static final int CAMERA_REQUEST_CODE=200;
    private static final int STORAGE_REQUEST_CODE=300;

    private static final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int IMAGE_PICK_CAMERA_CODE=500;



    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri image_uri;

    private LocationManager locationManager;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seller);

        backBtn=findViewById(R.id.backBtn);
        gpsBtn=findViewById(R.id.gpsBtn);
        profileTv=findViewById(R.id.profileTv);
        NameEt=findViewById(R.id.NameEt);
        ShopNameEt=findViewById(R.id.ShopNameEt);
        PhoneEt=findViewById(R.id.PhoneEt);
        deliveryFeeEt=findViewById(R.id.deliveryFeeEt);
        countryEt=findViewById(R.id.countryEt);
        stateEt=findViewById(R.id.stateEt);
        cityEt=findViewById(R.id.cityEt);
        adressEt=findViewById(R.id.addressEt);
        emailEt=findViewById(R.id.emailEt);
        passwordEt=findViewById(R.id.passwordEt);
        cPasswordEt=findViewById(R.id.cPasswordEt);
        registerBtn=findViewById(R.id.registerBtn);

        locationPermissions=new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkLocationPermission()){
                    detectLocation();
                }
                else{
                    requestLocationPermission();
                }
            }
        });
        profileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }

        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
    }
    private String fullName,shopName,phoneNumber,deliveryFee,country,state,city,address,email,password,confirmPassword;
    private void inputData() {
        fullName=NameEt.getText().toString().trim();
        shopName=ShopNameEt.getText().toString().trim();
        phoneNumber=PhoneEt.getText().toString().trim();
        deliveryFee=deliveryFeeEt.getText().toString().trim();
        country=countryEt.getText().toString().trim();
        address=adressEt.getText().toString().trim();
        email=emailEt.getText().toString().trim();
        password=passwordEt.getText().toString().trim();
        confirmPassword=cPasswordEt.getText().toString().trim();

        //validate data
        if(TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(shopName)){
            Toast.makeText(this, "Enter Shop Name...", Toast.LENGTH_SHORT).show();
            return;
        } if(TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Enter Phone Number...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(deliveryFee)){
            Toast.makeText(this, "Enter Delivery Fee...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(latitude==0.0||longitude==0.0){
            Toast.makeText(this, "Please click gps button to detect location...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email Patterns...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length()<6){
            Toast.makeText(this, "Password must be atleast 6 characters...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!(password.equals(confirmPassword))){
            Toast.makeText(this, "password doesnot match...", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();
    }

    private void createAccount() {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //create acc
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //account cerated
                saverFirebaseData();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        progressDialog.dismiss();
                        Toast.makeText(RegisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saverFirebaseData() {
        progressDialog.setMessage("Saving account information...");
        final String timestamp=""+System.currentTimeMillis();
        if(image_uri==null){
            //save info without image
            HashMap<String,Object> hashMap=new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("email",""+email);
            hashMap.put("name",""+ fullName);
            hashMap.put("shopName",""+shopName);
            hashMap.put("phone",""+phoneNumber);
            hashMap.put("deliveryFee",""+deliveryFee);
            hashMap.put("country",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("accountType","Seller");
            hashMap.put("online","true");
            hashMap.put("shopOpen","true");
            hashMap.put("profileImage","");

            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("vegies");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //dp updated;
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterSellerActivity.this, MainSellerActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed updating db
                    progressDialog.dismiss();
                    startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                    finish();
                }
            });
        }
        else{
            String filePathAndName="profile_images/"+""+firebaseAuth.getUid();
            //upload image
            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of uploaded img
                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while(uriTask.isSuccessful());
                            Uri downloadImageUri =uriTask.getResult();
                            if(uriTask.isSuccessful()){
                                HashMap<String,Object> hashMap=new HashMap<>();
                                hashMap.put("eid",""+firebaseAuth.getUid());
                                hashMap.put("email",""+email);
                                hashMap.put("name",""+ fullName);
                                hashMap.put("shopName",""+shopName);
                                hashMap.put("phone",""+phoneNumber);
                                hashMap.put("deliveryFee",""+deliveryFee);
                                hashMap.put("country",""+country);
                                hashMap.put("state",""+state);
                                hashMap.put("city",""+city);
                                hashMap.put("address",""+address);
                                hashMap.put("latitude",""+latitude);
                                hashMap.put("longitude",""+longitude);
                                hashMap.put("timestamp",""+timestamp);
                                hashMap.put("accountType","Seller");
                                hashMap.put("online","true");
                                hashMap.put("shopOpen","true");
                                hashMap.put("profileImage",""+downloadImageUri);//url of upload image

                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("vegies");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //dp updated;
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed updating db
                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                                        finish();
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showImagePickDialog() {
        String[] options={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    if(checkCameraPermission()){
                        pickFromCamera();
                    }
                    else{
                        requestCameraPermission();
                    }
                }
                else{
                    if(checkStoragePermission()){
                        pickFromGallery();
                    }
                    else{
                        requestStoragePermission();
                    }
                }
            }
        }).show();
    }

    private void pickFromGallery(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/e");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera(){
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image Decription");

        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Firebase wait.....", Toast.LENGTH_LONG).show();
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
    }
    private void findAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder=new Geocoder(this, Locale.getDefault());

        try{
            addresses=geocoder.getFromLocation(latitude,longitude,1);
            String address=addresses.get(0).getAddressLine(0);
            String city=addresses.get(0).getLocality();
            String state=addresses.get(0).getAdminArea();
            String country=addresses.get(0).getCountryName();

            countryEt.setText(country);
            stateEt.setText(state);
            cityEt.setText(city);
            adressEt.setText(address);
        }
        catch(Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private boolean checkLocationPermission(){
        boolean result= ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result=ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);

    }
    private boolean checkCameraPermission(){
        boolean result=ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1=ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result&& result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }
    @Override
    public void onLocationChanged(Location location) {
    latitude=location.getLatitude();
    longitude=location.getLongitude();

    findAddress();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Please turn onLocation...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length == 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        detectLocation();
                    } else {
                        Toast.makeText(this, "Location Permission is neccessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
                case CAMERA_REQUEST_CODE: {
                    if (grantResults.length == 0) {
                        boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                        if (cameraAccepted&&storageAccepted) {
                            pickFromCamera();
                        } else {
                            Toast.makeText(this, "camera Permission is neccessary", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length == 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "storage Permission is neccessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                image_uri=data.getData();
                profileTv.setImageURI(image_uri);

            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                profileTv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
