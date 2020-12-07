package com.lab3.lab3_game.UserAccount;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.lab3.lab3_game.R;

public class UserPageActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference myRef;
    private StorageReference reference;
    private Uri imgUri;
    private ProgressDialog progressDialog;
    private boolean gravatar = false;
    private String image;

    ImageView imageView;
    Button button;
    EditText editText;

    public static final String STORAGE_PATH = "image/";
    public static final int REQUEST_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        editText = findViewById(R.id.NameUser);
        button = findViewById(R.id.ApplyName);
        imageView = findViewById(R.id.imageView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        reference = firebaseStorage.getReference();
        myRef = database.getReference("Users").child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (Objects.equals(child.getKey(), "Gravatar")) {
                        if (Boolean.parseBoolean(Objects.requireNonNull(child.getValue()).toString())) {
                            gravatar = true;
                            ((RadioButton) findViewById(R.id.GravatarButton)).setChecked(true);
                            findViewById(R.id.ChooseImage).setVisibility(View.INVISIBLE);
                            findViewById(R.id.UpdateImage).setVisibility(View.INVISIBLE);
                        } else {
                            ((RadioButton) findViewById(R.id.FireBaseButton)).setChecked(true);
                            findViewById(R.id.ChooseImage).setVisibility(View.VISIBLE);
                            findViewById(R.id.UpdateImage).setVisibility(View.VISIBLE);
                        }
                    } else if (Objects.equals(child.getKey(), "userName")) {
                        editText.setHint(Objects.requireNonNull(child.getValue()).toString());
                    } else {
                        image = Objects.requireNonNull(child.getValue()).toString();
                        if (gravatar) {
                            String hash = md5(Objects.requireNonNull(firebaseAuth.getCurrentUser().getEmail()));
                            String gravatarUrl = "https://s.gravatar.com/avatar/" + hash + "?s=80";
                            Picasso.with(getApplicationContext())
                                    .load(gravatarUrl)
                                    .into(imageView);
                        } else {
                            Picasso.with(getApplicationContext())
                                    .load(image)
                                    .into(imageView);
                        }
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().isEmpty()) {
                    Query query = database.getReference().child("Users").orderByChild("userName").equalTo(editText.getText().toString());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                myRef.child("userName").setValue(editText.getText().toString());
                                Toast.makeText(UserPageActivity.this, "Changed successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserPageActivity.this, "This name already exists", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Toast.makeText(UserPageActivity.this, "Field is empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void btnBrowse_Click(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select image"), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imgUri = data.getData();
            try {
                Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                imageView.setImageBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getImageExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private String md5(String in) {
        String result = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            BigInteger bigInt = new BigInteger(1, digest.digest());
            result = bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public void btnUpload_Click(View v) {
        if (imgUri != null) {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Uploading Image");
            dialog.show();

            StorageReference ref = reference.child(STORAGE_PATH + System.currentTimeMillis() + "." + getImageExt(imgUri));
            ref.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(UserPageActivity.this.getApplicationContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getReference()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("Image", uri.toString());
                            myRef.updateChildren(childUpdates);
                        }
                    });

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(UserPageActivity.this.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                            dialog.setMessage("Uploaded" + (int) progress + "%");
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Please select image", Toast.LENGTH_SHORT).show();
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.GravatarButton:
                if (checked) {
                    findViewById(R.id.ChooseImage).setVisibility(View.INVISIBLE);
                    findViewById(R.id.UpdateImage).setVisibility(View.INVISIBLE);
                    String hash = md5(Objects.requireNonNull(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail()));
                    String gravatarUrl = "https://s.gravatar.com/avatar/" + hash + "?s=80";
                    Picasso.with(getApplicationContext())
                            .load(gravatarUrl)
                            .into(imageView);
                    myRef.child("Image").setValue(gravatarUrl);
                    myRef.child("Gravatar").setValue(true);
                }
                break;
            case R.id.FireBaseButton:
                if (checked) {
                    findViewById(R.id.ChooseImage).setVisibility(View.VISIBLE);
                    findViewById(R.id.UpdateImage).setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext())
                            .load(image)
                            .into(imageView);
                    myRef.child("Gravatar").setValue(false);
                    myRef.child("Image").setValue("https://firebasestorage.googleapis.com/v0/b/playgamekl.appspot.com/o/image%2F1606652344403.png?alt=media&token=e5c51ce4-3939-490e-9d4f-fc5ddeef127e");
                }
                break;
        }
    }

}