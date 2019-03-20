package com.ginko.common;

import com.ginko.api.request.MiscRequest;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.utils.ZipUtil;

import java.io.File;
import java.io.IOException;

public class Logger {
	private static final String logTag = "ginko";
    private static org.apache.log4j.Logger gLogger;


	public static boolean showLog() {
		return true;
	}

	public static void log(String msg) {
		if (!showLog()) {
			return;
		}
        getLog4jLogger().info(msg);
//		Log.d(logTag, msg);
	}

	public static void error(String msg) {
		if (!showLog()) {
			return;
		}
        getLog4jLogger().error(msg);
//		Log.e(logTag, msg);
	}

	public static void warn(String msg) {
		if (!showLog()) {
			return;
		}
        getLog4jLogger().warn(msg);
//		Log.w(logTag, msg);
	}

    public static void warn(String msg, Throwable e) {
        if (!showLog()) {
            return;
        }
        getLog4jLogger().warn(msg,e);
//        Log.w(logTag, msg,e);
    }

    public static void error(Throwable e) {
        error(e.getMessage(), e);
    }
    public static void error(String msg, Throwable e) {
        if (!showLog()) {
            return;
        }
        getLog4jLogger().error(msg,e);
//        Log.e(logTag, msg, e);
    }

	public static void debug(String msg) {
		if (!showLog()) {
			return;
		}
        getLog4jLogger().debug(msg);
//		Log.d(logTag, msg);
	}

	public static void info(String msg) {
		if (!showLog()) {
			return;
		}
        getLog4jLogger().info(msg);
//		Log.i(logTag, msg);
	}
    public static void fatal(String msg) {
        getLog4jLogger().error(msg);
        Uitils.reportBug("Fatal Error Occur!",msg);
    }

    private static org.apache.log4j.Logger getLog4jLogger() {
        if (gLogger==null){
            gLogger = org.apache.log4j.Logger.getLogger(logTag);
        }
        return gLogger;
    }

    public static void uploadLog(){
        File outFile = zipLog();
        if (outFile == null) return;
        final String logfilePath = outFile.getAbsolutePath();
        MiscRequest.uploadLogfile(outFile, new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse response) {
                if(!response.isSuccess()){
                    Uitils.alert("Can't upload log file to server, you can get it in " + logfilePath);
                }
            }
        });
    }

    public static File zipLog() {
        File outFile = null;
        try {
            outFile =new File ( RuntimeContext.getTempFolder() , "log.zip");
            if (outFile.exists()){
                outFile.delete();
            }
            ZipUtil.zip(RuntimeContext.getLogerFolder(), outFile);
        } catch (IOException e) {
            Logger.error(e);
        }
        //upload log
        if (outFile==null || !outFile.exists()) {
            Uitils.alert("Can't upload log file to server.");
            return null;
        }
        return outFile;
    }
}
