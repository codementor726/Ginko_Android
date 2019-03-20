package com.videophotofilter.android.com;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ginko.context.ConstValues;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;

public class SelectMoreColorActivity  extends Activity implements OnClickListener{

    private final int FILTERED_PHOTO = 4;

    private TextView txtTitle;
	private ImageButton btnPrev;
	private GridView colorGridView;
	
	//gird adapter
	private CustomGridAdapter gridAdpater;
	
	//colors
	final int colors[] = {0xffff8b8b , 0xffbaacff , 0xff7fb2fe , 0xff6ce0d6 , 0xffb8fa8f , 0xffffdb4d , 0xfffdb6f5
			 			, 0xffff5757 , 0xff9b87ff , 0xff599bff , 0xff36dfd1 , 0xff97ef61 , 0xffffd52f , 0xffff8cf3
			 			, 0xffff3c3c , 0xff795eff , 0xff2d81ff , 0xff05e3d1 , 0xff75f443 , 0xffffcb00 , 0xfff96feb
			 			, 0xffff0000 , 0xff4722ff , 0xff0066ff , 0xff00bfaf , 0xff59ee1f , 0xffffba00 , 0xfffa40e7
			 			, 0xffdf0000 , 0xff2700e9 , 0xff0054d3 , 0xff00a89a , 0xff3fe100 , 0xffffa200 , 0xfffa00e0
			 			, 0xffa10000 , 0xff2000c2 , 0xff0044aa , 0xff24aa9f , 0xff50c722 , 0xffe69405 , 0xffc300af
			 			, 0xffa02121 , 0xff341ea2 , 0xff2158a9 , 0xff47ada5 , 0xff6cc849 , 0xffdaa13e , 0xffb026a2
			 			, 0xffa13f3f , 0xff4d3ba3 , 0xff446da9 , 0xff66ada7 , 0xff8ace70 , 0xffdcb36b , 0xffb447a9
			 			, 0xffa96363 , 0xff6c60a4 , 0xff6480a8 , 0xff88bab6 , 0xffa9e493 , 0xffe2c38d , 0xffc370bb
			 			, 0xffd09696 , 0xff9d94c7 , 0xff9ab2d4 , 0xffb6dcd9 , 0xffcae9b4 , 0xfff1d7a9 , 0xffd8a8d3};

	private int nTradeCardType = ConstValues.HOME_PHOTO_EDITOR;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectmorecolor);

		Intent intent = this.getIntent();
		this.nTradeCardType = intent.getIntExtra("tradecardType" , ConstValues.HOME_PHOTO_EDITOR);

		getUIObjects();
		
	}

	private void getUIObjects()
	{
        txtTitle = (TextView)findViewById(R.id.txtTitle);
        if(MyApp.currentTakePhotoTitle != null)
        {
            txtTitle.setText(MyApp.currentTakePhotoTitle);
        }
        else
        {
            txtTitle.setText(getResources().getString(R.string.home_info));
        }

		btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
		
		colorGridView = (GridView)findViewById(R.id.colorgird);
		
		
		
		gridAdpater = new CustomGridAdapter(this , colors);
		
		colorGridView.setAdapter(gridAdpater);
		
		colorGridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SelectMoreColorActivity.this , PhotoFilterActivity.class);
				intent.putExtra("isResource", true);
				intent.putExtra("path_or_name", "bc_"+String.valueOf(arg2+1));
				intent.putExtra("tradecardType", nTradeCardType);
				startActivityForResult(intent, FILTERED_PHOTO);
			}});
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btnPrev:
			finish();
			break;
		
		}
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FILTERED_PHOTO && resultCode == RESULT_OK)
        {
            String backgroundPhotoPath = data.getStringExtra("backgroundPhotoPath");
            String foregroundPhotoPath = data.getStringExtra("foregroundPhotoPath");
            int foregroundLeft = data.getIntExtra("foregroundLeft" , 0);
            int foregroundTop = data.getIntExtra("foregroundTop" , 0);
            int foregroundWidth = data.getIntExtra("foregroundWidth" , 0);
            int foregroundHeight = data.getIntExtra("foregroundHeight" , 0);


            Intent resultIntent = new Intent();
            resultIntent.putExtra("backgroundPhotoPath" , backgroundPhotoPath);
            resultIntent.putExtra("foregroundPhotoPath" , foregroundPhotoPath);
            resultIntent.putExtra("foregroundLeft" , foregroundLeft);
            resultIntent.putExtra("foregroundTop" , foregroundTop);
            resultIntent.putExtra("foregroundWidth" , foregroundWidth);
            resultIntent.putExtra("foregroundHeight" , foregroundHeight);

            SelectMoreColorActivity.this.setResult(RESULT_OK , resultIntent);
            SelectMoreColorActivity.this.finish();
        }
    }

    private int getDrawableIdFromName(String name)
    {
    	Resources resources = getResources();
    	final int resourceId = resources.getIdentifier(name, "drawable", 
    			getPackageName());
    	return resourceId;
    }
	
	class CustomGridAdapter extends BaseAdapter{
	    private Context mContext;
	    public int colorsRGB[];
	    private LayoutInflater inflater ;
	    public CustomGridAdapter(Context c,int[] colors) {
	          mContext = c;
	          colorsRGB = colors;
	          
	          inflater = (LayoutInflater) mContext
	    		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    }
	    @Override
	    public int getCount() {
	      // TODO Auto-generated method stub
	      return colorsRGB.length;
	    }
	    @Override
	    public Object getItem(int position) {
	      // TODO Auto-generated method stub
	      return colorsRGB[position];
	    }
	    @Override
	    public long getItemId(int position) {
	      // TODO Auto-generated method stub
	      return position;
	    }
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	      // TODO Auto-generated method stub
	    	View colorView = null;
	      	if (convertView == null) {
    	  
	    		colorView = inflater.inflate(R.layout.color_grid_single_item, null);
		    } else {
				  colorView = convertView;
		    }

		  	View colorV = colorView.findViewById(R.id.colorView);

		  	colorV.setBackgroundColor(colorsRGB[position]);

	        return colorView;
	    }
	    
	    
	}
}
