package com.ginko.activity.contact;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.activity.cb.CBMainActivity;
import com.ginko.activity.exchange.ExchangeRequestActivity;
import com.ginko.activity.favorite.FavoriteActivity;
import com.ginko.activity.group.GroupMainActivity;
import com.ginko.activity.im.ImPreActivity;
import com.ginko.activity.menu.MenuActivity;
import com.ginko.activity.menu.ScanMeActivity;
import com.ginko.activity.profiles.GreyContactOne;
import com.ginko.activity.profiles.GreyContactProfile;
import com.ginko.activity.profiles.PurpleContactProfile;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.activity.profiles.UserEntityMultiLocationsProfileActivity;
import com.ginko.activity.profiles.UserEntityProfileActivity;
import com.ginko.activity.sprout.MyGinkoMeActivity;
import com.ginko.activity.sprout.SproutSearchItem;
import com.ginko.activity.sync.SyncHomeActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.IMRequest;
import com.ginko.api.request.SpoutRequest;
import com.ginko.api.request.SyncRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.ImageButtonTab;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.AlphabetSidebar;
import com.ginko.customview.ProgressHUD;
import com.ginko.customview.SproutProgressDialog;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ContactStruct;
import com.ginko.database.ContactTableModel;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.imagecrop.Util;
import com.ginko.service.SproutService;
import com.ginko.setup.CountryCodeItem;
import com.ginko.setup.GoToInviteContactScreenConfirmActivity;
import com.ginko.setup.InviteGinkoConnects;
import com.ginko.setup.RegisterConfirmationMobileActivity;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;
import com.ginko.vo.IMBoardMessage;
import com.ginko.vo.ImMessageVO;
import com.ginko.vo.PurpleContactWholeProfileVO;
import com.ginko.vo.UserEntityProfileVO;
import com.ginko.vo.UserLoginVO;
import com.ginko.vo.UserWholeProfileVO;
import com.hb.views.PinnedSectionListView;
import com.hb.views.PullToRefreshPinnedSectionListView;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.PullToRefreshBase.OnRefreshListener;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactMainActivity extends MyBaseActivity implements View.OnClickListener,
        SproutService.SproutServiceActionListener,
        AlphabetSidebar.OnTouchingLetterChangedListener
{
	private ContactListAdapter adapter;
    private ImageView btnSearch , btnInviteContact, btnGroup;
    private RelativeLayout chatNavLayout;
    private ImageView btnThumb;
    private ImageButton btnSprout;
    private TextView txtSproutBadge , txtMessageBadge , txtExchangeRequestBadge;
    private LinearLayout shortCutButtonLayout , contactListLayout, noteNoFavoriteCotnact;

    private ImageButton btnFilterEntity , btnFilterHome , btnFilterWork , btnFilterAll, btnFilterFavorite;

    private AlphabetSidebar alphabetScrollbar;
	private PinnedSectionListView mListView;
	private PullToRefreshPinnedSectionListView mPullListView;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
	private boolean mIsStart = false;
    private boolean isPause = false;

	private int mCurIndex = 0;
	private int mLoadDataCount = 100;

    private UserEntityProfileVO entity;

    private int newMessageCount = 0;
    private int newSproutCount = 0;
    private int newExchangedRequestCount = 0;

    private Handler mHandler = new Handler();
    private Handler handler = new Handler();

    private boolean isSproutOn = false;
    private boolean isFavorited = false;
    private SproutService sproutService = null;

    private Object lockObj = new Object();

    private ContactChangeReceiver contactChangeReceiver; private boolean isContactChangeReceiverRegistered = false;
    private ExchangeRequestReceiver exchangeRequestReceiver; private boolean isExchangeRequestReceiverRegistered = false;
    private EntityRemoveReceiver entityRemoveReceiver; private boolean isEntityRemoveReceiverRegistered = false;

    private boolean isUICreated = false;
    private boolean isLoadingEnd = false;
    private int tmp_contactid = 0;

    private int entityContactsCount = 0 , homeContactsCount = 0 , workContactsCount = 0, favoritesCount = 0;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            Logger.debug("Bind to sprout service successfully.");
            sproutService = ((SproutService.LocalBinder) binder).getService();
            if(!sproutService.isLocationServiceEnabled())
                showLocationServiceSettingAlertDialog();

            sproutService.registerSproutActionListener(ContactMainActivity.this);
            if(RuntimeContext.getUser() != null)
                isSproutOn = RuntimeContext.getUser().getLocationOn();
            refreshGPSStatusUI();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            sproutService = null;
            //这里可以提示用户
        }
    };

    private void binderLocationService() {
        Intent intent = new Intent(this, SproutService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onSproutAutoStopped() {
        System.out.println("---- Time over! Turned off the service automatically!---");
        Uitils.storeSproutStartTime(MyApp.getContext() , 0);
        this.isSproutOn = false;
        refreshGPSStatusUI();
    }

    @Override
    public void singleLocationUpdateStarted() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("----Single Location update started----");
                btnSprout.setImageResource(R.drawable.satellite_on);
            }
        }, 100);
    }

    @Override
    public void singleLocationUpdateEnded() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("----Single Location update ended----");
                btnSprout.setImageResource(R.drawable.satellite_off);
                //if(mProgressHUD != null)
                //    mProgressHUD.dismiss();
            }
        } , 100);
    }

    @Override
    public void singleLocationChanged() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                detectNearFriends(null, false);
            }
        }, 100);
    }

    //when scroll side alphabet scroll bar
    @Override
    public void onTouchingLetterChanged(String s) {
        if(mListView != null && adapter != null)
        {
            int index =adapter.getSectionItemIndex(s);
            if(index >= 0) {
                mListView.setSelection(index);
            }
        }
    }

    public enum FilterType {
		Entity(0), Home(1), Work(2), Favorite(3),None(null);
        private Integer value;
        private FilterType(Integer val)
        {
            this.value = val;
        }

		public Integer value() {
			return this.value;
		}
	}
	
    private FilterType currentFilterType = FilterType.None;

    private boolean currentIsContactTileStyle = true;

    private static ContactMainActivity instance;

    public static ContactMainActivity getInstance()
    {
        return ContactMainActivity.instance;
    }

    private SproutProgressDialog mProgressHUD;

    private ProgressHUD progressHUD;

    private boolean isThumbPressing = false;

    private boolean isLongClick = false;

    //---------------------------------//
    /* new message listener */
    private ImMsgReceiver imReceiver;
    private boolean isImReceiverRegistered = false;

    private Animation animAlpha;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_main);

        animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha_animation);

        if(savedInstanceState!= null)
        {
            UserLoginVO loginVo = (UserLoginVO) savedInstanceState.getSerializable("login_info");

            RuntimeContext.setUser(loginVo);
        }

        MyApp.getInstance().startSproutService();

        ContactMainActivity.instance = this;

        progressHUD = ProgressHUD.createProgressDialog(ContactMainActivity.this, "", false, false, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(progressHUD != null && progressHUD.isShowing())
                    progressHUD.dismiss();
            }
        });

        currentIsContactTileStyle = Uitils.getIsContactTileStyle(ContactMainActivity.this);

        shortCutButtonLayout = (LinearLayout)findViewById(R.id.shortCutButtonLayout);
        contactListLayout = (LinearLayout)findViewById(R.id.contactListLayout);
        noteNoFavoriteCotnact = (LinearLayout)findViewById(R.id.noteNoFavoriteContact);

        btnSearch = (ImageView)findViewById(R.id.btnSearch); btnSearch.setOnClickListener(this);
        btnInviteContact = (ImageView)findViewById(R.id.btnInviteContact); btnInviteContact.setOnClickListener(this);
        btnGroup = (ImageView)findViewById(R.id.btnGroup); btnGroup.setOnClickListener(this);
        chatNavLayout = (RelativeLayout)findViewById(R.id.chatNavLayout); chatNavLayout.setOnClickListener(this);
        txtSproutBadge = (TextView)findViewById(R.id.txtSproutBadge);
        txtMessageBadge = (TextView)findViewById(R.id.txtMessageBadge);
        txtExchangeRequestBadge = (TextView)findViewById(R.id.txtExchangeRequestBadge);

        btnSprout = (ImageButton)findViewById(R.id.btnSprout);

        btnFilterEntity   = (ImageButton)findViewById(R.id.filter_entity);btnFilterEntity.setOnClickListener(this);
        btnFilterHome     = (ImageButton)findViewById(R.id.filter_home);btnFilterHome.setOnClickListener(this);
        btnFilterWork     = (ImageButton)findViewById(R.id.filter_work);btnFilterWork.setOnClickListener(this);
        btnFilterAll      = (ImageButton)findViewById(R.id.noFilter);btnFilterAll.setOnClickListener(this);
        btnFilterFavorite = (ImageButton)findViewById(R.id.btnFavorite); btnFilterFavorite.setOnClickListener(this);

        //default animation
        LinearLayout ll_scan = (LinearLayout)findViewById(R.id.id_scan);
        ll_scan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        v.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        v.setAlpha(1.0f);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        LinearLayout ll_ginko = (LinearLayout)findViewById(R.id.id_ginko);
        ll_ginko.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        v.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        v.setAlpha(1.0f);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        LinearLayout ll_cb = (LinearLayout)findViewById(R.id.id_cb);
        ll_cb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        v.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        v.setAlpha(1.0f);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        LinearLayout ll_gps = (LinearLayout)findViewById(R.id.id_gps);
        ll_gps.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        v.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        v.setAlpha(1.0f);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        LinearLayout ll_import = (LinearLayout)findViewById(R.id.id_import);
        ll_import.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        v.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        v.setAlpha(1.0f);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        //default category
        currentFilterType = FilterType.None;

        refreshCategoryIcons();

        btnThumb = (ImageView)findViewById(R.id.btnThumb);

        btnThumb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (sproutService == null || isSproutOn == true)
                    return true;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacksAndMessages(null);
                        isLongClick = false;
                        if (!sproutService.isLocationServiceEnabled()) {
                            return true;
                        }
                        isThumbPressing = false;

                        if (mProgressHUD != null && mProgressHUD.isShowing())
                            mProgressHUD.dismiss();
                        btnThumb.setImageResource(R.drawable.fingerprint_enabled);
                        sproutService.stopSingleLocationUpdate();

                        break;
                    case MotionEvent.ACTION_DOWN:
                        handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!sproutService.isLocationServiceEnabled()) {
                                    showLocationServiceSettingAlertDialog();
                                    return;
                                }
                                isThumbPressing = true;
                                if (mProgressHUD == null) {
                                    mProgressHUD = SproutProgressDialog.show(ContactMainActivity.this, true, false, null);
                                } else
                                    mProgressHUD.show();
                                btnThumb.setImageResource(R.drawable.fingerprint_disabled);
                                sproutService.startSingleLocationUpdate();
                            }
                        }, 100);

                        break;
                    case MotionEvent.ACTION_CANCEL:
                        if (!sproutService.isLocationServiceEnabled()) {
                            return true;
                        }
                        isThumbPressing = false;
                        if (mProgressHUD != null && mProgressHUD.isShowing())
                            mProgressHUD.dismiss();
                        btnThumb.setImageResource(R.drawable.fingerprint_enabled);
                        sproutService.stopSingleLocationUpdate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

		mPullListView = (PullToRefreshPinnedSectionListView) this
				.findViewById(R.id.pull_refresh_list);

		mPullListView.setPullLoadEnabled(false);

		mPullListView.setScrollLoadEnabled(false);

		mCurIndex = mLoadDataCount;
		mListView = mPullListView.getRefreshableView();

        alphabetScrollbar = (AlphabetSidebar)findViewById(R.id.alphabetScrollbar);
        alphabetScrollbar.setOnTouchingLetterChangedListener(this);

		this.adapter = new ContactListAdapter(this);
    	mListView.setAdapter(adapter);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);

		mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                final ContactItem contactItem = (ContactItem) adapter.getItem(position);
                if (contactItem.isSection()) return;
                //if (tmp_contactid == contactItem.getContactId()) return;
                tmp_contactid = contactItem.getContactId();

                if (!contactItem.getIsRead() && contactItem.getContactType() != 3) {
                    contactItem.setIsRead(true);
                    final ContactItem item = contactItem;
                    adapter.notifyDataSetChanged();
                    UserRequest.readContact(new Integer(contactItem.getContactId()), contactItem.getContactType(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                //MyApp.getInstance().getContactsModel().update(item);
                            }
                        }
                    }, false);

                }
                if (contactItem.getContactType() == 1)//purple contact
                {
                    if (contactItem.getSharingStatus() == 4) {
                        /*Intent purpleContactProfileIntent = new Intent(ContactMainActivity.this, ShareYourLeafActivity.class);
                        purpleContactProfileIntent.putExtra("contactFullname", contactItem.getFullName());
                        purpleContactProfileIntent.putExtra("contactID", String.valueOf(contactItem.getContactId()));
                        purpleContactProfileIntent.putExtra("isUnexchangedContact" , false);
                        startActivity(purpleContactProfileIntent);*/
                        MyApp.getInstance().showSimpleAlertDiloag(ContactMainActivity.this, "Oops! Contact would like to chat only", null);
                    } else {
                        final String strContactId = String.valueOf(contactItem.getContactId());
                        final String strFullName = contactItem.getFullName();
                        final Intent purpleContactProfileIntent = new Intent(ContactMainActivity.this, PurpleContactProfile.class);
                        final Bundle bundle = new Bundle();
                        bundle.putString("fullname", contactItem.getFullName());
                        //bundle.putBoolean("isFavorite", contactItem.getIsFavorite());
                        bundle.putString("contactID", String.valueOf(contactItem.getContactId()));
                        bundle.putString("StartActivity", "ContactMain");
                        UserRequest.getContactDetail(String.valueOf(strContactId), "1", new ResponseCallBack<PurpleContactWholeProfileVO>() {
                            @Override
                            public void onCompleted(JsonResponse<PurpleContactWholeProfileVO> response) {
                                if (response.isSuccess()) {
                                    PurpleContactWholeProfileVO responseData = response.getData();
                                    bundle.putSerializable("responseData", responseData);
                                    purpleContactProfileIntent.putExtras(bundle);
                                    startActivity(purpleContactProfileIntent);
                                } else {
                                    if (response.getErrorCode() == 350)//The contact can't be found.
                                    {
                                        /*
                                        Intent contactSharingSettingIntent = new Intent(ContactMainActivity.this, ShareYourLeafActivity.class);
                                        contactSharingSettingIntent.putExtra("contactID", String.valueOf(strContactId));
                                        contactSharingSettingIntent.putExtra("contactFullname", strFullName);
                                        contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                                        startActivity(contactSharingSettingIntent);*/
                                        MyApp.getInstance().getContactsModel().deleteContactWithContactId(contactItem.getContactId());
                                        MyApp.getInstance().removefromContacts(contactItem.getContactId());
                                        refreshContactList();

                                    } else {
                                    }
                                }
                            }
                        });
                    }
                } else {
                    if (contactItem.getContactType() == 2)//grey contact
                    {
                        String strContactId = String.valueOf(contactItem.getContactId());
                        SyncRequest.getSyncContactDetial(strContactId, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    JSONObject jsonRes = response.getData();
                                    Intent greyContactProfileIntent = new Intent(ContactMainActivity.this, GreyContactOne.class);
                                    greyContactProfileIntent.putExtra("jsonvalue", jsonRes.toString());
                                    startActivity(greyContactProfileIntent);
                                }
                            }
                        });
                    } else {//entity
                        EntityRequest.viewEntity(contactItem.getContactId(), new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    try {
                                        JSONObject jsonObj = response.getData();
                                        try {
                                            entity = JsonConverter.json2Object(
                                                    (JSONObject) jsonObj, (Class<UserEntityProfileVO>) UserEntityProfileVO.class);
                                        } catch (JsonConvertException e) {
                                            e.printStackTrace();
                                            entity = null;
                                        }

                                        if (entity.getInfos().size() > 1) {
                                            Intent entityProfileIntent = new Intent(ContactMainActivity.this, UserEntityMultiLocationsProfileActivity.class);
                                            entityProfileIntent.putExtra("entityJson", entity);
                                            entityProfileIntent.putExtra("contactID", jsonObj.getInt("entity_id"));
                                            entityProfileIntent.putExtra("isFavorite", jsonObj.getBoolean("is_favorite"));
                                            entityProfileIntent.putExtra("isfollowing_entity", true);
                                            startActivity(entityProfileIntent);
                                        } else {
                                            Intent entityProfileIntent = new Intent(ContactMainActivity.this, UserEntityProfileActivity.class);
                                            entityProfileIntent.putExtra("entityJson", entity);
                                            entityProfileIntent.putExtra("contactID", jsonObj.getInt("entity_id"));
                                            entityProfileIntent.putExtra("isFavorite", jsonObj.getBoolean("is_favorite"));
                                            entityProfileIntent.putExtra("isfollowing_entity", true);
                                            startActivity(entityProfileIntent);
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (response.getErrorCode() == 700 && response.getErrorMessage().equals("The entity can't be found.")) {
                                        MyApp.getInstance().getContactsModel().deleteContactWithContactId(contactItem.getContactId());
                                        MyApp.getInstance().removefromContacts(contactItem.getContactId());
                                        refreshContactList();
                                    }
                                }
                            }
                        }, true);
                    }
                }
            }
        });

		mPullListView
				.setOnRefreshListener(new OnRefreshListener<PinnedSectionListView>() {
                    @Override
                    public void onPullDownToRefresh(PullToRefreshBase<PinnedSectionListView> refreshView) {
                        MyApp.getInstance().getSyncUpdatedContacts(Uitils.getLastSyncTime(getApplicationContext(), RuntimeContext.getUser().getUserId()));
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // when fail, also let the header/footer hide.
                                mPullListView.onPullDownRefreshComplete();
                                mPullListView.onPullUpRefreshComplete();
                            }
                        }, 1500);
                    }

                    @Override
                    public void onPullUpToRefresh(PullToRefreshBase<PinnedSectionListView> refreshView) {
                    }
                });
		setLastUpdateTime();

        //loadContacts(FilterType.None, null, null, false);
        refreshContactList();
        mIsStart = true;

        imReceiver = new ImMsgReceiver();

        if (this.imReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
            registerReceiver(this.imReceiver, msgReceiverIntent);
            isImReceiverRegistered = true;
        }

        this.contactChangeReceiver = new ContactChangeReceiver();
        this.exchangeRequestReceiver = new ExchangeRequestReceiver();
        this.entityRemoveReceiver = new EntityRemoveReceiver();

        if (this.contactChangeReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactChangeReceiver, msgReceiverIntent);
            isContactChangeReceiverRegistered = true;
        }

        if(this.exchangeRequestReceiver != null)
        {
            IntentFilter exchangeRequestReceiver = new IntentFilter();
            exchangeRequestReceiver.addAction("android.intent.action.EXCHANGE_REQUEST");
            registerReceiver(this.exchangeRequestReceiver, exchangeRequestReceiver);
            isExchangeRequestReceiverRegistered = true;
        }

        if (this.entityRemoveReceiver != null) {
            IntentFilter receiveChange = new IntentFilter();
            receiveChange.addAction("android.intent.action.ENTITY_REMOVED");
            this.registerReceiver(this.entityRemoveReceiver, receiveChange);
            isEntityRemoveReceiverRegistered = true;
        }
	}

	private void setLastUpdateTime() {
		String text = formatDateTime(System.currentTimeMillis());
		mPullListView.setLastUpdatedLabel(text);
	}

	private String formatDateTime(long time) {
		if (0 == time) {
			return "";
		}
		return mDateFormat.format(new Date(time));
	}

	// private int currentPage;

	private synchronized void loadContacts(FilterType filter,String search, String sortBy , boolean bProgressDialog) {
		// currentPage = pageNum;
        final Integer filterType = filter.value();

        long lastSyncTimeStamp = Uitils.getLastSyncTime(ContactMainActivity.this, RuntimeContext.getUser().getUserId());
        if(lastSyncTimeStamp == 0) {
            //get all contacts
            progressHUD.show();
            UserRequest.getContacts(null, search, sortBy, new ResponseCallBack<List<JSONObject>>() {

                @Override
                public void onCompleted(JsonResponse<List<JSONObject>> response) {
                    if (response.isSuccess()) {
                        adapter.clear();
                        mListView.initView();
                        String currentSectionName = "";
                        List<JSONObject> contacts = response.getData();
                        List<ContactItem> contactList = new ArrayList<ContactItem>();
                        boolean isSortByFName = Uitils.getIsSortByFName(ContactMainActivity.this);

                        ContactItemComparator contactItemComparator = new ContactItemComparator(ContactMainActivity.this, isSortByFName);
                        ArrayList<ContactStruct> contactStructs = new ArrayList<ContactStruct>();

                        for (JSONObject jsonObject : contacts) {
                            try {
                                int contactType = jsonObject.optInt("contact_type", 1);
                                ContactItem item = null;
                                if (contactType == 1)//purple contact
                                {
                                    item = ContactTableModel.parsePurpleContact(jsonObject);
                                    if(item != null)
                                        contactList.add(item);

                                } else if (contactType == 2)//grey contact
                                {
                                    item = ContactTableModel.parseGreyContact(jsonObject);
                                    if(item != null)
                                        contactList.add(item);

                                } else//entity
                                {
                                    item = ContactTableModel.parseEntityContact(jsonObject);
                                    if(item != null)
                                        contactList.add(item);
                                }
                                if(item != null) {
                                    ContactStruct contactStruct = new ContactStruct();
                                    contactStruct.setFirstName(item.getFirstName());
                                    contactStruct.setLastName(item.getLastName());
                                    contactStruct.setContactType(contactType);
                                    contactStruct.setContactOrEntityId(item.getContactId());
                                    contactStruct.setJsonValue(jsonObject.toString());
                                    contactStructs.add(contactStruct);

                                    MyApp.getInstance().getContactsModel().add(contactStruct);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        //store all contacts to the database
                        //MyApp.getInstance().getContactsModel().addAll(contactStructs);
                        //store login sync stamp time
                        Uitils.storeLastSyncTime(getApplicationContext() , RuntimeContext.getUser().getUserId() , RuntimeContext.getUser().getSyncTimestamp());

                        boolean hasMoreData = contactList.size() != 0;
                        sortContactsAndMakeSectionHeaders(contactList);

                        synchronized (lockObj) {
                            adapter.addAll(contactList);
                        }
                        //adapter.notifyDataSetChanged();
                        mPullListView.setHasMoreData(hasMoreData);
                        setLastUpdateTime();

                        if (adapter.getCount() == 0 && filterType == FilterType.None.value()) {
                            shortCutButtonLayout.setVisibility(View.VISIBLE);
                            contactListLayout.setVisibility(View.INVISIBLE);
                        } else {
                            shortCutButtonLayout.setVisibility(View.INVISIBLE);
                            contactListLayout.setVisibility(View.VISIBLE);
                        }

                    }
                    // when fail, also let the header/footer hide.
                    mPullListView.onPullDownRefreshComplete();
                    mPullListView.onPullUpRefreshComplete();

                    Uitils.storeLastSyncTime(ContactMainActivity.this, RuntimeContext.getUser().getUserId(), RuntimeContext.getUser().getSyncTimestamp());

                    MyApp.getInstance().getAllContactItemsFromDatabase();
                    isLoadingEnd = true;

                    progressHUD.cancel();
                }
            }, bProgressDialog);
        }
        else //if last sync time is not 0 ,then get contacts from DB
        {
            new Thread(){
                @Override
                public void run()
                {
                    ContactTableModel contactsTableModel = MyApp.getInstance().getContactsModel();
                    if(contactsTableModel != null) {
                        MyApp.getInstance().getAllContactItemsFromDatabase();
                        if(MyApp.g_contactItems == null || MyApp.g_contactItems.size() == 0)
                        {
                            //if sometimes db data is lost or can't get data , then refersh syncTime
                            Uitils.storeLastSyncTime(ContactMainActivity.this , RuntimeContext.getUser().getUserId() , 0);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadContacts(FilterType.None, null, null, false);
                                }
                            });
                        }
                        else
                            refreshContactList();
                    }
                }
            }.start();
        }

	}


    private void sortContactsAndMakeSectionHeaders(List<ContactItem> contactList)
    {
        int index = 0;
        String currentSectionName = "";
        boolean isSortByFName = Uitils.getIsSortByFName(ContactMainActivity.this);

        ContactItemComparator contactItemComparator = new ContactItemComparator(ContactMainActivity.this, isSortByFName);

        synchronized (lockObj) {
            try {
                Collections.sort(contactList, contactItemComparator);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        while (index < contactList.size()) {
            String sectionName = createSectionAsNeeded(contactList.get(index), currentSectionName, isSortByFName);
            if (!sectionName.equals("")) {
                currentSectionName = sectionName;
                contactList.add(index, ContactItem.createSection(sectionName));
                index += 2;
                continue;
            }
            index++;
        }
    }

	@SuppressLint("DefaultLocale")
	protected String createSectionAsNeeded(ContactItem item , String currentSectionName , boolean isSortByFName) {
		int contactType = item.getContactType();
        String checkName = null;
        if(isSortByFName)
        {
            checkName = item.getFirstName() + item.getLastName();
        }else
        {
            checkName = item.getLastName() + item.getFirstName();
        }
        checkName = checkName.trim();

		if (checkName.length() > 0
				&& (StringUtils.isEmpty(currentSectionName) || !checkName
						.toUpperCase().startsWith(currentSectionName))) {
			// New section;
            char firstLetter = checkName.charAt(0);
            if(!((firstLetter >= 'a' && firstLetter <= 'z') || (firstLetter >= 'A' && firstLetter <= 'Z') ||
                 (firstLetter >= '0' && firstLetter <= '9')))
            {
                return "#";
            }
            String newSectionName = checkName.substring(0, 1).toUpperCase();

            return newSectionName;
		}

        return "";
	}


	public void onClickCB (View view){
        if(!isLoadingEnd) return;
		Uitils.toActivity(this, CBMainActivity.class, false);
	}

    public void onClickScanMe(View view){
        if(!isLoadingEnd) return;
        Uitils.toActivity(this, ScanMeActivity.class, false);
    }
	public void onClickImport (View view){
        if(!isLoadingEnd) return;
		Uitils.toActivity(this, AddGreyOneActivity.class, false);
	}

	public void onClickGps (View view){
        if(!isLoadingEnd) return;
		//Uitils.toActivity(this, GinkoMeActivity.class, false);
        MyApp.getInstance().SyncUpdateEntities(newSproutCount, ContactMainActivity.this);
        /*
        Intent sproutIntent = new Intent(ContactMainActivity.this, GinkoMeActivity.class);
        //Intent sproutIntent = new Intent(ContactMainActivity.this, MyGinkoMeActivity.class);
        if(newSproutCount > 0)
            sproutIntent.putExtra("isDetectedContacts", true);
        else
            sproutIntent.putExtra("isDetectedContacts", false);
        sproutIntent.putExtra("isNewDetection", false);
        startActivity(sproutIntent);
        */
	}

    public void onClickGpsOn (View view){
        if(!isLoadingEnd) return;
        //Uitils.toActivity(this, GinkoMeActivity.class, false);
        //Intent sproutIntent = new Intent(ContactMainActivity.this, GinkoMeActivity.class);
        MyApp.getInstance().SyncUpdateEntities(newSproutCount, ContactMainActivity.this);
        /*
        Intent sproutIntent = new Intent(ContactMainActivity.this, MyGinkoMeActivity.class);
        if(newSproutCount > 0)
            sproutIntent.putExtra("isDetectedContacts", true);
        else
            sproutIntent.putExtra("isDetectedContacts", false);
        sproutIntent.putExtra("isNewDetection", false);
        sproutIntent.putExtra("turnOn",true);
        startActivity(sproutIntent);
        */
    }

    public void onClickExchange (View view){
        if(!isLoadingEnd) return;
		Uitils.toActivity(this, ExchangeRequestActivity.class, false);
        /*UserLoginVO user = RuntimeContext.getUser();

        if(user == null) return;
        if(!user.getPhoneVerified())
        {
            Intent intent = new Intent(ContactMainActivity.this , GoToInviteContactScreenConfirmActivity.class);
            intent.putExtra("isFromMainContactScreen" , true);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(ContactMainActivity.this , InviteGinkoConnects.class );
            intent.putExtra("isFromMainContactScreen" , true);
            startActivity(intent);

        }*/
	}
    public void onClickGinkoConnect(View view){
        if(!isLoadingEnd) return;
        UserLoginVO user = RuntimeContext.getUser();

        if(user == null) return;
        if(!user.getPhoneVerified())
        {
            Intent intent = new Intent(ContactMainActivity.this , GoToInviteContactScreenConfirmActivity.class);
            intent.putExtra("isFromMainContactScreen" , true);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(ContactMainActivity.this , InviteGinkoConnects.class );
            intent.putExtra("isFromMainContactScreen" , true);
            startActivity(intent);

        }
    }
    public void onClickImportContacts(View view)
    {
        if(!isLoadingEnd) return;
        Uitils.toActivity(this, SyncHomeActivity.class, false);
    }

	
	public void onClickMainMenu (View view){
        if(!isLoadingEnd) return;
        Intent intent = new Intent(ContactMainActivity.this , MenuActivity.class);
        intent.putExtra("message_count" , newMessageCount);
        startActivity(intent);
	}
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.chatNavLayout:
                if(!isLoadingEnd) return;
                Uitils.toActivity(ImPreActivity.class, false);
                break;
            case R.id.btnSearch:
                if(!isLoadingEnd) return;
                Uitils.toActivity(SearchContactActivity.class, false);
                break;

            case R.id.btnInviteContact:
                if(!isLoadingEnd) return;
                UserLoginVO user = RuntimeContext.getUser();

                if(user == null) return;

                if(!user.getPhoneVerified())
                {
                    Intent intent = new Intent(ContactMainActivity.this , GoToInviteContactScreenConfirmActivity.class);
                    intent.putExtra("isFromMainContactScreen" , true);
                    startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(ContactMainActivity.this , InviteGinkoConnects.class );
                    intent.putExtra("isFromMainContactScreen" , true);
                    startActivity(intent);

                }
                break;

            case R.id.btnGroup:
                if(!isLoadingEnd) return;
                Intent groupIntent = new Intent(ContactMainActivity.this, GroupMainActivity.class);
                startActivity(groupIntent);
                break;

            case R.id.filter_entity:
                if(!isLoadingEnd) return;
                if(entityContactsCount <= 0)
                    return;
                isFavorited = false;
                currentFilterType = FilterType.Entity;
                //this.loadContacts(currentFilterType, null, null , true);
                refreshContactList();
                break;

            case R.id.filter_home:
                if(!isLoadingEnd) return;
                if(homeContactsCount <= 0)
                    return;
                isFavorited = false;
                currentFilterType = FilterType.Home;
                refreshContactList();

                break;

            case R.id.filter_work:
                if(!isLoadingEnd) return;
                if(workContactsCount <= 0)
                    return;
                isFavorited = false;
                currentFilterType = FilterType.Work;
                refreshContactList();
                break;

            case R.id.noFilter:
                if(!isLoadingEnd) return;
                isFavorited = false;
                currentFilterType = FilterType.None;
                refreshContactList();
                break;

            case R.id.btnFavorite:
                if(!isLoadingEnd) return;
                isFavorited = true;
                currentFilterType = FilterType.Favorite;
                refreshContactList();
                /*Intent favoriteIntent = new Intent(ContactMainActivity.this, FavoriteActivity.class);
                startActivity(favoriteIntent);*/
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        UserLoginVO loginVo = RuntimeContext.getUser();
        outState.putSerializable("login_info", loginVo);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        UserLoginVO loginVo = (UserLoginVO) savedInstanceState.getSerializable("login_info");

        RuntimeContext.setUser(loginVo);
    }

    @Override
    protected void onResume() {
        super.onResume();

        isUICreated = true;

        tmp_contactid = 0;

        if (MyApp.getInstance().isNetworkConnected() == false) {
            Uitils.alert("Internet connection is missing.");
            return;
        }

        this.binderLocationService();

        boolean isTileStyle = Uitils.getIsContactTileStyle(ContactMainActivity.this);

        if(adapter!=null && currentIsContactTileStyle != isTileStyle)//if tile style is changed
        {
            try
            {
                adapter.forceNewlyClear();
                adapter.notifyDataSetChanged();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            finally {
                adapter.setContactListItemStyle(isTileStyle);
                this.currentIsContactTileStyle = isTileStyle;
            }
        }

        /*mPullListView.doPullRefreshing(true, 100);  //FIXME for testing
        mHandler.postDelayed(new Runnable(){
            public void run()
            {
                mPullListView.onPullDownRefreshComplete();
                mPullListView.onPullUpRefreshComplete();
            }
        } , 300);*/

        //refresh current list
        if(!mIsStart)
            refreshContactList();

        if(mIsStart)
            mIsStart = false;

        if(contactChangeReceiver == null)
            this.contactChangeReceiver = new ContactChangeReceiver();
        if(exchangeRequestReceiver == null)
            this.exchangeRequestReceiver = new ExchangeRequestReceiver();
        if(entityRemoveReceiver == null)
            this.entityRemoveReceiver = new EntityRemoveReceiver();

        if (this.contactChangeReceiver != null && isContactChangeReceiverRegistered == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactChangeReceiver, msgReceiverIntent);
            isContactChangeReceiverRegistered = true;
        }

        if(this.exchangeRequestReceiver != null && isExchangeRequestReceiverRegistered == false)
        {
            IntentFilter exchangeRequestReceiver = new IntentFilter();
            exchangeRequestReceiver.addAction("android.intent.action.EXCHANGE_REQUEST");
            registerReceiver(this.exchangeRequestReceiver, exchangeRequestReceiver);
            isExchangeRequestReceiverRegistered = true;
        }

        if(this.entityRemoveReceiver != null && isEntityRemoveReceiverRegistered == false)
        {
            IntentFilter receiveChange = new IntentFilter();
            receiveChange.addAction("android.intent.action.ENTITY_REMOVED");
            this.registerReceiver(this.entityRemoveReceiver, receiveChange);
            isEntityRemoveReceiverRegistered = true;
        }

        MyApp.getInstance().getSyncUpdatedContacts(Uitils.getLastSyncTime(getApplicationContext() , RuntimeContext.getUser().getUserId()));

        if(newSproutCount>0)
        {
            txtSproutBadge.setVisibility(View.VISIBLE);
            txtSproutBadge.setText(String.valueOf(newSproutCount));
        }
        else {
            txtSproutBadge.setVisibility(View.GONE);
        }
        if(newMessageCount>0)
        {
            txtMessageBadge.setVisibility(View.VISIBLE);
            txtMessageBadge.setText(String.valueOf(newMessageCount));
        }
        else {
            txtMessageBadge.setVisibility(View.INVISIBLE);
        }
        if(newExchangedRequestCount >0)
        {
            txtExchangeRequestBadge.setVisibility(View.VISIBLE);
            txtExchangeRequestBadge.setText(String.valueOf(newExchangedRequestCount));
        }
        else
        {
            txtExchangeRequestBadge.setVisibility(View.GONE);
        }
        if(RuntimeContext.getUser() != null)
        {
            //refresh sprout setting
            isSproutOn = RuntimeContext.getUser().getLocationOn();
            refreshGPSStatusUI();
        }

        if (this.imReceiver != null && isImReceiverRegistered == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
            registerReceiver(this.imReceiver, msgReceiverIntent);
            isImReceiverRegistered = true;
        }

        getContactSummaries();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onPause() {

        isUICreated = false;

        if (mProgressHUD != null) {
            mProgressHUD.dismiss();
            mProgressHUD = null;
        }

        super.onPause();

        if (MyApp.getInstance().isNetworkConnected() == false) {
            Uitils.alert("Internet connection is missing.");
            return;
        }

        if (this.contactChangeReceiver != null && isContactChangeReceiverRegistered == true) {
            unregisterReceiver(this.contactChangeReceiver);
            isContactChangeReceiverRegistered = false;
        }

        if(this.exchangeRequestReceiver != null && isExchangeRequestReceiverRegistered == true)
        {
            unregisterReceiver(exchangeRequestReceiver);
            isExchangeRequestReceiverRegistered = false;
        }

        if(this.entityRemoveReceiver != null && isEntityRemoveReceiverRegistered == true)
        {
            unregisterReceiver(entityRemoveReceiver);
            isEntityRemoveReceiverRegistered = false;
        }

        if(sproutService != null)//service binded
            sproutService.unregisterSproutActionListener(this);

        unbindService(serviceConnection);

        if (this.imReceiver != null && isImReceiverRegistered == true) {
            unregisterReceiver(this.imReceiver);
            isImReceiverRegistered = false;
        }
    }

    private void showLocationServiceSettingAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactMainActivity.this);
        builder.setMessage(getResources().getString(R.string.gps_network_not_enabled));
        builder.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void refreshGPSStatusUI() {
        if (isSproutOn) {
            btnSprout.setImageResource(R.drawable.satellite_on);
            btnThumb.setImageResource(R.drawable.fingerprint_disabled);
        } else {
            btnSprout.setImageResource(R.drawable.satellite_off);
            btnThumb.setImageResource(R.drawable.fingerprint_enabled);
        }
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
        ContactMainActivity.instance = null;
	}

    private void getContactSummaries()
    {
        //call User/contact/summary to get new events
        UserRequest.getContactSummary(new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    //sample response
                    //{"xcg_req_num":0,"not_xcg_sprout_num":0,"new_chat_msg_num":1,"contact_counts":{"work":2,"home":3,"entity":1},"all_cb_valid":true}

                    JSONObject jsonObject = response.getData();
                    newSproutCount = jsonObject.optInt("not_xcg_sprout_num", 0);
                    newMessageCount = jsonObject.optInt("new_chat_msg_num", 0);
                    newExchangedRequestCount = jsonObject.optInt("xcg_req_num", 0);
                    if (isUICreated) {
                        if (newSproutCount > 0) {
                            txtSproutBadge.setVisibility(View.VISIBLE);
                            txtSproutBadge.setText(String.valueOf(newSproutCount));
                        } else {
                            txtSproutBadge.setVisibility(View.GONE);
                        }
                        if (newMessageCount > 0) {
                            txtMessageBadge.setVisibility(View.VISIBLE);
                            txtMessageBadge.setText(String.valueOf(newMessageCount));
                        } else {
                            txtMessageBadge.setVisibility(View.INVISIBLE);
                        }
                        if (newExchangedRequestCount > 0) {
                            txtExchangeRequestBadge.setVisibility(View.VISIBLE);
                            txtExchangeRequestBadge.setText(String.valueOf(newExchangedRequestCount));
                        } else {
                            txtExchangeRequestBadge.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }

    private void detectNearFriends(final String searchString , boolean bProgressShow) {
        SpoutRequest.detectNearUsers(3, searchString, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(
                    JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    List<JSONObject> detects = Uitils.toJsonList(response.getData().optJSONArray("friends"));
                    int cnt_detectOnlyUnExchange = 0;
                    if (detects.size() > 0) {
                        ArrayList<SproutSearchItem> detectedList = new ArrayList<SproutSearchItem>();
                        String strPreDetectedContacts = Uitils.getStringFromSharedPreferences(getApplicationContext(), "sproutDetetedContacts", "");
                        StringBuilder strDetectedContacts = new StringBuilder();
                        boolean hasNewDetection = false;

                        if (strPreDetectedContacts.equals(""))
                            hasNewDetection = true;

                        for (JSONObject json : detects) {
                            SproutSearchItem item = new SproutSearchItem(json);
                            detectedList.add(item);
                            String strDetectedItem = String.valueOf(item.contactType) + "_" + String.valueOf(item.contactOrEntityID) + "_" + String.valueOf(item.isExchanged ? 1 : 0) + ",";
                            strDetectedContacts.append(strDetectedItem);
                                    /*if(!item.isExchanged && !item.isPending)
                                        cnt_detectOnlyUnExchange++;*/
                            if (hasNewDetection == false && !strPreDetectedContacts.contains(strDetectedItem))
                                hasNewDetection = true;
                        }
                               /* //Add by lee for detect new account.
                                if(newSproutCount != cnt_detectOnlyUnExchange) {
                                    hasNewDetection = true;
                                }*/

                        Uitils.setStringToSharedPreferences(getApplicationContext(), "sproutDetetedContacts", strDetectedContacts.toString());
                        if (hasNewDetection) {
                            Intent sproutIntent = new Intent(ContactMainActivity.this, MyGinkoMeActivity.class);
                            sproutIntent.putParcelableArrayListExtra("detectedList", detectedList);
                            sproutIntent.putExtra("isNewDetection", true);
                            startActivity(sproutIntent);
                        } else {
                            if (isThumbPressing) {
                                detectNearFriends(null, false);
                                return;
                            }
                        }
                    } else {
                        //if still presses the thumb , repeat to detect contacts
                        if (isThumbPressing) {
                            detectNearFriends(null, false);
                            return;
                        }
                    }

                }
                //if thumb clicked and was detecting nearby friends , then hide progress
                if (mProgressHUD != null && mProgressHUD.isShowing()) {
                    //imgGPSOnOff.setImageResource(R.drawable.gpsoff);
                    mProgressHUD.dismiss();

                    //if(sproutService!=null)
                    //    sproutService.stopSingleLocationUpdate();
                }
            }
        }, bProgressShow);
    }

    private void saveToDB(final String searchString , boolean bProgressShow) {
        SpoutRequest.detectNearUsers(3, searchString, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(
                    JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    List<JSONObject> detects = Uitils.toJsonList(response.getData().optJSONArray("friends"));
                    if (detects.size() > 0) {
                        ArrayList<SproutSearchItem> detectedList = new ArrayList<SproutSearchItem>();
                        StringBuilder strDetectedContacts = new StringBuilder();
                        for (JSONObject json : detects) {
                            SproutSearchItem item = new SproutSearchItem(json);
                            detectedList.add(item);
                            String strDetectedItem = String.valueOf(item.contactType)+"_"+String.valueOf(item.contactOrEntityID)+"_"+String.valueOf(item.isExchanged?1:0)+",";
                            strDetectedContacts.append(strDetectedItem);
                        }
                        Uitils.setStringToSharedPreferences(getApplicationContext() , "sproutDetetedContacts" , strDetectedContacts.toString());
                    }
                }
            }
        }, bProgressShow);
    }

    private void refreshCategoryIcons()
    {
        if(currentFilterType.value() == FilterType.None.value())
        {
            btnFilterAll.setImageResource(R.drawable.btnallup);
            btnFilterEntity.setImageResource(entityContactsCount>0 ? R.drawable.btnentity : R.drawable.btnentity_disabled);
            btnFilterHome.setImageResource(homeContactsCount>0 ? R.drawable.btnhome : R.drawable.btnhome_disabled);
            btnFilterWork.setImageResource(workContactsCount>0 ? R.drawable.btnwork : R.drawable.btnwork_disabled);
            btnFilterFavorite.setImageResource(favoritesCount>0 ? R.drawable.grey_favorite : R.drawable.grey_favorite);
        }
        else if(currentFilterType.value() == FilterType.Entity.value())
        {
            btnFilterAll.setImageResource(R.drawable.btnall);
            btnFilterEntity.setImageResource(entityContactsCount>0 ? R.drawable.btnentityup : R.drawable.btnentity_disabled);
            btnFilterHome.setImageResource(homeContactsCount>0 ? R.drawable.btnhome : R.drawable.btnhome_disabled);
            btnFilterWork.setImageResource(workContactsCount>0 ? R.drawable.btnwork : R.drawable.btnwork_disabled);
            btnFilterFavorite.setImageResource(favoritesCount>0 ? R.drawable.grey_favorite : R.drawable.grey_favorite);
        }
        else if(currentFilterType.value() == FilterType.Home.value())
        {
            btnFilterAll.setImageResource(R.drawable.btnall);
            btnFilterEntity.setImageResource(entityContactsCount>0 ? R.drawable.btnentity : R.drawable.btnentity_disabled);
            btnFilterHome.setImageResource(homeContactsCount>0 ? R.drawable.btnhomeup : R.drawable.btnhome_disabled);
            btnFilterWork.setImageResource(workContactsCount>0 ? R.drawable.btnwork : R.drawable.btnwork_disabled);
            btnFilterFavorite.setImageResource(favoritesCount>0 ? R.drawable.grey_favorite : R.drawable.grey_favorite);
        }
        else if(currentFilterType.value() == FilterType.Work.value())
        {
            btnFilterAll.setImageResource(R.drawable.btnall);
            btnFilterEntity.setImageResource(entityContactsCount>0 ? R.drawable.btnentity : R.drawable.btnentity_disabled);
            btnFilterHome.setImageResource(homeContactsCount>0 ? R.drawable.btnhome : R.drawable.btnhome_disabled);
            btnFilterWork.setImageResource(workContactsCount>0 ? R.drawable.btnworkup : R.drawable.btnwork_disabled);
            btnFilterFavorite.setImageResource(favoritesCount>0 ? R.drawable.grey_favorite : R.drawable.grey_favorite_none);
        }
        else if(currentFilterType.value() == FilterType.Favorite.value())
        {
            btnFilterAll.setImageResource(R.drawable.btnall);
            btnFilterEntity.setImageResource(entityContactsCount>0 ? R.drawable.btnentity : R.drawable.btnentity_disabled);
            btnFilterHome.setImageResource(homeContactsCount>0 ? R.drawable.btnhome : R.drawable.btnhome_disabled);
            btnFilterWork.setImageResource(workContactsCount>0 ? R.drawable.btnwork : R.drawable.btnwork_disabled);
            //btnFilterFavorite.setImageResource(favoritesCount>0 ? R.drawable.purple_favorite : R.drawable.grey_favorite_none);
            btnFilterFavorite.setImageResource(favoritesCount>0 ? R.drawable.purple_favorite : R.drawable.purple_favorite);
        }
    }

    private synchronized void refreshContactList()
    {
        if(MyApp.g_contactItems == null) return;
        final Integer filterType = currentFilterType.value();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressHUD.show();
                adapter.clear();
                mListView.initView();
                ArrayList<ContactItem> contactList = new ArrayList<ContactItem>();
                entityContactsCount = 0;
                homeContactsCount = 0;
                workContactsCount = 0;
                favoritesCount =0;

                for(int i=0; i< MyApp.g_contactItems.size(); i++)
                {
                    boolean isEntityContact = false;
                    boolean isHomeContact = false;
                    boolean isWorkContact = false;

                    ContactItem item = MyApp.g_contactItems.get(i);

                    if(item.getContactType() == 1)//purple contact
                    {
                        if(item.getSharingStatus() == 1 || item.getSharingStatus() == 3)//home
                            isHomeContact = true;
                        if(item.getSharingStatus() == 2 || item.getSharingStatus() == 3)//wrok
                            isWorkContact = true;
                    }
                    else if(item.getContactType() == 2)//grey contact
                    {
                        if(item.getGreyType() == 1)
                            isHomeContact = true;
                        if(item.getGreyType() == 2)
                            isWorkContact = true;
                        if(item.getGreyType() == 0)
                            isEntityContact = true;
                    }
                    else if(item.getContactType() == 3) // entity contact
                    {
                        isEntityContact = true;
                    }

                    if(item.getIsFavorite())
                        favoritesCount++;
                    if(isEntityContact)
                        entityContactsCount++;
                    if(isHomeContact)
                        homeContactsCount++;
                    if(isWorkContact)
                        workContactsCount++;

                    if(filterType == FilterType.None.value()) {
                        contactList.add(item);
                    }
                    else if(filterType == FilterType.Home.value()) //if only get home shared
                    {
                        if(item.getContactType() == 3) continue;
                        if(isHomeContact)
                            contactList.add(item);
                    }
                    else if(filterType == FilterType.Work.value()) //if only get home shared
                    {
                        if(item.getContactType() == 3) continue;
                        if(isWorkContact)
                           contactList.add(item);
                    }
                    else if(filterType == FilterType.Entity.value())
                    {
                        if(isEntityContact)
                            contactList.add(item);
                    }
                    if(filterType == FilterType.Favorite.value())
                    {
                        if(item.getIsFavorite())
                            contactList.add(item);
                    }
                }
                boolean hasMoreData = contactList.size() != 0;
                sortContactsAndMakeSectionHeaders(contactList);

                synchronized (lockObj) {
                    adapter.addAll(contactList);
                    //adapter.notifyDataSetChanged();
                }

                refreshCategoryIcons();
                //adapter.notifyDataSetChanged();
                if (adapter.getCount() == 0)
                    mPullListView.setPullRefreshEnabled(false);
                else
                    mPullListView.setPullRefreshEnabled(true);

                mPullListView.setHasMoreData(hasMoreData);
                setLastUpdateTime();

                if (adapter.getCount() == 0 && filterType == FilterType.None.value()) {
                    shortCutButtonLayout.setVisibility(View.VISIBLE);
                    contactListLayout.setVisibility(View.INVISIBLE);
                    alphabetScrollbar.setVisibility(View.GONE);
                } else {
                    shortCutButtonLayout.setVisibility(View.INVISIBLE);
                    contactListLayout.setVisibility(View.VISIBLE);
                    alphabetScrollbar.setVisibility(View.VISIBLE);
                }

                if(favoritesCount <= 0 && isFavorited) {
                    noteNoFavoriteCotnact.setVisibility(View.VISIBLE);
                    alphabetScrollbar.setVisibility(View.GONE);
                }
                else {
                    noteNoFavoriteCotnact.setVisibility(View.INVISIBLE);
                    alphabetScrollbar.setVisibility(View.VISIBLE);
                }
                // when fail, also let the header/footer hide.
                mPullListView.onPullDownRefreshComplete();
                mPullListView.onPullUpRefreshComplete();

                progressHUD.cancel();
                isLoadingEnd = true;
            }
        }, 300);

        saveToDB("", false);
    }

    public class ContactChangeReceiver extends BroadcastReceiver {
        public ContactChangeReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New Contact Change");

            refreshContactList();

        }
    }

    public class EntityRemoveReceiver extends BroadcastReceiver {
        public EntityRemoveReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String str_id = bundle.getString("entity_id", "0");
            int contact_id = Integer.parseInt(str_id);

            MyApp.getInstance().getContactsModel().deleteContactWithContactId(contact_id);
            MyApp.getInstance().getGinkoModel().deleteContactWithContactId(contact_id);
            refreshContactList();
        }
    }

    public class ExchangeRequestReceiver extends BroadcastReceiver {
        public ExchangeRequestReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New exchange request");

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    getContactSummaries();
                }
            });
        }
    }

    public class ImMsgReceiver extends BroadcastReceiver {
        public ImMsgReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New message");

            getContactSummaries();

        }
    }
}
