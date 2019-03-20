package com.ginko.activity.im;

import com.ginko.vo.ImMessageVO;

import java.util.Comparator;

public class MessageItemComparator implements Comparator<ImMessageVO> {


    public MessageItemComparator()
    {

    }

    @Override
    public int compare(ImMessageVO left, ImMessageVO right) {
        //an integer < 0 if lhs is less than rhs, 0 if they are equal, and > 0 if lhs is greater than rhs.
        int result = 0;

        if(left.getSendTime().getTime()>right.getSendTime().getTime())
        {
            result = 1;
        }
        else if(left.getSendTime().getTime()==right.getSendTime().getTime())
        {
            result = 0;
        }
        else if(left.getSendTime().getTime()<right.getSendTime().getTime())
        {
            result = -1;
        }

        return result;
    }
}
