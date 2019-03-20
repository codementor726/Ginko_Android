package com.ginko.setup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.customview.MyViewPager;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.videophotofilter.library.android.com.AspectFrameLayout;

public class TutorialActivity extends MyBaseActivity implements View.OnClickListener{

    /* UI Objects */
    private ImageButton btnClose;
    private AspectFrameLayout aspectFrameLayout;
    private ImageView btnPageIndex0 , btnPageIndex1 ,btnPageIndex2, btnPageIndex3 ,btnPageIndex4 , btnPageIndex5, btnPageIndex6 , btnPageIndex7;

    private ViewPager screenViewPager;

    /* Variables*/
    private boolean isFromSignUp = false;
    private int currentPageIndex = 0;

    private int[] tutorialImagesId = {R.drawable.tutorial1 , R.drawable.tutorial2 , R.drawable.tutorial3 , R.drawable.tutorial4,
                    R.drawable.tutorial5 , R.drawable.tutorial6, R.drawable.tutorial7, R.drawable.tutorial8};

    private ViewPagerAdapter adapter;
    private MyOnPageChangeListener pageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        Intent intent = this.getIntent();
        if(intent != null && intent.hasExtra("isFromSignUp"))
        {
            isFromSignUp = intent.getBooleanExtra("isFromSignUp" , false);
        }

        currentPageIndex = 0;

        getUIObjects();
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnClose = (ImageButton)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);

        aspectFrameLayout = (AspectFrameLayout)findViewById(R.id.aspectFrameView);
        aspectFrameLayout.setAspectRatio(0.61296d);

        btnPageIndex0 = (ImageView)findViewById(R.id.btnPageIndex0); btnPageIndex0.setOnClickListener(this);
        btnPageIndex1 = (ImageView)findViewById(R.id.btnPageIndex1); btnPageIndex1.setOnClickListener(this);
        btnPageIndex2 = (ImageView)findViewById(R.id.btnPageIndex2); btnPageIndex2.setOnClickListener(this);
        btnPageIndex3 = (ImageView)findViewById(R.id.btnPageIndex3); btnPageIndex3.setOnClickListener(this);
        btnPageIndex4 = (ImageView)findViewById(R.id.btnPageIndex4); btnPageIndex4.setOnClickListener(this);
        btnPageIndex5 = (ImageView)findViewById(R.id.btnPageIndex5); btnPageIndex5.setOnClickListener(this);
        btnPageIndex6 = (ImageView)findViewById(R.id.btnPageIndex6); btnPageIndex6.setOnClickListener(this);
        btnPageIndex7 = (ImageView)findViewById(R.id.btnPageIndex7); btnPageIndex7.setOnClickListener(this);

        screenViewPager = (ViewPager)findViewById(R.id.viewPager);

        adapter = new ViewPagerAdapter(TutorialActivity.this , tutorialImagesId);
        pageListener = new MyOnPageChangeListener();
        screenViewPager.setOnPageChangeListener(pageListener);
        screenViewPager.setAdapter(adapter);
        screenViewPager.setCurrentItem(currentPageIndex);

        selectPage(currentPageIndex);
        updatePageIndicator(currentPageIndex);
    }

    private void selectPage(int pageIndex)
    {
        currentPageIndex = pageIndex;

        screenViewPager.setCurrentItem(currentPageIndex);

        updatePageIndicator(pageIndex);
    }

    private void updatePageIndicator(int pageIndex)
    {
        btnPageIndex0.setImageResource(R.drawable.purple_pageindicator_outline);
        btnPageIndex1.setImageResource(R.drawable.purple_pageindicator_outline);
        btnPageIndex2.setImageResource(R.drawable.purple_pageindicator_outline);
        btnPageIndex3.setImageResource(R.drawable.purple_pageindicator_outline);

        btnPageIndex4.setImageResource(R.drawable.green_pageindicator_outline);
        btnPageIndex5.setImageResource(R.drawable.green_pageindicator_outline);
        btnPageIndex6.setImageResource(R.drawable.green_pageindicator_outline);
        btnPageIndex7.setImageResource(R.drawable.green_pageindicator_outline);


        switch(pageIndex)
        {
            case 0:
                btnPageIndex0.setImageResource(R.drawable.purple_pageindicator_fill);
                break;
            case 1:
                btnPageIndex1.setImageResource(R.drawable.purple_pageindicator_fill);
                break;
            case 2:
                btnPageIndex2.setImageResource(R.drawable.purple_pageindicator_fill);
                break;
            case 3:
                btnPageIndex3.setImageResource(R.drawable.purple_pageindicator_fill);
                break;
            case 4:
                btnPageIndex4.setImageResource(R.drawable.green_pageindicator_fill);
                break;
            case 5:
                btnPageIndex5.setImageResource(R.drawable.green_pageindicator_fill);
                break;
            case 6:
                btnPageIndex6.setImageResource(R.drawable.green_pageindicator_fill);
                break;
            case 7:
                btnPageIndex7.setImageResource(R.drawable.green_pageindicator_fill);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
       switch(v.getId())
       {
           case R.id.btnClose:
                if(isFromSignUp)
                {
                    Intent contactMainIntent = new Intent(TutorialActivity.this , ContactMainActivity.class);
                    TutorialActivity.this.startActivity(contactMainIntent);
                    finish();
                }
                else
                {
                    finish();
                }
               break;

           case R.id.btnPageIndex0:
               selectPage(0);
               break;

           case R.id.btnPageIndex1:
               selectPage(1);
               break;

           case R.id.btnPageIndex2:
               selectPage(2);
               break;
           case R.id.btnPageIndex3:
               selectPage(3);
               break;
           case R.id.btnPageIndex4:
               selectPage(4);
               break;
           case R.id.btnPageIndex5:
               selectPage(5);
               break;
           case R.id.btnPageIndex6:
               selectPage(6);
               break;
           case R.id.btnPageIndex7:
               selectPage(7);
               break;

       }
    }

    public class ViewPagerAdapter extends PagerAdapter {
        // Declare Variables
        Context context;
        int[] tutorialResourceImages;

        public ViewPagerAdapter(Context context, int[] images) {
            this.context = context;
            this.tutorialResourceImages = images;
        }

        @Override
        public int getCount() {
            return tutorialResourceImages.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((ImageView) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            ImageView itemView = new ImageView(context);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT);
            // Capture position and set to the TextViews

            itemView.setImageResource(tutorialResourceImages[position]);

            // Add viewpager_item.xml to ViewPager
            ((ViewPager) container).addView(itemView , params);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // Remove viewpager_item.xml from ViewPager
            ((ViewPager) container).removeView((ImageView) object);

        }
    }
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            currentPageIndex = position;

            updatePageIndicator(currentPageIndex);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
