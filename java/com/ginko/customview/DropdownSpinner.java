package com.ginko.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.ginko.ginko.R;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is useful to make custom DropDown spinner.You can Set Strings and
 * Images in this spinner.
 * 
 * @author Ankit Thakkar
 * 
 */
public class DropdownSpinner extends TextView implements OnClickListener,
		OnItemClickListener, OnDismissListener, OnItemSelectedListener {

	private PopupWindow pw;
	private ListView lv;
	private PopupListItemAdapter adapter;
	private List<String> list;
	private int selectedPosition;
	private OnClickListener onClickListener = null;
	private OnItemClickListener onItemClickListener;
	private OnItemSelectedListener onItemSelectedListener;
	private int popupHeight, popupWidth;
	// private Drawable myBackgroundDrawable;
	private Drawable listDrawable;
	private float textSize = 20;
	private int textColor = Color.BLACK, backgroundColor = Color.WHITE;
	private String TAG = getClass().getName();
	// private Context context;
	private int visibleItemNo = 1;
	private int leftPadding = 8, rightPadding = 8, topPadding = 4,
			bottomPadding = 4;

    private Context mContext;

	public DropdownSpinner(Context context) {
		super(context);
        init(context);
	}

	public DropdownSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DropdownSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {

        this.mContext = context;
        setTypeface(null, Typeface.NORMAL);

        textSize = mContext.getResources().getDimension(R.dimen.dropdown_spinner_textsize);

		listDrawable = getRoundDrawable();
		// myBackgroundDrawable = getRoundDrawable();
		setPadding(8, 8, 8, 8);
		setGravity(Gravity.CENTER_VERTICAL);
		//setTextSize(textSize);
		setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.dropdown_spinner_textsize));

		setTextColor(textColor);
		setText("");
		super.setOnClickListener(this);
		// setBackgroundDrawable(myBackgroundDrawable);
		popupHeight = 100;
		popupWidth = 150;
	}

	/**
	 * Set the item text font color in drop down.
	 * 
	 * @param color
	 *            - color code in integer
	 */
	public void setItemTextColor(int color) {
		this.textColor = color;
	}

	/**
	 * Set the item background color in drop down.
	 * 
	 * @param color
	 *            - color code in integer
	 */
	public void setItemBackgroundColor(int color) {
		this.backgroundColor = color;
	}

	private Drawable getRoundDrawable() {
		ShapeDrawable drawable = new ShapeDrawable();
		drawable.getPaint().setColor(Color.WHITE);
		// drawable.setShape(new RoundRectShape(r, null, null));
		return drawable;
	}

	public void setListAdapter(PopupListItemAdapter adapter) {
		if (adapter != null) {
			this.adapter = adapter;
			if (lv == null)
				lv = new ListView(getContext());
			// lv.setCacheColorHint(0);
			lv.setBackgroundDrawable(listDrawable);
			lv.setAdapter(adapter);
			// lv.setDividerHeight(1);
			lv.setOnItemClickListener(this);
			lv.setOnItemSelectedListener(this);
			lv.setSelector(android.R.color.transparent);
			lv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			selectedPosition = 0;
		} else {
			selectedPosition = -1;
			this.adapter = null;
		}
		// refreshView();
	}

	// private void refreshView() {
	// if (adapter != null) {
	// PopupListItem popupListItem = adapter.getItem(selectedPosition);
	// setText(popupListItem.toString());
	// if(popupListItem.getResId() != -1)
	// setCompoundDrawablesWithIntrinsicBounds(popupListItem.getResId(),
	// 0, 0, 0);
	// }
	// }
	
	/**
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public void setItemPadding(int left, int top, int right, int bottom) {
		this.leftPadding = left;
		this.rightPadding = right;
		this.topPadding = top;
		this.bottomPadding = bottom;
		setListViewHeightBasedOnChildren(lv);

	}

	/**
	 * Set the item text size
	 * 
	 * @param size
	 *            - size of text
	 */
	public void setItemTextSize(int size) {
		this.textSize = size;
		setListViewHeightBasedOnChildren(lv);
	}

	@Override
	public void onClick(View v) {
		if (pw == null || !pw.isShowing()) {
			pw = new PopupWindow(v);
			pw.setContentView(lv);
            if(popupWidth<v.getWidth())
                pw.setWidth(v.getWidth());
            else
			    pw.setWidth(popupWidth);
			pw.setHeight(popupHeight);
			pw.setBackgroundDrawable(new BitmapDrawable());
			pw.setOutsideTouchable(false);
			pw.setFocusable(true);
			pw.setClippingEnabled(true);
			pw.showAsDropDown(v, 0, 0);
			pw.setOnDismissListener(this);
		}
		if (onClickListener != null)
			onClickListener.onClick(v);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (pw != null)
			pw.dismiss();
		selectedPosition = arg2;
		// refreshView();
		if (onItemClickListener != null)
			onItemClickListener.onItemClick(arg0, arg1, arg2, arg3);

	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if (onItemSelectedListener != null)
			onItemSelectedListener.onItemSelected(arg0, arg1, arg2, arg3);
	}



	/**
	 * Set the height of DropDown spinner equal to number of visible rows
	 * 
	 * @param no
	 *            - number (Integer) of visible item row
	 */
	public void setVisibleItemNo(int no) {
		this.visibleItemNo = no;
		setListViewHeightBasedOnChildren(lv);
	}

	/**
	 * Add the single string item in spinner
	 * 
	 * @param item
	 *            - String add in spinner
	 */
	public void addItem(String item) {
		if (item == null)
			throw new NullPointerException("Item is null.");

		if (list == null) {
			list = new ArrayList<String>();
			list.clear();
			//adapter = new PopupListItemAdapter<String>(getContext(), list);
			//setListAdapter(adapter);
		}
		list.add(item);
		if(adapter != null)
			adapter.notifyDataSetChanged();

	}

	/**
	 * Set the string array in spinner
	 * 
	 * @param arr
	 *            - array of string
	 */
	public void setItems(String[] arr) {
		if (arr == null)
			throw new NullPointerException("Items Array is null.");
		if (list == null)
			list = new ArrayList<String>();
		list.clear();
		for (String text : arr) {
			list.add(text);
		}
		if(adapter != null) {

			setListAdapter(adapter);
		}
		// refreshView();
	}

	@Override
	public void setTextSize(float size) {
		super.setTextSize(size);
		textSize = size;
	}

	@Override
	public void onDismiss() {
		pw = null;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		if (onItemSelectedListener != null)
			onItemSelectedListener.onNothingSelected(arg0);
	}

	@Override
	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public void setOnItemSelectedListener(
			OnItemSelectedListener onItemSelectedListener) {
		this.onItemSelectedListener = onItemSelectedListener;
	}

	/**
	 * Return the position of currently selected item within the adapter's data
	 * set.
	 * 
	 * @return int position - (starting at 0).
	 */
	public int getSelectedPosition() {
		return selectedPosition;
	}

	/**
	 * Set the currently selected item.
	 * 
	 * @param selectedPosition
	 *            index - (starting at 0) of the data item to be selected.
	 */
	public void setSelectedPosition(int selectedPosition) {
		this.selectedPosition = selectedPosition;
		// refreshView();
	}

	private void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}
		int totalHeight = 0, totalWidth = 0;
		// int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(),
		// MeasureSpec.AT_MOST);
		// Log.e(TAG, "desired width:" + desiredWidth);
		for (int i = 0; i < visibleItemNo; i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			Log.e(TAG, "item width:" + listItem.getMeasuredWidth());
			if (listItem.getMeasuredWidth() > totalWidth) {
				Log.e(TAG, "max item width:" + listItem.getMeasuredWidth());
				totalWidth = listItem.getMeasuredWidth();
			}
		}
		// Log.e(TAG, "final height:" + totalHeight);
		Log.e(TAG, "final width:" + totalWidth);
		LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (visibleItemNo - 1));
		// Log.e(TAG, "height:" + params.height);
		popupHeight = params.height;
		popupWidth = totalWidth;
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// // TODO Auto-generated method stub
	// //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	// //setMeasuredDimension(45, 45);
	// }

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		//setText("");
	}

	public class SizeNotMatchException extends Exception {
		private static final long serialVersionUID = 1L;

		// Parameterless Constructor
		public SizeNotMatchException() {
		}

		// Constructor that accepts a message
		public SizeNotMatchException(String message) {
			super(message);
		}
	}
}
