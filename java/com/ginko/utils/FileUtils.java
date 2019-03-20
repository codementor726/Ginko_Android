package com.ginko.utils;

import com.ginko.common.MyStringUtils;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Stony on 1/6/2015.
 */
public class FileUtils extends org.apache.commons.io.FileUtils {
    public static void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
        OutputStream out = null;
        File file = new File(uploadedFileLocation);
        try {
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(file);
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                uploadedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void deleteFile(String file) {
        File f = new File(file);
        if (!f.exists()){
            return;
        }
        f.delete();
    }

    public static String getMainName(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

    public static String getExtName(String fileName){
        int index = fileName.lastIndexOf(".");
        if (index==-1){
            return "";
        }
        return fileName.substring(index+1);
    }

    public static String createTimestampFileName(String fileName){
        String mainName = System.currentTimeMillis() + MyStringUtils.randomCreator(10);
        String extName = getExtName(fileName) ;
        return mainName + "." + extName;
    }

    public static File getUniqueFile(String folder,String name){
        File dir = new File(folder);
        if (!dir.isDirectory()){
            dir.mkdirs();
        }
        File file = new File(dir,name);
        if (!file.exists()){
            return file;
        }
        String mainName = getMainName(name);
        String extName = getExtName(name);
        String newName = mainName + "_1" + "." + extName;
        return  new File(dir,newName);
    }
    public static String getUniqueFileName(String folder,String name){
       return getUniqueFile(folder,name).getAbsolutePath();
    }

    public static String getFileNameFromUrl(String url){
        if(StringUtils.isBlank(url)){
            return "";
        }
        int start = url.lastIndexOf("/") + 1;
        int end = url.indexOf("?");
        if(end!=-1){
            return url.substring(start,end);
        }
        else{
            return url.substring(start);
        }
    }
}
