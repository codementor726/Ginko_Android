package com.ginko.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YongJong on 10/25/16.
 */
public class GinkoTableModel extends GinkoDBSQLHelper {
    public String TABLE_NAME = "GinkoTable";

    private static String DBName = "";

    private static Context mContext;

    private static GinkoDBSQLHelper sqlHelper = null;

    private static SQLiteDatabase db = null;

    private static GinkoTableModel mInstance = null;

    public GinkoTableModel(Context context, String DatabaseName) {
        super(context, DatabaseName);
        this.mContext = context;
        this.DBName = DatabaseName;
        this.sqlHelper =  GinkoDBSQLHelper.getInstance(context , this.DBName);
        GinkoTableModel.mInstance = this;
    }

    public static GinkoTableModel getInstance(Context ctx , String databaseName) {

        if (mInstance == null) {
            mContext = ctx;
            DBName = databaseName;
            GinkoTableModel.mInstance = new GinkoTableModel(ctx.getApplicationContext() , databaseName);
        }
        return mInstance;
    }

    public static void clearInstance()
    {
        mInstance = null;
        sqlHelper = null;
        GinkoDBSQLHelper.clearInstance();
    }
    public synchronized void getDB()
    {
        if(sqlHelper == null)
            sqlHelper = GinkoDBSQLHelper.getInstance(mContext , this.DBName);

        if(db == null)
        {
            db = sqlHelper.getWritableDatabase();
        }
    }

    public synchronized int add(GinkoMeStruct struct)
    {
        getDB();

        boolean bExists = false;
        Cursor cursor = db.query(TABLE_NAME, new String[] {
                        "Id",
                        "ContactOrEntityId"}, "ContactOrEntityId" + "=?",
                new String[] {String.valueOf(struct.getContactOrEntityID()) }, null, null, null, null);
        if (cursor != null && cursor.getCount()>0)
        {
            bExists = true;
        }
        if (cursor!= null)
            cursor.close();

        ContentValues values = new ContentValues();
        //values.put("Id" , Long.valueOf(struct.RoomID));
        values.put("ContactOrEntityId" , Integer.valueOf(struct.getContactOrEntityID()));
        values.put("ContactName" , struct.getEntityOrContactName());
        values.put("ProfileImage", struct.getProfileImage());
        values.put("Latitude", struct.getLat());
        values.put("Longitude", struct.getLng());

        long last_insert_id = 0;
        if(bExists == false)
        {
            last_insert_id = db.insert(TABLE_NAME, null, values);
        }
        else//else update row
        {
            last_insert_id = db.insert(TABLE_NAME, null, values);
            //last_insert_id = db.update(TABLE_NAME, values, "ContactOrEntityId = ?",
              //      new String[] { String.valueOf(struct.getContactOrEntityID())});
        }

        return (int)last_insert_id;
    }

