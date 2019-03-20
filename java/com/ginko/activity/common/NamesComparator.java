package com.ginko.activity.common;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

/**
 * Created by YongJong on 11/29/16.
 */

public class NamesComparator implements Comparator<JSONObject> {
    public NamesComparator(Context context)
    {
    }

    @Override
    public int compare(JSONObject lhs, JSONObject rhs) {
        // TODO Auto-generated method stub
        int result = 0;
        String leftName = null , rightName = null;

        try {
            leftName = lhs.getString("first_name").toLowerCase();
            rightName = rhs.getString("first_name").toLowerCase();
            char leftFirstLetter = ' ';
            char rightFirstLetter = ' ';

            if (!leftName.trim().equals(""))
                leftFirstLetter = leftName.charAt(0);
            else
                leftFirstLetter = ' ';
            if (!rightName.trim().equals(""))
                rightFirstLetter = rightName.charAt(0);
            else
                rightFirstLetter = ' ';
            if(!((leftFirstLetter >= 'a' && leftFirstLetter <= 'z') || (leftFirstLetter >= 'A' && leftFirstLetter <= 'Z'))
                    && !((rightFirstLetter >= 'a' && rightFirstLetter <= 'z') || (rightFirstLetter >= 'A' && rightFirstLetter <= 'Z')))
            {
                result = 0;
                return result;
            } else if(!((leftFirstLetter >= 'a' && leftFirstLetter <= 'z') || (leftFirstLetter >= 'A' && leftFirstLetter <= 'Z'))
                    && ((rightFirstLetter >= 'a' && rightFirstLetter <= 'z') || (rightFirstLetter >= 'A' && rightFirstLetter <= 'Z'))){
                result = 1;
                return result;
            } else if(((leftFirstLetter >= 'a' && leftFirstLetter <= 'z') || (leftFirstLetter >= 'A' && leftFirstLetter <= 'Z'))
                    && !((rightFirstLetter >= 'a' && rightFirstLetter <= 'z') || (rightFirstLetter >= 'A' && rightFirstLetter <= 'Z')))
            {
                result = -1;
                return result;
            }
            String s1 = (String)leftName;
            String s2 = (String)rightName;

            int thisMarker = 0;
            int thatMarker = 0;
            int s1Length = s1.length();
            int s2Length = s2.length();

            while (thisMarker < s1Length && thatMarker < s2Length)
            {
                String thisChunk = getChunk(s1, s1Length, thisMarker);
                thisMarker += thisChunk.length();

                String thatChunk = getChunk(s2, s2Length, thatMarker);
                thatMarker += thatChunk.length();

                // If both chunks contain numeric characters, sort them numerically
                if (Character.isDigit(thisChunk.charAt(0)) && Character.isDigit(thatChunk.charAt(0)))
                {
                    // Simple chunk comparison by length.
                    int thisChunkLength = thisChunk.length();
                    result = thisChunkLength - thatChunk.length();
                    // If equal, the first different number counts
                    if (result == 0)
                    {
                        for (int i = 0; i < thisChunkLength; i++)
                        {
                            result = thisChunk.charAt(i) - thatChunk.charAt(i);
                            if (result != 0)
                            {
                                return result;
                            }
                        }
                    }
                } else
                {
                    result = thisChunk.compareTo(thatChunk);
                }

                if (result != 0)
                    return result;
            }

            return s1Length - s2Length;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
    }

    /** Length of string is passed in for improved efficiency (only need to calculate it once) **/
    private final String getChunk(String s, int slength, int marker)
    {
        StringBuilder chunk = new StringBuilder();
        char c = s.charAt(marker);
        chunk.append(c);
        marker++;
        if (Character.isDigit(c))
        {
            while (marker < slength)
            {
                c = s.charAt(marker);
                if (!Character.isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        } else
        {
            while (marker < slength)
            {
                c = s.charAt(marker);
                if (Character.isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        }
        return chunk.toString();
    }
}