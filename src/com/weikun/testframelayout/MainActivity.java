package com.weikun.testframelayout;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	
	RelativeLayout[] layouts;
	RelativeLayout layout0;
	
	Display mDisplay;
	Point mSize;
	
	View.OnTouchListener mTouchListener = new View.OnTouchListener() {
		
		View startView;
		View selectedView;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			final int X = (int) event.getRawX();
			final int Y = (int) event.getRawY();
			
			//Log.i(TAG, "view id "+ v.getId() + " " + String.format("%d,%d", X, Y));
			
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					
					//which one was selected
					startView = v;
					v.getId();
					
					//TODO change code here for your requirement
					ImageView selectImageView = (ImageView)v.getTag();
					ImageView imageView = (ImageView)layout0.findViewById(R.id.iv0);
					imageView.setImageDrawable(selectImageView.getDrawable());
					
					RelativeLayout.LayoutParams cacheParams = (RelativeLayout.LayoutParams) layout0.getLayoutParams();
					cacheParams.leftMargin = X - cacheParams.width/2;
					cacheParams.topMargin = Y - cacheParams.height/2;
					layout0.setLayoutParams(cacheParams);
					layout0.setVisibility(View.VISIBLE);
					
					break;
				
				case MotionEvent.ACTION_UP:
					layout0.setVisibility(View.GONE);
					
					swapLayoutContent();
					
					break;
				
				case MotionEvent.ACTION_MOVE:
					RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout0.getLayoutParams();
					layoutParams.leftMargin = X - layoutParams.width/2;
					layoutParams.topMargin = Y - layoutParams.height/2;
					layout0.setLayoutParams(layoutParams);
					break;
			}
			
			checkRegion(X, Y);
			
			return true;
		}

		private void checkRegion(int x, int y) {
			
			int width = mSize.x/2;
			int height = mSize.y/2;
			
			//update current region background
			int loc = -1;
			if(x <= width && y <= height){
				loc &= 0x0;
			}else if(x > width && y <= height){
				loc &= 0x1;
			}else if(x <= width && y > height){
				loc &= 0x2;
			}else if(x > width && y > height){
				loc &= 0x3;
			}
			
			if(loc>=0){
				selectedView = layouts[loc];
				for (int i = 0; i < layouts.length; i++) {
					if(i==loc){
						layouts[i].setBackgroundResource(R.drawable.select_effect);
					}else{
						layouts[i].setBackgroundResource(R.color.default_bg);
					}
				}
			}
			
		}
		
		private void swapLayoutContent(){
			if(startView.getId() != selectedView.getId()){
				ViewGroup vg1 = (ViewGroup)startView;
				ViewGroup vg2 = (ViewGroup)selectedView;
				
				if(vg1.getChildCount()>0 && vg2.getChildCount()>0){
					View tmpView;
					View childAt1 = vg1.getChildAt(0);
					View childAt2 = vg2.getChildAt(0);
					tmpView = childAt1;
					vg1.removeAllViews();
					vg2.removeAllViews();
					vg1.addView(childAt2);
					vg2.addView(tmpView);
				}
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//get device width and height
		mDisplay = getWindowManager().getDefaultDisplay();
		mSize = new Point();
		mDisplay.getSize(mSize);
		Log.i(TAG, String.format("display width:%d  height:%d", mSize.x, mSize.y));
		final int divideWidth = mSize.x/2;
		final int divideHeight = mSize.y/2;
		
		final int[] layoutIds = {R.id.layout1, R.id.layout2, R.id.layout3, R.id.layout4};
		final int[] objectIds = {R.id.iv1, R.id.iv2, R.id.iv3, R.id.iv4};//TODO change your object id here
		RelativeLayout.LayoutParams params;
		
		layouts = new RelativeLayout[layoutIds.length];
		for (int i = 0; i < layoutIds.length; i++) {
			layouts[i] = (RelativeLayout)findViewById(layoutIds[i]);
			layouts[i].setOnTouchListener(mTouchListener);
			
			//tag object for further event handling
			layouts[i].setTag(findViewById(objectIds[i]));
			
			//re allocate layout width and height
			params = (RelativeLayout.LayoutParams) layouts[i].getLayoutParams();
			params.width = divideWidth;
			params.height = divideHeight;
			layouts[i].setLayoutParams(params);
		}
		
		layout0 = (RelativeLayout)findViewById(R.id.layout0);
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
