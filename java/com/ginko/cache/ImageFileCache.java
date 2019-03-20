package com.ginko.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.utils.SDCardUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

public class ImageFileCache {
    private static final String WHOLESALE_CONV = ".cach";
                                                            
	private static final int MB = 1024 * 1024;
	
    private static final int CACHE_SIZE = 10;
    private static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 10;
                                                                
    public ImageFileCache() {
        //娓呯悊鏂囦欢缂撳瓨
        removeCache(getDirectory());
    }
                                                                
    /** 浠庣紦瀛樹腑鑾峰彇鍥剧墖 **/
    public Bitmap getImage(final String url) {    
        final String path = getDirectory() + "/" + convertUrlToFileName(url);
        File file = new File(path);
        if (file.exists()) {
            Bitmap bmp = BitmapFactory.decodeFile(path);
            if (bmp == null) {
                file.delete();
            } else {
                updateFileTime(path);
                return bmp;
            }
        }
        return null;
    }
                                                                
    /** 灏嗗浘鐗囧瓨鍏ユ枃浠剁紦瀛�**/
    public synchronized void  saveBitmap(Bitmap bm, String url) {
        if (bm == null) {
            return;
        }
        //鍒ゆ柇sdcard涓婄殑绌洪棿
        if (!SDCardUtil.checkFreeSpace(FREE_SD_SPACE_NEEDED_TO_CACHE)) {
            //SD绌洪棿涓嶈冻
            return;
        }
        String filename = convertUrlToFileName(url);
        String dir = getDirectory();
        File dirFile = new File(dir);
        if (!dirFile.exists())
            dirFile.mkdirs();
        File file = new File(dir +"/" + filename);
        try {
            file.createNewFile();
            OutputStream outStream = new FileOutputStream(file);
            //Stony, confirmed, also can use Bitmap.CompressFormat.PNG, don't know the difference so far.
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            Logger.warn( "FileNotFoundException");
        } catch (IOException e) {
            Logger.warn("IOException");
        }
    } 
                                                                
    /**
     * 璁＄畻瀛樺偍鐩綍涓嬬殑鏂囦欢澶у皬锛�
     * 褰撴枃浠舵�澶у皬澶т簬瑙勫畾鐨凜ACHE_SIZE鎴栬�sdcard鍓╀綑绌洪棿灏忎簬FREE_SD_SPACE_NEEDED_TO_CACHE鐨勮瀹�
     * 閭ｄ箞鍒犻櫎40%鏈�繎娌℃湁琚娇鐢ㄧ殑鏂囦欢
     */
    private boolean removeCache(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }
        if (!SDCardUtil.sdCardReady()) {
            return false;
        }
                                                            
        int dirSize = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(WHOLESALE_CONV)) {
                dirSize += files[i].length();
            }
        }
                                                            
        if (dirSize > CACHE_SIZE * MB || !SDCardUtil.checkFreeSpace(FREE_SD_SPACE_NEEDED_TO_CACHE)) {
            int removeFactor = (int) ((0.4 * files.length) + 1);
            Arrays.sort(files, new FileLastModifSort());
            for (int i = 0; i < removeFactor; i++) {
                if (files[i].getName().contains(WHOLESALE_CONV)) {
                    files[i].delete();
                }
            }
        }
                                                            
        if (freeSpaceOnSd() <= CACHE_SIZE) {
            return false;
        }
                                                                    
        return true;
    }


                                                                
    private int freeSpaceOnSd() {
		return SDCardUtil.getFreeSpaceOnSd();
	}

	/** 淇敼鏂囦欢鐨勬渶鍚庝慨鏀规椂闂�**/
    public void updateFileTime(String path) {
        File file = new File(path);
        long newModifiedTime = System.currentTimeMillis();
        file.setLastModified(newModifiedTime);
    }
                                                                
                                                                
    /** 灏唘rl杞垚鏂囦欢鍚�**/
    private String convertUrlToFileName(String url) {
        String[] strs = url.split("/");
        return strs[strs.length - 1] + WHOLESALE_CONV;
    }
                                                                
    /** 鑾峰緱缂撳瓨鐩綍 **/
    private String getDirectory() {
        String dir = RuntimeContext.getCacheFolder().getAbsolutePath();
        return dir;
    }
                                                                
    /**
     * 鏍规嵁鏂囦欢鐨勬渶鍚庝慨鏀规椂闂磋繘琛屾帓搴�
     */
    private class FileLastModifSort implements Comparator<File> {
        public int compare(File arg0, File arg1) {
            if (arg0.lastModified() > arg1.lastModified()) {
                return 1;
            } else if (arg0.lastModified() == arg1.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }
                                                            
}