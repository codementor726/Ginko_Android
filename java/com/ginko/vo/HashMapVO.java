package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;

/**
 * Created by YongJong on 03/29/17.
 */
public class HashMapVO implements Serializable{
    @Alias("id")
    private Integer id;

    @Alias("name")
    private String name;

    @Alias("photo_url")
    private String photoUrl;

    public Integer getId() {return id;}
    public void setId(Integer _data) {this.id = _data;}

    public String getName() {return name;}
    public void setName(String _data) {this.name = _data;}

    public String getPhotoUrl() {return photoUrl;}
    public void setPhotoUrl(String _data) {this.photoUrl = _data;}
}
