package com.ginko.activity.profiles;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
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
import com.ginko.activity.im.LocationMapViewerActivity;
import com.ginko.common.Uitils;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.FontSettingVo;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Locale;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link com.ginko.activity.profiles.PurpleContactUserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@SuppressWarnings("ALL")
public class PurpleContactUserProfileFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "type";

    private String type;

    private View view;
    private CustomNetworkImageView imgBackgroundPhoto;
    private AbsoluteLayout rootView;
    private AbsoluteLayout photoContainer;


    private ImageLoader imageLoader;

    private UserUpdateVO typeInfo;

    private CustomNetworkImageView frontView;

    private boolean isAbbr = false;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param type Parameter 1.
     * @return A new instance of fragment UserProfileFragment.
     */
    public static PurpleContactUserProfileFragment newInstance(String type) {
        PurpleContactUserProfileFragment fragment = new PurpleContactUserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, type);
        fragment.setArguments(args);
        return fragment;
    }

    public PurpleContactUserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        imgBackgroundPhoto = (CustomNetworkImageView)view.findViewById(R.id.imgBackgroundPhoto);
        rootView = (AbsoluteLayout) view.findViewById(R.id.container);
        photoContainer = (AbsoluteLayout)view.findViewById(R.id.photoContainer);

        return view;
    }


    public void init(UserUpdateVO workInfo) {
        if(workInfo == null)
            return;

        rootView.removeAllViews();
        photoContainer.removeAllViews();

        this.typeInfo = workInfo;
        if(imageLoader == null)
            imageLoader = MyApp.getInstance().getImageLoader();

        String backgroundPhotoUrl = "";
        TcImageVO foregroundPhoto= null;
        for(int i=0; i<typeInfo.getImages().size(); i++)
        {
            if(typeInfo.getImages().get(i).getZIndex() == 0 && backgroundPhotoUrl.equals(""))
                backgroundPhotoUrl = typeInfo.getImages().get(i).getUrl();
            else if(typeInfo.getImages().get(i).getZIndex() == 1 && foregroundPhoto == null)
            {
                foregroundPhoto = typeInfo.getImages().get(i);
            }
        }


        if(typeInfo.getImages()!=null && typeInfo.getImages().size()>0) {
            imgBackgroundPhoto.setImageUrl(backgroundPhotoUrl, imageLoader);
        }

        float ratio = Uitils.getScreenRatioViaIPhone(getActivity());

        //foreground photo
        if(typeInfo.getImages()!= null && typeInfo.getImages().size()>0 && foregroundPhoto != null)
        {
            CustomNetworkImageView _frontView = new CustomNetworkImageView(this.getActivity());
            _frontView.setAdustImageAspect(false);
            _frontView.setImageScaleType(ImageView.ScaleType.FIT_CENTER);
            _frontView.setImageUrl(foregroundPhoto.getUrl() , imageLoader);

            Float width = foregroundPhoto.getWidth();
            Float height = foregroundPhoto.getHeight();
            Float x = foregroundPhoto.getLeft();
            Float y = foregroundPhoto.getTop();
            //if width , height or x,y are not specified , then it means full layout with some padding from background photo
            if(width!=null && height != null && x != null && y != null)
            {
                int nWidth = Float.valueOf(width * ratio).intValue();
                int nHeight = Float.valueOf(height * ratio).intValue();
                int nX = Float.valueOf(x * ratio).intValue();
                int nY = Float.valueOf(y * ratio).intValue();
                AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(nWidth,nHeight,nX,nY);
                photoContainer.addView(_frontView,layoutParams);

            }
            else
            {
                DisplayMetrics dm = Uitils.getResolution(getActivity());

                int nXPadding = (int)(dm.widthPixels*0.10);
                int nYPadding = (int)(dm.heightPixels*0.10);

                int nWidth = dm.widthPixels - nXPadding*2;
                int nHeight = dm.heightPixels - nYPadding*2;
                int nX = nXPadding;
                int nY = nYPadding;
                AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(nWidth,nHeight,nX,nY);
                photoContainer.addView(_frontView,layoutParams);
            }

            this.frontView = _frontView;
        }


        String[] dontShowFields = { "foreground", "background",
                "privilege", "abbr", "video" };
        List<Typeface> fontTypefaces = MyApp.getInstance().getFontFaces();


        List<UserProfileVO> fields = workInfo.getFields();
        for (int i = 0; i < fields.size(); i++) {
            UserProfileVO field = fields.get(i);
            final int fieldId = field.getId()==null?0:field.getId();
            final String fieldName = field.getFieldName();
            final String fieldValue = field.getValue();
            final String fieldType = field.getFieldType();
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

            //if(fontSettingVo == null)
            //    continue;

            //if(!fieldColor.startsWith("#")){
            //    fieldColor = "#" + fieldColor;
            //}

            TextView textView = new TextView(this.getActivity());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(fieldValue.trim().equals("")) return;
                    if(fieldType.equalsIgnoreCase("phone") || fieldType.equalsIgnoreCase("mobile"))
                    {
                        try
                        {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + fieldValue.trim()));
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                getActivity().startActivity(intent);
                            }
                        }catch(Exception e){e.printStackTrace();}
                    }
                    else if(fieldType.equalsIgnoreCase("email"))
                    {
                        try {
                            Intent email = new Intent(Intent.ACTION_SEND);
                            email.putExtra(Intent.EXTRA_EMAIL, new String[]{fieldValue.trim()});
                            email.putExtra(Intent.EXTRA_SUBJECT, "");
                            email.putExtra(Intent.EXTRA_TEXT, "");

                            // need this to prompts email client only
                            email.setType("message/rfc822");

                            getActivity().startActivity(Intent.createChooser(email, "Choose an Email client"));
                        }catch (Exception e){e.printStackTrace();}
                    }
                    else if(fieldName.toLowerCase().trim().contains("website"))
                    {
                        try {
                            String url = fieldValue.trim();
                            if(!url.startsWith("http://") && !url.startsWith("https://"))
                                url = "http://"+fieldValue.trim();
                            Uri webpage = Uri.parse(url);
                            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                getActivity().startActivity(intent);
                            }
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if(fieldType.equalsIgnoreCase("address"))
                    {
                        try
                        {
                            String uri = String.format(Locale.ENGLISH, "geo:0,0?q=%s", fieldValue.trim());
                            Intent goolgeMapIntent = new Intent(Intent.ACTION_VIEW , Uri.parse(uri));
                            //goolgeMapIntent.setData(geoLocation);
                            // check if Google Maps is supported on given device
                            if (goolgeMapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                getActivity().startActivity(goolgeMapIntent);
                            }
                            else
                            {
                                //open my ginko lcoation viewer activity
                                Intent locationMessageIntent = new Intent(getActivity(), LocationMapViewerActivity.class);
                                locationMessageIntent.putExtra("address" , fieldValue.trim());

                                getActivity().startActivity(locationMessageIntent);
                            }
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if(fieldType.equalsIgnoreCase("facebook"))
                    {

                    }
                    else if(fieldType.equalsIgnoreCase("twitter"))
                    {

                    }
                }
            });
            textView.setText(fieldValue);
            /*if(!fieldColor.equals(""))
                textView.setTextColor(Color.parseColor(fieldColor));*/
            textView.setTag(fieldName + fieldId);

            //textView.setTypeface(fontTypefaces.get(fontSettingVo.getFontNameArrayIndex()));
            int styleId = 0;
           /* if (fontSettingVo.getFontStyle().equalsIgnoreCase("Normal")) {
                textView.setTypeface(textView.getTypeface(), styleId);
            } else if (fontSettingVo.getFontStyle().equalsIgnoreCase("Bold")) {
                styleId = Typeface.BOLD;
            } else if (fontSettingVo.getFontStyle().equalsIgnoreCase("Italic")) {
                styleId = Typeface.ITALIC;
            } else if (fontSettingVo.getFontStyle().equalsIgnoreCase("Bold Italic")) {
                styleId = Typeface.BOLD_ITALIC;
            }
            textView.setTypeface(textView.getTypeface(), styleId);
            textView.setTextSize(Integer.valueOf(fontSettingVo.getFontSize()));*/

            AbsoluteLayout.LayoutParams layoutParams = getLayoutParams(ratio, fieldPosition);
            layoutParams.width= AbsoluteLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height= AbsoluteLayout.LayoutParams.WRAP_CONTENT;
            rootView.addView(textView, layoutParams);
        }

        isAbbr = typeInfo.getAbbr();
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

    public void reShowForAbbr(boolean abbr) {
        String[] abbrFieldTypes = new String[]{"birthday", "twitter", "email", "mobile", "phone", "facebook", "website" , "fax", "date", "url"};
        for (UserProfileVO field : this.typeInfo.getFields()) {
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

            int fieldId = field.getId()==null?0:field.getId();

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
}
