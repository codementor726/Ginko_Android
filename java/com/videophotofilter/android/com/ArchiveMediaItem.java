package com.videophotofilter.android.com;

import android.graphics.Bitmap;

import com.ginko.context.ConstValues;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.TcVideoVO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ArchiveMediaItem {

    public static final int VIDEO_MEDIAT_TYPE = 0;
    public static final int PHOTO_MEDIAT_TYPE = 1;

    public int archiveID = 0;
    public int mediaType = 0;//0:Video , 1:Image
    public List<String> downloadedFilePaths = new ArrayList<String>();
    public List<TcImageVO> userImages;
    public List<TcVideoVO> thumbImages;
    public List<EntityImageVO> entityImages;
    public boolean isDefaultItem = false; //if this is default item , then show no available image
    public boolean isNewItem = false;
    public int archiveType = ConstValues.HOME_PHOTO_EDITOR;


    public ArchiveMediaItem(int _archiveType)
    {
        this.archiveType = _archiveType;
        this.mediaType = PHOTO_MEDIAT_TYPE;
        downloadedFilePaths = new ArrayList<String>();
        userImages = new ArrayList<TcImageVO>();
        thumbImages = new ArrayList<TcVideoVO>();
        entityImages = new ArrayList<EntityImageVO>();
        this.isDefaultItem = false;
        this.isNewItem = false;
    }

}
