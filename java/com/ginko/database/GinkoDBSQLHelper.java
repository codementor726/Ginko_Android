package com.ginko.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by YongJong on 10/25/16.
 */
public class GinkoDBSQLHelper extends SQLiteOpenHelper {
    //database openhelper instance
    private static GinkoDBSQLHelper mInstance = null;

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 6;

    // Database Name
    private static String DATABASE_NAME = "GinkoDB";

    //context
    private static Context mContext;

    public GinkoDBSQLHelper(Context context, String DatabaseName) {
        super(context, DatabaseName, null, DATABASE_VERSION);
        this.DATABASE_NAME = DatabaseName;
        // TODO Auto-generated constructor stub
        this.mContext = context;
        GinkoDBSQLHelper.mInstance = this;
    }

    public static GinkoDBSQLHelper getInstance(Context ctx , String databaseName) {
        /**
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information:
         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
         */

        if (mInstance == null) {
            mContext = ctx;
            DATABASE_NAME = databaseName;
            GinkoDBSQLHelper.mInstance = new GinkoDBSQLHelper(ctx.getApplicationContext() , GinkoDBSQLHelper.DATABASE_NAME);
        }
        return mInstance;
    }

    public static void clearInstance()
    {
        mContext = null;
        DATABASE_NAME = null;
        mInstance = null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS GinkoTable"+
                    " (Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "  ContactOrEntityId INTEGER NOT NULL DEFAULT -1,"+
                    "  ContactName TEXT Not NULL," +
                    "  ProfileImage TEXT Not Null," +
                    "  Latitude DOUBLE Not NULL DEFAULT 0.00," +
                    "  Longitude DOUBLE Not NULL DEFAULT 0.00);");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS GinkoTable");
        onCreate(db);
    }
}
