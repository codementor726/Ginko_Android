package com.ginko.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.ginko.activity.contact.ContactItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContactTableModel extends ContactDbSQLHelper{
	public String TABLE_NAME = "ContactTable";
	
	private static String DBName = "";
	
	private static Context mContext;
	
	private static ContactDbSQLHelper sqlHelper = null;
	
	private static SQLiteDatabase db = null;
	
	private static ContactTableModel mInstance = null;
	
	/*
	 Id = 0;
     UserId = 0;
     ContactOrEntityId = 0;
     ContactType = 1;
     JsonValue = "";
	 */
	public ContactTableModel(Context context , String DatabaseName) {
		super(context , DatabaseName);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.DBName = DatabaseName;
		this.sqlHelper =  ContactDbSQLHelper.getInstance(context , this.DBName);
        ContactTableModel.mInstance = this;
	}
	public static ContactTableModel getInstance(Context ctx , String databaseName) {

        if (mInstance == null) {
        	mContext = ctx;
        	DBName = databaseName;
            ContactTableModel.mInstance = new ContactTableModel(ctx.getApplicationContext() , databaseName);
        }
        return mInstance;
    }
	
	public static void clearInstance()
	{
		mInstance = null;
		sqlHelper = null;
        ContactDbSQLHelper.clearInstance();
	}
	public synchronized void getDB()
	{
		if(sqlHelper == null)
			sqlHelper = ContactDbSQLHelper.getInstance(mContext , this.DBName);

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
		
	public synchronized int add(ContactStruct struct)
	{
		getDB();
		
		boolean bExists = false;
		Cursor cursor = db.query(TABLE_NAME, new String[] {
			 	"Id",
				"ContactOrEntityId"}, "ContactOrEntityId" + "=?",
	            new String[] {String.valueOf(struct.getContactOrEntityId()) }, null, null, null, null);
		if (cursor != null && cursor.getCount()>0)
		{
			bExists = true;
		}
		if (cursor!= null)
			cursor.close();
		
		ContentValues values = new ContentValues();
		//values.put("Id" , Long.valueOf(struct.RoomID));
		values.put("ContactOrEntityId" , Integer.valueOf(struct.getContactOrEntityId()));
		values.put("ContactType" , Integer.valueOf(struct.getContactType()));
		values.put("FirstName" , struct.getFirstName());
		values.put("MiddleName" , struct.getMiddleName());
		values.put("LastName" , struct.getLastName());
		values.put("JsonValue" , struct.getJsonValue());

		long last_insert_id = 0;
		if(bExists == false)
		{
			last_insert_id = db.insert(TABLE_NAME, null, values);
		}
		else//else update row
	    {
	    	last_insert_id = db.update(TABLE_NAME, values, "ContactOrEntityId = ?",
		            new String[] { String.valueOf(struct.getContactOrEntityId()) });
	    }

    	return (int)last_insert_id;
	}

	public synchronized void removeAll(ArrayList<ContactStruct> contactStructs)
	{
		if(contactStructs == null || contactStructs.size() == 0) return;
		getDB();

		String sql = "DELETE FREOM " + TABLE_NAME + " WHERE ContactOrEntityId = ?";

		db.beginTransaction();
		SQLiteStatement stmt = db.compileStatement(sql);

		for(ContactStruct struct : contactStructs) {
			ContentValues values = new ContentValues();
			//values.put("Id" , Long.valueOf(struct.RoomID));

			stmt.bindString(1, String.valueOf(struct.getContactOrEntityId()));
			stmt.execute();
			stmt.clearBindings();
		}

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public synchronized void addAll(ArrayList<ContactStruct> contactStructs)
	{
		if(contactStructs == null || contactStructs.size() == 0) return;
		getDB();

		String sql_1 = "INSERT INTO " + TABLE_NAME + " (ContactOrEntityId, ContactName, ProfileImage, Latitude, Longitude) " +
				"VALUES (?, ?, ?, ?, ?)";
		String sql_2 = "UPDATE " + TABLE_NAME + "SET WHERE ContactOrEntityId = ?";

		for(ContactStruct struct : contactStructs) {
			boolean bExists = false;
			Cursor cursor = db.query(TABLE_NAME, new String[]{
							"Id",
							"ContactOrEntityId"}, "ContactOrEntityId" + "=?",
					new String[]{String.valueOf(struct.getContactOrEntityId())}, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				bExists = true;
			}
			if (cursor != null)
				cursor.close();

			ContentValues values = new ContentValues();
			//values.put("Id" , Long.valueOf(struct.RoomID));
			values.put("ContactOrEntityId", Integer.valueOf(struct.getContactOrEntityId()));
			values.put("ContactType", Integer.valueOf(struct.getContactType()));
			values.put("FirstName", struct.getFirstName());
			values.put("MiddleName" , struct.getMiddleName());
			values.put("LastName", struct.getLastName());
			values.put("JsonValue", struct.getJsonValue());

			long last_insert_id = 0;
			if (bExists == false) {
				last_insert_id = db.insert(TABLE_NAME, null, values);
			} else//else update row
			{
				last_insert_id = db.update(TABLE_NAME, values, "ContactOrEntityId = ?",
						new String[]{String.valueOf(struct.getContactOrEntityId())});
			}
		}
	}
	public synchronized int update(ContactStruct struct)
	{
		getDB();
		
		ContentValues values = new ContentValues();
		values.put("Id" , Integer.valueOf(struct.getId()));
        values.put("ContactOrEntityId" , Integer.valueOf(struct.getContactOrEntityId()));
        values.put("ContactType" , Integer.valueOf(struct.getContactType()));
		values.put("FirstName" , struct.getFirstName());
		values.put("MiddleName" , struct.getMiddleName());
		values.put("LastName" , struct.getLastName());
        values.put("JsonValue", struct.getJsonValue());
		 
		int last_insert_id = db.update(TABLE_NAME, values, "Id = ?",
	            new String[] { String.valueOf(struct.getId()) });
    	return last_insert_id;
	}
	public synchronized int updateContactWithContactId(ContactStruct struct)
	{
		getDB();

		ContentValues values = new ContentValues();
		values.put("Id" , Integer.valueOf(struct.getId()));
		values.put("ContactOrEntityId" , Integer.valueOf(struct.getContactOrEntityId()));
		values.put("ContactType" , Integer.valueOf(struct.getContactType()));
		values.put("FirstName" , struct.getFirstName());
		values.put("MiddleName" , struct.getMiddleName());
		values.put("LastName" , struct.getLastName());
		values.put("JsonValue", struct.getJsonValue());

		int last_insert_id = db.update(TABLE_NAME, values, "ContactOrEntityId = ?",
				new String[] { String.valueOf(struct.getContactOrEntityId()) });
		return last_insert_id;
	}
	public synchronized ContactStruct get(int id)
	{
		getDB();
		
		Cursor cursor = db.query(TABLE_NAME, new String[] {
				"Id",
				"ContactOrEntityId",
				"GroupID",
				"ContactType",
				"FirstName",
				"MiddleName",
				"LastName",
				"JsonValue"
				}, "Id" + "=?",
	            new String[] { String.valueOf(id) }, null, null, null, null);
		if(cursor == null)
			return null;

		
	    if(cursor.getCount()<=0)
	    {
	    	cursor.close();
	    	return null;
	    }

        ContactStruct struct = null;
	    if(cursor.moveToFirst())
	    {
			struct = new ContactStruct();
			struct.setId(cursor.getInt(0));
			struct.setContactOrEntityId(cursor.getInt(1));
			struct.setContactType(cursor.getInt(2));
			struct.setFirstName(cursor.getString(3));
			struct.setMiddleName(cursor.getString(4));
			struct.setLastName(cursor.getString(5));
			struct.setJsonValue(cursor.getString(6));
	    }
	    cursor.close();
		return struct;
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

	public synchronized ContactStruct getContactById(int contactId)
	{
		getDB();

		String selectQuery = "SELECT  * FROM " + TABLE_NAME+" WHERE ContactOrEntityId="+String.valueOf(contactId);

		Cursor cursor = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if(cursor == null)
		{
			return null;
		}
		/*Cursor cursor = db.query(TABLE_NAME, new String[] {
						"Id",
						"ContactOrEntityId",
						"GroupID",
						"ContactType",
						"FirstName",
						"MiddleName",
						"LastName",
						"JsonValue"
				}, "ContactOrEntityId" + "=?",
				new String[] { String.valueOf(contactId) }, null, null, null, null);*/
		if(cursor == null)
			return null;


		if(cursor.getCount()<=0)
		{
			cursor.close();
			return null;
		}

		ContactStruct struct = null;
		if(cursor.moveToFirst())
		{
			struct = new ContactStruct();
			struct.setId(cursor.getInt(0));
			struct.setContactOrEntityId(cursor.getInt(1));
			struct.setContactType(cursor.getInt(2));
			struct.setFirstName(cursor.getString(3));
			struct.setMiddleName(cursor.getString(4));
			struct.setLastName(cursor.getString(5));
			struct.setJsonValue(cursor.getString(6));
			if(!struct.getJsonValue().equals("")) {
				try {
					JSONObject jsonObject = new JSONObject(struct.getJsonValue());
					if (struct.getContactType() == 1) //purple contact
					{
						struct.setContactItem(parsePurpleContact(jsonObject));
					} else if (struct.getContactType() == 2)//grey contact
					{
						struct.setContactItem(parseGreyContact(jsonObject));
					} else {
						struct.setContactItem(parseEntityContact(jsonObject));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		cursor.close();
		return struct;
	}
	public synchronized List<ContactStruct> getAll()
	{
		getDB();
		List<ContactStruct> contactList = new ArrayList<ContactStruct>();
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
                ContactStruct struct = new ContactStruct();
                struct.setId(cursor.getInt(0));
                struct.setContactOrEntityId(cursor.getInt(1));
                struct.setContactType(cursor.getInt(2));
				struct.setFirstName(cursor.getString(3));
				struct.setMiddleName(cursor.getString(4));
				struct.setLastName(cursor.getString(5));
                struct.setJsonValue(cursor.getString(6));
                contactList.add(struct);
	        }while(cursor.moveToNext());
	    }
	    cursor.close();
		return contactList;
	}

	public synchronized List<ContactStruct> getAllContactItems()
	{
		getDB();
		List<ContactStruct> contactList = new ArrayList<ContactStruct>();
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
				ContactStruct struct = new ContactStruct();
				struct = new ContactStruct();
				struct.setId(cursor.getInt(0));
				struct.setContactOrEntityId(cursor.getInt(1));
				struct.setContactType(cursor.getInt(2));
				struct.setFirstName(cursor.getString(3));
				struct.setMiddleName(cursor.getString(4));
				struct.setLastName(cursor.getString(5));
				struct.setJsonValue(cursor.getString(6));
				if(struct.getJsonValue().equals(""))
				{
					cursor.moveToNext();
					continue;
				}
				try {
					JSONObject jsonObject = new JSONObject(struct.getJsonValue());
					if(struct.getContactType() == 1) //purple contact
					{
						struct.setContactItem(parsePurpleContact(jsonObject));
					}
					else if(struct.getContactType() == 2)//grey contact
					{
						struct.setContactItem(parseGreyContact(jsonObject));
					}
					else
					{
						struct.setContactItem(parseEntityContact(jsonObject));
					}
				} catch (JSONException e) {
					e.printStackTrace();
					cursor.moveToNext();
					continue;
				}

				contactList.add(struct);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return contactList;
	}

	public synchronized List<ContactStruct> getAllGreyContactItems()
	{
		getDB();
		List<ContactStruct> contactList = new ArrayList<ContactStruct>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_NAME +" WHERE ContactType=2";

		Cursor cursor = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if(cursor == null)
		{
			return contactList;
		}
		if (cursor.moveToFirst()) {
			do {
				ContactStruct struct = new ContactStruct();
				struct = new ContactStruct();
				struct.setId(cursor.getInt(0));
				struct.setContactOrEntityId(cursor.getInt(1));
				struct.setContactType(cursor.getInt(2));
				struct.setFirstName(cursor.getString(3));
				struct.setMiddleName(cursor.getString(4));
				struct.setLastName(cursor.getString(5));
				struct.setJsonValue(cursor.getString(6));
				if(struct.getJsonValue().equals(""))
				{
					cursor.moveToNext();
					continue;
				}
				try {
					JSONObject jsonObject = new JSONObject(struct.getJsonValue());
					if(struct.getContactType() == 1) //purple contact
					{
						struct.setContactItem(parsePurpleContact(jsonObject));
					}
					else if(struct.getContactType() == 2)//grey contact
					{
						struct.setContactItem(parseGreyContact(jsonObject));
					}
					else
					{
						struct.setContactItem(parseEntityContact(jsonObject));
					}
				} catch (JSONException e) {
					e.printStackTrace();
					cursor.moveToNext();
					continue;
				}

				contactList.add(struct);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return contactList;
	}

	public synchronized List<ContactStruct> getAllPurpleContactItems()
	{
		getDB();
		List<ContactStruct> contactList = new ArrayList<ContactStruct>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_NAME +" WHERE ContactType=1";

		Cursor cursor = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if(cursor == null)
		{
			return contactList;
		}
		if (cursor.moveToFirst()) {
			do {
				ContactStruct struct = new ContactStruct();
				struct = new ContactStruct();
				struct.setId(cursor.getInt(0));
				struct.setContactOrEntityId(cursor.getInt(1));
				struct.setContactType(cursor.getInt(2));
				struct.setFirstName(cursor.getString(3));
				struct.setMiddleName(cursor.getString(4));
				struct.setLastName(cursor.getString(5));
				struct.setJsonValue(cursor.getString(6));
				if(struct.getJsonValue().equals(""))
				{
					cursor.moveToNext();
					continue;
				}
				try {
					JSONObject jsonObject = new JSONObject(struct.getJsonValue());
					if(struct.getContactType() == 1) //purple contact
					{
						struct.setContactItem(parsePurpleContact(jsonObject));
					}
					else if(struct.getContactType() == 2)//grey contact
					{
						struct.setContactItem(parseGreyContact(jsonObject));
					}
					else
					{
						struct.setContactItem(parseEntityContact(jsonObject));
					}
				} catch (JSONException e) {
					e.printStackTrace();
					cursor.moveToNext();
					continue;
				}

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
				new String[] { String.valueOf(contactId) });
	}

    public synchronized void deleteContact(ContactStruct struct) {

        getDB();
        ContactStruct Struct = (ContactStruct)struct;
        db.delete(TABLE_NAME, "Id = ?",
                new String[] { String.valueOf(Struct.getId()) });
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

	public static ContactItem parsePurpleContact(JSONObject jsonObject)
	{
		try
		{
			String firstName = jsonObject.optString("first_name", "");
			String middleName = jsonObject.optString("middle_name", "");
			String lastName = jsonObject.optString("last_name", "");
			String profileImage = jsonObject.optString("profile_image", "");
			if (firstName.isEmpty() && lastName.isEmpty()) {
				return null;
			}

			JSONArray phonesJsonArray = jsonObject.optJSONArray("phones");
			JSONArray emailsJsonArray = jsonObject.optJSONArray("emails");

			final List<String> phones = new ArrayList<String>();
			if (phonesJsonArray != null) {
				for (int i = 0; i < phonesJsonArray.length(); i++) {
					phones.add(phonesJsonArray.optString(i));
				}
			}

			phones.add("Cancel");
			final List<String> emails = new ArrayList<String>();
			if (emailsJsonArray != null) {
				for (int i = 0; i < emailsJsonArray.length(); i++) {
					emails.add(emailsJsonArray.optString(i));
				}
			}
			ContactItem item = ContactItem.createItem(firstName, lastName);
			item.setMiddleName(middleName);
			item.setProfileImage(profileImage);
			item.setContactType(1);
			item.setContactId(jsonObject.optInt("contact_id"));
			item.setPhones(phones);
			item.setEmails(emails);
			item.setIsRead(jsonObject.optBoolean("is_read", false));
			item.setIsFavorite(jsonObject.optBoolean("is_favorite", false));
			item.setSharingStatus(jsonObject.optInt("sharing_status", 1)); //1:home , 2:work , 3: both

			return item;
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static ContactItem parseGreyContact(JSONObject jsonObject)
	{
		try
		{
			String firstName = jsonObject.optString("first_name", "");
			String lastName = jsonObject.optString("last_name", "");
			String middleName = jsonObject.optString("middle_name", "");
			String profileImage = jsonObject.optString("photo_url", "");

			JSONArray phonesJsonArray = jsonObject.optJSONArray("phones");
			JSONArray emailsJsonArray = jsonObject.optJSONArray("emails");
			JSONArray fieldsJsonArray = jsonObject.optJSONArray("fields");

			final List<String> phones = new ArrayList<String>();
			if (phonesJsonArray != null) {
				for (int i = 0; i < phonesJsonArray.length(); i++) {
					phones.add(phonesJsonArray.optString(i));
				}
			}
			if (phones.size() < 1)
			{
				if (fieldsJsonArray != null)
				{
					for (int i = 0; i < fieldsJsonArray.length(); i++) {
						JSONObject data = fieldsJsonArray.getJSONObject(i);
						if (data.getString("field_type").equalsIgnoreCase("mobile"))
							phones.add(data.getString("field_value"));
					}
				}
			}

			phones.add("Cancel");

			final List<String> emails = new ArrayList<String>();
			if (emailsJsonArray != null) {
				for (int i = 0; i < emailsJsonArray.length(); i++) {
					emails.add(emailsJsonArray.optString(i));
				}
			}

			if (emails.size() < 1)
			{
				if (fieldsJsonArray != null)
				{
					for (int i = 0; i < fieldsJsonArray.length(); i++) {
						JSONObject data = fieldsJsonArray.getJSONObject(i);
						if (data.getString("field_type").equalsIgnoreCase("email"))
							emails.add(data.getString("field_value"));
					}
				}
			}

			ContactItem item = ContactItem.createItem(firstName, lastName);
			item.setMiddleName(middleName);
			item.setProfileImage(profileImage);
			item.setContactId(jsonObject.optInt("contact_id"));
			item.setContactType(2);
			item.setPhones(phones);
			item.setEmails(emails);
			item.setIsRead(jsonObject.optBoolean("is_read", true));
			item.setIsFavorite(jsonObject.optBoolean("is_favorite", true));
			item.setGreyType(jsonObject.optInt("type")); //0:entity , 1:home , 2:work
			return item;
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static ContactItem parseEntityContact(JSONObject jsonObject)
	{
		try
		{
			String name = jsonObject.optString("name", "");
			String profileImage = jsonObject.optString("profile_image", "");

			ContactItem item = ContactItem.createItem(name, "");
			item.setProfileImage(profileImage);
			item.setEntityName(name);
			item.setContactId(jsonObject.optInt("entity_id"));
			item.setContactType(3);
			item.setIsRead(jsonObject.optBoolean("is_read", false));
			item.setIsFavorite(jsonObject.optBoolean("is_favorite", false));
			item.setnFollowerCount(jsonObject.optInt("follower_total", 0));
			item.setFollowed(true);
			return item;
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
