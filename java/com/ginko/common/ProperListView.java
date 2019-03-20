package com.ginko.common;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by YongJong on 08/21/16.
 */
public class ProperListView extends ListView {

    private static final String TAG = ProperListView.class.getName();

    @SuppressWarnings("unused")
    public ProperListView(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public ProperListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public ProperListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();

            refreshVisibleViews();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();

            refreshVisibleViews();
        }
    }

    private DataSetObserver mDataSetObserver = new AdapterDataSetObserver();
    private Adapter mAdapter;

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);

        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;

        mAdapter.registerDataSetObserver(mDataSetObserver);
    }

    void refreshVisibleViews() {
        if (mAdapter != null) {
            for (int i = getFirstVisiblePosition(); i <= getLastVisiblePosition(); i ++) {
                final int dataPosition = i - getHeaderViewsCount();
                final int childPosition = i - getFirstVisiblePosition();
                if (dataPosition >= 0 && dataPosition < mAdapter.getCount()
                        && getChildAt(childPosition) != null) {
                    mAdapter.getView(dataPosition, getChildAt(childPosition), this);
                }
            }
        }
    }

}
