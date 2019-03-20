package com.ginko.setup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.api.request.TradeCard;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.DropdownSpinner;
import com.ginko.customview.PopupListItemAdapter;
import com.ginko.customview.PrefixedEditText;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegisterConfirmationMobileActivity extends MyBaseActivity implements View.OnClickListener{

    /* UI variables*/

    private Button btnSkip , btnNext;
    private PrefixedEditText edtMobileNumber;
    private TextView spinnerCountrySelector;
    /* Variables*/

    private String[] strCountryCodeArrays;
    private ArrayList<String> arrayCountryCodes = new ArrayList<String>();
    private ArrayList<String> arrayCountryNames = new ArrayList<String>();
    //private CountryNameSpinnerAdapter countryNameSpinnerAdapter;

    private String strPadding = "     ";

    private int currentCountryIndex = 0;

    private static RegisterConfirmationMobileActivity mInstance;

    private final int GET_COUNTRY_CODE = 1;

    /* Variables */
    private boolean fromMainContactScreen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register_mobile_number);
        mInstance = this;

        Intent intent = this.getIntent();
        fromMainContactScreen = intent.getBooleanExtra("isFromMainContactScreen" , false);

        strCountryCodeArrays = getResources().getStringArray(R.array.countrycodes);

        for(int i=0;i<strCountryCodeArrays.length;i++)
        {
            try {
                String strCountryCode = strCountryCodeArrays[i];
                arrayCountryNames.add(strCountryCode.substring(0, strCountryCode.indexOf("+") - 1));
                arrayCountryCodes.add(strCountryCode.substring(strCountryCode.indexOf("+")  , strCountryCode.length()));
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        currentCountryIndex = 221;//default united states

        getUIObjects();
    }

    public static RegisterConfirmationMobileActivity getInstance()
    {
        return RegisterConfirmationMobileActivity.mInstance;
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnNext = (Button)findViewById(R.id.btnNext); btnNext.setOnClickListener(this);
        btnSkip = (Button)findViewById(R.id.btnSkip); btnSkip.setOnClickListener(this);

        edtMobileNumber = (PrefixedEditText)findViewById(R.id.edtMobileNumber);
        edtMobileNumber.setPrefix(arrayCountryCodes.get(currentCountryIndex) + strPadding);
        edtMobileNumber.requestFocus();

        edtMobileNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edtMobileNumber.setCursorVisible(true);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(edtMobileNumber, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    edtMobileNumber.setCursorVisible(false);
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtMobileNumber.getWindowToken(), 0);
                }
            }
        });

        edtMobileNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String strMobileNumber = arrayCountryCodes.get(currentCountryIndex) + edtMobileNumber.getText().toString().trim();

                    if (edtMobileNumber.getText().toString().trim().length() == 0){
                        AlertDialog alertDialog = new AlertDialog.Builder(RegisterConfirmationMobileActivity.this).create();
                        alertDialog.setTitle("Oops!");
                        alertDialog.setCancelable(false);
                        alertDialog.setMessage("Please input phone number.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        return false;
                    }
                    if(strMobileNumber.length() < 6) {
                        Toast.makeText(RegisterConfirmationMobileActivity.this, "Invalid mobile number.", Toast.LENGTH_LONG).show();
                        return false;
                    }

                    final String mobileNumber = strMobileNumber;

                    UserRequest.getVerifyCodeBySMS(strMobileNumber, new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            if (response.isSuccess()) {
                                System.out.println("------Request send SMS -----" + response.toString() + " -------");

                                Intent confirmCodeIntent = new Intent(RegisterConfirmationMobileActivity.this, ConfirmYourNumberActivity.class);
                                confirmCodeIntent.putExtra("isFromMainContactScreen" , fromMainContactScreen);
                                confirmCodeIntent.putExtra("mobileNumber", mobileNumber);
                                startActivity(confirmCodeIntent);

                            } else {
                                //Uitils.alert(RegisterConfirmationMobileActivity.this, "Failed to request verify code by SMS... please try again..");
                                Uitils.alert(RegisterConfirmationMobileActivity.this, "No Internet Connection.");
                            }
                        }
                    }, true);

                    return true;
                }
                return false;
            }
        });

        LinearLayout mobileLayout = (LinearLayout)findViewById(R.id.mobilenumberLayout);
        mobileLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (edtMobileNumber.isFocused()) {
                        Rect outRect = new Rect();
                        edtMobileNumber.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            edtMobileNumber.clearFocus();
                            //InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            //imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    }
                }
                return false;
            }
        });

        //countryNameSpinnerAdapter = new CountryNameSpinnerAdapter(this , arrayCountryNames);
        spinnerCountrySelector = (TextView)findViewById(R.id.spinnerCountrySelector);
        spinnerCountrySelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterConfirmationMobileActivity.this , SelectCountryCodeActivity.class);
                intent.putExtra("current_country_index" , currentCountryIndex);
                startActivityForResult(intent , GET_COUNTRY_CODE);
            }
        });

        /*spinnerCountrySelector.setListAdapter(countryNameSpinnerAdapter);
        spinnerCountrySelector.setVisibleItemNo(8);
        spinnerCountrySelector.setSelectedPosition(currentCountryIndex);*/


        /*spinnerCountrySelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String countryName = ((CountryNameSpinnerAdapter) parent.getAdapter()).getItem(position);
                currentCountryIndex = position;
                if (view != null) {
                    spinnerCountrySelector.setText(countryName);
                    edtMobileNumber.setPrefix(arrayCountryCodes.get(currentCountryIndex) + strPadding);
                    edtMobileNumber.invalidate();
                }
            }
        });*/

        //select the first font as default
        spinnerCountrySelector.setText(arrayCountryNames.get(currentCountryIndex));

        updateCountryCode();
    }

    private void updateCountryCode()
    {
        String countryName = arrayCountryNames.get(currentCountryIndex);
        spinnerCountrySelector.setText(countryName);
        edtMobileNumber.setPrefix(arrayCountryCodes.get(currentCountryIndex) + strPadding);
        edtMobileNumber.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        edtMobileNumber.requestFocus();
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_COUNTRY_CODE && resultCode == RESULT_OK && data!=null)
        {
            this.currentCountryIndex = data.getIntExtra("countryCodeIndex" , 221);
            updateCountryCode();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mInstance = null;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnSkip:
                InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtMobileNumber.getWindowToken(), 0);

                Intent intent = new Intent();
                intent.setClass(RegisterConfirmationMobileActivity.this, ContactMainActivity.class);
                RegisterConfirmationMobileActivity.this.startActivity(intent);
                RegisterConfirmationMobileActivity.this.finish();

                break;

            case R.id.btnNext:
                String strMobileNumber = arrayCountryCodes.get(currentCountryIndex) + edtMobileNumber.getText().toString().trim();

                if (edtMobileNumber.getText().toString().trim().length() == 0){
                    AlertDialog alertDialog = new AlertDialog.Builder(RegisterConfirmationMobileActivity.this).create();
                    alertDialog.setTitle("Oops!");
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage("Please input phone number.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }
                if(strMobileNumber.length() < 6) {
                    Toast.makeText(this, "Invalid mobile number.", Toast.LENGTH_LONG).show();
                    return;
                }

                final String mobileNumber = strMobileNumber;

                UserRequest.getVerifyCodeBySMS(strMobileNumber, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            System.out.println("------Request send SMS -----" + response.toString() + " -------");

                            Intent confirmCodeIntent = new Intent(RegisterConfirmationMobileActivity.this, ConfirmYourNumberActivity.class);
                            confirmCodeIntent.putExtra("isFromMainContactScreen" , fromMainContactScreen);
                            confirmCodeIntent.putExtra("mobileNumber", mobileNumber);
                            startActivity(confirmCodeIntent);

                        } else {
                            //Uitils.alert(RegisterConfirmationMobileActivity.this, "Failed to request verify code by SMS... please try again..");
                            Uitils.alert(RegisterConfirmationMobileActivity.this, "No Internet Connection.");
                        }
                    }
                }, true);

                break;

        }
    }

    class CountryNameSpinnerAdapter extends PopupListItemAdapter<String> {

        private Context mContext;
        public CountryNameSpinnerAdapter(Context context, List<String> objects) {
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
            return row;
        }
    }
}
