package student.inti.signuplogin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nullable;

import student.inti.signuplogin.Adapter.DisplayNote;
import student.inti.signuplogin.DataClass;
import student.inti.signuplogin.UploadActivity;

public class addNewNote extends AppCompatActivity {

    private static final String TAG = "addNewNote";
    FloatingActionButton fab;
    RecyclerView recyclerView;
    ArrayList<DataClass> dataList;
    DisplayNote displayNote;
    private FirebaseAuth auth;
    private String userId;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView userCurrentEmail;
    private FirebaseFirestore mFirestore;
    private SearchView searchView; //search

    // Firestore reference
    final private CollectionReference dataReference = FirebaseFirestore.getInstance().collection("Images");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_note);

        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(addNewNote.this));
        dataList = new ArrayList<>();
        displayNote = new DisplayNote(dataList, this);
        recyclerView.setAdapter(displayNote);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        mFirestore = FirebaseFirestore.getInstance();

        searchView = findViewById(R.id.searchView);//search function

        setupToolbarAndDrawer();
        fetchUserData();
        // Fetch data from Firestore
        dataReference.whereEqualTo("userId", userId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                fetchData(snapshots);
            }
        });

        //search function
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            //perform search when submit has press
            public boolean onQueryTextSubmit(String query) {
                displayNote.filterImageNote(query);
                return true;
            }

            //perform live search will typing
            @Override
            public boolean onQueryTextChange(String query) {
                displayNote.filterImageNote(query); // Perform live filtering
                return true;
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(addNewNote.this, UploadActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    // Fetch user data from Firestore to display in the navigation drawer header
    private void fetchUserData() {
        //get the header view from index 0, means only have one in the activity_main.xml, so the index is 0
        View headerView = navigationView.getHeaderView(0);
        userCurrentEmail = headerView.findViewById(R.id.userCurrentEmail);


        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        //to get the data from the firebase by using a class call DocumentReference and name it is instance as documentReference.
        DocumentReference documentReference = mFirestore.collection("userDetails").document(userId);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String email = "";
                    email = documentSnapshot.getString("Email");
                    Log.d(TAG, "Email does exists! ");
                    userCurrentEmail.setText(email);
                } else {
                    Log.d(TAG, "Email does not exists! ");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error fetching document", e);
            }
        });
    }


    private void setupToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        getSupportActionBar().setSubtitle("Today, " + currentDate);
        getSupportActionBar().setTitle("My Notes");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // NAVIGATION HERE----------------------------------------------------------------------------------------
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {

                Intent intent = new Intent(addNewNote.this, MainActivity.class);
                startActivity(intent);

            } else if (id == R.id.nav_calendar) {

                Intent intent = new Intent(addNewNote.this, MyCalendar.class);
                startActivity(intent);

            } else if (id == R.id.logout) {
                logout();

            } else if (id == R.id.nav_note) {
                Intent intent = new Intent(addNewNote.this, addNewNote.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.nav_menu, menu);
//        return true;
//    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(addNewNote.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }


    private void fetchData(QuerySnapshot snapshots) {
        if (snapshots != null) {
            dataList.clear();
            displayNote.getFullDataList().clear(); // Clear previous data
            for (QueryDocumentSnapshot document : snapshots) {
                String documentId = document.getId();
                String imageURL = document.getString("imageURL");
                String caption = document.getString("caption");
                String userId = document.getString("userId");

                // Create DataClass instance
                DataClass dataClass = new DataClass(imageURL, caption, userId, documentId);
                dataList.add(dataClass);
                displayNote.getFullDataList().add(dataClass); // Add to full data list
            }
            displayNote.notifyDataSetChanged();

        }
    }
}

