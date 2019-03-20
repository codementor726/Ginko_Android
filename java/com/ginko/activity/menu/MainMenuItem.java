package com.ginko.activity.menu;

import android.app.Activity;
import android.content.Intent;

import com.ginko.common.Logger;
import com.ginko.ginko.MyApp;

public class MainMenuItem {
	private int icon;
	private String text;

	private Class<?> toActivity;
	private Intent intent;
	
	private  MenuAction action;
    private Object menuItemObject;

	public MainMenuItem(String text) {
		this(text, -1);
	}

	public MainMenuItem(String text, int icon) {
		this(text, icon, (Intent) null);
	}

    public MainMenuItem(String text , Object obj , MenuAction action) {
        this.icon = -1;
        this.text = text;
        this.toActivity = null;
        this.menuItemObject = obj;
        this.action = action;
    }

    public MainMenuItem(String text, int icon, Class<?> taget) {
		this.icon = icon;
		this.text = text;
		this.toActivity = taget;
        this.menuItemObject = null;
	}

	public MainMenuItem(String text, int icon, Intent intent) {
		this.icon = icon;
		this.text = text;
		this.intent = intent;
        this.menuItemObject = null;
	}
	
	public MainMenuItem(String text, int icon, MenuAction action) {
		this.icon = icon;
		this.text = text;
		this.action = action;
        this.menuItemObject = null;
	}


	public void execute() {
		Activity currentActivity = MyApp.getInstance().getCurrentActivity();

		if (this.getAction()!= null){
			this.getAction().run(this.getMenuItemObject());
			return;
		}
        if (currentActivity==null){
            Logger.fatal("When select menu:" + this.getText() + " the currentActivity is null; it's impossible!");
            return;
        }
		Intent intent = this.intent;
		if (intent == null) {
			if (this.toActivity==null){
				return;
			}
			intent = new Intent(currentActivity, this.toActivity);
		}
		currentActivity.startActivity(intent);

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

	public Class<?> getToActivity() {
		return toActivity;
	}

	public void setToActivity(Class<? extends Activity> toActivity) {
		this.toActivity = toActivity;
	}

	public Intent getIntent() {
		return intent;
	}

	public void setIntent(Intent intent) {
		this.intent = intent;
	}

	public MenuAction getAction() {
		return action;
	}

	public void setAction(MenuAction action) {
		this.action = action;
	}

    public void setMenuItemObject(Object obj){this.menuItemObject = obj;}
    public Object getMenuItemObject(){return  this.menuItemObject;}

	public static interface MenuAction{
		public void run(Object menuItemObj);
	}
}
