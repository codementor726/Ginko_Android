package com.sz.util.json;

import com.ginko.common.Logger;
import com.ginko.context.ConstValues;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;


public class JsonConverter {

	private static ObjectAttributesAnnotationScanner scanner =new ObjectAttributesAnnotationScanner();

	private static java.util.logging.Logger log = java.util.logging.Logger.getAnonymousLogger();

	// general json
	public static JSONObject object2Json(Object bean) throws JsonConvertException {
		JSONObject json = generalSendObject(bean);
		return json;
	}
	
	public static String object2JsonString(Object bean) throws JsonConvertException {
		JSONObject json=object2Json(bean);
		return json.toString();
	}

	public <T > T json2Object(
			String jsonStr, Class<T> messageClass)
			throws JsonConvertException {

		try {
			return json2Object(new JSONObject(jsonStr),
					messageClass);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.error(e);
		}
		return null;
	}

	public static <T> T json2Object(JSONObject jsonObj, Class<T> messageClass)
			throws JsonConvertException {
		boolean needntConvert = messageClass.isAssignableFrom(JSONObject.class);
		if (needntConvert) {
			return (T) jsonObj;
		}else{
			return (T) _json2Obj(jsonObj, messageClass);
		}
	}

	public static <T> T _json2Obj(JSONObject jsonObj, Class<T> messageClass)
			throws JsonConvertException {

		try {
			Object obj = messageClass.newInstance();
			// scan class structure
			Map<String, FieldStructsBean> attributeMapping = scanner
					.generalObjectAttributesMapping(messageClass);

			for (Iterator iterator = jsonObj.keys(); iterator.hasNext();) {
				String alias = (String) iterator.next();

				// get field structure
				FieldStructsBean fieldStructsBean = getFieldStructsBeanByAlias(
						alias, attributeMapping);
				if (null == fieldStructsBean) {
					Logger.debug("Can't find field named {} in class {}" + alias
                            + messageClass.getName());
					continue;
				}
				// Logger.debug("field-alias="+alias);
				String setMethodName = fieldStructsBean.getFieldSetMethodName();
				if (setMethodName==null){
					continue;
				}
				Class fieldType = fieldStructsBean.getFieldType();
				Method setMethod = messageClass.getMethod(setMethodName,
						fieldType);
				Field oriField = fieldStructsBean.getOriginalField();

				Object fieldValue = jsonObj.get(alias);

				if (null != fieldValue
						&& JSONObject.class.isAssignableFrom(fieldValue
								.getClass())) {
					// restore map
					if (Properties.class.isAssignableFrom(fieldType)) {
						fieldValue = restoreProperties((JSONObject) fieldValue);
					} else if (Map.class.isAssignableFrom(fieldType)) {
						fieldValue = restoreMap((JSONObject) fieldValue,
								getGenericType(oriField));
					}
					// restore Reference object
					else if (scanner.isNotJavaClass(fieldType)) {
						fieldValue = _json2Obj(
								(JSONObject) fieldValue,
								fieldStructsBean.getFieldType());
					}
				} else if (null != fieldValue
						&& JSONArray.class.isAssignableFrom(fieldValue
								.getClass())) {
					// restore collection
					if (Collection.class.isAssignableFrom(fieldType)) {
						Type[] collectionType = getGenericType(oriField);
						if (isGenericType(collectionType[0])) {
							fieldValue = restoreCollection(
									(JSONArray) fieldValue, fieldType,
									(ParameterizedType) collectionType[0]);
						} else {
							// array generic
							if (GenericArrayType.class
									.isAssignableFrom(collectionType[0]
											.getClass())) {
								fieldValue = restoreCollection(
										(JSONArray) fieldValue, fieldType,
										(GenericArrayType) collectionType[0]);
							} else {
								fieldValue = restoreCollection(
										(JSONArray) fieldValue, fieldType,
										(Class) collectionType[0]);
							}
						}
					}
					// restore array
					else if (fieldType.isArray()) {
						Type fieldGenericType = oriField.getGenericType();
						// List or Map array
						if (GenericArrayType.class
								.isAssignableFrom(fieldGenericType.getClass())) {
							GenericArrayType arrGenericType = (GenericArrayType) fieldGenericType;
							fieldValue = restoreArray((JSONArray) fieldValue,
									arrGenericType);
						}
						// Object array
						else if (Class.class.isAssignableFrom(fieldGenericType
								.getClass())) {
							String className = ((Class) fieldGenericType)
									.getCanonicalName();
							className = className.substring(0,
									className.length() - 2);
							Class elementType = Class.forName(className);
							fieldValue = restoreArray((JSONArray) fieldValue,
									elementType, null);
						}
					}
				} else {
					fieldValue = baseTypeConverter(fieldValue,  fieldStructsBean);
					if (fieldValue == null){
						continue;
					}
				}

				setMethod.invoke(obj, fieldValue);
			}

			return (T)obj;
		} catch (Exception e) {
			throw new JsonConvertException(e);
		}
	}

