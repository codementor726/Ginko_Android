package com.ginko.customview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;

import com.ginko.customview.ColorPickerView.OnColorChangedListener;

public class ColorPickerDialog extends Dialog {
	private final boolean debug = true;
	private final String TAG = "ColorPicker";
	
	Context context;
	private String title;
	private int mInitialColor;
    private OnColorChangedListener mListener;

	/**
     * ��ʼ��ɫ��ɫ
     * @param context
     * @param title �Ի������
     * @param listener �ص�
     */
    public ColorPickerDialog(Context context, String title, 
    		OnColorChangedListener listener) {
    	this(context, Color.BLACK, title, listener);
    }
    
    /**
     * 
     * @param context
     * @param initialColor ��ʼ��ɫ
     * @param title ����
     * @param listener �ص�
     */
    public ColorPickerDialog(Context context, int initialColor, 
    		String title, OnColorChangedListener listener) {
        super(context);
        this.context = context;
        mListener = listener;
        mInitialColor = initialColor;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager manager = getWindow().getWindowManager();
		int height = (int) (manager.getDefaultDisplay().getHeight() * 0.5f);
		int width = (int) (manager.getDefaultDisplay().getWidth() * 0.7f);
		ColorPickerView myView = new ColorPickerView(context, height, width);
        setContentView(myView);
        myView.setmInitialColor(mInitialColor);
        myView.setmListener(mListener);
        setTitle(title);
    }
    
    
    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getmInitialColor() {
		return mInitialColor;
	}

	public void setmInitialColor(int mInitialColor) {
		this.mInitialColor = mInitialColor;
	}
}
