package com.ginko.activity.cb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.api.request.CBRequest;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.MyStringUtils;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.SectionViewAdapter;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.CbEmailVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SaveCBActivity extends MyBaseActivity implements View.OnClickListener{
    private ImageButton btnPrev , btnConfirm;
    private ImageView btnDelete;
    private ImageView imgEmailStatus;
    private ImageView imgShareAll , imgChatOnly;
    private TextView txtDescription;
    private ListView list;
    private RelativeLayout emailTypeLayout;
    private ImageView confirmedEmailIcon , invalidEmailIcon;
    private LinearLayout invalidEmailIconLayout;

    private RelativeLayout shareOnLayout , shareOffLayout , bottomLayout;

	// private String authUrl;
	// private String provider;
	// private String authType;
	// private String password;
	private List<UserProfileVO> allHomeFields = new ArrayList<UserProfileVO>();
	private List<UserProfileVO> allWorkFields = new ArrayList<UserProfileVO>();

	private CbEmailVO cb;

    private List<Integer> originalSharedHomeFields = new ArrayList<Integer>();
    private List<Integer> originalSharedWorkFields = new ArrayList<Integer>();

	private List<Integer> sharedHomeFields = new ArrayList<Integer>();
	private List<Integer> sharedWorkFields = new ArrayList<Integer>();

	private SectionViewAdapter<String, UserProfileVO> adapter;
	
	private boolean manChange= true;

	private boolean isNewCBCreate = true;

    private boolean isShareAllSelected = false;
    private boolean isChatOnlySelected = false;

    private boolean originalActiveStatus = true;
    private boolean activeStatus = true;

    private boolean isChanged = false;
    private int originalSharingStatus = 1;

    private boolean bIsSelectedWorkFlag = false;
    private boolean bIsSelectedHomeFlag = false;
    private int nWorkSectionPos = -1;
    private int nHomeSectionPos = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save_cb);

		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			this.cb = (CbEmailVO) extras.getSerializable("cb");
            this.isNewCBCreate = extras.getBoolean("isNewCB");

			TextView txtEmail = (TextView) findViewById(R.id.txtEmail);
			txtEmail.setText(this.cb.getEmail());

		}

		if (this.cb == null) {
			Uitils.alert("Unknown Error", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SaveCBActivity.this.finish();

				}
			});
		}


        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnDelete = (ImageView)findViewById(R.id.btnDelete); btnDelete.setOnClickListener(this);

        imgShareAll = (ImageView)findViewById(R.id.imgShareAll); imgShareAll.setOnClickListener(this);
        imgChatOnly = (ImageView)findViewById(R.id.imgChatOnly); imgChatOnly.setOnClickListener(this);

        txtDescription = (TextView)findViewById(R.id.txtDescription);

        shareOnLayout = (RelativeLayout)findViewById(R.id.shareOnLayout);
        shareOffLayout = (RelativeLayout)findViewById(R.id.shareOffLayout);
        bottomLayout = (RelativeLayout)findViewById(R.id.bottomLayout);

        imgEmailStatus = (ImageView)findViewById(R.id.emailStatus);
        if (StringUtils.equalsIgnoreCase(cb.getValid(), "yes")) {
            imgEmailStatus.setImageResource(R.drawable.confirm);
        } else {
            imgEmailStatus.setImageResource(R.drawable.warning);
        }

        emailTypeLayout = (RelativeLayout)findViewById(R.id.emailTypeLayout);
        confirmedEmailIcon = (ImageView)findViewById(R.id.confirmedEmailIcon);
        invalidEmailIcon = (ImageView)findViewById(R.id.invalidEmailIcon);
        invalidEmailIconLayout = (LinearLayout)findViewById(R.id.invalidEmailIconLayout);

        String strEmailAddress = cb.getEmail();
        if(strEmailAddress.toLowerCase().endsWith("yahoo.com") || strEmailAddress.toLowerCase().endsWith("gmail.com") || strEmailAddress.toLowerCase().endsWith("hotmail.com")) {
            emailTypeLayout.setVisibility(View.VISIBLE);
            if (strEmailAddress.toLowerCase().endsWith("yahoo.com")) {
                confirmedEmailIcon.setImageResource(R.drawable.cb_import_yahoo);
                invalidEmailIcon.setImageResource(R.drawable.cb_import_yahoo);
                invalidEmailIconLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addYahoo();
                    }
                });
            } else if (strEmailAddress.toLowerCase().endsWith("gmail.com")) {
                confirmedEmailIcon.setImageResource(R.drawable.cb_import_gmail);
                invalidEmailIcon.setImageResource(R.drawable.cb_import_gmail);
                invalidEmailIconLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addGmail();
                    }
                });
            }else if (strEmailAddress.toLowerCase().endsWith("hotmail.com")) {
                confirmedEmailIcon.setImageResource(R.drawable.cb_import_msn);
                invalidEmailIcon.setImageResource(R.drawable.cb_import_msn);
                invalidEmailIconLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addLive();
                    }
                });
            }

            if (StringUtils.equalsIgnoreCase(cb.getValid(), "yes")) {
                confirmedEmailIcon.setVisibility(View.VISIBLE);
                invalidEmailIconLayout.setVisibility(View.GONE);
            } else {
                confirmedEmailIcon.setVisibility(View.GONE);
                invalidEmailIconLayout.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            emailTypeLayout.setVisibility(View.GONE);
        }

        originalActiveStatus = StringUtils.equalsIgnoreCase(cb.getActive(), "yes");
        activeStatus = originalActiveStatus;

        RadioButton radioTurnOn = (RadioButton) this.findViewById(R.id.rdTurnOn);
        radioTurnOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    activeStatus = true;
                    updateTurnOnOff();
                    checkIsChanged();
                }
            }
        });
        RadioButton radioTurnOff = (RadioButton) this.findViewById(R.id.rdTurnOff);
        radioTurnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    activeStatus = false;
                    updateTurnOnOff();
                    checkIsChanged();
                }
            }
        });

        if (activeStatus) {
            radioTurnOn.setChecked(true);
        } else {
            radioTurnOff.setChecked(true);
        }

        updateTurnOnOff();

        if(isNewCBCreate) {
            btnConfirm.setVisibility(View.VISIBLE);
            bottomLayout.setVisibility(View.GONE);
        }
        else {
            btnConfirm.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.VISIBLE);
        }

        //get shared home & work fields
		sharedHomeFields.addAll(Arrays.asList(MyStringUtils.stringToArray(cb
				.getSharedHomeFieldIds())));
        originalSharedHomeFields.addAll(Arrays.asList(MyStringUtils.stringToArray(cb
                .getSharedHomeFieldIds())));

		sharedWorkFields.addAll(Arrays.asList(MyStringUtils.stringToArray(cb
				.getSharedWorkFieldIds())));
        originalSharedWorkFields.addAll(Arrays.asList(MyStringUtils.stringToArray(cb
                .getSharedWorkFieldIds())));



		// TODO initial the shared Items...

		list = (ListView) this.findViewById(R.id.list);

		adapter = new SectionViewAdapter<String, UserProfileVO>() {
			@Override
			protected View getItemView(UserProfileVO item, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) SaveCBActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.profile_list_item,
						parent, false);

				Integer fid = item.getId();
				if (sharedHomeFields.contains(fid)
						|| sharedWorkFields.contains(fid)) {
                    view.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_selected_color));
				}
                else
                {
                    view.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_unselected_color));
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
				LayoutInflater inflater = (LayoutInflater) SaveCBActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.profile_list_header,
						parent, false);

				TextView a = (TextView) view
						.findViewById(R.id.list_header_title);
				a.setText(section);

                if (section.equals("home"))
                {
                    if (bIsSelectedHomeFlag && allHomeFields.size() > 0) {
                        view.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_selected_color));
                    }
                    else
                    {
                        view.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_unselected_color));
                    }
                }
                else if (section.equals("work"))
                {
                    if (bIsSelectedWorkFlag && allWorkFields.size() > 0)
                    {
                        view.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_selected_color));
                    }
                    else
                    {
                        view.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_unselected_color));
                    }
                }
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
                    if (position == 0) {    // share home info
                        if (allHomeFields.size() > 0) {
                            bIsSelectedHomeFlag = !bIsSelectedHomeFlag;
                            selectShareAllHomeInfo(bIsSelectedHomeFlag);
                        }
                    }
                    else    // share work info
                    {
                        if (allWorkFields.size() > 0)
                        {
                            bIsSelectedWorkFlag = !bIsSelectedWorkFlag;
                            selectShareAllWorkInfo(bIsSelectedWorkFlag);
                        }

                    }

                    if (bIsSelectedHomeFlag == true && bIsSelectedWorkFlag == true)
                    {
                        isShareAllSelected = true;
                        isChatOnlySelected = false;
                        checkIsChanged();
                        updateSharingCheckBox();
                        selectShareAll(true);
                    }
                    else if ((bIsSelectedHomeFlag == false && allHomeFields.size() <= 0) && bIsSelectedWorkFlag == true)
                    {
                        isShareAllSelected = true;
                        isChatOnlySelected = false;
                        checkIsChanged();
                        updateSharingCheckBox();
                        selectShareAll(true);
                    }
                    else if ((bIsSelectedWorkFlag == false && allWorkFields.size() <= 0) && bIsSelectedHomeFlag == true)
                    {
                        isShareAllSelected = true;
                        isChatOnlySelected = false;
                        checkIsChanged();
                        updateSharingCheckBox();
                        selectShareAll(true);
                    }
                    else{
                        isShareAllSelected = false;
                        if (bIsSelectedHomeFlag == true || bIsSelectedWorkFlag == true)
                            isChatOnlySelected = false;
                        else
                            isChatOnlySelected = true;
                        checkIsChanged();
                        updateSharingCheckBox();
                    }

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

                if(isSelected)
                    view.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_selected_color));
                else
                    view.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_unselected_color));

                if(sharedWorkFields.size() == 0 && sharedHomeFields.size() == 0)
                {
                    isChatOnlySelected = true;
                    isShareAllSelected = false;
                }
                else if(sharedHomeFields.size() == allHomeFields.size() && sharedWorkFields.size() == allWorkFields.size())
                {
                    isShareAllSelected = true;
                    isChatOnlySelected = false;
                }
                else
                {
                    isShareAllSelected = false;
                    isChatOnlySelected = false;
                }

                if (allHomeFields.size() > 0 && sharedHomeFields.size() == allHomeFields.size()) {
                    bIsSelectedHomeFlag = true;
                }
                else
                {
                    bIsSelectedHomeFlag = false;
                }

                if (allWorkFields.size() > 0 && sharedWorkFields.size() == allWorkFields.size())
                {
                    bIsSelectedWorkFlag = true;
                }
                else
                {
                    bIsSelectedWorkFlag = false;
                }

                checkIsChanged();
                updateSharingCheckBox();
                adapter.notifyDataSetChanged();
			}

		});

        originalSharingStatus = cb.getSharingStatus();

		if (cb.getSharingStatus() == ConstValues.SHARE_CHAT_ONLY) {
            isShareAllSelected = false;
            isChatOnlySelected = true;
		}
        else if(cb.getSharingStatus() == ConstValues.SHARE_BOTH
                && StringUtils.isBlank(cb.getSharedHomeFieldIds())
                && StringUtils.isBlank(cb.getSharedWorkFieldIds()))
        {
            isShareAllSelected = true;
            isChatOnlySelected = false;
        }
        else
        {
            isShareAllSelected = false;
            isChatOnlySelected = false;
        }
		
        updateSharingCheckBox();


		UserInfoRequest.getInfo(null, new ResponseCallBack<UserWholeProfileVO>() {

			@Override
			public void onCompleted(JsonResponse<UserWholeProfileVO> response) {
				if (response.isSuccess()) {
					UserWholeProfileVO data = response.getData();
					allHomeFields = fillProfileList(data.getHome());
					allWorkFields = fillProfileList(data.getWork());
					adapter.addItems("home", allHomeFields);
					adapter.addItems("work", allWorkFields);
					
					if (isShareAllSelected){
						selectShareAll(true);
					}else{
						adapter.notifyDataSetChanged();
					}
				}

			}

			private List<UserProfileVO> fillProfileList (UserUpdateVO data) {
				List<UserProfileVO> allFields = new ArrayList<UserProfileVO>();

				String[] dontShowFields = { "foreground", "background",
						"privilege", "abbr", "video" };
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
		} , true);
	}

	private void selectShareAll(boolean bShareAll) {
		sharedHomeFields.clear();
		sharedWorkFields.clear();
        if(bShareAll) {
            for (UserProfileVO jsonField : this.allHomeFields) {
                Integer fid = jsonField.getId();
                sharedHomeFields.add(fid);
            }

            for (UserProfileVO jsonField : this.allWorkFields) {
                Integer fid = jsonField.getId();
                sharedWorkFields.add(fid);
            }
        }
		adapter.notifyDataSetChanged();
	}
    private void selectShareAllHomeInfo(boolean bShareAllHomeInfo) {
        sharedHomeFields.clear();
        if(bShareAllHomeInfo) {
            for (UserProfileVO jsonField : this.allHomeFields) {
                Integer fid = jsonField.getId();
                sharedHomeFields.add(fid);
            }
        }
        adapter.notifyDataSetChanged();
    }
    private void selectShareAllWorkInfo(boolean bShareAllWorkInfo) {
        sharedWorkFields.clear();
        if(bShareAllWorkInfo) {

            for (UserProfileVO jsonField : this.allWorkFields) {
                Integer fid = jsonField.getId();
                sharedWorkFields.add(fid);
            }
        }
        adapter.notifyDataSetChanged();
    }

	private void selectChatOnly() {
		cb.setSharedHomeFieldIds("");
		cb.setSharedWorkFieldIds("");
		sharedHomeFields.clear();
		sharedWorkFields.clear();
		adapter.notifyDataSetChanged();
	}

    private void updateTurnOnOff()
    {
        if(activeStatus)
        {
            shareOnLayout.setVisibility(View.VISIBLE);
            shareOffLayout.setVisibility(View.GONE);
            txtDescription.setText(getString(R.string.builder_automatically_send_exchange_requests_to_new_email_contacts));
        }
        else
        {
            shareOnLayout.setVisibility(View.GONE);
            shareOffLayout.setVisibility(View.VISIBLE);
            txtDescription.setText(getString(R.string.builder_manually_send_exchange_requests_to_new_email_contacts));
        }
    }

    private void updateSharingCheckBox()
    {
        if(isShareAllSelected)
            imgShareAll.setImageResource(R.drawable.share_profile_selected);
        else
            imgShareAll.setImageResource(R.drawable.share_profile_non_selected);

        if(isChatOnlySelected)
            imgChatOnly.setImageResource(R.drawable.share_profile_selected);
        else
            imgChatOnly.setImageResource(R.drawable.share_profile_non_selected);
    }

	private void checkIsChanged()
    {
        boolean isChanged = false;
        if(isNewCBCreate) return;//if creates new CB account , then always enable confirm button

        if(originalActiveStatus != activeStatus) {
            isChanged = true;
            showHideConfirmButton(isChanged);
            return;
        }
        else if(originalSharingStatus != cb.getSharingStatus())
        {
            isChanged = true;
            showHideConfirmButton(isChanged);
            return;
        }
        else if(originalSharedHomeFields.size() != sharedHomeFields.size() || originalSharedWorkFields.size() != sharedWorkFields.size())
        {
            isChanged = true;
            showHideConfirmButton(isChanged);
            return;
        }
        else
        {
            if(originalSharedHomeFields.size() == sharedHomeFields.size()) {
                for (int i = 0; i < sharedHomeFields.size(); i++) {
                    if (!originalSharedHomeFields.contains(sharedHomeFields.get(i))) {
                        isChanged = true;
                        showHideConfirmButton(isChanged);
                        return;
                    }
                }
            }
            if(originalSharedWorkFields.size() == sharedWorkFields .size()) {
                for (int i = 0; i < sharedWorkFields.size(); i++) {
                    if (!originalSharedWorkFields.contains(sharedWorkFields.get(i))) {
                        isChanged = true;
                        showHideConfirmButton(isChanged);
                        return;
                    }
                }
            }
        }

        showHideConfirmButton(isChanged);

    }

    private void showHideConfirmButton(boolean isChanged)
    {
        if(isChanged)
            btnConfirm.setVisibility(View.VISIBLE);
        else
            btnConfirm.setVisibility(View.GONE);
    }


	private boolean isAllSelected(){
		return this.isAllHomeSelected() && this.isAllWorkSelected();
	}
	
	private boolean isNoneSelected(){
		return this.sharedHomeFields.size()== 0 && this.sharedWorkFields.size()== 0;
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

	@Override
	public void onPause() {
		super.onPause();
	}



	private void saveCB() {
		// EditText txtEmail = (EditText) findViewById(R.id.txtEmail);
		// String email = txtEmail.getText().toString().trim();

		// CbEmailVO cb = new CbEmailVO();
		// String oauthtoken = getOauthToken(this.authUrl);
		//
		// cb.setOauthtoken(oauthtoken);
		// cb.setProvider(this.provider);
		// cb.setAuthType(this.authType);
		// cb.setPassword(this.password);
		// cb.setEmail(email);
		String shardHomeFids = MyStringUtils
				.arrayToString(this.sharedHomeFields.toArray(new Integer[0]));
		String shardWorkFids = MyStringUtils
				.arrayToString(this.sharedWorkFields.toArray(new Integer[0]));
		int sharing = this.getShareStatus();
		cb.setSharingStatus(sharing);
		cb.setActive(activeStatus ? "yes" : "no");
		if (!isAllHomeSelected()) {
			cb.setSharedHomeFieldIds(shardHomeFids);
		}
		if (!isAllWorkSelected()) {
			cb.setSharedWorkFieldIds(shardWorkFids);
		}

		CBRequest.saveCB(cb, new ResponseCallBack<JSONObject>() {

			@Override
			public void onCompleted(JsonResponse<JSONObject> response) {
                if(response.isSuccess())
                {
                    JSONObject resultObj = response.getData();
                    if(resultObj.has("err"))
                    {
                        try {
                            JSONObject errOb = resultObj.getJSONObject("err");
                            MyApp.getInstance().showSimpleAlertDiloag(SaveCBActivity.this ,errOb.optString("errMsg" , "") , new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    SaveCBActivity.this.finish();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    else
                        SaveCBActivity.this.finish();
                }
                else
                {
                    MyApp.getInstance().showSimpleAlertDiloag(SaveCBActivity.this , R.string.str_alert_failed_to_save_builder_account , null);
                }
			}
		});
	}

	private void delete(int cbId) {
		CBRequest.deleteEmail( cbId, new ResponseCallBack<Void>() {

			@Override
			public void onCompleted(JsonResponse<Void> response) {

                if(response.isSuccess())
                {
                    SaveCBActivity.this.finish();
                }
                else
                {
                    MyApp.getInstance().showSimpleAlertDiloag(SaveCBActivity.this , R.string.str_alert_failed_to_delete_builder_account , null);
                }
			}
		});
	}

	private int getShareStatus() {

		if (isChatOnlySelected) {
			return ConstValues.SHARE_CHAT_ONLY;
		}
		if (isShareAllSelected) {
			return ConstValues.SHARE_BOTH;
		}
		if (sharedHomeFields.size() > 0 && sharedWorkFields.size() == 0) {
			return ConstValues.SHARE_HOME;
		}
		if (sharedWorkFields.size() > 0 && sharedHomeFields.size() == 0) {
			return ConstValues.SHARE_WORK;
		}
		return ConstValues.SHARE_BOTH;
	}


    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;
            case R.id.btnConfirm:
                saveCB();
                break;

            case R.id.btnDelete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Confirm");
                builder.setMessage(getResources().getString(R.string.str_delete_builder_confirm_dialog));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        delete(cb.getId());
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;

            case R.id.imgShareAll:
                if(isShareAllSelected)
                {
                    isShareAllSelected = false;

                    if (allWorkFields.size() > 0)
                        bIsSelectedWorkFlag = isShareAllSelected;

                    if (allHomeFields.size() > 0)
                        bIsSelectedHomeFlag = isShareAllSelected;

                    isChatOnlySelected = true;

                    checkIsChanged();
                    updateSharingCheckBox();
                    selectShareAll(false);
                }
                else {
                    if(isChatOnlySelected)
                        showHideConfirmButton(true);
                    else
                        checkIsChanged();

                    isShareAllSelected = true;

                    if (allWorkFields.size() > 0)
                        bIsSelectedWorkFlag = isShareAllSelected;

                    if (allHomeFields.size() > 0)
                        bIsSelectedHomeFlag = isShareAllSelected;

                    isChatOnlySelected = false;

                    updateSharingCheckBox();
                    selectShareAll(true);
                }
                break;
            case R.id.imgChatOnly:
                if(!isChatOnlySelected) {
                    isShareAllSelected = false;
                    bIsSelectedWorkFlag = isShareAllSelected;
                    bIsSelectedHomeFlag = isShareAllSelected;
                    isChatOnlySelected = true;

                    checkIsChanged();
                    updateSharingCheckBox();
                    selectChatOnly();
                }
                break;
        }
    }

    public void addGmail() { this.openAddCbViewByOAuth("google");}

    public void addYahoo() {
        this.openAddCbViewByOAuth("yahoo");
    }

    public void addLive() {
        this.openAddCbViewByOAuth("live");
    }

    private void openAddCbViewByOAuth(String provider) {
        Intent intent = new Intent();
        intent.setClass(SaveCBActivity.this, AddOAuthCBActivity.class);
        intent.putExtra("provider", provider);
        startActivity(intent);
    }
}