	// restore Map
	private static Map restoreMap(JSONObject jsonObj, Type[] parameterizedType)
			throws JsonConvertException {
		if (null == parameterizedType || parameterizedType.length != 2) {
			throw new JsonConvertException(
					"Not specified Generic Type or the Generic type not match for Map");
		}
		Type valueType = parameterizedType[1];
		boolean isGenericType = isGenericType(valueType);

		Map restoreMap = new HashMap();
		for (Iterator iterator = jsonObj.keys(); iterator.hasNext();) {
			String keyStr = (String) iterator.next();
			Object obj;
			try {
				obj = jsonObj.get(keyStr);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Logger.error(e);
				continue;
			}
			// generic type is Array
			if (JSONArray.class.isAssignableFrom(obj.getClass())
					&& GenericArrayType.class.isAssignableFrom(valueType
							.getClass())) {
				obj = restoreArray((JSONArray) obj,
						(GenericArrayType) valueType);
			}
			// generic type is Object(not in java.lang)
			else if (!isGenericType
					&& scanner.isNotJavaClass((Class) valueType)) {
				obj = _json2Obj((JSONObject) obj, (Class) valueType);
			} else {
				// get Collection or Map generic type
				Type[] genericType = getGenericType((ParameterizedType) valueType);
				// object type
				Class objRealClass = (Class) ((ParameterizedType) valueType)
						.getRawType();
				// generic type is Collection
				if (JSONArray.class.isAssignableFrom(obj.getClass())
						&& Collection.class.isAssignableFrom(objRealClass)
						&& genericType.length == 1) {
					// generic type of Collection
					ParameterizedType collectionGenericType = (ParameterizedType) genericType[0];
					obj = restoreCollection((JSONArray) obj, objRealClass,
							collectionGenericType);
				}
				// generic type is Map
				else if (JSONObject.class.isAssignableFrom(obj.getClass())
						&& Map.class.isAssignableFrom(objRealClass)
						&& genericType.length == 2) {
					obj = restoreMap((JSONObject) obj, genericType);
				}
			}
			restoreMap.put(keyStr, obj);
		}
		return restoreMap;
	}

	// restore properties
	private static Properties restoreProperties(JSONObject jsonObj)
			throws JsonConvertException {
		if (null == jsonObj)
			return null;
		Properties prop = new Properties();
		for (Iterator iterator = jsonObj.keys(); iterator.hasNext();) {
			String keyStr = (String) iterator.next();

			try {
				String value = (String) jsonObj.get(keyStr);
				prop.setProperty(keyStr, value);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Logger.error(e);
			}
		}
		return prop;
	}

