package com.ginko.activity.im;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ginko.ginko.R;

import java.util.ArrayList;

/**
 * Created by lee on 4/20/2015.
 */
public class EmoticonsGridAdapter extends BaseAdapter {
    private ArrayList<Integer> emoticonIndexs;
    private int pageNumber;
    private Context mContext;

    private EmoticonKeyClickListener mListener;
    private EmoticonUtility emoticons;

    public EmoticonsGridAdapter(Context context, EmoticonUtility _emoticons , ArrayList<Integer> indexs, int pageNumber, EmoticonKeyClickListener listener) {
        this.mContext = context;
        this.emoticonIndexs = indexs;
        this.pageNumber = pageNumber;
        this.mListener = listener;
        this.emoticons = _emoticons;
        this.emoticonIndexs = new ArrayList<Integer>();
        for(int i=0;i<indexs.size();i++)
            this.emoticonIndexs.add(indexs.get(i));
        this.emoticonIndexs.add(-1);//add back key button index
    }
    @Override
    public int getCount() {
        return this.emoticonIndexs.size();
    }

    @Override
    public Object getItem(int position) {
        return this.emoticonIndexs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getPageNumber () {
        return pageNumber;
    }
    public View getView(final int position, View convertView, ViewGroup parent){

        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.emoticons_item, null);
        }

        final int index = this.emoticonIndexs.get(position);

        ImageView image = (ImageView) v.findViewById(R.id.imgEmoticonItem);
        if(index>=0)//emoticon
            image.setImageBitmap(emoticons.getEmoticon(index));
        else if(index == -1)//back key button
            image.setImageResource(R.drawable.keyboard_backbutton);

        LinearLayout itemLayout = (LinearLayout)v.findViewById(R.id.emoticon_item_rootlayout);

        itemLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.EmoticonKeyClickedIndex(index);
            }
        });

        return v;
    }

    public interface EmoticonKeyClickListener {

        public void EmoticonKeyClickedIndex(int index);
    }
}
