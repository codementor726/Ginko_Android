package com.ginko.activity.menu;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.exchange.ExchangeRequestActivity;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

import org.json.JSONObject;

public class ScanMeActivity extends MyBaseActivity implements View.OnClickListener{

    /* UI variables */
    private ImageButton btnPrev , btnExchangeRequests , btnCamera;
    private TextView txtExchangeRequestBadge;
    private CustomNetworkImageView imgViewMyQrcode;

    /* variables*/
    private ImageLoader imgLoader;
    private int newExchangedRequestCount = 0;
    private boolean isUICreated = false;

    private ContactChangeReceiver contactChangeReceiver; private boolean isContactChangeReceiverRegistered = false;
    private ExchangeRequestReceiver exchangeRequestReceiver; private boolean isExchangeRequestReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanme);

        imgLoader = MyApp.getInstance().getImageLoader();

        getUIObjects();

        this.contactChangeReceiver = new ContactChangeReceiver();
        this.exchangeRequestReceiver = new ExchangeRequestReceiver();

        if (this.contactChangeReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactChangeReceiver, msgReceiverIntent);
            isContactChangeReceiverRegistered = true;
        }

        if(this.exchangeRequestReceiver != null)
        {
            IntentFilter exchangeRequestReceiver = new IntentFilter();
            exchangeRequestReceiver.addAction("android.intent.action.EXCHANGE_REQUEST");
            registerReceiver(this.exchangeRequestReceiver, exchangeRequestReceiver);
            isExchangeRequestReceiverRegistered = true;
        }
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnExchangeRequests = (ImageButton)findViewById(R.id.btnExchangeRequests); btnExchangeRequests.setOnClickListener(this);
        btnCamera = (ImageButton)findViewById(R.id.btnCamera); btnCamera.setOnClickListener(this);

        txtExchangeRequestBadge = (TextView)findViewById(R.id.txtExchangeRequestBadge);
        updateBadgeNum();

        imgViewMyQrcode = (CustomNetworkImageView)findViewById(R.id.imgMyQrCode);
        String qrCodeUrl = RuntimeContext.getUser().getQrCodeUrl();
        imgViewMyQrcode.setImageUrl(qrCodeUrl, imgLoader);
        System.out.println("----QRCode Url = "+qrCodeUrl+" ------");
    }

    private void updateBadgeNum()
    {
        if(newExchangedRequestCount > 0)
        {
            txtExchangeRequestBadge.setText(String.valueOf(newExchangedRequestCount));
            txtExchangeRequestBadge.setVisibility(View.VISIBLE);
        }
        else {
            txtExchangeRequestBadge.setVisibility(View.INVISIBLE);//hide as default
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isUICreated = true;

        getContactSummaries();
        updateBadgeNum();

        if(contactChangeReceiver == null)
            this.contactChangeReceiver = new ContactChangeReceiver();
        if(exchangeRequestReceiver == null)
            this.exchangeRequestReceiver = new ExchangeRequestReceiver();

        if (this.contactChangeReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactChangeReceiver, msgReceiverIntent);
            isContactChangeReceiverRegistered = true;
        }

        if(this.exchangeRequestReceiver != null)
        {
            IntentFilter exchangeRequestReceiver = new IntentFilter();
            exchangeRequestReceiver.addAction("android.intent.action.EXCHANGE_REQUEST");
            registerReceiver(this.exchangeRequestReceiver, exchangeRequestReceiver);
            isExchangeRequestReceiverRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        isUICreated = false;

        if (this.contactChangeReceiver != null && isContactChangeReceiverRegistered == true) {
            unregisterReceiver(this.contactChangeReceiver);
            isContactChangeReceiverRegistered = false;
        }

        if(this.exchangeRequestReceiver != null && isExchangeRequestReceiverRegistered == true)
        {
            unregisterReceiver(exchangeRequestReceiver);
            isExchangeRequestReceiverRegistered = false;
        }

        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;

            //go to exchange reuqest screen
            case R.id.btnExchangeRequests:
                Intent requestActivity = new Intent(ScanMeActivity.this , ExchangeRequestActivity.class);
                requestActivity.putExtra("first_pageindex" , 2);
                startActivity(requestActivity);
                //Uitils.toActivity(this, ExchangeRequestActivity.class, false);
                break;

            //go to camera preview screen to scan QR code
            case R.id.btnCamera:
                Intent intent = new Intent(ScanMeActivity.this , ScanQRCodeActivity.class);
                startActivity(intent);
                break;


        }
    }

    private void getContactSummaries()
    {
        //call User/contact/summary to get new events
        UserRequest.getContactSummary(new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    //sample response
                    //{"xcg_req_num":0,"not_xcg_sprout_num":0,"new_chat_msg_num":1,"contact_counts":{"work":2,"home":3,"entity":1},"all_cb_valid":true}

                    JSONObject jsonObject = response.getData();
                    //newSproutCount = jsonObject.optInt("not_xcg_sprout_num", 0);
                    //newMessageCount = jsonObject.optInt("new_chat_msg_num", 0);
                    newExchangedRequestCount = jsonObject.optInt("xcg_req_num", 0);

                    if(isUICreated)
                    {
                        updateBadgeNum();
                    }
                } else {
                    Uitils.alert("Failed to get data from server.");
                }
            }
        });
    }

    public class ContactChangeReceiver extends BroadcastReceiver {
        public ContactChangeReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New Contact Change");

            getContactSummaries();

        }
    }
    public class ExchangeRequestReceiver extends BroadcastReceiver {
        public ExchangeRequestReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New exchange request");

            getContactSummaries();

        }
    }
}