	// restore Collection
	private static Collection restoreCollection(JSONArray jsonArr, Class collType,
			Type elementType) throws JsonConvertException {
		// list
		Collection coll = new ArrayList();
		// vector
		if (Vector.class == collType) {
			coll = new Vector();
		}
		// set
		else if (Set.class == collType) {
			coll = new HashSet();
		}
		for (int i = 0; i < jsonArr.length(); i++) {
			Object obj = null;
			try {
				obj = jsonArr.get(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Logger.error(e);
			}
			// complex Collection
			if (isGenericType(elementType)) {
				Type[] genericType = getGenericType((ParameterizedType) elementType);
				if (JSONArray.class.isAssignableFrom(obj.getClass())
						&& genericType.length == 1) {
					ParameterizedType pt = (ParameterizedType) elementType;
					Class rawType = (Class) pt.getRawType();
					if (isGenericType(genericType[0])) {
						obj = restoreCollection((JSONArray) obj, rawType,
								(ParameterizedType) genericType[0]);
					} else {
						if (GenericArrayType.class
								.isAssignableFrom(genericType[0].getClass())
								&& Collection.class.isAssignableFrom(rawType)) {
							obj = restoreCollection((JSONArray) obj, rawType,
									(GenericArrayType) genericType[0]);
						} else {
							obj = restoreCollection((JSONArray) obj, rawType,
									(Class) genericType[0]);
						}
					}
				}
				// Map
				else if (JSONObject.class.isAssignableFrom(obj.getClass())
						&& genericType.length == 2) {
					obj = restoreMap((JSONObject) obj, genericType);
				}
			}
			// array
			else if (JSONArray.class.isAssignableFrom(obj.getClass())
					&& GenericArrayType.class.isAssignableFrom(elementType
							.getClass())) {
				obj = restoreArray((JSONArray) obj,
						(GenericArrayType) elementType);
			} else if (JSONObject.class.isAssignableFrom(obj.getClass())
					&& scanner.isNotJavaClass((Class) elementType)) {
				obj = _json2Obj((JSONObject) obj, (Class) elementType);
			}
			// array
			coll.add(obj);

		}
		return coll;
	}

	private static Object[] restoreArray(JSONArray jsonArr,
			GenericArrayType arrayGenericType) throws JsonConvertException {
		Type genericType = arrayGenericType.getGenericComponentType();
		boolean isGenericType = isGenericType(genericType);
		if (!isGenericType) {
			// restore Object array
			return restoreArray(jsonArr, (Class) genericType, null);
		}
		ParameterizedType pt = (ParameterizedType) genericType;
		// restore Collection or Map array
		return restoreArray(jsonArr, (Class) pt.getRawType(), pt);
	}

	// restore Object array
	private static Object[] restoreArray(JSONArray jsonArr, Class elementClass,
			ParameterizedType parameterizedType) throws JsonConvertException {
		if (null == jsonArr || jsonArr.length() == 0)
			return null;

		Object[] arr = (Object[]) Array.newInstance(elementClass,
				jsonArr.length());
		for (int i = 0; i < jsonArr.length(); i++) {
			Object arrElemenetObj = null;
			try {
				arrElemenetObj = jsonArr.get(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Logger.error(e);
				continue;
			}
			if (JSONObject.class.isAssignableFrom(arrElemenetObj.getClass())) {
				// Object
				if (scanner.isNotJavaClass(elementClass)) {
					arr[i] = _json2Obj((JSONObject) arrElemenetObj,
							elementClass);
				} else if (Map.class.isAssignableFrom(elementClass)) {
					arr[i] = restoreMap((JSONObject) arrElemenetObj,
							getGenericType(parameterizedType));
				}
			} else if (JSONArray.class.isAssignableFrom(arrElemenetObj
					.getClass())
					&& Collection.class.isAssignableFrom(elementClass)
					&& null != parameterizedType) {
				arr[i] = restoreCollection((JSONArray) arrElemenetObj,
						elementClass,
						(Class) parameterizedType.getActualTypeArguments()[0]);
			} else if (!scanner.isNotJavaClass(elementClass)) {
				arr[i] = arrElemenetObj;
			}
		}
		return arr;
	}

	// convert base field type
	private static Object baseTypeConverter(Object fieldValue, FieldStructsBean fieldBean) {
		Class fieldType = fieldBean.getFieldType();
		
		Class valueType = fieldValue.getClass();
		Object value = null;
		// String
		if (String.class.isAssignableFrom(fieldType)) {
			value = String.valueOf(fieldValue);
		}
		// boolean
		else if (Boolean.class == fieldType || boolean.class == fieldType) {
			value = false;
			if (String.class.isAssignableFrom(valueType)) {
				value = Boolean.valueOf((String) fieldValue);
			} else if (Boolean.class.isAssignableFrom(valueType)) {
				value = fieldValue;
			}
		}
		else if (Date.class == fieldType) {
			try {
				Alias alias = fieldBean.getAlias();
				String format = ConstValues.DEFAULT_DATA_FORMAT;
				if (alias != null) {
					format = alias.format();
				}
				//convert utc time to
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				value = simpleDateFormat.parse(String.valueOf(fieldValue));

				System.out.println("----");
				//value = DateUtils.parseDate(String.valueOf(fieldValue), new String[]{format});
				
			} catch (ParseException e) {
				Logger.error(e);
			}
		}
		// char or number
		else {
			if (fieldValue == JSONObject.NULL){
				return null;
			}
			// byte
			if (Byte.class == fieldType || byte.class == fieldType) {
				String valueStr = String.valueOf(fieldValue);
				if (valueStr.indexOf(".") != -1) {
					valueStr = valueStr.substring(0, valueStr.indexOf("."));
				}
				value = Byte.valueOf(valueStr);
			}
			// char
			else if (Character.class == fieldType || char.class == fieldType) {
				value = (char) 0;
				if (String.class.isAssignableFrom(valueType)) {
					String valueStr = (String) fieldValue;
					value = StringUtils.isBlank(valueStr) ? value : valueStr
							.length() == 1 ? valueStr.charAt(0) : value;
				} else if (!Boolean.class.isAssignableFrom(valueType)) {
					value = (char) ((Number) fieldValue).doubleValue();
				}
			}
			// short
			else if (Short.class == fieldType || short.class == fieldType) {
				String valueStr = String.valueOf(fieldValue);
				if (valueStr.indexOf(".") != -1) {
					valueStr = valueStr.substring(0, valueStr.indexOf("."));
				}
				value = Short.valueOf(valueStr);
			}
			// int
			else if (Integer.class == fieldType || int.class == fieldType) {
				String valueStr = String.valueOf(fieldValue);
				if (valueStr.indexOf(".") != -1) {
					valueStr = valueStr.substring(0, valueStr.indexOf("."));
				}
				value = Integer.valueOf(valueStr);
			}
			// long
			else if (Long.class == fieldType || long.class == fieldType) {
				String valueStr = String.valueOf(fieldValue);
				if (valueStr.indexOf(".") != -1) {
					valueStr = valueStr.substring(0, valueStr.indexOf("."));
				}
				value = Long.valueOf(valueStr);
			}
			// float
			else if (Float.class == fieldType || float.class == fieldType) {
				value = Float.valueOf(String.valueOf(fieldValue));
			}
			// double
			else if (Double.class == fieldType || double.class == fieldType) {
				value = Double.valueOf(String.valueOf(fieldValue));
			}
		}
		return value;
	}

	private static Type[] getGenericType(Field field) {
		Type fieldGenericType = field.getGenericType();
		ParameterizedType pt = (ParameterizedType) fieldGenericType;
		return getGenericType(pt);
	}

	private static Type[] getGenericType(ParameterizedType parameterizedType) {
		return parameterizedType.getActualTypeArguments();
	}

	private static boolean isGenericType(Type genericType) {
		return ParameterizedType.class.isAssignableFrom(genericType.getClass());
	}

	private static FieldStructsBean getFieldStructsBeanByAlias(String alias,
			Map<String, FieldStructsBean> attributeMapping) {
		Set<String> nameSet = attributeMapping.keySet();
		for (String fieldName : nameSet) {
			FieldStructsBean aliasBean = attributeMapping.get(fieldName);
			if (alias.equals(aliasBean.getAliasFieldName())) {
				return aliasBean;
			}
		}
		return null;
	}


	// general object that concert to json
	private static JSONObject generalSendObject(Object bean) throws JsonConvertException {
		// get bean class structure
		Map<String, FieldStructsBean> attributeMapping = scanner.generalObjectAttributesMapping(bean.getClass());
		
		return convertObj2Map(bean,attributeMapping);
	}

	/**
	 * put all properties into Map(convert properties name to annotation described)
	 * 
	 * @param bean
	 * @return Map
	 * @throws JsonConvertException
	 */
	private static JSONObject convertObj2Map(Object bean, Map<String, FieldStructsBean> attributeMapping) throws JsonConvertException {
		// instance a map
		Map<String, Object> attributeValueMapping = new HashMap<String, Object>();
		Set<String> attributeKeySet = attributeMapping.keySet();
		Class beanClass = bean.getClass();
		try {
			for (String fieldName : attributeKeySet) {
				// structs of field
				FieldStructsBean fieldStructs = attributeMapping.get(fieldName);
				// get field value
				String getMethodName = fieldStructs.getFieldGetMethodName();
				if (getMethodName == null){
					continue;
				}
				Method getMethod = beanClass.getMethod(getMethodName);
				// normal field value
				Object fieldValue = getMethod.invoke(bean);
				
				// get Field instance of field
				Class fieldClass = fieldStructs.getFieldType();
				if(null != fieldValue){
					// Collection field value
					if(Collection.class.isAssignableFrom(fieldClass)){
						fieldValue = generalSendCollection((Collection)fieldValue);
					}
					//Map field value
					else if(Map.class.isAssignableFrom(fieldClass)){
						fieldValue = generalSendMap((Map)fieldValue);
					}
					//Array field value
					else if(fieldClass.isArray()){
						//only convert reference type array , and only 1 dimension Array
						String arrGenericType = fieldClass.getCanonicalName();
						arrGenericType = arrGenericType.substring(0,arrGenericType.length()-2);
						if(!arrGenericType.endsWith("[]") && scanner.isNotJavaClass(arrGenericType)){
							fieldValue = generalSendArray((Object[])fieldValue);
						}
						// more dimension Array ?
					}
					//Reference field value
					else if(scanner.isNotJavaClass(fieldClass)){
	//					Logger.debug(fieldClass);
						log.info("Convert reference field : "+fieldName);
						fieldValue = generalSendObject(fieldValue);
					}
	
					// Map<String, AliasStructsBean> fieldAttributeMapping =
					// aliasStructs.getAttributeMapping();
					// Object fieldValue = aliasStructs.getFieldValue();
					// if(null != fieldAttributeMapping &&
					// fieldAttributeMapping.size() > 0){
					// fieldValue = convertObj2Map(fieldAttributeMapping);
					// }
					// Class fieldType = aliasStructs.getFieldType();
					// if(Collection.class.isAssignableFrom(fieldType)){
	
					// }
					attributeValueMapping.put(fieldStructs.getAliasFieldName(),
							fieldValue);
				}
			}
		} catch (Exception e) {
			throw new JsonConvertException(e);
		}

		return new JSONObject(attributeValueMapping);
	}


	
	private static JSONArray generalSendCollection(Collection oriColl) throws JsonConvertException{
		//list
		JSONArray coll = new JSONArray();
		for (Object obj : oriColl) {
			if(Collection.class.isAssignableFrom(obj.getClass())){
				obj = generalSendCollection((Collection)obj);
			}else if(Map.class.isAssignableFrom(obj.getClass())){
				obj = generalSendMap((Map)obj);
			}else if(obj.getClass().isArray()){
				obj = generalSendArray((Object[])obj);
			}else if(scanner.isNotJavaClass(obj.getClass())){
				obj = generalSendObject(obj);
			}
			coll.put(obj);
		}
		return coll;
	}
	
	private static Map generalSendMap(Map oriMap) throws JsonConvertException{
		Set keySet = oriMap.keySet();
		for (Object key : keySet) {
			Object value = oriMap.get(key);
			if(null != value){
				if(Collection.class.isAssignableFrom(value.getClass())){
					value = generalSendCollection((Collection)value);
				}else if(Map.class.isAssignableFrom(value.getClass())){
					value = generalSendMap((Map)value);
				}else if(value.getClass().isArray()){
					value = generalSendArray((Object[])value);
				}else if(scanner.isNotJavaClass(value.getClass())){
					value = generalSendObject(value);
				}
				oriMap.put(key, value);
			}
		}
		return oriMap;
	}

	private static JSONArray generalSendArray(Object[] oriObjArr)
			throws JsonConvertException {
		if (null != oriObjArr && oriObjArr.length > 0) {
			JSONArray obj = new JSONArray();
			for (int i = 0; i < oriObjArr.length; i++) {
				if(null != oriObjArr[i]){
					Object o = oriObjArr[i];
					if(Collection.class.isAssignableFrom(oriObjArr[i].getClass())){
						o= generalSendCollection((Collection)oriObjArr[i]);
					}else if(Map.class.isAssignableFrom(oriObjArr[i].getClass())){
						o = generalSendMap((Map)oriObjArr[i]);
					}else if(oriObjArr[i].getClass().isArray()){
						o= generalSendArray((Object[])oriObjArr[i]);
					}else if(scanner.isNotJavaClass(oriObjArr[i].getClass())){
						o= generalSendObject(oriObjArr[i]);
					}
					obj.put(o);
				}	
//				obj[i] = generalSendObject(oriObjArr[i]);
			}
			return obj;
		}
		return null;
	}
}
