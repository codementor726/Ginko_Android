package com.ginko.utils;

import android.os.Environment;
import android.os.StatFs;

public class SDCardUtil {
	private static final int MB = 1024 * 1024;

	/** 计算sdcard上的剩余空间 **/
	public static int getFreeSpaceOnSd() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize()) / MB;
		return (int) sdFreeMB;
	}

	
	public static boolean sdCardReady() {
		return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
	}
	
	public static boolean checkFreeSpace(int needSpace) {
		return needSpace < getFreeSpaceOnSd();
	}
}
