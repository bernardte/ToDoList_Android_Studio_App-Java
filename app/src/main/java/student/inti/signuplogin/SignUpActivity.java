package student.inti.signuplogin;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;
    private CheckBox mCheckBox, confirmPassCheckBox;
    private EditText confirm_signup_password;
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        mCheckBox = findViewById(R.id.password_toggle);
        confirmPassCheckBox = findViewById(R.id.confirm_password_toggle);
        confirm_signup_password = findViewById(R.id.confirm_signup_password);
        FirebaseAuth auth = FirebaseAuth.getInstance();//create and object of firebase
        firestore = FirebaseFirestore.getInstance();

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                showPassword(isChecked, signupPassword);
            }
        });

        confirmPassCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                showPassword(isChecked, confirm_signup_password);
            }
        });

        //Validate email and password
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = signupEmail.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();
                String confirmPass = confirm_signup_password.getText().toString().trim();

                if (user.isEmpty()) {
                    signupEmail.setError("Email cannot be empty, Please fill in...");
                    signupEmail.requestFocus();
                }

                if (pass.isEmpty() || confirmPass.isEmpty()) {
                    signupPassword.setError("Password cannot be empty,Please fill in...");
                    signupPassword.requestFocus();
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(user).matches()) {
                    signupEmail.setError("Must match email format");
                    signupEmail.requestFocus();
                } else if (pass.length() < 6 || confirmPass.length() < 6) {
                    signupPassword.setError("Password must contain at least 6 numbers");
                    signupPassword.requestFocus();
                } else if (!pass.equals(confirmPass)) {
                    signupPassword.setError("Both password must be match");
                } else {
                    //successfully then update to the database
                    auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Signup Successfully", Toast.LENGTH_SHORT).show();
                                userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                                DocumentReference documentReference = firestore.collection("userDetails").document(userId);
                                HashMap<String, Object> users = new HashMap<>();
                                users.put("Email", user);
                                documentReference.set(users).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "OnSuccess: user Profile successfully created " + userId);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "OnFailure: unsuccessfully created user profile due to " + e.getMessage());
                                    }
                                });
                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            } else {
                                Toast.makeText(SignUpActivity.this, "SignUp Unsuccessfully" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }


            }
        });
       /* Origin code
       loginRedirectText.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
           }
       });
       */
        //after shortcut code
        loginRedirectText.setOnClickListener(view -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));
    }

    //Show password checkbox
    private void showPassword(boolean isChecked, EditText password_display) {

        //checkbox status is changed from uncheck to check
        if (!isChecked) {
            //Password visible
            password_display.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            //Hide password
            password_display.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    }
}