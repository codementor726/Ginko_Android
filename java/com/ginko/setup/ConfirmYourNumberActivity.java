package com.ginko.setup;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ginko.api.request.UserRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

import org.json.JSONObject;

public class ConfirmYourNumberActivity extends MyBaseActivity implements View.OnClickListener{

    /* UI Variables */
    private ImageButton btnBack;
    private Button btnConfirm;
    private EditText edtCodeNumber1 , edtCodeNumber2 , edtCodeNumber3 , edtCodeNumber4 , edtCodeNumber5 , edtCodeNumber6;
    private Button btnResendCode;
    private boolean isAvailableSend = true;

    /* Variables */

    InputFilter filter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            if(dest.length()>0) return "";
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };


    private String strMobileNumber = "";


    private Handler mHandler = new Handler();

    private boolean fromMainContactScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confrim_your_number);

        if(savedInstanceState != null)
        {
            strMobileNumber = savedInstanceState.getString("mobileNubmer" , "");
        }
        else {
            Intent intent = this.getIntent();
            fromMainContactScreen = intent.getBooleanExtra("isFromMainContactScreen" , false);
            strMobileNumber = intent.getStringExtra("mobileNumber");
        }

        getUIObjects();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mobileNumber", strMobileNumber);
    }

    private void checkValidationCodeInputs()
    {
        if(edtCodeNumber1.getText().toString().equals("") ||
           edtCodeNumber2.getText().toString().equals("") ||
           edtCodeNumber3.getText().toString().equals("") ||
           edtCodeNumber4.getText().toString().equals("") ||
           edtCodeNumber5.getText().toString().equals("") ||
           edtCodeNumber6.getText().toString().equals(""))
        {
            btnConfirm.setVisibility(View.INVISIBLE);
        }
        else
        {
            btnConfirm.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnConfirm = (Button)findViewById(R.id.btnConfirm); btnConfirm.setVisibility(View.INVISIBLE); btnConfirm.setOnClickListener(this);
        btnBack = (ImageButton)findViewById(R.id.btnBack);  btnBack.setOnClickListener(this);

        btnResendCode = (Button)findViewById(R.id.btnResendCode); btnResendCode.setOnClickListener(this);

        edtCodeNumber1 = (EditText)findViewById(R.id.edtConfirmCode1); edtCodeNumber1.setFilters(new InputFilter[]{filter});    edtCodeNumber1.setText("");
        edtCodeNumber2 = (EditText)findViewById(R.id.edtConfirmCode2); edtCodeNumber2.setFilters(new InputFilter[]{filter}); edtCodeNumber2.setText("");
        edtCodeNumber3 = (EditText)findViewById(R.id.edtConfirmCode3); edtCodeNumber3.setFilters(new InputFilter[]{filter}); edtCodeNumber3.setText("");
        edtCodeNumber4 = (EditText)findViewById(R.id.edtConfirmCode4); edtCodeNumber4.setFilters(new InputFilter[]{filter}); edtCodeNumber4.setText("");
        edtCodeNumber5 = (EditText)findViewById(R.id.edtConfirmCode5); edtCodeNumber5.setFilters(new InputFilter[]{filter}); edtCodeNumber5.setText("");
        edtCodeNumber6 = (EditText)findViewById(R.id.edtConfirmCode6); edtCodeNumber6.setFilters(new InputFilter[]{filter}); edtCodeNumber6.setText("");

        edtCodeNumber1.requestFocus();

        edtCodeNumber1.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (edtCodeNumber1.getText().toString().length() == 1)     //size as per your requirement
                {
                    edtCodeNumber2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidationCodeInputs();
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                // TODO Auto-generated method stub
            }
        });

        edtCodeNumber1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // You can identify which key pressed buy checking keyCode value
                // with KeyEvent.KEYCODE_
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    // this is for backspace
                    edtCodeNumber1.setText("");
                } else if(edtCodeNumber1.getText().toString().length() == 1) {
                    edtCodeNumber2.requestFocus();
                }
                return false;
            }
        });

        edtCodeNumber2.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (edtCodeNumber2.getText().toString().length() == 1)     //size as per your requirement
                {
                    edtCodeNumber3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidationCodeInputs();
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                // TODO Auto-generated method stub
            }
        });
        edtCodeNumber2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // You can identify which key pressed buy checking keyCode value
                // with KeyEvent.KEYCODE_
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    // this is for backspace
                    if (edtCodeNumber2.getSelectionStart() <= 0) {
                        edtCodeNumber1.setText("");

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // do something
                                edtCodeNumber1.requestFocus();
                            }
                        }, 50);
                    }
                } else if(edtCodeNumber2.getText().toString().length() == 1)
                    edtCodeNumber3.requestFocus();
                return false;
            }
        });

        edtCodeNumber3.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (edtCodeNumber3.getText().toString().length() == 1) {
                    edtCodeNumber4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidationCodeInputs();
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                // TODO Auto-generated method stub
            }
        });
        edtCodeNumber3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // You can identify which key pressed buy checking keyCode value
                // with KeyEvent.KEYCODE_
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    // this is for backspace
                    if(edtCodeNumber3.getSelectionStart() <= 0) {
                        edtCodeNumber2.setText("");

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // do something
                                edtCodeNumber2.requestFocus();
                            }
                        }, 50);
                    }
                } else if (edtCodeNumber3.getText().toString().length() == 1)
                    edtCodeNumber4.requestFocus();
                return false;
            }
        });

        edtCodeNumber4.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (edtCodeNumber4.getText().toString().length() == 1)     //size as per your requirement
                {
                    edtCodeNumber5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidationCodeInputs();
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                // TODO Auto-generated method stub
            }
        });
        edtCodeNumber4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // You can identify which key pressed buy checking keyCode value
                // with KeyEvent.KEYCODE_
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    // this is for backspace
                    if(edtCodeNumber4.getSelectionStart() <= 0) {
                        edtCodeNumber3.setText("");

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // do something
                                edtCodeNumber3.requestFocus();
                            }
                        }, 50);
                    }
                } else if(edtCodeNumber4.getText().toString().length() == 1)
                    edtCodeNumber5.requestFocus();
                return false;
            }
        });

        edtCodeNumber5.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (edtCodeNumber5.getText().toString().length() == 1)     //size as per your requirement
                {
                    edtCodeNumber6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidationCodeInputs();
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                // TODO Auto-generated method stub
            }
        });
        edtCodeNumber5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // You can identify which key pressed buy checking keyCode value
                // with KeyEvent.KEYCODE_
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    // this is for backspace
                    if(edtCodeNumber5.getSelectionStart() <= 0) {
                        edtCodeNumber4.setText("");

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // do something
                                edtCodeNumber4.requestFocus();
                            }
                        }, 50);
                    }
                } else if(edtCodeNumber5.getText().toString().length() == 1)
                    edtCodeNumber6.requestFocus();
                return false;
            }
        });

        edtCodeNumber6.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (edtCodeNumber6.getText().toString().length() == 1)     //size as per your requirement
                {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //checkValidationCodeInputs();
                // this is for backspace
                if (s.length() == 0)
                    edtCodeNumber5.requestFocus();
                else {
                    if (strMobileNumber == null || strMobileNumber.equals("")) return;
                    //checkValidationCodeInputs();

                    confirmYourCode();
                }

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                // TODO Auto-generated method stub
            }
        });
        edtCodeNumber6.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // You can identify which key pressed buy checking keyCode value
                // with KeyEvent.KEYCODE_
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    // this is for backspace
                    edtCodeNumber5.setText("");

                    Handler handler =  new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // do something
                            edtCodeNumber5.requestFocus();
                        }
                    }, 50);
                }
                return false;
            }
        });

        edtCodeNumber1.requestFocus();

        checkValidationCodeInputs();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void confirmYourCode(){
        String verifyCode = edtCodeNumber1.getText().toString()+edtCodeNumber2.getText().toString()+edtCodeNumber3.getText().toString()+
                edtCodeNumber4.getText().toString()+edtCodeNumber5.getText().toString()+edtCodeNumber6.getText().toString();
        verifyCode = verifyCode.trim();

        UserRequest.verifyPhoneCode(strMobileNumber, verifyCode, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    if (RegisterConfirmationMobileActivity.getInstance() != null) {
                        RegisterConfirmationMobileActivity.getInstance().finish();
                    }

                    btnConfirm.setVisibility(View.INVISIBLE);
                    if(RuntimeContext.getUser() != null)
                        RuntimeContext.getUser().setPhoneVerified(true);
                    Intent intent = new Intent(ConfirmYourNumberActivity.this, InviteGinkoConnects.class);
                    intent.putExtra("isFromMainContactScreen" , fromMainContactScreen);
                    startActivity(intent);
                    finish();
                } else {
                    //Uitils.alert(ConfirmYourNumberActivity.this, "The verification code is invalid");
                    MyApp.getInstance().showSimpleAlertDiloag(ConfirmYourNumberActivity.this, "Oops, The verification code is invalid.", null);
                    edtCodeNumber1.setText("");
                    edtCodeNumber2.setText("");
                    edtCodeNumber3.setText("");
                    edtCodeNumber4.setText("");
                    edtCodeNumber5.setText("");
                    edtCodeNumber6.setText("");

                    btnConfirm.setVisibility(View.VISIBLE);
                    edtCodeNumber1.requestFocus();
                }
            }
        }, true);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnBack:
                finish();
                break;

            case R.id.btnConfirm:
                if(strMobileNumber == null || strMobileNumber.equals("")) return;
                checkValidationCodeInputs();

                confirmYourCode();

                break;

            case R.id.btnResendCode:
                if (isAvailableSend == false)
                    return;
                isAvailableSend = false;
                if(strMobileNumber == null || strMobileNumber.equals("")) return;
                UserRequest.getVerifyCodeBySMS(strMobileNumber, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        isAvailableSend = true;

                        if (response.isSuccess()) {
                            System.out.println("------Request send SMS -----" + response.toString() + " -------");
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    edtCodeNumber1.setText("");
                                    edtCodeNumber2.setText("");
                                    edtCodeNumber3.setText("");
                                    edtCodeNumber4.setText("");
                                    edtCodeNumber5.setText("");
                                    edtCodeNumber6.setText("");

                                    edtCodeNumber1.requestFocus();
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.showSoftInput(edtCodeNumber1, InputMethodManager.SHOW_IMPLICIT);

                                }
                            }, 500);
                            //Toast.makeText(ConfirmYourNumberActivity.this , "The verify code is sent to you successfully." , Toast.LENGTH_LONG).show();
                            MyApp.getInstance().showSimpleAlertDiloag(ConfirmYourNumberActivity.this, "Verification code sent.", null);

                            edtCodeNumber1.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(edtCodeNumber1, InputMethodManager.SHOW_IMPLICIT);
                        } else {
                            Uitils.alert(ConfirmYourNumberActivity.this, "Failed to send verify code by SMS... please try again..");

                            edtCodeNumber1.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(edtCodeNumber1, InputMethodManager.SHOW_IMPLICIT);

                        }
                    }
                }, true);
                break;
        }
    }
}
