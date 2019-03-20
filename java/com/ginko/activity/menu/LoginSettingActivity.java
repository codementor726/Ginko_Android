package com.ginko.activity.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.api.request.UserRequest;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.LoginEmailVO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginSettingActivity extends MyBaseActivity implements View.OnClickListener {
    private List<LoginEmailVO> loginEmails = new ArrayList<LoginEmailVO>();
    private Integer selectedItemIndex = -1;

    private RelativeLayout activityRootView;
    private EditText edtEmail;
    private ImageButton btnPrev;
    private Pattern pattern;

    private boolean isShownKeybaord = false;

    private BaseAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_setting);

        findViewById(R.id.btn_delete).setOnClickListener(this);
        findViewById(R.id.btn_send_confirmation).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);

        btnPrev = (ImageButton)findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(this);

        edtEmail = (EditText)findViewById(R.id.et_email);
        edtEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addEmail();
                return false;
            }
        });

        ListView list = (ListView)findViewById(R.id.list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItemIndex = position;
                adapter.notifyDataSetChanged();
            }
        });
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return loginEmails.size();
            }

            @Override
            public Object getItem(int position) {
                return loginEmails.get(position);
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
                            .inflate(R.layout.list_item_checkbox_subitem,
                                    parent, false);
                }
                final LoginEmailVO item = loginEmails.get(position);

                TextView tv = (TextView)view.findViewById(R.id.textView1);
                TextView textSubItem = (TextView)view.findViewById(R.id.textSubItem);
                tv.setText(item.getEmail());
                textSubItem.setText(item.getActivated().equalsIgnoreCase("yes") ? "Confirmed" : "Pending");

                final ImageView check = (ImageView) view.findViewById(R.id.select);

                if(selectedItemIndex == position)
                    check.setImageResource(R.drawable.radio_on);
                else
                    check.setImageResource(R.drawable.radio_off);
                return view;
            }
        };

        activityRootView = (RelativeLayout) findViewById(R.id.rootLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isShownKeybaord) {
                        isShownKeybaord = true;
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                } else {
                    if (isShownKeybaord) {
                        isShownKeybaord = false;
                    }
                }
            }
        });

        //For GAD-1264
        activityRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(activityRootView.getApplicationWindowToken(), 0);

                activityRootView.requestFocus();
                return false;
            }
        });

/*        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedDeactivateReasonCodePosition = position;
                adapter.notifyDataSetChanged();
            }
        });*/

        list.setAdapter(adapter);
        loadEmails();
    }

    private void loadEmails() {
        UserRequest.getLoginEmails(new ResponseCallBack<List<LoginEmailVO>>() {
            @Override
            public void onCompleted(JsonResponse<List<LoginEmailVO>> response) {
                if (response.isSuccess()) {
                    loginEmails.clear();
                    selectedItemIndex = -1;
                    loginEmails.addAll(response.getData());
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isShownKeybaord)
        {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (v.getId()) {
            case R.id.btnPrev:
                finish();
                break;
            case R.id.btn_add:
                addEmail();
                break;

            case R.id.btn_delete:
                deleteEmails();
                break;

            case R.id.btn_send_confirmation:
                sendConfirmation();
                break;

        }
    }
    private boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        if(pattern == null)
            pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }
    private void addEmail() {
        String email = edtEmail.getText().toString().trim();
        if (email.isEmpty() || !isEmailValid(email)){
            Uitils.alert("Please enter a correct Email");
            return;
        }
        UserRequest.addLogin(email, new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    loadEmails();
                    edtEmail.setText("");
                }
                else
                {
                    Uitils.alert(LoginSettingActivity.this , response.getErrorMessage());
                }
            }
        });
    }

    private void deleteEmails() {
        if (selectedItemIndex == -1){
            Uitils.alert("Select an email to delete.");
            return;
        }
        if(loginEmails.size()>selectedItemIndex)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm");
            builder.setMessage("Do you want to delete this user login?");
            builder.setPositiveButton(R.string.str_confirm_dialog_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //TODO
                    UserRequest.removeLogins(loginEmails.get(selectedItemIndex).getEmail(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                loadEmails();
                            } else {
                                Uitils.alert(LoginSettingActivity.this, response.getErrorMessage());
                            }
                        }
                    });
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.str_confirm_dialog_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //TODO
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            Uitils.alert("Select an email to delete.");
            return;
        }
    }

    private void sendConfirmation() {
        if (selectedItemIndex == -1){
            Uitils.alert("Oops! Please select an email to send a confirmation link.");
            return;
        }
        if(loginEmails.size()>selectedItemIndex && selectedItemIndex>=0)
        {
            if(loginEmails.get(selectedItemIndex).getActivated().equalsIgnoreCase("yes"))
            {
                Uitils.alert(LoginSettingActivity.this , "Oops! You cannot send a link to a confirmed email.");
            }
            else {
                final String email = loginEmails.get(selectedItemIndex).getEmail();
                UserRequest.sendValidationLinks(email,  new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if (response.isSuccess()) {
                            Uitils.alert(LoginSettingActivity.this, "Confirmation link is sent to "+email);
                            loadEmails();
                        }
                        else
                        {
                            Uitils.alert(LoginSettingActivity.this, "Failed to send confirmation link to "+email);
                        }
                    }
                });
            }
        }
        else
        {
            Uitils.alert("Oops! Please select a pending email to send a confirmation link.");
            return;
        }
    }
}
