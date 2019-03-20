package com.ginko.activity.sprout;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.exchange.ShareingLeafDialog;
import com.ginko.activity.im.GroupVideoChatActivity;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.api.request.IMRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.customview.SharingBean;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.EventUser;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.VideoMemberVO;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class SproutExchangedItemView  extends SproutListItemView {

    private ImageView imageSelectionCheck;
    private NetworkImageView imageView;
    private TextView tvName , txtFoundTime , txtAddress;
    private ImageView ivPhone , ivChat , ivEdit;


    private List<String> phones = null;

    private ImageLoader imgLoader;

    private boolean isListSelectable = false;

    public void setIsListSelectable(boolean isListSelectable)
    {
        this.isListSelectable = isListSelectable;
    }

    public SproutExchangedItemView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub

    }
    public SproutExchangedItemView(Context context,  SproutSearchItem _item)
    {
        super(context , _item);
        inflater.inflate(R.layout.list_item_sprout_exchanged, this, true);

        imageSelectionCheck = (ImageView)findViewById(R.id.imageSelectionCheck);
        imageView = (NetworkImageView)findViewById(R.id.photo);
        tvName = (TextView)findViewById(R.id.txtName);
        txtFoundTime = (TextView)findViewById(R.id.lbl_time);
        txtAddress = (TextView)findViewById(R.id.txtAddress);
        ivPhone = (ImageView)findViewById(R.id.action_phone);
        ivChat = (ImageView)findViewById(R.id.action_chat);
        ivEdit = (ImageView)findViewById(R.id.action_edit);

        final JSONArray phonesJsonArray = item.jsonObject.optJSONArray("phones");

        phones  = new ArrayList<String>();

        if (phonesJsonArray != null) {
            for(int i = 0 ;i<phonesJsonArray.length();i++){
                phones.add(phonesJsonArray.optString(i));
            }
        }

        phones.add("Cancel");

        if (!phones.contains("Ginko Video Call"))
            phones.add(0, "Ginko Video Call");
        if (!phones.contains("Ginko Voice Call"))
            phones.add(0, "Ginko Voice Call");

        if (item.nSharingStatus == 4){
            ivPhone.setVisibility(View.GONE);
            ivEdit.setVisibility(View.VISIBLE);
            ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //if(isListSelectable) return;
                    /*
                    Intent intent = new Intent(mContext, ShareingLeafDialog.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("contact_uid", item.contactOrEntityID);

                    SharingBean sb = new SharingBean();
                    sb.setSharingStatus(item.nSharingStatus);
                    bundle.putSerializable("sharebean", sb);
                    intent.putExtras(bundle);
                    ((Activity)mContext).startActivityForResult(intent, 2000);*/


                    Intent intent = new Intent(mContext, ShareYourLeafActivity.class);
                    intent.putExtra("contactID", String.valueOf(item.contactOrEntityID));
                    intent.putExtra("contactFullname", item.entityOrContactName);
                    intent.putExtra("sharing_status", item.nSharingStatus);
                    intent.putExtra("isInviteContact", true);
                    ((Activity)mContext).startActivityForResult(intent, 2000);
                }
            });
        }else{
            ivPhone.setVisibility(View.VISIBLE);
            ivEdit.setVisibility(View.GONE);
        }

        if (phones != null && phones.size()!=0) {
            ivPhone.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (phones.size() == 1) {
                                Uitils.alert("Oops! No registered phone numbers.");
                                return;
                            }
                            //if (!checkSimCard(mContext))
                            //return;
                            //if(isListSelectable) return;
                            ((MyGinkoMeActivity)mContext).hideKeyboard();

                            final List<String> buttons = phones ;
                            BottomPopupWindow popupWindow = new BottomPopupWindow(mContext, buttons);
                            popupWindow.setClickListener(new BottomPopupWindow.OnButtonClickListener() {
                                @Override
                                public void onClick(View button, int position) {
                                    String text = buttons.get(position);

                                    if(text.equals("Cancel"))
                                        return;
                                    else if (text == "Ginko Video Call") {
                                        EventUser newUser = new EventUser();
                                        newUser.setFirstName(item.entityOrContactName);
                                        newUser.setLastName("");
                                        newUser.setPhotoUrl(item.getProfileImage());
                                        newUser.setUserId(item.contactOrEntityID);
                                        CreateVideoVoiceConferenceBoard(String.valueOf(item.contactOrEntityID), newUser, 1);
                                    } else if (text == "Ginko Voice Call") {
                                        EventUser newUser = new EventUser();
                                        newUser.setFirstName(item.entityOrContactName);
                                        newUser.setLastName("");
                                        newUser.setPhotoUrl(item.getProfileImage());
                                        newUser.setUserId(item.contactOrEntityID);
                                        CreateVideoVoiceConferenceBoard(String.valueOf(item.contactOrEntityID), newUser, 2);
                                    } else {
                                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + text));
                                        mContext.startActivity(intent);
                                    }
                                }
                            });

                            popupWindow.show(v);
                        }
                    }
            );
        }else{
            ivPhone.setVisibility(View.INVISIBLE);
        }

        ivChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if(isListSelectable) return;
                Intent intent = new Intent(mContext,ImBoardActivity.class);
                intent.putExtra("contact_name", item.entityOrContactName);
                intent.putExtra("contact_ids", item.contactOrEntityID + "");
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public void refreshView()
    {
        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        imageView.setDefaultImageResId(R.drawable.no_face);
        if(item.profile_image != null)
            imageView.setImageUrl(item.profile_image , imgLoader);

        tvName.setText(item.entityOrContactName);
        txtFoundTime.setText(item.foundTime);

        if (isListSelectable)
            imageSelectionCheck.setVisibility(View.VISIBLE);
        else
            imageSelectionCheck.setVisibility(View.GONE);

        if (item.nSharingStatus == 4){
            ivPhone.setVisibility(View.GONE);
            ivEdit.setVisibility(View.VISIBLE);

        }else{
            ivPhone.setVisibility(View.VISIBLE);
            ivEdit.setVisibility(View.GONE);
        }

        if (phones != null && phones.size()!=0) {

        }else{
            ivPhone.setVisibility(View.INVISIBLE);
        }
        txtAddress.setText(item.strAddress);
    }

    public void CreateVideoVoiceConferenceBoard(String userIds, final EventUser candidate, final int callType) {
        IMRequest.createBoard(String.valueOf(userIds), new ResponseCallBack<ImBoardVO>() {
            @Override
            public void onCompleted(JsonResponse<ImBoardVO> response) {
                if (response.isSuccess()) {
                    ImBoardVO board = response.getData();
                    int boardId = board.getBoardId();

                    EventUser ownUser = new EventUser();
                    ownUser.setFirstName(RuntimeContext.getUser().getFirstName());
                    ownUser.setLastName(RuntimeContext.getUser().getLastName());
                    ownUser.setPhotoUrl(RuntimeContext.getUser().getPhotoUrl());
                    ownUser.setUserId(RuntimeContext.getUser().getUserId());

                    ArrayList<EventUser> listTemp = new ArrayList<EventUser>();
                    listTemp.add(candidate);
                    listTemp.add(ownUser);

                    MyApp.getInstance().isOwnerForConfernece = true;

                    MyApp.getInstance().initializeVideoVariables();
                    VideoMemberVO currMember = new VideoMemberVO();
                    currMember.setUserId(String.valueOf(RuntimeContext.getUser().getUserId()));
                    currMember.setName(RuntimeContext.getUser().getFirstName());
                    currMember.setOwner(true);
                    currMember.setMe(true);
                    currMember.setWeight(0);
                    currMember.setInitialized(true);
                    if (callType == 1)
                        currMember.setVideoStatus(true);
                    else
                        currMember.setVideoStatus(false);
                    currMember.setVoiceStatus(true);

                    MyApp.getInstance().g_currMemberCon = currMember;

                    VideoMemberVO otherMember = new VideoMemberVO();
                    otherMember.setUserId(String.valueOf(candidate.getUserId()));
                    otherMember.setName(candidate.getFirstName());
                    otherMember.setImageUrl(candidate.getPhotoUrl());
                    otherMember.setOwner(false);
                    otherMember.setMe(false);
                    otherMember.setWeight(1);
                    otherMember.setYounger(true);
                    otherMember.setInitialized(true);
                    if (callType == 1)
                        otherMember.setVideoStatus(true);
                    else
                        otherMember.setVideoStatus(false);
                    otherMember.setVoiceStatus(true);

                    MyApp.getInstance().g_videoMemberList.add(currMember);
                    MyApp.getInstance().g_videoMemberList.add(otherMember);
                    MyApp.getInstance().g_videoMemIDs.add(currMember.getUserId());
                    MyApp.getInstance().g_videoMemIDs.add(otherMember.getUserId());

                    Intent groupVideoIntent = new Intent(mContext, GroupVideoChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("boardId", boardId);
                    bundle.putInt("callType", callType);
                    bundle.putString("conferenceName", otherMember.getName());
                    bundle.putSerializable("userData", listTemp);
                    bundle.putBoolean("isInitial", true);
                    groupVideoIntent.putExtras(bundle);
                    mContext.startActivity(groupVideoIntent);
                }
            }
        });
    }

    public boolean checkSimCard(Context context)
    {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if(telephonyManager.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE) {
            MyApp.getInstance().showSimpleAlertDiloag(context, "No Sim-card", null);
            return false;
        } else {
            int SIM_STATE = telephonyManager.getSimState();

            if (SIM_STATE == TelephonyManager.SIM_STATE_READY)
                return true;
            else {
                switch (SIM_STATE) {
                    case TelephonyManager.SIM_STATE_ABSENT: //SimState = "No Sim Found!";
                        MyApp.getInstance().showSimpleAlertDiloag(context, "No Sim-card", null);
                        break;
                    case TelephonyManager.SIM_STATE_NETWORK_LOCKED: //SimState = "Network Locked!";
                        break;
                    case TelephonyManager.SIM_STATE_PIN_REQUIRED: //SimState = "PIN Required to access SIM!";
                        break;
                    case TelephonyManager.SIM_STATE_PUK_REQUIRED: //SimState = "PUK Required to access SIM!"; // Personal Unblocking Code
                        break;
                    case TelephonyManager.SIM_STATE_UNKNOWN: //SimState = "Unknown SIM State!";
                        MyApp.getInstance().showSimpleAlertDiloag(context, "No Sim-card", null);
                        break;
                }
                return false;
            }
        }
    }
}
