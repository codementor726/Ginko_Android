package com.ginko.activity.sync;

import com.ginko.context.ConstValues;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SyncGreyContactItem {

    private int sync_contactId = 0;
    private boolean isSelected = false;
    private boolean isVisible = true;
    private String contactName = "";
    private String contactEmail = "";
    private List<String> contactPhoneNumbers = null;
    private int greyContactType = ConstValues.GREY_TYPE_NONE;
    private String photoUrl = "";
    private String address = "";
    private String strBirthday = "";
    private String strWebUrl = "";

    private String jsonString = "";

    public SyncGreyContactItem()
    {
        this.isVisible = true;
        this.isSelected = false;
    }

    public SyncGreyContactItem(SyncGreyContactItem item)
    {
        setSelection(getSelection());
        setVisibility(item.getVisibility());
        setGreyContactType(item.getGreyContactType());
        setGreyContactName(item.getGreyContactName());
        setGreyContactEmail(item.getGreyContactEmail());
        setSyncContactId(item.getSyncContactId());
        setContactPhoneNumbers(item.getContactPhoneNumbers());
        setPhotoUrl(item.getPhotoUrl());
        setAddress(item.getAddress());
        setBirthday(item.getBirthday());
        setWebsite(item.getWebsite());
        setJsonString(item.getJsonString());

    }

    public void setSelection(boolean selected){this.isSelected = selected;}
    public boolean getSelection(){return this.isSelected;}

    public void setVisibility(boolean visible){this.isVisible = visible;}
    public boolean getVisibility(){return this.isVisible;}

    public void setGreyContactName(String name){this.contactName = name;}
    public String getGreyContactName(){return this.contactName;}

    public void setGreyContactEmail(String email){this.contactEmail = email;}
    public String getGreyContactEmail(){return this.contactEmail;}

    public void setGreyContactType(int type){this.greyContactType = type;}
    public int getGreyContactType(){return this.greyContactType;}

    public void setSyncContactId(int id){this.sync_contactId = id;}
    public int getSyncContactId(){return this.sync_contactId;}

    public void addPhoneNumber(String number){
        if(this.contactPhoneNumbers==null)
            this.contactPhoneNumbers = new ArrayList<String>();
        contactPhoneNumbers.add(number);
    }

    public void setContactPhoneNumbers(List<String> numbers){this.contactPhoneNumbers = numbers;}
    public List<String> getContactPhoneNumbers(){return this.contactPhoneNumbers;}

    public void setPhotoUrl(String url){this.photoUrl = url;}
    public String getPhotoUrl(){return this.photoUrl;}

    public void setAddress(String addr){this.address = addr;}
    public String getAddress(){return this.address;}

    public void setBirthday(String birthdate){this.strBirthday = birthdate;}
    public String getBirthday(){return this.strBirthday;}

    public void setWebsite(String weburl){this.strWebUrl = weburl;}
    public String getWebsite(){return this.strWebUrl;}

    public void setJsonString(String jsonStr){this.jsonString = jsonStr;}
    public String getJsonString(){return this.jsonString;}
}
