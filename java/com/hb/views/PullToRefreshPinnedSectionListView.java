package com.hb.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;

import com.ginko.utils.ViewIdGenerator;
import com.lee.pullrefresh.ui.FooterLoadingLayout;
import com.lee.pullrefresh.ui.ILoadingLayout.State;
import com.lee.pullrefresh.ui.LoadingLayout;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.RotateLoadingLayout;

public class PullToRefreshPinnedSectionListView  extends PullToRefreshBase<PinnedSectionListView> implements OnScrollListener {


    
    /**ListView*/
    private PinnedSectionListView mListView;
    /**���ڻ����ײ��Զ����ص�Footer*/
    private LoadingLayout mLoadMoreFooterLayout;
    /**�����ļ�����*/
    private OnScrollListener mScrollListener;
    
    /**
     * ���췽��
     * 
     * @param context context
     */
    public PullToRefreshPinnedSectionListView(Context context) {
        this(context, null);
    }
    
    /**
     * ���췽��
     * 
     * @param context context
     * @param attrs attrs
     */
    public PullToRefreshPinnedSectionListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    /**
     * ���췽��
     * 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public PullToRefreshPinnedSectionListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
  
        setPullLoadEnabled(false);
    }

//    public void setListView(PinnedSectionListView listView){
//    	this.mListView = listView;
//    }
    
    @Override
    protected PinnedSectionListView createRefreshableView(Context context, AttributeSet attrs) {
    	PinnedSectionListView listView = new PinnedSectionListView(context,attrs);
        listView.setId(ViewIdGenerator.generateViewId());
    	this.mListView = listView;
    	this.mListView.setOnScrollListener(this);
        return this.mListView;
    }
    
    /**
     * �����Ƿ��и����ݵı�־
     * 
     * @param hasMoreData true��ʾ���и�����ݣ�false��ʾû�и�������
     */
    public void setHasMoreData(boolean hasMoreData) {
        if (!hasMoreData) {
            if (null != mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout.setState(State.NO_MORE_DATA);
            }
            
            LoadingLayout footerLoadingLayout = getFooterLoadingLayout();
            if (null != footerLoadingLayout) {
                footerLoadingLayout.setState(State.NO_MORE_DATA);
            }
        }
    }

    /**
     * ���û����ļ�����
     * 
     * @param l ������
     */
    public void setOnScrollListener(OnScrollListener l) {
        mScrollListener = l;
    }
    
    @Override
    protected boolean isReadyForPullUp() {
        return isLastItemVisible();
    }

    @Override
    protected boolean isReadyForPullDown() {
        return isFirstItemVisible();
    }

    @Override
    protected void startLoading() {
        super.startLoading();
        
        if (null != mLoadMoreFooterLayout) {
            mLoadMoreFooterLayout.setState(State.REFRESHING);
        }
    }
    
    @Override
    public void onPullUpRefreshComplete() {
        super.onPullUpRefreshComplete();
        
        if (null != mLoadMoreFooterLayout) {
            mLoadMoreFooterLayout.setState(State.RESET);
        }
    }
    
    @Override
    public void setScrollLoadEnabled(boolean scrollLoadEnabled) {
        super.setScrollLoadEnabled(scrollLoadEnabled);
        
        if (scrollLoadEnabled) {
            // ����Footer
            if (null == mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout = new FooterLoadingLayout(getContext());
            }
            
            if (null == mLoadMoreFooterLayout.getParent()) {
                mListView.addFooterView(mLoadMoreFooterLayout, null, false);
            }
            mLoadMoreFooterLayout.show(true);
        } else {
            if (null != mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout.show(false);
            }
        }
    }
    
    @Override
    public LoadingLayout getFooterLoadingLayout() {
        if (isScrollLoadEnabled()) {
            return mLoadMoreFooterLayout;
        }

        return super.getFooterLoadingLayout();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isScrollLoadEnabled() && hasMoreData()) {
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE 
                    || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                if (isReadyForPullUp()) {
                    startLoading();
                }
            }
        }
        
        if (null != mScrollListener) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (null != mScrollListener) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }
    
    @Override
    protected LoadingLayout createHeaderLoadingLayout(Context context, AttributeSet attrs) {
        return new RotateLoadingLayout(context);
    }
    
    /**
     * ��ʾ�Ƿ��и�����
     * 
     * @return true��ʾ���и�����
     */
    private boolean hasMoreData() {
        if ((null != mLoadMoreFooterLayout) && (mLoadMoreFooterLayout.getState() == State.NO_MORE_DATA)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * �жϵ�һ��child�Ƿ���ȫ��ʾ����
     * 
     * @return true��ȫ��ʾ����������false
     */
    private boolean isFirstItemVisible() {
        final Adapter adapter = mListView.getAdapter();

        if (null == adapter || adapter.isEmpty()) {
            return true;
        }

        int mostTop = (mListView.getChildCount() > 0) ? mListView.getChildAt(0).getTop() : 0;
        if (mostTop >= 0) {
            return true;
        }

        return false;
    }

    /**
     * �ж����һ��child�Ƿ���ȫ��ʾ����
     * 
     * @return true��ȫ��ʾ����������false
     */
    private boolean isLastItemVisible() {
        final Adapter adapter = mListView.getAdapter();

        if (null == adapter || adapter.isEmpty()) {
            return true;
        }

        final int lastItemPosition = adapter.getCount() - 1;
        final int lastVisiblePosition = mListView.getLastVisiblePosition();

        /**
         * This check should really just be: lastVisiblePosition == lastItemPosition, but ListView
         * internally uses a FooterView which messes the positions up. For me we'll just subtract
         * one to account for it and rely on the inner condition which checks getBottom().
         */
        if (lastVisiblePosition >= lastItemPosition - 1) {
            final int childIndex = lastVisiblePosition - mListView.getFirstVisiblePosition();
            final int childCount = mListView.getChildCount();
            final int index = Math.min(childIndex, childCount - 1);
            final View lastVisibleChild = mListView.getChildAt(index);
            if (lastVisibleChild != null) {
                return lastVisibleChild.getBottom() <= mListView.getBottom();
            }
        }

        return false;
    }


}
