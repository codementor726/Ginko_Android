package com.ginko.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Network;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.ginko.activity.profiles.PurpleContactProfile;
import com.ginko.api.request.UserRequest;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.customview.PersonalProfilePreviewFieldGroupView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PersonalProfileFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "type";
    private static final String GROUP_INFO = "groupInfo";

    private String type;

    //2016.9.21 Layout Update for Big Profile Show
    private NetworkImageView tiledProfilePhoto;
    private RelativeLayout hiddenLayout;
    private ImageView imgDimClose;

    private View view;
    private LinearLayout containerLayout;
    private LinearLayout showMoreLayout;
    private NetworkImageView profilePhoto;
    //private ImageView profilePhoto;

    private TextView txtUserName,showMoreTxt;
    private CustomNetworkImageView imgWallpaper , imgVideoThumb;
    private ImageView imgProfileLock, imgProfileFavorite,showMoreImg;
    private RelativeLayout videoLayout;

    private UserUpdateVO groupInfo;
    private ImageLoader imgLoader;
    private boolean isDirectory = false;

    private boolean isUICreated = false;
    private boolean isFavoriteSel = false;
    private boolean isChanged = false;
    private boolean isShowMore = false;

    private int userId = 0;
    private int contactID = 0;

    private ArrayList<UserProfileVO> nameFields = null ,
            companyFields = null ,
            mobileFields = null,
            phoneFields = null,
            emailFields = null ,
            addressFields = null ,
            faxFields = null,
            birthdayFields = null ,
            socialNetworkFields = null ,
            websiteFields = null,
            customFields = null;

    private PersonalProfilePreviewFieldGroupView nameFieldView = null; //name or title
    private PersonalProfilePreviewFieldGroupView companyFieldView = null;
    private PersonalProfilePreviewFieldGroupView mobileFieldView = null;
    private PersonalProfilePreviewFieldGroupView emailFieldView = null;
    private PersonalProfilePreviewFieldGroupView addressFieldView = null;
    private PersonalProfilePreviewFieldGroupView faxFieldView = null;
    private PersonalProfilePreviewFieldGroupView birthdayFieldView = null;
    private PersonalProfilePreviewFieldGroupView socialNetworksFieldView = null;
    private PersonalProfilePreviewFieldGroupView websiteFieldView = null;
    private PersonalProfilePreviewFieldGroupView customFieldView = null;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param type Parameter 1.
     * @return A new instance of fragment UserProfileFragment.
     */
    public static PersonalProfileFragment newInstance(String type , UserUpdateVO groupINFO, Boolean _isDirectory) {
        PersonalProfileFragment fragment = new PersonalProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, type);
        args.putSerializable(GROUP_INFO, groupINFO);
        fragment.setArguments(args);
        return fragment;
    }


    public PersonalProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_PARAM1);
            if(groupInfo == null)
                groupInfo = (UserUpdateVO)getArguments().getSerializable(GROUP_INFO);
        }

        Bundle extra = getArguments();
        userId = extra.getInt("isUser");
        isDirectory = extra.getBoolean("isDirectory", false);
        contactID = extra.getInt("contactID");
        //isFavoriteSel = extra.getBoolean("isFavorite");

        isUICreated = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_personal_profile, container, false);

        containerLayout = (LinearLayout) view.findViewById(R.id.container);
        showMoreLayout = (LinearLayout) view.findViewById(R.id.showMorecontainer);
        showMoreImg = (ImageView)view.findViewById(R.id.showMoreImg);
        showMoreTxt = (TextView)view.findViewById(R.id.showMoreTxt);
        showMoreLayout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                isShowMore = !isShowMore;
                if (isShowMore == true){
                    showMoreImg.setImageResource(R.drawable.showlessbutton);
                    showMoreTxt.setText("Show less");
                    init(groupInfo);
                }else
                {
                    showMoreImg.setImageResource(R.drawable.showmorebutton);
                    showMoreTxt.setText("Show more");
                    init(groupInfo);
                }
            }
        });

        //2016.9.21 Update
        tiledProfilePhoto = (NetworkImageView)getActivity().findViewById(R.id.tileProfileImage);
        hiddenLayout = (RelativeLayout)getActivity().findViewById(R.id.hiddenLayout);
        imgDimClose = (ImageView)getActivity().findViewById(R.id.imgDimClose);

        if (hiddenLayout != null)
        {
            hiddenLayout.setVisibility(View.GONE);
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
        }

        profilePhoto = (NetworkImageView)view.findViewById(R.id.imgProfilePhoto);
        //profilePhoto.setImageResource(R.drawable.profile_preview_default_icon);
        profilePhoto.setDefaultImageResId(R.drawable.profile_preview_default_icon);

        imgWallpaper = (CustomNetworkImageView)view.findViewById(R.id.imgWallpaper);
        imgWallpaper.setDefaultImageResId(R.drawable.default_wallpaper);

        txtUserName = (TextView)view.findViewById(R.id.txtUserName);
        txtUserName.setText("");

        try {
            reloadPersonalInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //reloadPersonalInfo();
    }

    private void reloadPersonalInfo() throws IOException {
        /*
        UserRequest.getContactDetailJSON(String.valueOf(contactID), "1", new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    if (userId != 1) {   //is not user(owner)
                        try {
                            JSONObject jsonObject = response.getData();
                            isFavoriteSel = jsonObject.getBoolean("is_favorite");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } */

                imgProfileLock = (ImageView) view.findViewById(R.id.imgProfileLock);
                imgProfileFavorite = (ImageView) view.findViewById(R.id.imgProfileFavorite);
                if (isDirectory)
                    imgProfileFavorite.setVisibility(View.GONE);

                imgProfileFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isDirectory) return;

                        if (!isFavoriteSel) {
                            isFavoriteSel = true;
                            UserRequest.setFavoriteContacts(contactID, "1", new ResponseCallBack<JSONObject>() {
                                @Override
                                public void onCompleted(JsonResponse<JSONObject> response) {
                                    imgProfileFavorite.setImageResource(R.drawable.img_favorite);
                                    updateAll();

                                }
                            });
                        } else {
                            isFavoriteSel = false;
                            UserRequest.unsetFavoriteContacts(contactID, "1", new ResponseCallBack<JSONObject>() {
                                @Override
                                public void onCompleted(JsonResponse<JSONObject> response) {
                                    imgProfileFavorite.setImageResource(R.drawable.img_unfavorite);
                                    updateAll();
                                }
                            });
                        }
                    }
                });

                imgProfileLock.setImageResource(R.drawable.personal_profile_unlocked_white);
                if (userId == 1) {
                    imgProfileLock.setVisibility(View.VISIBLE);
                    imgProfileFavorite.setVisibility(View.GONE);
                } else {
                    imgProfileLock.setVisibility(View.GONE);
                    if (!isDirectory)
                    {
                        imgProfileFavorite.setVisibility(View.VISIBLE);
                        if (isFavoriteSel)
                            imgProfileFavorite.setImageResource(R.drawable.img_favorite);
                        else
                            imgProfileFavorite.setImageResource(R.drawable.img_unfavorite);
                    }
                }

                videoLayout = (RelativeLayout) view.findViewById(R.id.videoLayout);
                imgVideoThumb = (CustomNetworkImageView) view.findViewById(R.id.imgVideoThumb);

                videoLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (groupInfo == null)
                            return;
                        if (groupInfo.getVideo() == null || groupInfo.getVideo() == null || groupInfo.getVideo().getVideo_url().equals(""))
                            return;
                        try {
                            Intent videoPlayIntent = new Intent(Intent.ACTION_VIEW);
                            videoPlayIntent.setDataAndType(Uri.parse(groupInfo.getVideo().getVideo_url()), "video/*");
                            getActivity().startActivity(videoPlayIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                isUICreated = true;
                init(groupInfo);
                //}
          //  }
       // });
    }
    private void updateAll()
    {
        PurpleContactProfile pActivity = (PurpleContactProfile)getActivity();
        pActivity.refreshFragments(type);
        pActivity.sendFavoriteValue(isFavoriteSel, type);
    }

    public void setFavouriteValue(boolean favourite) {
        isFavoriteSel = favourite;
    }

    public void setFavorite(boolean favrite) {
        isFavoriteSel = favrite;
        if(isFavoriteSel)
            imgProfileFavorite.setImageResource(R.drawable.img_favorite);
        else
            imgProfileFavorite.setImageResource(R.drawable.img_unfavorite);
    }

    public boolean getFavorite() {return isFavoriteSel;}

    private void updateLockButton()
    {
        if(this.groupInfo == null) return;
        TcImageVO wallPaperImage = groupInfo.getWallpapaerImage();
        if (this.groupInfo.isPublic()){
            if(wallPaperImage != null && !wallPaperImage.getUrl().equals(""))
                imgProfileLock.setImageResource(R.drawable.personal_profile_unlocked_white);
            else
                imgProfileLock.setImageResource(R.drawable.personal_profile_unlocked_white);
        }else{
            if(wallPaperImage != null && !wallPaperImage.getUrl().equals(""))
                imgProfileLock.setImageResource(R.drawable.personal_profile_preview_locked_white);
            else
                imgProfileLock.setImageResource(R.drawable.personal_profile_preview_locked_white);
        }

        imgProfileLock.invalidate();
    }

    public void init(UserUpdateVO info){
        this.groupInfo = info;

        if(isUICreated == false) return;

        if(this.imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        if(this.groupInfo == null || this.groupInfo.getFields().size() < 1) {
            profilePhoto.refreshOriginalBitmap();
            profilePhoto.setImageUrl("", imgLoader);
            profilePhoto.setDefaultImageResId(R.drawable.profile_preview_default_icon);

            //profilePhoto.setImageBitmap();
            //Glide.clear(profilePhoto);
            //profilePhoto.setImageResource(R.drawable.profile_preview_default_icon);
            profilePhoto.invalidate();

            //Glide.with(getContext()).load(R.drawable.profile_preview_default_icon).bitmapTransform(new CropCircleTransformation(getContext())).into(profilePhoto);
            /*Glide.with(getContext()).load(R.drawable.profile_preview_default_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.IMMEDIATE)
                    .bitmapTransform(new CropCircleTransformation(getContext()))
                    .into(profileImage);*/
            //profilePhoto.invalidate();

            imgWallpaper.refreshOriginalBitmap();
            imgWallpaper.setImageUrl("", imgLoader);
            imgWallpaper.invalidate();

            txtUserName.setText("");

            updateLockButton();

            containerLayout.removeAllViews();
            return;
        }

        profilePhoto.refreshOriginalBitmap();
        profilePhoto.setImageUrl(this.groupInfo.getProfileImage(), imgLoader);
        profilePhoto.invalidate();


        //rotateImage(this.groupInfo.getProfileImage());
        //Glide.with(this).load(this.groupInfo.getProfileImage()).bitmapTransform(new CropCircleTransformation(getContext())).into(profilePhoto);
        //profilePhoto.invalidate();
        /*Glide.clear(profilePhoto);
        Glide.with(getContext()).load(this.groupInfo.getProfileImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.IMMEDIATE)
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .into(profilePhoto);*/
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hiddenLayout != null) {
                    hiddenLayout.setVisibility(View.VISIBLE);
                    fadeInAndShowImage(tiledProfilePhoto);
                }
            }
        });

        if (imgDimClose != null)
        {
            imgDimClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hiddenLayout != null) {
                        hiddenLayout.setVisibility(View.GONE);
                        tiledProfilePhoto.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        if(this.groupInfo.getVideo() == null || this.groupInfo.getVideo().getThumbUrl().equals(""))
        {
            videoLayout.setVisibility(View.GONE);
            showMoreLayout.setVisibility(View.GONE);
            isShowMore = true;
            imgVideoThumb.refreshOriginalBitmap();
            imgVideoThumb.setImageUrl("", imgLoader);
            imgVideoThumb.invalidate();
        }
        else
        {
            if (this.groupInfo.getFields().size() <= 4)
            {
                showMoreLayout.setVisibility(View.GONE);
                isShowMore = true;
            }
            else
                showMoreLayout.setVisibility(View.VISIBLE);

            imgVideoThumb.setImageUrl(this.groupInfo.getVideo().getThumbUrl(), imgLoader);
            imgVideoThumb.invalidate();
            videoLayout.setVisibility(View.VISIBLE);
        }

        TcImageVO wallpaperImage = null;
        if((wallpaperImage = groupInfo.getWallpapaerImage()) !=null) {
            imgWallpaper.setImageUrl(wallpaperImage.getUrl() , imgLoader);
        }
        else
        {
            imgWallpaper.setImageUrl("" , imgLoader);
            imgWallpaper.invalidate();
        }

        String strUserName = groupInfo.getProfileUserName();

        txtUserName.setText(strUserName);

        updateLockButton();

        containerLayout.removeAllViews();

        nameFields = null;
        companyFields = null;
        mobileFields = null;
        phoneFields = null;
        emailFields = null;
        addressFields = null;
        faxFields = null;
        birthdayFields = null;
        socialNetworkFields = null;
        websiteFields = null;
        customFields = null;

        nameFieldView = null; //name or title
        companyFieldView = null;
        mobileFieldView = null;
        emailFieldView = null;
        addressFieldView = null;
        faxFieldView = null;
        birthdayFieldView = null;
        socialNetworksFieldView = null;
        websiteFieldView = null;
        customFieldView = null;

        addEachFieldInfo();

        //sort the fields by field name like email ,email#2 , email#3
        FieldNameComparator fieldNameComparator = new FieldNameComparator();

        try {
                Collections.sort(mobileFields, fieldNameComparator);
        } catch (Exception e) {
                e.printStackTrace();
        }
        try {
            Collections.sort(emailFields, fieldNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Collections.sort(addressFields, fieldNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Collections.sort(socialNetworkFields, fieldNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Collections.sort(customFields, fieldNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Context mContext = (Context)getActivity();
        //Context mContext = getContext();
        int i = 0;
        boolean bExist = false;

        while (!bExist)
        {
            if(companyFields!= null) {
                companyFieldView = new PersonalProfilePreviewFieldGroupView(companyFields, companyFields.get(0).getFieldName(), mContext, i, isShowMore);
                containerLayout.addView(companyFieldView);
                i+=companyFields.size();
            }

            if (i > 2 && !isShowMore) break;

            if(nameFields != null) {
                nameFieldView = new PersonalProfilePreviewFieldGroupView(nameFields, nameFields.get(0).getFieldName(), mContext, i, false);
                containerLayout.addView(nameFieldView);
            }

            if(mobileFields!= null) {

                mobileFieldView = new PersonalProfilePreviewFieldGroupView(mobileFields, mobileFields.get(0).getFieldName(), mContext, i, isShowMore);
                containerLayout.addView(mobileFieldView);
                i+=mobileFields.size();
            }

            if (i > 2 && !isShowMore) break;

            if(emailFields!= null) {
                emailFieldView = new PersonalProfilePreviewFieldGroupView(emailFields, emailFields.get(0).getFieldName(), mContext, i, isShowMore);
                containerLayout.addView(emailFieldView);
                i+=emailFields.size();
            }

            if (i > 2 && !isShowMore) break;

            if(addressFields!= null) {
                addressFieldView = new PersonalProfilePreviewFieldGroupView(addressFields, addressFields.get(0).getFieldName(), mContext, i, isShowMore);
                containerLayout.addView(addressFieldView);
                i+=addressFields.size();
            }

            if (i > 2 && !isShowMore) break;

            if(faxFields!= null) {
                faxFieldView = new PersonalProfilePreviewFieldGroupView(faxFields, faxFields.get(0).getFieldName(), mContext, i, isShowMore);
                containerLayout.addView(faxFieldView);
                i+=faxFields.size();
            }

            if (i > 2 && !isShowMore) break;

            if(birthdayFields!= null) {
                birthdayFieldView = new PersonalProfilePreviewFieldGroupView(birthdayFields, birthdayFields.get(0).getFieldName(), mContext, i, isShowMore);
                containerLayout.addView(birthdayFieldView);
                i+=birthdayFields.size();
            }

            if (i > 2 && !isShowMore) break;

            if(socialNetworkFields!= null) {
                socialNetworksFieldView = new PersonalProfilePreviewFieldGroupView(socialNetworkFields, socialNetworkFields.get(0).getFieldName(), mContext, i, isShowMore);
                containerLayout.addView(socialNetworksFieldView);
                i+=socialNetworkFields.size();
            }

            if (i > 2 && !isShowMore) break;

            if(websiteFields!= null) {
                websiteFieldView = new PersonalProfilePreviewFieldGroupView(websiteFields, websiteFields.get(0).getFieldName(), mContext, i, isShowMore);
                containerLayout.addView(websiteFieldView);
                i+=websiteFields.size();
            }

            if (i > 2 && !isShowMore) break;

            if(customFields!= null) {
                customFieldView = new PersonalProfilePreviewFieldGroupView(customFields, customFields.get(0).getFieldName(), mContext, i, isShowMore);
                containerLayout.addView(customFieldView);
                i+=customFields.size();
            }

            break;
        }
    }

    private void fadeInAndShowImage(final NetworkImageView img)
    {
        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(600);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        img.startAnimation(fadeOut);
    }

    public boolean isLoaded(){
        return isUICreated;
    }

    private void addEachFieldInfo(){
        List<UserProfileVO> fields = groupInfo.getFields();

        for(UserProfileVO field:fields)
        {
            String strFieldName = field.getFieldName();
            String fieldType = field.getFieldType();
            if(fieldType.equals(""))
                continue;
            if (fieldType.equalsIgnoreCase("abbr"))
                continue;
            if (fieldType.equalsIgnoreCase("privilege"))
                continue;
            if (fieldType.equalsIgnoreCase("video"))
                continue;
            if (fieldType.equalsIgnoreCase("foreground"))
                continue;
            if (fieldType.equalsIgnoreCase("background"))
                continue;
            if (strFieldName.toLowerCase().contains("company")) {
                if(companyFields == null)
                    companyFields = new ArrayList<UserProfileVO>();
                companyFields.add(field);

            }else if(strFieldName.toLowerCase().contains("title"))
            {
                if(nameFields == null)
                    nameFields = new ArrayList<UserProfileVO>();
                nameFields.add(field);

            }else if (strFieldName.toLowerCase().contains("mobile") || strFieldName.toLowerCase().contains("phone")) {
                if(mobileFields == null)
                    mobileFields = new ArrayList<UserProfileVO>();
                mobileFields.add(field);

            }
            else if (strFieldName.toLowerCase().contains("email")) {
                if(emailFields == null)
                    emailFields = new ArrayList<UserProfileVO>();
                emailFields.add(field);

            }
            else if (strFieldName.toLowerCase().contains("address")) {
                if(addressFields == null)
                    addressFields = new ArrayList<UserProfileVO>();
                addressFields.add(field);

            }
            else if (strFieldName.toLowerCase().contains("fax")) {
                if(faxFields == null)
                    faxFields = new ArrayList<UserProfileVO>();
                faxFields.add(field);

            }
            else if (strFieldName.toLowerCase().contains("birthday")) {
                if(birthdayFields == null)
                    birthdayFields = new ArrayList<UserProfileVO>();
                birthdayFields.add(field);

            }
            else if (strFieldName.toLowerCase().contains("facebook") || strFieldName.toLowerCase().contains("twitter") || strFieldName.toLowerCase().contains("linkedin")) {
                if(socialNetworkFields == null)
                    socialNetworkFields = new ArrayList<UserProfileVO>();
                socialNetworkFields.add(field);
            }
            else if (strFieldName.toLowerCase().contains("website")) {
                if(websiteFields == null)
                    websiteFields = new ArrayList<UserProfileVO>();
                websiteFields.add(field);

            }
            else if (strFieldName.toLowerCase().contains("custom")) {
                if(customFields == null)
                    customFields = new ArrayList<UserProfileVO>();
                customFields.add(field);

            }
        }
    }

    class FieldNameComparator implements Comparator<UserProfileVO> {
        public FieldNameComparator()
        {
        }

        @Override
        public int compare(UserProfileVO lhs, UserProfileVO rhs) {
            return compareFieldName(lhs.getFieldName() , rhs.getFieldName());
        }

        private int compareFieldName(String leftFieldName ,String rightFieldName)
        {
            int result = 0;
            if(leftFieldName.equalsIgnoreCase(rightFieldName))
            {
                result = 0;
            }
            else
            {
                String leftFieldNamePrefix = "";
                String rightFieldNamePrefix = "";
                if(leftFieldName.contains("#"))
                    leftFieldNamePrefix = leftFieldName.substring(0, leftFieldName.indexOf("#"));
                else
                    leftFieldNamePrefix = leftFieldName;

                if(rightFieldName.contains("#"))
                    rightFieldNamePrefix = rightFieldName.substring(0 , rightFieldName.indexOf("#"));
                else
                    rightFieldNamePrefix = rightFieldName;

                if(!leftFieldNamePrefix.equalsIgnoreCase(rightFieldNamePrefix))
                {
                    result = leftFieldNamePrefix.compareTo(rightFieldNamePrefix);
                }
                else {

                    int leftFieldIndex = 1;
                    int rightFieldIndex = 1;
                    if (leftFieldName.contains("#")) {
                        try {
                            leftFieldIndex = Integer.valueOf(leftFieldName.substring(leftFieldName.length() - 1, leftFieldName.length() ));
                        } catch (Exception e) {
                            e.printStackTrace();
                            leftFieldIndex = 2;
                        }
                    } else {
                        leftFieldIndex = 0;
                    }

                    if (rightFieldName.contains("#")) {
                        try {
                            rightFieldIndex = Integer.valueOf(rightFieldName.substring(rightFieldName.length() - 1, rightFieldName.length()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            rightFieldIndex = 2;
                        }
                    } else {
                        rightFieldIndex = 0;
                    }
                    if (leftFieldIndex < rightFieldIndex)
                        result = -1;
                    else if (leftFieldIndex == rightFieldIndex)
                        result = 0;
                    else if (leftFieldIndex > rightFieldIndex)
                        result = 1;
                }
            }
            return result;
        }
    }

    public class CropCircleTransformation implements Transformation<Bitmap> {

        private BitmapPool mBitmapPool;

        public CropCircleTransformation(Context context) {
            this(Glide.get(context).getBitmapPool());
        }

        public CropCircleTransformation(BitmapPool pool) {
            this.mBitmapPool = pool;
        }

        @Override
        public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
            Bitmap source = resource.get();
            int size = Math.min(source.getWidth(), source.getHeight());

            int width = (source.getWidth() - size) / 2;
            int height = (source.getHeight() - size) / 2;

            Bitmap bitmap = mBitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader =
                    new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            if (width != 0 || height != 0) {
                // source isn't square, move viewport to center
                Matrix matrix = new Matrix();
                matrix.setTranslate(-width, -height);
                shader.setLocalMatrix(matrix);
            }
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            return BitmapResource.obtain(bitmap, mBitmapPool);
        }

        @Override public String getId() {
            return "CropCircleTransformation()";
        }
    }
}
