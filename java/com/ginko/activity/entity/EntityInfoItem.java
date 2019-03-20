package com.ginko.activity.entity;

import android.text.InputType;
import android.widget.EditText;

public class EntityInfoItem
{
    private String strInfoValue;
    private String strFieldType;
    private String strInfoName = "Phone";
    private int nItemInputType;
    private boolean isSelected = false;
    private boolean isVisible = true;
    private boolean isDefaultField = false;
    private boolean isAddressConfirmed = false;

    private boolean isAddressSkipped = false;

    private Double lat = null , lng = null;

    public EntityInfoItem(String infoName , String infoTypeFiled , String infoValue , boolean _isVisible)
    {
        this.strInfoValue = infoValue;
        this.strFieldType = infoTypeFiled;
        this.strInfoName = infoName;
        this.nItemInputType = InputType.TYPE_CLASS_TEXT ;
        this.isSelected = false;
        this.isVisible = _isVisible;
        this.isAddressConfirmed = false;

        if(this.strInfoName.toLowerCase().contains("phone") || this.strInfoName.contains("mobile") || this.strInfoName.toLowerCase().contains("fax")) {
//            this.nItemInputType = InputType.TYPE_CLASS_PHONE;
            this.nItemInputType = InputType.TYPE_CLASS_TEXT;
        }
        else if(this.strInfoName.toLowerCase().contains("email"))
            this.nItemInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        else if(this.strInfoName.toLowerCase().contains("website"))
            this.nItemInputType = InputType.TYPE_TEXT_VARIATION_URI;
        else
            this.nItemInputType = InputType.TYPE_CLASS_TEXT;

        //name and keysearch fileds are default field that can't be deleted
        if(this.strInfoName.equalsIgnoreCase("name") || this.strInfoName.equalsIgnoreCase("keysearch"))
            this.isDefaultField = true;
        else
            this.isDefaultField = false;
    }
    public void setFieldValue(String infoValue)
    {
        this.strInfoValue = infoValue;
    }
    public String getFieldValue(){return  this.strInfoValue;}

    public void setFieldType(String fieldType)
    {
        this.strFieldType = fieldType;
    }
    public String getFieldType(){return  this.strFieldType;}

    public void setFieldName(String name)
    {
        this.strInfoName = name;
    }
    public String getFieldName(){return  this.strInfoName;}

    public void setVisibility(boolean visible)
    {
        this.isVisible = visible;
    }
    public boolean getVisibility(){return  this.isVisible;}

    public int getTextInputType(){return this.nItemInputType;}

    public void setIsSelected(boolean selected){this.isSelected = selected;}
    public boolean getIsSelected(){return this.isSelected;}

    public boolean isDefaultField(){return  this.isDefaultField;}

    public void setIsAddressConfirmed(boolean bconfirmed){this.isAddressConfirmed = bconfirmed;}
    public boolean isAddressConfirmed(){return this.isAddressConfirmed;}

    public void setAddressSkipped(boolean isSkipped){this.isAddressSkipped = isSkipped;}
    public boolean isAddressSkipped(){return this.isAddressSkipped;}

    public void setLatitude(Double d){this.lat = d;}
    public Double getLatitude(){return this.lat;}

    public void setLongitude(Double d){this.lng = d;}
    public Double getLongitude(){return this.lng;}

}