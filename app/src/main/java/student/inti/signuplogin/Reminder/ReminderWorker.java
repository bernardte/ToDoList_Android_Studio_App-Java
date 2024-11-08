package student.inti.signuplogin.Reminder;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get the input data from the popup dialog
        String title = getInputData().getString("Title");
        String message = getInputData().getString("Message");

        if (title != null && message != null) {
            // Create notification using NotificationHelper
            new NotificationHelper(getApplicationContext()).createNotification(title, message);
        }

        // Return the successful result
        return Result.success();
    }
}
