package com.ginko.database;

import com.ginko.activity.contact.ContactItem;
import com.ginko.vo.EntityMessageVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ContactStruct implements Serializable {

    private int Id = 0;
    private int ContactOrEntityId = 0;
    private int ContactType = 1;
    private String strFirstName = "";
    private String strMiddleName = "";
    private String strLastName = "";
    private String JsonValue = "";

    private ContactItem contactItem;

    public ContactStruct()
    {

    }
    public ContactStruct(int id , int contactOrEntityId , int contactType , String firstName , String middleName ,  String lastName ,String jsonValue)
    {
        this.Id = id;
        this.ContactOrEntityId = contactOrEntityId;
        this.ContactType = contactType;
        this.strFirstName = firstName;
        this.strMiddleName = middleName;
        this.strLastName = lastName;
        this.JsonValue = jsonValue;
    }

    public void update(ContactStruct st)
    {
        this.Id = st.Id;
        this.ContactOrEntityId = st.ContactOrEntityId;
        this.ContactType = st.ContactType;
        this.strFirstName = "";
        this.strMiddleName = "";
        this.strLastName = "";
        this.JsonValue = st.JsonValue;
    }

    public boolean isPurpleContact(){
        return  ContactType == 1;
    }
    public boolean isGreyContact(){
        return  ContactType == 2;
    }
    public boolean isEntity(){
        return  ContactType == 3;
    }

    public void setId(int id){this.Id = id;}
    public int getId(){return this.Id;}

    public void setContactOrEntityId(int contactOrEntityId){this.ContactOrEntityId = contactOrEntityId;}
    public int getContactOrEntityId(){return this.ContactOrEntityId;}

    public void setContactType(int contactType){this.ContactType = contactType;}
    public int getContactType(){return this.ContactType;}

    public void setFirstName(String firstName){this.strFirstName = firstName;}
    public String getFirstName(){return  this.strFirstName;}

    public void setMiddleName(String middleName){this.strMiddleName = middleName;}
    public String getMiddleName(){return  this.strMiddleName;}

    public void setLastName(String lastName){this.strLastName = lastName;}
    public String getLastName(){return  this.strLastName;}

    public void setJsonValue(String jsonValue){
        this.JsonValue = jsonValue;
        if(jsonValue!=null && jsonValue.compareTo("") != 0)
        {
            try {
                JSONObject jsonObject = new JSONObject(jsonValue);
                this.contactItem = JsonConverter.json2Object(
                        jsonObject, (Class<ContactItem>) ContactItem.class);
            } catch (JSONException e) {
                e.printStackTrace();
                this.contactItem = null;
            } catch (JsonConvertException e) {
                e.printStackTrace();
                this.contactItem = null;
            }
        }
        else
        {
            this.contactItem = null;
        }

    }
    public String getJsonValue(){return  this.JsonValue;}

    public void setContactItem(ContactItem item){this.contactItem = item;}
    public ContactItem getContactItem(){return  this.contactItem;}
}
