package com.ginko.customview;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ginko.ginko.R;

import java.util.List;


public class BottomPopupWindow extends PopupWindow {
    private final Context context;
    private final View view;
    private final ListView listView;

    private List<String> buttons;
    private BaseAdapter adapter;

    private OnButtonClickListener clickListener;

    public BottomPopupWindow(Context context ,List<String> buttons){

        this.context = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view = layoutInflater.inflate(R.layout.popup_bottom, null);

        super.setContentView(view);
        //Set the width of the pop window
        this.setWidth(LayoutParams.MATCH_PARENT);
        //Set the height of the pop window
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //Set if the pop window can be clicked.
        this.setFocusable(true);
        //set Anim of the window.
        this.setAnimationStyle(R.style.AnimBottom);
//        ColorDrawable dw = new ColorDrawable(0xb0000000);
        ColorDrawable dw = new ColorDrawable(0x000000);
        //Set the window's background.
        this.setBackgroundDrawable(dw);

        listView = (ListView) view.findViewById(R.id.listView);

        adapter = new GroupAdapter(context,buttons);
        listView.setAdapter(adapter);

        view.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = view.findViewById(R.id.listView).getTop();
                int y=(int) event.getY();
                if(event.getAction()==MotionEvent.ACTION_UP){
                    if(y<height){
                        dismiss();
                    }
                }
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                if (clickListener!=null){
                    clickListener.onClick(view,position);
                }
                dismiss();
            }
        });

        this.setFocusable(true);

        // Set when click outside of the window, the window disappear.
        this.setOutsideTouchable(true);
        // when click Back button, the window should disappear.
//        this.setBackgroundDrawable(new BitmapDrawable());
        this.setAnimationStyle(R.style.AnimBottom);

    }

    public void show(View parent){
//        this.showAsDropDown(parent, 0, 5);
        this.showAtLocation(parent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    public void setClickListener(OnButtonClickListener clickListener) {
        this.clickListener = clickListener;
    }


    public static interface OnButtonClickListener{
       void onClick(View button, int position);
    }



     class GroupAdapter extends BaseAdapter {

        private Context context;

        private List<String> list;

        public GroupAdapter(Context context, List<String> list) {

            this.context = context;
            this.list = list;

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {

            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {


            ViewHolder holder;
            if (convertView==null) {
                convertView=LayoutInflater.from(context).inflate(R.layout.list_item_bottom_popup, null);
                holder=new ViewHolder();
                convertView.setTag(holder);
                holder.groupItem=(TextView) convertView.findViewById(R.id.button);

            }
            else{
                holder=(ViewHolder) convertView.getTag();
            }
            holder.groupItem.setText(list.get(position));

            return convertView;
        }

          class ViewHolder {
              TextView groupItem;
        }

    }

}
