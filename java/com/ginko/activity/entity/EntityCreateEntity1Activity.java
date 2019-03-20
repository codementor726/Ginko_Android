package com.ginko.activity.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.api.request.EntityRequest;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.PopupListItemAdapter;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;
import com.ginko.vo.PageCategory;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

public class EntityCreateEntity1Activity extends MyBaseActivity implements View.OnClickListener{

    private RelativeLayout rootLayout;
    private ImageButton btnPrev , btnConfirm;

	private TextView spinnerCategory;
	private int mainCategoryId;

    private ListView categoryListView;
    private List<PageCategory> categories = new ArrayList<PageCategory>();
    private List<String> categoryNames = new ArrayList<String>();

    private CategoryListAdapter adapter;

    private int nSelectedCategoryIndex = 0;

    private PopupWindow categoryPopup;
    private View popupview;

    private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entity_create_entity1);

        mainCategoryId = this.getIntent().getExtras().getInt("main_category_id");

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);

        spinnerCategory = (TextView) findViewById(R.id.spinnerCategory); spinnerCategory.setOnClickListener(this);

        if(mainCategoryId != 0){
            //add default name "Select a category"
            if(categoryNames == null)
                categoryNames = new ArrayList<String>();
            categoryNames.add(getResources().getString(R.string.choose_a_category));
            if(categories == null)
                categories = new ArrayList<PageCategory>();
            categories.add(new PageCategory());

            adapter = new CategoryListAdapter(this , categoryNames);

            //create popup window

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            popupview = inflater.inflate(R.layout.entity_category_popup_layout, null , false);

            categoryListView = (ListView)popupview.findViewById(R.id.categoryList);
            categoryListView.setBackgroundColor(Color.TRANSPARENT);

            categoryListView.setAdapter(adapter);

            categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    nSelectedCategoryIndex = position;
                    String categoryName = ((CategoryListAdapter)parent.getAdapter()).getItem(position);
                    if(view != null)
                    {
                        spinnerCategory.setText(categoryName);
                    }
                    if(categoryPopup!=null && categoryPopup.isShowing())
                        categoryPopup.dismiss();
                }
            });

            //select the first font as default
            spinnerCategory.setText(categoryNames.get(0));

			loadCategories(mainCategoryId);
		}else{
			TextView title= (TextView)findViewById(R.id.textView1);
			title.setText("Cause or Community");
            spinnerCategory.setVisibility(View.INVISIBLE);
		}


	}

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.getInstance().hideKeyboard(rootLayout);
    }

    private void loadCategories(int mainCategoryId) {
		/*PageCategory categories = DataCache.getCatetoryByid(mainCategoryId);
		if (categories!=null){
            updateCategorySpinner(categories);
			return;
		}*/
		EntityRequest.getCategoryById(mainCategoryId, new ResponseCallBack<PageCategory>() {

			@Override
			public void onCompleted(JsonResponse<PageCategory> response) {
				if (response.isSuccess()) {
					PageCategory data = response.getData();
                    updateCategorySpinner(data);
				}
			}
		});
	}

	private void updateCategorySpinner(PageCategory data) {
        List<PageCategory> childCategories = data.getChildren();
        for(int i=0;i<childCategories.size();i++)
        {
            categories.add(childCategories.get(i));
            categoryNames.add(childCategories.get(i).getName());
        }
		adapter.notifyDataSetChanged();
		
		TextView title= (TextView)findViewById(R.id.textView1);
		title.setText(data.getName());
	}

    private void showPopup(View view)
    {
        if(view == null) return;
        if(categoryPopup == null)
        {
            categoryPopup = new PopupWindow(this);
            categoryPopup.setFocusable(true);
            categoryPopup.setBackgroundDrawable(new ColorDrawable(
                    android.graphics.Color.TRANSPARENT));
            //popupWindow.setWidth(250);
            //popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.white));
            //popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            categoryPopup.setWindowLayoutMode(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            //categoryPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            // set the list view as pop up window content
            categoryPopup.setContentView(popupview);
        }
        if(categoryPopup.isShowing())
        {
            categoryPopup.dismiss();
        }
        else {
            //int[] location = new int[2];
            //view.getLocationOnScreen(location);
            //LinearLayout popupLayout = (LinearLayout)findViewById(R.id.popupLayout);
            //categoryPopup.showAtLocation(view , Gravity.NO_GRAVITY ,  location[0]-categoryPopup.getWidth(), location[1]);
            categoryPopup.showAsDropDown(view, 0, 0); // show popup like dropdown list
        }


    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;

            case R.id.btnConfirm:
                EditText txtName = (EditText)findViewById(R.id.txtName);
                EditText txtEntityTags	= (EditText)findViewById(R.id.txtEntityTags);
                if (txtName.getText() == null || txtName.getText().toString().isEmpty()){
                    Uitils.alert("Please enter Entity Name.");
                    return;
                }
                if (txtEntityTags.getText() == null || txtEntityTags.getText().toString().isEmpty()){
                    Uitils.alert("Please enter key search words.");
                    return;
                }
                int category = mainCategoryId;
                if (mainCategoryId!=0){
                    if(nSelectedCategoryIndex == 0)
                    {
                        Uitils.alert("Please choose a category.");
                        return;
                    }
                    PageCategory categoryy = (PageCategory)categories.get(nSelectedCategoryIndex);
                    category= categoryy.getId();
                }
                String name = txtName.getText().toString();
                String searchWords = txtEntityTags.getText().toString();

                EntityVO entity = new EntityVO();
                entity.setName(name);
                entity.setTags(searchWords);
                entity.setCategoryId(category);
                entity.setPrivilege(1);
                EntityRequest.saveEntity(entity, new ResponseCallBack<EntityVO>(){

                    @Override
                    public void onCompleted(JsonResponse<EntityVO> response) {
                        if (response.isSuccess()){
                            Bundle bundle = new Bundle();
                            EntityVO myEntity = null;
                            myEntity = response.getData();
                            for (int i = 0; i < myEntity.getEntityInfos().size(); i++) {
                                EntityInfoVO location = myEntity.getEntityInfos().get(i);
                                if (location.isAddressConfirmed() == false) {
                                    location.setLatitude(null);
                                    location.setLongitude(null);
                                }
                            }

                            bundle.putSerializable("entity", myEntity);
                            Intent intent = new Intent(EntityCreateEntity1Activity.this,TradeCardPhotoEditorSetActivity.class);
                            intent.putExtra("isSetNewPhotoInfo", true);
                            intent.putExtra("tradecardType", ConstValues.ENTITY_PHOTO_EDITOR);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            if(EntityCategorySelectActivity.getInstance() != null)
                                EntityCategorySelectActivity.getInstance().finish();
                            finish();
                        }
                    }
                });
                break;

            case R.id.spinnerCategory:
                MyApp.getInstance().hideKeyboard(rootLayout);
                final View view = v;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showPopup(view);
                    }
                } , 100);

                break;
        }
    }

    class CategoryListAdapter extends PopupListItemAdapter<String> {

        private Context mContext;
        public CategoryListAdapter(Context context, List<String> objects) {
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
