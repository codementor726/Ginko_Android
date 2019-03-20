package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;


public class UserProfileVO implements Serializable {

    @Alias("field_id")
    private Integer id;

    @Alias("field_name")
    private String fieldName;

    @Alias("field_value")
    private String value;

    @Alias("field_type")
    private String fieldType;

    //@Alias("field_color")
    //private String color;

    //@Alias("field_font")
    //private String font;

    @Alias("field_position")
    private String position;

    @Alias(ignoreGet = true , ignoreSet = true)
    private boolean isShared = false;

    @Alias(ignoreGet = true , ignoreSet = true)
    private FontSettingVo fontSettingVo;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }


    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /*public String getColor() {
        return color;
    }

    public void setColor(String color) {
            this.color = color;
    }

    public String getFont() {
        return font;
    }

    public FontSettingVo getFontSettingVo(){
        if(font == null)
        if(this.fontSettingVo == null)
        {
            String fieldFont = font;
            int fontSize = 17;//default
            String fontStyle = "Normal";
            if(fieldFont.contains(":"))
            {
                int index = fieldFont.indexOf(":");
                String strFontSize = fieldFont.substring(index+1);
                if(strFontSize.contains(":"))//if has font style
                {
                    int secondIndex = strFontSize.indexOf(":");
                    fontStyle = strFontSize.substring(secondIndex+1);
                    strFontSize = strFontSize.substring(0 , secondIndex);
                    try {
                        float f = Float.valueOf(strFontSize);
                        fontSize = (int) f;
                    } catch (Exception e) {
                        e.printStackTrace();
                        fontSize = 17;
                    }
                }
                else {
                    try {
                        float f = Float.valueOf(strFontSize);
                        fontSize = (int) f;
                    } catch (Exception e) {
                        e.printStackTrace();
                        fontSize = 17;
                    }
                }
                fieldFont = fieldFont.substring(0 , index);
            }

            this.fontSettingVo = new FontSettingVo(fieldFont , fontStyle , String.valueOf(fontSize));
        }
        return  this.fontSettingVo;
    }
    public void setFont(String font)
    {
        this.font = font;

        String fieldFont = font;
        int fontSize = 17;//default
        String fontStyle = "Normal";
        if(fieldFont.contains(":"))
        {
            int index = fieldFont.indexOf(":");
            String strFontSize = fieldFont.substring(index+1);
            if(strFontSize.contains(":"))//if has font style
            {
                int secondIndex = strFontSize.indexOf(":");
                fontStyle = strFontSize.substring(secondIndex+1);
                strFontSize = strFontSize.substring(0 , secondIndex);
                try {
                    float f = Float.valueOf(strFontSize);
                    fontSize = (int) f;
                } catch (Exception e) {
                    e.printStackTrace();
                    fontSize = 17;
                }
            }
            else {
                try {
                    float f = Float.valueOf(strFontSize);
                    fontSize = (int) f;
                } catch (Exception e) {
                    e.printStackTrace();
                    fontSize = 17;
                }
            }
            fieldFont = fieldFont.substring(0 , index);
        }

        this.fontSettingVo = new FontSettingVo(fieldFont , fontStyle , String.valueOf(fontSize));
    }*/

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setPosition(int x, int y, int width, int height,  float ratio) {
        String format = "NSRect: {{%s, %s}, {%s, %s}}";
        String postionStr = String.format(format, x / (ratio * 1.0), y / (ratio * 1.0), width / (ratio * 1.0), height / (ratio * 1.0));
        setPosition(postionStr);
    }

    public void setIsShared(boolean _isShared)
    {
        this.isShared = _isShared;
    }
    public boolean isShared(){return this.isShared;}
}
