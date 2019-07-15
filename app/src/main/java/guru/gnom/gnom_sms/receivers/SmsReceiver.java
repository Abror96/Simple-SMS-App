package guru.gnom.gnom_sms.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import guru.gnom.gnom_sms.R;
import guru.gnom.gnom_sms.activities.SmsListDetailedView;
import guru.gnom.gnom_sms.constants.Constants;
import guru.gnom.gnom_sms.services.SaveSmsService;
import guru.gnom.gnom_sms.utils.Helpers;

/**
 * Created by R Ankit on 24-12-2016.
 */

public class SmsReceiver extends BroadcastReceiver {


    private String TAG = SmsReceiver.class.getSimpleName();
    private Bundle bundle;
    private SmsMessage currentSMS;
    private int mNotificationId = 101;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            Log.e(TAG, "smsReceiver");

            bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                if (pdu_Objects != null) {

                    for (Object aObject : pdu_Objects) {

                        currentSMS = getIncomingMessage(aObject, bundle);

                        String senderNo = currentSMS.getDisplayOriginatingAddress();
                        String message = currentSMS.getDisplayMessageBody();
                        //Log.d(TAG, "senderNum: " + senderNo + " :\n message: " + message);

                        issueNotification(context, senderNo, message);
                        saveSmsInInbox(context,currentSMS);


                    }
                    this.abortBroadcast();
                    // End of loop
                }
            }
        } // bundle null
    }

    private void saveSmsInInbox(Context context, SmsMessage sms) {

        Intent serviceIntent = new Intent(context, SaveSmsService.class);
        serviceIntent.putExtra("sender_no", sms.getDisplayOriginatingAddress());
        serviceIntent.putExtra("message", sms.getDisplayMessageBody());
        serviceIntent.putExtra("date", sms.getTimestampMillis());
        context.startService(serviceIntent);

    }

    private void issueNotification(Context context, String senderNo, String message) {

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);

        String NOTIFICATION_CHANNEL_ID = "gnom_sms_notification_channel_id";

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setLargeIcon(icon)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(Helpers.getContactbyPhoneNumber(context, senderNo))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setAutoCancel(true)
                        .setContentText(message);

        Intent resultIntent = new Intent(context, SmsListDetailedView.class);
        resultIntent.putExtra(Constants.CONTACT_NAME,senderNo);
        resultIntent.putExtra(Constants.FROM_SMS_RECIEVER,true);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);

            //Configure Notification Channel
            channel.setDescription("Game Notifications");
            channel.enableLights(true);
            channel.enableVibration(true);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(mNotificationId, mBuilder.build());

    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject);
        }
        return currentSMS;
    }
}
