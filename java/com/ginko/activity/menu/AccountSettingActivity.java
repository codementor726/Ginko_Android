package com.ginko.activity.menu;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.activity.group.GroupDetailActivity;
import com.ginko.api.request.ContactGroupRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.InputDialog;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.setup.Sign;
import com.ginko.vo.GroupVO;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccountSettingActivity extends MyBaseActivity implements View.OnClickListener {
    private ImageButton btnPrev;

    private List<JSONObject> reasons = new ArrayList<JSONObject>();

    private int selectedDeactivateReasonCodePosition = -1;

    private int deactiveReasonCode = -1;

    private RadioButton sort_by_fname , sort_by_lname;
    private boolean isSortByFName = true;

    private BaseAdapter adapter;
    private RadioButton.OnCheckedChangeListener radioChangeListener = new RadioButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        }
    };

    private View.OnClickListener sortRadioClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(sort_by_fname.isChecked())
            {
                Uitils.storeIsSortByFName(getApplicationContext() , true);
            }
            else
                Uitils.storeIsSortByFName(getApplicationContext() , false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        btnPrev = (ImageButton)findViewById(R.id.btnPrev);btnPrev.setOnClickListener(this);
        sort_by_fname = (RadioButton)findViewById(R.id.sort_by_fname);
        sort_by_lname = (RadioButton)findViewById(R.id.sort_by_lname);

        sort_by_fname.setOnClickListener(sortRadioClickListener);
        sort_by_lname.setOnClickListener(sortRadioClickListener);

        isSortByFName = Uitils.getIsSortByFName(getApplicationContext());
        if(isSortByFName)
        {
            sort_by_fname.setChecked(true);
            sort_by_lname.setChecked(false);
        }
        else
        {
            sort_by_fname.setChecked(false);
            sort_by_lname.setChecked(true);
        }

        //sort_by_fname.setOnCheckedChangeListener(radioChangeListener);


        findViewById(R.id.btn_deactivate).setOnClickListener(this);

        ListView list = (ListView)findViewById(R.id.list);
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return reasons.size();
            }

            @Override
            public Object getItem(int position) {
                return reasons.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) parent.getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater
                            .inflate(R.layout.list_item_deactivation_reason,
                                    parent, false);
                }
                JSONObject item = reasons.get(position);
                final int id = item.optInt("id");
                TextView tv = (TextView)view.findViewById(R.id.textView1);
                tv.setText(item.optString("description").trim());
                tv.setClickable(true);

                final ImageView check = (ImageView) view.findViewById(R.id.select);

                if (selectedDeactivateReasonCodePosition == position){
                    check.setImageResource(R.drawable.share_profile_selected);
                }else{
                    check.setImageResource(R.drawable.share_profile_non_selected);
                }

                check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectedDeactivateReasonCodePosition != position){
                            selectedDeactivateReasonCodePosition = position;
                            deactiveReasonCode = id;
                        }else{
                            selectedDeactivateReasonCodePosition = -1;
                            deactiveReasonCode = -1;
                        }
                        reRenderList();
                    }
                });

                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (selectedDeactivateReasonCodePosition != position){
                            selectedDeactivateReasonCodePosition = position;
                            deactiveReasonCode = id;
                        }else{
                            selectedDeactivateReasonCodePosition = -1;
                            deactiveReasonCode = -1;
                        }
                        reRenderList();
                    }
                });
                return view;
            }
        };

