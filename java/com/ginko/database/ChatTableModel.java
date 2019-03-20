package com.ginko.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ginko.vo.ImMessageVO;
import com.ginko.vo.TcImageVO;
import com.sz.util.json.JsonConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatTableModel extends ChatHistoryDbSQLHelper{
	public String TABLE_NAME = "MessageBoardTable";

	private static String DBName = "";

	private static Context mContext;

	private static ChatHistoryDbSQLHelper sqlHelper = null;

	private static SQLiteDatabase db = null;

	private static ChatTableModel mInstance = null;


	public ChatTableModel(Context context, String DatabaseName) {
		super(context , DatabaseName);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.DBName = DatabaseName;
		this.sqlHelper =  ChatHistoryDbSQLHelper.getInstance(context , this.DBName);
        ChatTableModel.mInstance = this;
	}
	public static ChatTableModel getInstance(Context ctx , String databaseName) {

        if (mInstance == null) {
        	mContext = ctx;
        	DBName = databaseName;
            ChatTableModel.mInstance = new ChatTableModel(ctx.getApplicationContext() , databaseName);
        }
        return mInstance;
    }
	
	public static void clearInstance()
	{
		mInstance = null;
		sqlHelper = null;
        ChatHistoryDbSQLHelper.clearInstance();
	}
	public synchronized void getDB()
	{
		if(sqlHelper == null)
			sqlHelper = ChatHistoryDbSQLHelper.getInstance(mContext , this.DBName);

		if(db == null)
		{
			db = sqlHelper.getWritableDatabase();
		}
		/*
		else
		{
			if(db.isReadOnly())
			{
				try
				{
					db.close();
				}catch(Exception e){e.printStackTrace();}
				finally{
					db = null;
					db = sqlHelper.getWritableDatabase();
				}
			}
			else
			{
				if(!db.isOpen())
				{
					try
					{
						db.close();
					}catch(Exception e){e.printStackTrace();}
					finally{
						db = null;
						db = sqlHelper.getWritableDatabase();
					}
				}
			}
		}*/
		
	}
		
	public synchronized void add(MessageDbConstruct struct)
	{
		getDB();

        /*String query = String.format("insert into %s (MsgId,BoardId,MsgTime,MsgContent,MedaiFilePath) values(%d,%d,%s,%s,%s);" ,
                        TABLE_NAME,
                        struct.msgId,
                        struct.boardId,
                        struct.strMsgTime,
                        struct.msgContent,
                        struct.mediaFilePath
                        );

        boolean bExists = false;
        Cursor cursor = db.query(TABLE_NAME, new String[] {
                        "MsgId"}, "MsgId" + "=?",
                new String[] {String.valueOf(struct.msgId) }, null, null, null, null);
        if (cursor != null && cursor.getCount()>0)
        {
            bExists = true;
        }
        if (cursor!= null)
            cursor.close();*/

        ContentValues values = new ContentValues();
        values.put("MsgId", Long.valueOf(struct.msgId));
        values.put("BoardId" , Long.valueOf(struct.boardId));
        values.put("MsgTime" , struct.strMsgTime);
        values.put("MsgContent" , struct.msgContent);
        values.put("MedaiFilePath", struct.mediaFilePath);

        long last_insert_id = 0;
        //if(bExists == false)
        {
            try {
                last_insert_id = db.insert(TABLE_NAME, null, values);
            }catch(Exception e)
            {
                e.printStackTrace();
            }

        }
        /*else//else update row
        {
            last_insert_id = db.update(TABLE_NAME, values, "ContactOrEntityId = ?",
                    new String[] { String.valueOf(struct.getContactOrEntityId()) });
        }*/

	}
	public synchronized void addAll(ArrayList<MessageDbConstruct> messageStructs)
	{
		if(messageStructs == null || messageStructs.size() == 0) return;
		getDB();

		for(MessageDbConstruct struct : messageStructs) {
            String query = String.format("insert into %s (MsgId,BoardId,MsgTime,MsgContent,MedaiFilePath) values(%d,%d,%s,%s,%s);" ,
                    TABLE_NAME,
                    struct.msgId,
                    struct.boardId,
                    struct.strMsgTime,
                    struct.msgContent,
                    struct.mediaFilePath
            );

            db.execSQL(query);
		}
	}
	public synchronized int update(MessageDbConstruct struct)
	{
		getDB();
		
		ContentValues values = new ContentValues();
		values.put("MsgId", Long.valueOf(struct.msgId));
        values.put("BoardId" , Long.valueOf(struct.boardId));
        values.put("MsgTime" , struct.strMsgTime);
		values.put("MsgContent" , struct.msgContent);
		values.put("MedaiFilePath", struct.mediaFilePath);

		int last_insert_id = db.update(TABLE_NAME, values, "MsgId = ?",
	            new String[] { String.valueOf(struct.msgId) });
    	return last_insert_id;
	}

	public synchronized MessageDbConstruct get(long msgId)
	{
		getDB();
		
		Cursor cursor = db.query(TABLE_NAME, new String[] {
				"MsgId",
				"BoardId",
				"MsgTime",
				"MsgContent",
				"MedaiFilePath"
				}, "MsgId" + "=?",
	            new String[] { String.valueOf(msgId) }, null, null, null, null);
		if(cursor == null)
			return null;

		
	    if(cursor.getCount()<=0)
	    {
	    	cursor.close();
	    	return null;
	    }

        MessageDbConstruct struct = null;
	    if(cursor.moveToFirst())
	    {
            struct = new MessageDbConstruct();
            struct.msgId = cursor.getLong(0);
            struct.boardId = cursor.getLong(1);
            struct.strMsgTime = cursor.getString(2);
            struct.msgContent = cursor.getString(3);
            struct.mediaFilePath = cursor.getString(4);
	    }
	    cursor.close();
		return struct;
	}
	public synchronized List<MessageDbConstruct> getAll(long boardId)
	{
		getDB();
		List<MessageDbConstruct> messageList = new ArrayList<MessageDbConstruct>();
		 // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ";
	 
	    Cursor cursor = db.rawQuery(selectQuery, null);
	    // looping through all rows and adding to list
	    if(cursor == null)
	    {
	    	return messageList;
	    }
	    if (cursor.moveToFirst()) {
	        do {
				MessageDbConstruct struct = new MessageDbConstruct();
                struct.msgId = cursor.getLong(0);
                struct.boardId = cursor.getLong(1);
				struct.strMsgTime = cursor.getString(2);
				struct.msgContent = cursor.getString(3);
				struct.mediaFilePath = cursor.getString(4);
                messageList.add(struct);
	        }while(cursor.moveToNext());
	    }
	    cursor.close();
		return messageList;
	}

	public synchronized List<ImMessageVO> getLatestChatsByTime(long boardID , String datetime , boolean isEarlier , Integer number)
	{
		getDB();
		List<ImMessageVO> messageList = new ArrayList<ImMessageVO>();
		// Select All Query
		String selectQuery = "";
        if(datetime == null)
        {
            selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE BoardId=" + String.valueOf(boardID) + " ORDER BY date(MsgTime) DESC Limit "+String.valueOf(number);
        }
        else
        {
            if(isEarlier) {
                selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE BoardId=" + String.valueOf(boardID) + " AND MsgTime < "+"Datetime('"+datetime+"') ORDER BY date(MsgTime) ASC Limit "+String.valueOf(number);
            }
            else
            {
                selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE BoardId=" + String.valueOf(boardID) + " AND MsgTime > "+"Datetime('"+datetime+"') ORDER BY date(MsgTime) ASC Limit "+String.valueOf(number);
            }
        }

		Cursor cursor = null;
		try {
			cursor = db.rawQuery(selectQuery, null);
		}catch(Exception e)
		{
			e.printStackTrace();
			return messageList;
		}
		// looping through all rows and adding to list
		if(cursor == null)
		{
			return messageList;
		}
		if (cursor.moveToFirst()) {
			do {
                MessageDbConstruct struct = new MessageDbConstruct();
                struct.msgId = cursor.getLong(0);
                struct.boardId = cursor.getLong(1);
                struct.strMsgTime = cursor.getString(2);
                struct.msgContent = cursor.getString(3);
                struct.mediaFilePath = cursor.getString(4);
				if(struct.msgContent.equals(""))
				{
					cursor.moveToNext();
					continue;
				}
				try {
					JSONObject jsonObject = new JSONObject(struct.msgContent );
					ImMessageVO msg = parseJSONMessageVO(jsonObject , struct.strMsgTime);
                    msg.setFile(struct.mediaFilePath);
                    if(msg != null)
                        messageList.add(msg);
				} catch (JSONException e) {
					e.printStackTrace();
					cursor.moveToNext();
					continue;
				}

			}while(cursor.moveToNext());
		}
		cursor.close();
		return messageList;
	}
	
	public synchronized void deleteMessage(ImMessageVO msg) {

	    getDB();

        try {
            db.delete(TABLE_NAME, "MsgId = ?",
                    new String[]{String.valueOf(msg.getMsgId())});
        }catch(Exception e)
        {
            e.printStackTrace();
        }
	}

	public synchronized void deleteWholeBoardMessage(long boardId) {

		getDB();
        try {
            String query = "DELETE FROM " + TABLE_NAME + " WHERE BoardId=" + String.valueOf(boardId);

            db.execSQL(query);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
	}

   	public synchronized void deleteAllChatHistory() {
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

	public static ImMessageVO parseJSONMessageVO(JSONObject jsonObject , String utcTIme)
	{
        if(jsonObject == null) return null;
		try
		{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            ImMessageVO msg = JsonConverter.json2Object(jsonObject, (Class<ImMessageVO>) ImMessageVO.class);
            msg.utcSendTime = simpleDateFormat.parse(utcTIme);

            return msg;
    	}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

	}

}
