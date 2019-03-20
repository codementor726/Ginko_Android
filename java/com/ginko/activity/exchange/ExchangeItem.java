package com.ginko.activity.exchange;


import com.ginko.common.MyDataUtils;

import java.util.Date;

public class ExchangeItem
{
    public static final int EXCHANGE_PENDING = 0;
    public static final int EXCHANGE_INVITE = 1;
    public static final int EXCHANGE_REQUESTS = 2;

    public int contactType = 1;
    public String contactName = "";
    public int contactId = 0;
    public int entityId = 0;
    public int nFollowerCount = 0;
    public int id = 0;
    public String photoUrl = "";
    public String email = "";
    public String shared_home_fids = "";
    public String shared_work_fids = "";

    public int sharingStatus = 0;

    private Date time;

    private int exchangeStatus = EXCHANGE_PENDING;

    private boolean isVisible = true;
    private boolean isSelected = false;

    public boolean isInGinko = false;

    public ExchangeItem(int _exchangeStatus)
    {
        this.id = 0;
        this.isSelected = false;
        this.isVisible = true;
        this.exchangeStatus = _exchangeStatus;
        this.contactType = 1;
        this.isInGinko = false;
    }

    public boolean isSelected(){return isSelected;}
    public void setSelection(boolean _isSelected){this.isSelected = _isSelected;}

    public void setVisibility(boolean _visible){this.isVisible = _visible;}
    public boolean getVisibility(){return  this.isVisible;}

    public void setTime(Date dateTime){this.time = dateTime;}
    public void setTime(String dateTime){
        this.time = MyDataUtils.parse(dateTime);
        //this.time = MyDataUtils.parseOnlyDate(dateTime);
    }

    public Date getTime(){return this.time;}

    public boolean isPendingExchange(){return this.exchangeStatus==EXCHANGE_PENDING?true:false;}
    public boolean isInviteExchange(){return this.exchangeStatus==EXCHANGE_INVITE?true:false;}
    public boolean isRequestsExchange(){return this.exchangeStatus==EXCHANGE_REQUESTS?true:false;}

}