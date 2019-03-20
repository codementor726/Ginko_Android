package com.ginko.activity.menu;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.cb.CBMainActivity;
import com.ginko.activity.contact.AddGreyOneActivity;
import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.contact.SearchContactActivity;
import com.ginko.activity.directory.CreateDirNamesActivity;
import com.ginko.activity.directory.DirAdminPreviewActivity;
import com.ginko.activity.directory.DirMailSelectActivity;
import com.ginko.activity.entity.EntityCategorySelectActivity;
import com.ginko.activity.entity.EntityEditActivity;
import com.ginko.activity.entity.EntityMultiLocationsPreviewActivity;
import com.ginko.activity.entity.EntityProfilePreviewActivity;
import com.ginko.activity.entity.OldEntityProfilePreviewActivity;
import com.ginko.activity.exchange.ExchangeRequestActivity;
import com.ginko.activity.im.GroupVideoChatActivity;
import com.ginko.activity.im.ImPreActivity;
import com.ginko.activity.group.GroupMainActivity;
import com.ginko.activity.im.VideoChatAddUserActivity;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.activity.sync.SyncHomeActivity;
import com.ginko.activity.user.PersonalProfilePreviewActivity;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.ImageButtonTab;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.ProgressHUD;
import com.ginko.customview.SectionViewAdapter;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.service.SproutService;
import com.ginko.setup.TutorialActivity;
import com.ginko.vo.DirectoryVO;
import com.ginko.vo.EntityVO;
import com.ginko.vo.EventUser;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserWholeProfileVO;
import com.ginko.vo.VideoMemberVO;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MenuActivity extends MyBaseActivity {
	private SectionViewAdapter<String, MainMenuItem> adapter;

	private ImageButtonTab imgButtonTab = new ImageButtonTab();
    private TextView txtFirstName , txtLastName;

    private ImageButton btnPrev;
    private ImageView btnChatNav;
    private TextView txtMessageBadge;

    private ImageButton btnContactTileStyle ,btnContactListStyle;

    private NetworkImageView profilePhoto;
    private ImageLoader imgLoader;

    private ProgressHUD progressDialog = null;

    private boolean isSynchronizingContacts = false;

    private Handler mHandler = new Handler();
    private int newMessageCount = 0;
    private boolean isUICreated = false;
    private boolean isLoadPages = false;
    private boolean isLoadDirectories = false;

    private boolean isImReceiverRegistered = false;
    private boolean isEntityReceiverRegistered = false;
    private boolean isShowDialog = false;
    private int directoryId = 0;
    private int statusCode = 0;
    private String directoryName = "";

    //---------------------------------//
    /* new message listener */
    private ImMsgReceiver imReceiver;
    private EntityMsgReceiver entityMsgReceiver;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

        Intent intent = this.getIntent();
        newMessageCount = intent.getIntExtra("message_count" , 0);
        isShowDialog = intent.getBooleanExtra("isShowDialog", false);
        directoryId = intent.getIntExtra("directoryID", 0);
        directoryName = intent.getStringExtra("directoryName");

        btnPrev = (ImageButton)findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnChatNav = (ImageView)findViewById(R.id.btnChatNav);
        btnChatNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uitils.toActivity(ImPreActivity.class, false);
            }
        });

        txtMessageBadge = (TextView)findViewById(R.id.txtMessageBadge);
        txtFirstName = (TextView)findViewById(R.id.txtFirstName);
        txtLastName = (TextView)findViewById(R.id.txtLastName);

        if(RuntimeContext.getUser()!= null)
        {
            txtFirstName.setText(RuntimeContext.getUser().getFirstName());
            if(RuntimeContext.getUser().getMiddleName().trim().equals(""))
                txtLastName.setText(RuntimeContext.getUser().getLastName());
            else
                txtLastName.setText(RuntimeContext.getUser().getMiddleName()+" "+RuntimeContext.getUser().getLastName());

        }

        btnContactTileStyle = (ImageButton) this.findViewById(R.id.contact_tile_style);
        btnContactListStyle = (ImageButton) this.findViewById(R.id.contact_list_style);

		imgButtonTab.addButton(btnContactTileStyle,
				R.drawable.tileselect, R.drawable.tileunselect);
		imgButtonTab.addButton(btnContactListStyle,
				R.drawable.listselect, R.drawable.listunselect);

        if(Uitils.getIsContactTileStyle(MenuActivity.this ))//contact tile style
        {
            btnContactTileStyle.setImageResource(R.drawable.tileselect);
            btnContactListStyle.setImageResource(R.drawable.listunselect);
        }
        else
        {
            btnContactTileStyle.setImageResource(R.drawable.tileunselect);
            btnContactListStyle.setImageResource(R.drawable.listselect);
        }

		final ListView list = (ListView) this.findViewById(R.id.list);

		adapter = new SectionViewAdapter<String, MainMenuItem>() {
			@Override
			protected View getItemView(MainMenuItem item, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) MenuActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.list_item_mainmenu,
						parent, false);

				TextView a = (TextView) view.findViewById(R.id.list_item_title);
                a.setSingleLine(true);

				a.setText(item.getText());

				ImageView icon = (ImageView) view.findViewById(R.id.icon);
				ImageView arrowIcon = (ImageView) view.findViewById(R.id.right_arraw);
                if(item.getText().equals("Sign out"))
                    arrowIcon.setVisibility(View.GONE);
				if (item.getIcon() != -1) {
					icon.setImageResource(item.getIcon());
				}
				return view;
			}

			@Override
			protected View getSectionView(String section, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) MenuActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(
						R.layout.list_section_header_mainmenu, parent, false);

				TextView a = (TextView) view
						.findViewById(R.id.list_header_title);
				a.setText(section);
				return view;
			}

		};

		list.setAdapter(adapter);

		list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Object item = adapter.getItem(position);
                boolean isSection = adapter.isSection(item);
                if (isSection) {
                    return;
                }
                MainMenuItem menuItem = (MainMenuItem) item;
                Logger.debug("select Menu in MenuActivity:" + menuItem.getText());
                menuItem.execute();
            }

        });



        profilePhoto =(NetworkImageView)findViewById(R.id.photo);
        profilePhoto.setDefaultImageResId(R.drawable.profile_preview_default_icon);

        findViewById(R.id.myinfo_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Uitils.toActivity(UserProfilePreviewActivity.class, false);
                Uitils.toActivity(PersonalProfilePreviewActivity.class, false);
            }
        });

        imReceiver = new ImMsgReceiver();
        entityMsgReceiver = new EntityMsgReceiver();

        if (this.imReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
            registerReceiver(this.imReceiver, msgReceiverIntent);
            isImReceiverRegistered = true;
        }
        if(this.entityMsgReceiver != null)
        {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.ENTITY_NEW_MSG");
            registerReceiver(this.entityMsgReceiver, msgReceiverIntent);
            isEntityReceiverRegistered = true;
        }

        initialMenus();
        if (isShowDialog == true) {
//            showCongratulations("jellastar");
            if (!directoryName.equals("")) {
                showCongratulations(directoryName);
            }
        }
	}

    public void showValuableMessage(int nStatus, String name)
    {
        switch (nStatus)
        {
            case 1:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                TextView myMsg = new TextView(this);
                String msgText = "You joined the Ginko \ndirectory.  A directory icon \nwill appear in Groups.";
                msgText = msgText.replace("Ginko", name);
                myMsg.setText(msgText);
//                myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_1));
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
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                ShareYourLeafActivity.menuName = "";
                ShareYourLeafActivity.menuCode = 0;
            }
            break;
            case 2:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
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
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            break;
            case 3:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
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
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            break;
            case 4:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
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
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            break;
            case 6:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                TextView myMsg = new TextView(this);
                myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_6));
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
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            break;
        }

    }

    @Override
    protected void onResume() {
        isUICreated = true;

        super.onResume();

        if (MyApp.getInstance().isNetworkConnected() == false) {
            Uitils.alert("Internet connection is missing.");
            return;
        }

        if(imReceiver == null)
            imReceiver = new ImMsgReceiver();

        if (this.imReceiver != null && isImReceiverRegistered == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
            registerReceiver(this.imReceiver, msgReceiverIntent);
            isImReceiverRegistered = true;
        }

        if(entityMsgReceiver == null)
            entityMsgReceiver = new EntityMsgReceiver();
        if (this.entityMsgReceiver != null && isEntityReceiverRegistered == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.ENTITY_NEW_MSG");
            registerReceiver(this.entityMsgReceiver, msgReceiverIntent);
            isEntityReceiverRegistered = true;
        }

        if(newMessageCount>0)
        {
            txtMessageBadge.setVisibility(View.VISIBLE);
            txtMessageBadge.setText(String.valueOf(newMessageCount));
        }
        else {
            txtMessageBadge.setVisibility(View.INVISIBLE);
        }


        UserInfoRequest.getInfo(new ResponseCallBack<UserWholeProfileVO>() {
            @Override
            public void onCompleted(JsonResponse<UserWholeProfileVO> response) {
                if (response.isSuccess()) {
                    UserWholeProfileVO myInfo = response.getData();
                    if (myInfo != null) {
                        if (imgLoader == null)
                            imgLoader = MyApp.getInstance().getImageLoader();

                        profilePhoto.refreshOriginalBitmap();
                        //if user has personal(home) profile
                        if(myInfo.getHome() != null && myInfo.getHome().getFields() != null && myInfo.getHome().getInputableFieldsCount() > 1) {
                            profilePhoto.setImageUrl(myInfo.getHome().getProfileImage(), imgLoader);
                        }
                        else {
                            profilePhoto.setImageUrl(myInfo.getWork().getProfileImage(), imgLoader);
                            txtFirstName.setText(myInfo.getWork().getProfileUserName());
                            String name = myInfo.getWork().getProfileUserName();
                            String firstName=name;
                            String lastName="";
                            int index = name.indexOf(" ");
                            if (index>0){
                                firstName = name.substring(0, index);
                                lastName= name.substring(index+1);
                            }
                            myInfo.setFirstName(firstName);
                            myInfo.setLastName(lastName);
                        }

                        txtFirstName.setText(myInfo.getFirstName());
                        if(myInfo.getMiddleName().trim().equals(""))
                            txtLastName.setText(myInfo.getLastName());
                        else
                            txtLastName.setText(myInfo.getMiddleName()+" "+myInfo.getLastName());
                        profilePhoto.invalidate();


                    }
                }
            }
        });

        if (isLoadPages == true)
            loadPages();
        if (isLoadDirectories == true)
            loadDirectories();

        getNewMessageCounts();

        showValuableMessage(ShareYourLeafActivity.menuCode, ShareYourLeafActivity.menuName);

    }

    @Override
    protected void onPause() {
        isUICreated = false;

        super.onPause();

        if (MyApp.getInstance().isNetworkConnected() == false) {
            Uitils.alert("Internet connection is missing.");
            return;
        }

        if (this.imReceiver != null && isImReceiverRegistered == true) {
            unregisterReceiver(this.imReceiver);
            isImReceiverRegistered = false;
        }

        if (this.entityMsgReceiver != null && isEntityReceiverRegistered == true) {
            unregisterReceiver(this.entityMsgReceiver);
            isEntityReceiverRegistered = false;
        }
    }

    private void initialMenus() {

        adapter.clearAllData();

		List<MainMenuItem> items = new ArrayList<MainMenuItem>();

        MainMenuItem itemGinko = new MainMenuItem("Ginko Call", R.drawable.btnphone_40, new MainMenuItem.MenuAction() {
            @Override
            public void run(Object menuItemObj) {

                Intent groupVideoIntent = new Intent(MenuActivity.this, VideoChatAddUserActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("boardId", 0);
                bundle.putBoolean("isReturnFromConference", false);
                bundle.putString("existContactIds", "");
                groupVideoIntent.putExtras(bundle);
                startActivity(groupVideoIntent);
            }
        });

        MainMenuItem item = new MainMenuItem("Backup Contacts"  ,
                R.drawable.btnimport, SyncHomeActivity.class);

		MainMenuItem item3 = new MainMenuItem("Find Contacts",
				R.drawable.btnfind , SearchContactActivity.class);
		//MainMenuItem item1 = new MainMenuItem("Groups", R.drawable.menu_groups, GroupMainActivity.class);
        //MainMenuItem item2_1 = new MainMenuItem("Scan Me", R.drawable.btnscanme, ScanMeActivity.class);

        /*MainMenuItem item2 = null;

        if(RuntimeContext.getUser() != null)
        {
            //refresh sprout setting
            if (RuntimeContext.getUser().getLocationOn()) {
                long sproutStartedTime = Uitils.getSproutStartedTime(MyApp.getContext());
                long currentTime = Calendar.getInstance().getTimeInMillis();
                if (sproutStartedTime == 0 || currentTime - sproutStartedTime > SproutService.AUTO_TURN_OFF_TIME_LIMIT)//if sprout is not set to turn off after one hour automatically or
                //or the sprout time is exceed one hour , then hide one hour number textview
                {
                    item2 = new MainMenuItem("Ginko Me",
                            R.drawable.sprout_icon, SproutActivity.class);//gps on
                } else {
                    item2 = new MainMenuItem("Ginko Me",
                            R.drawable.sprout_icon, SproutActivity.class); //gps on ,automatic turn off after one hour
                }
            } else {
                item2 = new MainMenuItem("Ginko Me",
                        R.drawable.sprout_icon, SproutActivity.class);
            }
        }*/
        MainMenuItem item5 = new MainMenuItem("Builder", R.drawable.btnbuilder, CBMainActivity.class);
		//MainMenuItem item4 = new MainMenuItem("Exchange",
		//		R.drawable.exchange_purple, ExchangeRequestActivity.class);

		//MainMenuItem item6 = new MainMenuItem("Add Contact", R.drawable.btnadd, AddGreyContactActivity.class);
        MainMenuItem item6 = new MainMenuItem("Add Contact", R.drawable.btnadd, AddGreyOneActivity.class);
		MainMenuItem item7 = new MainMenuItem("Sync to Device",
				R.drawable.btnsync , new MainMenuItem.MenuAction(){
            @Override
            public void run(Object menuItemObj) {
                if(isSynchronizingContacts) return;
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                builder.setTitle("Confirm");
                builder.setMessage("Do you want sync your Ginko Address Book to your phone?");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        syncDevice();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        dialog.dismiss();
                    }
                });
                AlertDialog dialogShowConfirm = builder.create();
                dialogShowConfirm.show();
            }
        });

        items.add(itemGinko);
		items.add(item);
		//items.add(item1);
        //items.add(item2_1);
		//items.add(item2);
        items.add(item3);
		//items.add(item4);
        items.add(item5);
        items.add(item6);
        items.add(item7);
		adapter.addItems("Function", items);

        MainMenuItem addDirPage = new MainMenuItem("New", R.drawable.btnnew, CreateDirNamesActivity.class);
        adapter.addItem("Directories", addDirPage);

		MainMenuItem addNewPage = new MainMenuItem("New", R.drawable.btnnew, EntityCategorySelectActivity.class);
        adapter.addItem("Pages", addNewPage);

		this.addAdminMenu();

        runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

        if (MyApp.getInstance().isNetworkConnected() == true) {
            loadDirectories();
            loadPages();
        }

	}

    private void showCongratulations(String name)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
        TextView myMsg = new TextView(this);
        String msgText = "You joined the Ginko \ndirectory.  A directory icon \nwill appear in Groups.";
        msgText = msgText.replace("Ginko", name);
        myMsg.setText(msgText);
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(myMsg);

        TextView title = new TextView(this);
        title.setText("Congratulations!");
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
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void syncDevice()
    {
        if (progressDialog == null) {
            progressDialog = ProgressHUD.createProgressDialog(MenuActivity.this, getResources().getString(R.string.str_synchronizing_contacts),
                    true, false, null);
            progressDialog.show();
        } else {
            progressDialog.show();
        }
        isSynchronizingContacts = true;
        new Thread() {
            @Override
            public void run() {
                if (MyApp.g_contactItems == null) {
                    MyApp.getInstance().getAllContactItemsFromDatabase();
                }

                for (ContactItem item : MyApp.g_contactItems) {

                    writeContact(item.getFullName(), item.getPhones(), item.getEmails());
                }
                isSynchronizingContacts = false;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(progressDialog != null)
                        progressDialog.dismiss();
                    }
                });
            }
        }.start();
    }

	private void loadPages() {
		EntityRequest.listEntities(new ResponseCallBack<List<EntityVO>>() {

            @Override
            public void onCompleted(JsonResponse<List<EntityVO>> response) {
                if (isLoadPages == false)
                    isLoadPages = true;

                if (response.isSuccess()) {
                    adapter.removeItems("Pages");
                    MainMenuItem initPage = new MainMenuItem("New", R.drawable.btnnew, EntityCategorySelectActivity.class);
                    adapter.addItem("Pages", initPage);


                    List<EntityVO> data = response.getData();
                    for (final EntityVO entityVO : data) {
                        String name = entityVO.getName();
                        String profileImage = entityVO.getProfileImage();
                        MainMenuItem addNewPage = new MainMenuItem(name, entityVO, new MainMenuItem.MenuAction() {
                            @Override
                            public void run(Object menuItemObj) {
                                if (menuItemObj == null) return;
                                EntityVO entity = (EntityVO) menuItemObj;
                                if (entity.getEntityInfos().size() > 0) {
                                    if (entity.getEntityInfos().size() > 1) {
                                        Intent entityMultiLocationsPreviewIntent = new Intent(MenuActivity.this, EntityMultiLocationsPreviewActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("entity", entity);
                                        bundle.putBoolean("isNewEntity", false);
                                        entityMultiLocationsPreviewIntent.putExtras(bundle);
                                        startActivity(entityMultiLocationsPreviewIntent);
                                    } else {
                                        Intent entityProfilePreviewIntent = new Intent(MenuActivity.this, EntityProfilePreviewActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("entity", entity);
                                        bundle.putBoolean("isNewEntity", false);
                                        bundle.putBoolean("isMultiLocations", false);
                                        entityProfilePreviewIntent.putExtras(bundle);
                                        startActivity(entityProfilePreviewIntent);
                                    }
                                } else {
                                    Intent intent = new Intent(MenuActivity.this, EntityEditActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("entity", entity);
                                    bundle.putSerializable("isNewEntity", true);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }
                            }
                        });
                        adapter.addItem("Pages", addNewPage);
                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

            }
        });
	}

    private void loadDirectories() {
        DirectoryRequest.listDirectories(new ResponseCallBack<List<DirectoryVO>>() {

            @Override
            public void onCompleted(JsonResponse<List<DirectoryVO>> response) {
                if (isLoadDirectories == false)
                    isLoadDirectories = true;

                if (response.isSuccess()) {
                    adapter.removeItems("Directories");
                    MainMenuItem initPage = new MainMenuItem("New", R.drawable.btnnew, CreateDirNamesActivity.class);
                    adapter.addItem("Directories", initPage);

                    List<DirectoryVO> data = response.getData();
                    for (final DirectoryVO dirVO : data) {
                        String name = dirVO.getName();
                        String profileImage = dirVO.getProfileImage();
                        MainMenuItem addNewPage = new MainMenuItem(name, dirVO, new MainMenuItem.MenuAction() {
                            @Override
                            public void run(Object menuItemObj) {
                                if (menuItemObj == null) return;
                                final DirectoryVO dirInfo = (DirectoryVO) menuItemObj;

                                DirectoryRequest.getDirectoryDetail(dirInfo.getId(), new ResponseCallBack<DirectoryVO>() {
                                    @Override
                                    public void onCompleted(JsonResponse<DirectoryVO> response) {
                                        if (response.isSuccess()){
                                            DirectoryVO newInfo = response.getData();
                                            Intent directoryPreviewIntent = new Intent(MenuActivity.this, DirAdminPreviewActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("directory", newInfo);
                                            directoryPreviewIntent.putExtras(bundle);
                                            startActivity(directoryPreviewIntent);
                                        }
                                    }
                                });
                            }
                        });
                        adapter.addItem("Directories", addNewPage);
                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

            }
        });
    }

	private void addAdminMenu() {
		List<MainMenuItem> items = new ArrayList<MainMenuItem>();
		MainMenuItem item = new MainMenuItem("Settings", R.drawable.btnsetting, AccountSettingActivity.class);
		MainMenuItem item1 = new MainMenuItem("Login", R.drawable.btnlogin,LoginSettingActivity.class);
		MainMenuItem item2 = new MainMenuItem("Password",
				R.drawable.btnpassword, ChangePasswordActivity.class);
		MainMenuItem item3 = new MainMenuItem("Notifications",
				R.drawable.btnnotification, NotificationsActivity.class);
        MainMenuItem item4 = new MainMenuItem("Tutorial",
                R.drawable.tutorial_icon, TutorialActivity.class);
		MainMenuItem item5 = new MainMenuItem("Sign out", R.drawable.btnsignout, new MainMenuItem.MenuAction(){
            @Override
            public void run(Object menuItemObj) {
                logout();
            }
        });

		items.add(item);
		items.add(item1);
		items.add(item2);
		items.add(item3);
		items.add(item4);
        items.add(item5);
		adapter.addItems("Admin", items);
	}

	@Override
	protected void onDestroy() {
		this.imgButtonTab.clear();
		super.onDestroy();
	}
	
	public void changeContactViewStyle(View view){
		this.imgButtonTab.selectButton((ImageButton) view);
        if(view.getId() == R.id.contact_tile_style)
        {
            Uitils.storeIsContactTileStyle(MenuActivity.this, true);
        }
        else
        {
            Uitils.storeIsContactTileStyle(MenuActivity.this , false);
        }
	}

    private void writeContact(String displayName, List<String> phoneNumbers , List<String> emails) {
        ArrayList contentProviderOperations = new ArrayList();
        //insert raw contact using RawContacts.CONTENT_URI
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null).withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        //insert contact display name using Data.CONTENT_URI
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName).build());

        //insert mobile number using Data.CONTENT_URI

        if(phoneNumbers != null && phoneNumbers.size()>0) {
            for(int i=0;i<phoneNumbers.size();i++) {
                contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumbers.get(i))
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER).build());
            }
        }

        //insert email address using Data.CONTENT_URI
        if(emails != null && emails.size()>0) {

            for(int i = 0;i<emails.size();i++) {
                contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, emails.get(i))
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_OTHER).build());
            }
        }

        try {
            getApplicationContext().getContentResolver().
                    applyBatch(ContactsContract.AUTHORITY, contentProviderOperations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
    private void getNewMessageCounts()
    {
        //call User/contact/summary to get new events
        UserRequest.getContactSummary(new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if(response.isSuccess())
                {
                    //sample response
                    //{"xcg_req_num":0,"not_xcg_sprout_num":0,"new_chat_msg_num":1,"contact_counts":{"work":2,"home":3,"entity":1},"all_cb_valid":true}

                    JSONObject jsonObject = response.getData();
                    newMessageCount = jsonObject.optInt("new_chat_msg_num" , 0);
                    if(isUICreated) {
                        if (newMessageCount > 0) {
                            txtMessageBadge.setVisibility(View.VISIBLE);
                            txtMessageBadge.setText(String.valueOf(newMessageCount));
                        } else {
                            txtMessageBadge.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        });
    }

    public class ImMsgReceiver extends BroadcastReceiver {
        public ImMsgReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New message");

            getNewMessageCounts();

        }
    }
    public class EntityMsgReceiver extends BroadcastReceiver {
        public EntityMsgReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New message");

            getNewMessageCounts();
        }
    }
}
