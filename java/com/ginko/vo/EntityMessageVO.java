package com.ginko.vo;

import com.ginko.context.ConstValues;
import com.sz.util.json.Alias;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;


public class EntityMessageVO extends MultimediaMessageVO implements Serializable {
    public static String MSG_TEXT="text";
    public static String MSG_VOICE="voice";
    public static String MSG_VIDEO="video";
    public static String MSG_PHOTO="photo";
    public static String MSG_LOCATION="location";

    public static final int MSG_TYPE_TEXT = 0;
    public static final int MSG_TYPE_VOICE = 1;
    public static final int MSG_TYPE_VIDEO = 2;
    public static final int MSG_TYPE_PHOTO = 3;
    public static final int MSG_TYPE_LOCATION = 4;

    @Alias(ignoreSet = true , ignoreGet = true)
    public int messageType;/*
                                    MSG_TYPE_TEXT
                                    MSG_TYPE_VOICE
                                    MSG_TYPE_VIDEO
                                    MSG_TYPE_PHOTO
                                    MSG_TYPE_LOCATION
                                    */
    @Alias(ignoreSet = true , ignoreGet = true)
    public Integer msgType;/*
	                            1: Text message
	                            2: File Message

	                               */
	@Alias( "msg_id")
	private long msgId;
	@Alias( "content")
	private String content;
	@Alias( "sent_time")
	private Date sendTime;

    @Alias( ignoreGet = true,ignoreSet = true)
    public String file;

    @Alias(ignoreGet = true,ignoreSet = true)
    private String fileUrl;

    @Alias(ignoreSet = true , ignoreGet = true)
    private String thumbUrl = "";

    @Alias(ignoreGet = true ,ignoreSet = true)
    public double lattitude;

    @Alias(ignoreGet = true ,ignoreSet = true)
    public double longitude;

    @Override
    public long getMsgId() {
		return msgId;
	}
    @Override
	public void setMsgId(long id) {
		this.msgId = id;
	}

    @Alias(ignoreGet = true,ignoreSet = true)
    public boolean isSelected;

    @Alias(ignoreGet = true,ignoreSet = true)
    public boolean isTimeShowable;

    @Alias(ignoreGet = true,ignoreSet = true)
    public boolean isUserPhotoShowable;

    @Alias(ignoreGet = true,ignoreSet = true)
    public boolean isCommingMsg = false;

    @Alias(ignoreGet = true,ignoreSet = true)
    public String strProfilePhoto;

    @Alias(ignoreGet = true,ignoreSet = true)
    public String strEntityName;

    @Alias(ignoreGet = true,ignoreSet = true)
    public int entityId;

    @Alias(ignoreGet = true,ignoreSet = true)
    public boolean isPending = false;

    public EntityMessageVO()
    {
        this.isSelected = false;

        this.file = "";
        this.thumbUrl = "";
        this.fileUrl = "";
        this.isPending = false;
    }
    public EntityMessageVO(int _entityId , String profilePhoto , String entityName)
    {
        this.isSelected = false;

        this.file = "";
        this.thumbUrl = "";
        this.fileUrl = "";

        this.entityId = _entityId;
        this.strProfilePhoto = profilePhoto;
        this.strEntityName = entityName;
    }

    @Override
	public String getContent() {
		return content;
	}
    @Override
	public void setContent(String content) {
		this.content = content;
        if(this.content.startsWith("{") && this.content.endsWith("}")) {
            try {
                JSONObject msgObj = new JSONObject(content);
                if (msgObj.has("file_type")) {
                    String fileType = msgObj.optString("file_type" , "photo");
                    String strUrl = msgObj.optString("url" , "");
                    setFileUrl(strUrl);
                    if(fileType.equals(MSG_PHOTO))
                        setMessageType(MSG_TYPE_PHOTO);
                    else if(fileType.equals(MSG_VIDEO))
                    {
                        setMessageType(MSG_TYPE_VIDEO);
                        String strThumbUrl = msgObj.optString("thumnail_url" , "");
                        setThumnail(strThumbUrl);
                    }
                    else
                        setMessageType(MSG_TYPE_VOICE);

                    setMsgType(2);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                setMessageType(MSG_TYPE_TEXT);
            }
        }
        else//location or text message
        {
            setMsgType(1);
            if(this.content.startsWith(ConstValues.IM_LOCATION_PREFIX))
            {
                setMessageType(MSG_TYPE_LOCATION);
                String str1 = content.substring(ConstValues.IM_LOCATION_PREFIX.length() , content.length());
                int seperatorIndex = str1.indexOf(',');
                if(seperatorIndex>0) {
                    try {
                        String strLat = str1.substring(0, seperatorIndex);
                        this.lattitude = Double.valueOf(strLat);
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.lattitude = 0.0;
                    }
                    try {
                        String strLng = str1.substring(seperatorIndex+1);
                        this.longitude = Double.valueOf(strLng);
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.longitude = 0.0;
                    }
                }
                else
                {
                    this.lattitude = 0.0;
                    this.longitude = 0.0;
                }
            }
            else
            {
                setMessageType(MSG_TYPE_TEXT);
            }
        }
    }

    @Override
    public void setMsgType(Integer msgType){this.msgType = msgType.intValue();}

    @Override
    public Integer getMsgType(){return new Integer(this.msgType);}

    @Override
    public void setMessageType(int _messageType)
    {
        this.messageType = _messageType;
    }
    @Override
    public int getMessageType(){
        return this.messageType;
    }

    @Override
	public Date getSendTime() {
		return sendTime;
	}
    @Override
	public void setSendTime(Date sentTime) {
		this.sendTime = sentTime;
	}

    @Override
    public double getLattitude(){return this.lattitude;}
    @Override
    public double getLongitude(){return this.longitude;}

    @Override
    public String getFile() {
        return file;
    }
    @Override
    public void setFile(String _file) {
        this.file = _file;
    }

    @Override
    public void setFileUrl(String url){this.fileUrl = url;}
    @Override
    public String getFileUrl(){return this.fileUrl;}

    @Override
    public String getThumnail() {
        return thumbUrl;
    }
    @Override
    public void setThumnail(String thumnail) {
        this.thumbUrl = thumnail;
    }

    public boolean isSelected(){return isSelected;}
    public void setIsSelected(boolean selected){this.isSelected = selected;}

    public boolean isTimeShowable(){return this.isTimeShowable;}
    public void setIsTimeShowable(boolean _bTmeShow){this.isTimeShowable = _bTmeShow;}

    public boolean isUserPhotoShowable(){return this.isUserPhotoShowable;}
    public void setIsUserPhotoShowable(boolean _bPhotoShow){this.isUserPhotoShowable = _bPhotoShow;}

    public boolean isCommingMsg(){
        return false;
        //return this.isCommingMsg;
    }
    public void setIsCommingMsg(boolean _isCommingMsg){this.isCommingMsg = _isCommingMsg;}
}
