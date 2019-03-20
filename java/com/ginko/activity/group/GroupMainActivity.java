package com.ginko.activity.group;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.cb.CBSelectActivity;
import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.directory.DirMailSelectActivity;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.menu.LoginSettingActivity;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.api.request.ContactGroupRequest;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.api.request.IMRequest;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.Uitils;
import com.ginko.customview.DragGridView;
import com.ginko.customview.InputDialog;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.GroupVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.UserLoginVO;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupMainActivity extends MyBaseActivity implements View.OnClickListener{
    private final int GO_TO_CONTACT = 100;
    private final int SHARE_YOUR_LEAF_ACTIVITY = 2;
    private final int VALIDATE_LINK_ACTIVITY = 3;

    private ImageButton btnPrev ;
    private ImageView btnAddGroup;
    private ImageView btnEdit, btnSort, btnEditClose, btnJoinDirectory;
    private TextView txtNoneGroup;
    //private ListView list = null;
    private DragGridView list = null;

    private BaseAdapter adapter;
    private List<GroupVO> groups = new ArrayList<GroupVO>();
    private List<GroupVO> groupsTemp = null;

    private boolean isEditable = false;
    private boolean isAddShow = false;
    private boolean isJoinShow = false;

    private DialogInterface m_dialog;
    private InputDialog addNewGroupDialog;
    private InputDialog joinNewDirectoryDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_main);

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnAddGroup = (ImageView)findViewById(R.id.btnAddGroup); btnAddGroup.setOnClickListener(this);
        btnEdit =(ImageView)findViewById(R.id.imgBtnEditGroup); btnEdit.setImageResource(R.drawable.btnedit_group_disable); btnEdit.setOnClickListener(this);
        btnSort =(ImageView)findViewById(R.id.imgBtnSort); btnSort.setImageResource(R.drawable.group_sort_disable); btnSort.setOnClickListener(this);
        btnJoinDirectory = (ImageView)findViewById(R.id.imgBtnJoinDirectory); btnJoinDirectory.setOnClickListener(this);
        btnEditClose = (ImageView)findViewById(R.id.btnCloseEditGroup); btnEditClose.setOnClickListener(this);

        txtNoneGroup = (TextView)findViewById(R.id.txtNoneGroup);


        /*list = (ListView) findViewById(R.id.list);*/
        list = (DragGridView) findViewById(R.id.list);
        adapter = new ArrayAdapter<GroupVO>(this,R.layout.list_item_group, groups){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                ImageLoader imgLoader = null;

                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.list_item_group, parent, false);
                }

                if(imgLoader == null)
                    imgLoader = MyApp.getInstance().getImageLoader();

                TextView tvGroupame = (TextView) view.findViewById(R.id.groupName);
                TextView tvContacts = (TextView) view.findViewById(R.id.txtGroupContacts);

                NetworkImageView imgPhoto = (NetworkImageView)view.findViewById(R.id.photo);
                ImageView btnDelete = (ImageView)view.findViewById(R.id.imgDelete);
                if(isEditable)
                    btnDelete.setVisibility(View.VISIBLE);
                else
                    btnDelete.setVisibility(View.INVISIBLE);

                final GroupVO group = getItem(position);

                if (group.getType() == 0) {
                    imgPhoto.setBorderColor(getResources().getColor(R.color.purple_contact_color));
                    imgPhoto.setDefaultImageResId(R.drawable.no_face);
                    imgPhoto.setImageUrl(group.getProfile_image(), imgLoader);

                    tvContacts.setBackgroundResource(R.drawable.grey_number_badge_bg);
                    tvContacts.setTextColor(Color.parseColor("#ffffff"));
                }
                else if (group.getType() == 1){
                    imgPhoto.setBorderColor(getResources().getColor(R.color.purple_contact_color));
                    imgPhoto.setDefaultImageResId(R.drawable.no_face);
                    imgPhoto.setImageUrl(group.getProfile_image(), imgLoader);

                    tvContacts.setBackgroundResource(R.drawable.purple_number_badge_bg);
                    tvContacts.setTextColor(Color.parseColor("#ffffff"));
                }
                else if (group.getType() == 2){
                    imgPhoto.setBorderColor(getResources().getColor(R.color.green_top_titlebar_color));
                    imgPhoto.setDefaultImageResId(R.drawable.no_face);
                    imgPhoto.setImageUrl(group.getProfile_image(), imgLoader);

                    tvContacts.setBackgroundResource(R.drawable.green_number_badge_bg);
                    tvContacts.setTextColor(Color.parseColor("#aa000000"));
                }
                tvGroupame.setText(group.getName());
                tvContacts.setText("" + group.getUserCount());
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDeleteItems = new AlertDialog.Builder(GroupMainActivity.this);
                        if (group.getType() < 2)
                            alertDeleteItems.setMessage(getResources().getString(R.string.str_delete_group_confirm_dialog));
                        else
                            alertDeleteItems.setMessage(getResources().getString(R.string.str_delete_directory_confirm_dialog));

                        alertDeleteItems.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                // TODO Auto-generated method stub
                                if (group.getType() < 2)
                                {
                                    ContactGroupRequest.delete(group.getGroup_id(), new ResponseCallBack<Void>() {
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if (response.isSuccess()) {
                                                loadGroups();
                                            }
                                        }
                                    });
                                } else
                                {
                                    DirectoryRequest.quitMember(group.getGroup_id(), new ResponseCallBack<Void>() {
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if (response.isSuccess()) loadGroups();
                                        }
                                    }, true);
                                }
                            }
                        });
                        alertDeleteItems.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int paramInt) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                            }
                        });
                        alertDeleteItems.setCancelable(false);
                        alertDeleteItems.show();
                    }
                });
                return view;

            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // go to the group list detail view.
                if(isEditable) return;
                GroupVO group = groups.get(position);

                Intent intent = new Intent(GroupMainActivity.this, GroupDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("group", group);
                intent.putExtras(bundle);
                startActivityForResult(intent, GO_TO_CONTACT);
            }
        });
        list.setOnChangeListener(new DragGridView.OnChanageListener() {
            @Override
            public void onChange(int from, int to) {
                GroupVO temp = groups.get(from);
                GroupVO last = groups.get(to);

                temp.setPosition(last.getPosition());
                last.setPosition(temp.getPosition());

                if(from < to){
                    ContactGroupRequest.setChangeGroupOrder(temp.getId(), from, to, new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            //loadGroups();
                        }
                    });
                    for(int i=from; i<to; i++){
                        Collections.swap(groups, i, i + 1);
                        /*ContactGroupRequest.setChangeGroupOrder(groups.get(i+1).getId(), i+1, i, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                            }
                        });*/
                    }
                }else if(from > to){
                    ContactGroupRequest.setChangeGroupOrder(temp.getId(), from, to, new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            //loadGroups();
                        }
                    });
                    for(int i=from; i>to; i--){
                        Collections.swap(groups, i, i - 1);

                        /*ContactGroupRequest.setChangeGroupOrder(groups.get(i-1).getId(), (i-1), i, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                            }
                        });*/
                    }
                }
                //groups.set(to, temp);

               /* ContactGroupRequest.setAllChangeGroupOrder(sessionId, new ResponseCallBack<JSONObject>(){
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        Log.d("panda","result==="+response.getErrorMessage());
                    }
                });*/
                MyApp.getInstance().isSortableName = false;
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isAddShow) {
            addNewGroupDialog.showKeyboard();
        }

        if (isJoinShow) {
            joinNewDirectoryDialog.showKeyboard();
        }

        loadGroups();
    }

    private void loadGroups() {
        ContactGroupRequest.listGroups(new ResponseCallBack<List<GroupVO>>() {
            @Override
            public void onCompleted(JsonResponse<List<GroupVO>> response) {
                if (response.isSuccess()) {
                    groups.clear();
                    groups.addAll(response.getData());
                    try {
                        Collections.sort(groups, groupComparator);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (groups.size() == 0) {
                        isEditable = false;
                        updateTopButtons();
                    }

                    for (int i = 0; i < groups.size(); i++) {
                        GroupVO member = groups.get(i);
                        member.setPosition(i);
                    }

                    groupsTemp = new ArrayList<GroupVO>(groups);
                }
                updateListView();
            }
        });

    }

    private void updateListView()
    {
        if(groups.size()>0) {
            txtNoneGroup.setVisibility(View.GONE);
            list.setBackgroundColor(Color.WHITE);
            btnEdit.setImageResource(R.drawable.btnedit_group);
            btnEdit.setEnabled(true);

            btnSort.setImageResource(R.drawable.group_sort);
            btnSort.setEnabled(true);
        }
        else {
            txtNoneGroup.setVisibility(View.VISIBLE);
            list.setBackgroundDrawable(null);
            btnEdit.setImageResource(R.drawable.btnedit_group_disable);
            btnEdit.setEnabled(false);

            btnSort.setImageResource(R.drawable.group_sort_disable);
            btnSort.setEnabled(false);
        }

        if(adapter != null)
            adapter.notifyDataSetChanged();
    }

    private void addNewGroup() {

        isAddShow = true;

        addNewGroupDialog = new InputDialog(this,
                InputType.TYPE_CLASS_TEXT,
                "Create a new Group" , //title
                "Enter Group Name", //Hint
                false , //show titlebar
                getResources().getString(R.string.str_cancel) , //left button name
                new InputDialog.OnButtonClickListener(){
                    @Override
                    public boolean onClick(Dialog dialog , View v, String input) {
                        isAddShow = false;
                        RepeatSafeToast.cancel();
                        return true;
                    }//left button clicklistener
                },
                getResources().getString(R.string.str_add), //right button name
                new InputDialog.OnButtonClickListener() //right button clicklistener
                {
                    @Override
                    public boolean onClick(Dialog dialog , View v , String groupName) {
                        if(groupName.trim().equalsIgnoreCase(""))
                        {
                            RepeatSafeToast.show(GroupMainActivity.this, "Please enter group name.");
                            return false;
                        }

                        RepeatSafeToast.cancel();
                        isAddShow = false;
                        ContactGroupRequest.add(groupName.trim(), new ResponseCallBack<GroupVO>() {
                            @Override
                            public void onCompleted(JsonResponse<GroupVO> response) {
                                if (response.isSuccess()) {
                                    GroupVO group = response.getData();
                                    group.setGroup_id(group.getId());
                                    group.setPosition(groups.size());
                                    groups.add(response.getData());
                                    Intent intent = new Intent(GroupMainActivity.this, GroupDetailActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("group", group);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }
                                else {
                                    if(response.getErrorCode() == 650)
                                        MyApp.getInstance().showSimpleAlertDiloag(GroupMainActivity.this, "The Group already existed.", null);
                                    else
                                        MyApp.getInstance().showSimpleAlertDiloag(GroupMainActivity.this, response.getErrorMessage(), null);
                                }
                                updateListView();
                            }
                        });

                        return true;
                    }
                },
                new InputDialog.OnEditorDoneActionListener() {

                    @Override
                    public void onEditorActionDone(Dialog dialog, String groupName) {
                        if(groupName.trim().equalsIgnoreCase(""))
                        {
                            RepeatSafeToast.show(GroupMainActivity.this, "Please enter group name.");
                            return;
                        }
                        RepeatSafeToast.cancel();
                        isAddShow = false;

                        dialog.dismiss();
                        ContactGroupRequest.add(groupName.trim(), new ResponseCallBack<GroupVO>() {
                            @Override
                            public void onCompleted(JsonResponse<GroupVO> response) {
                                if (response.isSuccess()) {
                                    GroupVO group = response.getData();
                                    group.setPosition(groups.size());
                                    groups.add(group);
                                    Intent intent = new Intent(GroupMainActivity.this, GroupDetailActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("group", group);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }else {
                                    if(response.getErrorCode() == 650)
                                        MyApp.getInstance().showSimpleAlertDiloag(GroupMainActivity.this, "The Group already existed.", null);
                                    else
                                        MyApp.getInstance().showSimpleAlertDiloag(GroupMainActivity.this, response.getErrorMessage(), null);
                                }
                                updateListView();
                            }
                        });
                    }
                }
        );
        addNewGroupDialog.show();
        addNewGroupDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                m_dialog = dialog;
                ((InputDialog)m_dialog).showKeyboard();
            }
        });

    }

    private void updateTopButtons()
    {
        if(isEditable)
        {
            btnPrev.setVisibility(View.INVISIBLE);
            btnEdit.setVisibility(View.INVISIBLE);
            btnSort.setVisibility(View.INVISIBLE);
            btnAddGroup.setVisibility(View.INVISIBLE);
            btnEditClose.setVisibility(View.VISIBLE);
            btnJoinDirectory.setVisibility(View.INVISIBLE);
        }
        else
        {
            btnPrev.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
            btnSort.setVisibility(View.VISIBLE);
            btnAddGroup.setVisibility(View.VISIBLE);
            btnEditClose.setVisibility(View.INVISIBLE);
            btnJoinDirectory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == RESULT_OK && data!=null)
        {
            switch (requestCode)
            {
                case GO_TO_CONTACT:
                    if(data.getBooleanExtra("isContactDeleted" , false))
                        finish();
                    break;
                case SHARE_YOUR_LEAF_ACTIVITY:
                    int nStatus = data.getIntExtra("status_code", 0);
                    showValuableMessage(nStatus);
                    break;
                case VALIDATE_LINK_ACTIVITY:
                    if (data.getBooleanExtra("isSuccess", false))
                        showValuableMessage(1);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        RepeatSafeToast.cancel();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                RepeatSafeToast.cancel();
                finish();

                changeAllGroupOrder();
                break;

            case R.id.btnAddGroup:
                addNewGroup();
                break;

            case R.id.btnCloseEditGroup:
                isEditable = false;
                updateTopButtons();
                adapter.notifyDataSetChanged();
                break;

            case R.id.imgBtnEditGroup:
                isEditable = true;
                updateTopButtons();
                adapter.notifyDataSetChanged();
                break;

            case R.id.imgBtnSort:
                sortGroupByName();
                break;
            case R.id.imgBtnJoinDirectory:
                JoinAndNextScreen();
                break;
        }
    }

    private void changeAllGroupOrder()
    {
        JSONArray jsonArray = new JSONArray();
        if (groupsTemp == null)
            return;

        for (int i = 0; i < groupsTemp.size() ; i ++) {
            for (int j = 0; j < groups.size() ; j ++) {
                if (groupsTemp.get(i).getOrder_weight() == groups.get(j).getOrder_weight() && i != j) {
                    JSONObject movePositionGroup = new JSONObject();
                    try {
                        movePositionGroup.put("id", groupsTemp.get(i).getId());
                        movePositionGroup.put("order_num", j);
                        movePositionGroup.put("order_old_num", i);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    jsonArray.put(movePositionGroup);
                }
            }
        }
        JSONObject allMovedGroups = new JSONObject();
        try{
            allMovedGroups.put("data", jsonArray);
        } catch (JSONException e){
            e.printStackTrace();
        }
        if (jsonArray.length() != 0) {
            ContactGroupRequest.setAllChangeGroupOrder(allMovedGroups, new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) {
                    if (response.isSuccess()) {
                        Log.w("Set for Group's Order.", "It's Success!");
                    }
                }
            });
        }

        groupsTemp.clear();
        groupsTemp.addAll(groups);
    }

    //Group order when load groups.
    private final static Comparator<GroupVO> groupComparator = new Comparator<GroupVO>()
    {
        @Override
        public int compare(GroupVO lhs, GroupVO rhs) {
            if (lhs.getOrder_weight() < rhs.getOrder_weight())
                return -1;
            else if (lhs.getOrder_weight() > rhs.getOrder_weight())
                return 1;
            else
                return 0;
        }
    };
    //Sort by name of group.
    private final static Comparator<GroupVO> groupNameComparator = new Comparator<GroupVO>()
    {
        private final Collator   collator = Collator.getInstance();
        @Override
        public int compare(GroupVO lhs, GroupVO rhs) {
            return collator.compare(lhs.getName(), rhs.getName());
        }
    };

    public void sortGroupByName(){
        try {
            Collections.sort(groups, groupNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }

        changeAllGroupOrder();
        adapter.notifyDataSetChanged();
    }

    public void showValuableMessage(int nStatus)
    {
        switch (nStatus)
        {
            case 1:
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupMainActivity.this);
                    TextView myMsg = new TextView(this);
                    myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_1));
                    myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                    builder.setView(myMsg);

                    TextView title = new TextView(this);
                    title.setText("Congratulations!");
                    //title.setBackgroundColor(Color.DKGRAY);
                    title.setPadding(10, 10, 10, 10);
                    title.setGravity(Gravity.CENTER);
                    title.setTextColor(Color.BLUE);
                    title.setTextSize(20);
                    builder.setCustomTitle(title);
                    builder.setCancelable(false);


                    builder.setCancelable(false);
                    builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            loadGroups();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
            case 2:
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupMainActivity.this);
                    TextView myMsg = new TextView(this);
                    myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_2));
                    myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                    builder.setView(myMsg);

                    TextView title = new TextView(this);
                    title.setText("Thank you!");
                    //title.setBackgroundColor(Color.DKGRAY);
                    title.setPadding(10, 10, 10, 10);
                    title.setGravity(Gravity.CENTER);
                    title.setTextColor(Color.BLUE);
                    title.setTextSize(20);
                    builder.setCustomTitle(title);
                    builder.setCancelable(false);


                    builder.setCancelable(false);
                    builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            loadGroups();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
            case 3:
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupMainActivity.this);
                    TextView myMsg = new TextView(this);
                    myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_3));
                    myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                    builder.setView(myMsg);

                    TextView title = new TextView(this);
                    title.setText("Almost done!");
                    //title.setBackgroundColor(Color.DKGRAY);
                    title.setPadding(10, 10, 10, 10);
                    title.setGravity(Gravity.CENTER);
                    title.setTextColor(Color.BLUE);
                    title.setTextSize(20);
                    builder.setCustomTitle(title);
                    builder.setCancelable(false);

                    builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent cbSelectIntent = new Intent(GroupMainActivity.this , DirMailSelectActivity.class);
                            startActivityForResult(cbSelectIntent, VALIDATE_LINK_ACTIVITY);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
            case 4:
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupMainActivity.this);
                    TextView myMsg = new TextView(this);
                    myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_4));
                    myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                    builder.setView(myMsg);

                    TextView title = new TextView(this);
                    title.setText("Fantastic!");
                    //title.setBackgroundColor(Color.DKGRAY);
                    title.setPadding(10, 10, 10, 10);
                    title.setGravity(Gravity.CENTER);
                    title.setTextColor(Color.BLUE);
                    title.setTextSize(20);
                    builder.setCustomTitle(title);
                    builder.setCancelable(false);

                    builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
            case 5:
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupMainActivity.this);
                    TextView myMsg = new TextView(this);
                    myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_5));
                    myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                    builder.setView(myMsg);

                    TextView title = new TextView(this);
                    title.setText("Almost done!");
                    //title.setBackgroundColor(Color.DKGRAY);
                    title.setPadding(10, 10, 10, 10);
                    title.setGravity(Gravity.CENTER);
                    title.setTextColor(Color.BLUE);
                    title.setTextSize(20);
                    builder.setCustomTitle(title);
                    builder.setCancelable(false);

                    builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent cbSelectIntent = new Intent(GroupMainActivity.this , DirMailSelectActivity.class);
                            startActivityForResult(cbSelectIntent, VALIDATE_LINK_ACTIVITY);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
            case 6:
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupMainActivity.this);
                    TextView myMsg = new TextView(this);
                    String msgText = "Your Ginko registered \nemail must have a domain \nof gmail.com to join this \ndirectory.\n" +
                            "            Go to Ginko Login to add \nand validate a matching \nemail domain based on the admin’s directory \nrequirement then try again.";
                    msgText = msgText.replace("gmail.com", ShareYourLeafActivity.domainName);
                    myMsg.setText(msgText);
