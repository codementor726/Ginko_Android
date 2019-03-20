package com.ginko.vo;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.sz.util.json.Alias;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserUpdateVO implements Serializable {
    @Alias(value = "group")
    private String groupName;

    private List<UserProfileVO> fields = new ArrayList<UserProfileVO>();

    @Alias("images")
    private List<TcImageVO> images = new ArrayList<TcImageVO>();

    @Alias("video")
    private TcVideoVO video;

    @Alias("profile_image")
    private String profileImage;

    public List<UserProfileVO> getFields() {
        return fields;
    }

    public int getInputableFieldsCount()
    {
        if(fields == null) return 0;
        int count = 0;
        for (UserProfileVO field : fields) {
            String fieldType = field.getFieldType();
            if(fieldType.equals(""))
                continue;
            if (fieldType.equalsIgnoreCase("abbr"))
                continue;
            if (fieldType.equalsIgnoreCase("privilege"))
                continue;
            if (fieldType.equalsIgnoreCase("video"))
                continue;
            if (fieldType.equalsIgnoreCase("foreground"))
                continue;
            if (fieldType.equalsIgnoreCase("background"))
                continue;

            count++;
        }
        return  count;
    }

    public void setFields(List<UserProfileVO> fields) {
        this.fields = fields;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<TcImageVO> getImages() {
        return images;
    }

    public void setImages(List<TcImageVO> images) {
        this.images = images;
    }

    @Alias(ignoreGet =  true , ignoreSet = true)
    public TcImageVO getWallpapaerImage()
    {
        TcImageVO wallpaperImage = null;
        if(images == null || images.size() == 0) return wallpaperImage;

        for(int i=0;i<images.size();i++)
        {
            if(images.get(i).getZIndex() == 0)//background image is changed to wallpaper
            {
                wallpaperImage = images.get(i);
                break;
            }
        }

        return wallpaperImage;
    }

    public TcVideoVO getVideo() {
        return video;
    }

    public void setVideo(TcVideoVO video) {
        this.video = video;
    }

    @Alias("profile_image")
    public String getProfileImage() {
        if(profileImage == null) profileImage = "";
        return profileImage;
    }

    public URI getImageUri(String str) throws URISyntaxException {
        URI myUri = null;
        if (str == "" || str == null)
            return myUri;

        myUri = new URI(str);
        return myUri;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }


    public boolean isPublic() {
        for (UserProfileVO field : fields) {
            if (field.getFieldName().equalsIgnoreCase("Privilege")) {
                if (field.getValue().equalsIgnoreCase("1")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setPublic(boolean isPublic) {
        UserProfileVO privilegeField = null;
        for (UserProfileVO field : fields) {
            if (field.getFieldName().equalsIgnoreCase("privilege")) {
                privilegeField = field;
                break;
            }
        }
        if (privilegeField == null) {
            privilegeField = new UserProfileVO();
            privilegeField.setFieldName("Privilege");
            //privilegeField.setFont("");
            //privilegeField.setColor("");
            privilegeField.setPosition("");
            privilegeField.setFieldType("privilege");
            fields.add(privilegeField);
        }
        privilegeField.setValue(isPublic ? "1" : "0");
    }

    public void removeForeground() {
        removeField("Foreground");
    }

    public void removeBackground() {
        String fieldName = "Background";
        removeField(fieldName);
    }

    private void removeField(String fieldName) {
        for(Iterator<UserProfileVO> iter = fields.iterator();iter.hasNext();){
            UserProfileVO field = iter.next();
            if (field.getFieldName().equalsIgnoreCase(fieldName)) {
                iter.remove();
            }
        }
    }

    public boolean getAbbr() {
        UserProfileVO f = getFieldByName("Abbr");
        if (f != null) {
            return f.getValue().equalsIgnoreCase("1");
        }
        return false;
    }


    public void setAbbr(boolean abbr) {
        UserProfileVO f = getFieldByName("Abbr");
        if (f == null) {
            f = new UserProfileVO();
            f.setFieldName("Abbr");
            f.setFieldType("abbr");
            //f.setFont("");
            //f.setColor("");
            f.setPosition("");
            fields.add(f);
        }
        f.setValue(abbr ? "1" : "0");
    }

    private UserProfileVO getFieldByName(String fieldName) {
        UserProfileVO f = null;
        for (UserProfileVO field : fields) {
            if (field.getFieldName().equalsIgnoreCase(fieldName)) {
                f =  field;
                break;
            }
        }
        return f;
    }

    public TcImageVO getForeground(){
        for (TcImageVO image : this.images) {
            if(image.getZIndex()==0){
                return image;
            }
        }
        return null;
    }

    public TcImageVO getBackground(){
        for (TcImageVO image : this.images) {
            if (image.getZIndex() > 0) {
                return image;
            }
        }
        return null;
    }

    public String getVideoUrl() {
        if(video == null)
            return "";
        else
            return video.getVideo_url();

        /*UserProfileVO f = getFieldByName("video");
        if (f != null) {
            return f.getValue();
        }
        return null;*/
    }

    public boolean hasVideo(){
        return StringUtils.isNotBlank(this.getVideoUrl());
    }

    public void removeVideo() {
        removeField("Video");
    }

    @Alias(ignoreGet =  true , ignoreSet = true)
    public String getProfileUserName()
    {
        String strName = "";
        if(fields != null && fields.size()>0)
        {
            for(UserProfileVO field:fields)
            {
                if(field.getFieldName().trim().toLowerCase().contains("name"))//get name field
                {
                    strName = field.getValue();
                    break;
                }
            }
        }
        return strName;
    }

    @Alias(ignoreGet =  true , ignoreSet = true)
    public void setProfileUserName(String name)
    {
        if(fields == null)
            fields = new ArrayList<UserProfileVO>();
        for(UserProfileVO field : fields)
        {
            if(field.getFieldType().toLowerCase().contains("name"))
            {
                field.setValue(name);
                return;
            }
        }
        //if the name field wasn't exist, then add new one

    }

}
