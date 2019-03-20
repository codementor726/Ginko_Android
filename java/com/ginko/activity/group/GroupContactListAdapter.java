package com.ginko.activity.group;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.contact.IndexCursor;
import com.ginko.activity.entity.ViewEntityPostsActivity;
import com.ginko.activity.im.GroupVideoChatActivity;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.api.request.IMRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.utils.ViewIdGenerator;
import com.ginko.view.ext.SelectableListAdapter;
import com.ginko.vo.EventUser;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.VideoMemberVO;
import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupContactListAdapter extends SelectableListAdapter<ContactItem> implements
        PinnedSectionListAdapter,SectionIndexer {

    private Context mContext;

    private boolean isContactTileStyle = true;
    private boolean isDirectory = false;

    private boolean isListSelectable = false;
    private List<ContactItem> contactList;
    private HashMap<String , Integer> sectionNameItemsMap =  new HashMap<String , Integer>();

    public GroupContactListAdapter(Context context) {
        super(context);
        this.mContext = context;
        alphabetIndexer = new AlphabetIndexer(new IndexCursor(this), 0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        isContactTileStyle = Uitils.getIsContactTileStyle(mContext);
    }

    public void setListIsSeletable(boolean _isListSelectable)
    {
        this.isListSelectable = _isListSelectable;
        if(!this.isListSelectable)
            unSelect2();
    }

    public void setContactListItemStyle(boolean _isContactTileStyle)
    {
        this.isContactTileStyle = _isContactTileStyle;
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
        if(contactList == null)
            contactList = new ArrayList<ContactItem>();
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

    public int getVisibleCount() {
        int totalCnt = 0;
        if (contactList == null)
            return  0;

        for(int i=0;i<contactList.size();i++)
        {
            if (isItemVisible(i)) totalCnt++;
        }

        return totalCnt;
    }

    public List<ContactItem> getVisibleItems() {
        List<ContactItem> result = new ArrayList<ContactItem>();
        if (contactList == null)
            return  null;

        for(int i=0;i<contactList.size();i++)
            result.add((ContactItem) getItem(i));

        return result;
    }

    @Override
    public ContactItem getItem(int position) {
        if(contactList == null) return null;
        if(position>=contactList.size())
            return  null;
        return contactList.get(position);
    }

    public boolean isSectionView(int position)
    {
        int type = getItemViewType(position);
        if(type == ContactItem.SECTION)
            return true;

        return false;
    }

    public void setIsDirectory(boolean _directory)
    {
        isDirectory = _directory;
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

    public void searchItems(String searchKeyword)
    {
        //contactList = getAll();
        if(searchKeyword.compareTo("") != 0)
        {
            for(int i=0;i<contactList.size();i++)
            {
                if(isSectionView(i))
                    showItem(i , false);
                else
                {
                    ContactItem item = contactList.get(i);
                    if(item.getFullName().trim().toLowerCase().contains(searchKeyword))
                        showItem(i , true);
                    else
                        showItem(i , false);
                }
            }
        }
        else
        {
            for(int i=0;i<contactList.size();i++)
            {
                showItem(i , true);
            }
        }
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
                view = new SectionView(mContext , item);
            else
                view = new SectionChildItemView(mContext , item);
        }
        else
        {
            view = (ItemView)convertView;
        }

        if(type != ContactItem.SECTION) {
            //check box
            ImageView imgCheck = (ImageView) view.findViewById(R.id.imgCheckBox);
            if(imgCheck != null) {
                if (isListSelectable)
                    imgCheck.setVisibility(View.VISIBLE);
                else
                    imgCheck.setVisibility(View.GONE);

                if (isSelected(position))
                    imgCheck.setImageResource(R.drawable.chatmessage_selected);
                else
                    imgCheck.setImageResource(R.drawable.chatmessage_nonsel);
            }
        }


        if(isItemVisible(position))
        {
            view.findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
        }
        else
        {
            view.findViewById(R.id.rootLayout).setVisibility(View.GONE);
        }

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
        return item==null?0:getItem(position).getType();
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

        public SectionView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub

        }
        public SectionView(Context context,  ContactItem _item) {
            super(context);
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

        private ImageLoader imgLoader;

        private int contactId;
        private List<String> phones = new ArrayList<String>();
        private List<String> emails = new ArrayList<String>();
        private int contactType = 1;
        private String strContactShowName = "" , fullName = "";

        public SectionChildItemView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub

        }
        public SectionChildItemView(Context context,  ContactItem _item)
        {
            super(context);
            setItem(_item);
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if(imgLoader == null)
                imgLoader = MyApp.getInstance().getImageLoader();


            if(isContactTileStyle)
                inflater.inflate(R.layout.contact_tile_style_list_item, this, true);
            else
                inflater.inflate(R.layout.contact_list_style_item, this, true);

            setId(ViewIdGenerator.generateViewId());

            txtContactName = (TextView) findViewById(R.id.txtContactName);
            emailIcon = (ImageView) findViewById(R.id.email_icon);
            phoneIcon = (ImageView) findViewById(R.id.phone_icon);
            profileImage = (NetworkImageView) findViewById(R.id.profileImage);
            imgIsNew = (ImageView)findViewById(R.id.imgNewIcon);

            imgCheckBox = (ImageView)findViewById(R.id.imgCheckBox);

            if (item.getContactId() == MyApp.getInstance().getUserId())
            {
                phoneIcon.setVisibility(View.GONE);
                emailIcon.setVisibility(View.GONE);
            }

            phoneIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CollectionUtils.isEmpty(phones) || item.getSharingStatus() == 4){
                        Intent contactSharingSettingIntent = new Intent(mContext, ShareYourLeafActivity.class);
                        contactSharingSettingIntent.putExtra("contactID" , String.valueOf(contactId));
                        contactSharingSettingIntent.putExtra("contactFullname" , item.getFullName());
                        contactSharingSettingIntent.putExtra("isUnexchangedContact" , false);
                        contactSharingSettingIntent.putExtra("isInviteContact" , true);
                        contactSharingSettingIntent.putExtra("isPendingRequest" , true);
                        mContext.startActivity(contactSharingSettingIntent);
                        return;
                    }
                    final List<String> buttons = phones;
                    final BottomPopupWindow popupWindow = new BottomPopupWindow(mContext, buttons);

                    popupWindow.setClickListener(new BottomPopupWindow.OnButtonClickListener() {
                        @Override
                        public void onClick(View button, int position) {
                            String text = buttons.get(position);
                            if (text.equals("Cancel"))
                                popupWindow.dismiss();
                            else if (text == "Ginko Video Call") {
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
                            }
                            else {
                                if (!checkSimCard(mContext))
                                    return;
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + text));
                                mContext.startActivity(intent);
                            }
                        }
                    });
                    popupWindow.show(v);
                }
            });


            emailIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(contactType == 1) //purple contact
                    {
                        Intent intent = new Intent(mContext, ImBoardActivity.class);
                        intent.putExtra("contact_name", fullName);
                        intent.putExtra("contact_ids", contactId + "");
                        mContext.startActivity(intent);
                    }
                    else if(contactType == 2)//grey contact
                    {
                        return;
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

            if (contactType == 3 && StringUtils.isEmpty(item.getProfileImage())){
                profileImage.setDefaultImageResId(R.drawable.entity_dummy);
                profileImage.setImageUrl(item.getProfileImage(), imgLoader);
            }else{
                profileImage.setDefaultImageResId(R.drawable.no_face);
                profileImage.setImageUrl( item.getProfileImage() , imgLoader);
            }

            if (contactType == 1)//purple contact
            {
                if (item.getSharingStatus() == 4 || CollectionUtils.isEmpty(phones))
                    phoneIcon.setImageResource(R.drawable.editcontact);
                else
                    phoneIcon.setImageResource(R.drawable.btnphone);

                emailIcon.setImageResource(R.drawable.btnchat_70);

            } else if (contactType == 2)//grey contact
            {
                //grey contact
                profileImage.setBorderColor(mContext.getResources().getColor(R.color.grey_contact_color));
                profileImage.setDefaultImageResId(R.drawable.no_face_grey);
                profileImage.setImageUrl(item.getProfileImage(), imgLoader);
                profileImage.invalidate();
                txtContactName.setTextColor(0xff4d4d4d);
                phoneIcon.setImageResource(R.drawable.btnphonegrey);

                if (CollectionUtils.isEmpty(emails)){
                    emailIcon.setImageResource(R.drawable.mail_2x);
                }else{
                    emailIcon.setImageResource(R.drawable.btnmailgrey);
                }
            } else {
                //entity
                emailIcon.setImageResource(R.drawable.btn_wall_70);
            }

            txtContactName.setText(this.strContactShowName);

            if (isDirectory)
                imgIsNew.setVisibility(View.GONE);
            else
            {
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
    }

    private abstract class ItemView<T> extends LinearLayout{
        public Context mContext;
        public ItemView(Context context)
        {
            super(context);
            this.mContext = context;
        }

        public ItemView(Context context , T item)
        {
            super(context);
            this.mContext = context;
        }

        public void setItem(T obj){};
        public void refreshView(){}
    }
}
