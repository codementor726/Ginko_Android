package com.ginko.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ProfileFieldAddOverlayView;
import com.ginko.customview.ProgressHUD;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;

import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeWorkAddInfoFragment extends Fragment {
    private static final String ARG_PARAM1 = "type";
    private static final String GROUP_INFO = "groupInfo";

    private final int GROUP_HOME = 1;
    private final int GROUP_WORK = 2;

    private ProgressHUD progressHUD;

    private int groupType = GROUP_HOME;

    /*life cycle fragment
        onAttach
        onCreate
        onCreateView
        onActivityCreated
        onStart
        onResume
        onPause
        onStop
        onDestroyView
        onDestroy
        onDetach
    */

    private Pattern pattern;

    private boolean isUICreated = false;
    private LinearLayout infoListLayout;

    private String type;
    private UserUpdateVO groupInfo;

    private boolean isKeyboardVisible = false;

    private final String[] strInfoNames = {
            "Name",
            "Company",
            "Title",
            "Mobile",
            "Mobile#2",
            "Mobile#3",
            "Phone",
            "Phone#2",
            "Phone#3",
            "Email",
            "Email#2",
            "Address",
            "Address#2",
            "Hours",
            "Fax",
            "Birthday",
            "Facebook",
            "Twitter",
            "LinkedIn",
            "Website",
            "Custom",
            "Custom#2",
            "Custom#3",
    };
    private final String[] strFiledTypeNames = {
            ConstValues.PROFILE_FIELD_TYPE_NAME,
            ConstValues.PROFILE_FIELD_TYPE_COMPANY,
            ConstValues.PROFILE_FIELD_TYPE_TITLE,
            ConstValues.PROFILE_FIELD_TYPE_MOBILE,
            ConstValues.PROFILE_FIELD_TYPE_MOBILE,
            ConstValues.PROFILE_FIELD_TYPE_MOBILE,
            ConstValues.PROFILE_FIELD_TYPE_PHONE,
            ConstValues.PROFILE_FIELD_TYPE_PHONE,
            ConstValues.PROFILE_FIELD_TYPE_PHONE,
            ConstValues.PROFILE_FIELD_TYPE_EMAIL,
            ConstValues.PROFILE_FIELD_TYPE_EMAIL,
            ConstValues.PROFILE_FIELD_TYPE_ADDRESS,
            ConstValues.PROFILE_FIELD_TYPE_ADDRESS,
            ConstValues.PROFILE_FIELD_TYPE_HOURS,
            ConstValues.PROFILE_FIELD_TYPE_FAX,
            ConstValues.PROFILE_FIELD_TYPE_DATE,
            ConstValues.PROFILE_FIELD_TYPE_FACEBOOK,
            ConstValues.PROFILE_FIELD_TYPE_TWITTER,
            ConstValues.PROFILE_FIELD_TYPE_LINKEDIN,
            ConstValues.PROFILE_FIELD_TYPE_WEBSITE,
            ConstValues.PROFILE_FIELD_TYPE_CUSTOM,
            ConstValues.PROFILE_FIELD_TYPE_CUSTOM,
            ConstValues.PROFILE_FIELD_TYPE_CUSTOM,
    };
    private HashMap<String , Integer> infoNameMap;

    private ArrayList<InfoItem> infoList;
    private ArrayList<InfoItemView> infoItemViews;

    private List<Typeface> faces ;

    private ProfileFieldAddOverlayView.OnProfileFieldItemsChangeListener onProfileFieldItemsChangeListener = null;


    public void setOnProfileFieldItemsChangeListener(ProfileFieldAddOverlayView.OnProfileFieldItemsChangeListener listener)
    {
        this.onProfileFieldItemsChangeListener = listener;
    }

    public ArrayList<InfoItem> getInfoList(){
        return  this.infoList;
    }

    public void setKeyboardVisibilty(boolean visibilty)
    {
        this.isKeyboardVisible = visibilty;
        if(!isUICreated) return;
        for(InfoItemView itemView:infoItemViews)
        {
            if(itemView.getVisibility() == View.VISIBLE)
                itemView.refreshView();
        }
    }

    public void updateInfoView(boolean isEditable)
    {
        if(!isUICreated) return;
        for(InfoItemView itemView:infoItemViews)
        {
            if(itemView.getVisibility() == View.VISIBLE)
                itemView.UpdateEditable(isEditable);
        }
    }

    private EditText edtTextEmailInputType;
    private int emailInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;

    InputFilter smileyFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                //System.out.println("Type : " + type);
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        }
    };

    public void addNewInfoItem(String fieldName)
    {
        int infoItemIndex = infoNameMap.get(fieldName);
        infoList.get(infoItemIndex).setVisibility(true);
        infoList.get(infoItemIndex).setInfoValue("");
        if(infoItemViews!=null) {
            infoItemViews.get(infoItemIndex).resetValues();
        }
        updateInfoListViews(false);
    }

    public void removeInfoItem(String fieldName)
    {
        int infoItemIndex = infoNameMap.get(fieldName);
        infoList.get(infoItemIndex).setVisibility(false);
        infoList.get(infoItemIndex).setInfoValue("");
        if(infoItemViews!=null) {
            infoItemViews.get(infoItemIndex).resetValues();
        }
        updateInfoListViews(false);
    }

    public void hiddenInfoItem(String fieldName)
    {
        int infoItemIndex = infoNameMap.get(fieldName);
        infoList.get(infoItemIndex).setVisibility(false);
        infoList.get(infoItemIndex).setPending(true);

        updateInfoListViews(false);
    }

    public void restoreInfoItem(String fieldName)
    {
        int infoItemIndex = infoNameMap.get(fieldName);
        infoList.get(infoItemIndex).setVisibility(true);
        infoList.get(infoItemIndex).setPending(false);

        updateInfoListViews(false);
    }


    public void getEditingInfoItemValues()
    {
        for(int i = 0;i<strInfoNames.length;i++)
        {
            if(infoList.get(i).isVisible)
            {
                infoList.get(i).strInfoValue = infoItemViews.get(i).getEdtTextValue();
            }
        }
    }

    public boolean hasMoreThanOneInputedValues()
    {
        getEditingInfoItemValues();

        {
            int nValidItemCount = 0;
            String[] dontShowFields = { "foreground", "background",
                    "privilege", "abbr", "video" };
            for(int i=0; i<infoList.size(); i++)
            {
                HomeWorkAddInfoFragment.InfoItem infoItem = infoList.get(i);
                if(!infoItem.isVisible) continue;
                if (ArrayUtils.contains(dontShowFields,
                        infoItem.strFieldType.toLowerCase())) {
                    continue;
                }
                if(!infoItem.strInfoValue.equals(""))
                    nValidItemCount++;
            }

            if(nValidItemCount > 0) return true;
        }
        return false;
    }

    public UserUpdateVO saveGroupInfo(Context context ,boolean isPublicLocked , boolean bShowAlert)
    {
        UserUpdateVO newGroupInfo = new UserUpdateVO();

        getEditingInfoItemValues();

        List<UserProfileVO> fields = new ArrayList<UserProfileVO>();;

        newGroupInfo.setFields(fields);
        String[] dontShowFields = { "name" , "foreground", "background",
                "privilege", "abbr", "video" };

        //check inputs validation
        for(int i=0;i<infoList.size();i++)
        {
            HomeWorkAddInfoFragment.InfoItem infoItem = infoList.get(i);
            if(!infoItem.isVisible) continue;
            if(infoItem.strInfoValue.equals(""))
            {
                if(infoItem.strFieldType.equalsIgnoreCase(ConstValues.PROFILE_FIELD_TYPE_NAME))//full name
                {
                    //infoItem.setInfoValue("Sandy Poll");
                    if(bShowAlert)
                        //MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                        MyApp.getInstance().showSimpleAlertDiloag(context, "Please input Your Full Name." , null);
                    return null;
                }
                else if(groupType == GROUP_WORK && infoItem.strFieldType.equalsIgnoreCase(ConstValues.PROFILE_FIELD_TYPE_TITLE)) // Title
                {
                    //infoItem.setInfoValue("My Title");
                    if(bShowAlert)
                        //MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                        MyApp.getInstance().showSimpleAlertDiloag(context, "Please input Your Titile." , null);
                    return null;
                }
                else if(groupType == GROUP_WORK && infoItem.strFieldType.equalsIgnoreCase(ConstValues.PROFILE_FIELD_TYPE_COMPANY)) // Company
                {
                    //infoItem.setInfoValue("My Company name");
                    if(bShowAlert)
                        //MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                        MyApp.getInstance().showSimpleAlertDiloag(context, "Please input Company Name." , null);
                    return null;
                }
                else if(infoItem.strFieldType.equalsIgnoreCase(ConstValues.PROFILE_FIELD_TYPE_MOBILE)) //mobile
                {
                    //infoItem.setInfoValue("112-123-122");
                    if(bShowAlert)
                        //MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                        MyApp.getInstance().showSimpleAlertDiloag(context, "Please input "+infoItem.strInfoName+" Number." , null);
                    return null;
                }
                else if(infoItem.strFieldType.equalsIgnoreCase(ConstValues.PROFILE_FIELD_TYPE_PHONE)) //phone
                {
                    //infoItem.setInfoValue("112-123-122");
                    if(bShowAlert)
                        //MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                        MyApp.getInstance().showSimpleAlertDiloag(context, "Please input "+infoItem.strInfoName+" Number." , null);
                    return null;
                }
                else if(infoItem.strFieldType.equalsIgnoreCase(ConstValues.PROFILE_FIELD_TYPE_EMAIL)) //email
                {
                    //infoItem.setInfoValue("sample@gmail.com");
                    //MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    MyApp.getInstance().showSimpleAlertDiloag(context, "Please input "+infoItem.strInfoName , null);
                    return null;
                }
                else if(infoItem.strFieldType.equalsIgnoreCase(ConstValues.PROFILE_FIELD_TYPE_ADDRESS)) //address
                {
                    //infoItem.setInfoValue("China-ShenYang");
                    if(bShowAlert)
                        //MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                        MyApp.getInstance().showSimpleAlertDiloag(context, "Please input "+infoItem.strInfoName , null);
                    return null;
                }
                else
                {
                    if(bShowAlert)
                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    return null;
                }
            }
            else
            {
                //check email type
                if(infoItem.strFieldType.contains(ConstValues.PROFILE_FIELD_TYPE_EMAIL))
                {
                    if(!isEmailValid(infoItem.strInfoValue))
                    {
                        if(bShowAlert) {
                            MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_invalid_email_address, null);
                        }
                        return null;
                    }
                }

                /*if(infoItem.strFieldType.contains(ConstValues.PROFILE_FIELD_TYPE_ADDRESS))
                {
                    if (!isValidAddress(infoItem.strInfoValue)) {
                        if (bShowAlert) {
                            MyApp.getInstance().showSimpleAlertDiloag(context, "Please input correct address.", null);
                        }
                        return null;
                    }
                }*/
                //check phone number
                /*if(infoItem.strFieldType.contains(ConstValues.CONTACT_FIELD_TYPE_PHONE) || infoItem.strFieldType.contains(ConstValues.CONTACT_FIELD_TYPE_MOBILE))
                {
                    boolean bValidPhoneNumber = false;
                    for(int k=0; k<ConstValues.validPhoneNumberFormats.length;k++) {

                        //Pattern phonePattern  = Pattern.compile(ConstValues.validPhoneNumberFormats[k], Pattern.CASE_INSENSITIVE);
                        //Matcher matcher = phonePattern .matcher(infoItem.strInfoValue);
                        //if(matcher.matches())
                        System.out.println(ConstValues.validPhoneNumberFormats[k] +" ("+k+")");
                        if(infoItem.strInfoValue.matches(ConstValues.validPhoneNumberFormats[k]))
                        {
                            bValidPhoneNumber = true;
                            break;
                        }
                    }
                    if(!bValidPhoneNumber) {
                        if (bShowAlert) {
                            if(infoItem.strFieldType.contains(ConstValues.CONTACT_FIELD_TYPE_PHONE))
                                MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_invalid_phone_number, null);
                            else
                                MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_invalid_mobile_number, null);
                        }
                        return null;
                    }
                }*/

                UserProfileVO fieldItem = new UserProfileVO();
                //fieldItem.setColor("ff000000");
                for (int j = 0; j< groupInfo.getFields().size(); j++)
                {
                    String strName = groupInfo.getFields().get(j).getFieldName().trim();
                    String strType = groupInfo.getFields().get(j).getFieldType().trim();
                    if(infoItem.strInfoName.equals(strName))
                        fieldItem.setId(groupInfo.getFields().get(j).getId());
                }
                fieldItem.setFieldName(infoItem.strInfoName);
                fieldItem.setFieldType(infoItem.strFieldType);
                fieldItem.setValue(infoItem.strInfoValue);
                //fieldItem.setFont("Arial" + ":" + "17"+":"+"Normal");//default font & font size
                fieldItem.setIsShared(!isPublicLocked);
                fields.add(fieldItem);

            }
        }

        /*
        for (int j = 0; j< groupInfo.getFields().size(); j++)
        {
            String strName = groupInfo.getFields().get(j).getFieldName().trim();
            String strType = groupInfo.getFields().get(j).getFieldType().trim();
            if(strName.equalsIgnoreCase("privilege")) {
                UserProfileVO privilegeField = new UserProfileVO();
                privilegeField.setFieldName("Privilege");
                privilegeField.setPosition("");
                privilegeField.setFieldType("privilege");
                privilegeField.setId(groupInfo.getFields().get(j).getId());
                privilegeField.setValue(!isPublicLocked ? "1" : "0");
                fields.add(privilegeField);
            }
        } */

        newGroupInfo.setPublic(!isPublicLocked);

        return newGroupInfo;
    }

    public int getCurrentInfoItemCounts()
    {
        if(infoList == null) return 4;
        int nCount = 0;
        for(int i=0;i<strInfoNames.length;i++)
        {
            if(i == 0 || // Full name
                (groupType == GROUP_WORK && i == 1) || //Title
                (groupType == GROUP_WORK && i == 2) || //Company
               i == 3 || //mobile
               i == 9) //email

            {
                nCount++;
                continue;
            }

            if(infoList.get(i).isVisible)
                nCount++;
        }

        return nCount;
    }

    public static HomeWorkAddInfoFragment newInstance(String type,UserUpdateVO groupInfo) {
        HomeWorkAddInfoFragment fragment = new HomeWorkAddInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, type);
        args.putSerializable(GROUP_INFO, groupInfo);
        fragment.setArguments(args);
        return fragment;
    }

    public static HomeWorkAddInfoFragment newInstance(String type,UserUpdateVO groupInfo , String fullName) {
        HomeWorkAddInfoFragment fragment = new HomeWorkAddInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, type);
        args.putSerializable(GROUP_INFO, groupInfo);
        args.putString("fullname", fullName);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeWorkAddInfoFragment(){}

    public UserUpdateVO getGroupInfo()
    {
        return this.groupInfo;
    }

    public int getCurrentInputableFieldsCount()
    {
        if(groupInfo == null || infoList == null) return 0;
        List<UserProfileVO> fields = groupInfo.getFields();
        int count = 0;
        for (int i =0 ;i < infoList.size(); i++) {
            if(infoList.get(i).isVisible)
                count++;
        }
        return  count;
    }
    private void checkFieldVisibilityAndRemovability()
    {

    }
    private int getAvailableEmailFieldCount()
    {
        int count = 0;
        if(infoList == null || infoList.size() < 1) return 0;
        for(int i =0;i<infoList.size();i++) {
            if (infoList.get(i).isVisible && infoList.get(i).strInfoName.toLowerCase().contains("email"))
                count++;
        }
        return count;
    }
    private int getAvailableMobileFieldCount()
    {
        int count = 0;
        if(infoList == null || infoList.size() < 1) return 0;
        for(int i =0;i<infoList.size();i++) {
            if (infoList.get(i).isVisible && infoList.get(i).strInfoName.toLowerCase().contains("mobile"))
                count++;
        }
        return count;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            type = getArguments().getString(ARG_PARAM1);
            groupInfo = (UserUpdateVO)getArguments().getSerializable(GROUP_INFO);
            String strFullName = getArguments().getString("fullname" , "");

            if(type.equalsIgnoreCase("home"))
                groupType = GROUP_HOME;
            else
                groupType = GROUP_WORK;

            infoNameMap = new HashMap<String , Integer>();
            infoList = new ArrayList<InfoItem>();
            infoItemViews = new ArrayList<InfoItemView>();

            String[] dontShowFields = { "foreground", "background",
                    "privilege", "abbr", "video" };

            int valuableFieldCount = 0;
            List <UserProfileVO> fields = groupInfo.getFields();
            if(fields != null) {
                for (UserProfileVO field : fields) {
                    String fieldType = field.getFieldType();
                    if(fieldType.equals(""))
                        continue;
                    if (ArrayUtils.contains(dontShowFields,
                            fieldType.toLowerCase()))
                        continue;
                    if(field.getValue() != null && field.getValue().compareTo("") != 0)
                        valuableFieldCount ++;
                }
            }

            for(int i=0;i<strInfoNames.length;i++)
            {
                infoNameMap.put(strInfoNames[i] , new Integer(i));
                //show default items
                if(i == 0)//hide name field
                {
                    infoList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i], "", false, true));
                    continue;
                }
                if((groupType == GROUP_WORK && i == 1) || //Title
                    (groupType == GROUP_WORK && i == 2) || //Company
                        i == 3 || //mobile
                        i == 9  //email
                        ) {
                    if (valuableFieldCount > 0) //if there is already existing field, then do not show default value
                    {
                        infoList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i], "", false, true));
                    }
                    else
                    {
                        infoList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i], "", true, true));
                    }
                }
                else
                    infoList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i] ,"" , false , true));
            }

            //if(strFullName.compareTo("") != 0) {
            //    infoList.get(0).strInfoValue = strFullName;
            ///}

            if(fields!=null) {
                int fieldCount = 0;
                for (UserProfileVO field : fields) {
                    String fieldType = field.getFieldType();
                    if(fieldType.equals(""))
                        continue;
                    if (ArrayUtils.contains(dontShowFields,
                            fieldType.toLowerCase()))
                        continue;
                    fieldCount++;
                    String fieldName = field.getFieldName();
                    for(int i =0;i<infoList.size();i++)
                    {
                        if(infoList.get(i).strInfoName.equalsIgnoreCase(fieldName))
                        {
                            infoList.get(i).setInfoValue(field.getValue());
                            if(i == 0)
                                infoList.get(i).setVisibility(false);//hide name field as default
                            else
                                infoList.get(i).setVisibility(true);

                        }
                    }
                }

                //if there isn't any field info, then set the default fields as visible
                if(fieldCount == 0)
                {
                    for(int i=0; i<infoList.size(); i++)
                    {
                        if((groupType == GROUP_WORK && i == 1) || //Title
                                (groupType == GROUP_WORK && i == 2) || //Company
                                i == 3 || //mobile
                                i == 9  //email
                                )
                            infoList.get(i).setVisibility(true);
                        if(i == 9)//email . if there isn't any info ,then make sample email with user's registered name
                        {
                            String userName = Uitils.getUserFullname(getActivity());
                            if(userName.trim().contains(" "))
                                userName = userName.substring(0 , userName.indexOf(" ") -1 );
                            //String email = userName+"@"+userName+".com";
                            String email = Uitils.getLoginEmail(getActivity());
                            infoList.get(i).strInfoValue = email;
                        }
                    }
                }
            }

        }
        //Show process progress dialog
        progressHUD = ProgressHUD.createProgressDialog(getActivity(), "", false, false, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(progressHUD != null && progressHUD.isShowing())
                    progressHUD.dismiss();
            }
        });
    }


    private int deviceWidth = 0 , deviceHeight = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_work_add_info , container , false);
        infoListLayout = (LinearLayout)view.findViewById(R.id.infoListLayout);

        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        deviceWidth = size.x;
        deviceHeight = size.y;

        faces = MyApp.getInstance().getFontFaces();

        edtTextEmailInputType = (EditText)view.findViewById(R.id.edtTextEmailInputType);
        emailInputType = edtTextEmailInputType.getInputType();//this is used for special google keyboard inputtypes

        for(int i=0;i<strInfoNames.length;i++)
        {
            infoItemViews.add(new InfoItemView(getActivity() , infoList.get(i)));
            infoListLayout.addView(infoItemViews.get(i));
        }

        isUICreated = true;

        updateInfoListViews(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        isUICreated = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isUICreated = false;
    }

    public ArrayList<String> getCurrentVisibleInfoItems()
    {
        ArrayList<String> infoItems = new ArrayList<String>();
        if(infoList!=null)
        {
            for(InfoItem item:infoList)
            {
                if(item.isVisible)
                    infoItems.add(item.strInfoName);
            }
        }
        return infoItems;
    }



    public void updateInfoListViews(boolean isReset)
    {
        if(!isUICreated ) return;
        if(infoList == null || infoList.size() < 1) return;

        int emailCount = getAvailableEmailFieldCount();
        int mobileCount = getAvailableMobileFieldCount();

        for(int i=0;i<strInfoNames.length;i++)
        {
            infoList.get(i).isRemovable = true;
        }

        //if there is only one mobile or email field item , then its not removable
        if((emailCount < 1 && mobileCount == 1) || (emailCount == 1 && mobileCount < 1)){
            if(mobileCount == 1)
            {
                for(int i=0;i<infoList.size();i++)
                {
                    InfoItem item = infoList.get(i);
                    if(item.isVisible && item.strInfoName.toLowerCase().contains("mobile")) {
                        item.isRemovable = false;
                        break;
                    }
                }
            }
            else if(emailCount == 1)
            {
                for(int i=0;i<infoList.size();i++)
                {
                    InfoItem item = infoList.get(i);
                    if(item.isVisible && item.strInfoName.toLowerCase().contains("email")) {
                        item.isRemovable = false;
                        break;
                    }
                }
            }
        }
        for(int i=0;i<strInfoNames.length;i++)
        {
            if(!infoList.get(i).isVisible) {
                infoItemViews.get(i).setVisibility(View.GONE);
            }
            else {
                infoItemViews.get(i).setVisibility(View.VISIBLE);
            }
            if(isReset)
                infoItemViews.get(i).resetValues();
            infoItemViews.get(i).refreshView();
        }
    }

    private boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        if(pattern == null)
            pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    private boolean isValidAddress(String strAddress)
    {
        progressHUD.show();
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> addresses = null;

        try {
            // Getting a maximum of 3 Address that matches the input text
            addresses = geocoder.getFromLocationName(strAddress, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        progressHUD.cancel();

        if(addresses == null || addresses.size() == 0)
            return false;
        else
            return true;
    }

    public class InfoItem
    {
        public String strInfoValue;
        public String strFieldType;
        public String strInfoName = "Phone";
        public int nItemInputType;
        public int nMaxLines = 1;
        public int nFieldId = -1;

        public boolean isVisible = true;
        public boolean isRemovable = true;
        public boolean isPending = false;

        public InfoItem(String infoName , String infoTypeFiled , String infoValue , boolean _isVisible , boolean isRemovable)
        {
            this.strInfoValue = infoValue;
            this.strFieldType = infoTypeFiled;
            this.strInfoName = infoName;
            this.nItemInputType = InputType.TYPE_CLASS_TEXT;
            this.nMaxLines = 1;
            this.isVisible = _isVisible;
            this.isRemovable = isRemovable;

            if(this.strInfoName.contains("Address")) {
                this.nMaxLines = 2;
            }

            if(this.strInfoName.toLowerCase().contains("phone") || this.strInfoName.toLowerCase().contains("mobile") || this.strInfoName.toLowerCase().contains("fax")) {
                //this.nItemInputType = InputType.TYPE_CLASS_PHONE;
                this.nItemInputType = InputType.TYPE_CLASS_TEXT;
            }
            else if(this.strInfoName.toLowerCase().contains("email")) {
                this.nItemInputType = emailInputType;
                //this.nItemInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            }

            //else if(this.strInfoName.equals("Birthday")) {
            //    this.nItemInputType = InputType.TYPE_CLASS_DATETIME;
            //}
            else if(this.strInfoName.toLowerCase().contains("website") || this.strInfoName.toLowerCase().contains("facebook")
                    || this.strInfoName.toLowerCase().contains("twitter") || this.strInfoName.toLowerCase().contains("linkedin"))
                this.nItemInputType = InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS;
            else
                this.nItemInputType = InputType.TYPE_CLASS_TEXT;
        }
        public void setInfoValue(String infoValue)
        {
            this.strInfoValue = infoValue;
        }
        public void setVisibility(boolean visible)
        {
            this.isVisible = visible;
        }
        public void setRemovable(boolean removable)
        {
            this.isRemovable = removable;
        }
        public void setPending(boolean pending)
        {
            this.isPending = pending;
        }

    }

    String[] defaultHomeInfoFields = { "name", "mobile",
            "email", "address"};
    String[] defaultWorkInfoFields = { "name", "title" , "company" ,"mobile",
            "email", "address"};

    public class InfoItemView extends LinearLayout {
        protected Context mContext;
        protected LayoutInflater inflater;

        protected InfoItem item;
        private LinearLayout rootLayout;

        private ImageView imgFieldIcon;
        private ImageButton btnDeleteField , btnBackspace;
        private EditText edtInfoItem;

        private EmailValidationCheckRunnable emailCheckerThread = null;

        private Handler mHandler;

        public InfoItemView(Context context) {
            super(context);
            this.mContext = context;
        }
        public InfoItemView(Context context ,InfoItem _item)
        {
            super(context);
            this.mContext = context;
            this.item = _item;

            mHandler = new Handler(this.mContext.getMainLooper());

            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.home_work_add_info_item, this, true);

            rootLayout = (LinearLayout)findViewById(R.id.rootLayout);

            edtInfoItem = (EditText)findViewById(R.id.edtInfoItem);

            imgFieldIcon = (ImageView)findViewById(R.id.imgFieldIcon);
            btnBackspace = (ImageButton)findViewById(R.id.btnBackspace); btnBackspace.setVisibility(View.GONE);
            btnDeleteField = (ImageButton) findViewById(R.id.btnDeleteField);
            edtInfoItem.setFilters(new InputFilter[]{smileyFilter});

            edtInfoItem.setCursorVisible(false);//hide cursor as default
            edtInfoItem.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        edtInfoItem.setCursorVisible(true);
                    } else {
                        edtInfoItem.setCursorVisible(false);
                    }
                    if (item.strInfoName.toLowerCase().contains("email") && hasFocus == false) {
                        if (emailCheckerThread == null) {
                            emailCheckerThread = new EmailValidationCheckRunnable(mContext, edtInfoItem.getText().toString());
                        } else {
                            if (mHandler != null)
                                mHandler.removeCallbacks(emailCheckerThread);
                        }
                        if (isUICreated) {
                            emailCheckerThread.setEmailString(edtInfoItem.getText().toString());
                            mHandler.postDelayed(emailCheckerThread, 100);
                        }
                    }
                    refreshView();
                }
            });
            //this enables scrollable in scrollview
            /*edtInfoItem.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (v.getId() == R.id.edtInfoItem) {
                        v.getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_UP:
                                v.getParent().getParent().getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }
                    }
                    return false;
                }
            });*/
            /*btnBackspace.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    edtInfoItem.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                }
            });*/

            btnDeleteField.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onProfileFieldItemsChangeListener != null)
                        onProfileFieldItemsChangeListener.onRemovedProfileField(item.strInfoName);
                }
            });

            /*if(groupType == GROUP_HOME)
            {
                edtInfoItem.setTextColor(0xffa3a3a3);
                edtInfoItem.setHintTextColor(0xff000000);
                //edtInfoItem.setBackgroundResource(R.drawable.home_info_item_background);
            }
            else
            {
                edtInfoItem.setTextColor(0xff8064a1);
                edtInfoItem.setHintTextColor(0xff8064a1);
                //edtInfoItem.setBackgroundResource(R.drawable.work_info_item_background);
            }*/

            edtInfoItem.setHintTextColor(0xffa3a3a3);
            edtInfoItem.setTextColor(0xff000000);

            edtInfoItem.setHint(item.strInfoName);

            edtInfoItem.setText(item.strInfoValue);

            if(item.strInfoName.toLowerCase().contains("email"))
                edtInfoItem.setInputType(emailInputType);
            else
                edtInfoItem.setInputType(this.item.nItemInputType);

            //get edtInfo's parent layout param
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) edtInfoItem.getLayoutParams();

            if(item.strInfoName.toLowerCase().contains("address") || item.strInfoName.toLowerCase().contains("hours"))
            {
                params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                //params.height = mContext.getResources().getDimensionPixelSize(R.dimen.contact_profile_address_input_filed_height);
                edtInfoItem.setLayoutParams(params);
                edtInfoItem.setSingleLine(false);
                //edtInfoItem.setMinLines(2);
                //edtInfoItem.setMaxLines(2);
            }
            else
            {
                params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                edtInfoItem.setLayoutParams(params);
                edtInfoItem.setSingleLine(true);
                edtInfoItem.setMaxLines(1);
                edtInfoItem.setLines(1);
            }

            if(item.strInfoName.toLowerCase().contains("name"))
            {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_title);
            }
            else if (item.strInfoName.toLowerCase().contains("title")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_title);
            }
            else if (item.strInfoName.toLowerCase().contains("company")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_company);
            }
            else if (item.strInfoName.toLowerCase().contains("mobile")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_mobile);
                edtInfoItem.setInputType(EditorInfo.TYPE_CLASS_TEXT);
            }
            else if (item.strInfoName.toLowerCase().contains("phone")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_phone);
                edtInfoItem.setInputType(EditorInfo.TYPE_CLASS_TEXT);
            }
            else if (item.strInfoName.toLowerCase().contains("email")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_email);
            }
            else if (item.strInfoName.toLowerCase().contains("address")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_address);
            }
            else if(item.strInfoName.toLowerCase().contains("hours"))
            {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_hours);
            }
            else if (item.strInfoName.toLowerCase().contains("fax")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_fax);
            }
            else if (item.strInfoName.toLowerCase().contains("birthday")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_birthday);
            }
            else if (item.strInfoName.toLowerCase().contains("facebook")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_facebook);
            }
            else if (item.strInfoName.toLowerCase().contains("twitter")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_twitter);
            }
            else if (item.strInfoName.toLowerCase().contains("linkedin")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_linkedin);
            }
            else if (item.strInfoName.toLowerCase().contains("website")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_website);
            }
            else if (item.strInfoName.toLowerCase().contains("custom")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_custom);
            }

            /*if(this.item.nMaxLines>1)
            {
                edtInfoItem.setSingleLine(false);
                edtInfoItem.setLines(2);
                edtInfoItem.setMaxLines(2);
                edtInfoItem.setMinLines(2);
                edtInfoItem.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                edtInfoItem.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                edtInfoItem.setMinHeight(mContext.getResources().getDimensionPixelOffset(R.dimen.home_work_add_info_address_edtbox_min_height));

            }
            else
            {
                edtInfoItem.setMaxLines(1);
                edtInfoItem.setSingleLine(true);
            }*/



            refreshView();

            //edtInfoItem.addTextChangedListener(edtTextWatcher);
        }

        public InfoItem getItem(){return this.item;}

        public void resetValues()
        {
            //edtInfoItem.removeTextChangedListener(edtTextWatcher);
            edtInfoItem.setText(item.strInfoValue);
            //edtInfoItem.addTextChangedListener(edtTextWatcher);
        }

        public String getEdtTextValue()
        {
            return edtInfoItem.getText().toString().trim();
        }


        public void refreshView()
        {
            if(!edtInfoItem.isFocused())
            {
                //btnBackspace.setVisibility(View.GONE);
                btnDeleteField.setVisibility(View.VISIBLE);
            }
            else {
                if (isKeyboardVisible) {
                   // btnBackspace.setVisibility(View.VISIBLE);
                    btnDeleteField.setVisibility(View.VISIBLE);
                } else {
                    //if (ArrayUtils.contains(defaultHomeInfoFields,item.strInfoName.toLowerCase()) && groupType == GROUP_HOME)
                    //btnBackspace.setVisibility(View.GONE);
                    btnDeleteField.setVisibility(View.VISIBLE);
                }
            }

            if(!item.isRemovable && btnDeleteField.getVisibility() == View.VISIBLE)
                btnDeleteField.setVisibility(View.INVISIBLE);

            rootLayout.requestLayout();
        }

        public void UpdateEditable(boolean isFocus)
        {
            if (isFocus)
            {
                btnDeleteField.setVisibility(View.VISIBLE);
                edtInfoItem.setEnabled(true);
            } else {
                btnDeleteField.setVisibility(View.INVISIBLE);
                edtInfoItem.setEnabled(false);
            }

            if(!item.isRemovable && btnDeleteField.getVisibility() == View.VISIBLE)
                btnDeleteField.setVisibility(View.INVISIBLE);

            rootLayout.requestLayout();
        }
    }

    private class EmailValidationCheckRunnable implements Runnable {
        private String email = "";
        private Context mContext;
        public  EmailValidationCheckRunnable(Context context, String emailContent)
        {
            this.email = emailContent;
            this.mContext = context;
        }

        public void setEmailString(String emailContent)
        {
            this.email = emailContent;
        }

        @Override
        public void run()
        {
            String strEmail = email.trim();
            if(strEmail.compareTo("") !=0 && !isEmailValid(strEmail))
            {
                Toast.makeText(mContext, getResources().getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
