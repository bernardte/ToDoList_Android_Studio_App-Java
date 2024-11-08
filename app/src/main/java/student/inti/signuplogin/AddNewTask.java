    package student.inti.signuplogin;

    import static android.app.Activity.RESULT_OK;

    import android.app.Activity;
    import android.app.DatePickerDialog;
    import android.app.TimePickerDialog;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.icu.util.Calendar;
    import android.net.Uri;
    import android.os.Bundle;
    import android.speech.RecognitionService;
    import android.speech.RecognizerIntent;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.DatePicker;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.TextView;
    import android.widget.Toast;
    import java.util.ArrayList;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.core.app.NotificationManagerCompat;
    import androidx.fragment.app.Fragment;

    import student.inti.signuplogin.Reminder.ReminderWorker;
    import student.inti.signuplogin.databinding.ActivityMainBinding;
    import student.inti.signuplogin.databinding.ReminderDialogBinding;
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.OnFailureListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.FieldValue;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

    import android.app.AlertDialog;
    import android.os.Build;
    import android.provider.Settings;
    import android.util.Log;
    import android.widget.ArrayAdapter;

    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.work.Data;
    import androidx.work.OneTimeWorkRequest;
    import androidx.work.WorkManager;

    import com.google.android.material.dialog.MaterialAlertDialogBuilder;

    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Locale;
    import java.util.Objects;
    import java.util.concurrent.TimeUnit;

    public class AddNewTask extends BottomSheetDialogFragment {

        public static final String TAG = "AddNewTask";
        private TextView setDueDate, setDueTime;
        private EditText mTaskEdit;
        private Button mSaveBtn;
        private FirebaseFirestore firestore;
        private FirebaseAuth auth;
        private Context context;
        private String dueDate = "";
        private String dueTime = "";
        private int hour, minute;
        private String userId = "";
        private String id = "";
        private String dueDateUpdate = "";
        private String dueTimeUpdate = "";
        private java.util.Calendar pickedDate = java.util.Calendar.getInstance();
        private ImageButton speechButton;
        private static final int RECOGNIZER_CODE = 1;

        public static AddNewTask newInstance() {
            return new AddNewTask();
        }


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.add_new_task, container, false);

        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Initialize the Views from xml
            setDueDate = view.findViewById(R.id.set_due_tv);
            setDueTime = view.findViewById(R.id.set_due_time_tv);
            mTaskEdit = view.findViewById(R.id.task_edittext);
            mSaveBtn = view.findViewById(R.id.save_button);
            speechButton = view.findViewById(R.id.mic_icon);

            speechButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now");
                    startActivityForResult(intent, RECOGNIZER_CODE);
                }
            });

            // Find the Add Reminder button
            Button addReminderBtn = view.findViewById(R.id.addReminder);

            // IF no this, addreminder() cannot be found.
            addReminderBtn.setOnClickListener(v -> addReminder());


            firestore = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            final boolean isUpdate;

            final Bundle bundle = getArguments();
            if (bundle != null) {
                isUpdate = true;
                String task = bundle.getString("task");
                id = bundle.getString("id");
                dueDateUpdate = bundle.getString("due");
                dueTimeUpdate = bundle.getString("dueTime");

                mTaskEdit.setText(task);
                setDueDate.setText(dueDateUpdate);   //set dueDate of the current due date to the view
                setDueTime.setText(dueTimeUpdate);


                //check how long for the user typing
                if (task != null && task.length() >= 0) {
                    mSaveBtn.setEnabled(false);
                    mSaveBtn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                }
            } else {
                isUpdate = false;  // Initialize isUpdate to false if there's no bundle
            }


            mTaskEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    if (charSequence.toString().isEmpty()) {
                        mSaveBtn.setEnabled(false);
                        mSaveBtn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    } else {
                        mSaveBtn.setEnabled(true);
                        mSaveBtn.setBackgroundColor(getResources().getColor(R.color.blue));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });


            //this part is for selecting the calendar
            setDueDate.setOnClickListener(view1 -> {
                Calendar calendar = Calendar.getInstance();
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (datePicker, year1, month1, date) -> {
                    month1 += 1;
                    setDueDate.setText(date + "/" + month1 + "/" + year1);
                    dueDate = date + "/" + month1 + "/" + year1;
                }, year, month, day);

                datePickerDialog.show();
            });


            //This part is for CLOCK time
            setDueTime.setOnClickListener(view12 -> {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, selectedHour, selectedMinute) -> {
                    hour = selectedHour;
                    minute = selectedMinute;
                    setDueTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                    dueTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                };

                TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), onTimeSetListener, hour, minute, true);
                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            });


            boolean finalIsUpdate = isUpdate;
            // This part is Save task to Firestore
            mSaveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //get the text from the string
                    String task = mTaskEdit.getText().toString();
                    userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                    // Use old values if the user didn't update them
                    if (dueDate.isEmpty()) {
                        dueDate = dueDateUpdate;  // Keep old due date if not updated
                    }

                    if (dueTime.isEmpty()) {
                        dueTime = dueTimeUpdate;  // Keep old due time if not updated
                    }

                    //according to the id to update the specific userId content
                    if (finalIsUpdate) {
                        firestore.collection("task").document(id).update("userId", userId,
                                "task", task, "due", dueDate, "dueTime", dueTime);
                        Toast.makeText(context, "Task Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        //the update task cannot be empty
                        if (task.isEmpty()) {
                            Toast.makeText(context, "Task cannot be empty! ", Toast.LENGTH_SHORT).show();
                        } else {
                            //save to the hashmap of the object
                            HashMap<String, Object> taskMap = new HashMap<>();

                            taskMap.put("userId", userId);
                            taskMap.put("task", task);
                            taskMap.put("due", dueDate);
                            taskMap.put("dueTime", dueTime);
                            taskMap.put("status", 0);
                            taskMap.put("time", FieldValue.serverTimestamp());

                            //collected the task  and add the hashmap to the firestore
                            firestore.collection("task").add(taskMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    //is successfully display tasked save
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Task Saved", Toast.LENGTH_SHORT).show();
                                    } else {
                                        //unsuccessfully display error message
                                        Toast.makeText(context, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //if unable to save the certain content display the error message
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    dismiss();
                }
            });
        }

        //Speech API to text
        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == RECOGNIZER_CODE && resultCode == RESULT_OK){
                ArrayList<String> taskText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                mTaskEdit.setText(taskText.get(0).toString());
            }

        }


        //Both of these onAttach and onDismiss are important or else, system will crash after clicked on save button
        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            this.context = context;
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            Activity activity = getActivity();
            if (activity instanceof OnDialogCloseListener) {
                ((OnDialogCloseListener) activity).onDialogClose(dialog);
            }
        }


        //NOTIFICATION SECTION----------------------------------------------------------------------------------------------------


        // Method to show reminder dialog
        private void addReminder() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                showNotificationPermissionDialog();
            } else {
                addReminderDialog();
            }
        }

        private void addReminderDialog() {

            ReminderDialogBinding dialogBinding = ReminderDialogBinding.inflate(LayoutInflater.from(requireContext()));
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setView(dialogBinding.getRoot()) // Set the dialog layout
                    .show();

            // Setting up the spinner and buttons inside the dialog
            dialogBinding.remindertype.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.ReminderTypes)));

            // Handle cancel button
            dialogBinding.canceButton.setOnClickListener(v -> dialog.dismiss());

            // Handle date picker dialog
            dialogBinding.pickedDateAndTime.setOnClickListener(v -> showDatePickerDialog(dialogBinding));

            // Handle submit button
            dialogBinding.submitButton.setOnClickListener(v -> {
                if (dialogBinding.etTitle.getText().toString().isEmpty()) {
                    dialogBinding.etTitle.requestFocus();
                    dialogBinding.etTitle.setError("Please Provide Title");
                } else if (dialogBinding.pickedDateAndTime.getText().equals(getResources().getString(R.string.date_and_time))) {
                    dialogBinding.pickedDateAndTime.setError("Please select Date and Time");
                } else {
                    long timeDelayInSeconds = (pickedDate.getTimeInMillis() / 1000L) - (java.util.Calendar.getInstance().getTimeInMillis() / 1000L);
                    if (timeDelayInSeconds < 0) {
                        Toast.makeText(requireContext(), "Can't set reminders for past", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Work Manager
                    createWorkRequest(dialogBinding.etTitle.getText().toString(),
                            getResources().getStringArray(R.array.ReminderTypes)[dialogBinding.remindertype.getSelectedItemPosition()],
                            timeDelayInSeconds);

                    Toast.makeText(requireContext(), "Reminder Added", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
        }


        private void createWorkRequest(String title, String reminderType, long delay) {
            Data inputData = new Data.Builder()
                    .putString("Title", "Todo: " + reminderType)
                    .putString("Message", title)
                    .build();

            OneTimeWorkRequest reminderWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                    .setInitialDelay(delay, TimeUnit.SECONDS)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(requireContext()).enqueue(reminderWorkRequest);
        }


        private void showDatePickerDialog(ReminderDialogBinding dialogBinding) {
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (datePicker, year1, month1, date) -> {
                month1 += 1;
                int finalMonth = month1;
                TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (timePicker, hourOfDay, minute) -> {
                    pickedDate.set(year1, finalMonth - 1, date, hourOfDay, minute);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                    dialogBinding.pickedDateAndTime.setText(dateFormat.format(new Date(pickedDate.getTimeInMillis())));
                }, 12, 0, false);
                timePickerDialog.show();
            }, year, month, day);

            datePickerDialog.show();
        }

        private void showNotificationPermissionDialog() {
            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
                    .setTitle("Notification Permission")
                    .setMessage("Notification permission required to show notifications")
                    .setPositiveButton("OK", (dialog, which) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= 33) {
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                        showNotificationPermissionDialog();
                    } else {
                        showSettingsDialog();
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show();
            }
        });


        private void showSettingsDialog() {
            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
                    .setTitle("Notification Permission")
                    .setMessage("Notification permission required to show notifications")
                    .setPositiveButton("OK", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }