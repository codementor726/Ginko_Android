package com.ginko.vo;

import java.util.Date;

public class MultimediaMessageVO {
    public MultimediaMessageVO()
    {

    }

    public long getMsgId(){
        return 0;
    };
    public void setMsgId(long id){}

    public String getFile() {
        return "";
    }
    public void setFile(String _file) {}

    public void setFileUrl(String url){}
    public String getFileUrl(){return "";}

    public String getThumnail() {
        return "";
    }
    public void setThumnail(String thumnail) {}

    public Date getSendTime() {
        return null;
    }
    public void setSendTime(Date sentTime) {}

    public String getContent() {
        return "";
    }
    public void setContent(String content) {}

    public int getFrom(){return 0;};
    public void setFrom(int id){}

    public void setMessageType(int _messageType){}
    public int getMessageType(){
        return 0;
    }

    /*get msg type whether its file message or text message*/
    public Integer getMsgType() {
        return 0;
    }
    public void setMsgType(Integer msgType) {}

    public double getLattitude(){return 0.0d;}
    public double getLongitude(){return 0.0d;}
}
