package com.ginko.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ginko.activity.exchange.ExchangeItem;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.api.request.CBRequest;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.common.MyDataUtils;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.BaseExchangeFragment;
import com.ginko.ginko.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class PendingRequestFragment extends BaseExchangeFragment {

    private int nLoadCnt = 0;
    private int nRequestCnt = 0;

    public PendingRequestFragment(){
        super(ExchangeItem.EXCHANGE_PENDING);
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
                    ImageView btnDelete = (ImageView) getActivity().findViewById(R.id.btnDelete);
                    if (getSelectedItemsCount() > 0)
                        btnDelete.setImageResource(R.drawable.img_trash_white_border);
                    else
                        btnDelete.setImageResource(R.drawable.img_trash_disable);
                }
                else {
                    ExchangeItem item = (ExchangeItem) adapter.getItem(position);
                    if(item.id == 0) return;
                    if (item.contactType != 2)
                    {
                        Intent contactSharingSettingIntent = new Intent(getActivity(), ShareYourLeafActivity.class);
                        contactSharingSettingIntent.putExtra("contactID", "0");
                        String contactName = item.contactName.equals("")?item.email:item.contactName;
                        contactSharingSettingIntent.putExtra("contactFullname", contactName);
                        contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                        contactSharingSettingIntent.putExtra("isPendingRequest" , true);
                        contactSharingSettingIntent.putExtra("email" , item.email);
                        contactSharingSettingIntent.putExtra("shared_home_fids" , item.shared_home_fids);
                        contactSharingSettingIntent.putExtra("shared_work_fids" , item.shared_work_fids);
                        contactSharingSettingIntent.putExtra("sharing_status" , item.sharingStatus);
                        contactSharingSettingIntent.putExtra("isInviteContact" , true);

                        startActivity(contactSharingSettingIntent);
                    } else
                    {
                        Intent contactSharingSettingIntent = new Intent(getActivity() , ShareYourLeafActivity.class);
                        contactSharingSettingIntent.putExtra("contactID" , "0");
                        String contactName = item.contactName.equals("") ? item.email : item.contactName;
                        contactSharingSettingIntent.putExtra("contactFullname" , contactName);
                        contactSharingSettingIntent.putExtra("isDirectory" , true);
                        contactSharingSettingIntent.putExtra("directoryID", item.id);
                        contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                        contactSharingSettingIntent.putExtra("isInviteContact", true);
                        contactSharingSettingIntent.putExtra("isPendingRequest" , true);
                        contactSharingSettingIntent.putExtra("StartActivity", "ContactMain");

                        startActivity(contactSharingSettingIntent);
                    }
                }
            }
        });
        loadData();
    }

	@Override
	public void loadData(){
        if (adapter == null)
            return;
        adapter.clearAll();

        nLoadCnt = 0;
		CBRequest.getPendingExhchangeRequest(null, null, null, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                nLoadCnt++;
                if(response.isSuccess())
                {
                    JSONObject resultObj = response.getData();
                    try {
                        JSONArray resultArray = resultObj.getJSONArray("results");
                        for(int i=0;i<resultArray.length();i++)
                        {
                            JSONObject obj = (JSONObject) resultArray.get(i);
                            ExchangeItem item = new ExchangeItem(ExchangeItem.EXCHANGE_PENDING);
                            item.id = obj.optInt("id");
                            String contactName = obj.optString("first_name" , "")+" "+obj.optString("last_name" , "");
                            item.contactName = contactName.trim();
                            item.email = obj.optString("email", "");
                            item.photoUrl = obj.optString("photo_url", "");
                            item.setTime(MyDataUtils.convertUTCTimeToLocalTime(obj.optString("sent_time")));
                            //item.setTime(obj.optString("sent_time"));
                            item.shared_home_fids = obj.optString("shared_home_fids" , "");
                            item.shared_work_fids = obj.optString("shared_work_fids" , "");
                            item.sharingStatus = obj.optInt("sharing_status" , ConstValues.SHARE_NONE);
                            adapter.addItem(item);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    if (nLoadCnt == 2)
                    {
                        nLoadCnt = 0;
                        updateBadgeNum();
                        updateNotesData(false);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        DirectoryRequest.getRequestSent(null, null, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                nLoadCnt++;
                if (response.isSuccess()) {
                    JSONObject resultObj = response.getData();
                    try {
                        JSONArray resultArray = resultObj.getJSONArray("data");
                        for (int i = 0; i < resultArray.length(); i++) {
                            JSONObject obj = (JSONObject) resultArray.get(i);
                            ExchangeItem item = new ExchangeItem(ExchangeItem.EXCHANGE_PENDING);
                            item.id = obj.optInt("id");
                            String contactName = obj.optString("name", "");
                            item.contactName = contactName.trim();
                            item.photoUrl = obj.optString("profile_image", "");
                            //item.setTime(MyDataUtils.convertUTCTimeToLocalTime(obj.optString("sent_time")));
                            //item.setTime(obj.optString("sent_time"));
                            item.contactType = 2;
                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                            item.setTime(MyDataUtils.convertUTCTimeToLocalTime(currentDateTimeString));

                            adapter.addItem(item);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (nLoadCnt == 2) {
                        nLoadCnt = 0;
                        updateBadgeNum();
                        updateNotesData(false);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
	}

    @Override
    public void deleteItems()
    {
        super.deleteItems();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        TextView title = new TextView(getActivity());
        title.setText("Confirm");
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLUE);
        title.setTextSize(20);
        builder.setCustomTitle(title);

        builder.setMessage(getResources().getString(R.string.str_confirm_dialog_delete_pending_contact));
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
                ImageView btnDelete = (ImageView) getActivity().findViewById(R.id.btnDelete);
                btnDelete.setImageResource(R.drawable.img_trash_disable);

                List<ExchangeItem> selectedItems = adapter.getSelectedItems();
                StringBuffer emails = new StringBuffer();
                StringBuffer directorys = new StringBuffer();

                for (ExchangeItem item : selectedItems) {
                    if (item.contactType != 2)
                        emails.append(item.email).append(",");
                    else
                        directorys.append(item.id).append(",");
                }

                if (emails.length() > 0)
                    nRequestCnt++;
                if (directorys.length() > 0)
                    nRequestCnt++;

                if (emails.length() > 0) {
                    emails.deleteCharAt(emails.length() - 1);
                    CBRequest.cancelRequestByEmail(emails.toString(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            nRequestCnt--;
                            if (response.isSuccess()) {
                                if (nRequestCnt == 0) {
                                    adapter.deleteSelectedItems();
                                    updateBadgeNum();
                                    updateNotesData(true);
                                    adapter.notifyDataSetChanged();
                                }
                            } else {

                            }
                        }
                    });
                }

                if (directorys.length() > 0) {
                    directorys.deleteCharAt(directorys.length() - 1);
                    DirectoryRequest.cancelPendingRequest(directorys.toString(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            nRequestCnt--;
                            if (response.isSuccess()) {
                                if (nRequestCnt == 0) {
                                    adapter.deleteSelectedItems();
                                    updateBadgeNum();
                                    updateNotesData(true);
                                    adapter.notifyDataSetChanged();
                                }
                            } else {

                            }
                        }
                    });
                }

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
