package com.ginko.activity.directory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.ginko.activity.menu.MenuActivity;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.common.Uitils;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.DirectoryVO;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.widget.RelativeLayout.*;

public class DirAdminPreviewActivity extends MyBaseFragmentActivity implements View.OnClickListener{
    public static final int DIRECTORY_EDIT_NUM = 101;
    private final int SHARE_YOUR_LEAF_ACTIVITY = 2;
    /* UI Variables*/
    private ImageButton btnBack;
    private Button btnDelete , btnEdit, btnLeftEdit, btnDone;
    private ImageButton btnInviteContact;
    private RelativeLayout headerlayout;
    private TextView txtDirname, textViewTitle;
    private LinearLayout domainLayout, methodTypeLayout;

    private ListView domainListView;
    private CheckBox chkPrivate, chkPublic, chkAuto, chkManual;
    private CustomNetworkImageView imgDirProfile;
    private ImageLoader imgLoader;

    //2016.9.21 Layout Update for Big Profile Show
    private CustomNetworkImageView tiledProfilePhoto;
    private RelativeLayout hiddenLayout;
    private ImageView imgDimClose;

    private DomainListAdapter mAdapter;
    private ArrayList<String> domainList = new ArrayList<String>();

    /* Variables */
    private DirectoryVO dirInfo;
    private int directoryId;
    private boolean isCreate = false;
    private int nPrivilege = 0;
    private boolean isInviteGo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dir_admin_preview);

        if(savedInstanceState != null)
        {
            this.dirInfo = (DirectoryVO) savedInstanceState.getSerializable("directory");
            this.isCreate = savedInstanceState.getBoolean("isCreate", false);
            this.isInviteGo = savedInstanceState.getBoolean("isInviteGo", false);
            this.nPrivilege = savedInstanceState.getInt("privilege", 0);
        }
        else
        {
            this.dirInfo = (DirectoryVO) this.getIntent().getSerializableExtra("directory");
            this.isCreate = getIntent().getBooleanExtra("isCreate", false);
            this.isInviteGo = getIntent().getBooleanExtra("isInviteGo", false);
            this.nPrivilege = getIntent().getIntExtra("privilege", 0);
        }

        getUIObjects();

        if (dirInfo != null)
            initValues();
        else
        {
            this.directoryId = getIntent().getIntExtra("directoryId", 0);
            DirectoryRequest.getDirectoryDetail(directoryId, new ResponseCallBack<DirectoryVO>() {
                @Override
                public void onCompleted(JsonResponse<DirectoryVO> response) {
                    if (response.isSuccess()) {
                        dirInfo = response.getData();
                        if (dirInfo != null)
                            initValues();
                    }
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        int new_boardId = intent.getIntExtra("directoryId", 0);

        Intent notificationIntent = new Intent(this, DirAdminPreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("directory", null);
        notificationIntent.putExtras(bundle);
        notificationIntent.putExtra("directoryId", new_boardId);
        startActivity(notificationIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDone:
            case R.id.btnBack:
                {
                    Intent intent = new Intent(DirAdminPreviewActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.btnLeftEdit:
            case R.id.btnEdit:
                {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("directory", this.dirInfo);
                    bundle.putBoolean("isCreate", false);
                    bundle.putBoolean("isNewDirectory", isCreate);
                    Intent intent = new Intent(DirAdminPreviewActivity.this, DirSettingsActivity.class);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, DIRECTORY_EDIT_NUM);
                }
                break;
            case R.id.btnDelete:
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DirAdminPreviewActivity.this);
                    builder.setTitle("GINKO");
                    builder.setMessage(getResources().getString(R.string.str_confirm_directory_delete));
                    builder.setNegativeButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO
                            DirectoryRequest.deleteDirectory(dirInfo.getId(), new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if (response.isSuccess()) {
                                        Intent intent = new Intent(DirAdminPreviewActivity.this, MenuActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {

                                    }
                                }
                            });
                            dialog.dismiss();
                        }
                    });

                    builder.setPositiveButton(R.string.alert_no_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
            case R.id.btnInviteContact:
                Intent inviteContactIntent = new Intent(DirAdminPreviewActivity.this , DirAdminInviteActivity.class);
                inviteContactIntent.putExtra("directoryId", this.dirInfo.getId());
                Bundle bundle = new Bundle();
                bundle.putBoolean("isCreate", isCreate);
                inviteContactIntent.putExtras(bundle);
                startActivity(inviteContactIntent);
                break;
        }
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        domainLayout = (LinearLayout)findViewById(R.id.domainLayout);
        methodTypeLayout = (LinearLayout)findViewById(R.id.methodTypeLayout);
        btnDelete = (Button)findViewById(R.id.btnDelete); btnDelete.setOnClickListener(this);
        headerlayout = (RelativeLayout)findViewById(R.id.headerlayout);
        btnEdit = (Button)findViewById(R.id.btnEdit); btnEdit.setOnClickListener(this);
        btnLeftEdit = (Button)findViewById(R.id.btnLeftEdit); btnLeftEdit.setOnClickListener(this);
        btnDone = (Button)findViewById(R.id.btnDone); btnDone.setOnClickListener(this);

        btnBack = (ImageButton)findViewById(R.id.btnBack); btnBack.setOnClickListener(this);
        textViewTitle = (TextView)findViewById(R.id.textViewTitle);
        txtDirname = (TextView)findViewById(R.id.txtName);

        btnInviteContact = (ImageButton)findViewById(R.id.btnInviteContact);
        RelativeLayout.LayoutParams rel_invite = (RelativeLayout.LayoutParams)btnInviteContact.getLayoutParams();
        if (isCreate)
            rel_invite.addRule(RelativeLayout.RIGHT_OF, btnLeftEdit.getId());
        else
            rel_invite.addRule(RelativeLayout.RIGHT_OF, btnBack.getId());
        btnInviteContact.setLayoutParams(rel_invite);
        btnInviteContact.setOnClickListener(this);

        //2016.9.21 Update
        tiledProfilePhoto = (CustomNetworkImageView)findViewById(R.id.tileProfileImage);
        hiddenLayout = (RelativeLayout)findViewById(R.id.hiddenLayout);
        imgDimClose = (ImageView)findViewById(R.id.imgDimClose);
        tiledProfilePhoto.setDefaultImageResId(R.drawable.directory_add_logo);

        hiddenLayout.setVisibility(View.GONE);

        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        imgDirProfile = (CustomNetworkImageView)findViewById(R.id.imgDirLogo);
        imgDirProfile.setDefaultImageResId(R.drawable.directory_add_logo);

        if (isCreate)
        {
            btnDone.setVisibility(View.VISIBLE);
            btnLeftEdit.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            headerlayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            textViewTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnLeftEdit.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnDone.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnInviteContact.setImageResource(R.drawable.entity_invite_white);
        } else
        {
            btnDone.setVisibility(View.GONE);
            btnLeftEdit.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.VISIBLE);

            headerlayout.setBackgroundColor(getResources().getColor(R.color.green_top_titlebar_color));
        }

        chkPrivate = (CheckBox)findViewById(R.id.chkbox_private);
        chkPublic = (CheckBox)findViewById(R.id.chkbox_public);
        chkAuto = (CheckBox)findViewById(R.id.chkbox_auto);
        chkManual = (CheckBox)findViewById(R.id.chkbox_manual);

        domainListView = (ListView) findViewById(R.id.domainList);
        mAdapter = new DomainListAdapter(this);
        domainListView.setAdapter(mAdapter);

        if(domainList == null) {
            domainList = new ArrayList<String>();
        }
        mAdapter.setListItems(domainList);

        chkPrivate.setClickable(false);
        chkPublic.setClickable(false);
        chkAuto.setClickable(false);
        chkManual.setClickable(false);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("directory", this.dirInfo);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DirAdminPreviewActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMyApp.setCurrentActivity(this);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case DIRECTORY_EDIT_NUM:
                    dirInfo = (DirectoryVO) data.getSerializableExtra("directory");
                    initValues();
                    break;
                case SHARE_YOUR_LEAF_ACTIVITY:
                    showCongratulations();
                    break;
            }
        }

    }

    private void initValues()
    {
        if (dirInfo == null) return;

        if(dirInfo.getProfileImage() != null) {
            imgDirProfile.refreshOriginalBitmap();
            imgDirProfile.setImageUrl(dirInfo.getProfileImage(), imgLoader);
            imgDirProfile.invalidate();

            tiledProfilePhoto.refreshOriginalBitmap();
            tiledProfilePhoto.setImageUrl(dirInfo.getProfileImage(), imgLoader);
            tiledProfilePhoto.invalidate();
        }

        imgDirProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hiddenLayout.setVisibility(View.VISIBLE);
                fadeInAndShowImage(tiledProfilePhoto);
            }
        });

        imgDimClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hiddenLayout.setVisibility(View.GONE);
                tiledProfilePhoto.setVisibility(View.INVISIBLE);
            }
        });

        hiddenLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    if (tiledProfilePhoto.getAnimation() != null && !tiledProfilePhoto.getAnimation().hasEnded())
                        return false;

                    hiddenLayout.setVisibility(View.GONE);
                }
                return false;
            }
        });

        txtDirname.setText(dirInfo.getName());
        updateUIFromMethod(dirInfo.getPrivilege(), dirInfo.getApproveMode());
        if (dirInfo.getDomains() != null && !dirInfo.getDomains().equals(""))
        {
            domainList.clear();
            List<String> parseList = Arrays.asList(dirInfo.getDomains().split(","));
            domainList.addAll(parseList);
            mAdapter.notifyDataSetChanged();
        }

        if (isCreate == true)
        {
            showJoinDialog();
            btnDone.setVisibility(View.INVISIBLE);
        }

        if (isInviteGo)
        {
            Intent inviteContactIntent = new Intent(DirAdminPreviewActivity.this , DirAdminInviteActivity.class);
            inviteContactIntent.putExtra("directoryId", this.dirInfo.getId());
            Bundle bundle = new Bundle();
            bundle.putBoolean("isCreate", isCreate);
            bundle.putBoolean("isInviteGo", isInviteGo);
            inviteContactIntent.putExtras(bundle);
            startActivity(inviteContactIntent);
        }
    }

    private void showJoinDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(DirAdminPreviewActivity.this);
        builder.setTitle("Confirm");
        builder.setMessage(getResources().getString(R.string.ask_join_directory));
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int id) {
            //TODO
            DirectoryRequest.checkDirectoryExist(dirInfo.getName(), new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) {
                    if (response.isSuccess()) {
                        JSONObject object = response.getData();
                        String directoryId = "";
                        try {
                            directoryId = object.getString("id");
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        Intent contactSharingSettingIntent = new Intent(DirAdminPreviewActivity.this, ShareYourLeafActivity.class);
                        contactSharingSettingIntent.putExtra("contactID", "0");
                        contactSharingSettingIntent.putExtra("contactFullname", dirInfo.getName());
                        contactSharingSettingIntent.putExtra("isDirectory", true);
                        contactSharingSettingIntent.putExtra("directoryID", Integer.valueOf(directoryId));
                        contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                        contactSharingSettingIntent.putExtra("isInviteContact", false);
                        contactSharingSettingIntent.putExtra("isPendingRequest", false);
                        contactSharingSettingIntent.putExtra("StartActivity", "ContactMain");

                        startActivityForResult(contactSharingSettingIntent, SHARE_YOUR_LEAF_ACTIVITY);
                        finish();
                        dialog.dismiss();
                    } else {
                        Uitils.alert(DirAdminPreviewActivity.this, response.getErrorMessage());
                        dialog.dismiss();
                    }
                }
            });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
                Intent intent = new Intent(DirAdminPreviewActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showCongratulations()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(DirAdminPreviewActivity.this);
        TextView myMsg = new TextView(this);
        String msgText = "You joined the Ginko \ndirectory.  A directory icon \nwill appear in Groups.";
        msgText = msgText.replace("Ginko", dirInfo.getName());
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
                Intent intent = new Intent(DirAdminPreviewActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateUIFromMethod(Integer mPublic, Integer mApproved)
    {
        if (mPublic == 1)
        {
            chkPrivate.setChecked(false);
            chkPublic.setChecked(true);
            chkAuto.setVisibility(View.GONE);
            chkManual.setVisibility(View.GONE);
            domainLayout.setVisibility(View.GONE);
            domainListView.setVisibility(View.GONE);
            methodTypeLayout.setVisibility(View.INVISIBLE);
        } else
        {
            chkPrivate.setChecked(true);
            chkPublic.setChecked(false);
            chkAuto.setVisibility(View.VISIBLE);
            chkManual.setVisibility(View.VISIBLE);
            methodTypeLayout.setVisibility(View.VISIBLE);

            if (mApproved == 1)
            {
                chkAuto.setChecked(true);
                chkManual.setChecked(false);
                domainLayout.setVisibility(View.VISIBLE);
                domainListView.setVisibility(View.VISIBLE);
            } else
            {
                chkAuto.setChecked(false);
                chkManual.setChecked(true);
                domainLayout.setVisibility(View.GONE);
                domainListView.setVisibility(View.GONE);
            }
        }
    }

    private void fadeInAndShowImage(final CustomNetworkImageView img)
    {
        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(600);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }
}
