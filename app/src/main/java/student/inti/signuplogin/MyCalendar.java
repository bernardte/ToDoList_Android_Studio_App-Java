package student.inti.signuplogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;

import student.inti.signuplogin.Adapter.ToDoAdapter;
import student.inti.signuplogin.Adapter.TouchHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MyCalendar extends AppCompatActivity {

    private static final String TAG = "MyCalendar";
    private CalendarView calendarView;
    private ListView eventListView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ArrayAdapter<String> adapter;
    private List<String> eventsList;
    private String stringDateSelected;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userId;
    private String sharedWith = null;
    private FirebaseFirestore mFirestore;
    private TextView userCurrentEmail;


    private Query mQuery;
    private ListenerRegistration listenerRegistration;
    private List<ToDoModel> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        sharedWith = Objects.requireNonNull(auth.getCurrentUser()).getUid();


        // Initialize views
        calendarView = findViewById(R.id.calendarView);
        eventListView = findViewById(R.id.eventListView);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        mFirestore = FirebaseFirestore.getInstance();

        // Initialize task list and adapter for ListView
        eventsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventsList);
        eventListView.setAdapter(adapter);

        // Set listener on CalendarView to fetch tasks for selected date
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Use Calendar to manipulate date
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);

            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            stringDateSelected = dateFormat.format(calendar.getTime());

            // Fetch tasks for the selected date
            fetchTasksForSelectedDate(stringDateSelected);

        });
        setupToolbarAndDrawer();
        fetchUserData();
    }



    // Set up toolbar and navigation drawer
    private void setupToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        getSupportActionBar().setSubtitle("Today, "+currentDate);
        getSupportActionBar().setTitle("Calendar");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();



        // NAVIGATION HERE----------------------------------------------------------------------------------------
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {

                Intent intent = new Intent(MyCalendar.this, MainActivity.class);
                startActivity(intent);

            } else if (id == R.id.nav_note) {

                Intent intent = new Intent(MyCalendar.this, addNewNote.class);
                startActivity(intent);

            } else if (id == R.id.logout) {
                logout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    //logout
    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MyCalendar.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();//finish current activity
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
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
                if(documentSnapshot.exists()){
                    String email = "";
                    email = documentSnapshot.getString("Email");
                    Log.d(TAG, "Email does exists! ");
                    userCurrentEmail.setText(email);
                }else{
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

    // Fetch tasks from Firestore based on the selected date
    private void fetchTasksForSelectedDate(String dateSelected) {
        // Clear the events list before fetching new tasks
        eventsList.clear();

        // Query 1: Fetch tasks based on due date
        Query dueQuery = firestore.collection("task")
                .whereEqualTo("userId", userId)
                .whereEqualTo("due", dateSelected);

        // Execute the first query for due date
        dueQuery.get().addOnSuccessListener(dueSnapshot -> {
            if (dueSnapshot != null && !dueSnapshot.isEmpty()) {
                for (DocumentSnapshot document : dueSnapshot.getDocuments()) {
                    ToDoModel toDoModel = document.toObject(ToDoModel.class);
                    if (toDoModel != null) {
                        addTaskToList(toDoModel);
                    }
                }
            }

            // Query 2: Fetch tasks based on sharedWith field
            Query sharedWithQuery = firestore.collection("task")
                    .whereEqualTo("sharedWith", userId)
                    .whereEqualTo("due", dateSelected);

            // Execute the second query for sharedWith
            sharedWithQuery.get().addOnSuccessListener(sharedSnapshot -> {
                if (sharedSnapshot != null && !sharedSnapshot.isEmpty()) {
                    for (DocumentSnapshot document : sharedSnapshot.getDocuments()) {
                        ToDoModel toDoModel = document.toObject(ToDoModel.class);
                        if (toDoModel != null) {
                            addTaskToList(toDoModel);
                        }
                    }
                }

                // Notify adapter after both queries have completed
                if (eventsList.isEmpty()) {
                    eventsList.add("No tasks for this date.");
                }
                adapter.notifyDataSetChanged();

            }).addOnFailureListener(e -> {
                Log.d(TAG, "Error fetching tasks based on sharedWith", e);
                Toast.makeText(MyCalendar.this, "Error fetching tasks!", Toast.LENGTH_SHORT).show();
            });

        }).addOnFailureListener(e -> {
            Log.d(TAG, "Error fetching tasks based on due date", e);
            Toast.makeText(MyCalendar.this, "Error fetching tasks!", Toast.LENGTH_SHORT).show();
        });
    }

    // Helper method to add a task to the events list without duplicates
    private void addTaskToList(ToDoModel toDoModel) {
        String taskWithTime = toDoModel.getTask() + "\n" + toDoModel.getDueTime();
        if (!eventsList.contains(taskWithTime)) { // Avoid duplicates
            eventsList.add(taskWithTime);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove(); // Remove the Firestore listener to prevent memory leaks
        }
    }
}