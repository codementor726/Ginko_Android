package com.ginko.customview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.Logger;
import com.ginko.common.MyStringUtils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.R;
import com.ginko.vo.SharedInfoVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeafShareChooser extends RelativeLayout {
	private List<UserProfileVO> allHomeFields = new ArrayList<UserProfileVO>();
	private List<UserProfileVO> allWorkFields = new ArrayList<UserProfileVO>();

	private static final String SELECTED_COLOR = "green";

	private List<Integer> sharedHomeFields = new ArrayList<Integer>();
	private List<Integer> sharedWorkFields = new ArrayList<Integer>();

	private SectionViewAdapter<String, UserProfileVO> adapter;
	private SharingBean shareBean;
	private boolean manChange = true;

	public LeafShareChooser(Context context) {
		super(context);
	}

	public LeafShareChooser(final Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.customize_leaf_profile_choosor, this);
		final ListView list = (ListView) this.findViewById(R.id.list);

		adapter = new SectionViewAdapter<String, UserProfileVO>() {
			@Override
			protected View getItemView(UserProfileVO item, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.profile_list_item,
						parent, false);

				Integer fid = item.getId();
				if (sharedHomeFields.contains(fid)
						|| sharedWorkFields.contains(fid)) {
					changeStyleForSelectedItem(view);
				}

				TextView a = (TextView) view
						.findViewById(R.id.profile_field_name);
				a.setText(item.getFieldName() + ".");
				TextView b = (TextView) view
						.findViewById(R.id.profile_field_value);
				b.setText(item.getValue());
				return view;
			}

			@Override
			protected View getSectionView(String section, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.profile_list_header,
						parent, false);

				TextView a = (TextView) view
						.findViewById(R.id.list_header_title);
				a.setText(section);
				return view;
			}

		};

		list.setAdapter(adapter);

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object item = adapter.getItem(position);
				boolean isSection = adapter.isSection(item);
				if (isSection) {
					return;
				}
                UserProfileVO jsonField = (UserProfileVO) item;
				Integer fid = jsonField.getId();

				boolean isSelected = true;
				// is Home
				if (position < adapter.getSectionPosition("work")) {
					if (sharedHomeFields.contains(fid)) {
						sharedHomeFields.remove(fid);
						isSelected = false;
					} else {
						sharedHomeFields.add(fid);
					}
				} else {
					if (sharedWorkFields.contains(fid)) {
						sharedWorkFields.remove(fid);
						isSelected = false;
					} else {
						sharedWorkFields.add(fid);
					}
				}
				if (isSelected) {
					changeStyleForSelectedItem(view);
				} else {
					view.setBackgroundResource(android.R.color.transparent);
				}

				// if (isAllHomeSelected() && isAllWorkSelected()) {
				// selectShareAll();
				// }
				// list.destroyDrawingCache();
				boolean refreshData = correctShareRadioButtonStatus();
				// if(!refreshData){
				// adapter.notifyDataSetChanged();
				// }
			}

		});

		RadioGroup group = (RadioGroup) this.findViewById(R.id.shareGroup);

		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int radioId) {
				if (radioId == R.id.rdShareAll) {
					Logger.error("shard all changed.");
				}
				if (!manChange) {
					return;
				}
				int checkedRadioButtonId = arg0.getCheckedRadioButtonId();

				RadioButton rb = (RadioButton) findViewById(radioId);
				if (rb.isSelected()) {
					return;
				}
				if (radioId != checkedRadioButtonId) {
					return;
				}
				if (radioId == R.id.rdShareAll) {
					selectShareAll();
				} else if (radioId == R.id.rdChatOnly) {
					selectChatOnly();
				}
			}
		});
	}

	public void setShareBean(SharingBean shareBean) {
		this.shareBean = shareBean;
	}

	public SharingBean getShareBean() {
		return shareBean;
	}
	
	public SharingBean getSelectedAsShareBean(){
		SharingBean sb = new SharingBean();
		
		int shareStatus = getShareStatus();
		sb.setSharingStatus(shareStatus);
		
		String shardHomeFids = MyStringUtils
				.arrayToString(this.sharedHomeFields.toArray(new Integer[0]));
		String shardWorkFids = MyStringUtils
				.arrayToString(this.sharedWorkFields.toArray(new Integer[0]));
		
		if (!isAllHomeSelected()) {
			sb.setSharedHomeFieldIds(shardHomeFids);
		}
		if (!isAllWorkSelected()) {
			sb.setSharedWorkFieldIds(shardWorkFids);
		}
		return sb;
	}

	public void updateItemSelectedStauts() {
		if (shareBean == null) {
			return;
		}
		
		if (shareBean.sharedAll()) {
			selectShareAll();
		} 
		sharedHomeFields.addAll(Arrays.asList(MyStringUtils
				.stringToArray(shareBean.getSharedHomeFieldIds())));
		sharedWorkFields.addAll(Arrays.asList(MyStringUtils
				.stringToArray(shareBean.getSharedWorkFieldIds())));
		
		if (shareBean.isShareAllHome()){
			this.shareAllHome();
		}
		if (shareBean.isSharedAllWork()){
			this.shareAllWork();
		}

		if (shareBean.getSharingStatus() == 4) {
			checkRadioButton(
					((RadioButton) this.findViewById(R.id.rdChatOnly)), true);
		}

		if (shareBean.sharedAll()) {
			checkRadioButton(
					((RadioButton) this.findViewById(R.id.rdShareAll)), true);
		} else {
			checkRadioButton(
					((RadioButton) this.findViewById(R.id.rdShareAll)), false);
			checkRadioButton(
					((RadioButton) this.findViewById(R.id.rdChatOnly)), false); // incorrect
		}
	}

	public void retrieveUserProfile(Integer contactUid) {
		UserInfoRequest.getInfo(contactUid, new ResponseCallBack<UserWholeProfileVO>() {

			@Override
			public void onCompleted(JsonResponse<UserWholeProfileVO> response) {
				if (response.isSuccess()) {
					UserWholeProfileVO data = response.getData();
					allHomeFields = fillProfileList( data.getHome());
					allWorkFields = fillProfileList(data.getWork());
					adapter.addItems("home", allHomeFields);
					adapter.addItems("work", allWorkFields);

					SharedInfoVO sharing = data.getShare();
					if (sharing != null) {
						shareBean = new SharingBean();
						shareBean.setSharingStatus(sharing.getSharingStatus());
						shareBean.setSharedHomeFieldIds(sharing.getSharedHomeFIds());

						shareBean.setSharedWorkFieldIds(sharing.getSharedWorkFIds());

					}
					updateItemSelectedStauts();
					adapter.notifyDataSetChanged();
				
				}

			}

            private List<UserProfileVO> fillProfileList (UserUpdateVO data) {
                List<UserProfileVO> allFields = new ArrayList<UserProfileVO>();

                String[] dontShowFields = { "Foreground", "Background",
                        "Privilege", "Abbr", "Video" };
                if (data != null) {
                    List<UserProfileVO> arrFields = data.getFields();
                    for (int i = 0; i < arrFields.size(); i++) {
                        UserProfileVO jsonField = arrFields.get(i);
                        String fName = jsonField.getFieldName();
                        if (ArrayUtils.contains(dontShowFields,
                                fName.toLowerCase())) {
                            continue;
                        }
                        allFields.add(jsonField);

                    }
                }
                return allFields;
            }
		});
	}

	public LeafShareChooser(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private void selectShareAll() {
		checkRadioButton(((RadioButton) this.findViewById(R.id.rdShareAll)),
				true);
		sharedHomeFields.clear();
		sharedWorkFields.clear();
		shareAllHome();
		shareAllWork();
		adapter.notifyDataSetChanged();
	}
	
	private void shareAllHome(){
		for (UserProfileVO jsonField : this.allHomeFields) {
			Integer fid = jsonField.getId();
			sharedHomeFields.add(fid);
		}
	}
	
	private void shareAllWork(){
		for (UserProfileVO jsonField : this.allWorkFields) {
			Integer fid = jsonField.getId();
			sharedWorkFields.add(fid);
		}
	}

	private void selectChatOnly() {
		checkRadioButton(((RadioButton) this.findViewById(R.id.rdChatOnly)),
				true);
		sharedHomeFields.clear();
		sharedWorkFields.clear();
		adapter.notifyDataSetChanged();
	}

	/*
	 * If return false, means ,the method didn't call
	 * adapter.notifyDataSetChanged();
	 */
	protected boolean correctShareRadioButtonStatus() {
		if (sharedHomeFields.size() == 0 && sharedWorkFields.size() == 0) {
			selectChatOnly();
			return true;
		}
		// boolean isAllHomeSelected = this.isAllHomeSelected();
		// boolean isAllWorkSelected = this.isAllWorkSelected();
		if (isAllSelected()) {
			selectShareAll();
			return true;
		}
		checkRadioButton(((RadioButton) this.findViewById(R.id.rdChatOnly)),
				false);
		checkRadioButton(((RadioButton) this.findViewById(R.id.rdShareAll)),
				false);
		return false;
	}

	private void checkRadioButton(RadioButton radio, boolean checked) {
		this.manChange = false;
		radio.setChecked(checked);
		// Logger.error( radio.isChecked() + "");
		this.manChange = true;
	}

	private boolean isAllSelected() {
		return this.isAllHomeSelected() && this.isAllWorkSelected();
	}

	private boolean isNoneSelected() {
		return this.sharedHomeFields.size() == 0
				&& this.sharedWorkFields.size() == 0;
	}

	private boolean isAllWorkSelected() {
		for (UserProfileVO jsonField : this.allWorkFields) {
			Integer fid = jsonField.getId();
			if (!this.sharedWorkFields.contains(fid)) {
				return false;
			}
		}
		return true;
	}

	private boolean isAllHomeSelected() {
		for (UserProfileVO jsonField : this.allHomeFields) {
			Integer fid = jsonField.getId();
			if (!this.sharedHomeFields.contains(fid)) {
				return false;
			}
		}
		return true;
	}

	private int getShareStatus() {
		RadioGroup shareGroup = (RadioGroup) this.findViewById(R.id.shareGroup);
		int radioButtonId = shareGroup.getCheckedRadioButtonId();
		if (radioButtonId == R.id.rdChatOnly) {
			return 4;
		}
		if (radioButtonId == R.id.rdShareAll) {
			return 3;
		}
		if (sharedHomeFields.size() > 0 && sharedWorkFields.size() == 0) {
			return 1;
		}
		if (sharedWorkFields.size() > 0 && sharedHomeFields.size() == 0) {
			return 2;
		}
		return 3;
	}

	private void changeStyleForSelectedItem(View view) {
		view.setBackgroundColor(Color.parseColor(SELECTED_COLOR));
	}

}
