package guru.gnom.gnom_sms.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import guru.gnom.gnom_sms.utils.OnSmsSentListener;

/**
 * Created by R Ankit on 30-12-2016.
 */

public class SentReceiver extends BroadcastReceiver {

    private static OnSmsSentListener onSmsSentListener;

    public static void setOnSmsSentListener(OnSmsSentListener onSmsSentListener) {
        SentReceiver.onSmsSentListener = onSmsSentListener;
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Toast.makeText(context, "SMS Sent", Toast.LENGTH_SHORT).show();
                onSmsSentListener.onSendListener("ok");
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Toast.makeText(context, "Generic failure",
                        Toast.LENGTH_SHORT).show();
                onSmsSentListener.onSendListener("fail");
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                Toast.makeText(context, "No service",
                        Toast.LENGTH_SHORT).show();
                onSmsSentListener.onSendListener("fail");
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT)
                        .show();
                onSmsSentListener.onSendListener("fail");
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Toast.makeText(context, "No network",
                        Toast.LENGTH_SHORT).show();
                onSmsSentListener.onSendListener("fail");
                break;
        }

    }
}
