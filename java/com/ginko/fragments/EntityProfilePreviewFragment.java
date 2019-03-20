package com.ginko.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.im.LocationMapViewerActivity;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.customview.EntityProfilePreviewFieldGroupView;
import com.ginko.customview.EntityProfilePreviewLocationMapView;
import com.ginko.customview.ExpandableTextView;
import com.ginko.customview.PersonalProfilePreviewFieldGroupView;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.EntityInfoDetailVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class EntityProfilePreviewFragment extends Fragment {

    private View view;
    private LinearLayout containerLayout;
    private LinearLayout showMoreLayout;
    private CustomNetworkImageView profilePhoto;
    private TextView txtEntityName, showMoreTxt;
    private CustomNetworkImageView imgWallpaper , imgVideoThumb;
    private ImageView imgProfileLock, imgProfileFavorite, showMoreImg;
    private RelativeLayout videoLayout;
    private ExpandableTextView txtEntityDescription;
    private TextView txtEntityCategory;

    private EntityVO entity;
    private EntityInfoVO entityInfo;
    private ImageLoader imgLoader;
    //2016.9.21 Layout Update for Big Profile Show
    private CustomNetworkImageView tiledProfilePhoto;
    private RelativeLayout hiddenLayout;
    private ImageView imgDimClose;


    private boolean isUICreated = false;
    private boolean isShowMore = false;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserProfileFragment.
     */
    public static EntityProfilePreviewFragment newInstance(EntityVO _entity , EntityInfoVO _entityInfo) {
        EntityProfilePreviewFragment fragment = new EntityProfilePreviewFragment();
        Bundle args = new Bundle();
        args.putSerializable("entity", _entity);
        args.putSerializable("entityInfo", _entityInfo);
        fragment.setArguments(args);
        return fragment;
    }


    public EntityProfilePreviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if(this.entity == null)
                this.entity = (EntityVO)getArguments().getSerializable("entity");
            if(this.entityInfo == null)
                this.entityInfo = (EntityInfoVO)getArguments().getSerializable("entityInfo");
        }
        isUICreated = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_entity_profile_preview, container, false);
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
                    init(entity, entityInfo);
                }else
                {
                    showMoreImg.setImageResource(R.drawable.showmorebutton);
                    showMoreTxt.setText("Show more");
                    init(entity, entityInfo);
                }
            }
        });


        //2016.9.21 Update
        tiledProfilePhoto = (CustomNetworkImageView)getActivity().findViewById(R.id.tileProfileImage);
        hiddenLayout = (RelativeLayout)getActivity().findViewById(R.id.hiddenLayout);
        imgDimClose = (ImageView)getActivity().findViewById(R.id.imgDimClose);
        tiledProfilePhoto.setDefaultImageResId(R.drawable.entity_profile_preview);

        hiddenLayout.setVisibility(View.GONE);

        profilePhoto =(CustomNetworkImageView)view.findViewById(R.id.imgProfilePhoto);
        //profilePhoto.setDefaultImageResId(R.drawable.profile_preview_default_icon);
        profilePhoto.setDefaultImageResId(R.drawable.entity_profile_preview);

        imgWallpaper = (CustomNetworkImageView)view.findViewById(R.id.imgWallpaper);
        imgWallpaper.setDefaultImageResId(R.drawable.default_wallpaper);

        txtEntityName = (TextView)view.findViewById(R.id.txtEntityName);
        txtEntityName.setText("");

        imgProfileLock = (ImageView)view.findViewById(R.id.imgProfileLock);
        imgProfileFavorite = (ImageView)view.findViewById(R.id.imgProfileFavorite);
        imgProfileLock.setImageResource(R.drawable.personal_profile_unlocked_white);
        imgProfileFavorite.setVisibility(View.GONE);

        videoLayout = (RelativeLayout)view.findViewById(R.id.videoLayout);
        imgVideoThumb = (CustomNetworkImageView)view.findViewById(R.id.imgVideoThumb);


        txtEntityDescription = (ExpandableTextView)view.findViewById(R.id.txtEntityDescription);
        txtEntityCategory = (TextView)view.findViewById(R.id.txtEntityCategory);

        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(entity == null)
                    return;
                if(entity.getVideo() == null || entity.getVideo().equals(""))
                    return;
                try {
                    Intent videoPlayIntent = new Intent(Intent.ACTION_VIEW);
                    videoPlayIntent.setDataAndType(Uri.parse(entity.getVideo()), "video/*");
                    getActivity().startActivity(videoPlayIntent);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        isUICreated = true;
        init(entity, entityInfo);
        return view;
    }

    private void updateLockButton()
    {
        if(this.entity == null || this.entityInfo == null) return;
        EntityImageVO wallPaperImage = entity.getWallpapaerImage();
        if (this.entity.getPrivilege()>0){
            if(wallPaperImage != null && !wallPaperImage.getUrl().equals(""))
                imgProfileLock.setImageResource(R.drawable.personal_profile_unlocked_white);
            else
                imgProfileLock.setImageResource(R.drawable.personal_profile_unlocked_white);
        }else{
            if(wallPaperImage != null && !wallPaperImage.getUrl().equals(""))
                imgProfileLock.setImageResource(R.drawable.personal_profile_preview_locked);
            else
                imgProfileLock.setImageResource(R.drawable.personal_profile_preview_locked_white);
        }
        imgProfileLock.invalidate();
    }

    public void init(EntityVO _entity , EntityInfoVO _entityInfo)
    {
        this.entity = _entity;
        this.entityInfo = _entityInfo;

        if(isUICreated == false) return;

        if(this.imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        if(this.entityInfo == null || this.entityInfo.getEntityInfoDetails().size() < 1) {
            tiledProfilePhoto.refreshOriginalBitmap();
            tiledProfilePhoto.setImageUrl("", imgLoader);
            tiledProfilePhoto.invalidate();

            profilePhoto.refreshOriginalBitmap();
            profilePhoto.setImageUrl("", imgLoader);
            profilePhoto.invalidate();

            imgWallpaper.refreshOriginalBitmap();
            imgWallpaper.setImageUrl("", imgLoader);
            imgWallpaper.invalidate();

            txtEntityName.setText("");

            updateLockButton();

            containerLayout.removeAllViews();
            return;
        }

        profilePhoto.setOnClickListener(new View.OnClickListener() {
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

        profilePhoto.refreshOriginalBitmap();
        profilePhoto.setImageUrl(this.entity.getProfileImage(), imgLoader);

        profilePhoto.invalidate();

        tiledProfilePhoto.refreshOriginalBitmap();
        tiledProfilePhoto.setImageUrl(this.entity.getProfileImage(), imgLoader);
        tiledProfilePhoto.invalidate();

        if(this.entity.getVideo() == null || this.entity.getVideoThumbUrl().equals(""))
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
            imgVideoThumb.setImageUrl(this.entity.getVideoThumbUrl(), imgLoader);
            imgVideoThumb.invalidate();
            videoLayout.setVisibility(View.VISIBLE);
            if (this.entityInfo.getEntityInfoDetails().size() <= 2)
            {
                showMoreLayout.setVisibility(View.GONE);
                isShowMore = true;
            }
            else
                showMoreLayout.setVisibility(View.VISIBLE);
        }

        EntityImageVO wallpaperImage = null;
        if((wallpaperImage = entity.getWallpapaerImage()) !=null) {
            imgWallpaper.setImageUrl(wallpaperImage.getUrl() , imgLoader);
        }

        txtEntityName.setText(entity.getName());

        switch (entity.getCategoryId())
        {

            case 0:
                txtEntityCategory.setText("Local Business or Place");
                break;
            case 1:
                txtEntityCategory.setText("Company, Organization or Institution");
                break;
            case 2:
                txtEntityCategory.setText("Brand or Product");
                break;
            case 3:
                txtEntityCategory.setText("Entertainment");
                break;
            case 4:
                txtEntityCategory.setText("Artist, Brand or Public Figure");
                break;

            case 5:
                txtEntityCategory.setText("Cause or Community");
                break;


        }
        if("".equals(entity.getDescription()) || entity.getDescription() == null)
            txtEntityDescription.setVisibility(View.GONE);
        else
            txtEntityDescription.setText(entity.getDescription());

        updateLockButton();

        containerLayout.removeAllViews();

        ArrayList<EntityInfoDetailVO>
                                mobileFields = null,
                                phoneFields = null,
                                emailFields = null ,
                                addressFields = null ,
                                faxFields = null,
                                hoursFields = null ,
                                birthdayFields = null ,
                                socialNetworkFields = null ,
                                websiteFields = null,
                                customFields = null;

        EntityProfilePreviewFieldGroupView hoursFieldView = null;
        EntityProfilePreviewFieldGroupView mobileFieldView = null;
        EntityProfilePreviewFieldGroupView emailFieldView = null;
        EntityProfilePreviewFieldGroupView addressFieldView = null;
        EntityProfilePreviewLocationMapView locationMapView = null;
        EntityProfilePreviewFieldGroupView faxFieldView = null;
        EntityProfilePreviewFieldGroupView socialNetworksFieldView = null;
        EntityProfilePreviewFieldGroupView websiteFieldView = null;
        EntityProfilePreviewFieldGroupView customFieldView = null;

        List<EntityInfoDetailVO> fields = entityInfo.getEntityInfoDetails();

        int i = 0;

        for(EntityInfoDetailVO field:fields)
        {
            String strFieldName = field.getFieldName();
            String fieldType = field.getType();
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

            if (strFieldName.toLowerCase().contains("mobile") || strFieldName.toLowerCase().contains("phone")) {
                if(mobileFields == null)
                    mobileFields = new ArrayList<EntityInfoDetailVO>();
                mobileFields.add(field);
                i++;
                if (i == 2 && !isShowMore) break;
            }
            else if (strFieldName.toLowerCase().contains("email")) {
                if(emailFields == null)
                    emailFields = new ArrayList<EntityInfoDetailVO>();
                emailFields.add(field);
                i++;
                if (i == 2 && !isShowMore) break;
            }
            else if (strFieldName.toLowerCase().contains("address")) {
                if(addressFields == null)
                    addressFields = new ArrayList<EntityInfoDetailVO>();
                addressFields.add(field);
                i++;
                if (i == 2 && !isShowMore) break;
            }
            else if (strFieldName.toLowerCase().contains("fax")) {
                if(faxFields == null)
                    faxFields = new ArrayList<EntityInfoDetailVO>();
                faxFields.add(field);
                i++;
                if (i == 2 && !isShowMore) break;
            }
            else if (strFieldName.toLowerCase().contains("hours")) {
                if(hoursFields == null)
                    hoursFields = new ArrayList<EntityInfoDetailVO>();
                hoursFields.add(field);
                i++;
                if (i == 2 && !isShowMore) break;
            }
            else if (strFieldName.toLowerCase().contains("birthday")) {
                if(birthdayFields == null)
                    birthdayFields = new ArrayList<EntityInfoDetailVO>();
                birthdayFields.add(field);
                i++;
                if (i == 2 && !isShowMore) break;
            }
            else if (strFieldName.toLowerCase().contains("facebook") || strFieldName.toLowerCase().contains("twitter") || strFieldName.toLowerCase().contains("linkedin")) {
                if(socialNetworkFields == null)
                    socialNetworkFields = new ArrayList<EntityInfoDetailVO>();
                socialNetworkFields.add(field);
                i++;
                if (i == 2 && !isShowMore) break;
            }
            else if (strFieldName.toLowerCase().contains("website")) {
                if(websiteFields == null)
                    websiteFields = new ArrayList<EntityInfoDetailVO>();
                websiteFields.add(field);
                i++;
                if (i == 2 && !isShowMore) break;
            }
            else if (strFieldName.toLowerCase().contains("custom")) {
                if(customFields == null)
                    customFields = new ArrayList<EntityInfoDetailVO>();
                customFields.add(field);
                i++;
                if (i == 2 && !isShowMore) break;
            }
        }

        //sort the fields by field name like email ,email#2 , email#3
        FieldNameComparator fieldNameComparator = new FieldNameComparator();

        /* This is for GAD-876.
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
*/
        Context mContext = (Context)getActivity();


        if(mobileFields!= null) {
            mobileFieldView = new EntityProfilePreviewFieldGroupView(mobileFields, mobileFields.get(0).getFieldName(), mContext);
            containerLayout.addView(mobileFieldView);
        }

        if(emailFields!= null) {
            emailFieldView = new EntityProfilePreviewFieldGroupView(emailFields, emailFields.get(0).getFieldName(), mContext);
            containerLayout.addView(emailFieldView);
        }

        if(addressFields!= null) {
            addressFieldView = new EntityProfilePreviewFieldGroupView(addressFields, addressFields.get(0).getFieldName(), mContext);
            containerLayout.addView(addressFieldView);
        }

        if(hoursFields!= null) {
            hoursFieldView = new EntityProfilePreviewFieldGroupView(hoursFields, hoursFields.get(0).getFieldName(), mContext);
            containerLayout.addView(hoursFieldView);
        }

        Double lat = null , lng = null;
        if(entityInfo.getLatitude() != null && entityInfo.isAddressConfirmed()) {
            try {
                lat = Double.valueOf(entityInfo.getLatitude());
            } catch (Exception e) {
                e.printStackTrace();
                lat = null;
            }
        }
        if(entityInfo.getLongitude() != null && entityInfo.isAddressConfirmed()) {
            try {
                lng = Double.valueOf(entityInfo.getLongitude());
            } catch (Exception e) {
                e.printStackTrace();
                lng = null;
            }
        }
        //lat = 31.3212d;
        //lng = 18.323d;


        if (addressFields != null) {
            final String fieldValue = addressFields.get(0).getValue().trim();

            if(lat != null && lng != null)
            {
                locationMapView = new EntityProfilePreviewLocationMapView(getActivity() , lat , lng);
                locationMapView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            String uri = String.format(Locale.ENGLISH, "geo:0,0?q=%s", fieldValue);
                            Intent goolgeMapIntent = new Intent(Intent.ACTION_VIEW , Uri.parse(uri));
                            //goolgeMapIntent.setData(geoLocation);
                            // check if Google Maps is supported on given device
                            if (goolgeMapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                getActivity().startActivity(goolgeMapIntent);
                            }
                            else
                            {
                                //open my ginko lcoation viewer activity
                                Intent locationMessageIntent = new Intent(getActivity(), LocationMapViewerActivity.class);
                                locationMessageIntent.putExtra("address" , fieldValue);

                                getActivity().startActivity(locationMessageIntent);
                            }
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                containerLayout.addView(locationMapView);
            }
        }

        if(faxFields!= null) {
            faxFieldView = new EntityProfilePreviewFieldGroupView(faxFields, faxFields.get(0).getFieldName(), mContext);
            containerLayout.addView(faxFieldView);
        }

        if(socialNetworkFields!= null) {
            socialNetworksFieldView = new EntityProfilePreviewFieldGroupView(socialNetworkFields, socialNetworkFields.get(0).getFieldName(), mContext);
            containerLayout.addView(socialNetworksFieldView);
        }

        if(websiteFields!= null) {
            websiteFieldView = new EntityProfilePreviewFieldGroupView(websiteFields, websiteFields.get(0).getFieldName(), mContext);
            containerLayout.addView(websiteFieldView);
        }

        if(customFields!= null) {
            customFieldView = new EntityProfilePreviewFieldGroupView(customFields, customFields.get(0).getFieldName(), mContext);
            containerLayout.addView(customFieldView);
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

    class FieldNameComparator implements Comparator<EntityInfoDetailVO> {
        public FieldNameComparator()
        {
        }

        @Override
        public int compare(EntityInfoDetailVO lhs, EntityInfoDetailVO rhs) {
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
                    leftFieldNamePrefix = leftFieldName.substring(0, leftFieldName.indexOf("#")-1);
                else
                    leftFieldNamePrefix = leftFieldName;

                if(rightFieldName.contains("#"))
                    rightFieldNamePrefix = rightFieldName.substring(0 , rightFieldName.indexOf("#")-1);
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
                            leftFieldIndex = Integer.valueOf(leftFieldName.charAt(leftFieldName.length() - 1));
                        } catch (Exception e) {
                            e.printStackTrace();
                            leftFieldIndex = 2;
                        }
                    } else {
                        leftFieldIndex = 0;
                    }
                    if (rightFieldName.contains("#")) {
                        try {
                            rightFieldIndex = Integer.valueOf(rightFieldName.charAt(rightFieldName.length() - 1));
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

}
