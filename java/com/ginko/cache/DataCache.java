package com.ginko.cache;

import com.ginko.activity.contact.ContactMainActivity.FilterType;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.utils.SDCardUtil;
import com.ginko.vo.PageCategory;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DataCache {
	private static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 10;
	public static final String CONTACTS = "contacts";
	private static final String fileExt = ".ginko";

	public static PageCategory getCatetoryByid(int parantId) {
		InputStream is = MyApp.getInstance().getResources().openRawResource(R.raw.entity_category_list);
		try {
			String resp = IOUtils.toString(is);
			if (StringUtils.isBlank(resp)) {
				return null;
			}
			JSONObject json = new JSONObject(resp);
			JSONArray allCatetories = json.getJSONArray("data");
			for (int i = 0; i < allCatetories.length(); i++) {
				JSONObject jsonCategory = allCatetories.getJSONObject(i);
				int id = jsonCategory.getInt("id");
				if (id == parantId) {
					return JsonConverter.json2Object(jsonCategory, PageCategory.class);
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (JSONException e) {
			Logger.error(e);
		} catch (JsonConvertException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 
	 * @param contactUid
	 * @param contactType can be 1,2,3; 1:purple contact, 2:grey contact, 3:entity
	 * @return
	 */
	public static JSONObject getContactData(int contactUid, int contactType) {
		JSONObject data = getData(CONTACTS);
		if (data == null){
			return null;
		}
		try {
			JSONArray contacts = data.getJSONArray("data");
			for (int i = 0; i < contacts.length(); i++) {
				JSONObject jsonContact = contacts.getJSONObject(i);
				int cid = jsonContact.getInt("contact_id");
				int type = jsonContact.getInt("contact_type");
				if(cid == contactUid && type == contactType){
					return jsonContact;
				}
			}
		} catch (JSONException e) {
			Logger.error(e);
		}
		return null;
	}

	public static void putData(String key, Object data) {
        if (!SDCardUtil.checkFreeSpace(FREE_SD_SPACE_NEEDED_TO_CACHE)) {
			// SD绌洪棿涓嶈冻
			return;
		}
		File dirFile  = RuntimeContext.getDataCacheForlder();

		File file = new File(dirFile , key + fileExt);
		try {
			file.createNewFile();
			FileWriter writer = new FileWriter(file);
			// Stony, confirmed, also can use Bitmap.CompressFormat.PNG, don't
			// know the difference so far.
//			outStream.write
			
			writer.append(data.toString());
			
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			Logger.warn("FileNotFoundException");
		} catch (IOException e) {
			Logger.warn( "IOException");
		}
	}

	public static JSONObject getData(String key) {
		File dirFile  = RuntimeContext.getDataCacheForlder();
		File file = new File(dirFile , key + fileExt);
		if (!file.exists()){
			return null;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			String cacheData = IOUtils.toString(is);
			return new JSONObject(cacheData);
		} catch (IOException e) {
			Logger.error(e);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.error(e);
		}finally {
			if (is!=null){
				try {
					is.close();
				} catch (IOException e) {
					Logger.error(e);
				}
			}
		}
		return null;
	}


}
