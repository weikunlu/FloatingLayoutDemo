package com.weikun.testframelayout;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SurfaceImageView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

	private static final String TAG = SurfaceImageView.class.getSimpleName();

	Thread thread = null;
	SurfaceHolder surfaceHolder;
	volatile boolean running = false;

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	Random random;

	public SurfaceImageView(Context context, AttributeSet attrSet) {
		super(context, attrSet);

		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		random = new Random();
		
	}

	public void onResumeMySurfaceView() {
		if(!running){
			running = true;
			thread = new Thread(this);
			thread.start();			
		}
	}

	public void onPauseMySurfaceView() {
		boolean retry = true;
		running = false;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		while (running) {
			if (surfaceHolder.getSurface().isValid()) {
				Canvas canvas = surfaceHolder.lockCanvas();
				// ... actual drawing on canvas

				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(3);

				int w = canvas.getWidth();
				int h = canvas.getHeight();
				int x = random.nextInt(w - 1);
				int y = random.nextInt(h - 1);
				int r = random.nextInt(255);
				int g = random.nextInt(255);
				int b = random.nextInt(255);
				paint.setColor(0xff000000 + (r << 16) + (g << 8) + b);
				canvas.drawPoint(x, y, paint);

				surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
	        int height) {
	    // TODO Auto-generated method stub
		Log.i(TAG, "surfaceChanged");
		onResumeMySurfaceView();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	    // TODO Auto-generated method stub
		Log.i(TAG, "surfaceCreated");
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    // TODO Auto-generated method stub
		Log.i(TAG, "surfaceDestroyed");
		onPauseMySurfaceView();
	}
	
}
