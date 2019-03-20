package com.ginko.vo;

import com.ginko.context.ConstValues;

import org.apache.commons.lang.ArrayUtils;

import java.io.Serializable;

public class FontSettingVo implements Serializable {
    private String fontName = "Arial";
    private String fontStyle = "Normal";
    private String fontSize = "17";

    private int fontNameIndex = 0;
    private int fontStyleIndex = 0;
    private int fontSizeIndex = 0;


    public FontSettingVo()
    {
        //default values
        this.fontName = "Arial";
        this.fontStyle = "Normal";
        this.fontSize = "17";
        this.fontNameIndex = ArrayUtils.indexOf(ConstValues.fontNamesArray, "Arial");
        this.fontStyleIndex = ArrayUtils.indexOf(ConstValues.fontStyleArray, "Normal");
        this.fontSizeIndex = ArrayUtils.indexOf(ConstValues.fontSizeArray, "17");
    }

    public FontSettingVo(String fontname , String fontstyle , String fontsize)
    {
        if(fontname.compareTo("") == 0)
            this.fontName = "Arial";
        else
            this.fontName = fontname;
        if(fontstyle.compareTo("") == 0)
            this.fontStyle = "Normal";
        else
            this.fontStyle = fontstyle;
        if(fontsize.compareTo("") == 0)
            this.fontSize = "17";
        else
            this.fontSize = fontsize;
        this.fontNameIndex = ArrayUtils.indexOf(ConstValues.fontNamesArray, this.fontName); this.fontNameIndex = this.fontNameIndex<0?0:this.fontNameIndex;
        this.fontStyleIndex = ArrayUtils.indexOf(ConstValues.fontStyleArray, this.fontStyle);this.fontStyleIndex = this.fontStyleIndex<0?0:this.fontStyleIndex;
        this.fontSizeIndex = ArrayUtils.indexOf(ConstValues.fontSizeArray, this.fontSize); this.fontSizeIndex = this.fontSizeIndex<0?0:this.fontSizeIndex;
    }

    public void setFontName(String fontname){
        if(fontname.compareTo("") == 0)
            this.fontName = "Arial";
        else
            this.fontName = fontname;
        this.fontNameIndex = ArrayUtils.indexOf(ConstValues.fontNamesArray, this.fontName); this.fontNameIndex = this.fontNameIndex<0?0:this.fontNameIndex;
    }
    public String getFontName(){return  this.fontName;}

    public void setFontStyle(String fontstyle){
        if(fontstyle.compareTo("") == 0)
            this.fontStyle = "Normal";
        else
            this.fontStyle = fontstyle;
        this.fontStyleIndex = ArrayUtils.indexOf(ConstValues.fontStyleArray, this.fontStyle);this.fontStyleIndex = this.fontStyleIndex<0?0:this.fontStyleIndex;
    }
    public String getFontStyle(){return  this.fontStyle;}

    public void setFontSize(String fontsize){
        if(fontsize.compareTo("") == 0)
            this.fontSize = "17";
        else
            this.fontSize = fontsize;
        this.fontSizeIndex = ArrayUtils.indexOf(ConstValues.fontSizeArray, this.fontSize); this.fontSizeIndex = this.fontSizeIndex<0?0:this.fontSizeIndex;
    }
    public String getFontSize(){return  this.fontSize;}

    public int getFontNameArrayIndex(){
        return this.fontNameIndex;
    }

    public int getFontStyleArrayIndex(){
        return this.fontStyleIndex;
    }

    public int getFontSizeArrayIndex(){
        return this.fontSizeIndex;
    }
}
