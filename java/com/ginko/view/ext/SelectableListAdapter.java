package com.ginko.view.ext;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SelectableListAdapter<T> extends BaseAdapter {
	private List<T> items;

	private ItemSelectedListener<T> selectedListener;
	private Set<Integer> selectedItems = new HashSet<Integer>();
    private Set<Integer> hiddenItems = new HashSet<Integer>();
	private Context context;

	public SelectableListAdapter(Context context, List<T> items) {
		this.context = context;
		this.items = items;
	}

	public SelectableListAdapter(Context context) {
		this.context = context;
		items = new ArrayList<T>();
	}

	public void select(int position) {
		this.selectedItems.add(position);
	}

	public void unSelect(int position) {
		this.selectedItems.remove(position);
	}

	public void clearSelection() {
		this.selectedItems.clear();
	}

	public boolean isSelected(int position) {
		return this.selectedItems.contains(position);
	}

    public void showItem(int position , boolean visibility){
        if(visibility)
        {
            if(hiddenItems.contains(position))
                this.hiddenItems.remove(position);
        }
        else {
            this.hiddenItems.add(position);
        }
    }

    public void showAllItems(){
        if(this.hiddenItems != null) {
            try {
                this.hiddenItems.clear();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            this.hiddenItems = new HashSet<Integer>();
        }
    }

    public void showOnlySelectedItems()
    {
        try {
            this.hiddenItems.clear();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        for(int i=0;i<items.size();i++)
        {
            if(!isSelected(i))
                this.hiddenItems.add(i);
        }
        notifyDataSetChanged();
    }

    public boolean isItemVisible(int position){return this.hiddenItems.contains(position)?false:true;}

	public List<T> getSelectedItems() {
		List<T> result = new ArrayList<T>();
		for (Integer position : selectedItems) {
            result.add((T) getItem(position));
		}
		return result;
	}

    public List<T> getSelectedVisibleItems() {
        List<T> result = new ArrayList<T>();
        for (Integer position : selectedItems) {
            if (isItemVisible(position))
                result.add((T) getItem(position));
        }
        return result;
    }

    public List<T> getVisibleItems() {
        List<T> result = new ArrayList<T>();
        for (int i=0; i<items.size(); i++) {
            if (isItemVisible(i))
                result.add((T) getItem(i));
        }
        return result;
    }

    public int getSelectedItemCount() {
        return selectedItems == null?0:selectedItems.size();
    }

    public int getSelectedVisibleItemCount() {
        int totalCnt = 0;
        for(int i=0;i<items.size();i++)
        {
            if(isItemVisible(i) && isSelected(i)) totalCnt++;
        }

        return totalCnt;
    }

	public void tiggel(int position) {
		if (this.isSelected(position)) {
			this.unSelect(position);
		} else {
			this.select(position);
		}
		if (selectedListener != null) {
			selectedListener.onSelectedStausChanged(getItem(position),this.isSelected(position));
		}
	}

	@Override
	public int getCount() {
		return items==null?0:items.size();
	}

    public int getVisibleCount() {
        int totalCnt = 0;
        for(int i=0;i<items.size();i++)
        {
            if(isItemVisible(i)) totalCnt++;
        }

        return totalCnt;
    }
    public List<T> getListItems(){return items;}

	@Override
	public T getItem(int position) {
        if(items==null) return null;
        if(items.size() < position+1)
            return null;
		return this.items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addToHead(List<T> newItems) {
		if (newItems == null || newItems.size() == 0) {
			return;
		}
		for (int i = newItems.size() - 1; i >= 0; i--) {
			this.items.add(0, newItems.get(i));
		}
	}

	public synchronized void addAll(List<T> newItems) {
		if (newItems == null || newItems.size() == 0) {
			return;
		}
		for (int i = 0; i < newItems.size(); i++) {
			add(newItems.get(i));
		}
	}

    public List<T> getAll()
    {
        return items;
    }


	public void clear() {
        try {
            items.clear();
            selectedItems.clear();
            hiddenItems.clear();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        finally {
            items = new ArrayList<T>();
            selectedItems = new HashSet<Integer>();
            hiddenItems = new HashSet<Integer>();
        }

	}

    public void clearAdapter(){
        items = new ArrayList<T>();
        unSelectAll();
        showAllItems();
    }

	public void add(T item) {
		if (items.contains(item)) {
			return;
		}
		this.items.add(item);
	}

	public void remove(T t) {
		this.items.remove(t);
	}

    public void removeItem(int position){this.items.remove(position);}

	public ItemSelectedListener<T> getSelectedListener() {
		return selectedListener;
	}

	public void setSelectedListener(ItemSelectedListener<T> selectedListener) {
		this.selectedListener = selectedListener;
	}

    public synchronized void selectAll() {
        if(selectedItems == null)
            selectedItems = new HashSet<Integer>();
        selectedItems.clear();
        for (int i = 0; i < items.size() ; i++) {
            if (isItemVisible(i)) this.selectedItems.add(i);
        }
    }

    public synchronized void unSelectAll()
    {
        if(selectedItems!=null)
        {
            for (int i = 0; i < items.size() ; i++) {
                if (isItemVisible(i)) this.selectedItems.remove(i);
            }
        }
        else
        {
            selectedItems = new HashSet<Integer>();
        }
    }

    public synchronized void unSelect2()
    {
        if(selectedItems!=null)
        {
            selectedItems.clear();
        }
        else
        {
            selectedItems = new HashSet<Integer>();
        }
    }


    public boolean isAllselected() {
        return this.items.size() == this.selectedItems.size();
    }

    public interface ItemSelectedListener<T> {
		void onSelectedStausChanged(T t, boolean isSelected);
	}

}