    public synchronized void addAll(ArrayList<GinkoMeStruct> ginkoStructs)
    {
        if(ginkoStructs == null || ginkoStructs.size() == 0) return;
        getDB();

        String sql = "INSERT INTO " + TABLE_NAME + " (ContactOrEntityId, ContactName, ProfileImage, Latitude, Longitude) " +
            "VALUES (?, ?, ?, ?, ?)";

        db.beginTransaction();
        SQLiteStatement stmt = db.compileStatement(sql);

        for(GinkoMeStruct struct : ginkoStructs) {
            ContentValues values = new ContentValues();
            //values.put("Id" , Long.valueOf(struct.RoomID));
            values.put("ContactOrEntityId" , Integer.valueOf(struct.getContactOrEntityID()));
            values.put("ContactName" , struct.getEntityOrContactName());
            values.put("ProfileImage", struct.getProfileImage());
            values.put("Latitude", struct.getLat());
            values.put("Longitude", struct.getLng());
            stmt.bindString(1, String.valueOf(struct.getContactOrEntityID()));
            stmt.bindString(2, struct.getEntityOrContactName());
            stmt.bindString(3, struct.getProfileImage());
            stmt.bindString(4, String.valueOf(struct.getLat()));
            stmt.bindString(5, String.valueOf(struct.getLng()));
            stmt.execute();
            stmt.clearBindings();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public synchronized int update(GinkoMeStruct struct)
    {
        getDB();

        ContentValues values = new ContentValues();
        values.put("Id", Integer.valueOf(struct.getId()));
        values.put("ContactOrEntityId" , Integer.valueOf(struct.getContactOrEntityID()));
        values.put("ContactName" , struct.getEntityOrContactName());
        values.put("ProfileImage", struct.getProfileImage());
        values.put("Latitude", struct.getLat());
        values.put("Longitude", struct.getLng());

        int last_insert_id = db.update(TABLE_NAME, values, "Id = ?",
                new String[] { String.valueOf(struct.getId()) });
        return last_insert_id;
    }

    public synchronized int updateDataWithEntityId(GinkoMeStruct struct)
    {
        getDB();

        ContentValues values = new ContentValues();
        values.put("Id", Integer.valueOf(struct.getId()));
        values.put("ContactOrEntityId" , Integer.valueOf(struct.getContactOrEntityID()));
        values.put("ContactName" , struct.getEntityOrContactName());
        values.put("ProfileImage", struct.getProfileImage());
        values.put("Latitude", struct.getLat());
        values.put("Longitude", struct.getLng());

        int last_insert_id = db.update(TABLE_NAME, values, "ContactOrEntityId = ?",
                new String[] { String.valueOf(struct.getContactOrEntityID()) });
        return last_insert_id;
    }

    public synchronized GinkoMeStruct get(int id)
    {
        getDB();

        Cursor cursor = db.query(TABLE_NAME, new String[] {
                        "Id",
                        "ContactOrEntityId",
                        "ContactName",
                        "ProfileImage",
                        "Latitude",
                        "Longitude",
                }, "Id" + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if(cursor == null)
            return null;


        if(cursor.getCount()<=0)
        {
            cursor.close();
            return null;
        }

        GinkoMeStruct struct = null;
        if(cursor.moveToFirst())
        {
            struct = new GinkoMeStruct();
            struct.setId(cursor.getInt(0));
            struct.setContactOrEntityID(cursor.getInt(1));
            struct.setEntityOrContactName(cursor.getString(2));
            struct.setProfileImage(cursor.getString(3));
            struct.setLat(cursor.getDouble(4));
            struct.setLng(cursor.getDouble(5));
        }
        cursor.close();
        return struct;
    }

    public synchronized List<GinkoMeStruct> getContactById(int contactId)
    {
        getDB();

        List<GinkoMeStruct> ginkoList = new ArrayList<GinkoMeStruct>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME+" WHERE ContactOrEntityId="+String.valueOf(contactId);

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if(cursor == null)
        {
            return ginkoList;
        }
        if (cursor.moveToFirst()) {
            do {
                GinkoMeStruct struct = new GinkoMeStruct();
                struct.setId(cursor.getInt(0));
                struct.setContactOrEntityID(cursor.getInt(1));
                struct.setEntityOrContactName(cursor.getString(2));
                struct.setProfileImage(cursor.getString(3));
                struct.setLat(cursor.getDouble(4));
                struct.setLng(cursor.getDouble(5));
                ginkoList.add(struct);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return ginkoList;
    }

    public synchronized  boolean isTableExists() {
        boolean tableExists = false;

        getDB();
        Cursor cursor = null;
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        try
        {
            cursor = db.rawQuery(selectQuery, null);
            if(cursor == null)
                return tableExists;
            if (cursor.moveToFirst())
                tableExists = true;
        }
        catch (Exception e) {

        }

        cursor.close();
        return tableExists;
    }

    public synchronized  void dropTable() {
        getDB();

        try
        {
            db.delete("GinkoTable", null, null);
        }
        catch (Exception e) {

        }
    }

    public synchronized List<GinkoMeStruct> getAll()
    {
        getDB();
        List<GinkoMeStruct> ginkoList = new ArrayList<GinkoMeStruct>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if(cursor == null)
        {
            return ginkoList;
        }
        if (cursor.moveToFirst()) {
            do {
                GinkoMeStruct struct = new GinkoMeStruct();
                struct.setId(cursor.getInt(0));
                struct.setContactOrEntityID(cursor.getInt(1));
                struct.setEntityOrContactName(cursor.getString(2));
                struct.setProfileImage(cursor.getString(3));
                struct.setLat(cursor.getDouble(4));
                struct.setLng(cursor.getDouble(5));
                ginkoList.add(struct);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return ginkoList;
    }

    public synchronized List<GinkoMeStruct> getAllSproutItems()
    {
        getDB();
        List<GinkoMeStruct> contactList = new ArrayList<GinkoMeStruct>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if(cursor == null)
        {
            return contactList;
        }
        if (cursor.moveToFirst()) {
            do {
                GinkoMeStruct struct = new GinkoMeStruct();
                struct = new GinkoMeStruct();
                struct.setId(cursor.getInt(0));
                struct.setContactOrEntityID(cursor.getInt(1));
                struct.setEntityOrContactName(cursor.getString(2));
                struct.setProfileImage(cursor.getString(3));
                struct.setLat(cursor.getDouble(4));
                struct.setLng(cursor.getDouble(5));

                contactList.add(struct);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return contactList;
    }

    public synchronized List<GinkoMeStruct> getVisibleSproutItem(double west, double east, double north, double south)
    {
        getDB();
        List<GinkoMeStruct> contactList = new ArrayList<GinkoMeStruct>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE Longitude > " + west +
                             " AND Longitude < " + east + " AND Latitude > " + south +
                             " AND Latitude < " + north;

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if(cursor == null)
        {
            return contactList;
        }
        if (cursor.moveToFirst()) {
            do {
                GinkoMeStruct struct = new GinkoMeStruct();
                struct = new GinkoMeStruct();
                struct.setId(cursor.getInt(0));
                struct.setContactOrEntityID(cursor.getInt(1));
                struct.setEntityOrContactName(cursor.getString(2));
                struct.setProfileImage(cursor.getString(3));
                struct.setLat(cursor.getDouble(4));
                struct.setLng(cursor.getDouble(5));

                contactList.add(struct);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return contactList;
    }

    public synchronized void deleteContactWithDbId(int id) {

        getDB();

        db.delete(TABLE_NAME, "Id = ?",
                new String[]{String.valueOf(id)});
    }

    public synchronized void deleteContactWithContactId(int contactId) {

        getDB();

        db.delete(TABLE_NAME, "ContactOrEntityId = ?",
                new String[]{String.valueOf(contactId)});
    }

    public synchronized void deleteContact(GinkoMeStruct struct) {

        getDB();
        GinkoMeStruct Struct = (GinkoMeStruct)struct;
        db.delete(TABLE_NAME, "Id = ?",
                new String[]{String.valueOf(Struct.getId())});
    }

    public synchronized void deleteAll() {
        // TODO Auto-generated method stub
        getDB();
        db.delete(TABLE_NAME, null, null);
    }

    public void closeDB()
    {
        try
        {
            getDB();
            db.close();
        }catch(Exception e){e.printStackTrace();}
        finally
        {
            db = null;
        }
    }
}
