package com.ginko.activity.entity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.activity.im.PullToRefreshListView;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.Logger;
import com.ginko.customview.ActionSheet;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.UserEntityProfileVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewEntityPostsActivity extends MyBaseFragmentActivity implements View.OnClickListener,
                                                PullToRefreshListView.OnSinglePointTouchListener ,
                                                                ActionSheet.ActionSheetListener
{

    //UI Objects
    private ImageView btnClose;
    private TextView txtEntityName;
    //private ListView entityMessageListView;
    private PullToRefreshListView pullToRefreshView;
    private TextView txtNoPostsToView;
    private LinearLayout loadingLayout;

    //Variables
    private String strEntityName = "";
    private String strProfileImage = "";
    private int    entityId = 0;
    private long    msgId = 0;

    private boolean hasMoreMessage = false;
    private int     nPageNum = 0;
    private final int CountPerPage = 100;
    private int totalMessageNum = 0;
    private boolean flag_loading = false;

    private boolean isFollowingEntity = false;

    private EntityViewPostMessageAdapter adapter;

    private boolean isEntityReceiverRegistered = false;

    //---------------------------------//
    /* new message listener */
    private EntityMsgReceiver entityMsgReceiver;

    private boolean isFirstLoadMessages = true;

    //sharing messages
    private ActionSheet shareViaEmailActionSheet = null;
    public static EntityMessageVO sharingMessage = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_entity_posts);

        Intent intent = this.getIntent();
        this.strEntityName = intent.getStringExtra("entityName");
        this.entityId = intent.getIntExtra("entityId", -1);
        this.msgId = intent.getLongExtra("msgId", -1);
        this.strProfileImage = intent.getStringExtra("profileImage");
        if (this.strProfileImage == null || this.strProfileImage.trim().equalsIgnoreCase(""))
            getProfileImage(entityId);

        this.isFollowingEntity = intent.getBooleanExtra("isfollowing_entity" , false);

        if(this.entityId == -1)
            return;

        getUIObjects();

        //getMessages(nPageNum , CountPerPage , true);

        if (msgId == -1)
            entityMsgReceiver = new EntityMsgReceiver();

        if(this.entityMsgReceiver != null && msgId == -1)
        {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.ENTITY_NEW_MSG");
            registerReceiver(this.entityMsgReceiver, msgReceiverIntent);
            isEntityReceiverRegistered = true;
        }

        isFirstLoadMessages = true;
    }

    private void getProfileImage(int entityId)
    {
        EntityRequest.viewEntity(entityId, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    UserEntityProfileVO entity;

                    JSONObject jsonObj = response.getData();
                    JSONObject jData = null;
                    try {

                        entity = JsonConverter.json2Object(
                                (JSONObject) jsonObj, (Class<UserEntityProfileVO>) UserEntityProfileVO.class);
                        strProfileImage = entity.getProfileImage();
                    } catch (JsonConvertException e) {
                        e.printStackTrace();
                        entity = null;
                    }
                }
            }
        }, true);
    }

    private void getMessages(final Integer pageNum ,  Integer countPerPage , boolean bProgressDialog)
    {
        if(adapter == null || pullToRefreshView.getAdapter() == null) {
            pullToRefreshView.onRefreshComplete();
            return;
        }

        if (hasMoreMessage) {
            pullToRefreshView.onRefreshComplete();
            return;
        }

        if(entityId <=0) {
            pullToRefreshView.onRefreshComplete();
            return;
        }

        EntityRequest.listMessages(entityId, pageNum, countPerPage, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    JSONObject rootJson = response.getData();
                    try {
                        int total = rootJson.optInt("total", 0);

                        JSONArray dataArrayObj = rootJson.getJSONArray("data");
                        int messageCount = dataArrayObj.length();
                        if (messageCount < CountPerPage)
                            hasMoreMessage = false;
                        else
                            hasMoreMessage = true;
                        if (messageCount > 0) {

                            List<EntityMessageVO> messages = new ArrayList<EntityMessageVO>();
                            for (int i = 0; i < dataArrayObj.length(); i++) {
                                if (msgId != -1) {
                                    String msg_id = dataArrayObj.getJSONObject(i).get("msg_id").toString();
                                    if (Long.parseLong(msg_id) != msgId)
                                        continue;
                                    else {
                                        try {
                                            EntityMessageVO msg = null;
                                            msg = JsonConverter.json2Object(
                                                    dataArrayObj.getJSONObject(i), (Class<EntityMessageVO>) EntityMessageVO.class);
                                            msg.strProfilePhoto = strProfileImage;
                                            msg.strEntityName = strEntityName;
                                            if (msg != null) {
                                                messages.add(msg);
                                            }
                                        } catch (JsonConvertException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    try {
                                        EntityMessageVO msg = null;
                                        msg = JsonConverter.json2Object(
                                                dataArrayObj.getJSONObject(i), (Class<EntityMessageVO>) EntityMessageVO.class);
                                        msg.strProfilePhoto = strProfileImage;
                                        msg.strEntityName = strEntityName;
                                        if (msg != null) {
                                            messages.add(msg);
                                        }
                                    } catch (JsonConvertException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            if (adapter != null) {
                                adapter.clear();
                                adapter.addMessageItems(messages);
                                pullToRefreshView.onRefreshComplete();
                                //pullToRefreshView.setSelection(0);
                                //pullToRefreshView.smoothScrollToPosition(0);
                            }
                            /*
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pullToRefreshView.onRefreshComplete();
                                }
                            }, 100);
                            */
                        } else {
                            if (pageNum == null)
                                adapter.clear();
                            adapter.notifyDataSetChanged();
                            pullToRefreshView.onRefreshComplete();
                        }

                        totalMessageNum += messageCount;
                        if (totalMessageNum > 0) {
                            txtNoPostsToView.setVisibility(View.GONE);
                        } else {
                            txtNoPostsToView.setVisibility(View.VISIBLE);
                        }
                        nPageNum++;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                /*
                if(loadingLayout.getVisibility() == View.VISIBLE)
                {
                    loadingLayout.setVisibility(View.GONE);
                }
                */
                if (flag_loading)
                    flag_loading = false;
            }
        }

                , bProgressDialog);
    }


    //getUiObjects
    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnClose = (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        txtEntityName = (TextView)findViewById(R.id.txtEntityName); txtEntityName.setText(strEntityName);
        //entityMessageListView = (ListView)findViewById(R.id.entityMessageListView);
        pullToRefreshView = (PullToRefreshListView)findViewById(R.id.PullToRefreshView);

        txtNoPostsToView = (TextView)findViewById(R.id.txtNoPostsToView);
        txtNoPostsToView.setVisibility(View.GONE);

        //loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);
        //loadingLayout.setVisibility(View.GONE);
        if (msgId != -1) {
            pullToRefreshView.setRefreshEnable(false);
        }

        pullToRefreshView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO Auto-generated method stubif(hasMoreMessage == false)
                if(flag_loading == false)
                {
                    flag_loading = true;
                    //loadingLayout.setVisibility(View.VISIBLE);
                    getMessages(nPageNum , CountPerPage , false);
                }
            }
        });

        if (msgId != -1)
            adapter = new EntityViewPostMessageAdapter(ViewEntityPostsActivity.this , entityId , isFollowingEntity, false);
        else
            adapter = new EntityViewPostMessageAdapter(ViewEntityPostsActivity.this , entityId , isFollowingEntity, true);
        //entityMessageListView.setAdapter(adapter);
        pullToRefreshView.setAdapter(adapter);
        pullToRefreshView.setOnSinglePointTouchListener(this);

        if(flag_loading == false)
        {
            flag_loading = true;
            getMessages(nPageNum , CountPerPage , false);
        }


        /*
        entityMessageListView.setOnScrollListener(new  android.widget.AbsListView.OnScrollListener(){

             @Override
             public void onScroll(AbsListView view, int firstVisibleItem,
                                  int visibleItemCount, int totalItemCount) {
                 // TODO Auto-generated method stub
                 if(firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount!=0)
                 {
                     System.out.println("----onScroll----");
                     if(hasMoreMessage == false)
                         return;
                     if(flag_loading == false)
                     {
                         flag_loading = true;
                         loadingLayout.setVisibility(View.VISIBLE);
                         getMessages(nPageNum , CountPerPage , false);
                     }
                 }
             }

             @Override
             public void onScrollStateChanged(AbsListView view, int scrollState) {
                 // TODO Auto-generated method stub

             }}
        );
        */
    }



    public void shareMessageViaEmail(EntityMessageVO msg)
    {
        sharingMessage = msg;
        setTheme(R.style.ActionSheetStyleIOS7);

        if(shareViaEmailActionSheet == null)
            shareViaEmailActionSheet = ActionSheet.createBuilder(ViewEntityPostsActivity.this, getSupportFragmentManager())
                    .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                    .setOtherButtonTitles(
                            getResources().getString(R.string.str_share_via_email))
                    .setCancelableOnTouchOutside(true)
                    .setListener(this)
                    .show();
        else
            shareViaEmailActionSheet.show(getSupportFragmentManager() , "actionSheet");
    }
    @Override
    protected void onResume() {
        if(entityMsgReceiver == null && msgId == -1)
            entityMsgReceiver = new EntityMsgReceiver();
        if (this.entityMsgReceiver != null && isEntityReceiverRegistered == false && msgId == -1) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.ENTITY_NEW_MSG");
            registerReceiver(this.entityMsgReceiver, msgReceiverIntent);
            isEntityReceiverRegistered = true;
        }
        super.onResume();
        if(adapter!=null)
            adapter.registerDownloadManager();

        if(isFirstLoadMessages)
        {
            isFirstLoadMessages = false;
            if (flag_loading == false) {
                flag_loading = true;
                getMessages(null, null, true);
            }
        }
        else {
            if (flag_loading == false) {
                flag_loading = true;
                getMessages(null, null, true);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.entityMsgReceiver != null && isEntityReceiverRegistered == true && msgId == -1) {
            unregisterReceiver(this.entityMsgReceiver);
            isEntityReceiverRegistered = false;
        }
        if (this.adapter != null) {
            this.adapter.stopAllRecords();
            this.adapter.unregisterDownloadManager();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnClose:
                if (this.adapter != null) {
                    this.adapter.stopAllRecords();
                    this.adapter.unregisterDownloadManager();
                }
                finish();
                break;
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0) //share photo or video file via email
        {
            if(sharingMessage != null)
            {
                if(sharingMessage.getFile() != null && sharingMessage.getFile().equals(""))
                {
                    Toast.makeText(ViewEntityPostsActivity.this, "File Downloading... Please wait for a while", Toast.LENGTH_LONG).show();
                    return;
                }
                if(sharingMessage.getFile() != null)
                {
                    File file = new File(sharingMessage.getFile());
                    if(file.exists())
                    {
                        try {

                            final Intent emailIntent = new Intent(
                                    android.content.Intent.ACTION_SEND);

                            emailIntent.setType("plain/text");
                            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                                    new String[] { "" });//email
                            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                                    "");//subject

                            Uri attachFileUri = Uri.fromFile(file);

                            if (attachFileUri != null) {
                                emailIntent.putExtra(Intent.EXTRA_STREAM, attachFileUri);
                            }

                            emailIntent
                                    .putExtra(android.content.Intent.EXTRA_TEXT, "Sent from Ginko Android...");
                            this.startActivity(Intent.createChooser(emailIntent,
                                    "Send email via..."));
                        } catch (Throwable t) {
                            Toast.makeText(this,
                                    "Request failed try again: " + t.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(ViewEntityPostsActivity.this ,  "File Downloading... Please wait for a while" , Toast.LENGTH_LONG).show();
                        return;
                    }

                }
                else
                {
                    Toast.makeText(ViewEntityPostsActivity.this ,  "File Downloading... Please wait for a while" , Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    @Override
    public void onSinglePointTouchEvent() {

    }

    public class EntityMsgReceiver extends BroadcastReceiver {
        public EntityMsgReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            int ent_id = 0;
            try {
                ent_id = Integer.valueOf(intent.getStringExtra("entity_id"));
            }catch (Exception e)
            {
                ent_id = 0;
            }
            if(ent_id > 0 && ent_id == entityId)
            {
                System.out.println("---Refreshing Entity Message Board----");
                getMessages(null , null , false);
            }

        }
    }
}
