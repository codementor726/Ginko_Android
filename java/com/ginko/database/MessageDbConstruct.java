package com.ginko.database;

import com.ginko.vo.ImMessageVO;

import org.json.JSONObject;

public class MessageDbConstruct {
    public long msgId = 0;
    public long boardId = 0;
    public String strMsgTime = "";
    public String msgContent = "";
    public String mediaFilePath = "";

    public MessageDbConstruct()
    {

    }
    public MessageDbConstruct(long _boardId , ImMessageVO msg , String utcMessageTime)
    {
        this.boardId = _boardId;
        this.msgId = msg.getMsgId();
        this.strMsgTime = utcMessageTime;
        if(msg.getContent() != null)
        {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("content" , msg.getContent());
                jsonObject.put("send_time" , utcMessageTime);
                jsonObject.put("msg_id", String.valueOf(msg.getMsgId()));
                jsonObject.put("msgType" , msg.getMsgType());
                jsonObject.put("send_from" , String.valueOf(msg.getFrom()));
                jsonObject.put("is_read" , "true");
                jsonObject.put("is_new" , "false");

                this.msgContent = jsonObject.toString();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if(msg.getFile() != null && !msg.getFile().equals(""))
        {
            this.mediaFilePath = msg.getFile();
        }
    }

}
