package com.ginko.activity.contact;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.List;

public class ContactItem implements Serializable{
	public static final int ITEM = 0;
	public static final int SECTION = 1;

	private int type;  //section or item

	private String sectionName;
	private String firstName="";
	private String middleName="";
	private String lastName="";
	private String profileImage = "";

    private boolean isRead = false;
    private boolean isFavorite = false;

    private int contactId;
    private int Id;
    private List<String> phones;
    private List<String> emails;

	private int contactType;
	private int sectionPosition;
	private int listPosition;
	private int sharingStatus;
	private int greyType;

	private String entityName;
	private int nFollowerCount = 0;
	private boolean isFollowed = false;
	private boolean isPending = false;
	private String sharedHomeFields = "";
	private String sharedWorkFields = "";

	public int getContactId() {
		return contactId;
	}

	public void setContactId(int contactId) {
		this.contactId = contactId;
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		this.Id = id;
	}

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    public int getContactType() {
        return contactType;
    }

    public void setContactType(int contactType) {
        this.contactType = contactType;
    }

//	private String profileImage = "http://www.iteye.com/images/user-logo.gif?1324994303";



	public static ContactItem createSection(String sectionName) {
		ContactItem contactItem = new ContactItem(SECTION);
		contactItem.setSectionName(sectionName);
		return contactItem;
	}

	public static ContactItem createItem(String firstName, String lastName) {
		ContactItem contactItem = new ContactItem(ITEM);
		contactItem.setFirstName(firstName);
		contactItem.setLastName(lastName);
		return contactItem;
	}

	public ContactItem(int type) {
		this.type = type;
	}

	public ContactItem(ContactItem item)
	{
		this.type = item.getType();

		this.sectionName = item.getSectionName();
		this.firstName = item.getFirstName();
		this.middleName = item.getMiddleName();
		this.lastName = item.getLastName();
		this.profileImage = item.getProfileImage();

		this.isRead = item.getIsRead();
		this.isFavorite = item.getIsFavorite();

		this.contactId = item.getContactId();
		this.Id = item.getId();
		this.phones = item.getPhones();
		this.emails = item.getEmails();

		this.contactType = item.getContactType();
		this.sectionPosition = item.getSectionPosition();
		this.listPosition = item.getListPosition();
		this.sharingStatus = item.getSharingStatus();
		this.greyType = item.getGreyType();

		this.nFollowerCount = item.getnFollowerCount();
		this.isFollowed = item.getFollowed();
		this.isPending = item.getPending();
		this.sharedHomeFields = item.getSharedHomeFields();
		this.sharedWorkFields = item.getSharedWorkFields();
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public String getFirstName() {
		return firstName==null?"":firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName==null?"":lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getSectionPosition() {
		return sectionPosition;
	}

	public void setSectionPosition(int sectionPosition) {
		this.sectionPosition = sectionPosition;
	}

	public int getListPosition() {
		return listPosition;
	}

	public void setListPosition(int listPosition) {
		this.listPosition = listPosition;
	}

	public boolean isSection() {
		return this.getType() == SECTION;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public void setIsFavorite(boolean _isFavorite){this.isFavorite = _isFavorite;}
	public boolean getIsFavorite(){return this.isFavorite;}

	public String getMiddleName() {
		return middleName==null?"":middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

    public String getFullName(){
        String name = this.getFirstName();
        if (StringUtils.isNotBlank(this.getMiddleName())) {
            name += " " + this.getMiddleName();
        }
        if (StringUtils.isNotBlank(this.getLastName())) {
            name += " " + this.getLastName();
        }
        return name.trim();
    }

    public void setIsRead(boolean _isRead){this.isRead = _isRead;}
    public boolean getIsRead(){return this.isRead;}

	public void setSharingStatus(int sharing_status){this.sharingStatus = sharing_status;}
	public int getSharingStatus(){return  this.sharingStatus;}

	public void setGreyType(int type){this.greyType = type;}
	public int getGreyType(){return this.greyType;}

	public void setnFollowerCount(int nFollowerCount) {this.nFollowerCount = nFollowerCount;}
	public int getnFollowerCount(){return this.nFollowerCount;}

	public void setFollowed(boolean _isFollowed){this.isFollowed = _isFollowed;}
	public boolean getFollowed(){return this.isFollowed;}

	public void setPending(boolean _isPending){this.isPending = _isPending;}
	public boolean getPending(){return this.isPending;}

	public void setSharedHomeFields(String sharedHomeFields) {this.sharedHomeFields = sharedHomeFields;}
	public String getSharedHomeFields(){return this.sharedHomeFields;}

	public void setSharedWorkFields(String sharedWorkFields) {this.sharedWorkFields = sharedWorkFields;}
	public String getSharedWorkFields(){return this.sharedWorkFields;}

	public void setEntityName(String entityName) {this.entityName = entityName;}
	public String getEntityName(){return this.entityName;}
}
