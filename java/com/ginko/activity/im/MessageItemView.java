package com.ginko.activity.im;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.ginko.common.RuntimeContext;
import com.ginko.ginko.MyApp;
import com.ginko.vo.EventUser;
import com.ginko.vo.ImBoardMemeberVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.ImContactVO;
import com.ginko.vo.MultimediaMessageVO;
import com.ginko.vo.VideoMemberVO;

import java.util.HashMap;

public class MessageItemView extends LinearLayout {

    protected Context mContext;
    protected LayoutInflater inflater;

    protected ImBoardVO board;
    protected MultimediaMessageVO item;
    protected HashMap<Integer, ImBoardMemeberVO> entireMapList;

    protected boolean isCommingMsg = false;

    protected Handler mHandler;

    public MessageItemView(Context context) {
        super(context);
        this.mContext = context;

        mHandler = new Handler(mContext.getMainLooper());
    }
    public MessageItemView(Context context , MultimediaMessageVO messageItem)
    {
        super(context);
        this.mContext = context;
        this.item = messageItem;

        mHandler = new Handler(mContext.getMainLooper());

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public MessageItemView(Context context ,ImBoardVO boardInfo , MultimediaMessageVO messageItem, HashMap<Integer, ImBoardMemeberVO> entireMemberList)
    {
        super(context);
        this.mContext = context;
        this.item = messageItem;
        this.board = boardInfo;
        this.entireMapList = new HashMap<Integer, ImBoardMemeberVO>();
        this.entireMapList.clear();
        if (entireMemberList != null)
            this.entireMapList.putAll(entireMemberList);

        mHandler = new Handler(mContext.getMainLooper());

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public void updateBoard(ImBoardVO boardInfo, HashMap<Integer, ImBoardMemeberVO> entireMemberList)
    {
        this.entireMapList = entireMemberList;
        this.board = boardInfo;
    }

    public MultimediaMessageVO getMessageItem()
    {
        return this.item;
    }

    public void setMessageItem(MultimediaMessageVO messageItem)
    {
        this.item = messageItem;
        this.isCommingMsg = isCommingMsg(messageItem);
    }

    public void getUIObjects(boolean isSelectable)
    {

    }

    public void refreshView(boolean isSelectable)
    {
        getUIObjects(isSelectable);
    }

    public String getContactShortName(int userId) {
        if (this.board == null)
            return "";
        if (this.board.isGroup())
        {
            if (entireMapList != null && entireMapList.containsKey(userId))
            {
                ImBoardMemeberVO memberInfo = entireMapList.get(userId);
                String contactName = memberInfo.getName();
                if(contactName.equals("")) return "";
                String strShortContactName = "";
                if(contactName.contains(" ")) {
                    strShortContactName += contactName.charAt(0);
                    for(int index = 1; index<contactName.length()-1;index++)
                    {
                        if(contactName.charAt(index) == ' ' && contactName.charAt(index+1) != ' '
                                && strShortContactName.length()<3)
                        {
                            strShortContactName += contactName.charAt(index+1);
                        }
                    }
                }
                else
                {
                    if(contactName.length()>6)
                        strShortContactName = contactName.substring(0, 6);
                    else
                        strShortContactName = contactName;
                }
                return strShortContactName;
            }

        }
        else if (board.isVideoChat() == true)
        {
            for (int i = 0; i < MyApp.getInstance().g_videoMemberList.size(); i++) {
                VideoMemberVO member = MyApp.getInstance().g_videoMemberList.get(i);
                if (member.getUserId().equals(String.valueOf(userId))) {
                    String contactName = member.getName();
                    if(contactName.equals("")) return "";
                    String strShortContactName = "";
                    if(contactName.contains(" ")) {
                        strShortContactName += contactName.charAt(0);
                        for(int index = 1; index<contactName.length()-1;index++)
                        {
                            if(contactName.charAt(index) == ' ' && contactName.charAt(index+1) != ' '
                                    && strShortContactName.length()<3)
                            {
                                strShortContactName += contactName.charAt(index+1);
                            }
                        }
                    }
                    else
                    {
                        if(contactName.length()>6)
                            strShortContactName = contactName.substring(0, 6);
                        else
                            strShortContactName = contactName;
                    }
                    return strShortContactName;
                }
            }
        }
        else
        {
            for (int i = 0; i < this.board.getMembers().size(); i++) {
                ImBoardMemeberVO member = this.board.getMembers().get(i);
                if (member.getUser().getUserId() == userId) {
                    String contactName = member.getUser().getFullName();
                    if(contactName.equals("")) return "";
                    String strShortContactName = "";
                    if(contactName.contains(" ")) {
                        strShortContactName += contactName.charAt(0);
                        for(int index = 1; index<contactName.length()-1;index++)
                        {
                            if(contactName.charAt(index) == ' ' && contactName.charAt(index+1) != ' '
                                    && strShortContactName.length()<3)
                            {
                                strShortContactName += contactName.charAt(index+1);
                            }
                        }
                    }
                    else
                    {
                        if(contactName.length()>6)
                            strShortContactName = contactName.substring(0, 6);
                        else
                            strShortContactName = contactName;
                    }
                    return strShortContactName;
                }
            }
        }

        return "";
    }


    public String getUserPhoto(int userId) {
        if (this.board == null) {
            return "";
        }

        if (this.board.isGroup())
        {
            if (entireMapList != null && entireMapList.containsKey(userId))
            {
                ImBoardMemeberVO memberInfo = entireMapList.get(userId);
                return memberInfo.getProfileImage();
            }
        }
        else if (this.board.isVideoChat())
        {
            for (int i = 0; i < MyApp.getInstance().g_videoMemberList.size(); i++) {
                VideoMemberVO member = MyApp.getInstance().g_videoMemberList.get(i);
                if (member.getUserId().equals(String.valueOf(userId))) {
                    return member.getImageUrl();
                }
            }
        }
        else
        {
            for (int i = 0; i < this.board.getMembers().size(); i++) {
                ImBoardMemeberVO member = this.board.getMembers().get(i);
                if (member.getUser().getUserId() == userId) {
                    return member.getUser().getPhotoUrl();
                }
            }
        }

        return "";
    }

    public boolean isCommingMsg(MultimediaMessageVO msg) {
        return !RuntimeContext.isLoginUser(msg.getFrom());
    }
}
