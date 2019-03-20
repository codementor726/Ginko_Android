package com.ginko.api.request;

import com.ginko.common.NoNullProperties;
import com.ginko.common.RuntimeContext;
import com.ginko.data.IGinkoRequest;
import com.ginko.data.ResponseCallBack;
import com.ginko.vo.DirectoryVO;
import com.ginko.vo.PurpleContactWholeProfileVO;
import com.ginko.vo.UserWholeProfileVO;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

/**
 * Created by YongJong on 01/11/17.
 */
public class DirectoryRequest extends GinkoRequest {
    private static final String apiGroup = "/directory";
    private static final String createDirectory= apiGroup + "/create";
    private static final String updateDirectory= apiGroup + "/update";
    private static final String removeDirectory= apiGroup + "/delete";
    private static final String listAllDirectory= apiGroup + "/list";
    private static final String getDirectory= apiGroup + "/get";
    private static final String checkAvailableName= apiGroup + "/checkAvail";
    private static final String inviteMembers= apiGroup + "/invite";
    private static final String removeInvite= apiGroup + "/removeInvite";
    private static final String removeMember= apiGroup + "/removeMember";
    private static final String listInvites= apiGroup + "/listInvite";
    private static final String listConfirmed= apiGroup + "/listConfirmed";
    private static final String listRequest= apiGroup + "/listRequest";

    private static final String requestApprove= apiGroup + "/request/approve";
    private static final String requestDelete= apiGroup + "/request/remove";

    private static final String checkExisted= apiGroup + "/member/checkExisted";
    private static final String joinDirectory= apiGroup + "/member/join";
    private static final String getPermission= apiGroup + "/member/getPermission";
    private static final String updatePermission= apiGroup + "/member/updatePermission";
    private static final String listJoined= apiGroup + "/member/listJoined";
    private static final String listReceivedInvite= apiGroup + "/member/listReceivedInvite";
    private static final String listSentRequest= apiGroup + "/member/listSentRequest";
    private static final String exitMember = apiGroup + "/member/quit";
    private static final String listMember = apiGroup + "/member/list";
    private static final String memberDetail = apiGroup + "/member/detail";
    private static final String validateMember = apiGroup + "/member/validateEmail";
    private static final String removeInviteRequest = apiGroup + "/member/removeJoinInvite";
    private static final String cancelPendingRequest = apiGroup + "/member/cancelJoinRequest";

    private static final String profileUpload= apiGroup + "/profile/image/upload";
    private static final String profileRemove= apiGroup + "/profile/image/remove";


    public static void createDirectory(DirectoryVO dirInfo, ResponseCallBack<JSONObject> callback) {
        QueryParametersRequest request = new QueryParametesWithBodyRequest(createDirectory, dirInfo);
        _sendRequest(JSONObject.class, callback, request , true , true);
    }

    public static void updateDirectory(DirectoryVO dirInfo, ResponseCallBack<JSONObject> callback)  {
         QueryParametersRequest request = new QueryParametesWithBodyRequest(updateDirectory, dirInfo);
        _sendRequest(JSONObject.class, callback, request , false , true);
    }

    public static void removeProfileImage(Integer directoryId , ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        QueryParametersRequest request = new QueryParametersRequest(profileRemove, qPara);
        _sendRequest(Void.class, callback, request , true , true);
    }

    public static void uploadProfileImage(Integer directoryId,File image, boolean showProgress,
                                          ResponseCallBack<JSONObject > callback) {
        NoNullProperties pros = new NoNullProperties();
        pros.setProperty("id", directoryId);
        HashMap<String, Object> formData = new HashMap<String, Object>();
        formData.put("image", image);
        AbstractRequest request = new FormSubmitRequest(profileUpload, pros,
                formData);
        request.setFormData(formData);
        _sendRequest(JSONObject.class, callback, request , false , showProgress);
    }

    public static void checkNameAvailable(String name, ResponseCallBack<Void> callBack) {
        NoNullProperties pros = new NoNullProperties();
        pros.setProperty("name", name);
        QueryParametersRequest request = new QueryParametersRequest(checkAvailableName, get,
                pros);
        _sendRequest(Void.class, callBack, request, false, true);
    }

