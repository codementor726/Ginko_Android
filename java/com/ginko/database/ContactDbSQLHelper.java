package com.ginko.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDbSQLHelper extends SQLiteOpenHelper {
	//database openhelper instance
	private static ContactDbSQLHelper mInstance = null;
	
	// All Static variables
    // Database Version
	private static final int DATABASE_VERSION = 6;
 
    // Database Name
    private static String DATABASE_NAME = "ContactsDB";
 
    //context
    private static Context mContext;

    public ContactDbSQLHelper(Context context, String DatabaseName) {
		super(context, DatabaseName, null, DATABASE_VERSION);
    	this.DATABASE_NAME = DatabaseName;
		// TODO Auto-generated constructor stub
		this.mContext = context;
        ContactDbSQLHelper.mInstance = this;

	}
    
    public static ContactDbSQLHelper getInstance(Context ctx , String databaseName) {
        /** 
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information: 
         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
         */

        if (mInstance == null) {
        	mContext = ctx;
        	DATABASE_NAME = databaseName;
            ContactDbSQLHelper.mInstance = new ContactDbSQLHelper(ctx.getApplicationContext() , ContactDbSQLHelper.DATABASE_NAME);
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
	public void onCreate(SQLiteDatabase db ) {
		
		db.execSQL("CREATE TABLE IF NOT EXISTS ContactTable"+
				" (Id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"  ContactOrEntityId INTEGER NOT NULL DEFAULT 0,"+
				"  ContactType INTEGER NOT NULL DEFAULT 1," +
				"  FirstName TEXT Not NULL," +
				"  MiddleName TEXT Not NULL," +
				"  LastName TEXT Not NULL," +
				"  JsonValue TEXT NOT NULL DEFAULT ''); " +
				"CREATE INDEX ContactOrEntityId_INDEX ON ContactTable (ContactOrEntityId);"+
				"CREATE INDEX ContactType_INDEX ON ContactTable (ContactType);");
	}
	@Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
    }
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS ContactTable");
        onCreate(db);
	}
	
}
