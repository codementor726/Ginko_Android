package com.ginko.setup;

import java.io.Serializable;

public class CountryCodeItem implements Serializable
{
    public static final int ITEM = 0;
    public static final int SECTION = 1;

    public boolean isChecked = false;
    public String strCountryName = "";
    public String strCountryCode = "";
    private String sectionName;

    public int originalIndex = 0;

    private int type;  //section or item

    public CountryCodeItem()
    {}
    public CountryCodeItem(int type) {
        this.type = type;
    }

    public CountryCodeItem createSection(String sectionName) {
        CountryCodeItem countryCodeItem = new CountryCodeItem(SECTION);
        countryCodeItem.setSectionName(sectionName);
        return countryCodeItem;
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

    public boolean isSection() {
        return this.getType() == SECTION;
    }

}