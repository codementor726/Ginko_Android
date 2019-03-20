package com.ginko.activity.directory;

/**
 * Created by YongJong on 01/14/17.
 */
public class DirectoryInviteContactItem {
    private String firstName = "";
    private String middleName = "";
    private String lastName = "";
    private String fullName = "";
    private int contactId = 0;
    private String photoUrl = "";

    private boolean isSelected = false;
    private boolean isVisible = true;

    public DirectoryInviteContactItem()
    {
        this.isSelected = false;
        this.isVisible = true;
    }

    public void setFirstName(String _firstName){this.firstName = _firstName;}
    public String getFirstName(){return this.firstName;}

    public void setMiddleName(String _middleName){this.middleName = _middleName;}
    public String getMiddleName(){return this.middleName;}

    public void setLastName(String _lastName){this.lastName = _lastName;}
    public String getLastName(){return this.lastName;}

    public void setPhotoUrl(String url){this.photoUrl = url;}
    public String getPhotoUrl(){return this.photoUrl;}

    public void setContactId(int id){this.contactId = id;}
    public int getContactId(){return this.contactId;}

    public void setSelected(boolean selected){this.isSelected = selected;}
    public boolean isSelected(){return this.isSelected;}

    public void setVisibility(boolean visible){this.isVisible = visible;}
    public boolean getVisibility(){return this.isVisible;}

    public void setFullName(String fullname){this.fullName = fullname;}
    public String getFullName(){return this.fullName;}
}
