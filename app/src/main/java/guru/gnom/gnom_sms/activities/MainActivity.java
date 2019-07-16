package guru.gnom.gnom_sms.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import guru.gnom.gnom_sms.R;
import guru.gnom.gnom_sms.SMS;
import guru.gnom.gnom_sms.adapters.AllConversationAdapter;
import guru.gnom.gnom_sms.adapters.ItemCLickListener;
import guru.gnom.gnom_sms.constants.Constants;
import guru.gnom.gnom_sms.services.SaveSmsService;
import guru.gnom.gnom_sms.utils.OnNewSmsListListener;

import static guru.gnom.gnom_sms.utils.Helpers.getContactbyPhoneNumber;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        ItemCLickListener, OnNewSmsListListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private AllConversationAdapter allConversationAdapter;
    private String TAG = "LOGGERR MainActivity";
    private List<SMS> data;
    private LinearLayoutManager linearLayoutManager;
    private BroadcastReceiver mReceiver;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Идёт синхронизация");
        progressDialog.setCancelable(false);

        SaveSmsService.setOnNewSmsListListener(this);
        init();

    }

    private void init() {

        recyclerView = findViewById(R.id.recyclerview);
        fab = findViewById(R.id.fab_new);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        fab.setOnClickListener(this);

        if (checkDefaultSettings())
            checkPermissions();


    }

    private void checkPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE},
                        Constants.MY_PERMISSIONS_REQUEST_READ_SMS);
            }

        }


        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        if (permissionCheck == 0) {
            // update list
            loadSms();
        }

    }

    @SuppressLint("StaticFieldLeak")
    private void loadSms() {
        LoadSms loadSms = new LoadSms();
        loadSms.execute();
    }


    private boolean checkDefaultSettings() {

        boolean isDefault = false;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(getPackageName())) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("This app is not set as your default messaging app. Do you want to set it as default?")
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                checkPermissions();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @TargetApi(19)
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                                startActivity(intent);
                                checkPermissions();
                            }
                        });
                builder.show();

                isDefault = false;
            } else
                isDefault = true;
        }
        return isDefault;
    }


    private void setRecyclerView(List<SMS> data) {
        allConversationAdapter = new AllConversationAdapter(this, data);
        allConversationAdapter.setItemClickListener(this);
        recyclerView.setAdapter(allConversationAdapter);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.fab_new:

                startActivityForResult(new Intent(this, NewSMSActivity.class), 4378);
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_READ_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_CONTACTS)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_CONTACTS)) {
                        } else {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.READ_CONTACTS},
                                    Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                        }

                    } else {
                        // update list
                        loadSms();
                    }

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Can't access messages.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    public void itemClicked(int color, String contact, long id, String read, String name, String thread_id) {

        Intent intent = new Intent(this, SmsListDetailedView.class);
        intent.putExtra(Constants.CONTACT_NAME, contact);
        intent.putExtra(Constants.COLOR, color);
        intent.putExtra(Constants.SMS_ID, id);
        intent.putExtra(Constants.READ, read);
        intent.putExtra(Constants.NAME, name);
        intent.putExtra(Constants.THREAD_ID, thread_id);

        startActivityForResult(intent, 4378);

        Log.d(TAG, "itemClicked: " + id + " " + read);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4378 && data != null) {
            boolean isUpdated = data.getBooleanExtra("isChanged", false);
            if (isUpdated) {
                loadSms();
            }
        }
    }

    class LoadSms extends AsyncTask<String, Void, List<SMS>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected List<SMS> doInBackground(String... args) {
            Cursor inbox = null;
            try {
                Uri uriInbox = Uri.parse("content://sms/");
                inbox = getContentResolver().query(uriInbox, null, "address IS NOT NULL) GROUP BY (thread_id", null, null);



            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return getAllSmsToFile(inbox);
        }

        @Override
        protected void onPostExecute(List<SMS> lstSms) {
            try {
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            sortAndSetToRecycler(lstSms);
        }
    }

    public List<SMS> getAllSmsToFile(Cursor c) {

        List<SMS> lstSms = new ArrayList<SMS>();
        SMS objSMS = null;
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                try {
                    String name;
                    objSMS = new SMS();
                    objSMS.setId(c.getLong(c.getColumnIndexOrThrow("_id")));
                    String num = c.getString(c.getColumnIndexOrThrow("address"));

                    name = getContactbyPhoneNumber(getApplicationContext(), num);

                    objSMS.setName(name);
                    objSMS.setAddress(num);
                    objSMS.setThread_id(c.getString(c.getColumnIndexOrThrow("thread_id")));
                    objSMS.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                    objSMS.setReadState(c.getString(c.getColumnIndex("read")));
                    objSMS.setTime(c.getLong(c.getColumnIndexOrThrow("date")));
                    if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                        objSMS.setFolderName("inbox");
                    } else {
                        objSMS.setFolderName("sent");
                    }

                } catch (Exception e) {

                } finally {

                    lstSms.add(objSMS);
                    c.moveToNext();
                }
            }
        }
        c.close();

        data = lstSms;
        return lstSms;
    }

    private void sortAndSetToRecycler(List<SMS> lstSms) {

        Set<SMS> s = new LinkedHashSet<>(lstSms);
        data = new ArrayList<>(s);
        setRecyclerView(data);

        convertToJson(lstSms);
    }

    private void convertToJson(List<SMS> lstSms) {

        Type listType = new TypeToken<List<SMS>>() {
        }.getType();
        Gson gson = new Gson();
        String json = gson.toJson(lstSms, listType);

        SharedPreferences sp = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.SMS_JSON, json);
        editor.apply();
        //List<String> target2 = gson.fromJson(json, listType);
        //Log.d(TAG, json);

    }

    @Override
    public void newSms() {
        // update list
        Log.d(TAG, "newSms: ");
        loadSms();
    }
}
