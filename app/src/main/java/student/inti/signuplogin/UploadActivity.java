package student.inti.signuplogin;

import static com.google.common.io.Files.getFileExtension;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadActivity extends AppCompatActivity {

    private ImageButton uploadButton;
    private ImageView uploadImage;
    private EditText uploadCaption;
    private ProgressBar progressBar;
    private Uri imageUri;
    private ImageButton backButton;
    private FirebaseAuth auth;

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private String userId; // get userId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        initializeUIElements();

        //get userId from authentication
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                imageUri = data.getData();
                                uploadImage.setImageURI(imageUri);
                            }
                        } else {
                            showToast("No Image Found");
                        }
                    }
                });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToNextActivity();
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker(activityResultLauncher);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    uploadToFirebase(imageUri, userId);
                } else {
                    showToast("Please select an Image!!");
                }
            }
        });
    }

    private void initializeUIElements() {
        auth = FirebaseAuth.getInstance();
        uploadButton = findViewById(R.id.uploadButton);
        uploadImage = findViewById(R.id.uploadImage);
        uploadCaption = findViewById(R.id.uploadCaption);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        backButton = findViewById(R.id.backButton);
    }

    private void openImagePicker(ActivityResultLauncher<Intent> launcher) {
        Intent photoPicker = new Intent(Intent.ACTION_GET_CONTENT);
        photoPicker.setType("image/*");
        launcher.launch(photoPicker);
    }

    private void uploadToFirebase(Uri uri, String userId) {
        String caption = uploadCaption.getText().toString();
        final StorageReference imageRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        imageRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uploadImageDetailsToFirestore(uri, caption, userId);
                            }
                        });
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        showToast("Failed To Upload!");
                    }
                });
    }

    private void uploadImageDetailsToFirestore(Uri uri, String caption, String userId) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("imageURL", uri.toString());
        data.put("caption", caption);
        data.put("userId", userId);
        data.put("timestamp", new Timestamp(new Date()));

        firestore.collection("Images").add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        progressBar.setVisibility(View.INVISIBLE);
                        showToast("Uploaded Successfully");
                        navigateToNextActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        showToast("Failed to upload!");
                    }
                });
    }

    private void navigateToNextActivity() {
        Intent intent = new Intent(UploadActivity.this, addNewNote.class);
        startActivity(intent);
        finish();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void showToast(String message) {
        Toast.makeText(UploadActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private String getUserIdFromAuth() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null; // Return user ID or null
    }

}