//                    myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_6));
                    myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                    builder.setView(myMsg);

                    TextView title = new TextView(this);
                    title.setText("Sorry!");
                    //title.setBackgroundColor(Color.DKGRAY);
                    title.setPadding(10, 10, 10, 10);
                    title.setGravity(Gravity.CENTER);
                    title.setTextColor(Color.BLUE);
                    title.setTextSize(20);
                    builder.setCustomTitle(title);
                    builder.setCancelable(false);

                    builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("Okay", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int paramInt) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                            Intent contentIntent = new Intent(GroupMainActivity.this, LoginSettingActivity.class);
                            startActivity(contentIntent);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
        }

    }

    public boolean findDirectoryName(String name)
    {
        boolean result = false;
        if (groups == null || groups.size() == 0)
            return false;

        for (int i=0; i <groups.size(); i++) {
            GroupVO group = groups.get(i);
            if (group.getType() == 2 && group.getName().equalsIgnoreCase(name))
            {
                result = true;
                Intent intent = new Intent(GroupMainActivity.this, GroupDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("group", group);
                intent.putExtras(bundle);
                startActivityForResult(intent, GO_TO_CONTACT);
                break;
            }
        }

        return result;
    }

    public void JoinAndNextScreen()
    {
        isJoinShow = true;

        joinNewDirectoryDialog = new InputDialog(this,
                InputType.TYPE_CLASS_TEXT,
                "Join a Directory." , //title
                "Enter Directory Name", //Hint
                false , //show titlebar
                getResources().getString(R.string.str_cancel) , //left button name
                new InputDialog.OnButtonClickListener(){
                    @Override
                    public boolean onClick(Dialog dialog , View v, String input) {
                        isJoinShow = false;
                        RepeatSafeToast.cancel();
                        return true;
                    }//left button clicklistener
                },
                getResources().getString(R.string.str_okay), //right button name
                new InputDialog.OnButtonClickListener() //right button clicklistener
                {
                    @Override
                    public boolean onClick(Dialog dialog , View v , final String directoryName) {
                        if(directoryName.trim().equalsIgnoreCase(""))
                        {
                            RepeatSafeToast.show(GroupMainActivity.this, "Please enter directory name.");
                            return false;
                        }

                        RepeatSafeToast.cancel();
                        isJoinShow = false;

                        boolean isExistName = findDirectoryName(directoryName);

                        if (!isExistName)
                        {
                            DirectoryRequest.checkDirectoryExist(directoryName, new ResponseCallBack<JSONObject>() {
                                @Override
                                public void onCompleted(JsonResponse<JSONObject> response) {
                                    if (response.isSuccess())
                                    {
                                        JSONObject object = response.getData();
                                        String directoryId = "";
                                        try {
                                            directoryId = object.getString("id");
                                        } catch (JSONException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }

                                        Intent contactSharingSettingIntent = new Intent(GroupMainActivity.this , ShareYourLeafActivity.class);
                                        contactSharingSettingIntent.putExtra("contactID" , "0");
                                        contactSharingSettingIntent.putExtra("contactFullname" , directoryName);
                                        contactSharingSettingIntent.putExtra("isDirectory" , true);
                                        contactSharingSettingIntent.putExtra("directoryID", Integer.valueOf(directoryId));
                                        contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                                        contactSharingSettingIntent.putExtra("isInviteContact", false);
                                        contactSharingSettingIntent.putExtra("isPendingRequest", false);
                                        contactSharingSettingIntent.putExtra("StartActivity", "ContactMain");
                                        contactSharingSettingIntent.putExtra("isJoinDirectory", true);

                                        startActivityForResult(contactSharingSettingIntent, SHARE_YOUR_LEAF_ACTIVITY );
                                    } else
                                    {
                                        Uitils.alert(GroupMainActivity.this, response.getErrorMessage());
                                    }
                                }
                            });

                        }

                        return true;
                    }
                },
                new InputDialog.OnEditorDoneActionListener() {

                    @Override
                    public void onEditorActionDone(Dialog dialog, String directoryName) {
                        if(directoryName.trim().equalsIgnoreCase(""))
                        {
                            RepeatSafeToast.show(GroupMainActivity.this, "Please enter directory name.");
                            return;
                        }
                        RepeatSafeToast.cancel();
                        isJoinShow = false;

                        DirectoryRequest.checkDirectoryExist(directoryName, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess())
                                {

                                } else
                                {
                                    Uitils.alert(GroupMainActivity.this, response.getErrorMessage());
                                }
                            }
                        });

                        dialog.dismiss();

                    }
                }
        );
        joinNewDirectoryDialog.show();
        joinNewDirectoryDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                m_dialog = dialog;
                ((InputDialog)m_dialog).showKeyboard();
            }
        });
    }

    public static class RepeatSafeToast {

        private static final int DURATION = 4000;

        private static final Map<Object, Long> lastShown = new HashMap<Object, Long>();
        private static Toast myToast;

        private static boolean isRecent(Object obj) {
            Long last = lastShown.get(obj);
            if (last == null) {
                return false;
            }
            long now = System.currentTimeMillis();
            if (last + DURATION < now) {
                return false;
            }
            return true;
        }

        public static synchronized void show(Context context, int resId) {
            if (isRecent(resId)) {
                return;
            }
            myToast =  Toast.makeText(context, resId, Toast.LENGTH_SHORT);
            myToast.show();
            lastShown.put(resId, System.currentTimeMillis());
        }

        public static synchronized void show(Context context, String msg) {
            if (isRecent(msg)) {
                return;
            }
            myToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            myToast.show();
            lastShown.put(msg, System.currentTimeMillis());
        }

        public static synchronized  void cancel()
        {
            if (myToast != null)
                myToast.cancel();
        }
    }
}
