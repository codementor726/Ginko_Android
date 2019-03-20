package com.sz.util.json;

import java.lang.reflect.Field;
import java.util.Map;

public class FieldStructsBean {
	private String originalFieldName;
	private String aliasFieldName;
	private String fieldGetMethodName;
	private String fieldSetMethodName;
	private Class fieldType;
	private Field originalField;
	
	private Alias alias;

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

	public Alias getAlias() {
		return alias;
	}

	public void setAlias(Alias alias) {
		this.alias = alias;
	}

}
