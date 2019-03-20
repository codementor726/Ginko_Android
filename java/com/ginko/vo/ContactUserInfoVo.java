package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;

public class ContactUserInfoVo implements Serializable{
    @Alias("last_name")
    private String lastName;

    @Alias("midle_name")
    private String middleName;

    @Alias("first_name")
    private String firstName;

    public String getLastName(){return lastName==null?"":lastName;}
    public void setLastName(String name){this.lastName = name;}

    public String getMiddleName(){return middleName==null?"":middleName;}
    public void setMiddleName(String name){this.middleName = name;}

    public String getFirstName(){return firstName==null?"":firstName;}
    public void setFirstName(String name){this.firstName = name;}
}
