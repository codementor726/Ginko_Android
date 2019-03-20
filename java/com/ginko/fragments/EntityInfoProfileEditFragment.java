package com.ginko.fragments;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ginko.activity.entity.EntityInfoItem;
import com.ginko.activity.entity.EntityInfoItemView;
import com.ginko.common.Uitils;
import com.ginko.customview.DragableTextView;
import com.ginko.customview.FontSelector;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.EntityInfoDetailVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.FontSettingVo;
import com.ginko.vo.UserProfileVO;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntityInfoProfileEditFragment extends Fragment {

	private View view;

    private boolean isUICreated =false;

    private EntityInfoVO entityInfo;
    private AbsoluteLayout rootView;
    private FontSelector fontSelector;

    private TextView currentEditTextView;

    public int position = 0;

    private HashMap<String , EntityInfoDetailVO> tagFieldsMap;

    private boolean isAbbr = false;
    private float ratio = 1.0f;

    public void refreshFieldsData()
    {
        if(isUICreated) {

        }
    }

    public EntityInfoProfileEditFragment()
    {}


    public EntityInfoProfileEditFragment(EntityInfoVO info , FontSelector fontselector , float _ratio){
        super();
        this.entityInfo = info;
        this.fontSelector = fontselector;
        this.ratio = _ratio;
        tagFieldsMap = new HashMap<String , EntityInfoDetailVO>();
        List<EntityInfoDetailVO> fieldList = this.entityInfo.getEntityInfoDetails();
        for(int i=0;i<fieldList.size();i++)
        {
            EntityInfoDetailVO field = fieldList.get(i);
            int fieldId = field.getId()==null?i:field.getId();
            tagFieldsMap.put(field.getFieldName() + fieldId , field);
        }

    }

    public void setEntityInfoItemList(EntityInfoVO info)
    {
        init(info);
    }
    public void setFontSelector(FontSelector font_selector)
    {
        this.fontSelector = font_selector;
    }

    public void setRatio(float _ratio)
    {
        this.ratio = _ratio;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        isUICreated = false;
    }

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_entity_profile_edit, container, false);
        rootView = (AbsoluteLayout) view.findViewById(R.id.container);

        isUICreated = true;

        init(this.entityInfo);
        return view;
	}

    public void updateTag(boolean bAddr)
    {
        reShowForAbbr(bAddr);
    }

    public void init(EntityInfoVO info)
    {
        this.entityInfo = info;

        tagFieldsMap = new HashMap<String , EntityInfoDetailVO>();
        List<EntityInfoDetailVO> fieldList = this.entityInfo.getEntityInfoDetails();
        for(int i=0;i<fieldList.size();i++)
        {
            EntityInfoDetailVO field = fieldList.get(i);
            int fieldId = field.getId()==null?i:field.getId();
            tagFieldsMap.put(field.getFieldName() + fieldId , field);
        }

        if(!isUICreated) return;
        rootView.removeAllViews();

        List<EntityInfoDetailVO> fields = entityInfo.getEntityInfoDetails();

        String[] dontShowFields = { "foreground", "background",
                "privilege", "abbr", "video" , "keysearch" , "search words" };
        List<Typeface> fontTypefaces = MyApp.getInstance().getFontFaces();

        for (int i = 0; i < fields.size(); i++) {
            EntityInfoDetailVO field = fields.get(i);
            int fieldId = field.getId()==null?i:field.getId();

            String fieldName = field.getFieldName();
            String fieldValue = field.getValue();
            String fieldType = field.getType();
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

            /*if(!fieldColor.startsWith("#") && !fieldColor.equals("")){
                fieldColor = "#" + fieldColor;
            }*/

            final DragableTextView textView = new DragableTextView(this.getActivity());

            textView.setText(fieldValue);
            /*if(!fieldColor.equals(""))
                textView.setTextColor(Color.parseColor(fieldColor));*/
            textView.setTag(fieldName + fieldId);
            //textView.setTypeface(fontTypefaces.get(fontSettingVo.getFontNameArrayIndex()));
            int styleId = 0;
            /*if (fontSettingVo.getFontStyle().equalsIgnoreCase("Normal")) {
                textView.setTypeface(textView.getTypeface(), styleId);
            } else if (fontSettingVo.getFontStyle().equalsIgnoreCase("Bold")) {
                styleId = Typeface.BOLD;
            } else if (fontSettingVo.getFontStyle().equalsIgnoreCase("Italic")) {
                styleId = Typeface.ITALIC;
            } else if (fontSettingVo.getFontStyle().equalsIgnoreCase("Bold Italic")) {
                styleId = Typeface.BOLD_ITALIC;
            }*/
            textView.setTypeface(textView.getTypeface(), styleId);

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
                        fontSelector.setTargetView(textView, tagFieldsMap.get((String) currentEditTextView.getTag()).getFontSettingVo());
                    }*/
                }
            });
            //textView.setTextSize(Integer.valueOf(fontSettingVo.getFontSize()));

            AbsoluteLayout.LayoutParams layoutParams = getLayoutParams(ratio, fieldPosition);
            layoutParams.width= AbsoluteLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height= AbsoluteLayout.LayoutParams.WRAP_CONTENT;
            rootView.addView(textView,layoutParams);
        }

        isAbbr = this.entityInfo.getAbbr();
        reShowForAbbr(isAbbr);
    }


    @Override
    public void onResume() {
        super.onResume();
        isUICreated = true;
        refreshFieldsData();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        //saveEditingInfoItems();
        super.onDestroyView();
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
        String[] abbrFieldTypes = new String[]{ "hours" , "date", "fax" , "twitter", "email", "mobile", "phone", "facebook", "url"};

        List<EntityInfoDetailVO> fields =  this.entityInfo.getEntityInfoDetails();
        for (int i=0;i<fields.size();i++) {
            EntityInfoDetailVO field = fields.get(i);
            String fieldType = field.getType();
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
                textView.setText(fieldType.substring(0, 1).toLowerCase() + ". " + field.getValue());
            } else {
                textView.setText(field.getValue());
            }
        }
    }

    public void save() {
        float ratio = Uitils.getScreenRatioViaIPhone(this.getActivity());
        List<EntityInfoDetailVO> fields =  this.entityInfo.getEntityInfoDetails();
        for (int i=0;i<fields.size();i++) {
            EntityInfoDetailVO field = fields.get(i);
            String fieldType = field.getType();
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
    }
}
