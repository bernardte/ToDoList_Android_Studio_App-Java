package student.inti.signuplogin.Adapter;


import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Objects;

import android.content.Context;

import android.os.Bundle;
import android.widget.Toast;

import student.inti.signuplogin.LoginActivity;
import student.inti.signuplogin.R;
import student.inti.signuplogin.ToDoModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import student.inti.signuplogin.AddNewTask;

import student.inti.signuplogin.MainActivity;

import student.inti.signuplogin.TaskId;

import student.inti.signuplogin.ToDoModel;
import com.google.firebase.firestore.QuerySnapshot;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder>{

    private List <ToDoModel> todoList;
    private student.inti.signuplogin.MainActivity activity;
    private FirebaseFirestore mFirestore;




    //shareTask = findViewById(R.id.shareTask);

    public ToDoAdapter(student.inti.signuplogin.MainActivity mainActivity, List<ToDoModel> todoList){
        this.todoList = todoList;
        activity = mainActivity;
    }

    //deleted task
    public void deletedTask(int position){
        ToDoModel todoModel = todoList.get(position);
        mFirestore.collection("task").document(todoModel.TaskId).delete();
        todoList.remove(position);
        notifyItemRemoved(position);

    }

    public Context getContext(){
        return activity;
    }

    //edit task
    public void editTask(int position){
        ToDoModel toDoModel = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("task", toDoModel.getTask());
        bundle.putString("due", toDoModel.getDue());
        bundle.putString("dueTime", toDoModel.getDueTime());
        bundle.putString("id", toDoModel.TaskId);
        AddNewTask addNewTask = new AddNewTask();
        //pass the bundle to the addNewTask
        addNewTask.setArguments(bundle);
        //form to a swipe view with the addNewTask
        addNewTask.show(activity.getSupportFragmentManager(), addNewTask.getTag());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mFirestore = FirebaseFirestore.getInstance();

        View view = LayoutInflater.from(activity).inflate(R.layout.each_task, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ToDoModel toDoModel = todoList.get(position);
        // set the task
        holder.mCheckBox.setText(toDoModel.getTask());
        // set the due date
        holder.mDueDateTv.setText("Due Date: " + toDoModel.getDue());
        // set the due time
        holder.mDueTimeTv.setText("Due Time: " + toDoModel.getDueTime());
        // set the status
        holder.mCheckBox.setChecked(convertToBoolean(toDoModel.getStatus()));

        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(holder.itemView.getContext(), "Checked", Toast.LENGTH_SHORT).show(); // Use holder.itemView.getContext()
                    mFirestore.collection("task").document(toDoModel.TaskId).update("status", 1);
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Unchecked", Toast.LENGTH_SHORT).show(); // Use holder.itemView.getContext()
                    mFirestore.collection("task").document(toDoModel.TaskId).update("status", 0);
                }
            }
        });



        // Share button logic: Open the activity_share_container.xml dialog
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShareDialog(holder.itemView.getContext(), toDoModel); // Call the method to open the dialog
            }
        });

    }

    // Method to open the share dialog
    private void openShareDialog(Context context, ToDoModel toDoModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.activity_share_container, null);
        EditText emailBox = dialogView.findViewById(R.id.emailBox);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = emailBox.getText().toString().trim();

                if(TextUtils.isEmpty(userEmail)){
                    Toast.makeText(context, "Email cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                    Toast.makeText(context, "Invalid email format!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!TextUtils.isEmpty(userEmail) && Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    // Step 1: Fetch userId from Firestore using the provided userEmail
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("userDetails").whereEqualTo("Email", userEmail).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                        DocumentSnapshot userDocument = task.getResult().getDocuments().get(0);
                                        String userId = userDocument.getId();  // userId is the document ID from collection:task

                                        // Step 2: Locate the taskId in the 'tasks' collection
                                        db.collection("task").document(toDoModel.TaskId)
                                                .update("sharedWith", userId)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // Successfully updated the task with the userId
                                                            Toast.makeText(context, "Task shared with " + userEmail, Toast.LENGTH_SHORT).show();
                                                            dialog.dismiss(); // Close the dialog
                                                        } else {
                                                            // Handle failure to update the task
                                                            Toast.makeText(context, "Failed to share task: "
                                                                    + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        // Handle case where no user with that email was found
                                        Toast.makeText(context, "No user found with this email.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }


            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss(); // Close the dialog
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0)); // Make the dialog background transparent
        }
        dialog.show(); // Show the dialog
    }

    // This method converts an int status (1/0) to a boolean (true/false)
    public boolean convertToBoolean(int status){
        return status != 0; // Returns true if status is not 0, otherwise false
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView mDueDateTv;
        TextView mDueTimeTv;
        CheckBox mCheckBox;
        TextView shareButton;

        @SuppressLint("WrongViewCast")
        public MyViewHolder(@NonNull View itemView){
            super(itemView);

            mDueDateTv = itemView.findViewById(R.id.due_date_tv);
            mDueTimeTv = itemView.findViewById(R.id.set_due_time_tv);
            mCheckBox = itemView.findViewById(R.id.mcheckbox);
            shareButton = itemView.findViewById(R.id.shareTask);
        }
    }

}
