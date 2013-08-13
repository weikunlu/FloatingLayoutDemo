package com.weikun.testframelayout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	final int[] mLayoutPosAry = { R.id.layout1, R.id.layout2, R.id.layout3, R.id.layout4 };
	private SparseArray<RelativeLayout> mLayoutMap = new SparseArray<RelativeLayout>();
	private RelativeLayout layout0;

	Display mDisplay;
	Point mSize;

	// touch fields
	private int X;
	private int Y;
	private int startViewId;
	private int selectedViewId;

	private float mDownFocusX;
	private float mDownFocusY;

	// constants for Message.what used by GestureHandler below
	private static final int LONG_PRESS = 1;
	private static final int TAP = 2;

	private static final int LONGPRESS_TIMEOUT = 500;
	private static final int TAP_TIMEOUT = 180;
	private static final int DOUBLE_TAP_TIMEOUT = 300;

	private int mTouchSlopSquare = 8 * 8;
	private int mDoubleTapTouchSlopSquare = mTouchSlopSquare;
	private int mDoubleTapSlopSquare = 50 * 50;

	private boolean mInLongPress;
	private boolean mIsDoubleTapping;
	private boolean mAlwaysInTapRegion;
	private boolean mAlwaysInBiggerTapRegion;

	private MotionEvent mCurrentDownEvent;
	private MotionEvent mPreviousUpEvent;

	final Handler mGestureHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LONG_PRESS:
				dispatchLongPress();
				break;
			case TAP:
				// do nothing because TAG flag just a signal to identify double tap
				break;
			}
		}
	};

	View.OnTouchListener mTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			X = (int) event.getRawX();
			Y = (int) event.getRawY();

			final boolean pointerUp = (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP;
			final int skipIndex = pointerUp ? event.getActionIndex() : -1;

			// Determine focal point
			float sumX = 0, sumY = 0;
			final int count = event.getPointerCount();
			for (int i = 0; i < count; i++) {
				if (skipIndex == i)
					continue;
				sumX += event.getX(i);
				sumY += event.getY(i);
			}
			final int div = pointerUp ? count - 1 : count;
			final float focusX = sumX / div;
			final float focusY = sumY / div;

			switch (event.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_POINTER_DOWN:
				mDownFocusX = focusX;
				mDownFocusY = focusY;
				// Cancel long press and taps
				cancelTaps();
				break;

			case MotionEvent.ACTION_POINTER_UP:
				mDownFocusX = focusX;
				mDownFocusY = focusY;
				break;

			case MotionEvent.ACTION_DOWN:

				// which one was selected
				startViewId = v.getId();

				boolean hadTapMessage = mGestureHandler.hasMessages(TAP);
				if ((mCurrentDownEvent != null)
						&& (mPreviousUpEvent != null)
						&& hadTapMessage
						&& isConsideredDoubleTap(mCurrentDownEvent,
								mPreviousUpEvent, event)) {
					// double tap
					mGestureHandler.removeMessages(TAP);

					mIsDoubleTapping = true;

				} else {
					mGestureHandler.sendEmptyMessageAtTime(TAP,
							event.getDownTime() + DOUBLE_TAP_TIMEOUT);
				}

				mGestureHandler.removeMessages(LONG_PRESS);
				mGestureHandler.sendEmptyMessageAtTime(LONG_PRESS,
						event.getDownTime() + TAP_TIMEOUT + LONGPRESS_TIMEOUT);

				mDownFocusX = focusX;
				mDownFocusY = focusY;
				if (mCurrentDownEvent != null) {
					mCurrentDownEvent.recycle();
				}
				mCurrentDownEvent = MotionEvent.obtain(event);

				mAlwaysInTapRegion = true;
				mAlwaysInBiggerTapRegion = true;

				break;

			case MotionEvent.ACTION_UP:

				MotionEvent currentUpEvent = MotionEvent.obtain(event);

				if (mIsDoubleTapping) {
					// TODO double tap task here
					Log.i(TAG, "on double tap");
					Toast.makeText(MainActivity.this, "double tap",
							Toast.LENGTH_SHORT).show();
					;
					// fullScreen(selectedView); ?

					mIsDoubleTapping = false;
				}
				if (mInLongPress) {
					layout0.setVisibility(View.GONE);

					// swapLayoutContent();
					swapLayout();

					mInLongPress = false;
				}

				mGestureHandler.removeMessages(LONG_PRESS);

				if (mPreviousUpEvent != null) {
					mPreviousUpEvent.recycle();
				}
				// Hold the event we obtained above - listeners may have changed
				// the original.
				mPreviousUpEvent = currentUpEvent;

				break;

			case MotionEvent.ACTION_MOVE:

				if (mInLongPress) {
					RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout0
							.getLayoutParams();
					layoutParams.leftMargin = X - layoutParams.width / 2;
					layoutParams.topMargin = Y - layoutParams.height / 2;
					layout0.setLayoutParams(layoutParams);

					break;
				} else if (mAlwaysInTapRegion) {
					final int deltaX = (int) (focusX - mDownFocusX);
					final int deltaY = (int) (focusY - mDownFocusY);
					int distance = (deltaX * deltaX) + (deltaY * deltaY);
					if (distance > mTouchSlopSquare) {
						mAlwaysInTapRegion = false;
						mGestureHandler.removeMessages(TAP);
						mGestureHandler.removeMessages(LONG_PRESS);
					}
					if (distance > mDoubleTapTouchSlopSquare) {
						mAlwaysInBiggerTapRegion = false;
					}
				}

				break;
			}
			
			checkRegion(v, X, Y);
			return true;
		}

		private void cancelTaps() {
			mGestureHandler.removeMessages(LONG_PRESS);
			mGestureHandler.removeMessages(TAP);
			mIsDoubleTapping = false;
			mAlwaysInTapRegion = false;
			mAlwaysInBiggerTapRegion = false;
			if (mInLongPress) {
				mInLongPress = false;
			}
		}

		private boolean isConsideredDoubleTap(MotionEvent firstDown,
				MotionEvent firstUp, MotionEvent secondDown) {
			if (!mAlwaysInBiggerTapRegion) {
				return false;
			}

			if (secondDown.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
				return false;
			}

			int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
			int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
			int square = (deltaX * deltaX + deltaY * deltaY);

			return (square < mDoubleTapSlopSquare);
		}

	};

	private void dispatchLongPress() {
		mInLongPress = true;

		// TODO change code here for your requirement
		ImageView selectImageView = (ImageView) mLayoutMap.get(startViewId).getTag();
		ImageView imageView = (ImageView) layout0.findViewById(R.id.iv0);
		imageView.setImageDrawable(selectImageView.getDrawable());

		RelativeLayout.LayoutParams cacheParams = (RelativeLayout.LayoutParams) layout0.getLayoutParams();
		cacheParams.leftMargin = X - cacheParams.width / 2;
		cacheParams.topMargin = Y - cacheParams.height / 2;
		layout0.setLayoutParams(cacheParams);
		layout0.setVisibility(View.VISIBLE);

	}

	private void checkRegion(View view, int x, int y) {

		int width = mSize.x / 2;
		int height = mSize.y / 2;

		// update current region background
		int loc = 0;
		if (x > width && y <= height) {
			loc = 1;
		} else if (x <= width && y > height) {
			loc = 2;
		} else if (x > width && y > height) {
			loc = 3;
		}

		for (int i = 0; i < mLayoutPosAry.length; i++) {
			if(i == loc){
				mLayoutMap.get(mLayoutPosAry[i]).setBackgroundResource(R.drawable.select_effect);
				selectedViewId = mLayoutPosAry[i];
			}else{
				mLayoutMap.get(mLayoutPosAry[i]).setBackgroundResource(R.color.default_bg);
			}
		}

	}

	private void swapLayout() {
		Log.i(TAG, "swap startViewId: " + startViewId + " selectedViewId: "
				+ selectedViewId);
		if (startViewId != selectedViewId) {
			ViewGroup vg1 = (ViewGroup) mLayoutMap.get(startViewId);
			ViewGroup vg2 = (ViewGroup) mLayoutMap.get(selectedViewId);

			RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams) vg1
					.getLayoutParams();
			RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) vg2
					.getLayoutParams();

			int tmptop = lp1.topMargin;
			int tmpleft = lp1.leftMargin;
			lp1.topMargin = lp2.topMargin;
			lp1.leftMargin = lp2.leftMargin;
			lp2.topMargin = tmptop;
			lp2.leftMargin = tmpleft;
			vg1.setLayoutParams(lp1);
			vg2.setLayoutParams(lp2);

			int a = 0, b = 0;
			for (int i = 0; i < mLayoutPosAry.length; i++) {
				if (startViewId == mLayoutPosAry[i]) {
					a = i;
				} else if (selectedViewId == mLayoutPosAry[i]) {
					b = i;
				}
			}
			int tmp = mLayoutPosAry[a];
			mLayoutPosAry[a] = mLayoutPosAry[b];
			mLayoutPosAry[b] = tmp;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// get device width and height
		mDisplay = getWindowManager().getDefaultDisplay();
		mSize = new Point();
		mDisplay.getSize(mSize);
		Log.i(TAG, String.format("display width:%d  height:%d", mSize.x, mSize.y));
		final int divideWidth = mSize.x / 2;
		final int divideHeight = mSize.y / 2;

		// TODO change your object id here
		final int[] objectIds = { R.id.iv1, R.id.iv2, R.id.iv3, R.id.iv4 };

		RelativeLayout.LayoutParams params;

		for (int i = 0; i < mLayoutPosAry.length; i++) {
			RelativeLayout layout = (RelativeLayout) findViewById(mLayoutPosAry[i]);
			layout.setOnTouchListener(mTouchListener);

			// tag object for further event handling
			layout.setTag(findViewById(objectIds[i]));

			// re allocate layout width and height
			params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
			params.width = divideWidth;
			params.height = divideHeight;

			switch (i) {
			case 0:
				params.topMargin = 0;
				params.leftMargin = 0;
				break;
			case 1:
				params.topMargin = 0;
				params.leftMargin = divideWidth;
				break;
			case 2:
				params.topMargin = divideHeight;
				params.leftMargin = 0;
				break;
			case 3:
				params.topMargin = divideHeight;
				params.leftMargin = divideWidth;
				break;
			}

			layout.setLayoutParams(params);
			mLayoutMap.put(layout.getId(), layout);
		}

		layout0 = (RelativeLayout) findViewById(R.id.layout0);
		params = (RelativeLayout.LayoutParams) layout0.getLayoutParams();
		params.width = divideWidth;
		params.height = divideHeight;
		layout0.setLayoutParams(params);

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
