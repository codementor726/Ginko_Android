package com.ginko.activity.contact;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.load.resource.bitmap.ImageHeaderParser;
import com.ginko.activity.directory.DirAdminPreviewActivity;
import com.ginko.activity.entity.ViewEntityPostsActivity;
import com.ginko.activity.im.GroupVideoChatActivity;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.activity.profiles.UserEntityProfileActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.IMRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ContactStruct;
import com.ginko.database.ContactTableModel;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.imagecrop.Util;
import com.ginko.utils.ViewIdGenerator;
import com.ginko.vo.EventUser;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.UserLoginVO;
import com.ginko.vo.VideoMemberVO;
import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactListAdapter extends BaseAdapter implements
        PinnedSectionListAdapter,SectionIndexer {

    private Context mContext;

    private List<ContactItem> contactList;
    private HashMap<String , Integer> sectionNameItemsMap =  new HashMap<String , Integer>();

    private boolean isContactTileStyle = true;

    public ContactListAdapter(Context context) {
        this.mContext = context;
        contactList = new ArrayList<ContactItem>();
        alphabetIndexer = new AlphabetIndexer(new IndexCursor(this), 0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        isContactTileStyle = Uitils.getIsContactTileStyle(mContext);
    }

    public void setContactListItemStyle(boolean _isContactTileStyle)
    {
        this.isContactTileStyle = _isContactTileStyle;
    }

    public void add(ContactItem item)
    {
        if(contactList == null)
            contactList = new ArrayList<ContactItem>();
        if(item.isSection())
        {
            sectionNameItemsMap.put(item.getSectionName() , contactList.size());
        }
        contactList.add(item);
    }
    public synchronized void addAll(List<ContactItem> items)
    {
        contactList = items;
        if(sectionNameItemsMap == null)
            sectionNameItemsMap =  new HashMap<String , Integer>();
        else
        {
            try
            {
                sectionNameItemsMap.clear();
            }catch(Exception e){
                e.printStackTrace();
                sectionNameItemsMap =  new HashMap<String , Integer>();
            }
        }

        for(int i=0;i<contactList.size();i++)
        {
            if(contactList.get(i).isSection()) {
                sectionNameItemsMap.put(contactList.get(i).getSectionName() , i);
            }
        }

        notifyDataSetChanged();
    }
    public void clear()
    {
        if(contactList != null)
            contactList.clear();
        else
            contactList = new ArrayList<ContactItem>();

        if(sectionNameItemsMap == null)
            sectionNameItemsMap =  new HashMap<String , Integer>();
        else
        {
            try
            {
                sectionNameItemsMap.clear();
            }catch(Exception e){
                e.printStackTrace();
                sectionNameItemsMap =  new HashMap<String , Integer>();
            }
        }
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
    public int getSectionItemIndex(String sectionName)
    {
        if(sectionNameItemsMap.containsKey(sectionName))
            return sectionNameItemsMap.get(sectionName);
        return -1;
    }

    public void forceNewlyClear()
    {
        try
        {
            contactList.clear();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            contactList = null;
        }

        contactList = new ArrayList<ContactItem>();

    }



    public void clearAdpater()
    {
        contactList = new ArrayList<ContactItem>();
    }


    @Override
    public int getCount() {
        return contactList == null?0:contactList.size();
    }

    @Override
    public ContactItem getItem(int position) {
        if(contactList == null) return null;
        if(position>=contactList.size())
            return  null;
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactItem item = (ContactItem) getItem(position);
        if(item == null) return null;

        int type = getItemViewType(position);

        ItemView view = null;
        if (convertView == null) {

            if(type == ContactItem.SECTION)
                view = new SectionView(mContext , item , isContactTileStyle);
            else
                view = new SectionChildItemView(mContext , item , isContactTileStyle);
        }
        else
        {
            view = (ItemView)convertView;

            //if current itemview is still has old tile style view , then force to recreate them
            if(type == ContactItem.ITEM && view.isTileStyleItemView() != isContactTileStyle)
            {
                view = new SectionChildItemView(mContext , item , isContactTileStyle);
            }
        }

        //check box
        /*if(isItemVisible(position))
        {
            view.findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
        }
        else
        {
            view.findViewById(R.id.rootLayout).setVisibility(View.GONE);
        }*/

        view.setIsTileStyleView(isContactTileStyle);
        view.setItem(item);
        view.refreshView();
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ContactItem item = getItem(position);
        if(item == null) return 0;
        return item.getType() == ContactItem.SECTION?1:0;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == ContactItem.SECTION;
    }

    private AlphabetIndexer alphabetIndexer;
    @Override
    public Object[] getSections() {
        return alphabetIndexer.getSections();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return alphabetIndexer.getPositionForSection(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        return alphabetIndexer.getSectionForPosition(position);
    }

    private class SectionView extends ItemView<ContactItem> {
        private LayoutInflater inflater = null;
        private ContactItem item;
        private TextView txtSectionName;

        public SectionView(Context context , boolean isTileStyle) {
            super(context , isTileStyle);
            // TODO Auto-generated constructor stub

        }
        public SectionView(Context context,  ContactItem _item , boolean isTileStyle) {
            super(context , isTileStyle);
            this.mContext = context;
            item = _item;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            inflater.inflate(R.layout.contact_section, this, true);
            setId(ViewIdGenerator.generateViewId());

            txtSectionName = (TextView) findViewById(R.id.list_item_title);
        }

        @Override
        public void setItem(ContactItem _item)
        {
            this.item = _item;
        }

        @Override
        public void refreshView()
        {
            txtSectionName.setText(item.getSectionName());
        }
    }

    private class SectionChildItemView extends ItemView<ContactItem> {
        private LayoutInflater inflater = null;
        private ContactItem item;
        private NetworkImageView profileImage;
        private TextView txtContactName;
        private ImageView emailIcon , phoneIcon;
        private ImageView imgIsNew;
        private ImageView imgCheckBox;
        private RelativeLayout contact_list_info;

        private ImageLoader imgLoader;

        private int contactId;
        private List<String> phones = new ArrayList<String>();
        private List<String> emails = new ArrayList<String>();
        private int contactType = 1;
        private String strContactShowName = "" , fullName = "";

        public SectionChildItemView(Context context , boolean isTileStyle) {
            super(context , isTileStyle);
            // TODO Auto-generated constructor stub

        }
        public SectionChildItemView(final Context context,  ContactItem _item , boolean isTileStyle)
        {
            super(context , isTileStyle);
            setItem(_item);
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if(imgLoader == null)
                imgLoader = MyApp.getInstance().getImageLoader();


            if(isTileStyleItemView())
                inflater.inflate(R.layout.contact_tile_style_list_item, this, true);
            else
                inflater.inflate(R.layout.contact_list_style_item, this, true);

            setId(ViewIdGenerator.generateViewId());

            contact_list_info = (RelativeLayout)findViewById(R.id.contact_list_info);
            txtContactName = (TextView) findViewById(R.id.txtContactName);
            emailIcon = (ImageView) findViewById(R.id.email_icon);
            phoneIcon = (ImageView) findViewById(R.id.phone_icon);
            profileImage = (NetworkImageView) findViewById(R.id.profileImage);
            imgIsNew = (ImageView)findViewById(R.id.imgNewIcon);
            imgCheckBox = (ImageView)findViewById(R.id.imgCheckBox);
            imgCheckBox.setVisibility(View.GONE);

            phoneIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!item.getIsRead() && item.getContactType() != 3) {
                        item.setIsRead(true);
                        notifyDataSetChanged();
                        UserRequest.readContact(new Integer(item.getContactId()), item.getContactType(), new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                }
                            }
                        }, false);

                    }

                    if (item.getContactType() == 1 && item.getSharingStatus() == 4)//edit contact icon
                    {
                        Intent contactSharingSettingIntent = new Intent(mContext, ShareYourLeafActivity.class);
                        contactSharingSettingIntent.putExtra("contactID", String.valueOf(contactId));
                        contactSharingSettingIntent.putExtra("contactFullname", item.getFullName());
                        contactSharingSettingIntent.putExtra("isUnexchangedContact", false);
                        contactSharingSettingIntent.putExtra("isInviteContact", true);
                        contactSharingSettingIntent.putExtra("isPendingRequest", true);
                        mContext.startActivity(contactSharingSettingIntent);
                    } else {
                        // Add by lee for GAD-890
                        if (item.getContactType() == 3 || phones == null) {
                            ContactStruct struct = MyApp.getInstance().getContactsModel().getContactById(contactId);
                            String jsonValue = struct.getJsonValue();
                            if (jsonValue != null && jsonValue.compareTo("") != 0) {
                                try {
                                    JSONObject jsonObject = new JSONObject(jsonValue);
                                    JSONArray jsonArray = jsonObject.getJSONArray("phones");
                                    final List<String> m_phones = new ArrayList<String>();
                                    if (jsonArray != null) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            if (jsonArray.optString(i).contains("@"))
                                                continue;
                                            m_phones.add(jsonArray.optString(i));

                                        }
                                    }
                                    phones = m_phones;
                                    phones.add("Cancel");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if (item.getContactType() == 1)
                            {
                                if (!phones.contains("Ginko Video Call"))
                                    phones.add(0, "Ginko Video Call");
                                if (!phones.contains("Ginko Voice Call"))
                                    phones.add(0, "Ginko Voice Call");
                            }
                        }
                        /////////////////////////////////////////////////////////////////
                        if (phones.size() == 1) {
                            Uitils.alert("Oops! No registered phone numbers.");
                            return;
                        }
                        if (phones.size() > 2) {
                            //if (!checkSimCard(context))
                            //    return;
                            final List<String> buttons = phones;
                            final BottomPopupWindow popupWindow = new BottomPopupWindow(mContext, buttons);
                            popupWindow.setClickListener(new BottomPopupWindow.OnButtonClickListener() {
                                @Override
                                public void onClick(View button, int position) {
                                    String text = buttons.get(position);
                                    if (text == "Cancel") {
                                        popupWindow.dismiss();
                                    } else if (text == "Ginko Video Call") {
                                        EventUser newUser = new EventUser();
                                        newUser.setFirstName(item.getFirstName());
                                        newUser.setLastName(item.getLastName());
                                        newUser.setPhotoUrl(item.getProfileImage());
                                        newUser.setUserId(item.getContactId());
                                        CreateVideoVoiceConferenceBoard(String.valueOf(item.getContactId()), newUser, 1);
                                    } else if (text == "Ginko Voice Call") {
                                        EventUser newUser = new EventUser();
                                        newUser.setFirstName(item.getFirstName());
                                        newUser.setLastName(item.getLastName());
                                        newUser.setPhotoUrl(item.getProfileImage());
                                        newUser.setUserId(item.getContactId());
                                        CreateVideoVoiceConferenceBoard(String.valueOf(item.getContactId()), newUser, 2);
                                    } else {
                                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + text));
                                        mContext.startActivity(intent);
                                    }
                                }
                            });
                            popupWindow.show(v);
                        } else {
                            if (!checkSimCard(context))
                                return;
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phones.get(0)));
                            mContext.startActivity(intent);
                        }
                    }
                }
            });

            emailIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!item.getIsRead() && item.getContactType() != 3) {
                        item.setIsRead(true);
                        notifyDataSetChanged();
                        UserRequest.readContact(new Integer(item.getContactId()), item.getContactType(), new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                }
                            }
                        }, false);

                    }

                    if(contactType == 1) //purple contact
                    {
                        Intent intent = new Intent(mContext, ImBoardActivity.class);
                        intent.putExtra("contact_name", fullName);
                        intent.putExtra("contact_ids", contactId + "");
                        if (item.getContactType() == 1 && item.getSharingStatus() == 4)
                            intent.putExtra("isChatOnly", true);
                        mContext.startActivity(intent);
                    }
                    else if(contactType == 2)//grey contact
                    {
                        if(CollectionUtils.isEmpty(emails)) {
                            Uitils.alert("Oops! No registered emails!");
                            return;
                        }
                        if(emails.size()>1) {
                            final String[] emailArray = emails.toArray(new String[emails.size()]);
                            new AlertDialog.Builder(mContext)
                                    .setSingleChoiceItems(emailArray, 0, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            try {
                                                Intent intent = new Intent(Intent.ACTION_SEND);
                                                intent.setType("text/plain");
                                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailArray[which]});
                                                intent.putExtra(Intent.EXTRA_SUBJECT, "");//subject
                                                intent.putExtra(Intent.EXTRA_TEXT, "\n\n\n" + getResources().getString(R.string.str_send_email_bottom_suffix));

                                                mContext.startActivity(Intent.createChooser(intent, "Send Email"));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    })
                                    .setPositiveButton(R.string.str_confirm_dialog_cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();

                                            // Do something useful withe the position of the selected radio button
                                        }
                                    })
                                    .show();
                        }
                        else
                        {
                            final String[] emailArray = emails.toArray(new String[emails.size()]);
                            try {
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailArray[0]});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "");//subject
                                intent.putExtra(Intent.EXTRA_TEXT, "\n\n\n" + getResources().getString(R.string.str_send_email_bottom_suffix));

                                mContext.startActivity(Intent.createChooser(intent, "Send Email"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else//entity
                    {
                        Intent viewEntityPost = new Intent(mContext , ViewEntityPostsActivity.class);
                        viewEntityPost.putExtra("entityName" , fullName);
                        viewEntityPost.putExtra("entityId" , contactId);
                        viewEntityPost.putExtra("profileImage" , item.getProfileImage());
                        viewEntityPost.putExtra("isfollowing_entity" , true);
                        mContext.startActivity(viewEntityPost);
                    }
                }
            });
        }

        @Override
        public void setItem(ContactItem _item)
        {
            this.item = _item;

            this.contactId = item.getContactId();
            this.phones = item.getPhones();
            this.emails = item.getEmails();
            this.contactType = item.getContactType();
            String name = item.getFirstName();
            String contactName = item.getFirstName();
            if (StringUtils.isNotBlank(item.getMiddleName())) {
                name += " " + item.getMiddleName();
                contactName += " " + item.getMiddleName();
            }
            if (StringUtils.isNotBlank(item.getLastName())) {
                name += " " + item.getLastName();
                contactName += "\n" + item.getLastName();
            }

            this.fullName = name;
            this.strContactShowName = contactName;
        }

        @Override
        public void refreshView()
        {
            if(imgLoader == null)
                imgLoader = MyApp.getInstance().getImageLoader();

            boolean isTileStyleItemView = isTileStyleItemView();

            if (contactType == 3){
                profileImage.setBorderColor(mContext.getResources().getColor(R.color.purple_contact_color));
                profileImage.setDefaultImageResId(R.drawable.entity_dummy);
                //profileImage.setBackgroundResource(R.drawable.entity_dummy);
                profileImage.setImageUrl(item.getProfileImage(), imgLoader);

                profileImage.invalidate();
                /*Glide.with(getContext()).load(item.getProfileImage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.IMMEDIATE)
                        .bitmapTransform(new CropCircleTransformation(getContext()))
                        .into(profileImage);*/
                if(isTileStyleItemView)
                    contact_list_info.setBackgroundColor(0x3f7e5785);
                txtContactName.setTextColor(0xff7e5785);
            }else if(contactType == 2)//grey contact
            {
                profileImage.setBorderColor(mContext.getResources().getColor(R.color.grey_contact_color));
                profileImage.setDefaultImageResId(R.drawable.no_face_grey);
                //profileImage.setBackgroundResource(R.drawable.grey_photo);
                profileImage.setImageUrl(item.getProfileImage(), imgLoader);
                profileImage.invalidate();
                /*Glide.with(getContext()).load(item.getProfileImage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.IMMEDIATE)
                        .bitmapTransform(new CropCircleTransformation(getContext()))
                        .into(profileImage);*/

                if(isTileStyleItemView)
                    contact_list_info.setBackgroundColor(0x3fc9c9c9);
                txtContactName.setTextColor(0xff4d4d4d);
            }
            else if(contactType == 1)//purple contact
            {
                profileImage.setBorderColor(mContext.getResources().getColor(R.color.purple_contact_color));
                profileImage.setDefaultImageResId(R.drawable.no_face);
                profileImage.setImageUrl( item.getProfileImage() , imgLoader);
                profileImage.invalidate();

                /*
                Glide.with(getContext()).load(item.getProfileImage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.IMMEDIATE)
                        .bitmapTransform(new CropCircleTransformation(getContext()))
                        .into(profileImage); */

                if(isTileStyleItemView)
                    contact_list_info.setBackgroundColor(0x3f7e5785);
                txtContactName.setTextColor(0xff7e5785);
            }

            /*if (CollectionUtils.isEmpty(phones)){
                phoneIcon.setImageResource(R.drawable.phone_sel_70);
            }else{
                phoneIcon.setImageResource(R.drawable.phone_70);
            }*/

            if (contactType == 1)//purple contact
            {
                if(item.getSharingStatus() != 4)
                {
                    phoneIcon.setImageResource(R.drawable.btnphone);
                }
                else
                {
                    phoneIcon.setImageResource(R.drawable.editcontact);
                }
                emailIcon.setImageResource(R.drawable.btnchat_70);

            } else if (contactType == 2)//grey contact
            {
                //grey contact
                /*if (CollectionUtils.isEmpty(emails)){
                    emailIcon.setImageResource(R.drawable.mail_2x);
                }else{
                    emailIcon.setImageResource(R.drawable.email_70);
                }*/
                emailIcon.setImageResource(R.drawable.btnmailgrey);
                phoneIcon.setImageResource(R.drawable.btnphonegrey);
            } else {
                //entity
                phoneIcon.setImageResource(R.drawable.btnphone);
                emailIcon.setImageResource(R.drawable.btn_wall_70);
            }

            txtContactName.setText(this.strContactShowName);

            if(item.getContactType() == 3) {
                imgIsNew.setVisibility(View.INVISIBLE);
            }
            else {
                if (item.getIsRead())
                    imgIsNew.setVisibility(View.INVISIBLE);
                else
                    imgIsNew.setVisibility(View.VISIBLE);
            }
        }
    }

    private abstract class ItemView<T> extends LinearLayout{
        public Context mContext;
        private boolean isTileStyleItemView = false;
        public ItemView(Context context , boolean isTileStyle)
        {
            super(context);
            this.mContext = context;
            this.isTileStyleItemView = isTileStyle;
        }

        public ItemView(Context context , T item)
        {
            super(context);
            this.mContext = context;
        }

        public void setIsTileStyleView(boolean isTileStyle){this.isTileStyleItemView = isTileStyle;}
        public boolean isTileStyleItemView(){return  this.isTileStyleItemView;}

        public void setItem(T obj){};
        public void refreshView(){}
    }

    /*
    public class CropCircleTransformation implements Transformation<Bitmap> {

        private BitmapPool mBitmapPool;

        public CropCircleTransformation(Context context) {
            this(Glide.get(context).getBitmapPool());
        }

        public CropCircleTransformation(BitmapPool pool) {
            this.mBitmapPool = pool;
        }

        @Override
        public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
            Bitmap source = resource.get();
            int size = Math.min(source.getWidth(), source.getHeight());

            int width = (source.getWidth() - size) / 2;
            int height = (source.getHeight() - size) / 2;

            Bitmap bitmap = mBitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader =
                    new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            if (width != 0 || height != 0) {
                // source isn't square, move viewport to center
                Matrix matrix = new Matrix();
                matrix.setTranslate(-width, -height);
                shader.setLocalMatrix(matrix);
            }
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            return BitmapResource.obtain(bitmap, mBitmapPool);
        }

        @Override public String getId() {
            return "CropCircleTransformation()";
        }
    }
    */
}
