package com.ginko.api.request;

import android.os.Looper;

import com.ginko.common.NoNullProperties;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Stony Zhang on 12/30/2014.
 */
public class MiscRequest extends GinkoRequest {
    private static final String apiGroup = "/misctask";
    private static final String uploadLog = apiGroup  + "/uploadLog";
    private static final String reportBugWithLogs = apiGroup  + "/reportBugWithLogs";

    public static void uploadLogfile( File logFile,
                                          ResponseCallBack<Void> callback) {
        Integer userId = null;
        String deviceId = Uitils.getDeviceUid();

        if (RuntimeContext.getUser() != null){
            userId = RuntimeContext.getUser().getUserId();
        }

        NoNullProperties pros = new NoNullProperties();
        pros.setProperty("user_id", userId);
        pros.setProperty("device_id", deviceId);
        HashMap<String, Object> formData = new HashMap<String, Object>();
        formData.put("file", logFile);
        AbstractRequest request = new FormSubmitRequest(uploadLog, pros,
                formData);
        request.setNeedLogin(false);
        request.setFormData(formData);
        _sendRequest(Void.class, callback, request);
    }

    public static void reportBug(File screenShootFile, File logFile, String title, String content, String priority,
                                 ResponseCallBack<Void> callback) {
        Integer userId = null;
        String deviceId = Uitils.getDeviceUid();

        if (RuntimeContext.getUser() != null){
            userId = RuntimeContext.getUser().getUserId();
        }
        title += " (Build:" + ConstValues.buildVersion + ")";
        NoNullProperties pros = new NoNullProperties();
        pros.setProperty("user_id", userId);
        pros.setProperty("device_id", deviceId);
        pros.setProperty("title", title);
        pros.setProperty("content", content);
        pros.setProperty("priority", priority);
        HashMap<String, Object> formData = new HashMap<String, Object>();

        if (screenShootFile != null) {
            formData.put("screen_shot", screenShootFile);
        }
        if (logFile != null) {
            formData.put("file", logFile);
        }
        AbstractRequest request = new FormSubmitRequest(reportBugWithLogs, pros,
                formData);
        request.setNeedLogin(false);
        request.setFormData(formData);
        boolean isMainThread = Looper.myLooper() == Looper.getMainLooper();
        if (isMainThread){
            _sendRequest(Void.class, callback, request, false, false);
        }else{
            String rawResp = request.send();

            JsonResponse response = null;
            if (rawResp==null || rawResp.isEmpty()){
                response =  JsonResponse.NullResponse;
            }else{
                response= new JsonResponse(rawResp,Void.class);
            }
            try {
                callback.onCompleted(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
