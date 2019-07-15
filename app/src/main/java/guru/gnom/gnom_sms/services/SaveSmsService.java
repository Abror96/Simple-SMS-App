package guru.gnom.gnom_sms.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import guru.gnom.gnom_sms.constants.SmsContract;
import guru.gnom.gnom_sms.utils.OnNewSmsDetailListener;
import guru.gnom.gnom_sms.utils.OnNewSmsListListener;

/**
 * Created by R Ankit on 26-12-2016.
 */

public class SaveSmsService extends IntentService {

    public SaveSmsService() {
        super("SaveService");
    }
    private String TAG = "LOGGERR Save";

    private static OnNewSmsListListener onNewSmsListListener;
    private static OnNewSmsDetailListener onNewSmsDetailListener;

    public static void setOnNewSmsListListener(OnNewSmsListListener onNewSmsListListener) {
        SaveSmsService.onNewSmsListListener = onNewSmsListListener;
    }
    public static void setOnNewSmsDetailListener(OnNewSmsDetailListener onNewSmsDetailListener) {
        SaveSmsService.onNewSmsDetailListener = onNewSmsDetailListener;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String senderNo = intent.getStringExtra("sender_no");
        String message = intent.getStringExtra("message");
        long time = intent.getLongExtra("date",0);

        ContentValues values = new ContentValues();
        values.put("address", senderNo);
        values.put("body", message);
        values.put("date_sent",time);
        getContentResolver().insert(SmsContract.ALL_SMS_URI, values);

        Intent i = new Intent("android.intent.action.MAIN").putExtra("new_sms", true);
        Log.d(TAG, "onHandleIntent: " + message);
        if (onNewSmsListListener != null) {
            onNewSmsListListener.newSms();
        }
        if (onNewSmsDetailListener != null) {
            onNewSmsDetailListener.showLastSms();
        }
        this.sendBroadcast(i);

    }
}
