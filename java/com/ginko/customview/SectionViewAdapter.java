package com.ginko.customview;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public abstract class SectionViewAdapter<K, L> extends BaseAdapter {

	private LinkedHashMap<K, List<L>> datas = new LinkedHashMap<K, List<L>>();

	@Override
	public int getCount() {
		int total = 0;
		for (Iterator<Entry<K, List<L>>> iterator = datas.entrySet().iterator(); iterator
				.hasNext();) {
			Entry<K, List<L>> entry = iterator.next();
			total++;
			total += entry.getValue().size();

		}

		return total;
	}

	@Override
	public Object getItem(int position) {
		int flag = 0;
		for (Iterator<Entry<K, List<L>>> iterator = datas.entrySet().iterator(); iterator
				.hasNext();) {
			Entry<K, List<L>> entry = iterator.next();

			if (flag == position) {
				return entry.getKey();
			}
			flag++;
			if ((flag + entry.getValue().size()) > position) {
				return entry.getValue().get(position - flag);
			}
			flag += entry.getValue().size();
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return Long.valueOf(position).longValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Object item = getItem(position);

		if (this.isSection(item)) {
			return getSectionView((K) item, parent);
		}

		return this.getItemView((L) item, parent);
	}

	public boolean isSection(Object item) {
		if (datas.size() == 0) {
			return false;
		}
		for (Iterator<Entry<K, List<L>>> iterator = datas.entrySet().iterator(); iterator
				.hasNext();) {
			Entry<K, List<L>> entry = iterator.next();
			return entry.getKey().getClass() == item.getClass();
		}
		return false;
	}

	protected abstract View getItemView(L item, ViewGroup parent);

	protected abstract View getSectionView(K section, ViewGroup parent);

	public void clear() {
		datas.clear();
	}

	public void clear(K header) {
		datas.remove(header);
	}

	public void addItem(K header, L item) {
		List<L> list = datas.get(header);
		if (list == null) {
			list = new ArrayList<L>();
			datas.put(header, list);
		}
		list.add(item);
	}

	public void addItems(K header, List<L> items) {
		List<L> list = datas.get(header);
		if (list == null) {
			list = new ArrayList<L>();
			datas.put(header, list);
		}

		list.addAll(items);
	}

	public void removeItems(K header) {
		List<L> list = datas.get(header);
		if (list != null) {
			datas.get(header).clear();
		}
	}

    public void clearAllData()
    {
       if(datas == null)
       {
           datas = new LinkedHashMap<K, List<L>>();
       }
       try {
           datas.clear();
       }catch (Exception e)
       {
           e.printStackTrace();
       }
    }


	public int getSectionPosition(K header){
		int postion = 0;
		for (Iterator<Entry<K, List<L>>> iterator = datas.entrySet().iterator(); iterator
				.hasNext();) {
			Entry<K, List<L>> entry = iterator.next();
			if (header.equals(entry.getKey())){
				return postion;
			}
			postion++;  //the header postion.
			postion +=entry.getValue().size();
		}
		return postion;
	}

}
