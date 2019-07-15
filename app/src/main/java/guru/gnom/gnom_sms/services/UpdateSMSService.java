package guru.gnom.gnom_sms.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Created by R Ankit on 29-12-2016.
 */

public class UpdateSMSService extends IntentService {

    public UpdateSMSService() {
        super("UpdateSMSReceiver");
    }
    private String TAG = "LOGGERR Update";

    @Override
    protected void onHandleIntent(Intent intent) {
        markSmsRead(intent.getLongExtra("id", -123));
    }

    public void markSmsRead(long messageId) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("read", true);
            Log.d(TAG, "markSmsRead: " + messageId);
            getContentResolver().update(Uri.parse("content://sms/"), cv, "_id="+messageId, null);
        } catch (Exception e) {
            Log.d(TAG, "markSmsRead: error " + e.getMessage());
        }


    }

}
