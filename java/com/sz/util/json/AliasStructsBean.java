package com.sz.util.json;

import java.lang.reflect.Field;
import java.util.Map;

public class AliasStructsBean {
	private String originalFieldName;
	private String aliasFieldName;
	private String fieldGetMethodName;
	private String fieldSetMethodName;
	private Class fieldType;
	private Field originalField;
	// save collection generic type and Map key generic type
	private Map<String, AliasStructsBean> referanceTypeAttributeMapping;
	// save Map value generic type
	private Map<String, AliasStructsBean> mapValueGenericMapping;

	public String getOriginalFieldName() {
		return originalFieldName;
	}

	public void setOriginalFieldName(String originalFieldName) {
		this.originalFieldName = originalFieldName;
	}

	public String getAliasFieldName() {
		return aliasFieldName;
	}

	public void setAliasFieldName(String aliasFieldName) {
		this.aliasFieldName = aliasFieldName;
	}

	public String getFieldGetMethodName() {
		return fieldGetMethodName;
	}

	public void setFieldGetMethodName(String fieldGetMethodName) {
		this.fieldGetMethodName = fieldGetMethodName;
	}

	public String getFieldSetMethodName() {
		return fieldSetMethodName;
	}

	public void setFieldSetMethodName(String fieldSetMethodName) {
		this.fieldSetMethodName = fieldSetMethodName;
	}

	public Class getFieldType() {
		return fieldType;
	}

	public void setFieldType(Class fieldType) {
		this.fieldType = fieldType;
	}

	public Field getOriginalField() {
		return originalField;
	}

	public void setOriginalField(Field originalField) {
		this.originalField = originalField;
	}

	public Map<String, AliasStructsBean> getReferanceTypeAttributeMapping() {
		return referanceTypeAttributeMapping;
	}

	public void setReferanceTypeAttributeMapping(
			Map<String, AliasStructsBean> referanceTypeAttributeMapping) {
		this.referanceTypeAttributeMapping = referanceTypeAttributeMapping;
	}

	public Map<String, AliasStructsBean> getMapValueGenericMapping() {
		return mapValueGenericMapping;
	}

	public void setMapValueGenericMapping(
			Map<String, AliasStructsBean> mapValueGenericMapping) {
		this.mapValueGenericMapping = mapValueGenericMapping;
	}

}
