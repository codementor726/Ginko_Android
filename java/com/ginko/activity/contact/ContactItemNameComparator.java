package com.ginko.activity.contact;

import java.util.Comparator;

public class ContactItemNameComparator implements Comparator<ContactItem> {


    public ContactItemNameComparator()
    {

    }

    @Override
    public int compare(ContactItem left, ContactItem right) {
        //an integer < 0 if lhs is less than rhs, 0 if they are equal, and > 0 if lhs is greater than rhs.
        int result = 0;

        if(left.getFullName().compareTo("") == 0)
            return 1;
        if(right.getFullName().compareTo("") == 0)
            return -11;


        if(left.getFullName().toLowerCase().charAt(0)>right.getFullName().toLowerCase().charAt(0))
        {
            result = 1;
        }
        else if(left.getFullName().toLowerCase().charAt(0)==right.getFullName().toLowerCase().charAt(0))
        {
            result = 0;
        }
        else if(left.getFullName().toLowerCase().charAt(0)<right.getFullName().toLowerCase().charAt(0))
        {
            result = -1;
        }

        return result;
    }
}