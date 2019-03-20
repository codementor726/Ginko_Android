package com.ginko.activity.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.contact.ContactItem;
import com.ginko.api.request.ContactGroupRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.ImBoardMemeberVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.ImContactVO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupChatMembersActivity extends MyBaseActivity implements View.OnClickListener{

    /* UI Objects */
    private ImageButton btnPrev;
    private ListView listView;

    /* Variables */
    private ImBoardVO board;
    private HashMap<Integer, ImContactVO> mapList;
    private GroupChatMemberListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_members);

        Intent intent = this.getIntent();
        board = (ImBoardVO) intent.getSerializableExtra("board");

        getUIObjects();

        if(board == null)
            return;

        List<ImBoardMemeberVO> members = new ArrayList<ImBoardMemeberVO>();
        if (board.isGroup())
//            adapter.addAll(board.getMembers());
            adapter.addAll(getMembers());
        else
        {
            int myUserId = RuntimeContext.getUser().getUserId();
            for(int i=0;i<board.getMembers().size();i++)
            {
                if(board.getMembers().get(i).getUser().getUserId() != myUserId && board.getMembers().get(i).isFriend())
                    members.add(board.getMembers().get(i));
            }
            adapter.addAll(members);
        }
    }

    private List<ImBoardMemeberVO> getMembers() {
        List<ImBoardMemeberVO> members = new ArrayList<ImBoardMemeberVO>();
        ImBoardMemeberVO member;
        ContactItem mItem = null;
        String strName, strProfileImage;
        for (int i = 0; i < GroupDetailActivity.sltContactList.size(); i++) {
            mItem = GroupDetailActivity.sltContactList.get(i);
            strName = mItem.getFullName();
            strProfileImage = mItem.getProfileImage();

            member = new ImBoardMemeberVO();
            member.setName(strName);
            member.setProfileImage(strProfileImage);
            members.add(member);
        }

        return members;
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);

        listView = (ListView)findViewById(R.id.list);

        adapter = new GroupChatMemberListAdapter(this);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;
        }
    }



    class GroupChatMemberListAdapter extends BaseAdapter
    {
        private Context mContext;
        private List<ImBoardMemeberVO> memberList;

        public GroupChatMemberListAdapter(Context context)
        {
            mContext = context;
            memberList = new ArrayList<ImBoardMemeberVO>();
        }

        public void clearAdapter()
        {
            if(memberList != null)
            {
                try
                {
                    memberList.clear();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        public void addAll(List<ImBoardMemeberVO> list)
        {
            this.memberList = list;
        }

        @Override
        public int getCount() {
            return memberList==null?0:memberList.size();
        }

        @Override
        public Object getItem(int position) {
            return memberList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImBoardMemeberVO item = (ImBoardMemeberVO) getItem(position);
            if(item == null) return null;

            MemberItemView view = null;
            if (convertView == null) {

                view = new MemberItemView(mContext , item);
            }
            else
            {
                view = (MemberItemView)convertView;
            }

            view.setItem(item);
            view.refreshView();
            return view;
        }
    }

    class MemberItemView extends LinearLayout
    {
        private LayoutInflater inflater = null;
        private ImageLoader imgLoader;
        private ImBoardMemeberVO item;
        private NetworkImageView profileImage;
        private TextView txtContactItem;

        public MemberItemView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub

        }
        public MemberItemView(Context context,  ImBoardMemeberVO _item) {
            super(context);
            setItem(_item);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            inflater.inflate(R.layout.group_chat_member_item, this, true);

            profileImage = (NetworkImageView)findViewById(R.id.profileImage);
            txtContactItem = (TextView)findViewById(R.id.txtContactName);

            profileImage.setBorderColor(context.getResources().getColor(R.color.purple_contact_color));
            profileImage.setDefaultImageResId(R.drawable.no_face);


        }

        public void setItem(ImBoardMemeberVO _item)
        {
            this.item = _item;
        }

        public void refreshView()
        {
            if (imgLoader == null)
                imgLoader = MyApp.getInstance().getImageLoader();

            if (!board.isGroup()) {
                profileImage.setImageUrl(item.getUser().getPhotoUrl(), imgLoader);
                profileImage.invalidate();
            }
            else
            {
                profileImage.setImageUrl(item.getProfileImage(), imgLoader);
                profileImage.invalidate();
            }

            String strContactName = "";
            if (!board.isGroup())
            {
                strContactName = item.getUser().getFirstName();
                if(!item.getUser().getMiddleName().equals("") || item.getUser().getLastName().equals(""))
                    strContactName += "\n";
                if(!item.getUser().getLastName().equals(""))
                    strContactName += item.getUser().getMiddleName() + " " + item.getUser().getLastName();
                else
                    strContactName +=item.getUser().getMiddleName();
                strContactName = strContactName.trim();
            } else
                strContactName = item.getName();

            txtContactItem.setText(strContactName);
        }
    }

}
