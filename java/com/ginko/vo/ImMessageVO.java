package com.ginko.vo;

import com.ginko.common.RuntimeContext;
import com.ginko.context.ConstValues;
import com.sz.util.json.Alias;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class ImMessageVO extends MultimediaMessageVO implements Serializable{
    public static String MSG_TEXT="text";
    public static String MSG_VOICE="voice";
    public static String MSG_VIDEO="video";
    public static String MSG_PHOTO="photo";
    public static String MSG_LOCATION="location";
    public static String MSG_VIDEOCALL="videoCall";
    public static String MSG_AUDIOCALL="audioCall";

    public static final int MSG_TYPE_TEXT = 0;
    public static final int MSG_TYPE_VOICE = 1;
    public static final int MSG_TYPE_VIDEO = 2;
    public static final int MSG_TYPE_PHOTO = 3;
    public static final int MSG_TYPE_LOCATION = 4;
    public static final int MSG_TYPE_VIDEOCALL = 5;
    public static final int MSG_TYPE_AUDIOCALL = 6;

    public int messageType;/*
                                    MSG_TYPE_TEXT
                                    MSG_TYPE_VOICE
                                    MSG_TYPE_VIDEO
                                    MSG_TYPE_PHOTO
                                    MSG_TYPE_LOCATION
                                    */

    public String content;

	@Alias( "msg_id")
	public long msgId;

	@Alias( "send_from")
	public int from;

	@Alias( "send_time")
	public Date sendTime;

    @Alias(ignoreGet = true,ignoreSet = true)
    public Date utcSendTime;

	public Integer msgType;/*
	                            1: Text message
	                            2: File Message

	                               */
    @Alias(ignoreGet = true,ignoreSet = true)
    private String fileUrl;

	@Alias( "is_read")
	public boolean read;

	public boolean isNew;

    @Alias(ignoreGet = true,ignoreSet = true)
    public boolean isSelected;

    @Alias(ignoreGet = true,ignoreSet = true)
    public boolean isTimeShowable;

    @Alias(ignoreGet = true,ignoreSet = true)
    public boolean isUserPhotoShowable;

    @Alias(ignoreGet = true,ignoreSet = true)
    public boolean isCommingMsg = false;

    @Alias(ignoreGet = true,ignoreSet = true)
    public boolean isPending = false;

    //file and thumnail is only for photo,voice, video; it's the local path of file
    @Alias( ignoreGet = true,ignoreSet = true)
    public String file;

    @Alias( ignoreGet = true,ignoreSet = true)
    public String thumnail;

    @Alias(ignoreGet = true ,ignoreSet = true)
    public double lattitude;

    @Alias(ignoreGet = true ,ignoreSet = true)
    public double longitude;


    public ImMessageVO()
    {
        this.isSelected = false;
        this.isNew = false;
        this.read = false;
        this.isTimeShowable = false;
        this.isUserPhotoShowable = false;

        this.file = "";
        this.thumnail = "";
        this.fileUrl = "";
    }

    @Override
	public String getContent() {
		return content;
	}
    @Override
	public void setContent(String content) {
		this.content = content;

        //if (getMsgType() != null && getMsgType() == 1) {
        if(content.startsWith(ConstValues.IM_LOCATION_PREFIX)){
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
            return;
        }
        //}

        try {
            JSONObject json = new JSONObject(content);
            final String fileType = json.optString("file_type");

            if (fileType.equals(MSG_PHOTO))
            {
                setMessageType(MSG_TYPE_PHOTO);
                setFileUrl(json.optString("url"));
                return;
            }
            if(fileType.equals(MSG_VIDEO))
            {
                setMessageType(MSG_TYPE_VIDEO);
                setThumnail(json.optString("thumnail_url"));
                setFileUrl(json.optString("url"));
                return;
            }
            if(fileType.equals(MSG_VOICE)) {
                setMessageType(MSG_TYPE_VOICE);
                setFileUrl(json.optString("url"));
                return;
            }
        } catch (Exception e) {
            //It's a text message
        }

        setMessageType(MSG_TYPE_TEXT);
	}

    @Override
	public long getMsgId() {
		return msgId;
	}
    @Override
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}

    @Override
	public int getFrom() {
		return from;
	}
    @Override
	public void setFrom(int from) {
		this.from = from;

        setIsCommingMsg(!RuntimeContext.isLoginUser(from));
	}

	public Date getSendTime() {
		return sendTime;
	}

    public Date getUtcTime()
    {
        return this.utcSendTime;
    }


    @Override
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

    @Override
    public void setMessageType(int _messageType)
    {
        this.messageType = _messageType;
    }
    @Override
    public int getMessageType(){
        return this.messageType;
    }

    /*get msg type whether its file message or text message*/
    @Override
	public Integer getMsgType() {
		return msgType;
	}
    @Override
	public void setMsgType(Integer msgType) {
		this.msgType = msgType;
	}


    @Override
    public double getLattitude(){return this.lattitude;}
    @Override
    public double getLongitude(){return this.longitude;}


	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

    public boolean isSelected(){return isSelected;}

    public void setIsSelected(boolean selected){this.isSelected = selected;}

    public boolean isTimeShowable(){return this.isTimeShowable;}
    public void setIsTimeShowable(boolean _bTmeShow){this.isTimeShowable = _bTmeShow;}

    public boolean isUserPhotoShowable(){return this.isUserPhotoShowable;}
    public void setIsUserPhotoShowable(boolean _bPhotoShow){this.isUserPhotoShowable = _bPhotoShow;}

	@Alias("is_new")
	public boolean isNew(){
		return this.isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	
	public boolean equals(Object obj){
		if (!(obj instanceof ImMessageVO)){
			return false;
		}
		ImMessageVO other = (ImMessageVO)obj;
		return other.getMsgId() == this.getMsgId();
	}

    @Override
    public String getThumnail() {
        return thumnail;
    }
    @Override
    public void setThumnail(String thumnail) {
        this.thumnail = thumnail;
    }

    @Override
    public String getFile() {
        return file;
    }
    @Override
    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public void setFileUrl(String url){this.fileUrl = url;}
    @Override
    public String getFileUrl(){return this.fileUrl;}

    public boolean isVoice(){
        return getMessageType()==MSG_TYPE_VOICE?true:false;
    }
    public boolean isPhoto(){
        return getMessageType()==MSG_TYPE_PHOTO?true:false;
    }
    public boolean isVideo(){
        return getMessageType()==MSG_TYPE_VIDEO?true:false;
    }
    public boolean isLocation(){
        return getMessageType()==MSG_TYPE_LOCATION?true:false;
    }
    public boolean isText(){
        return getMessageType()==MSG_TYPE_TEXT?true:false;
    }

    @Alias( ignoreGet = true,ignoreSet = true)
    private boolean isLoading;
    public boolean isLoading(){
        return this.isLoading;
    }
    public void loadCompleted() {
            isLoading = false;
    }

    public boolean isCommingMsg(){return this.isCommingMsg;}
    public void setIsCommingMsg(boolean _isCommingMsg){this.isCommingMsg = _isCommingMsg;}
}
