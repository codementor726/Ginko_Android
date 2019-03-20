package com.ginko.activity.entity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.ginko.api.request.EntityRequest;
import com.ginko.context.ConstValues;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import java.util.ArrayList;

public class EntityCategorySelectActivity extends MyBaseActivity implements View.OnClickListener{

    private ImageButton btnBack;

	public static EntityCategorySelectActivity mInstance;

	public static EntityCategorySelectActivity getInstance()
	{
		return EntityCategorySelectActivity.mInstance;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entity_category_select);

		btnBack = (ImageButton)findViewById(R.id.btnBack); btnBack.setOnClickListener(this);

		EntityCategorySelectActivity.mInstance = this;
	}

	public void selectCategory (View view){
		int id = view.getId();
		int mainCategoryId = 0;  //It's cause
		if (id == R.id.categoryFive) {
			mainCategoryId =4;
		}else if (id == R.id.categoryTwo) {
			mainCategoryId =1;
		}else if (id == R.id.categoryFour) {
			mainCategoryId =3;
		}else if (id == R.id.categoryThree) {
			mainCategoryId =2;
		}else if (id == R.id.categoryOne) {
			mainCategoryId =0;
		}else if(id == R.id.categorySix){
			mainCategoryId =5;
		}
		
		/*Intent intent = new Intent();
		intent.setClass(this, EntityCreateEntity1Activity.class);
		intent.putExtra("main_category_id", mainCategoryId);
		startActivity(intent);*/
        //finish();

		EntityVO entity = new EntityVO();
		entity.setName("");
		entity.setTags("");
		entity.setId(0);//default 0
		entity.setCategoryId(mainCategoryId);
		entity.setPrivilege(1);
		entity.setProfileImage("");
		entity.setEntityInfos(new ArrayList<EntityInfoVO>());
		entity.setEntityImages(new ArrayList<EntityImageVO>());

		Bundle bundle = new Bundle();
		bundle.putSerializable("entity", entity);
		bundle.putBoolean("isNewEntity", true);
		bundle.putBoolean("isCreate", false);
		Intent intent = new Intent(EntityCategorySelectActivity.this,EntityEditActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
		//sEntityCategorySelectActivity.this.finish();
	}

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnBack:
                finish();
                break;
        }

    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EntityCategorySelectActivity.mInstance = null;
	}
}
