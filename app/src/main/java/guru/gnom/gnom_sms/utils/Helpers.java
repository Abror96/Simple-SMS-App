package guru.gnom.gnom_sms.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by R Ankit on 27-12-2016.
 */

public class Helpers {

    public static String getDate(long milliSeconds)
    {
        String dateFormat = "d MMM";

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String getContactbyPhoneNumber(Context c, String phoneNumber) {

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cursor = c.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return phoneNumber;
        }else {
            String name = phoneNumber;
            try {

                if (cursor.moveToFirst()) {
                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }

            } finally {
                cursor.close();
            }

            return name;
        }
    }

}
