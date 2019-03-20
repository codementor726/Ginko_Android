package com.ginko.activity.common;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;

import com.ginko.ginko.R;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoViewActivity extends Activity {
    ImageView mImageView;
    PhotoViewAttacher mAttacher;

    String imagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_photo_view);

        // Any implementation of ImageView can be used!
        mImageView = (ImageView) findViewById(com.example.imagescan.R.id.iv_photo);

        Bundle bundle = getIntent().getExtras();
        if (bundle!=null){
            imagePath = bundle.getString("image_path");
        }

        if(imagePath!=null){
            // Set the Drawable displayed
            Drawable bitmap = Drawable.createFromPath(imagePath);
            mImageView.setImageDrawable(bitmap);

            // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
            mAttacher = new PhotoViewAttacher(mImageView);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
