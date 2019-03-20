package com.ginko.vo;

import org.apache.commons.lang.StringUtils;

import com.sz.util.json.Alias;

public class GreyContactFieldVO {

	private Integer id;

	@Alias("field_name")
	private String name;

	@Alias("field_value")
	private String value;

	@Alias("field_type")
	private String type;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String fieldName) {
		this.name = fieldName;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String fieldType) {
		this.type = fieldType;
	}

	@Override
	public boolean equals(Object f) {
		if (!(f instanceof GreyContactFieldVO)) {
			return false;
		}
		GreyContactFieldVO other = (GreyContactFieldVO) f;
		if (StringUtils.equalsIgnoreCase(this.getName(), other.getName())
				&& StringUtils
						.equalsIgnoreCase(this.getType(), other.getType())
				&& StringUtils.equalsIgnoreCase(this.getValue(),
						other.getValue())) {
			return true;
		}
		return false;
	}

}
