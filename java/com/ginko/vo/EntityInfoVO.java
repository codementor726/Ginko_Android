package com.ginko.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sz.util.json.Alias;

public class  EntityInfoVO implements Serializable, Comparable<EntityInfoVO> {
	@Alias("info_id")
	private Integer id;

	// @XmlTransient
	private Integer sequence;

	private String latitude;

	private String longitude;

	@Alias("address_confirmed")
	private boolean addressConfirmed;

	@Alias("fields")
	private List<EntityInfoDetailVO> entityInfoDetails;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<EntityInfoDetailVO> getEntityInfoDetails() {
        if(entityInfoDetails == null)
            entityInfoDetails = new ArrayList<EntityInfoDetailVO>();
		return entityInfoDetails;
	}

	public void setEntityInfoDetails(List<EntityInfoDetailVO> entityInfoDetails) {
		this.entityInfoDetails = entityInfoDetails;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	@Override
	public int compareTo(EntityInfoVO o) {
		if (this.getSequence() == null) {

			return o.getSequence() == null ? 0 : -1;
		} else {
			return o.getSequence() == null ? 1 : this.getSequence()
					- o.getSequence();
		}

	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public boolean isAddressConfirmed() {
		return addressConfirmed;
	}

	public void setAddressConfirmed(boolean addressConfirmed) {
		this.addressConfirmed = addressConfirmed;
	}

    public boolean getAbbr() {
        EntityInfoDetailVO f = getFieldByName("Abbr");
        if (f != null) {
            return f.getValue().equalsIgnoreCase("1");
        }
        return false;
    }
    public void setAbbr(boolean bAbbr) {
        EntityInfoDetailVO f = getFieldByName("Abbr");
        if (f != null) {
            f.setValue( bAbbr?"1":"0");
        }
        else
        {
            f = new EntityInfoDetailVO();
            f.setFieldName("Abbr");
            f.setType("abbr");
            f.setValue(bAbbr?"1":"0");//default abbr value is 0
            //f.setColor(""); //default color
            //f.setFont(""); //default font and font size
            f.setPosition("");
            getEntityInfoDetails().add(f);
        }
    }

    private EntityInfoDetailVO getFieldByName(String fieldName) {
        EntityInfoDetailVO f = null;
        for (EntityInfoDetailVO field : entityInfoDetails) {
            if (field.getFieldName().equalsIgnoreCase(fieldName)) {
                f =  field;
                break;
            }
        }
        return f;
    }
}
