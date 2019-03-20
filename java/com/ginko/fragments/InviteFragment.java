package com.ginko.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.ginko.activity.exchange.ExchangeItem;
import com.ginko.activity.exchange.ExchangeRequestListAdapter;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.api.request.CBRequest;
import com.ginko.common.MyDataUtils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class InviteFragment extends BaseExchangeFragment {

    public InviteFragment(){
        super(ExchangeItem.EXCHANGE_INVITE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(getIsSelectable())
                {
                    adapter.triggerSelection(position);
                }
                else {
                    ExchangeItem item = (ExchangeItem) adapter.getItem(position);
                    if(item.id == 0) return;
                    Intent contactSharingSettingIntent = new Intent(getActivity(), ShareYourLeafActivity.class);
                    contactSharingSettingIntent.putExtra("contactID", String.valueOf(item.contactId));
                    String contactName = item.contactName.equals("")?item.email:item.contactName;
                    contactSharingSettingIntent.putExtra("contactFullname", contactName);
                    contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                    contactSharingSettingIntent.putExtra("email" , item.email);
                    startActivity(contactSharingSettingIntent);
                }
            }
        });
        loadData();
    }
    @Override
    public void loadData() {
        if (adapter == null)
            return;
        CBRequest.getInvitations(null, null, null, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if(response.isSuccess())
                {
                    adapter.clearAll();
                    JSONObject resultObj = response.getData();
                    try {
                        JSONArray resultArray = resultObj.getJSONArray("results");
                        for(int i=0;i<resultArray.length();i++)
                        {
                            JSONObject obj = (JSONObject) resultArray.get(i);
                            ExchangeItem item = new ExchangeItem(ExchangeItem.EXCHANGE_INVITE);
                            item.id = obj.optInt("id");
                            String contactName = obj.optString("first_name" , "")+" "+obj.optString("last_name" , "");
                            item.contactName = contactName.trim();
                            item.email = obj.optString("email", "");
                            item.photoUrl = obj.optString("photo_url" , "");
                            item.setTime(MyDataUtils.convertUTCTimeToLocalTime(obj.optString("created")));
                            //item.setTime(obj.optString("created"));
                            adapter.addItem(item);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    updateBadgeNum();
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void deleteItems()
    {
        super.deleteItems();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm");
        builder.setMessage(getResources().getString(R.string.str_confirm_dialog_delete_invited_emails));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
                List<ExchangeItem> selectedItems = adapter.getSelectedItems();
                StringBuffer emails=  new StringBuffer();
                for (ExchangeItem item : selectedItems) {
                    emails.append(item.email).append(",");
                }
                if (emails.length()>0){
                    emails.deleteCharAt(emails.length() -1 );
                    CBRequest.deleteInvitation(emails.toString(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if(response.isSuccess())
                            {
                                adapter.deleteSelectedItems();
                                updateBadgeNum();
                                adapter.notifyDataSetChanged();
                            }
                            else
                            {

                            }
                        }
                    });
                }

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
    }

	/*@Override
	protected void deleteItems() {

    }*/
	


}
