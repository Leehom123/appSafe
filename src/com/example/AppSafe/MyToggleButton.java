package com.example.AppSafe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class MyToggleButton extends View implements OnClickListener{

	private Bitmap backgroundBitmap;
	private Bitmap slideBtn;
	private Paint paint;
	private float slideBtn_left;

	public MyToggleButton(Context context) {
		super(context);
	}

	public MyToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initView();
	}
	
	private void initView() {
		
		backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.switch_background);
		slideBtn = BitmapFactory.decodeResource(getResources(), R.drawable.slide_button);
		
		
		paint = new Paint();
		paint.setAntiAlias(true); 
		
		
		setOnClickListener(this);
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		setMeasuredDimension(backgroundBitmap.getWidth(),backgroundBitmap.getHeight());
	}

	private boolean currState = false;
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(backgroundBitmap, 0, 0, paint);
		
		canvas.drawBitmap(slideBtn, slideBtn_left, 0, paint);
	}

	private boolean isDrag = false;
	@Override
	public void onClick(View v) {
		if(!isDrag){
			currState = !currState;
			flushState();
		}
	}


	private int firstX;
	private int lastX;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			firstX = lastX =(int) event.getX();
			isDrag = false;
			
			break;
		case MotionEvent.ACTION_MOVE:
			
			if(Math.abs(event.getX()-firstX)>5){
				isDrag = true;
			}
			
			int dis = (int) (event.getX() - lastX);
			
			lastX = (int) event.getX();
			
			slideBtn_left = slideBtn_left+dis;
			break;
		case MotionEvent.ACTION_UP:
			
			if (isDrag) {

				int maxLeft = backgroundBitmap.getWidth() - slideBtn.getWidth(); // slideBtn
				if (slideBtn_left > maxLeft / 2) { 
					currState = true;
				} else {
					currState = false;
				}

				flushState();
			}
			break;
		}
		
		flushView();
		
		return true; 
	}

	private void flushState() {
		if(currState){
			slideBtn_left = backgroundBitmap.getWidth()-slideBtn.getWidth();
		}else{
			slideBtn_left = 0;
		}
		
		flushView(); 
	}
	
	private void flushView() {
		
		int maxLeft = backgroundBitmap.getWidth()-slideBtn.getWidth();	
		
		slideBtn_left = (slideBtn_left>0)?slideBtn_left:0;
		
		slideBtn_left = (slideBtn_left<maxLeft)?slideBtn_left:maxLeft;
		
		invalidate();
	}

}
