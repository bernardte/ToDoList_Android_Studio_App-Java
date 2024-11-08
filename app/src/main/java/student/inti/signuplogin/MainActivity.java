    package student.inti.signuplogin;

    import android.app.AlertDialog;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.graphics.drawable.ColorDrawable;
    import android.os.Bundle;
    import android.text.TextUtils;
    import android.util.Log;
    import android.util.Patterns;
    import android.view.Menu;
    import android.view.View;
    import android.widget.EditText;
    import android.widget.TextView;
    import android.widget.Toast;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.Locale;


    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.ActionBarDrawerToggle;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.core.view.GravityCompat;
    import androidx.drawerlayout.widget.DrawerLayout;
    import androidx.recyclerview.widget.ItemTouchHelper;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;


    import student.inti.signuplogin.Adapter.ToDoAdapter;
    import student.inti.signuplogin.Adapter.TouchHelper;
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.OnFailureListener;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.android.gms.tasks.Task;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;
    import com.google.android.material.navigation.NavigationView;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.DocumentChange;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.EventListener;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.FirebaseFirestoreException;
    import com.google.firebase.firestore.ListenerRegistration;
    import com.google.firebase.firestore.Query;
    import com.google.firebase.firestore.QuerySnapshot;
    import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Objects;

    public class MainActivity extends AppCompatActivity implements OnDialogCloseListener {

        private static final String TAG = "MainActivity";
        private RecyclerView recyclerView;
        private FloatingActionButton mFab;
        private FirebaseFirestore mFirestore;
        private ToDoAdapter adapter;
        private List<ToDoModel> mList;
        private Query mQuery;
        private ListenerRegistration listenerRegistration;
        private FirebaseAuth auth;
        private String userId = "";
        private String sharedWith = null;
        private DrawerLayout drawerLayout;
        private NavigationView navigationView;
        private TextView userCurrentEmail;
        private SwipeRefreshLayout swipeRefreshLayout;


        //onCreate is the MAIN, compiler runs here first
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            FirebaseFirestore.getInstance().getPersistentCacheIndexManager();


            initializeViews();
            setupToolbarAndDrawer();
            setupRecyclerView();
            fetchUserData();
            showData();




            // Refresh listener
            swipeRefreshLayout.setOnRefreshListener(() -> {
                mList.clear();
                showData(); // Reload
                swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
            });

        }


        private void initializeViews() {
            recyclerView = findViewById(R.id.recyclerview);
            mFab = findViewById(R.id.floatingActionButton);
            mFirestore = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);


            // Setup FAB for adding tasks
            mFab.setOnClickListener(view -> AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG));

            // Prepare the task list and adapter
            mList = new ArrayList<>();
            adapter = new ToDoAdapter(MainActivity.this, mList);
            recyclerView.setAdapter(adapter);

            // Attach ItemTouchHelper to RecyclerView
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(adapter));
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }


        // Set up toolbar and navigation drawer
        private void setupToolbarAndDrawer() {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());
            getSupportActionBar().setSubtitle("Today, "+currentDate);
            getSupportActionBar().setTitle("My Tasks");



            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();



            // NAVIGATION HERE----------------------------------------------------------------------------------------
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if(id == R.id.nav_home){

                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);

                } else if (id == R.id.nav_calendar) {

                    Intent intent = new Intent(MainActivity.this, MyCalendar.class);
                    startActivity(intent);

                } else if (id == R.id.logout) {
                    logout();

                } else if (id == R.id.nav_note) {
                    Intent intent = new Intent(MainActivity.this, addNewNote.class);
                    startActivity(intent);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
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

        // Setup RecyclerView with layout manager
        private void setupRecyclerView() {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));


        }



        // Fetch tasks from Firestore and display in RecyclerView
        private void showData() {
            userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
            sharedWith = Objects.requireNonNull(auth.getCurrentUser()).getUid();

            // Clear the list to avoid duplicates when reloading
            mList.clear();

            // Query 1: Get tasks where the user is the owner
            Query userOwnedTasksQuery = mFirestore.collection("task")
                    .whereEqualTo("userId", userId)
                    .orderBy("due", Query.Direction.ASCENDING);

            // Query 2: Get tasks where the user is in the sharedWith
            Query sharedWithUserQuery = mFirestore.collection("task")
                    .whereEqualTo("sharedWith", sharedWith)
                    .orderBy("due", Query.Direction.ASCENDING);

            // Flag to check if data is fetched from both queries
            boolean[] dataFetched = {false, false};

            // Listener for tasks owned by the user
            listenerRegistration = userOwnedTasksQuery.addSnapshotListener((value, error) -> {
                dataFetched[0] = true;
                handleQueryResult(value, error);
                checkAndShowNoTasksMessage(dataFetched);
            });

            // Listener for tasks shared with the user
            listenerRegistration = sharedWithUserQuery.addSnapshotListener((value, error) -> {
                dataFetched[1] = true;
                handleQueryResult(value, error);
                checkAndShowNoTasksMessage(dataFetched);
            });
        }

        // Method to handle query result and update the task list
        private void handleQueryResult(QuerySnapshot value, FirebaseFirestoreException error) {
            if (error != null) {
                Log.d(TAG, "Error fetching tasks: " + error.getMessage());
                return;
            }

            if (value != null) {
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        String id = documentChange.getDocument().getId();
                        ToDoModel toDoModel = documentChange.getDocument().toObject(ToDoModel.class).withId(id);
                        if (!mList.contains(toDoModel)) { // Avoid duplicates
                            mList.add(toDoModel);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }

        // Method to check if no tasks are available after both queries have fetched data
        private void checkAndShowNoTasksMessage(boolean[] dataFetched) {
            if (dataFetched[0] && dataFetched[1] && mList.isEmpty()) {
                Log.d(TAG, "No tasks available!");
                Toast.makeText(MainActivity.this, "No tasks available!", Toast.LENGTH_SHORT).show();
            }
        }


        //logout
        private void logout() {
            new AlertDialog.Builder(this)
                    .setTitle("Logout Confirmation")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();//finish current activity
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.nav_menu, menu);
//        return true;
//    }

        @Override
        public void onDialogClose(DialogInterface dialogInterface) {
            mList.clear();
            showData();
            adapter.notifyDataSetChanged();
        }
    }