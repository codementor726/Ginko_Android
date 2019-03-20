package customviews.library.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class HorzontalScrollableInViewPagerHListView extends HListView{

	public HorzontalScrollableInViewPagerHListView( Context context ) {
		super( context, null );
	}
	public HorzontalScrollableInViewPagerHListView(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public HorzontalScrollableInViewPagerHListView( Context context, AttributeSet attrs, int defStyle )
	{
		super(context , attrs , defStyle);
	}
	@Override
    public boolean onInterceptTouchEvent(MotionEvent p_event)
    {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent p_event)
    {
        if (p_event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null)
        {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        return super.onTouchEvent(p_event);
    }
}
