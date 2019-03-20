package com.ginko.activity.entity;

import com.ginko.vo.EntityMessageExtVO;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.ImMessageVO;

import java.util.Comparator;

public class EntityMessageItemComparator implements Comparator<EntityMessageVO> {


    public EntityMessageItemComparator()
    {

    }

    @Override
    public int compare(EntityMessageVO left, EntityMessageVO right) {
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
