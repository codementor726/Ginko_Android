package com.ginko.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.entity.EntityInfoItem;
import com.ginko.activity.entity.EntityInfoItemView;
import com.ginko.activity.exchange.ExchangeItem;
import com.ginko.api.request.GeoLibrary;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.ginko.R;
import com.ginko.vo.EntityInfoDetailVO;
import com.ginko.vo.EntityInfoVO;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityInfoEditFragment extends Fragment  implements EntityInfoItemView.EditFocusChangeListener{

	private View view;

    private boolean isEditable = false;
    private boolean isUICreated =false;

    private List<EntityInfoItem> entityInfoItemList;

    public int position = 0;

    private GetLatLngFromAddress confirmAddressValidationTask = null;

    InputFilter smileyFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                //System.out.println("Type : " + type);
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        }
    };

    public void setIsEditable(boolean editable){
        this.isEditable = editable;
        //refresh selection when list is changed into selection mode
        if(entityInfoItemViews == null) return;

        for(int i=0;i<entityInfoItemList.size();i++) {
            entityInfoItemList.get(i).setIsSelected(false);
            entityInfoItemViews.get(i).setIsEditable(isEditable);
        }

    }
    public void updateListView(){
        if(isUICreated) {
            for(int i=0;i<entityInfoItemList.size();i++)
            {
                if(entityInfoItemList.get(i).getVisibility()) {
                    entityInfoItemViews.get(i).setVisibility(View.VISIBLE);
                    entityInfoItemViews.get(i).refreshView();
                }
                else
                    entityInfoItemViews.get(i).setVisibility(View.GONE);
            }
        }
    }

    public void refreshFieldsData()
    {
        if(isUICreated) {
            for(int i=0;i<entityInfoItemList.size();i++)
            {
                if(entityInfoItemList.get(i).getVisibility()) {
                    entityInfoItemViews.get(i).setVisibility(View.VISIBLE);
                    entityInfoItemViews.get(i).refreshData();
                }
                else
                    entityInfoItemViews.get(i).setVisibility(View.GONE);
            }
        }
    }

    public boolean saveEditingInfoItems()
    {
        if(isUICreated)
        {
            for(int i=0;i<entityInfoItemList.size();i++)
            {
                if(entityInfoItemList.get(i).getVisibility())
                {
                    entityInfoItemViews.get(i).getCurrentEditTextString();

                    EntityInfoItem item = entityInfoItemList.get(i);

                    if(item.getFieldType().equalsIgnoreCase("email") && !isEmailValid(item.getFieldValue()) && !item.getFieldValue().isEmpty()) {
                        Uitils.alert(getActivity(), item.getFieldValue() + " is invalid email address.");
                        return false;
                    }
                }
            }
        }

        return true;
    }


    public void addNewInfoItem(String fieldName , int index)
    {
        if(index<entityInfoItemList.size())
        {
            entityInfoItemList.get(index).setVisibility(true);
            entityInfoItemList.get(index).setIsSelected(false);
            entityInfoItemList.get(index).setFieldValue("");
            entityInfoItemViews.get(index).getEditText().setText("");
        }
    }

    public void deleteSelectedItems()
    {
        for(int i=0;i<entityInfoItemList.size();i++)
        {
            if(!entityInfoItemList.get(i).isDefaultField() && entityInfoItemList.get(i).getIsSelected())
            {
                entityInfoItemList.get(i).setVisibility(false);
                entityInfoItemList.get(i).setFieldValue("");
                entityInfoItemList.get(i).setIsSelected(false);
                entityInfoItemViews.get(i).getEditText().setText("");
            }
        }
    }

    private List<EntityInfoItemView> entityInfoItemViews;

    private LinearLayout infoListLayout;

    public EntityInfoEditFragment()
    {}


    public EntityInfoEditFragment(List<EntityInfoItem> infoList){
        super();
        this.entityInfoItemList = infoList;
    }

    public void setEntityInfoItemList(List<EntityInfoItem> itemList)
    {
        this.entityInfoItemList = itemList;
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        isUICreated = false;
    }
    private int emailInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.subview_entity_info, container, false);
        infoListLayout = (LinearLayout)view.findViewById(R.id.infoListLayout);
        EditText edtEmail = (EditText)view.findViewById(R.id.txtEmail);//email keyboard inputtype is a bit different in some android os , so we get it from a widget that is already set directly

        emailInputType = edtEmail.getInputType();

        entityInfoItemViews = new ArrayList<EntityInfoItemView>();

        for(int i=0;i<entityInfoItemList.size();i++)
        {
            EntityInfoItem item = entityInfoItemList.get(i);
            EntityInfoItemView itemView = new EntityInfoItemView(getActivity() , item , this , emailInputType);
            itemView.getEditText().setFilters(new InputFilter[]{smileyFilter});
            itemView.setIsEditable(this.isEditable);
            itemView.refreshData();
            entityInfoItemViews.add(itemView);
            infoListLayout.addView(itemView);
        }

        isUICreated = true;

		return view;
	}

    @Override
    public void onResume() {
        super.onResume();
        refreshFieldsData();

    }

    @Override
    public void onPause() {
        super.onPause();
        saveEditingInfoItems();
    }

    @Override
    public void onDestroyView() {
        //saveEditingInfoItems();
        isUICreated = false;

        super.onDestroyView();
    }

    public List<EntityInfoItem> getDetailInfoList()
    {
        return entityInfoItemList;
    }

    public void checkAddressConfirmed()
    {

    }


    @Override
    public void onEditFocusChangeListener(boolean hasFocus, String address , EntityInfoItem item) {
        if(hasFocus == true) return;
        try {
            if (confirmAddressValidationTask == null) {
                confirmAddressValidationTask = new GetLatLngFromAddress(getActivity(), address, item);
                if (Build.VERSION.SDK_INT >= 12)
                    confirmAddressValidationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    confirmAddressValidationTask.execute();
            } else {
                if (confirmAddressValidationTask.getStatus() == AsyncTask.Status.PENDING) {
                    confirmAddressValidationTask.cancel(true);
                    confirmAddressValidationTask = new GetLatLngFromAddress(getActivity(), address, item);
                    if (Build.VERSION.SDK_INT >= 12)
                        confirmAddressValidationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        confirmAddressValidationTask.execute();

                } else if (confirmAddressValidationTask.getStatus() == AsyncTask.Status.RUNNING) {
                    return;
                } else if (confirmAddressValidationTask.getStatus() == AsyncTask.Status.FINISHED) {
                    confirmAddressValidationTask = new GetLatLngFromAddress(getActivity(), address, item);
                    if (Build.VERSION.SDK_INT >= 12)
                        confirmAddressValidationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        confirmAddressValidationTask.execute();
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private class GetLatLngFromAddress extends AsyncTask<Void ,Void , Void>
    {
        private Context mContext;
        private String address;
        private EntityInfoItem infoItem;
        private Geocoder gc;
        private Double latitude = null , longitude = null;


        public GetLatLngFromAddress(Context context , String address, EntityInfoItem item)
        {
            this.mContext = context;
            this.address = address;
            this.infoItem = item;
            gc = new Geocoder(context);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            Address addr = null;
            List<Address> addressList = null;
            try {
                if (!TextUtils.isEmpty(address)) {
                    addressList = gc.getFromLocationName(address, 5);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null != addressList && addressList.size() > 0) {
                addr = addressList.get(0);
            }

            if (null != addr && addr.hasLatitude()
                    && addr.hasLongitude()) {

                latitude = addr.getLatitude();
                longitude = addr.getLongitude();
            }
            if (latitude != null && longitude != null)
            {
                infoItem.setIsAddressConfirmed(true);
                infoItem.setLatitude(latitude);
                infoItem.setLongitude(longitude);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(!isUICreated) return;
            /* modify by wang
            if(latitude == null && longitude == null && !infoItem.isAddressSkipped()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirm");
                builder.setMessage(getResources().getString(R.string.str_confirm_dialog_confirm_location_address));
                builder.setNegativeButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        infoItem.setIsAddressConfirmed(false);
                        infoItem.setAddressSkipped(false);
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton(R.string.alert_button_skip, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        infoItem.setIsAddressConfirmed(false);
                        infoItem.setAddressSkipped(true);
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }*/

        }

        @Override
        protected void onCancelled(Void result) {
            // TODO Auto-generated method stub
            super.onCancelled(result);
            infoItem.setIsAddressConfirmed(false);
        }
    }

    private Pattern pattern;
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

}
