package com.ginko.fragments;


import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.ginko.common.Uitils;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.customview.DragableCustomNetworkImageView;
import com.ginko.customview.DragableTextView;
import com.ginko.customview.FontSelector;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.FontSettingVo;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;

import org.apache.commons.lang.ArrayUtils;

import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileEditFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "type";
    private static final String GROUP_INFO = "groupInfo";


    private View view;
    private AbsoluteLayout rootView;

    private String type;
    private UserUpdateVO groupInfo;

    private FontSelector fontSelector;

    private TextView currentEditTextView;

    private CustomNetworkImageView imgBackgroundPhoto;

    private DragableCustomNetworkImageView frontView;

    private ImageLoader imageLoader;

    private HashMap<String , UserProfileVO> tagFieldsMap;

    private boolean isAbbr = false;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param type Parameter 2.
     * @return A new instance of fragment UserProfileEditFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserProfileEditFragment newInstance(String type,UserUpdateVO groupInfo) {
        UserProfileEditFragment fragment = new UserProfileEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, type);
        args.putSerializable(GROUP_INFO,groupInfo);
        fragment.setArguments(args);
        return fragment;
    }

    public UserProfileEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_PARAM1);
            groupInfo = (UserUpdateVO)getArguments().getSerializable(GROUP_INFO);
            tagFieldsMap = new HashMap<String , UserProfileVO>();
            List<UserProfileVO> fields = groupInfo.getFields();
            for(int i=0;i<fields.size();i++)
            {
                UserProfileVO field = fields.get(i);
                int fieldId = field.getId()==null?i:field.getId();
                tagFieldsMap.put(field.getFieldName() + fieldId , field);
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_user_profile_edit, container, false);
        rootView = (AbsoluteLayout) view.findViewById(R.id.container);
        fontSelector = (FontSelector) view.findViewById(R.id.font_selector);
        imgBackgroundPhoto = (CustomNetworkImageView)view.findViewById(R.id.imgBackgroundPhoto);

        this.init(groupInfo);
        return view;
    }


    public void init(UserUpdateVO workInfo) {
        if (groupInfo == null) {
            return;
        }
        System.out.println("----Re Init------");
        rootView.removeAllViews();

        this.groupInfo = workInfo;

        tagFieldsMap = new HashMap<String , UserProfileVO>();

        List<UserProfileVO> fieldList = groupInfo.getFields();
        for(int i=0;i<fieldList.size();i++)
        {
            UserProfileVO field = fieldList.get(i);
            int fieldId = field.getId()==null?i:field.getId();
            tagFieldsMap.put(field.getFieldName() + fieldId , field);
        }

        if(imageLoader == null)
            imageLoader = MyApp.getInstance().getImageLoader();
        imgBackgroundPhoto.refreshOriginalBitmap();
        if(groupInfo.getImages()!=null && groupInfo.getImages().size()>0) {
            boolean bHasBackground = false;
            int i = 0;
            for(i =0;i <groupInfo.getImages().size();i++)
            {
                if(groupInfo.getImages().get(i).getZIndex() == 0)
                {
                    bHasBackground = true;
                    break;
                }
            }
            if(bHasBackground) {
                String profileImage = groupInfo.getImages().get(i).getUrl();
                if (profileImage != null) {
                    imgBackgroundPhoto.setImageUrl(profileImage, imageLoader);
                    System.out.println("---ProfileImage = " + profileImage + " --- ");
                }
            }
            else
            {
                imgBackgroundPhoto.setImageUrl("", imageLoader);
            }
        }
        float ratio = Uitils.getScreenRatioViaIPhone(getActivity());

        //foreground photo
        if(groupInfo.getImages()!= null && groupInfo.getImages().size()>0)
        {
            boolean bHasForeground = false;
            int i = 0;
            for(i =0;i <groupInfo.getImages().size();i++)
            {
                if(groupInfo.getImages().get(i).getZIndex() == 1)
                {
                    bHasForeground = true;
                    break;
                }
            }
            if(bHasForeground) {
                TcImageVO imgInfo = groupInfo.getImages().get(i);
                String foregroundPhotoUrl = imgInfo.getUrl();
                DragableCustomNetworkImageView _frontView = new DragableCustomNetworkImageView(this.getActivity());
                _frontView.setAdustImageAspect(false);
                _frontView.setImageScaleType(ImageView.ScaleType.FIT_CENTER);
                _frontView.setImageUrl(foregroundPhotoUrl, imageLoader);

                Float width = imgInfo.getWidth();
                Float height = imgInfo.getHeight();
                Float x = imgInfo.getLeft();
                Float y = imgInfo.getTop();

                //if width , height or x,y are not specified , then it means full layout with some padding from background photo
                if (width != null && height != null && x != null && y != null) {
                    int nWidth = Float.valueOf(width * ratio).intValue();
                    int nHeight = Float.valueOf(height * ratio).intValue();
                    int nX = Float.valueOf(x * ratio).intValue();
                    int nY = Float.valueOf(y * ratio).intValue();
                    System.out.println("----(" + nX + "," + nY + ") - (" + nWidth + "," + nHeight + ")----");

                    AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(nWidth, nHeight, nX, nY);
                    //layoutParams.height = AbsoluteLayout.LayoutParams.WRAP_CONTENT;
                    rootView.addView(_frontView, layoutParams);
                    this.frontView = _frontView;


                } else {
                    DisplayMetrics dm = Uitils.getResolution(getActivity());

                    int nXPadding = (int) (dm.widthPixels * 0.10);
                    int nYPadding = (int) (dm.heightPixels * 0.10);

                    int nWidth = dm.widthPixels - nXPadding * 2;
                    int nHeight = dm.heightPixels - nYPadding * 2;
                    int nX = nXPadding;
                    int nY = nYPadding;
                    AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(nWidth, nHeight, nX, nY);
                    rootView.addView(_frontView, layoutParams);
                    this.frontView = _frontView;

                }
            }
            else
            {

            }
        }

        List<UserProfileVO> fields = workInfo.getFields();
        String[] dontShowFields = { "foreground", "background",
                "privilege", "abbr", "video" };

        List<Typeface> fontTypefaces = MyApp.getInstance().getFontFaces();

        for (int i = 0; i < fields.size(); i++) {
            UserProfileVO field = fields.get(i);
            int fieldId = field.getId()==null?i:field.getId();

            String fieldName = field.getFieldName();
            String fieldValue = field.getValue();
            String fieldType = field.getFieldType();
            //String fieldColor = field.getColor();

            if (ArrayUtils.contains(dontShowFields,
                    fieldType.toLowerCase())) {
                continue;
            }

            //FontSettingVo fontSettingVo = field.getFontSettingVo();
            String fieldPosition = field.getPosition();

            if(//fieldColor == null ||
                fieldPosition == null || fieldValue== null || fieldValue.equals(""))
                continue;

            /*if(fontSettingVo == null)
                continue;*/

            /*if(!fieldColor.startsWith("#") && !fieldColor.equals("")){
                fieldColor = "#" + fieldColor;
            }*/

            final DragableTextView textView = new DragableTextView(this.getActivity());


            textView.setText(fieldValue);
            /*if(!fieldColor.equals(""))
                textView.setTextColor(Color.parseColor(fieldColor));
            textView.setTag(fieldName + fieldId);

            textView.setTypeface(fontTypefaces.get(fontSettingVo.getFontNameArrayIndex()));
            int styleId = 0;
            if (fontSettingVo.getFontStyle().equalsIgnoreCase("Normal")) {
                textView.setTypeface(textView.getTypeface(), styleId);
            } else if (fontSettingVo.getFontStyle().equalsIgnoreCase("Bold")) {
                styleId = Typeface.BOLD;
            } else if (fontSettingVo.getFontStyle().equalsIgnoreCase("Italic")) {
                styleId = Typeface.ITALIC;
            } else if (fontSettingVo.getFontStyle().equalsIgnoreCase("Bold Italic")) {
                styleId = Typeface.BOLD_ITALIC;
            }*/
            //textView.setTypeface(textView.getTypeface(), styleId);

            textView.setClickListener(new DragableTextView.ClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(TextView textView) {
                    if (currentEditTextView != null) {
                        currentEditTextView.setBackground(null);
                    }
                    currentEditTextView = textView;
                    textView.setBackgroundResource(R.drawable.textview_border);
                    /*if(fontSelector!=null) {
                        fontSelector.setTargetView(textView, tagFieldsMap.get((String) currentEditTextView.getTag()).getFontSettingVo() );
                    }*/
                }
            });
            //textView.setTextSize(Integer.valueOf(fontSettingVo.getFontSize()));
            AbsoluteLayout.LayoutParams layoutParams = getLayoutParams(ratio, fieldPosition);
            layoutParams.width= AbsoluteLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height= AbsoluteLayout.LayoutParams.WRAP_CONTENT;
            rootView.addView(textView,layoutParams);
        }
        rootView.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if(currentEditTextView!=null)
                {
                    currentEditTextView.setBackground(null);
                    currentEditTextView = null;
                }
            }
        });

        isAbbr = groupInfo.getAbbr();
        reShowForAbbr(isAbbr);
    }

    private AbsoluteLayout.LayoutParams getLayoutParams(float ratio, String fieldPosition) {
        int width = 0;
        int height = 0;
        int x = 0;
        int y = 0;
        fieldPosition = fieldPosition.replaceAll("(NSRect:)|\\{|\\}","");
        String[] temp = fieldPosition.split(",");
        width = Float.valueOf(Float.valueOf(temp[2]) * ratio).intValue();
        height = Float.valueOf(Float.valueOf(temp[3]) * ratio).intValue();
        x = Float.valueOf(Float.valueOf(temp[0]) * ratio).intValue();
        y = Float.valueOf(Float.valueOf(temp[1]) * ratio).intValue();
        AbsoluteLayout.LayoutParams params= new AbsoluteLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT));
        params.x = x;
        params.y = y;
        return params;
        //return new AbsoluteLayout.LayoutParams(width,height,x,y);
    }

    public void setGroupInfo(UserUpdateVO groupInfo) {
        this.groupInfo = groupInfo;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void removeBackGround() {
        this.rootView.setBackground(null);
        if(imageLoader == null)
            imageLoader = MyApp.getInstance().getImageLoader();

        imgBackgroundPhoto.refreshOriginalBitmap();
        imgBackgroundPhoto.setImageUrl("" , imageLoader);
        imgBackgroundPhoto.invalidate();
    }

    public void removeForeGround() {
        if (this.frontView == null) {
            return;
        }
        this.frontView.setVisibility(View.GONE);
        this.rootView.removeView(this.frontView);
        this.frontView = null;
    }

    public void reShowForAbbr(boolean abbr) {
        String[] abbrFieldTypes = new String[]{"birthday", "twitter", "email", "mobile", "phone", "facebook", "website" , "fax", "date", "url"};
        List<UserProfileVO> fields =  this.groupInfo.getFields();
        for (int i=0;i<fields.size();i++) {
            UserProfileVO field = fields.get(i);

            String fieldType = field.getFieldType();
            String fieldName = field.getFieldName();
            /*if (!ArrayUtils.contains(abbrFieldTypes, fieldType.toLowerCase())) {
                continue;
            }*/
            boolean bIsAbbrField = false;
            for(int j=0;j<abbrFieldTypes.length;j++)
            {
                if(fieldType.toLowerCase().contains(abbrFieldTypes[j])) {
                    bIsAbbrField = true;
                    break;
                }
            }
            if(!bIsAbbrField) continue;

            int fieldId = field.getId()==null?i:field.getId();

            String tag = field.getFieldName() + fieldId;
            TextView textView = (TextView) this.rootView.findViewWithTag(tag);
            if (textView == null) {
                continue;
            }
            if (abbr) {
                textView.setText(fieldName.substring(0, 1).toLowerCase() + ". " + field.getValue());
            } else {
                textView.setText(field.getValue());
            }
        }
    }

    public UserUpdateVO save() {
        float ratio = Uitils.getScreenRatioViaIPhone(this.getActivity());
        List<UserProfileVO> fields =  this.groupInfo.getFields();
        for (int i=0;i<fields.size();i++) {
            UserProfileVO field = fields.get(i);
            String fieldType = field.getFieldType();
            int fieldId = field.getId()==null?i:field.getId();
            String tag = field.getFieldName() + fieldId;
            TextView textView = (TextView) this.rootView.findViewWithTag(tag);
            if (textView == null) {
                continue;
            }
            /*FontSettingVo fontSettingVo = field.getFontSettingVo();
            field.setFont(fontSettingVo.getFontName()+":"+fontSettingVo.getFontSize()+":"+fontSettingVo.getFontStyle());
            int textColor = textView.getCurrentTextColor();
            String colorStr = Integer.toHexString(textColor);
            field.setColor(colorStr);*/

            int x = textView.getLeft();
            int y = textView.getTop();
            int width = textView.getWidth();
            int height = textView.getHeight();

            field.setPosition(x,y,width,height, ratio);
        }

        List<TcImageVO>  images = groupInfo.getImages();
        int foregroundPhotoIndex = -1;
        for(int i= 0;i<images.size(); i++)
        {
            if(images.get(i).getZIndex() == 1)
            {
                foregroundPhotoIndex = i;
                break;
            }
        }
        if(frontView != null && foregroundPhotoIndex > 0)
        {
            TcImageVO image = images.get(foregroundPhotoIndex);

            int foregroundLeft = (int)(frontView.getLeft()  / (ratio * 1.0));
            int foregroundTop = (int)(frontView.getTop() / (ratio * 1.0));
            int foregroundWidth = (int)(frontView.getWidth() / (ratio * 1.0));
            int foregroundHeight = (int)(frontView.getHeight() / (ratio * 1.0));

            image.setWidth(Float.valueOf((float) foregroundWidth));
            image.setHeight(Float.valueOf((float) foregroundHeight));
            image.setTop(Float.valueOf((float) foregroundTop));
            image.setLeft(Float.valueOf((float) foregroundLeft));
        }

        return this.groupInfo;
    }
}
