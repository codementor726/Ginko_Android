package com.ginko.customview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ginko.context.ConstValues;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.FontSettingVo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO still has some bug when change font type or font style;
 * @author Cyber
 *
 */
public class FontSelector extends LinearLayout {
	private TextView targetView;
	private FontSettingVo targetFontSetting;

//	private LinearLayout selectColorLayout;
    private Context mContext = null;

	public FontSelector(Context context) {
		this(context, null);

	}

    private List<Typeface> faces ;
    private int[] colors = new int[20];

    private DropdownSpinner spinnerFontName;
    private DropdownSpinner spinnerFontStyle;
    private DropdownSpinner spinnerFontSize;
    private DropdownSpinner spinnerFontColor;


	public FontSelector(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FontSelector(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
        this.mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.customize_fontselector, this);

		init(context);
	}

    private void init(Context context) {
		if(isInEditMode()){
			return;
		}

        //------------------ Font Name ---------------------------//
		spinnerFontName = (DropdownSpinner) findViewById(R.id.spinnerFontNameInHomeEditInfo);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		//ArrayAdapter<CharSequence> adapterFontName = ArrayAdapter.createFromResource(context, R.array.font_name_array, android.R.layout.simple_spinner_item);
        List<String> fontNameArrayList = new ArrayList<String>(Arrays.asList(ConstValues.fontNamesArray));

        this.faces = MyApp.getInstance().getFontFaces();


        FontNameSpinnerAdapter fontNameSpinnerAdapter = new FontNameSpinnerAdapter(context , fontNameArrayList);
		// Specify the layout to use when the list of choices appears
		//adapterFontName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
        //fontNameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerFontName.setListAdapter(fontNameSpinnerAdapter);
        spinnerFontName.setVisibleItemNo(8);

        spinnerFontName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fontName = ((FontNameSpinnerAdapter)parent.getAdapter()).getItem(position);
                changeFontName(fontName);
                if(view != null)
                {
                    spinnerFontName.setText(fontName);
                    spinnerFontName.setTypeface(faces.get(position));
                }
            }
        });

        //select the first font as default
        spinnerFontName.setText(ConstValues.fontNamesArray[0]);
        spinnerFontName.setTypeface(faces.get(0));

        //------------------ Font Style ---------------------------//
		spinnerFontStyle = (DropdownSpinner) findViewById(R.id.spinnerFontStyleInHomeEditInfo);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		//ArrayAdapter<CharSequence> adapterFontStyle = ArrayAdapter.createFromResource(context, R.array.font_style_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		//adapterFontStyle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        List<String> fontStyleArrayList = new ArrayList<String>(Arrays.asList(ConstValues.fontStyleArray));
        FontStyleSpinnerAdapter fontStyleSpinnerAdapter = new FontStyleSpinnerAdapter(context ,fontStyleArrayList);
		// Apply the adapter to the spinner
		spinnerFontStyle.setListAdapter(fontStyleSpinnerAdapter);
        spinnerFontStyle.setVisibleItemNo(4);
        spinnerFontStyle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fontStyle = ((FontStyleSpinnerAdapter) parent.getAdapter()).getItem(position);
                changeFontStyle(fontStyle);
                spinnerFontStyle.setText(fontStyle);
                int styleId = 0;
                if (fontStyle.equalsIgnoreCase("Normal")) {
                    spinnerFontStyle.setTypeface(spinnerFontStyle.getTypeface(), styleId);
                    return;
                } else if (fontStyle.equalsIgnoreCase("Bold")) {
                    styleId = Typeface.BOLD;
                } else if (fontStyle.equalsIgnoreCase("Italic")) {
                    styleId = Typeface.ITALIC;
                } else if (fontStyle.equalsIgnoreCase("Bold Italic")) {
                    styleId = Typeface.BOLD_ITALIC;
                }
                spinnerFontStyle.setTypeface(spinnerFontStyle.getTypeface(), styleId);
            }
        });
        spinnerFontStyle.setText("Normal");

        //---------------- Font Size -------------------------//
        spinnerFontSize = (DropdownSpinner) findViewById(R.id.spinnerFontSizeInHomeEditInfo);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		//ArrayAdapter<CharSequence> adapterFontSize = ArrayAdapter.createFromResource(context, R.array.font_size_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		//adapterFontSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
        List<String> fontSizeArrayList = new ArrayList<String>(Arrays.asList(ConstValues.fontSizeArray));
        FontSizeSpinnerAdapter fontSizeSpinnerAdapter = new FontSizeSpinnerAdapter(context , fontSizeArrayList);
		spinnerFontSize.setListAdapter(fontSizeSpinnerAdapter);
        spinnerFontSize.setVisibleItemNo(8);

		spinnerFontSize.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strFontSize = ((FontSizeSpinnerAdapter) parent.getAdapter()).getItem(position);
                int size = Integer.valueOf(strFontSize);
                changeFontSize(size);
                spinnerFontSize.setText(String.valueOf(size));
            }
        });
        spinnerFontSize.setText(ConstValues.fontSizeArray[0]);


        spinnerFontColor = (DropdownSpinner) findViewById(R.id.change_color);
        colors = context.getResources().getIntArray(R.array.font_selecotr_colors);
        List<Integer> fontColorArrayList = new ArrayList<Integer>();
        for(int i=0;i<colors.length;i++)
        {
            fontColorArrayList.add(new Integer(colors[i]));
        }

        FontColorAdapter fontColorAdapter =  new FontColorAdapter(context , fontColorArrayList);
        spinnerFontColor.setListAdapter(fontColorAdapter);
        spinnerFontColor.setVisibleItemNo(6);

        spinnerFontColor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Integer color = ((FontColorAdapter) parent.getAdapter()).getItem(position);
                spinnerFontColor.setBackgroundColor(color);
                changeFontColor(color);
            }
        });
        spinnerFontColor.setBackgroundColor(colors[0]);


		/*View btnChangeColor = findViewById(R.id.change_color);
		btnChangeColor.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showColorLayout(v);
			}
		});*/
		

		
	}

	/*
	 * <item>Nomal</item> <item>Sans</item> <item>Serif</item>
	 * <item>Monospace</item>
	 */
	public void changeFontName(String fontName) {
		TextView textView = this.getTargetView();
		if (textView != null) {
            Typeface face=Typeface.createFromAsset(mContext.getAssets(),
                    "fonts/"+fontName+".ttf");
            int styleId = 0;
            if(textView.getTypeface() != null)
                styleId = textView.getTypeface().getStyle();
            textView.setTypeface(face , styleId);
            FontSettingVo targetFont = this.getTargetFontSetting();
            if(targetFont != null)
            {
                targetFont.setFontName(fontName);
            }
		}
	}

	/*
	 * <item>Nomal</item> <item>Bold</item> <item>Italic</item> <item>Bold
	 * Italic </item>
	 */
	public void changeFontStyle(String styleStr) {
		TextView textView = this.getTargetView();
		if (textView == null) {
			return;
		}
		int styleId = 0;
		if (styleStr.equalsIgnoreCase("Normal")) {
			textView.setTypeface(Typeface.create(textView.getTypeface(), styleId));
			return;
		} else if (styleStr.equalsIgnoreCase("Bold")) {
			styleId = Typeface.BOLD;
		} else if (styleStr.equalsIgnoreCase("Italic")) {
			styleId = Typeface.ITALIC;
		} else if (styleStr.equalsIgnoreCase("Bold Italic")) {
			styleId = Typeface.BOLD_ITALIC;
		}

		textView.setTypeface(textView.getTypeface(), styleId);

        FontSettingVo targetFont = this.getTargetFontSetting();
        if(targetFont != null)
        {
            targetFont.setFontStyle(styleStr);
        }
	}

	public void changeFontSize(int fontSize) {
		TextView textView = this.getTargetView();
		if (textView == null) {
			return;
		}
        FontSettingVo targetFont = this.getTargetFontSetting();
        if(targetFont != null)
        {
            targetFont.setFontSize(String.valueOf(fontSize));
        }

		textView.setTextSize(fontSize);
	}
    private PopupWindow pw= null;



	public void changeFontColor(int color) {
		TextView textView = this.getTargetView();
		if (textView == null) {
			return;
		}

        textView.setTextColor(color);
//		this.selectColorLayout.setVisibility(INVISIBLE);
		
		
	}

	public TextView getTargetView() {
		return this.targetView;
	}
    public FontSettingVo getTargetFontSetting(){return this.targetFontSetting;}

	public void setTargetView(TextView targetView , FontSettingVo fontSetting) {
		this.targetView = targetView;
        this.targetFontSetting = fontSetting;

        int targetColor = targetView.getCurrentTextColor();

        int i = 0;
        for(i=0;i<colors.length;i++)
        {
            if(targetColor == colors[i])
                break;
        }
        if(i>=colors.length)
            i = -1;

//      ColorDrawable background = (ColorDrawable)targetView.getCurrentTextColor();

        if(spinnerFontColor != null) {
            if(i < 0)//if selected color is not in the array
            {
                spinnerFontColor.setBackgroundColor(targetView.getCurrentTextColor());
                spinnerFontColor.setSelectedPosition(0);
            }
            else
            {
                spinnerFontColor.setBackgroundColor(colors[i]);
                spinnerFontColor.setSelectedPosition(i);
            }
        }

        if(faces == null)
            faces = MyApp.getInstance().getFontFaces();

        if(spinnerFontName != null)
        {
            String fontName = this.targetFontSetting.getFontName();
            int fontArrayIndex = this.targetFontSetting.getFontNameArrayIndex();
            spinnerFontName.setText(fontName);
            spinnerFontName.setTypeface(faces.get(fontArrayIndex));
            spinnerFontName.setSelectedPosition(fontArrayIndex);
        }
        if(spinnerFontStyle != null)
        {
            String fontStyle = this.targetFontSetting.getFontStyle();
            int fontStyleArrayIndex = this.targetFontSetting.getFontStyleArrayIndex();
            spinnerFontStyle.setText(fontStyle);
            int styleId = 0;
            if (fontStyle.equalsIgnoreCase("Normal")) {
                spinnerFontStyle.setTypeface(spinnerFontStyle.getTypeface(), styleId);
            } else if (fontStyle.equalsIgnoreCase("Bold")) {
                styleId = Typeface.BOLD;
            } else if (fontStyle.equalsIgnoreCase("Italic")) {
                styleId = Typeface.ITALIC;
            } else if (fontStyle.equalsIgnoreCase("Bold Italic")) {
                styleId = Typeface.BOLD_ITALIC;
            }
            spinnerFontStyle.setTypeface(spinnerFontStyle.getTypeface(), styleId);
            spinnerFontStyle.setSelectedPosition(fontStyleArrayIndex);
        }
        if(spinnerFontSize != null)
        {
            String strFontSize = this.targetFontSetting.getFontSize();
            int fontSizeArrayIndex=  this.targetFontSetting.getFontSizeArrayIndex();
            int size = Integer.valueOf(strFontSize);
            spinnerFontSize.setText(String.valueOf(size));
            spinnerFontSize.setSelectedPosition(fontSizeArrayIndex);
        }

	}

    class FontNameSpinnerAdapter extends PopupListItemAdapter<String>{

        private Context mContext;
        public FontNameSpinnerAdapter(Context context, List<String> objects) {
            super(context,  objects);
            this.mContext = context;

        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row=inflater.inflate(R.layout.spinner_font_dropdown_row, parent, false);
            TextView label=(TextView)row.findViewById(R.id.spinnerTextView);
            label.setText(getItem(position));

            label.setTypeface(faces.get(position));
            return row;
        }
    }

    private class FontStyleSpinnerAdapter extends PopupListItemAdapter<String>{

        private Context mContext;
        public FontStyleSpinnerAdapter(Context context, List<String> objects) {
            super(context, objects);
            this.mContext = context;
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row=inflater.inflate(R.layout.spinner_font_dropdown_row, parent, false);
            TextView label=(TextView)row.findViewById(R.id.spinnerTextView);
            label.setText(getItem(position));
            int styleId = 0;
            if (getItem(position).equalsIgnoreCase("Normal")) {
                label.setTypeface(Typeface.create(label.getTypeface(), styleId));
            } else if (getItem(position).equalsIgnoreCase("Bold")) {
                styleId = Typeface.BOLD;
            } else if (getItem(position).equalsIgnoreCase("Italic")) {
                styleId = Typeface.ITALIC;
            } else if (getItem(position).equalsIgnoreCase("Bold Italic")) {
                styleId = Typeface.BOLD_ITALIC;
            }

            label.setTypeface(label.getTypeface(), styleId);
            return row;
        }
    }

    private class FontSizeSpinnerAdapter extends PopupListItemAdapter<String>{

        private Context mContext;
        public FontSizeSpinnerAdapter(Context context, List<String> objects) {
            super(context, objects);
            this.mContext = context;
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row=inflater.inflate(R.layout.spinner_font_dropdown_row, parent, false);
            TextView label=(TextView)row.findViewById(R.id.spinnerTextView);
            label.setText(getItem(position));

            return row;
        }
    }
    private class FontColorAdapter extends PopupListItemAdapter<Integer>{

        private Context mContext;
        public FontColorAdapter(Context context, List<Integer> objects) {
            super(context, objects);
            this.mContext = context;
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row=inflater.inflate(R.layout.popup_color_selector, parent, false);
            TextView label=(TextView)row.findViewById(R.id.spinnerTextView);
            //label.setText(getItem(position));
            label.setBackgroundColor(getItem(position));
            return row;
        }
    }

}