    public static void deleteDirectory(Integer directoryId,
                                    ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        QueryParametersRequest request = new QueryParametersRequest(removeDirectory, qPara);
        _sendRequest(Void.class, callback, request , false , true);
    }

    public static void listDirectories(ResponseCallBack<List<DirectoryVO>> callback) {
        Type type = new TypeToken<List<DirectoryVO>>() {}.getType();
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("sessionId", RuntimeContext.getSessionId());

        QueryParametersRequest request = new QueryParametersRequest(listAllDirectory, get);
        _sendRequest(type, callback, request);
    }

    public static void getDirectoryDetail(Integer directoryId,
                                 ResponseCallBack<DirectoryVO> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        QueryParametersRequest request = new QueryParametersRequest(getDirectory, get, qPara);
        _sendRequest(DirectoryVO.class, callback, request, false, true);
    }

    public static void listInvites(Integer directoryId, Integer pageNum, Integer countPerPage, ResponseCallBack<JSONObject> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        qPara.setProperty("pageNum", pageNum);
        qPara.setProperty("countPerPage", countPerPage);
        QueryParametersRequest request = new QueryParametersRequest(listInvites, get, qPara);
        _sendRequest(JSONObject.class, callback, request , false  , true);
    }

    public static void listConfirmed(Integer directoryId, Integer pageNum, Integer countPerPage, ResponseCallBack<JSONObject> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        qPara.setProperty("pageNum", pageNum);
        qPara.setProperty("countPerPage", countPerPage);
        QueryParametersRequest request = new QueryParametersRequest(listConfirmed, get, qPara);
        _sendRequest(JSONObject.class, callback, request , false  , true);
    }

    public static void listRequest(Integer directoryId, Integer pageNum, Integer countPerPage, ResponseCallBack<JSONObject> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        qPara.setProperty("pageNum", pageNum);
        qPara.setProperty("countPerPage", countPerPage);
        QueryParametersRequest request = new QueryParametersRequest(listRequest, get, qPara);
        _sendRequest(JSONObject.class, callback, request , false  , true);
    }

    public static void inviteFriends(Integer directoryId, String contact_uids,
                                     ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        qPara.setProperty("m_uids", contact_uids);
        QueryParametersRequest request = new QueryParametersRequest(inviteMembers, qPara);
        _sendRequest(Void.class, callback, request);
    }

    public static void deleteMembers(Integer directoryId, String contact_uids,
                                       ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        qPara.setProperty("m_uids", contact_uids);
        QueryParametersRequest request = new QueryParametersRequest(removeMember, qPara);
        _sendRequest(Void.class, callback, request , false , true);
    }

    public static void approveRequests(Integer directoryId, String contact_uids,
                                     ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        qPara.setProperty("m_uids", contact_uids);
        QueryParametersRequest request = new QueryParametersRequest(requestApprove, qPara);
        _sendRequest(Void.class, callback, request);
    }

    public static void deleteRequests(Integer directoryId, String contact_uids,
                                       ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        qPara.setProperty("m_uids", contact_uids);
        QueryParametersRequest request = new QueryParametersRequest(requestDelete, qPara);
        _sendRequest(Void.class, callback, request , false , true);
    }

    public static void cancelInvite(Integer directoryId, String contact_uids,
                                     ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        qPara.setProperty("m_uids", contact_uids);
        QueryParametersRequest request = new QueryParametersRequest(removeInvite, qPara);
        _sendRequest(Void.class, callback, request , false , true);
    }

    public static void checkDirectoryExist(String name, ResponseCallBack<JSONObject> callBack) {
        NoNullProperties pros = new NoNullProperties();
        pros.setProperty("name", name);
        QueryParametersRequest request = new QueryParametersRequest(checkExisted, get,
                pros);
        _sendRequest(JSONObject.class, callBack, request, false, true);
    }

    public static void getPermissionShared(Integer contact_uid, ResponseCallBack<JSONObject> callback, boolean showProgressDialog) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("sessionId", RuntimeContext.getSessionId());

        if (contact_uid !=null){
            qPara.setProperty("id", contact_uid);
        }

