package com.ginko.activity.cb;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ginko.activity.exchange.ExchangeRequestActivity;
import com.ginko.api.request.CBRequest;
import com.ginko.common.Logger;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.CbEmailVO;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CBMainActivity extends MyBaseActivity implements Html.ImageGetter {


	private List<CbEmailVO> cbEmails = new ArrayList<CbEmailVO>();
	private ArrayAdapter<CbEmailVO> adapter;

    private ImageButton btnPrev;
    private ImageView btnAddCBEmail , btnRequestExchange;
    private TextView txtDescription;
    private LinearLayout contentLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cbmain);
		super.setShowLogoutMenu(true);
		
        txtDescription = (TextView)findViewById(R.id.txtDescription);
        txtDescription.setText(Html.fromHtml(getString(R.string.builder_description) , this, null));

        contentLayout = (LinearLayout)findViewById(R.id.contentLayout);

        btnPrev = (ImageButton)findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAddCBEmail = (ImageView)findViewById(R.id.btnAddCBEmail);
        btnAddCBEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cbSelectIntent = new Intent(CBMainActivity.this , CBSelectActivity.class);
                cbSelectIntent.putExtra("isNewGinkoStartUp" , false);
                startActivity(cbSelectIntent);
                //Uitils.toActivity(CBMainActivity.this, CBSelectActivity.class, false);
            }
        });

        btnRequestExchange = (ImageView)findViewById(R.id.btnRequestExchange);
        btnRequestExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uitils.toActivity(CBMainActivity.this, ExchangeRequestActivity.class, false);
            }
        });

		ListView list = (ListView) this.findViewById(R.id.list);


		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CbEmailVO cb = cbEmails.get(position);
				if (cb==null){
					Uitils.alert("Please select a Builder.");
					return;
				}
				Intent intent = new Intent();
				intent.setClass(CBMainActivity.this,
						SaveCBActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("cb", cb);
                bundle.putBoolean("isNewCB" , false);
//				bundle.putInt("cb_id", value);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});

		adapter = new ArrayAdapter<CbEmailVO>(this, R.layout.cb_item,
				cbEmails) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				CbEmailVO item = getItem(position);
				View view = convertView;
				if (view == null) {
					LayoutInflater inflater = (LayoutInflater) parent
							.getContext().getSystemService(
									Context.LAYOUT_INFLATER_SERVICE);
					view = inflater.inflate(R.layout.cb_item, parent, false);
				}
				TextView cbEmailAddress = (TextView) view
						.findViewById(R.id.cbEmailAddress);
				ImageView icon = (ImageView) view.findViewById(R.id.validIcon);
				if (StringUtils.equalsIgnoreCase(item.getActive(), "yes")) {
					icon.setImageResource(R.drawable.confirm);
				} else {
					icon.setImageResource(R.drawable.warning);
				}
				cbEmailAddress.setText(item.getEmail());
				return view;
			}

		};
		list.setAdapter(adapter);


	}
	
	@Override
	protected void onResume(){
		super.onResume();
        CBRequest.getCBemails(this, new ResponseCallBack<List<CbEmailVO>>() {

            @Override
            public void onCompleted(JsonResponse<List<CbEmailVO>> response) {
                if (response.isSuccess()) {
                    cbEmails.clear();
                    cbEmails.addAll(response.getData());
                    for (CbEmailVO cb : response.getData()) {
                        Logger.error(cb.getEmail());
                    }
                    if (cbEmails.size() == 0) {
                        txtDescription.setVisibility(View.VISIBLE);
                        contentLayout.setVisibility(View.INVISIBLE);
                    } else {
                        txtDescription.setVisibility(View.INVISIBLE);
                        contentLayout.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                }

            }
        }, true);
	}

    @Override
    public Drawable getDrawable(String source) {
        int id;

        if (source.equals("cb_add")) {
            id = R.drawable.cb_add;
        }
        else {
            return null;
        }

        Drawable d = getResources().getDrawable(id);
        d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
        return d;
    }

}
