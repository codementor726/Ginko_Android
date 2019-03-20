package com.ginko.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ginko.activity.exchange.ExchangeRequestListAdapter;
import com.ginko.utils.ViewIdGenerator;

public abstract class BaseExchangeFragment extends Fragment {

	protected ExchangeRequestListAdapter adapter = null;

    protected ListView listView ;

    private   int pageIndex = 0;

    private onBadgeUpdateListener badgeNumUpdateListener = null;
    private onCheckNotesListener checkNotesListener = null;

    private boolean isLoading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listView = new ListView(this.getActivity());
        listView.setId(ViewIdGenerator.generateViewId());
        listView.setDivider(null);
        listView.setDividerHeight(0);
        return listView;
    }

    public BaseExchangeFragment(int _pageIndex) {
        this.pageIndex = _pageIndex;
    }

    public void setOnBadgeUpdateListener(onBadgeUpdateListener _listener)
    {
        badgeNumUpdateListener = _listener;
    }

    public void setOnCheckNotesUpdateListener(onCheckNotesListener _listener)
    {
        checkNotesListener = _listener;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initialList();
	}


    @Override
    public void onResume() {
        super.onResume();
        //loadData();
    }


    public void initialList() {
		if (this.getActivity() == null){
			return;
		}
		ListView list = this.listView;
		if (adapter == null) {
            adapter = new ExchangeRequestListAdapter(getActivity());
		}

		list.setAdapter(adapter);
	}

    public void setAdapter(ExchangeRequestListAdapter _adapter)
    {
        this.adapter = _adapter;
        if(this.adapter != null && this.listView!=null)
            listView.setAdapter(adapter);
    }

    public ExchangeRequestListAdapter getAdapter(){return this.adapter;}

    public void updateListView()
    {
        if(adapter != null)
            adapter.notifyDataSetChanged();
    }

    public void setIsSelectable(boolean _isSelectable)
    {
        if(adapter!=null) {
            adapter.setIsSelectable(_isSelectable);
        }
    }

    public boolean getIsSelectable()
    {
        if(adapter != null)
            return adapter.getIsSelectable();
        return false;
    }

	public void loadData() {
        if(isLoading) return;
	}

	public void filter(String query) {
        if(adapter!=null)
        {
            adapter.filterItemsByString(query);
        }
	}

    public int getSelectedItemsCount()
    {
        return adapter==null?0:adapter.getSelectedItemCount();
    }

    public void deleteSelectedItems()
    {
        if(adapter != null)
        {
            adapter.deleteSelectedItems();
            updateBadgeNum();
            try
            {
                adapter.notifyDataSetChanged();
            }catch(Exception e){e.printStackTrace();}
        }
    }

    public void updateBadgeNum()
    {
        if(badgeNumUpdateListener != null)
            badgeNumUpdateListener.onBadgeUpdated(pageIndex , adapter.getCount());
    }

    public void updateNotesData(boolean closeEditable)
    {
        if (checkNotesListener != null)
            checkNotesListener.onCheckNotesUpdated(closeEditable);
    }


    public void deleteItems()
    {}


    public interface onBadgeUpdateListener
    {
        public void onBadgeUpdated(int pageIndex , int badgeCount);
    }

    public interface onCheckNotesListener
    {
        public void onCheckNotesUpdated(boolean closeEditable);
    }
}
