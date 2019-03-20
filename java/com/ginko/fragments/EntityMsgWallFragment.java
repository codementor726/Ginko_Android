package com.ginko.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.ginko.activity.entity.EntityViewPostMessageAdapter;
import com.ginko.activity.entity.ViewEntityPostsActivity;
import com.ginko.activity.im.ImPreGetMessageCallbackListener;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.MyDataUtils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.ImMessageVO;

import org.apache.commons.lang.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class EntityMsgWallFragment extends Fragment {
    public String searchKeyword = "";
	private ListView list;
	private EntityViewPostMessageAdapter adapter;

    private ImPreGetMessageCallbackListener getRecentChatsCallbackListener;


    public EntityMsgWallFragment(){}

    public void setOnImPreGetMessageCallbackListener(ImPreGetMessageCallbackListener listener)
    {
        this.getRecentChatsCallbackListener = listener;
    }

    public BaseAdapter getListViewAdapter()
    {
        return this.adapter;
    }
    // private View view;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, Bundle savedInstanceState) {

		list = new ListView(this.getActivity());
        list.setDivider(null);

		adapter = new EntityViewPostMessageAdapter(this.getActivity());

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EntityMessageVO entityMessage = (EntityMessageVO) adapter.getItem(position);

                Intent viewEntityPost = new Intent(getActivity() , ViewEntityPostsActivity.class);
                viewEntityPost.putExtra("entityName" , entityMessage.strEntityName);
                viewEntityPost.putExtra("entityId" , entityMessage.entityId);
                viewEntityPost.putExtra("profileImage" , entityMessage.strProfilePhoto);
                viewEntityPost.putExtra("isfollowing_entity" , true);
                getActivity().startActivity(viewEntityPost);
            }
        });

		list.setAdapter(adapter);

		return list;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

    @Override
    public void onResume() {
        super.onResume();
        this.loadEntityMsg();
    }
    @Override
    public void onPause(){
        super.onPause();
        if (this.adapter != null) {
            this.adapter.stopAllRecords();
            this.adapter.unregisterDownloadManager();
        }
    }
    public void setStopAllRecords(){
        if (this.adapter != null) {
            this.adapter.stopAllRecords();
            this.adapter.unregisterDownloadManager();
        }
    }
	public void loadEntityMsg() {
		EntityRequest.listMessageWall(null, null,
				new ResponseCallBack<JSONObject>() {

					@Override
					public void onCompleted(JsonResponse<JSONObject> response) {
						if (response.isSuccess()) {
                            adapter.clear();

                            JSONArray boardsJArr = response.getData()
									.optJSONArray("data");
                            int size= boardsJArr.length();
                            for(int i=0;i<size;i++) {

                                try {
                                    JSONObject obj = (JSONObject) boardsJArr.get(i);
                                    EntityMessageVO entityMessage = new EntityMessageVO(obj.optInt("entity_id") , obj.optString("profile_image" , "") , obj.optString("entity_name" , ""));
                                    entityMessage.setContent(obj.getString("content"));
                                    Date sendDateTime = MyDataUtils.convertUTCTimeToLocalTime(obj.getString("sent_time"));
                                    //DateUtils.parseDate(String.valueOf(obj.getString("sent_time")), new String[]{"yyyy-MM-dd HH:mm:ss"});
                                    entityMessage.setSendTime(sendDateTime);
                                    entityMessage.setMsgId(obj.getInt("msg_id"));
                                    adapter.addMessageItem(entityMessage);
                                }catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            if (searchKeyword.compareTo("") != 0)
                                searchItems(searchKeyword);
                            if(getRecentChatsCallbackListener != null)
                            {
                                getRecentChatsCallbackListener.onGetRecentChats(adapter.getCount() , true);
                            }
                            adapter.notifyDataSetChanged();
						}
					}
				});

	}

    public void searchItems(String searchKeyword)
    {
        if(adapter != null)
        {
            if(searchKeyword.compareTo("") == 0)
                adapter.showAllItems();
            else
            {
                List<EntityMessageVO> entityMessages =  adapter.getAll();
                for(int i=0;i<entityMessages.size();i++)
                {
                    EntityMessageVO message = entityMessages.get(i);
                    int msgType = message.getMessageType();
                    String title = message.strEntityName;
                    String txtMsg = "";
                    switch(msgType)
                    {
                        case ImMessageVO.MSG_TYPE_TEXT:
                            txtMsg = message.getContent();
                            break;
                        case ImMessageVO.MSG_TYPE_PHOTO:
                            txtMsg = "photo";
                            break;
                        case ImMessageVO.MSG_TYPE_VOICE:
                            txtMsg = "voice";
                            break;
                        case ImMessageVO.MSG_TYPE_VIDEO:
                            txtMsg = "video";
                            break;
                        case ImMessageVO.MSG_TYPE_LOCATION:
                            txtMsg = "location";
                            break;
                    }

                    if(title.toLowerCase().contains(searchKeyword) || (msgType==ImMessageVO.MSG_TYPE_TEXT && txtMsg.toLowerCase().contains(searchKeyword)))
                    {

                        adapter.showItem(i, true);
                    }
                    else if(title.toLowerCase().contains(searchKeyword) || (msgType!=ImMessageVO.MSG_TYPE_TEXT && txtMsg.equalsIgnoreCase(searchKeyword)))
                    {
                        adapter.showItem(i , true);
                    }
                    else{
                        adapter.showItem(i , false);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    }
}
