package guru.gnom.gnom_sms.activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import guru.gnom.gnom_sms.R;
import guru.gnom.gnom_sms.adapters.SingleGroupAdapter;
import guru.gnom.gnom_sms.constants.Constants;
import guru.gnom.gnom_sms.constants.SmsContract;
import guru.gnom.gnom_sms.receivers.DeliverReceiver;
import guru.gnom.gnom_sms.receivers.SentReceiver;
import guru.gnom.gnom_sms.services.SaveSmsService;
import guru.gnom.gnom_sms.services.UpdateSMSService;
import guru.gnom.gnom_sms.utils.Helpers;
import guru.gnom.gnom_sms.utils.OnNewSmsDetailListener;
import guru.gnom.gnom_sms.utils.OnNewSmsListListener;
import guru.gnom.gnom_sms.utils.OnSmsSentListener;

public class SmsListDetailedView extends AppCompatActivity implements View.OnClickListener, OnSmsSentListener, OnNewSmsDetailListener {

    private String contact;
    private SingleGroupAdapter singleGroupAdapter;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageView btSend;
    private String name;
    private String message;
    private long _Id;
    private int color;
    private String read = "1";
    private String thread_id = "";
    private boolean isUpdated = false;
    private String TAG = "LOGGERR SmsList";
    private boolean from_sms_receiver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_detailed_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SentReceiver.setOnSmsSentListener(this);
        SaveSmsService.setOnNewSmsDetailListener(this);

        init();
    }

    private void init() {

        Intent intent = getIntent();


        contact = intent.getStringExtra(Constants.CONTACT_NAME);
        _Id = intent.getLongExtra(Constants.SMS_ID,-123);
        color = intent.getIntExtra(Constants.COLOR,0);
        read = intent.getStringExtra(Constants.READ);
        name = intent.getStringExtra(Constants.NAME);
        thread_id = intent.getStringExtra(Constants.THREAD_ID);
        from_sms_receiver = intent.getBooleanExtra(Constants.FROM_SMS_RECIEVER, false);

        Log.d(TAG, "init: " + _Id + " " + read + " " + thread_id);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(name != null ? name : Helpers.getContactbyPhoneNumber(this, contact));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        etMessage = (EditText) findViewById(R.id.etMessage);
        btSend = (ImageView) findViewById(R.id.btSend);

        btSend.setOnClickListener(this);

        setRecyclerView(null);

        setReadSMS();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        setReadSMS();
        switch (item.getItemId()) {
            case android.R.id.home:
                if (from_sms_receiver) {
                    Intent intent = new Intent(SmsListDetailedView.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("isChanged", isUpdated);
                    setResult(6543, intent);
                    finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setRecyclerView(Cursor cursor) {
        singleGroupAdapter = new SingleGroupAdapter(this, cursor,color);
        recyclerView.setAdapter(singleGroupAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadSms().execute();
    }

    @Override
    public void onSendListener(String state) {
        isUpdated = true;
        new LoadSms().execute();
    }

    @Override
    public void showLastSms() {
        isUpdated = true;
        new LoadSms().execute();
    }

    class LoadSms extends AsyncTask<String, Void, Cursor> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Cursor doInBackground(String... args) {

            Cursor inbox = null;
            try {
                Uri uriInbox = Uri.parse("content://sms/");
                inbox = getContentResolver().query(uriInbox, null, SmsContract.SMS_SELECTION, new String[]{contact}, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return inbox;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {
                singleGroupAdapter.swapCursor(cursor);
                cursor.moveToFirst();
                Log.d(TAG, "onPostExecute: " + cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
                _Id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            } else {
                //no sms
            }
        }
    }



    private void setReadSMS() {
        Intent intent = new Intent(this, UpdateSMSService.class);
        intent.putExtra("id", _Id);
        startService(intent);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btSend:
                sendSMSMessage();
                break;
        }
    }

    protected void sendSMSMessage() {

        message = etMessage.getText().toString();

        if (message!=null && message.trim().length()>0)
            requestPermisions();
        else
            etMessage.setError(getString(R.string.please_write_message));

    }

    private void requestPermisions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        Constants.MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }else{
            sendSMSNow();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSMSNow();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    private void sendSMSNow() {

        BroadcastReceiver sendBroadcastReceiver = new SentReceiver();
        BroadcastReceiver deliveryBroadcastReciever = new DeliverReceiver();

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        registerReceiver(sendBroadcastReceiver, new IntentFilter(SENT));
        registerReceiver(deliveryBroadcastReciever, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        Log.d(TAG, "sendSMSNow: " + contact + " " + message);
        try {
            sms.sendTextMessage(contact, null, message, sentPI, deliveredPI);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Null PDU", Toast.LENGTH_LONG).show();
        }

        ContentValues values = new ContentValues();
        values.put("address", contact);
        values.put("body", message);
        this.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        isUpdated = true;

        new LoadSms().execute();
        etMessage.setText("");

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        setReadSMS();
        if (from_sms_receiver) {
            Intent intent = new Intent(SmsListDetailedView.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent();
            intent.putExtra("isChanged", isUpdated);
            setResult(6543, intent);
            finish();
        }
    }
}
