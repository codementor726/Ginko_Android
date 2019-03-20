package com.ginko.activity.sync;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ginko.api.request.SyncRequest;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ProgressHUD;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class SyncHomeActivity extends MyBaseActivity implements View.OnClickListener{

    private ImageButton btnPrev;
    private ImageView btnImportHistory;

    private LoadContactThread loadContactThread;
    private boolean isThreadStopped = true;
    private ProgressHUD progressHud;
    private ArrayList<SyncGreyContactItem> contactlist = new ArrayList<SyncGreyContactItem>();

    private Handler mHandler = new Handler();

    private RelativeLayout activityRootView;

    private long old_ID = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_home);

        activityRootView = (RelativeLayout)findViewById(R.id.rootLayout);

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnImportHistory = (ImageView)findViewById(R.id.btnImportHistory); btnImportHistory.setOnClickListener(this);

        progressHud = ProgressHUD.createProgressDialog(SyncHomeActivity.this, "", false, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(loadContactThread != null && loadContactThread.isAlive())
                {
                    isThreadStopped = true;
                    try
                    {
                        loadContactThread.stop();
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally {
                        loadContactThread = null;
                        isThreadStopped = true;
                        if(progressHud != null && progressHud.isShowing())
                            progressHud.dismiss();
                    }
                }
            }
        });


    }


    public void importGmail(View view) {
        this.openSyncContactsViewByOAuth("google");
    }

    public void importOutlook(View view) {
        Uitils.toActivity(SyncOutlookActivity.class, false);
    }

    public void importLive(View view) {
        this.openSyncContactsViewByOAuth("live");
    }

    public void importYahoo(View view) {
        this.openSyncContactsViewByOAuth("yahoo");
    }

    public void importAddressBook(View view) {
        if(loadContactThread != null)
        {
            return;
        }
        else
        {
            isThreadStopped = false;
            if(progressHud != null)
                progressHud.show();
            loadContactThread = new LoadContactThread(SyncHomeActivity.this);
            loadContactThread.start();
        }

    }


    private void openSyncContactsViewByOAuth(String provider) {
        Intent intent = new Intent();
        intent.setClass(SyncHomeActivity.this, SyncOAuthActivity.class);
        intent.putExtra("provider", provider);
        startActivity(intent);
    }

    private class LoadContactThread extends Thread{
        private Context mContext;
        public LoadContactThread(Context context)
        {
            this.mContext = context;
        }

        @Override
        public void run()
        {
            if(contactlist == null)
                contactlist = new ArrayList<SyncGreyContactItem>();
            else
                contactlist.clear();

            /*
            *
            *
            * Changed by lee for speed up
            *
            * *

            Uri uri = ContactsContract.Contacts.CONTENT_URI;

            final String[] projection = new String[] {
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID, // contact ID -> use to get photo info
                    //ContactsContract.CommonDataKinds.Phone.NUMBER,        // contact phone number
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,  // contact name
            };
            String[] selectionArgs = null;

            String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    + " COLLATE LOCALIZED ASC";

            //Cursor contactCursor = getContentResolver().query(uri, projection, null,selectionArgs, sortOrder);
            ContentResolver cr = mContext.getContentResolver();
            Cursor contactCursor = cr.query(uri, null, null, null, sortOrder);


            if (contactCursor.moveToFirst()) {
                while (!contactCursor.isAfterLast()) {
                    if(isThreadStopped)
                        return;
                    int ididx = contactCursor.getColumnIndex(ContactsContract.Contacts._ID);

                    String id = contactCursor.getString(ididx); // id
                    // get contact name
                    String contactName = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    // get phone number
                    String phonenumber = "";
                    Cursor numCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,	ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
                    numCur.moveToFirst();
                    while (!numCur.isAfterLast()) {
                        phonenumber= numCur.getString(numCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        numCur.moveToNext();
                    }
                    numCur.close();

                    // get email
                    String email = "";
                    Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[] { ContactsContract.CommonDataKinds.Email.DATA },		ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { id }, null);
                    emailCur.moveToFirst();
                    while (!emailCur.isAfterLast()) {
                        email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        emailCur.moveToNext();
                    }
                    emailCur.close();

                    // get email
                    String address = "";
                    //Get Postal Address....

                    String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                    String[] addrWhereParams = new String[]{id,
                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
                    Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI, null, addrWhere, addrWhereParams, null);
                    while(addrCur.moveToNext()) {
                        address = "";
                        String poBox = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
                        String street = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                        String city = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                        String state = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                        String postalCode = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
                        String country = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                        String type = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
                        //System.out.println("---- Address = "+street+" , "+city+" , "+state+" , "+country+" ---------");
                        if(street != null)
                            address += street+" ";
                        if(city != null) {
                            address += city; address = address.trim() + " ";
                        }
                    *//*if(state != null) {.
                        address += state; address = address.trim() + " ";
                    }
                    if(country != null) {
                        address += country; address = address.trim() + " ";
                    }*//*
                        //System.out.println("---- Address = " + address +" -----");
                        if(!address.equals(""))
                            break;
                        // Do something with these....

                    }
                    addrCur.close();


                    if((phonenumber!=null && !phonenumber.equals("")) ||
                            (email!=null && !email.equals("")) ||
                            (address!=null && !address.equals("")) )
                    {
                        SyncGreyContactItem acontact = new SyncGreyContactItem();
                        acontact.setGreyContactName(contactName);
                        if(phonenumber!=null && !phonenumber.equals(""))
                            acontact.addPhoneNumber(phonenumber);
                        if(email!=null && !email.equals(""))
                            acontact.setGreyContactEmail(email);
                        if(address!=null && !address.equals(""))
                            acontact.setAddress(address);

                        contactlist.add(acontact);
                    }

                    contactCursor.moveToNext();
                }

            *//*do {
                //String phonenumber = contactCursor.getString(1);//.replaceAll("-","");
                if (phonenumber.length() == 10) {
                    phonenumber = phonenumber.substring(0, 3) + "-"
                            + phonenumber.substring(3, 6) + "-"
                            + phonenumber.substring(6);
                } else if (phonenumber.length() > 8) {
                    phonenumber = phonenumber.substring(0, 3) + "-"
                            + phonenumber.substring(3, 7) + "-"
                            + phonenumber.substring(7);
                }

                long contactId = contactCursor.getLong(ididx);
                //String contactName = contactCursor.getString(1);
                String contactName = "";
                String email = "";
                String phonenumber = "";
                Cursor emailCursor = null;
                try
                {
                    // check whether email is registered or not using current display name
                    emailCursor = getContentResolver()
                            .query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                    new String[] { ContactsContract.CommonDataKinds.Email.DATA },
                                    "DISPLAY_NAME" + "='" + contactName + "'", null, null);

                    while (emailCursor.moveToNext())
                    {
                        email = emailCursor.getString(emailCursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    }
                }
                catch (Exception e)
                {
                    //Log.e("[GetPhonenumberAdapter] getContactData",
                    //  e.toString());
                }
                finally
                {
                    if (emailCursor != null)
                    {
                        emailCursor.close();
                        emailCursor = null;
                    }
                }

                SyncGreyContactItem acontact = new SyncGreyContactItem();
                acontact.setGreyContactName(contactName);
                acontact.addPhoneNumber(phonenumber);
                acontact.setGreyContactEmail(email);

                contactlist.add(acontact);
            } while (contactCursor.moveToNext());*//*
            }
            *
            *
            * Changed by lee for speed up
            *
            * */
            ////////////////     Changed   ////////////////////////////
            ContentResolver resolver = getContentResolver();
            Cursor c = resolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    "(" + ContactsContract.Data.HAS_PHONE_NUMBER + "==0 OR " + ContactsContract.Data.HAS_PHONE_NUMBER + "!=0) And (" + ContactsContract.Data.MIMETYPE + "=? OR " + ContactsContract.Data.MIMETYPE + "=?)",
                    new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                    ContactsContract.Data.CONTACT_ID);
            if(c.isBeforeFirst()){
                while (c.moveToNext()) {
                    //Get first field value.
                    long id = c.getLong(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                    if(id == old_ID)
                        continue;
                    String contactName = c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    String data1 = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1));

                    String email = "";
                    String phonenumber = "";

                    if(c.moveToNext()) {
                        //Get second field value.
                        long id_after = c.getLong(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                        String contactName_after = c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                        String data1_after = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1));

                        if (id == id_after) {
                            old_ID = id_after;
                            if (data1.matches("\\d+(?:\\.\\d+)?"))
                                phonenumber = data1;
                            else if (data1.contains(".com"))
                                email = data1;
                            if (data1_after.matches("\\d+(?:\\.\\d+)?"))
                                phonenumber = data1_after;
                            else if (data1_after.contains(".com"))
                                email = data1_after;

                            contactName = contactName.trim();
                            email = email.trim();
                            phonenumber = phonenumber.trim();

                            if (!(contactName.equals("") || (email.equals("") && phonenumber.equals("")))) {
                                SyncGreyContactItem acontact = new SyncGreyContactItem();
                                acontact.setGreyContactName(contactName);
                                acontact.addPhoneNumber(phonenumber);
                                acontact.setGreyContactEmail(email);

                                contactlist.add(acontact);
                            }
                        } else {
                            if (data1.matches("\\d+(?:\\.\\d+)?"))
                                phonenumber = data1;
                            else if (data1.contains(".com"))
                                email = data1;

                            contactName = contactName.trim();
                            email = email.trim();
                            phonenumber = phonenumber.trim();

                            if (!(contactName.equals("") || (email.equals("") && phonenumber.equals("")))) {
                                SyncGreyContactItem acontact = new SyncGreyContactItem();
                                acontact.setGreyContactName(contactName);
                                acontact.addPhoneNumber(phonenumber);
                                acontact.setGreyContactEmail(email);

                                contactlist.add(acontact);
                            }

                            phonenumber = "";
                            email = "";

                            if (data1_after.matches("\\d+(?:\\.\\d+)?"))
                                phonenumber = data1_after;
                            else if (data1_after.contains(".com"))
                                email = data1_after;

                            contactName = contactName_after.trim();
                            email = email.trim();
                            phonenumber = phonenumber.trim();

                            if (!(contactName.equals("") || (email.equals("") && phonenumber.equals("")))) {
                                SyncGreyContactItem acontact = new SyncGreyContactItem();
                                acontact.setGreyContactName(contactName);
                                acontact.addPhoneNumber(phonenumber);
                                acontact.setGreyContactEmail(email);

                                contactlist.add(acontact);
                            }
                        }
                    }
                    else {
                        if (data1.matches("\\d+(?:\\.\\d+)?"))
                            phonenumber = data1;
                        else if (data1.contains(".com"))
                            email = data1;

                        contactName = contactName.trim();
                        email = email.trim();
                        phonenumber = phonenumber.trim();

                        if (!(contactName.equals("") || (email.equals("") && phonenumber.equals("")))) {
                            SyncGreyContactItem acontact = new SyncGreyContactItem();
                            acontact.setGreyContactName(contactName);
                            acontact.addPhoneNumber(phonenumber);
                            acontact.setGreyContactEmail(email);

                            contactlist.add(acontact);
                        }
                    }
                }
            }
            //////////////////////////////////////////////////////////////////////////////
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadContactThread = null;
                    isThreadStopped = true;
                    if(progressHud != null && progressHud.isShowing())
                        progressHud.dismiss();

                    ArrayList<SyncGreyContactItem> phoneContacts = contactlist;

                    if(phoneContacts.size()>0)
                    {
                        JSONObject data = new JSONObject();
                        try
                        {
                            JSONArray array = new JSONArray();
                            for(int i=0;i<phoneContacts.size();i++)
                            {
                                SyncGreyContactItem contactItem = phoneContacts.get(i);
                                JSONObject contactObj = new JSONObject();
                                String contactName =contactItem.getGreyContactName().trim();
                                String firstName , middleName , lastName;
                                if(contactName.contains(" "))
                                {
                                    String[] tokens = contactName.split(" ");
                                    if(tokens.length >= 3) {
                                        firstName = tokens[0];
                                        middleName = tokens[1];
                                        lastName = "";
                                        for(int j=2;j<tokens.length;j++)
                                            lastName+=tokens[j];
                                    }
                                    else if(tokens.length == 2)
                                    {
                                        firstName = tokens[0]; middleName = ""; lastName = tokens[1];
                                    }
                                    else if(tokens.length == 1)
                                    {
                                        firstName = tokens[0]; middleName = ""; lastName = "";
                                    }
                                    else
                                    {
                                        firstName = ""; middleName = ""; lastName = "";
                                    }
                                }
                                else
                                {
                                    firstName = contactName;
                                    middleName = "";
                                    lastName = "";
                                }
                                contactObj.put("first_name" , firstName);
                                contactObj.put("middle_name" , middleName);
                                contactObj.put("last_name" , lastName);
                                if(contactItem.getGreyContactEmail()==null)
                                    contactObj.put("email" , "");
                                else
                                    contactObj.put("email" , contactItem.getGreyContactEmail());
                                contactObj.put("photo_name" , "");
                                contactObj.put("notes" , "");

                                //add default fields
                                JSONArray fields = new JSONArray();
                                //add Phone
                                JSONObject phoneField = new JSONObject();
                                phoneField.put("field_name" , "Phone");
                                if(contactItem.getContactPhoneNumbers()!=null && contactItem.getContactPhoneNumbers().size()>0)
                                    phoneField.put("field_value" , contactItem.getContactPhoneNumbers().get(0));
                                else
                                    phoneField.put("field_value" , "");
                                phoneField.put("field_type" ,  ConstValues.PROFILE_FIELD_TYPE_PHONE);
                                fields.put(phoneField);

                                //add address
                                JSONObject addressField = new JSONObject();
                                addressField.put("field_name" , "Address");
                                addressField.put("field_value" , contactItem.getAddress()==null?"":contactItem.getAddress());
                                addressField.put("field_type" ,  ConstValues.PROFILE_FIELD_TYPE_ADDRESS);
                                fields.put(addressField);

                                //add birthday
                                JSONObject birthdayField = new JSONObject();
                                birthdayField.put("field_name" , "Birthday");
                                birthdayField.put("field_value" , contactItem.getBirthday()==null?"":contactItem.getBirthday());
                                birthdayField.put("field_type" ,  ConstValues.PROFILE_FIELD_TYPE_DATE);
                                fields.put(birthdayField);

                                //add website
                                JSONObject websiteField = new JSONObject();
                                websiteField.put("field_name" , "WebSite");
                                websiteField.put("field_value" , contactItem.getWebsite()==null?"":contactItem.getWebsite());
                                websiteField.put("field_type" ,  ConstValues.PROFILE_FIELD_TYPE_WEBSITE);
                                fields.put(websiteField);

                                contactObj.put("fields" , fields);
                                array.put(contactObj);
                            }
                            data.put("data" , array);

                            SyncRequest.saveSyncContacts(data , new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if(response.isSuccess())
                                    {
                                        Uitils.toActivity(SyncImportAddressBookActivity.class, false);
                                    }
                                }
                            } , true);
                        }catch(JSONException e)
                        {
                            e.printStackTrace();
                        }

                    }
                    else {
                        Uitils.toActivity(SyncImportAddressBookActivity.class, false);
                    }
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;

            case R.id.btnImportHistory:
                Intent intent = new Intent(SyncHomeActivity.this , SyncImportAddressBookActivity.class);
                startActivity(intent);
                break;
        }
    }
}
