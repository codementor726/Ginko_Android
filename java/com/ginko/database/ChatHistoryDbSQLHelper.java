package com.ginko.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatHistoryDbSQLHelper extends SQLiteOpenHelper {
	//database openhelper instance
	private static ChatHistoryDbSQLHelper mInstance = null;

	// All Static variables
    // Database Version
	private static final int DATABASE_VERSION = 6;

    // Database Name
    private static String DATABASE_NAME = "MessageBoardDB";

    //context
    private static Context mContext;

    public ChatHistoryDbSQLHelper(Context context, String DatabaseName) {
		super(context, DatabaseName, null, DATABASE_VERSION);
    	this.DATABASE_NAME = DatabaseName;
		// TODO Auto-generated constructor stub
		this.mContext = context;
        ChatHistoryDbSQLHelper.mInstance = this;

	}
    
    public static ChatHistoryDbSQLHelper getInstance(Context ctx , String databaseName) {
        /** 
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information: 
         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
         */

        if (mInstance == null) {
        	mContext = ctx;
        	DATABASE_NAME = databaseName;
            ChatHistoryDbSQLHelper.mInstance = new ChatHistoryDbSQLHelper(ctx.getApplicationContext() , ChatHistoryDbSQLHelper.DATABASE_NAME);
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

		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS MessageBoardTable" +
					" (MsgId INTEGER PRIMARY KEY," +
					"  BoardId INTEGER NOT NULL DEFAULT 0," +
					"  MsgTime DATETIME Not NULL," +
					"  MsgContent TEXT Not NULL DEFAULT ''," +
					"  MedaiFilePath TEXT Not NULL DEFAULT '');" +
					" CREATE INDEX BoardId_Index ON MessageBoardTable (BoardId);" +
					" CREATE INDEX MsgTime_Index ON MessageBoardTable (MsgTime);");
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
		db.execSQL("DROP TABLE IF EXISTS MessageBoardTable");
        onCreate(db);
	}
	
}
