package com.ginko.activity.menu;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.ginko.api.request.UserRequest;
import com.ginko.common.MyDataUtils;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

import org.json.JSONObject;

public class NotificationsActivity extends MyBaseActivity implements View.OnClickListener {

    private ImageButton btnPrev;

    private boolean isExchangeRequestOn = true;
    private boolean isChatMsgOn = true;
    private boolean isEntityNotification = true;
    private boolean isSproutOn = true;
    private boolean isProfileChangeOn = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);

        initialSwitchs();
    }

    private void initialSwitchs() {
        UserRequest.getNotifications(new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    Switch switch_exchange = (Switch) findViewById(R.id.switch_exchange);
                    Switch switch_chat = (Switch) findViewById(R.id.switch_chat);
                    Switch switch_entityMsg = (Switch) findViewById(R.id.switch_entity_msg);
                    Switch switch_sprout = (Switch) findViewById(R.id.switch_sprout);
                    Switch switch_profileUpdate = (Switch) findViewById(R.id.switch_profile_update);

                    isExchangeRequestOn = response.getData().optBoolean("exchange_request_notification", true);
                    isChatMsgOn = response.getData().optBoolean("chat_msg_notification", true);
                    isEntityNotification = response.getData().optBoolean("entity_notification", true);
                    isSproutOn = response.getData().optBoolean("sprout_notification", true);
                    isProfileChangeOn = response.getData().optBoolean("profile_change_notification", true);

                    switch_exchange.setChecked(isExchangeRequestOn);
                    switch_chat.setChecked(isChatMsgOn);
                    switch_entityMsg.setChecked(isEntityNotification);
                    switch_sprout.setChecked(isSproutOn);
                    switch_profileUpdate.setChecked(isProfileChangeOn);
                }
            }
        });
    }

    private void save()
    {
        boolean exchgReq = ((Switch) findViewById(R.id.switch_exchange)).isChecked();
        boolean chatMsg = ((Switch) findViewById(R.id.switch_chat)).isChecked();
        boolean entityMsg = ((Switch) findViewById(R.id.switch_entity_msg)).isChecked();
        boolean sprout = ((Switch) findViewById(R.id.switch_sprout)).isChecked();
        boolean bProfileUpdates = ((Switch)findViewById(R.id.switch_profile_update)).isChecked();

        //check the updates
        if(isExchangeRequestOn != exchgReq ||
                isChatMsgOn != chatMsg ||
               isEntityNotification != entityMsg ||
                isSproutOn != sprout ||
                isProfileChangeOn != bProfileUpdates)
        {
            UserRequest.setNotifications(exchgReq, chatMsg, entityMsg, sprout, bProfileUpdates, new ResponseCallBack<Void>() {
                @Override
                public void onCompleted(JsonResponse<Void> response) {
                    if (response.isSuccess()) {
                        NotificationsActivity.this.finish();

                        /*Uitils.alert("You successfully set the push notifications.", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick (DialogInterface dialog,int which){
                        }
                      });*/
                    }
                }
            });
        }
        else
        {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnPrev:
                save();

                break;

        }
    }
}
