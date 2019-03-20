package com.ginko.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.im.EmoticonUtility;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.im.ImPreGetMessageCallbackListener;
import com.ginko.api.request.IMRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ChatTableModel;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.view.ext.SelectableListAdapter;
import com.ginko.vo.ImBoardMemeberVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.ImMessageVO;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ImBoardListFragment extends Fragment {
    public String searchKeyword = "";
	private ListView list;
	private SelectableListAdapter<ImBoardVO> adapter;

    private boolean isListSelectable = false;
    private ImPreGetMessageCallbackListener getRecentChatsCallbackListener;

    private boolean isUILoaded = false;

    private OnBoardListItemSelectListener onBoardListItemSelectListener = null;

    //-------------------------------------//
    //Delete all message contents.
    private Object syncMessagesObj = new Object();
    private ChatTableModel chatTableModel;
    private int board_ID = 0;

    public BaseAdapter getListViewAdapter()
    {
        return this.adapter;
    }

    public void setOnImPreGetMessageCallbackListener(ImPreGetMessageCallbackListener listener)
    {
        this.getRecentChatsCallbackListener = listener;
    }

    public void setOnBoardListItemSelectListener(OnBoardListItemSelectListener onBoardListItemSelectListener)
    {
        this.onBoardListItemSelectListener = onBoardListItemSelectListener;
    }

    public void setIsListSelectable(boolean _isListSelectable)
    {
        this.isListSelectable = _isListSelectable;
        if(adapter == null) return;

        if(isListSelectable == false)
            adapter.clearSelection();
        adapter.notifyDataSetChanged();
    }

    public ImBoardListFragment(){}

	// private View view;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        chatTableModel = MyApp.getInstance().getChatDBModel();

		list = new ListView(this.getActivity());

        list.setDivider(null);

		adapter = new SelectableListAdapter<ImBoardVO>(this.getActivity()) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
                ItemView view = null;
                ImBoardVO item = getItem(position);
                if (convertView == null) {
                    view = new ItemView(getActivity() , item);
                }
                else
                {
                    view = (ItemView)convertView;
                }

                //check box
                ImageView imgCheck = (ImageView) view.findViewById(R.id.imageSelectionCheck);
                if(isListSelectable)
                    imgCheck.setVisibility(View.VISIBLE);
                else
                    imgCheck.setVisibility(View.GONE);

                if(isSelected(position))
                    imgCheck.setImageResource(R.drawable.chatmessage_selected);
                else
                    imgCheck.setImageResource(R.drawable.chatmessage_nonsel);

                if(isItemVisible(position))
                {
                    view.findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
                }
                else
                {
                    view.findViewById(R.id.rootLayout).setVisibility(View.GONE);
                }
                view.setItem(item);
                view.refreshView();
                return view;
			}
		};

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(isListSelectable)
                {
                    adapter.tiggel(position);
                    if(onBoardListItemSelectListener != null)
                        onBoardListItemSelectListener.onBoardListItemSelected(position , getSelectedItemsCount());
                    adapter.notifyDataSetChanged();
                }
                else {
                    ImBoardVO board = adapter.getItem(position);

                    Intent intent = new Intent(getActivity(), ImBoardActivity.class);
                    intent.putExtra("board_id", board.getBoardId());
                    if (board.getBoardName() != null && !board.getBoardName().equals(""))
                        intent.putExtra("groupname", board.getBoardName());
                    else
                        intent.putExtra("groupname", "");

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("board", board);
                    intent.putExtras(bundle);

                    startActivity(intent);
                }
			}
		});

		list.setAdapter(adapter);
        isUILoaded = true;
        this.loadBoardList();
		return list;
	}



	private void selectBoard(View view, int position) {
		boolean selected = adapter.isSelected(position);

		boolean newState = !selected;
		if (newState) {
			view.setBackgroundColor(getResources().getColor(R.color.item_selected_in_list));
		} else {
			view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		}
		adapter.tiggel(position);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
	}

    @Override
    public void onResume() {
        super.onResume();
        isUILoaded = true;
        this.loadBoardList();
    }

    @Override
    public void onPause() {
        super.onPause();
        isUILoaded = false;
    }

    public void loadBoardList() {
        if(!isUILoaded) return;
		IMRequest.listBoards(1, new ResponseCallBack<List<ImBoardVO>>() {
            @Override
            public void onCompleted(JsonResponse<List<ImBoardVO>> response) {
                if (response.isSuccess()) {
                    List<ImBoardVO> boards = response.getData();
                    adapter.clear();
                    adapter.addAll(boards);
                    if (getRecentChatsCallbackListener != null) {
                        getRecentChatsCallbackListener.onGetRecentChats(adapter.getCount(), false);
                    }

                    if (searchKeyword.compareTo("") != 0)
                        searchItems(searchKeyword);
                    adapter.notifyDataSetChanged();

                    if (getActivity() != null) {
                        ImageView btnEdit = (ImageView) getActivity().findViewById(R.id.btnEdit);
                        if (isListSelectable)
                            btnEdit.setImageResource(R.drawable.done_contact);
                        else {
                            if (adapter.getCount() > 0)
                                btnEdit.setImageResource(R.drawable.editcontact);
                            else
                                btnEdit.setImageResource(R.drawable.editcontact_disable);
                        }
                    }
                }
            }
        });
	}

	public void deleteBoards() {
		final List<ImBoardVO> selectedItems = adapter.getSelectedItems();
		
		String board_ids= "";
		for (ImBoardVO imBoardVO : selectedItems) {
			board_ids += imBoardVO.getBoardId()+",";
            //Add by lee for chat content history GAD-1115
            board_ID = imBoardVO.getBoardId();
            synchronized (syncMessagesObj)
            {
                if(chatTableModel == null)
                    chatTableModel = MyApp.getInstance().getChatDBModel();
                chatTableModel.deleteWholeBoardMessage(board_ID);
            }

        }
        IMRequest.leaveGroups(board_ids, new ResponseCallBack<Void>() {

            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    for (ImBoardVO imBoardVO : selectedItems) {
                        adapter.remove(imBoardVO);
                    }
                    adapter.clearSelection();
                    adapter.notifyDataSetChanged();
                }

            }
        }, true);
		
	}

    public int getSelectedItemsCount()
    {
        int count = 0;
        if(adapter != null)
            count = adapter.getSelectedItemCount();
        return count;
    }
    public void deleteAllImBoards() {
        //final List<ImBoardVO> selectedItems = adapter.getListItems();
        adapter.selectAll();
        final List<ImBoardVO> selectedItems = adapter.getSelectedItems();

        String board_ids= "";
        for (ImBoardVO imBoardVO : selectedItems) {
            board_ids += imBoardVO.getBoardId()+",";
            //Add by lee for delete chat content all history GAD-1115
            board_ID = imBoardVO.getBoardId();
            synchronized (syncMessagesObj)
            {
                if(chatTableModel == null)
                    chatTableModel = MyApp.getInstance().getChatDBModel();
                chatTableModel.deleteWholeBoardMessage(board_ID);
            }

        }
        IMRequest.leaveGroups(board_ids, new ResponseCallBack<Void>() {

            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()){
                    for (ImBoardVO imBoardVO : selectedItems) {
                        adapter.remove(imBoardVO);
                    }
                    adapter.clearSelection();
                    adapter.notifyDataSetChanged();
                }

            }
        } , true);

    }

    public boolean noChatHistory()
    {
        if (adapter != null && adapter.getVisibleCount() > 0)
            return false;
        return true;
    }

    public void searchItems(String searchKeyword)
    {
        if(adapter != null)
        {
            if(searchKeyword.compareTo("") == 0)
                adapter.showAllItems();
            else
            {
                List<ImBoardVO> boards =  adapter.getAll();
                for(int i=0;i<boards.size();i++)
                {
                    ImBoardVO board = boards.get(i);
                    String title = "";
                    String latestMsg = "";
                    String strProfileImage = "";

                    if (board.getMembers() == null || board.getMembers().size() == 0)
                    {
                        if (board.isGroup())
                            title = board.getBoardName();
                    } else
                    {
                        for (int j = 0; j < board.getMembers().size(); j++) {
                            ImBoardMemeberVO member = board.getMembers().get(j);
                            if(member.getUser().getUserId() == RuntimeContext.getUser().getUserId()){
                                continue;
                            }
                            strProfileImage = member.getUser().getPhotoUrl();
                            if (i>0 && title.compareTo("")!=0){
                                title +=" , ";
                            }
                            title +=member.getUser().getFullName();
                        }
                    }


                    int msgType = 0;

                    if (board.getRecentMessages()!= null && board.getRecentMessages().size()>0){
                        ImMessageVO lastMsgVo = board.getRecentMessages().get(0);
                        msgType = lastMsgVo.getMessageType();
                        switch(msgType)
                        {
                            case ImMessageVO.MSG_TYPE_TEXT:
                                latestMsg = lastMsgVo.getContent();
                                break;
                            case ImMessageVO.MSG_TYPE_PHOTO:
                                latestMsg = "photo";
                                break;
                            case ImMessageVO.MSG_TYPE_VOICE:
                                latestMsg = "voice";
                                break;
                            case ImMessageVO.MSG_TYPE_VIDEO:
                                latestMsg = "video";
                                break;
                            case ImMessageVO.MSG_TYPE_LOCATION:
                                latestMsg = "location";
                                break;
                            case ImMessageVO.MSG_TYPE_VIDEOCALL:
                                latestMsg = "videoCall";
                                break;
                            case ImMessageVO.MSG_TYPE_AUDIOCALL:
                                latestMsg = "audioCall";
                                break;
                        }
                    }

                    if(title.toLowerCase().contains(searchKeyword) || (msgType==ImMessageVO.MSG_TYPE_TEXT && latestMsg.toLowerCase().contains(searchKeyword)))
                    {

                        adapter.showItem(i, true);
                    }
                    else if(title.toLowerCase().contains(searchKeyword) || (msgType!=ImMessageVO.MSG_TYPE_TEXT && latestMsg.equalsIgnoreCase(searchKeyword)))
                    {
                        adapter.showItem(i , true);
                    }
                    else{
                        adapter.showItem(i , false);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private class ItemView extends LinearLayout {
        private LayoutInflater inflater = null;
        private RelativeLayout contentLayout;
        private ImBoardVO board;
        private ImageView imageSelectionCheck;
        private NetworkImageView profileImage;
        private TextView tvTitle , tvLatestMsg , tvLatestTime;
        private ImageView imgInOutMsgStatus;
        private String realType = "";
        private int endType = 1;


        private Context mContext;

        private List<String> phones = null;

        private EmoticonUtility emoticons;

        private com.android.volley.toolbox.ImageLoader imgLoader;

        public ItemView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            this.mContext = context;

        }
        public ItemView(Context context,  ImBoardVO _item)
        {
            super(context);
            this.mContext = context;
            board  = _item;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.subview_im_board, this, true);

            contentLayout = (RelativeLayout)findViewById(R.id.contentLayout);
            profileImage = (NetworkImageView)findViewById(R.id.profileImage);
            imageSelectionCheck = (ImageView)findViewById(R.id.imageSelectionCheck);
            tvTitle = (TextView) findViewById(R.id.board_title);
            tvLatestMsg = (TextView) findViewById(R.id.latest_msg);
            tvLatestTime = (TextView)findViewById(R.id.txtLastTime);
            imgInOutMsgStatus = (ImageView)findViewById(R.id.imgInOutMsg);

        }
        public void setItem(ImBoardVO _item)
        {
            this.board = _item;

        }
        private void parseContent(String content)
        {
            try {
                JSONObject json = new JSONObject(content);
                realType = json.getString("msgType");
                endType = json.getInt("endType");
            } catch (JSONException e)
            {

            }
        }

        private boolean getMemberFromMsg(ImBoardMemeberVO member, List<ImMessageVO> messageList)
        {
            int lastMsgId = -1;
            for (int i=messageList.size()-1; i>=0; i--)
            {
                ImMessageVO thisMsg = messageList.get(i);
                if (thisMsg.from == RuntimeContext.getUser().getUserId())
                    continue;;
                lastMsgId = thisMsg.from;
                break;
            }

            if (Integer.valueOf(lastMsgId).equals(member.getUser().getUserId()))
                return true;

            return false;
        }

        private void refreshView()
        {
            if(imgLoader == null)
                imgLoader = MyApp.getInstance().getImageLoader();

            if(this.emoticons == null)
                this.emoticons = MyApp.getInstance().getEmoticonUtility();

            List<ImBoardMemeberVO> memberArry = new ArrayList<ImBoardMemeberVO>();
            memberArry = this.board.getMembers();
            try {
                Collections.sort(memberArry, memberArryComparator);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String title = "";
            String latestMsg = "";
            String strProfileImage = "";
            int chatMemberCnt = -1;

            if (this.board.isGroup())
            {
                title = board.getBoardName();
                strProfileImage = board.getProfileImage();
            } else
            {
                for (int i = 0; i < memberArry.size(); i++) {
                    ImBoardMemeberVO member = memberArry.get(i);
                    if (RuntimeContext.isLoginUser(member.getUser().getUserId())) {
                        continue;
                    }

                    if (title.equals(""))
                    {
                        title = member.getUser().getFullName();
                        strProfileImage = member.getUser().getPhotoUrl();
                    }

                    chatMemberCnt++;
                }
            }

            if(chatMemberCnt > 0)
                title = title + "+" + chatMemberCnt;
            int endType = 1;
            boolean isOutgoing = false;

            if (board.getRecentMessages()!= null && board.getRecentMessages().size()>0){
                ImMessageVO lastMsgVo = board.getRecentMessages().get(0);
                if(chatMemberCnt > 0 && !board.isGroup()) {
                    for (int i = 0; i < memberArry.size(); i++) {
                        ImBoardMemeberVO member = memberArry.get(i);
                        boolean isUseful = getMemberFromMsg(member, board.getRecentMessages());

                        if (isUseful)
                        {
                            title = member.getUser().getFullName();
                            if (chatMemberCnt > 0)
                                title = title + "+" + chatMemberCnt;
                            strProfileImage = member.getUser().getPhotoUrl();
                            break;
                        }
                    }
                }

                int msgType = lastMsgVo.getMessageType();
                String content = lastMsgVo.getContent();
                parseContent(content);

                if (realType != null && realType.equals("videoCall"))
                    msgType = 5;
                else if (realType != null && realType.equals("audioCall"))
                    msgType = 6;

                switch(msgType)
                {
                    case ImMessageVO.MSG_TYPE_TEXT:
                        latestMsg = lastMsgVo.getContent();
                        break;
                    case ImMessageVO.MSG_TYPE_PHOTO:
                        latestMsg = "#Photo";
                        break;
                    case ImMessageVO.MSG_TYPE_VOICE:
                        latestMsg = "#Voice";
                        break;
                    case ImMessageVO.MSG_TYPE_VIDEO:
                        latestMsg = "#Video";
                        break;
                    case ImMessageVO.MSG_TYPE_LOCATION:
                        latestMsg = "#Location";
                        break;
                    case ImMessageVO.MSG_TYPE_VIDEOCALL:
                        latestMsg = "#Video Call";
                        break;
                    case ImMessageVO.MSG_TYPE_AUDIOCALL:
                        latestMsg = "#Audio Call";
                        break;
                }
                if(!lastMsgVo.isRead())
                {
                    contentLayout.setBackgroundColor(0xffdfd1ed);
                } else
                {
                    contentLayout.setBackgroundColor(Color.TRANSPARENT);
                }
                isOutgoing = RuntimeContext.isLoginUser(lastMsgVo.getFrom());
                //TODO show  a icon for incoming or outgoing.
                if(isOutgoing)
                    imgInOutMsgStatus.setImageResource(R.drawable.arrow_right);
                else
                    imgInOutMsgStatus.setImageResource(R.drawable.arrow_left);
            }

            Calendar today = Calendar.getInstance();
            Calendar lastMsgTime = Calendar.getInstance(); lastMsgTime.setTime(board.getLastActiveTime());
            //if today
            if(today.get(Calendar.YEAR) == lastMsgTime.get(Calendar.YEAR) &&
               today.get(Calendar.MONTH) == lastMsgTime.get(Calendar.MONTH) &&
               today.get(Calendar.DAY_OF_MONTH) == lastMsgTime.get(Calendar.DAY_OF_MONTH))
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
                tvLatestTime.setText(dateFormat.format(lastMsgTime.getTime()));
            }
            else
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                tvLatestTime.setText(dateFormat.format(lastMsgTime.getTime()));
            }

            profileImage.setDefaultImageResId(R.drawable.no_face);
            if(chatMemberCnt > 0)
                profileImage.setDefaultImageResId(R.drawable.group_chat_img);
            else
                profileImage.setImageUrl(strProfileImage, imgLoader);

            tvLatestMsg.setText(this.emoticons.addSmileySpans(latestMsg));
            if (latestMsg.equals("#Video Call"))
                tvLatestMsg.setText(this.emoticons.getEmoticonCode(21) + " " + this.emoticons.addSmileySpans(latestMsg));
            else if (latestMsg.equals("#Audio Call"))
                tvLatestMsg.setText(this.emoticons.getEmoticonCode(32) + " " + this.emoticons.addSmileySpans(latestMsg));
            else
                tvLatestMsg.setText(this.emoticons.addSmileySpans(latestMsg));

            tvLatestMsg.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            if (board.getBoardName() == null || board.getBoardName().equals(""))
                board.setBoardName(title);
            tvTitle.setText(title);
        }
    }

    private final static Comparator<ImBoardMemeberVO> memberArryComparator = new Comparator<ImBoardMemeberVO>()
    {

        @Override
        public int compare(ImBoardMemeberVO lhs, ImBoardMemeberVO rhs) {
            int result = 0;
            String leftName = null , rightName = null;

            leftName = lhs.getUser().getFullName().toLowerCase();
            rightName = rhs.getUser().getFullName().toLowerCase();

            char leftFirstLetter = leftName.charAt(0);
            char rightFirstLetter = rightName.charAt(0);

            if(!((leftFirstLetter >= 'a' && leftFirstLetter <= 'z') || (leftFirstLetter >= 'A' && leftFirstLetter <= 'Z')))
                return 1;
            if(!((rightFirstLetter >= 'a' && rightFirstLetter <= 'z') || (rightFirstLetter >= 'A' && rightFirstLetter <= 'Z')))
                return -1;

            if(leftName.compareTo(rightName)<0)
                result = -1;
            else if(leftName.compareTo(rightName) == 0)
                result = 0;
            else if(leftName.compareTo(rightName)>0)
                result = 1;

            return result;
        }
    };

    public interface OnBoardListItemSelectListener
    {
        public void onBoardListItemSelected(int selectedItemPosition , int selectedItemsCount);
    }
}