        QueryParametersRequest request = new QueryParametersRequest(getPermission, get, qPara);
        _sendRequest(JSONObject.class, callback, request, true,showProgressDialog);
    }

    public static void joinDirectory(Integer directoryID, Integer sharing, String sharedHomeFieldIds, String sharedWorkFieldIds, ResponseCallBack<JSONObject> callback , boolean bProgressDialog)
    {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryID);
        qPara.setProperty("sharing", sharing);
        qPara.setProperty("shared_home_fids", sharedHomeFieldIds);
        qPara.setProperty("shared_work_fids", sharedWorkFieldIds);
        QueryParametersRequest request = new QueryParametersRequest(joinDirectory, qPara);
        _sendRequest(JSONObject.class, callback, request , true , bProgressDialog);
    }

    public static void listJoinedDirectory(Integer pageNum, Integer countPerPage, ResponseCallBack<JSONObject> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("pageNum", pageNum);
        qPara.setProperty("countPerPage", countPerPage);
        QueryParametersRequest request = new QueryParametersRequest(listJoined, get, qPara);
        _sendRequest(JSONObject.class, callback, request , false  , true);
    }

    public static void updatePermission(Integer directoryId, Integer sharing, String sharedHomeFieldIds, String sharedWorkFieldIds, ResponseCallBack<Void> callback , boolean bProgressDialog) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        qPara.setProperty("sharing", sharing);
        qPara.setProperty("shared_home_fids", sharedHomeFieldIds);
        qPara.setProperty("shared_work_fids", sharedWorkFieldIds);
        QueryParametersRequest request = new QueryParametersRequest(updatePermission, qPara);
        _sendRequest(Void.class, callback, request , true , bProgressDialog);
    }

    public static void quitMember( Integer directoryId, ResponseCallBack<Void> callback , boolean bProgressDialog) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", directoryId);
        QueryParametersRequest request = new QueryParametersRequest(exitMember, qPara);
        _sendRequest(Void.class, callback, request , true , bProgressDialog);
    }

    public static void getAllMembers(int groupId, Integer pageNum, Integer countPerPage, ResponseCallBack<JSONObject> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id", groupId);
        qPara.setProperty("pageNum", pageNum);
        qPara.setProperty("countPerPage", countPerPage);
        QueryParametersRequest request = new QueryParametersRequest(listMember, get,qPara);
        _sendRequest(JSONObject.class, callback, request , false , true);
    }

    public static void getMemberDetail(Integer directoryId, Integer contactId , ResponseCallBack<PurpleContactWholeProfileVO> callback)
    {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("id" , directoryId);
        qPara.setProperty("uid" , contactId);

        QueryParametersRequest request = new QueryParametersRequest(memberDetail , get , qPara);
        _sendRequest(PurpleContactWholeProfileVO.class , callback , request , true , true);

    }

    public static void getReceivedInvite(Integer pageNum, Integer countPerPage, ResponseCallBack<JSONObject> callback){
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("pageNum", pageNum);
        qPara.setProperty("countPerPage", countPerPage);
        QueryParametersRequest request = new QueryParametersRequest(listReceivedInvite, get, qPara);
        _sendRequest(JSONObject.class, callback, request);
    }

    public static void getRequestSent(Integer pageNum, Integer countPerPage, ResponseCallBack<JSONObject> callback){
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("pageNum", pageNum);
        qPara.setProperty("countPerPage", countPerPage);
        QueryParametersRequest request = new QueryParametersRequest(listSentRequest, get, qPara);
        _sendRequest(JSONObject.class, callback, request);
    }

    public static void validateEmail(String key, ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("key", key);
        QueryParametersRequest request = new QueryParametersRequest(validateMember, get, qPara);
        _sendRequest(JSONObject.class, callback, request);
    }

    public static void removeJoinRequest( String ids, ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("ids", ids);
        QueryParametersRequest request = new QueryParametersRequest(removeInviteRequest, qPara);
        _sendRequest(Void.class, callback, request , false , true);
    }

    public static void cancelPendingRequest( String ids, ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("ids", ids);
        QueryParametersRequest request = new QueryParametersRequest(cancelPendingRequest, qPara);
        _sendRequest(Void.class, callback, request , false , true);
    }
}
