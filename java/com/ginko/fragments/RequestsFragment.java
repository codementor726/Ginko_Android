package com.ginko.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ginko.activity.exchange.ExchangeItem;
import com.ginko.activity.exchange.ExchangeRequestActivity;
import com.ginko.activity.exchange.ExchangeRequestListAdapter;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.activity.profiles.UserEntityMultiLocationsProfileActivity;
import com.ginko.activity.profiles.UserEntityProfileActivity;
import com.ginko.api.request.CBRequest;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.MyDataUtils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.UserEntityProfileVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class RequestsFragment extends BaseExchangeFragment {
    private int nLoadCnt = 0;
    private int nRequestCnt = 0;
    private final int SHARE_YOUR_LEAF_ACTIVITY = 2;

    private UserEntityProfileVO entity;
    public RequestsFragment(){
        super(ExchangeItem.EXCHANGE_REQUESTS);
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
                if (getIsSelectable()) {
                    adapter.triggerSelection(position);
                    ImageView btnDelete = (ImageView) getActivity().findViewById(R.id.btnDelete);
                    if (getSelectedItemsCount() > 0)
                        btnDelete.setImageResource(R.drawable.img_trash_white_border);
                    else
                        btnDelete.setImageResource(R.drawable.img_trash_disable);
                } else {
                    ExchangeItem item = (ExchangeItem) adapter.getItem(position);
                    final int idxPosition = position;
                    final int entityID = item.entityId;

                    if (item.contactType == 3) {
                        EntityRequest.viewEntity(item.entityId, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    try {
                                        JSONObject jsonObj = response.getData();
                                        try {
                                            entity = JsonConverter.json2Object(
                                                    (JSONObject) jsonObj, (Class<UserEntityProfileVO>) UserEntityProfileVO.class);
                                        } catch (JsonConvertException e) {
                                            e.printStackTrace();
                                            entity = null;
                                        }

                                        if (entity.getInfos().size() > 1) {
                                            Intent entityProfileIntent = new Intent(getActivity(), UserEntityMultiLocationsProfileActivity.class);
                                            entityProfileIntent.putExtra("entityJson", entity);
                                            entityProfileIntent.putExtra("contactID", jsonObj.getInt("entity_id"));
                                            entityProfileIntent.putExtra("isfollowing_entity", false);
                                            startActivity(entityProfileIntent);
                                        } else {
                                            Intent entityProfileIntent = new Intent(getActivity(), UserEntityProfileActivity.class);
                                            entityProfileIntent.putExtra("entityJson", entity);
                                            entityProfileIntent.putExtra("contactID", jsonObj.getInt("entity_id"));
                                            entityProfileIntent.putExtra("isfollowing_entity", false);
                                            startActivity(entityProfileIntent);
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (response.getErrorCode() == 700 && response.getErrorMessage().equals("The entity can't be found.")) {
                                        MyApp.getInstance().getContactsModel().deleteContactWithContactId(entityID);
                                        MyApp.getInstance().removefromContacts(entityID);
                                        adapter.deleteItem(idxPosition);
                                        updateBadgeNum();
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }, true);
                    } else {
                        if (item.contactType != 4)
                        {
                            Intent contactSharingSettingIntent = new Intent(getActivity(), ShareYourLeafActivity.class);
                            contactSharingSettingIntent.putExtra("contactID", String.valueOf(item.contactId));
                            String contactName = item.contactName.equals("") ? item.email : item.contactName;
                            contactSharingSettingIntent.putExtra("contactFullname", contactName);
                            contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                            contactSharingSettingIntent.putExtra("isInviteContact", true);

                            startActivity(contactSharingSettingIntent);
                        }
                        else
                        {
                            Intent contactSharingSettingIntent = new Intent(getActivity() , ShareYourLeafActivity.class);
                            contactSharingSettingIntent.putExtra("contactID" , "0");
                            String contactName = item.contactName.equals("") ? item.email : item.contactName;
                            contactSharingSettingIntent.putExtra("contactFullname" , contactName);
                            contactSharingSettingIntent.putExtra("isDirectory" , true);
                            contactSharingSettingIntent.putExtra("directoryID", item.id);
                            contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                            contactSharingSettingIntent.putExtra("isInviteContact", true);
                            contactSharingSettingIntent.putExtra("StartActivity", "ContactMain");

                            getActivity().startActivityForResult(contactSharingSettingIntent, SHARE_YOUR_LEAF_ACTIVITY);
                        }
                    }
                }
            }
        });
        loadData();
    }
    @Override
    public void loadData() {
        if (adapter == null)
            return;
        adapter.clearAll();
        nLoadCnt = 0;

        CBRequest.getExchangeRequestsNew(null, null, null, new ResponseCallBack<JSONObject>() {
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
                            ExchangeItem item = new ExchangeItem(ExchangeItem.EXCHANGE_REQUESTS);
                            item.id = obj.optInt("id" , 0);

                            item.email = obj.optString("email" , "");
                            item.contactType = obj.optInt("contact_type", 1);
                            if(item.contactType == 3)//entity
                            {
                                item.entityId = obj.optInt("entity_id");
                                item.nFollowerCount = obj.optInt("follower_total" , 0);
                                String contactName = obj.optString("name", "");
                                item.contactName = contactName.trim();
                                item.photoUrl = obj.optString("profile_image" , "");
                                item.setTime(obj.optString("create_time"));
                            }
                            else
                            {
                                String contactName = obj.optString("first_name", "") + " " + obj.optString("last_name", "");
                                item.contactName = contactName.trim();
                                item.photoUrl = obj.optString("profile_image", "");
                                if (item.photoUrl.equals(""))
                                    item.photoUrl = obj.optString("photo_url", "");
                                item.contactId = obj.optInt("contact_id");
                                item.setTime(obj.optString("request_time"));
                            }
                            adapter.addItem(item);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (nLoadCnt == 2 && getActivity() != null)
                    {
                        nLoadCnt = 0;
                        updateBadgeNum();
                        updateNotesData(false);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        DirectoryRequest.getReceivedInvite(null, null, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                nLoadCnt++;
                if (response.isSuccess()) {
                    JSONObject resultObj = response.getData();
                    try {
                        JSONArray resultArray = resultObj.getJSONArray("data");
                        for (int i = 0; i < resultArray.length(); i++) {
                            JSONObject obj = (JSONObject) resultArray.get(i);
                            ExchangeItem item = new ExchangeItem(ExchangeItem.EXCHANGE_REQUESTS);
                            item.id = obj.optInt("id", 0);

                            item.contactType = 4;

                            String contactName = obj.optString("name", "");
                            item.contactName = contactName.trim();
                            item.photoUrl = obj.optString("profile_image", "");
                            if (item.photoUrl.equals(""))
                                item.photoUrl = obj.optString("photo_url", "");
                            //item.contactId = obj.optInt("contact_id");
                            //item.setTime(obj.optString("request_time"));
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

        builder.setMessage(getResources().getString(R.string.str_confirm_dialog_delete_contact_request));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
                ImageView btnDelete = (ImageView) getActivity().findViewById(R.id.btnDelete);
                btnDelete.setImageResource(R.drawable.img_trash_disable);

                List<ExchangeItem> selectedItems = adapter.getSelectedItems();
                StringBuffer contactUids = new StringBuffer();
                StringBuffer entityIds = new StringBuffer();
                StringBuffer directoryIds = new StringBuffer();

                for (ExchangeItem item : selectedItems) {
                    if (item.contactType == 3)//user contact
                        entityIds.append(String.valueOf(item.entityId)).append(",");
                    else if (item.contactType == 4)
                        directoryIds.append(String.valueOf(item.id)).append(",");
                    else
                        contactUids.append(String.valueOf(item.contactId)).append(",");

                }

                if (contactUids.length() > 0 || entityIds.length() > 0)
                    nRequestCnt++;
                if (directoryIds.length() > 0)
                    nRequestCnt++;

                if (contactUids.length() > 0 || entityIds.length() > 0) {
                    String strContactIds = null, strEntityIds = null;
                    if (contactUids.length() > 0) {
                        contactUids.deleteCharAt(contactUids.length() - 1);
                        strContactIds = contactUids.toString();
                    }

                    if (entityIds.length() > 0) {
                        entityIds.deleteCharAt(entityIds.length() - 1);
                        strEntityIds = entityIds.toString();
                    }

                    CBRequest.deleteRequest(strContactIds, strEntityIds,
                            new ResponseCallBack<Void>() {

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

                if (directoryIds.length() > 0) {
                    String strDirectoryIds = null;
                    directoryIds.deleteCharAt(directoryIds.length() - 1);
                    strDirectoryIds = directoryIds.toString();

                    DirectoryRequest.removeJoinRequest(strDirectoryIds, new ResponseCallBack<Void>() {
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
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
/*
	@Override
	protected void deleteItems() {
		StringBuffer contactUids = new StringBuffer();
		for (JSONObject requestJson : this.selected) {
			String contact_id = requestJson.optString("contact_id");
			contactUids.append(contact_id).append(",");
		}
		if (contactUids.length() > 0) {
			contactUids.deleteCharAt(contactUids.length() - 1);
			CBRequest.deleteRequest(contactUids.toString(),
					new ResponseCallBack<Void>() {

						@Override
						public void onCompleted(JsonResponse<Void> response) {
							if (response.isSuccess()) {
								onDeleteSuccess();
							}

						}
					});

		}
	}*/

	/*
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		if (requestCode == 1000) {
			SharingBean sb = (SharingBean) data
					.getSerializableExtra("shareBean");
			Integer contactUid = data.getIntExtra("contact_uid",0);
			if (contactUid==0){
				contactUid = null;
                Logger.debug("contactUid is null, it should be impossible!");
			}
			final Integer contactUidTemp = contactUid;
			CBRequest.answerExchangeRequest(contactUid, sb.getSharingStatus(),
					sb.getSharedHomeFieldIds(), sb.getSharedWorkFieldIds(),
					new ResponseCallBack<Void>() {
						@Override
						public void onCompleted(JsonResponse<Void> response) {
							removeFromView(contactUidTemp);
							incrementBadge(-1);
						}

					});

		}
	}*/

}
