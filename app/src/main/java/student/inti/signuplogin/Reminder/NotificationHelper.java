package student.inti.signuplogin.Reminder;


import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import student.inti.signuplogin.AddNewTask;
import student.inti.signuplogin.R;

import java.util.Date;

public class NotificationHelper {

    private static final String CHANNEL_ID = "REMINDER_NOTIFICATIONS";
    private final Context context;
    private final int NOTIFICATION_ID;

    public NotificationHelper(Context context) {
        this.context = context;
        this.NOTIFICATION_ID = (int) (new Date().getTime() % 100000);
    }


    //This method is to for the UI of the notification, display what such as logo, title, message...
    @SuppressLint("MissingPermission")
    public void createNotification(String title, String message) {
        createNotificationChannel();

        Intent intent = new Intent(context, AddNewTask.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_icon);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_action_name)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Reminder channel to display all user scheduled reminders");

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
