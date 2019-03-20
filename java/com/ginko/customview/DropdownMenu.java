package com.ginko.customview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ginko.common.AppUtility;
import com.ginko.common.Logger;
import com.ginko.ginko.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DropdownMenu extends LinearLayout {
	private List<CustomMenuItem> menus = new ArrayList<CustomMenuItem>();;

	private MenuHandler handler;

	public DropdownMenu(Context context) {
		this(context, null);
	}

	public DropdownMenu(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	private void initial(Context context) {
//		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		this.setOrientation(LinearLayout.VERTICAL);
		
		this.setBackgroundResource(android.R.color.holo_blue_bright);
//		this.setLayoutParams(layoutParams);
		
		this.setPadding(AppUtility.dp2px(context, 10),
				AppUtility.dp2px(context, 5), AppUtility.dp2px(context, 10),
				AppUtility.dp2px(context, 5));
		updateMenu();
	}

	public DropdownMenu(final Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.DropdownMenu, defStyle, 0);

		int n = a.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.DropdownMenu_menu_src:
				Logger.error(R.xml.menu_entity_input_info + "");
				String menuxmlId = a.getString(attr);
				int value =getXmlIdByName(menuxmlId);
				 
				XmlResourceParser xrp = context.getResources().getXml(value);
				  try  
	                {  
	                    StringBuilder sb = new StringBuilder("");  
	                    //还没有到XML文档的结尾处  
	                    while(xrp.getEventType()!=XmlResourceParser.END_DOCUMENT)  
	                    {  
	                        //如果遇到了开始标签  
	                        if(xrp.getEventType()==XmlResourceParser.START_TAG)  
	                        {  
	                            //获取该标签的标签名  
	                            String tagName = xrp.getName();  
	                            if(tagName.equals("menu"))  
	                            {  
	                                //根据属性名获取属性值  
	                                String icon = xrp.getAttributeValue(0);  
	                                int iconId =  getDrawableIdByName(icon);
	                                //根据属性索引来获取属性值  
	                                String bookPrice = xrp.getAttributeValue(1);  
	                                this.addMenu(new CustomMenuItem(iconId,bookPrice));
	                            }  
	                            sb.append("\n");  
	                        }  
	                        //获取解析器的下一个事件  
	                        xrp.next();  
	                    }  
	      
	                }  
	                catch(XmlPullParserException e)  
	                {  
	                    Logger.error(e);
	                }  
	                catch(IOException e)  
	                {  
	                    Logger.error(e);
	                }  
				break;
		
			}
		}
		a.recycle();
		initial(context);
	}
	
	
	private int getDrawableIdByName(String name) {
		int index = name.indexOf("/");
		name = name.substring(index+1);
		
		Class<R.drawable> cls = R.drawable.class;
		int value = -1;
		try {
			value = cls.getDeclaredField(name).getInt(null);
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			Logger.error(e1);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			Logger.error(e1);
		} catch (NoSuchFieldException e1) {
			// TODO Auto-generated catch block
			Logger.error(e1);
		}
		return value;
	}
	
	private int getXmlIdByName(String name) {
		int index = name.indexOf("/");
		name = name.substring(index+1);
		Class<R.xml> cls = R.xml.class;
		int value = -1;
		try {
			value = cls.getDeclaredField(name).getInt(null);
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			Logger.error(e1);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			Logger.error(e1);
		} catch (NoSuchFieldException e1) {
			// TODO Auto-generated catch block
			Logger.error(e1);
		}
		return value;
	}

	public DropdownMenu addMenu(CustomMenuItem menu) {
		this.menus.add(menu);
		return this;
	}
	
	public void onSelectedCustomizedMenu(View view){
		Logger.error("onSelectedCustomizedMenu clicked");
	}

	public void updateMenu() {

		for (CustomMenuItem item : this.menus) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.drop_down_menu, this, false);
			
			ImageView menuIcon = (ImageView) view.findViewById(R.id.menu_icon); 
			TextView menuText = (TextView) view.findViewById(R.id.menu_text);
			
			
			menuIcon.setBackgroundResource(item.getIcon());
			menuText.setText(item.getText());
			
			this.addView(view);
		}

	}

	public static class CustomMenuItem {
		public CustomMenuItem() {

		}

		public CustomMenuItem(int icon, String text) {
			this.icon = icon;
			this.text = text;
		}

		public int getIcon() {
			return icon;
		}

		public void setIcon(int icon) {
			this.icon = icon;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		private int icon;
		private String text;
	}

	public static interface MenuHandler {
		void onSelected(CustomMenuItem menu);
	}

	public void addButtonToActionBar(Menu menu) {
		((Activity)this.getContext()).getMenuInflater().inflate(R.menu.drop_down_add_button, menu);
		
	}

	public void tiggle() {
		if (this.getVisibility() == View.VISIBLE){
			this.setVisibility(View.INVISIBLE);
		}else {
			this.setVisibility(View.VISIBLE);
		}
		
	}

}
