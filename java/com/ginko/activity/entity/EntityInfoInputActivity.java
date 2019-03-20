package com.ginko.activity.entity;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.profiles.CustomSizeMeasureView;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.ImageButtonTab;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.EntityInfoEditFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.utils.ImageScalingUtilities;
import com.ginko.vo.EntityInfoDetailVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;

public class EntityInfoInputActivity extends MyBaseFragmentActivity implements OnClickListener,
                                                                        CustomSizeMeasureView.OnMeasureListner,
                                                                        ActionSheet.ActionSheetListener
{

    private final String[] strFieldNames = {
            "Name",
            "Keysearch",
            "Address",
            "Address#2",
            "Hours",
            "Mobile",
            "Mobile#2",
            "Mobile#3",
            "Phone",
            "Phone#2",
            "Phone#3",
            "Fax",
            "Email",
            "Email#2",
            "Facebook",
            "Twitter",
            "LinkedIn",
            "Website",
            "Custom",
            "Custom#2",
            "Custom#3",
    };

    private final String[] strFieldTypes = {
            "name",
            "keysearch",
            "address",
            "address",
            "hours",
            "phone",
            "phone",
            "phone",
            "phone",
            "phone",
            "phone",
            "fax",
            "email",
            "email",
            "facebook",
            "twitter",
            "linkedin",
            "url",
            "custom",
            "custom",
            "custom",
    };
    private final int TAKE_PHOTO_FROM_CAMERA = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;

    /* UI Elements*/
    private RelativeLayout rootLayout;
    private LinearLayout popupRootLayout;

    private CustomSizeMeasureView sizeMeasureView;

    private ViewPager mPager;
    private ImageButton btnConfirm;
    private ImageView btnEdit , btnDelete , btnClose;
    private ImageView btnAddLocation , btnRemoveLocation , btnLock , btnAddInfo;
    private LinearLayout profileSnapshotLayout;
    private NetworkImageView imgPhoto;
    private TextView txtTitle;
    private TextView textBoundMeasureView;

    /*Variables */
	private MyOnPageChangeListener pageListener;
    private List<EntityInfoVO> entityInfos;
    private HashMap<Integer , EntityInfoEditFragment> fragmentMap;
	private int currIndex = 0;

	private EntityVO entity;

    private MyPagerAdapter pageAdater = null;
    private EntityInfoEditFragment currentFragment = null;

    private int numberOfLocation = 1;

    private boolean isEditable = false;
    private boolean isSharedPrivilege = false;

    private ActionSheet takePhotoActionSheet = null;

    private PopupWindow addInfoPopupWindow = null;
    private View addInfoPopupView = null;
    private ImageView btnAddInfoItemPopupClose , btnAddInfoItemPopupConfirm;
    private ListView addInfoListView;
    private AddInfoListAdapter addInfoListAdapter;

    private int activityHeight = 0 , activityWidth = 0;

    private String strProfileImagePath = "";
    private String strTempPhotoPath = "" , strNewUploadPhotoUrl = "";

    private Uri uri;
    private String tempPhotoUriPath = "";

    private float ratio = 1.0f;

    private ImageLoader imgLoader;

    private HashMap<String, Integer> infoNameMap;
    private List<List<EntityInfoItem>> entityInfoItemsList;

    private boolean isNewEntity = false;

    private List<Typeface> faces ;
    private int deviceWidth = 0 , deviceHeight = 0;


    private OnClickListener snapPhotoClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            if(takePhotoActionSheet == null)
                takePhotoActionSheet = ActionSheet.createBuilder(EntityInfoInputActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo) ,
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery) ,
                                getResources().getString(R.string.home_work_add_info_remove_photo)
                                )
                        .setCancelableOnTouchOutside(true)
                        .setListener(EntityInfoInputActivity.this)
                        .show();
            else
                takePhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
        }
    };



    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entity_info_input);

        if(savedInstanceState != null)
        {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
            this.isNewEntity = savedInstanceState.getBoolean("isNewEntity", false);
        }
        else {
            this.entity = (EntityVO) getIntent().getSerializableExtra("entity");
            this.isNewEntity = getIntent().getBooleanExtra("isNewEntity" , false);
        }

		if (this.entity != null) {
            entityInfos = this.entity.getEntityInfos();
		}
        else
        {
            entity = new EntityVO();
            entity.setId(479);
            entity.setCategoryId(18);
            entity.setName("aaaa");
            entity.setTags("bbb");
            entity.setPrivilege(1);

            entityInfos = this.entity.getEntityInfos();
        }

        if(faces == null)
            faces = MyApp.getInstance().getFontFaces();

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        deviceWidth = size.x;
        deviceHeight = size.y;


        if(this.entity.getPrivilege()!=null)
            this.isSharedPrivilege = this.entity.getPrivilege()>0?true:false;
        else
            this.isSharedPrivilege = false;

        this.infoNameMap = new HashMap<String, Integer>();
        this.entityInfoItemsList = new ArrayList<List<EntityInfoItem>>();
        this.fragmentMap = new HashMap<Integer , EntityInfoEditFragment>();

        for (int i = 0; i < strFieldNames.length; i++) {
            infoNameMap.put(strFieldNames[i], Integer.valueOf(i));
        }
        if (entityInfos.size() == 0) {
			createNewInfo();
		}
        else
        {
            for(int i=0;i<entityInfos.size();i++) {
                createInfoFromOriginalLocations(entityInfos.get(i));
            }
        }

        getUIObjects();
	}

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        textBoundMeasureView = (TextView)findViewById(R.id.textBoundMeasureView);

        sizeMeasureView = (CustomSizeMeasureView)findViewById(R.id.sizeMeasureView);
        sizeMeasureView.setOnMeasureListener(this);

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        popupRootLayout = (LinearLayout)findViewById(R.id.popupRootLayout);


        btnEdit = (ImageView)findViewById(R.id.btnEdit); btnEdit.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnDelete = (ImageView)findViewById(R.id.btnDelete); btnDelete.setOnClickListener(this);
        btnClose = (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        btnAddLocation = (ImageView)findViewById(R.id.btnAddLocation); btnAddLocation.setOnClickListener(this);
        btnRemoveLocation = (ImageView)findViewById(R.id.btnRemoveLocation); btnRemoveLocation.setOnClickListener(this);
        btnLock = (ImageView)findViewById(R.id.btnLock); btnLock.setOnClickListener(this);
        btnAddInfo = (ImageView)findViewById(R.id.btnAddInfo); btnAddInfo.setOnClickListener(this);

        profileSnapshotLayout = (LinearLayout)findViewById(R.id.profileSnapshotLayout); profileSnapshotLayout.setOnClickListener(snapPhotoClickListener);

        txtTitle = (TextView)findViewById(R.id.txtTitle);
        if(entity.getEntityInfos().size()<=1)
        {
           txtTitle.setText(getResources().getString(R.string.entity_info));
        }
        else
        {
            txtTitle.setText(getResources().getString(R.string.entity_info)+" + "+String.valueOf(entity.getEntityInfos().size()-1));
        }

        numberOfLocation = entity.getEntityInfos().size();

        addInfoPopupView = getLayoutInflater().inflate(R.layout.grey_contact_profile_add_info_popup, null);
        btnAddInfoItemPopupClose = (ImageView)addInfoPopupView.findViewById(R.id.btnAddInfoPopupClose);btnAddInfoItemPopupClose.setOnClickListener(this);
        btnAddInfoItemPopupConfirm = (ImageView)addInfoPopupView.findViewById(R.id.btnAddInfoPopupConfirm);btnAddInfoItemPopupConfirm.setOnClickListener(this);
        addInfoListView = (ListView)addInfoPopupView.findViewById(R.id.infoList);

        imgPhoto = (NetworkImageView)findViewById(R.id.imgPhoto); imgPhoto.setOnClickListener(snapPhotoClickListener);
        imgPhoto.setDefaultImageResId(R.drawable.entity_dummy);

        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        if(entity.getProfileImage()!=null && !entity.getProfileImage().equals(""))
            imgPhoto.setImageUrl(entity.getProfileImage() , imgLoader);

        btnDelete.setVisibility(View.INVISIBLE);//hide delete button as default
        if(entityInfos.size()>1)
            btnRemoveLocation.setVisibility(View.VISIBLE);

        updateLockButton();
        this.InitViewPager();
    }

    private void InitViewPager() {
        mPager = (ViewPager) findViewById(R.id.vPager);
        pageAdater = new MyPagerAdapter(this.getSupportFragmentManager(),
                entityInfos);
        pageListener = new MyOnPageChangeListener();
        mPager.setOnPageChangeListener(pageListener);
        mPager.setAdapter(pageAdater);
        mPager.setCurrentItem(0);
        pageListener.onPageSelected(0);

    }

    private void updateUIFromEditable()
    {
        if(isEditable)
        {
            btnDelete.setVisibility(View.VISIBLE);
            btnClose.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.GONE);
        }
        else
        {
            btnDelete.setVisibility(View.GONE);
            btnClose.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
            btnConfirm.setVisibility(View.VISIBLE);
        }

        if(currentFragment!=null) {
            currentFragment.setIsEditable(isEditable);
            currentFragment.updateListView();
        }
    }

    private void updateLockButton()
    {
        if(isSharedPrivilege)
            btnLock.setImageResource(R.drawable.img_bt_unlock);
        else
            btnLock.setImageResource(R.drawable.img_bt_lock);
    }

	private void createNewInfo() {
		EntityInfoVO info = new EntityInfoVO();
        info.setEntityInfoDetails(new ArrayList<EntityInfoDetailVO>());

        entityInfos.add(info);

        List<EntityInfoItem> infoItemList = new ArrayList<EntityInfoItem>();
        //initiate the info item list

        for (int i = 0; i < strFieldNames.length; i++) {
            infoItemList.add(new EntityInfoItem(strFieldNames[i], strFieldTypes[i], "", false));
        }

        List<EntityInfoDetailVO> fields = info.getEntityInfoDetails();

        if (fields.size() == 0)//if already fields are not existing
        {
            //add default value
            infoItemList.get(0).setFieldValue(entity.getName());//set entity name as default value
            infoItemList.get(1).setFieldValue(entity.getTags());//set entity search tags as defaulst value;

            //show the default fields
            infoItemList.get(0).setVisibility(true); //name
            infoItemList.get(1).setVisibility(true); //keysearch
            infoItemList.get(2).setVisibility(true); //Address
            infoItemList.get(4).setVisibility(true); //Hours
            infoItemList.get(7).setVisibility(true); //Phone
            infoItemList.get(11).setVisibility(true); //Email
            infoItemList.get(16).setVisibility(true); //Website

        } else {
            String[] dontShowFields = {"foreground", "background",
                    "privilege", "abbr", "video"};
            for (int i = 0; i < fields.size(); i++) {
                EntityInfoDetailVO field = fields.get(i);
                if (ArrayUtils.contains(dontShowFields,
                        field.getFieldName().toLowerCase())) {
                    continue;
                }
                int listIndex = infoNameMap.get(field.getFieldName());
                if (listIndex < infoItemList.size()) {
                    infoItemList.get(listIndex).setFieldValue(field.getValue());
                    infoItemList.get(listIndex).setVisibility(true);
                }
            }
        }
        entityInfoItemsList.add(infoItemList);

	}

    private void createInfoFromOriginalLocations(EntityInfoVO info) {
        //info.setEntityInfoDetails(new ArrayList<EntityInfoDetailVO>());
        List<EntityInfoItem> infoItemList = new ArrayList<EntityInfoItem>();
        //initiate the info item list

        for (int i = 0; i < strFieldNames.length; i++) {
            infoItemList.add(new EntityInfoItem(strFieldNames[i], strFieldTypes[i], "", false));
        }

        List<EntityInfoDetailVO> fields = info.getEntityInfoDetails();

        if (fields.size() == 0)//if already fields are not existing
        {
            //add default value
            infoItemList.get(0).setFieldValue(entity.getName());//set entity name as default value
            infoItemList.get(1).setFieldValue(entity.getTags());//set entity search tags as defaulst value;

            //show the default fields
            infoItemList.get(0).setVisibility(true); //name
            infoItemList.get(1).setVisibility(true); //keysearch
            infoItemList.get(2).setVisibility(true); //Address
            infoItemList.get(4).setVisibility(true); //Hours
            infoItemList.get(7).setVisibility(true); //Phone
            infoItemList.get(11).setVisibility(true); //Email
            infoItemList.get(16).setVisibility(true); //Website

        } else {
            String[] dontShowFields = {"foreground", "background",
                    "privilege", "abbr", "video"};
            for (int i = 0; i < fields.size(); i++) {
                EntityInfoDetailVO field = fields.get(i);
                if (ArrayUtils.contains(dontShowFields,
                        field.getFieldName().toLowerCase())) {
                    continue;
                }
                int listIndex = infoNameMap.get(field.getFieldName());
                if (listIndex < infoItemList.size()) {
                    infoItemList.get(listIndex).setFieldValue(field.getValue());
                    infoItemList.get(listIndex).setVisibility(true);
                }
            }
        }
        entityInfoItemsList.add(infoItemList);

    }

	private void removeInfo(int position) {
		entityInfos.remove(position);
        entityInfoItemsList.remove(position);
        currIndex --;
        if (currIndex < 0) {
            currIndex = 0;
        }
	}

    private void saveEntityProfilePhoto()
    {
        File photoFile = null;
        try {
            photoFile = new File(new URI(strTempPhotoPath.replaceAll(" ", "%20")));

            if(!photoFile.exists())
                return;

            String zippedPhotoFile = ImageScalingUtilities.decodeFile(photoFile.getAbsolutePath(), 300, 300);
            if(zippedPhotoFile.equalsIgnoreCase(""))
                return;

            photoFile = new File(zippedPhotoFile);

            final String strUploadPhotoPath = photoFile.getAbsolutePath();

            if(!strUploadPhotoPath.equals("") && photoFile.exists())
            {
                EntityRequest.uploadProfileImage(entity.getId() , new File(strUploadPhotoPath), true, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject data = response.getData();
                            try {
                                String photoUrl = data.getString("profile_image");
                                System.out.println("---Uploaded photo url = " + photoUrl + " ----");
                                strProfileImagePath = strTempPhotoPath;
                                strNewUploadPhotoUrl = photoUrl;
                                entity.setProfileImage(photoUrl);
                                imgPhoto.refreshOriginalBitmap();
                                imgPhoto.setImageUrl("file://"+strUploadPhotoPath, imgLoader);
                                imgPhoto.invalidate();
                                strTempPhotoPath = "";

                            } catch (Exception e) {
                                e.printStackTrace();
                                strTempPhotoPath = "";
                            }
                        } else {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";
                            imgPhoto.refreshOriginalBitmap();
                            imgPhoto.setImageUrl(strProfileImagePath, imgLoader);
                            imgPhoto.invalidate();
                            MyApp.getInstance().showSimpleAlertDiloag(EntityInfoInputActivity.this, R.string.str_err_upload_photo, null);
                        }
                    }
                });
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }

    }

    private void enableAddInfoItemPopup()
    {
        // Creating a pop window for emoticons keyboard
        addInfoPopupWindow = new PopupWindow(addInfoPopupView, android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                (int) activityHeight, false);
        addInfoPopupView.setFocusable(true);
        addInfoPopupWindow.setAnimationStyle(R.style.AnimationPopup);
        addInfoPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                popupRootLayout.setVisibility(LinearLayout.GONE);
            }
        });
    }
    private void showHidePopupView(PopupWindow popupWindow , boolean bShown)
    {
        if(popupWindow == null) return;
        if (!popupWindow.isShowing()) {
            popupWindow.setHeight((int) (activityHeight));
            if (bShown) {
                popupRootLayout.setVisibility(LinearLayout.GONE);
            } else {
                popupRootLayout.setVisibility(LinearLayout.VISIBLE);
            }
            popupWindow.showAtLocation(popupRootLayout, Gravity.BOTTOM, 0, 0);
            if(popupWindow == addInfoPopupWindow)
            {
                //list to show for adding empty item infos
                    List<EntityInfoItem> infoList = entityInfoItemsList.get(currIndex);
                    ArrayList<AddInfoItem> itemList = new ArrayList<AddInfoItem>();
                    for(int i=0;i<infoList.size();i++)
                    {
                        if(!infoList.get(i).getVisibility())
                            itemList.add(new AddInfoItem(infoList.get(i).getFieldName()));
                    }

                addInfoListAdapter = new AddInfoListAdapter(EntityInfoInputActivity.this , itemList);
                addInfoListView.setAdapter(addInfoListAdapter);
                btnAddInfoItemPopupConfirm.setVisibility(View.GONE);
            }
        }
        else
        {
            if (bShown) {
                popupRootLayout.setVisibility(LinearLayout.GONE);
            } else {
                popupRootLayout.setVisibility(LinearLayout.VISIBLE);
            }
            popupWindow.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("entity" , entity);
        outState.putBoolean("isNewEntity" , this.isNewEntity);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
        this.isNewEntity = savedInstanceState.getBoolean("isNewEntity" , false);

        if(this.entity.getPrivilege()!=null)
            this.isSharedPrivilege = this.entity.getPrivilege()>0?true:false;
        else
            this.isSharedPrivilege = false;
    }


    //disable back button
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(isNewEntity)
            super.onBackPressed();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        mMyApp.setCurrentActivity(this);

        if (resultCode == RESULT_OK) {
            switch(requestCode)
            {
                case TAKE_PHOTO_FROM_CAMERA:
                    strTempPhotoPath = tempPhotoUriPath;// uri.getPath();
                    if(!strTempPhotoPath.contains("file://"))
                        strTempPhotoPath = "file://"+strTempPhotoPath;

                    System.out.println("-----Photo Path= "+strTempPhotoPath+"----");
                    saveEntityProfilePhoto();

                    break;
                case TAKE_PHOTO_FROM_GALLERY:
                    if(data!=null)
                    {
                        uri = data.getData();
                        File myFile = new File(uri.getPath());
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                        String picturePath = "";

                        if (cursor != null && cursor.moveToFirst())
                        {
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            picturePath = cursor.getString(columnIndex);
                            cursor.close();
                        }
                        else
                        {
                            if (myFile.exists())
                                picturePath = myFile.getAbsolutePath();
                        }
                        strTempPhotoPath = picturePath;
                        if(!strTempPhotoPath.contains("file://"))
                            strTempPhotoPath = "file://"+strTempPhotoPath;

                        System.out.println("-----Photo Path= "+strTempPhotoPath+"----");
                        saveEntityProfilePhoto();
                    }
                    break;
            }
        }
        else
        {
            strTempPhotoPath = "";
            strProfileImagePath = "";
            //imgPhoto.setImageUrl(strProfileImagePath , imgLoader);
            //imgPhoto.invalidate();
        }
	}


    @Override
    public void onDestroy(){
        this.entityInfos.clear();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEdit:
                if (!isEditable) {
                    isEditable = true;
                    updateUIFromEditable();
                }
                break;

            case R.id.btnConfirm:
                boolean isValidFieldInputs = true;

                //get all field values from edit boxes
                Iterator<Integer> fragmentKeys = fragmentMap.keySet().iterator();
                while(fragmentKeys.hasNext())
                {
                    Integer key = fragmentKeys.next();
                    EntityInfoEditFragment ff = fragmentMap.get(key);
                    if(ff!=null) {
                        if(!ff.saveEditingInfoItems())
                            return;
                    }
                }

                //check the input fields validation
                for (int i = 0; i < entityInfoItemsList.size(); i++) {
                    List<EntityInfoItem> locationInfos = entityInfoItemsList.get(i);
                    boolean hasAtLeastOneInfo = false;
                    for (int j = 0; j < locationInfos.size(); j++) {
                        EntityInfoItem fieldItem = locationInfos.get(j);
                        if ((j == 0 && fieldItem.getFieldValue().trim().equals("")) ||
                                (j == 1 && fieldItem.getFieldValue().trim().equals("")))//if default item location name and keyword is empty
                        {
                            isValidFieldInputs = false;
                            break;
                        }
                        if (!fieldItem.getVisibility())
                            continue;

                        //if address is not confirmed and also not skipped , then show alert dialog
                        if(fieldItem.getFieldName().toLowerCase().contains("address") && !fieldItem.getFieldValue().trim().equals("") && !fieldItem.isAddressConfirmed() && !fieldItem.isAddressSkipped())
                        {
                            final EntityInfoItem infoItem = fieldItem;
                            AlertDialog.Builder builder = new AlertDialog.Builder(EntityInfoInputActivity.this);
                            builder.setTitle("Confirm");
                            builder.setMessage(getResources().getString(R.string.str_confirm_dialog_confirm_location_address));
                            builder.setNegativeButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //TODO
                                    infoItem.setIsAddressConfirmed(false);
                                    infoItem.setAddressSkipped(false);
                                    dialog.dismiss();
                                    return;
                                }
                            });
                            builder.setPositiveButton(R.string.alert_button_skip, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //TODO
                                    infoItem.setIsAddressConfirmed(false);
                                    infoItem.setAddressSkipped(true);
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        //if phone , address, or email field has at least one value
                        if((fieldItem.getFieldName().toLowerCase().contains("address") || fieldItem.getFieldName().toLowerCase().contains("phone") || fieldItem.getFieldName().toLowerCase().contains("email")) &&
                                !fieldItem.getFieldValue().trim().equals(""))
                            hasAtLeastOneInfo = true;

                        //check mobile number
                        /*if(!fieldItem.getFieldValue().trim().equals("") && (fieldItem.getFieldName().toLowerCase().contains("mobile") || fieldItem.getFieldName().toLowerCase().contains("phone")))
                        {
                            boolean bValidPhoneNumber = false;
                            for(int k=0; k< ConstValues.validPhoneNumberFormats.length;k++) {

                                //Pattern phonePattern  = Pattern.compile(ConstValues.validPhoneNumberFormats[k], Pattern.CASE_INSENSITIVE);
                                //Matcher matcher = phonePattern .matcher(infoItem.strInfoValue);
                                //if(matcher.matches())
                                System.out.println(ConstValues.validPhoneNumberFormats[k] + " (" + k + ")");
                                if (fieldItem.getFieldValue().matches(ConstValues.validPhoneNumberFormats[k])) {
                                    bValidPhoneNumber = true;
                                    break;
                                }
                            }
                            if(!bValidPhoneNumber)
                            {
                                if(fieldItem.getFieldName().toLowerCase().contains("phone"))
                                     MyApp.getInstance().showSimpleAlertDiloag(EntityInfoInputActivity.this, R.string.str_alert_invalid_phone_number, null);
                                else
                                     MyApp.getInstance().showSimpleAlertDiloag(EntityInfoInputActivity.this, R.string.str_alert_invalid_mobile_number, null);
                                return;
                            }
                        }*/
                    }
                    if (isValidFieldInputs == false)
                        break;
                    if (!hasAtLeastOneInfo) {
                        isValidFieldInputs = false;
                        break;
                    }
                }

                //if input fields have invalid value
                if(!isValidFieldInputs)
                {
                    MyApp.getInstance().showSimpleAlertDiloag(EntityInfoInputActivity.this , "Oops!",  R.string.str_alert_dialog_entity_at_least_one_contact_info , null);
                    return;
                }

                this.entity.setPrivilege(isSharedPrivilege?1:0);

                //loop all lcoations
                for(int i=0;i<entityInfos.size();i++)
                {
                    EntityInfoVO location = entityInfos.get(i);
                    List<EntityInfoItem> fieldItems = entityInfoItemsList.get(i);
                    List<EntityInfoDetailVO> fields = location.getEntityInfoDetails();
                    List<EntityInfoDetailVO> newFields = new ArrayList<EntityInfoDetailVO>();

                    location.setLatitude(null);
                    location.setLongitude(null);
                    location.setAddressConfirmed(false);

                    //calculate the start top position of each location
                    int startTop = (int)(activityHeight*0.05);
                    int margin = (int)(activityHeight*0.02);

                    HashMap<String , Integer> originalFieldNames = new HashMap<String , Integer>();
                    for(int j=0;j<fields.size(); j++)
                    {
                        originalFieldNames.put(fields.get(j).getFieldName() , j);
                    }
                    if(originalFieldNames.get("Abbr") == null)//if abbr field was missded
                    {
                        EntityInfoDetailVO field = new EntityInfoDetailVO();
                        field.setFieldName("Abbr");
                        field.setType("abbr");
                        field.setValue("0");//default abbr value is 0
                        //field.setColor(""); //default color
                        //field.setFont(""); //default font and font size
                        field.setPosition("");

                        newFields.add(field);
                    }
                    else
                        newFields.add(fields.get(originalFieldNames.get("Abbr")));

                    if(originalFieldNames.get("Privilege") == null)//if abbr field was missded
                    {
                        EntityInfoDetailVO field = new EntityInfoDetailVO();
                        field.setFieldName("Privilege");
                        field.setType("privilege");
                        field.setValue(isSharedPrivilege ? "1" : "0");
                        //field.setColor(""); //default color
                        //field.setFont(""); //default font and font size
                        field.setPosition("");
                        newFields.add(field);
                    }
                    else {
                        EntityInfoDetailVO field = fields.get(originalFieldNames.get("Privilege"));
                        field.setValue(isSharedPrivilege ? "1" : "0");
                        newFields.add(field);
                    }

                    String[] dontShowFields = { "foreground", "background",
                            "privilege", "abbr", "video" };
                    for(int j=0;j<fieldItems.size();j++)
                    {
                        EntityInfoItem fieldItem = fieldItems.get(j);
                        if(fieldItem.getVisibility() == false) {
                            continue;
                        }
                        if(fieldItem.getFieldValue().trim().equals(""))
                            continue;

                        if (ArrayUtils.contains(dontShowFields,
                                fieldItem.getFieldType().toLowerCase())) {
                            continue;
                        }

                        if(fieldItem.getFieldName().toLowerCase().contains("address") && location.getLatitude() == null && location.getLongitude() == null)
                        {
                            //set the lat and long of location if first address has valid lat and long value
                            if(fieldItem.getLatitude() != null && fieldItem.getLongitude()!=null)
                            {
                                location.setLatitude(String.valueOf(fieldItem.getLatitude()));
                                location.setLongitude(String.valueOf(fieldItem.getLongitude()));
                                location.setAddressConfirmed(true);
                            }
                        }

                        //if its not already existing field
                        if(originalFieldNames.get(fieldItem.getFieldName())==null) {
                            EntityInfoDetailVO field = new EntityInfoDetailVO();
                            field.setFieldName(fieldItem.getFieldName());
                            field.setType(fieldItem.getFieldType());
                            field.setValue(fieldItem.getFieldValue());
                            //field.setColor("ff000000"); //default color
                            //field.setFont("Arial" + ":" + "17" + ":" + "Normal"); //default font and font size
                            Rect bounds = new Rect();
                            if(fieldItem.getFieldValue().equals(""))
                                textBoundMeasureView.setText("Aaa");
                            else
                                textBoundMeasureView.setText(fieldItem.getFieldValue());
                            if(faces == null)
                                faces = MyApp.getInstance().getFontFaces();
                            textBoundMeasureView.setTypeface(faces.get(0));
                            textBoundMeasureView.invalidate();

                            Paint textPaint = textBoundMeasureView.getPaint();
                            //textPaint.getTextBounds(fieldItem.getFieldValue(), 0, fieldItem.getFieldValue().length(), bounds);
                            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
                            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                            textBoundMeasureView.measure(widthMeasureSpec, heightMeasureSpec);
                            bounds.right = bounds.left + textBoundMeasureView.getMeasuredWidth();
                            bounds.bottom = bounds.top  + textBoundMeasureView.getMeasuredHeight();

                            int height = bounds.height();
                            int width = bounds.width() + (int) (bounds.width() * 0.2);
                            bounds.left = (int) (activityWidth * 0.02);
                            bounds.top = startTop;
                            bounds.right = width;
                            bounds.bottom = height;
                            if (!field.getFieldName().equalsIgnoreCase("keysearch"))//exclude the keysearch field in the profile view
                            {
                                startTop = bounds.top + height + margin;
                            }
                            field.setPosition(bounds.left, bounds.top, bounds.right, bounds.bottom, ratio);

                            newFields.add(field);
                        }
                        else
                        {
                            EntityInfoDetailVO field = fields.get(originalFieldNames.get(fieldItem.getFieldName()));
                            field.setFieldName(fieldItem.getFieldName());
                            field.setType(fieldItem.getFieldType());
                            field.setValue(fieldItem.getFieldValue());

                            newFields.add(field);
                        }
                    }
                    location.setEntityInfoDetails(newFields);

                }


                if(isNewEntity) {
                    Intent intent = new Intent(EntityInfoInputActivity.this, EntityInfoNewProfileAddActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("entity", entity);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("entity", entity);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK , intent);
                    finish();
                }

                break;

            case R.id.btnDelete:
                if(currentFragment != null) {
                    currentFragment.deleteSelectedItems();
                    currentFragment.updateListView();
                }
                break;

            case R.id.btnClose:
                if(isEditable)
                {
                    isEditable = false;
                    updateUIFromEditable();
                }
                break;

            //add new location info of entity
            case R.id.btnAddLocation:
                btnRemoveLocation.setVisibility(View.VISIBLE);
                numberOfLocation++;
                createNewInfo();
                if(numberOfLocation<=1)
                {
                    txtTitle.setText(getResources().getString(R.string.entity_info));
                }
                else
                {
                    txtTitle.setText(getResources().getString(R.string.entity_info)+" + "+String.valueOf(numberOfLocation-1));
                }
                currIndex ++;
                pageAdater.notifyDataSetChanged();
                mPager.setCurrentItem(currIndex);
                pageListener.onPageSelected(currIndex);

                break;

            //remove a location info of entity
            case R.id.btnRemoveLocation:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Confirm");
                builder.setMessage(getResources().getString(R.string.str_confirm_dialog_delete_entity_location));
                builder.setPositiveButton(R.string.str_confirm_dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        btnRemoveLocation.setVisibility(View.VISIBLE);
                        numberOfLocation--;
                        if (numberOfLocation == 1) {
                            btnRemoveLocation.setVisibility(View.INVISIBLE);
                        }
                        removeInfo(currIndex);
                        if(numberOfLocation<=1)
                        {
                            txtTitle.setText(getResources().getString(R.string.entity_info));
                        }
                        else
                        {
                            txtTitle.setText(getResources().getString(R.string.entity_info)+" + "+String.valueOf(numberOfLocation-1));
                        }
                        pageAdater.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.str_confirm_dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                break;

            //lock or unlock
            case R.id.btnLock:
                isSharedPrivilege = !isSharedPrivilege;
                updateLockButton();
                break;

            //add new info , show popup window of add info
            case R.id.btnAddInfo:
                showHidePopupView(addInfoPopupWindow, true);
                break;

            //add info item listview popup close
            case R.id.btnAddInfoPopupClose:
                showHidePopupView(addInfoPopupWindow, false);
                break;

            //add info item listview popup confirm
            case R.id.btnAddInfoPopupConfirm:
                for(int i=0;i<addInfoListAdapter.getCount();i++)
                {
                    AddInfoItem item = (AddInfoItem) addInfoListAdapter.getItem(i);
                    if(item.isSelected)
                    {
                        int index = infoNameMap.get(item.strInfoName);
                        currentFragment.addNewInfoItem(item.strInfoName , index);
                    }
                }
                if(currentFragment != null)
                    currentFragment.updateListView();

                showHidePopupView(addInfoPopupWindow, false);
                break;
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0)//take a photo
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder("UserProfile") +
                    String.valueOf(System.currentTimeMillis()) + ".jpg"));
            tempPhotoUriPath = uri.getPath();
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
            EntityInfoInputActivity.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
        }
        else if(index == 1) //photo from gallery
        {
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            EntityInfoInputActivity.this.startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
        }
        else if(index ==2 )//remove photo
        {
            if(!entity.getProfileImage().equals("")) {
                EntityRequest.removeProfileImage(entity.getId(), new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if (response.isSuccess()) {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";
                            imgPhoto.refreshOriginalBitmap();
                            imgPhoto.setImageUrl(strProfileImagePath, imgLoader);
                            imgPhoto.invalidate();
                            entity.setProfileImage("");
                        }
                    }
                });
            }
            else
            {
                Uitils.alert(EntityInfoInputActivity.this, getResources().getString(R.string.str_grey_contact_remove_photo_alert));
            }
        }

    }

    @Override
    public void onViewSizeMeasure(int width, int height) {
        activityHeight = height;
        activityWidth = width;

        float r1 = (float)height/480;
        float r2 = (float)width/320;

        if(r2>r1)
            ratio = r2;
        else
            ratio = r1;

        if(addInfoPopupWindow == null)
            enableAddInfoItemPopup();
    }

    /**
	 */
	public class MyPagerAdapter extends FragmentPagerAdapter {

		private List<EntityInfoVO> infos;

		public MyPagerAdapter(FragmentManager fm, List<EntityInfoVO> infos) {
			super(fm);
			this.infos = infos;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            fragmentMap.remove(position);
		}

		@Override
		public int getCount() {
			return infos.size();
		}

		@Override
		public Object instantiateItem(ViewGroup arg0, int position) {
            EntityInfoEditFragment ff = (EntityInfoEditFragment) super.instantiateItem(arg0, position);
            ff.setEntityInfoItemList(entityInfoItemsList.get(position));
            ff.setIsEditable(isEditable);
            ff.refreshFieldsData();
            ff.position = position;
            if(position == currIndex)
                currentFragment = ff;
            fragmentMap.put(position, ff);
            return ff;
		}

		@Override
		public Fragment getItem(int position) {
            EntityInfoEditFragment entityInfoEditFragment = new EntityInfoEditFragment(entityInfoItemsList.get(position));
            entityInfoEditFragment.setIsEditable(isEditable);
            entityInfoEditFragment.refreshFieldsData();

            if(position == currIndex)
                currentFragment = entityInfoEditFragment;

            fragmentMap.put(position , entityInfoEditFragment);

            return entityInfoEditFragment;
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int position) {
			currIndex = position;

            currentFragment = fragmentMap.get(position);
            if(currentFragment!=null)
            {
                currentFragment.setIsEditable(isEditable);
                currentFragment.updateListView();
            }
        }

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

    class AddInfoItem
    {
        public String strInfoName = "";
        public boolean isSelected;
        public AddInfoItem(String infoName )
        {
            this.strInfoName = infoName;
        }
    }

    class AddInfoListAdapter extends BaseAdapter
    {
        private Context mContext;
        private ArrayList<AddInfoItem> listItems;
        private Resources res;
        public AddInfoListAdapter(Context context , ArrayList<AddInfoItem> items)
        {
            this.mContext = context;
            this.listItems = new ArrayList<AddInfoItem>();
            if(items != null) {
                for (int i = 0; i<items.size(); i++)
                    this.listItems.add(items.get(i));
            }
            this.res = mContext.getResources();
        }

        @Override
        public int getCount() {
            return listItems == null?0:listItems.size();
        }

        @Override
        public Object getItem(int position) {
            return listItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public boolean hasSelectedItems()
        {
            if(listItems == null)
                return false;

            int count = 0;
            for(int i=0;i<listItems.size();i++)
            {
                if(listItems.get(i).isSelected)
                    count++;
            }

            if(count>0)
                return true;

            return false;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = new ViewHolder(); // our view holder of the row
            if (convertView == null) {

                holder.txtInfoItem = new TextView(mContext);
                holder.txtInfoItem.setPadding(this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_left_right_padding),
                        this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_top_bottom_padding),
                        this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_left_right_padding),
                        this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_top_bottom_padding)
                );
                holder.txtInfoItem.setTextColor(Color.BLACK);
                holder.txtInfoItem.setTextSize(this.res.getDimension(R.dimen.grey_contact_add_info_text_size));
                holder.txtInfoItem.setBackgroundColor(Color.WHITE);
                holder.txtInfoItem.setSingleLine(true);
                holder.txtInfoItem.setLinksClickable(false);
                holder.txtInfoItem.setAutoLinkMask(0);
                convertView = holder.txtInfoItem;

                convertView.setTag(holder);

            }
            holder = (ViewHolder) convertView.getTag();
            holder.txtInfoItem.setText(listItems.get(position).strInfoName);
            if(listItems.get(position).isSelected)
                holder.txtInfoItem.setBackgroundColor(0xffd9d9d9);
            else
                holder.txtInfoItem.setBackgroundColor(Color.WHITE);

            holder.txtInfoItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(addInfoListAdapter!=null)
                    {
                        AddInfoItem item = (AddInfoItem) getItem(position);
                        item.isSelected = !item.isSelected;
                        if(addInfoListAdapter.hasSelectedItems())
                            btnAddInfoItemPopupConfirm.setVisibility(View.VISIBLE);
                        else
                            btnAddInfoItemPopupConfirm.setVisibility(View.GONE);

                        notifyDataSetChanged();
                    }
                }
            });
            return convertView;
        }
    }
    class ViewHolder
    {
        TextView txtInfoItem;
    }



}