/*        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedDeactivateReasonCodePosition = position;
                adapter.notifyDataSetChanged();
            }
        });*/

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject item = reasons.get(position);
                final int reasonid = item.optInt("id");
                if (selectedDeactivateReasonCodePosition != position){
                    selectedDeactivateReasonCodePosition = position;
                    deactiveReasonCode = reasonid;
                }else{
                    selectedDeactivateReasonCodePosition = -1;
                    deactiveReasonCode = -1;
                }
                reRenderList();
            }
        });
        loadDeactivateReason();
    }

    private void reRenderList(){
        adapter.notifyDataSetChanged();
    }

    private void loadDeactivateReason(){
        UserRequest.listDeactivatedReason(new ResponseCallBack<List<JSONObject>>() {
            @Override
            public void onCompleted(JsonResponse<List<JSONObject>> response) {
                if(response.isSuccess()){
                    reasons.clear();
                    reasons.addAll(response.getData());
                    try {
                        reasons.add(new JSONObject("{\"id\":0,\"description\":\"Other\"}"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    reRenderList();
                }
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sort_by_fname.isChecked())
        {
            Uitils.storeIsSortByFName(getApplicationContext() , true);
        }
        else
            Uitils.storeIsSortByFName(getApplicationContext() , false);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;

            case R.id.btn_deactivate:
                if(deactiveReasonCode == -1){
                    Uitils.alert("Oops! Please select a deactivation reason.");
                    return;
                }

                // show a dialog to let user to enter password
                final String inputErrHint = "Please enter your password to deactivate your account.";

                final InputDialog passwordInputDialog = new InputDialog(this,
                        InputType.TYPE_TEXT_VARIATION_PASSWORD,
                        "Input password" , //title
                        "", //Hint
                        true , //show titlebar
                        getResources().getString(R.string.str_cancel) , //left button name
                        new InputDialog.OnButtonClickListener(){
                            @Override
                            public boolean onClick(Dialog dialog , View v, String input) {
                                return true;
                            }//left button clicklistener
                        },
                        getResources().getString(R.string.str_okay), //right button name
                        new InputDialog.OnButtonClickListener() //right button clicklistener
                        {
                            @Override
                            public boolean onClick(Dialog dialog , View v , String password) {
                                if(password.trim().equalsIgnoreCase(""))
                                {
                                    Toast.makeText(AccountSettingActivity.this , inputErrHint , Toast.LENGTH_LONG).show();
                                    return false;
                                }
                                if(password.trim().length() < 6)
                                {
                                    Uitils.alert(AccountSettingActivity.this , getResources().getString(R.string.str_alert_invalid_password_length));
                                    return false;
                                }

                                String otherReason = null;
                                final int reasonCode = deactiveReasonCode;
                                if(reasonCode == 0)//other
                                    otherReason = "Other";
                                else
                                    otherReason = "";

                                UserRequest.deactivate(password, reasonCode==0?null:reasonCode, otherReason, new ResponseCallBack<Void>() {
                                    @Override
                                    public void onCompleted(JsonResponse<Void> response) {
                                        if (response.isSuccess()){
                                                MyApp.getInstance().showSimpleAlertDiloag(AccountSettingActivity.this , R.string.str_alert_your_account_is_deactivated , new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Uitils.LogoutFromFacebook(AccountSettingActivity.this);
                                                    Uitils.setStringToSharedPreferences(AccountSettingActivity.this,
                                                            "sessionId", "");
                                                    Uitils.storeLastSyncTime(getApplicationContext(), RuntimeContext.getUser().getUserId(), 0);
                                                    RuntimeContext.setSessionId("");
                                                    MyApp.getInstance().clearGlobalVariables();
                                                    if(ContactMainActivity.getInstance() != null)
                                                        ContactMainActivity.getInstance().finish();

                                                    dialog.dismiss();
                                                    Intent intent = new Intent(AccountSettingActivity.this , Sign.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    AccountSettingActivity.this.startActivity(intent);
                                                    AccountSettingActivity.this.finish();
                                                }
                                            });
                                        }
                                        else {
                                            //MyApp.getInstance().showSimpleAlertDiloag(AccountSettingActivity.this, R.string.str_alert_fail_to_deactivate_your_account, null);
                                            MyApp.getInstance().showSimpleAlertDiloag(AccountSettingActivity.this, response.getErrorMessage(), null);
                                        }
                                    }
                                });

                                return true;
                            }
                        },
                        new InputDialog.OnEditorDoneActionListener() {

                            @Override
                            public void onEditorActionDone(Dialog dialog, String password) {
                                if(password.trim().equalsIgnoreCase(""))
                                {
                                    Toast.makeText(AccountSettingActivity.this , inputErrHint , Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if(password.trim().length() < 6)
                                {
                                    Uitils.alert(AccountSettingActivity.this, getResources().getString(R.string.str_alert_invalid_password_length));
                                    return;
                                }
                                dialog.dismiss();
                                String otherReason = null;
                                final int reasonCode = deactiveReasonCode;
                                if(reasonCode == 0)//other
                                    otherReason = "Other";
                                else
                                    otherReason = "";

                                UserRequest.deactivate(password, reasonCode==0?null:reasonCode, otherReason, new ResponseCallBack<Void>() {
                                    @Override
                                    public void onCompleted(JsonResponse<Void> response) {
                                        if (response.isSuccess()){
                                            MyApp.getInstance().showSimpleAlertDiloag(AccountSettingActivity.this , R.string.str_alert_your_account_is_deactivated , new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Uitils.LogoutFromFacebook(AccountSettingActivity.this);
                                                    Uitils.setStringToSharedPreferences(AccountSettingActivity.this,
                                                            "sessionId", "");
                                                    Uitils.storeLastSyncTime(getApplicationContext(), RuntimeContext.getUser().getUserId(), 0);
                                                    RuntimeContext.setSessionId("");
                                                    MyApp.getInstance().clearGlobalVariables();
                                                    if(ContactMainActivity.getInstance() != null)
                                                        ContactMainActivity.getInstance().finish();

                                                    dialog.dismiss();
                                                    Intent intent = new Intent(AccountSettingActivity.this , Sign.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    AccountSettingActivity.this.startActivity(intent);
                                                    AccountSettingActivity.this.finish();
                                                }
                                            });
                                        }
                                        else {
                                            //MyApp.getInstance().showSimpleAlertDiloag(AccountSettingActivity.this, R.string.str_alert_fail_to_deactivate_your_account, null);
                                            MyApp.getInstance().showSimpleAlertDiloag(AccountSettingActivity.this, response.getErrorMessage(), null);
                                        }
                                    }
                                });
                            }
                        }

                );
                passwordInputDialog.show();
                passwordInputDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        passwordInputDialog.showKeyboard();
                    }
                });
                break;
        }

    }
}
