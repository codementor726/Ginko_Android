package com.ginko.setup;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.contact.IndexCursor;
import com.ginko.activity.entity.ViewEntityPostsActivity;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.AlphabetSidebar;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.utils.ViewIdGenerator;
import com.google.android.gms.common.api.Releasable;
import com.hb.views.PinnedSectionListView;
import com.hb.views.PullToRefreshPinnedSectionListView;
import com.lee.pullrefresh.ui.PullToRefreshBase;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class SelectCountryCodeActivity extends MyBaseActivity implements View.OnClickListener,
        AlphabetSidebar.OnTouchingLetterChangedListener

{

    /* UI Variables */
    private Button btnCancel , btnConfirm, btnSearchCancel;
    private ImageView btnCancelSearch;
    private PullToRefreshPinnedSectionListView mPullListView;
    private PinnedSectionListView mListView;
    private AlphabetSidebar alphabetScrollbar;
    private EditText edtSearch;
    private RelativeLayout headerLayout;

    /* Variables */
    private String[] strCountryCodeArrays;
    private ArrayList<String> arrayCountryCodes = new ArrayList<String>();
    private ArrayList<String> arrayCountryNames = new ArrayList<String>();

    private ArrayList<CountryCodeItem> countryCodeItemArrayList = new ArrayList<CountryCodeItem>();

    private int currentCountryIndex = 0;
    private String currentSectionName = "";
    private int currentCheckedListviewIndex = 0;

    private CountryCodeListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_country_code);

        Intent intent = this.getIntent();
        currentCountryIndex = intent.getIntExtra("current_country_index", 221);//221 is united states

        initCountryArray();

        getUIObjects();
    }

    private void initCountryArray(){
        strCountryCodeArrays = getResources().getStringArray(R.array.countrycodes);

        for(int i=0;i<strCountryCodeArrays.length;i++)
        {
            try {
                String strCountryCode = strCountryCodeArrays[i];
                arrayCountryNames.add(strCountryCode.substring(0, strCountryCode.indexOf("+") - 1));
                arrayCountryCodes.add(strCountryCode.substring(strCountryCode.indexOf("+")  , strCountryCode.length()));
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        countryCodeItemArrayList = new ArrayList<CountryCodeItem>();
        for(int i = 0 ;i<strCountryCodeArrays.length;i++)
        {
            CountryCodeItem item = new CountryCodeItem();
            item.strCountryCode = arrayCountryCodes.get(i);
            item.strCountryName = arrayCountryNames.get(i);
            item.originalIndex = i;
            if(currentCountryIndex == i)
                item.isChecked = true;
            else
                item.isChecked = false;

            countryCodeItemArrayList.add(item);
        }

        sortGroupByName();

        int index = 0;
        while (index < countryCodeItemArrayList.size()) {
            String sectionName = createSectionAsNeeded(countryCodeItemArrayList.get(index), currentSectionName);
            if(countryCodeItemArrayList.get(index).isChecked)
                currentCheckedListviewIndex = index;
            if (!sectionName.equals("")) {
                currentSectionName = sectionName;
                countryCodeItemArrayList.add(index, countryCodeItemArrayList.get(index).createSection(sectionName));
                index += 2;
                continue;
            }
            index++;
        }
    }
    //Sort by country Name.
    private final static Comparator<CountryCodeItem> countryCodeItemComparator = new Comparator<CountryCodeItem>()
    {
        private final Collator collator = Collator.getInstance();
        @Override
        public int compare(CountryCodeItem lhs, CountryCodeItem rhs) {
            return collator.compare(lhs.strCountryName, rhs.strCountryName);
        }
    };

    public void sortGroupByName(){
        try {
            Collections.sort(countryCodeItemArrayList, countryCodeItemComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    ////////////////////////////////////
    @Override
    protected void getUIObjects() {
        super.getUIObjects();

        headerLayout    = (RelativeLayout)findViewById(R.id.headerlayout);
        btnCancel       = (Button)findViewById(R.id.btnCancel); btnCancel.setOnClickListener(this);
        btnConfirm      = (Button)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this); btnConfirm.setVisibility(View.GONE);
        btnSearchCancel = (Button)findViewById(R.id.btnSearchCancel); btnSearchCancel.setOnClickListener(this); //btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch = (ImageView)findViewById(R.id.imgCancelSearch); btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setText("");
                btnCancelSearch.setVisibility(View.GONE);
                initCountryArray();
                adapter.addAll(countryCodeItemArrayList);

                mListView.setAdapter(adapter);
                mListView.setFastScrollEnabled(false);
                mListView.setFastScrollAlwaysVisible(false);
            }
        });

        edtSearch = (EditText)findViewById(R.id.edtSearch);
        edtSearch.addTextChangedListener(new TextWatcher() {
                                             @Override
                                             public void beforeTextChanged(CharSequence s, int start, int count,
                                                                           int after) {
                                             }

                                             @Override
                                             public void onTextChanged(CharSequence s, int start, int before,
                                                                       int count) {
                                                 if (s.length() > 0)
                                                     btnCancelSearch.setVisibility(View.VISIBLE);
                                                 else
                                                     btnCancelSearch.setVisibility(View.GONE);
                                                 String countryName = s.toString();
                                                 searchCountry(countryName);
                                             }

                                             @Override
                                             public void afterTextChanged(Editable s) {
                                                 // TODO Auto-generated method stub

                                             }
                                         }
        );
        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);

                }
                return false;
            }
        });

        edtSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edtSearch.setCursorVisible(true);
                    btnSearchCancel.setVisibility(View.VISIBLE);
                    if (edtSearch.getText().toString().length() > 0)
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    else
                        btnCancelSearch.setVisibility(View.GONE);
                } else {
                    edtSearch.setCursorVisible(false);
                    btnSearchCancel.setVisibility(View.GONE);
                    btnCancelSearch.setVisibility(View.GONE);
                }
            }
        });

        mPullListView = (PullToRefreshPinnedSectionListView)findViewById(R.id.countryCodeListView);

        mPullListView.setPullLoadEnabled(false);

        mPullListView.setScrollLoadEnabled(false);

        mListView = mPullListView.getRefreshableView();

        mListView.setDividerHeight(getResources().getDimensionPixelOffset(R.dimen.listview_divider_height));
        mListView.setDivider(getResources().getDrawable(R.drawable.listview_divider_drawable));
        mListView.setSelector(getResources().getDrawable(R.drawable.ginkome_filter_conatct_list_selector));

        alphabetScrollbar = (AlphabetSidebar)findViewById(R.id.alphabetScrollbar);
        alphabetScrollbar.setOnTouchingLetterChangedListener(this);

        adapter = new CountryCodeListAdapter(this);
        adapter.addAll(countryCodeItemArrayList);

        mListView.setAdapter(adapter);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);

        if(currentCheckedListviewIndex > 4)
            currentCheckedListviewIndex = currentCheckedListviewIndex -  4;//include header view , so scroll to one further position
        mListView.setSelection(currentCheckedListviewIndex);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                if (!((CountryCodeItem) adapter.getItem(position)).isSection()) {
                    adapter.selectItemPosition(position);
                    adapter.notifyDataSetChanged();
                    CountryCodeItem selectedCountryItem = countryCodeItemArrayList.get(adapter.getSelectedItemPosition());
                    Intent intent = new Intent();
                    intent.putExtra("countryCodeIndex" , selectedCountryItem.originalIndex);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        mPullListView
                .setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<PinnedSectionListView>() {
                    @Override
                    public void onPullDownToRefresh(
                            PullToRefreshBase<PinnedSectionListView> refreshView) {
                        mPullListView.onPullDownRefreshComplete();
                        mPullListView.onPullUpRefreshComplete();

                    }

                    @Override
                    public void onPullUpToRefresh(
                            PullToRefreshBase<PinnedSectionListView> refreshView) {
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCountryArray();
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
            case R.id.btnCancel:
                finish();
                break;

            case R.id.btnConfirm:
                CountryCodeItem selectedCountryItem = countryCodeItemArrayList.get(adapter.getSelectedItemPosition());
                Intent intent = new Intent();
                intent.putExtra("countryCodeIndex" , selectedCountryItem.originalIndex);
                setResult(RESULT_OK, intent);
                this.finish();
                break;
            case R.id.btnSearchCancel:
                edtSearch.setText("");
                edtSearch.clearFocus();
                btnSearchCancel.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);

                initCountryArray();
                adapter.addAll(countryCodeItemArrayList);

                mListView.setAdapter(adapter);
                mListView.setFastScrollEnabled(false);
                mListView.setFastScrollAlwaysVisible(false);
                break;
        }
    }

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

    private void searchCountry(String s){
        if(mListView != null && adapter != null) {
            strCountryCodeArrays = getResources().getStringArray(R.array.countrycodes);

            for(int i=0;i<strCountryCodeArrays.length;i++)
            {
                try {
                    String strCountryCode = strCountryCodeArrays[i];
                    String strCtyname = strCountryCode.substring(0, strCountryCode.indexOf("+") - 1);
                    if(strCtyname.contains(s)) {
                        arrayCountryNames.add(strCountryCode.substring(0, strCountryCode.indexOf("+") - 1));
                        arrayCountryCodes.add(strCountryCode.substring(strCountryCode.indexOf("+"), strCountryCode.length()));
                    }
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

            countryCodeItemArrayList = new ArrayList<CountryCodeItem>();
            for(int i = 0 ;i<strCountryCodeArrays.length;i++)
            {
                String countryName = arrayCountryNames.get(i);

                CountryCodeItem item = new CountryCodeItem();
                if(countryName.contains(s)) {
                    item.strCountryCode = arrayCountryCodes.get(i);
                    item.strCountryName = arrayCountryNames.get(i);
                    item.originalIndex = i;
                    if (currentCountryIndex == i)
                        item.isChecked = true;
                    else
                        item.isChecked = false;

                    countryCodeItemArrayList.add(item);
                }
            }


            int index = 0;
            while (index < countryCodeItemArrayList.size()) {
                String sectionName = createSectionAsNeeded(countryCodeItemArrayList.get(index), currentSectionName);
                if(countryCodeItemArrayList.get(index).isChecked)
                    currentCheckedListviewIndex = index;
                if (!sectionName.equals("")) {
                    currentSectionName = sectionName;
                    countryCodeItemArrayList.add(index, countryCodeItemArrayList.get(index).createSection(sectionName));
                    index += 2;
                    continue;
                }
                index++;
            }
        }
        adapter.addAll(countryCodeItemArrayList);

        adapter.notifyDataSetChanged();

        mListView.setAdapter(adapter);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);
    }

    @SuppressLint("DefaultLocale")
    protected String createSectionAsNeeded(CountryCodeItem codeItem, String currentSectionName) {
        String checkName = codeItem.strCountryName;

        checkName = checkName.trim();

        if (checkName.length() > 0
                && (StringUtils.isEmpty(currentSectionName) || !checkName
                .toUpperCase().startsWith(currentSectionName))) {
            // New section;
            char firstLetter = checkName.charAt(0);
            if(!((firstLetter >= 'a' && firstLetter <= 'z') || (firstLetter >= 'A' && firstLetter <= 'Z')))
            {
                return "#";
            }
            String newSectionName = checkName.substring(0, 1).toUpperCase();

            return newSectionName;
        }

        return "";
    }

    private class CountryCodeListAdapter extends BaseAdapter implements
            PinnedSectionListView.PinnedSectionListAdapter,SectionIndexer {
        private Context mContext;
        private HashMap<String , Integer> sectionNameItemsMap =  new HashMap<String , Integer>();

        private ArrayList<CountryCodeItem> countryCodesItems = new ArrayList<CountryCodeItem>();

        public CountryCodeListAdapter(Context context)
        {
            this.mContext = context;
            alphabetIndexer = new AlphabetIndexer(new CountryCodeIndexCursor(this), 0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        }

        public int getSectionItemIndex(String sectionName)
        {
            if(sectionNameItemsMap.containsKey(sectionName))
                return sectionNameItemsMap.get(sectionName);
            return -1;
        }

        public void selectItemPosition(int position)
        {
            if(countryCodesItems == null) return;
            for(int i=0;i<countryCodesItems.size();i++)
            {
                if(i == position)
                    countryCodesItems.get(i).isChecked = true;
                else
                    countryCodesItems.get(i).isChecked = false;
            }
        }

        public int getSelectedItemPosition()
        {
            int position = 0;
            if(countryCodesItems == null) return position;
            for(int i=0;i<countryCodesItems.size();i++)
            {
                if(countryCodesItems.get(i).isChecked) {
                    position = i;
                    break;
                }
            }
            return position;
        }

        public void add(CountryCodeItem item)
        {
            if(countryCodesItems == null)
                countryCodesItems = new ArrayList<CountryCodeItem>();
            countryCodesItems.clear();
            if(item.isSection())
            {
                sectionNameItemsMap.put(item.getSectionName() , countryCodesItems.size());
            }
            countryCodesItems.add(item);
        }

        public synchronized void addAll(ArrayList<CountryCodeItem> items)
        {
            if(countryCodesItems != null)
                countryCodesItems.clear();
            countryCodesItems = items;
            if(sectionNameItemsMap == null)
                sectionNameItemsMap =  new HashMap<String , Integer>();
            else
            {
                try
                {
                    sectionNameItemsMap.clear();
                }catch(Exception e){
                    e.printStackTrace();
                    sectionNameItemsMap =  new HashMap<String , Integer>();
                }
            }

            for(int i=0;i<countryCodesItems.size();i++)
            {
                if(countryCodesItems.get(i).isSection()) {
                    sectionNameItemsMap.put(countryCodesItems.get(i).getSectionName() , i);
                }
            }

            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return countryCodesItems==null?0:countryCodesItems.size();
        }

        @Override
        public CountryCodeItem getItem(int position) {
            if (position > countryCodesItems.size())
                return countryCodesItems.get(countryCodesItems.size()-1);
            else
                return countryCodesItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CountryCodeItem item = (CountryCodeItem) getItem(position);
            if(item == null) return null;

            int type = getItemViewType(position);

            ItemView view = null;
            if (convertView == null) {

                if(type == CountryCodeItem.SECTION)
                    view = new SectionView(mContext , item );
                else
                    view = new SectionChildItemView(mContext , item );
            }
            else
            {
                view = (ItemView)convertView;
            }

            view.setItem(item);
            view.refreshView();
            return view;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            CountryCodeItem item = getItem(position);
            if(item == null) return 0;
            return item.getType() == CountryCodeItem.SECTION?1:0;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == CountryCodeItem.SECTION;
        }

        private AlphabetIndexer alphabetIndexer;
        @Override
        public Object[] getSections() {
            return alphabetIndexer.getSections();
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return alphabetIndexer.getPositionForSection(sectionIndex);
        }

        @Override
        public int getSectionForPosition(int position) {
            return alphabetIndexer.getSectionForPosition(position);
        }
    }

    private class SectionView extends ItemView<CountryCodeItem> {
        private LayoutInflater inflater = null;
        private CountryCodeItem item;
        private TextView txtSectionName;

        public SectionView(Context context , boolean isTileStyle) {
            super(context);
            // TODO Auto-generated constructor stub

        }
        public SectionView(Context context,  CountryCodeItem _item) {
            super(context);
            this.mContext = context;
            item = _item;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            inflater.inflate(R.layout.select_country_code_list_section_header, this, true);
            setId(ViewIdGenerator.generateViewId());

            txtSectionName = (TextView) findViewById(R.id.txtSectionHeader);
        }

        @Override
        public void setItem(CountryCodeItem _item)
        {
            this.item = _item;
        }

        @Override
        public void refreshView()
        {
            txtSectionName.setText(item.getSectionName());
        }
    }

    private class SectionChildItemView extends ItemView<CountryCodeItem> {
        private LayoutInflater inflater = null;
        private CountryCodeItem item;
        private TextView txtCountryName;
        private TextView txtCountryCode;
        private ImageView imgCheckIcon;

        private ImageLoader imgLoader;

        private int contactId;


        public SectionChildItemView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub

        }
        public SectionChildItemView(Context context,  CountryCodeItem _item)
        {
            super(context);
            setItem(_item);
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if(imgLoader == null)
                imgLoader = MyApp.getInstance().getImageLoader();


            inflater.inflate(R.layout.select_country_code_item, this, true);

            setId(ViewIdGenerator.generateViewId());

            txtCountryCode = (TextView)findViewById(R.id.txtCountryCode);
            txtCountryName = (TextView)findViewById(R.id.txtCountryName);
            imgCheckIcon = (ImageView)findViewById(R.id.imgCheckIcon);
        }

        @Override
        public void setItem(CountryCodeItem _item)
        {
            this.item = _item;
        }

        @Override
        public void refreshView()
        {
            if(item.isChecked)
                imgCheckIcon.setVisibility(View.VISIBLE);
            else
                imgCheckIcon.setVisibility(View.GONE);

            txtCountryCode.setText(item.strCountryCode);
            txtCountryName.setText(item.strCountryName);
        }
    }

    private abstract class ItemView<T> extends LinearLayout {
        public Context mContext;
        public ItemView(Context context)
        {
            super(context);
            this.mContext = context;
        }

        public ItemView(Context context , T item)
        {
            super(context);
            this.mContext = context;
        }

        public void setItem(T obj){};
        public void refreshView(){}
    }
}
