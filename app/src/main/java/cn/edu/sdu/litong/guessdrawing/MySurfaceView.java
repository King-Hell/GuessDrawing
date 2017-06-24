package cn.edu.sdu.litong.guessdrawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Created by hasee on 2017/6/22.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback,Runnable{
    private SurfaceHolder mHodler;
    private Canvas mCanvas;
    private boolean mIsDrawing;
    private Paint mPaint = new Paint();
    private Path mPath = new Path();
    private int x;
    private int y;

    private Socket socket;


    public MySurfaceView(Context context) {
        super(context);

        init();

    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mHodler = getHolder();
        mHodler.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
    }
    //开启子线程进行绘制
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing =false;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if(MainActivity.isServer){
            return true;
        }
        final int x  = (int) event.getX();
        final int y = (int) event.getY();
        if(MainActivity.isLogin&&!MainActivity.isServer){
           new Thread(){
               @Override
               public void run() {
                   try{
                       socket=MainActivity.socket;
                       int type=event.getAction();
                       DataOutputStream out=new DataOutputStream(socket.getOutputStream());
                       out.writeInt(x);
                       out.writeInt(y);
                       out.writeInt(type);
                       out.flush();
                   }catch (Exception e){
                       e.printStackTrace();
                   }
               }
           }.start();

        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(x,y);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x,y);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }
    public void net(int x,int y,int type){
        switch(type){
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(x,y);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x,y);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
    }
    @Override
    public void run() {
        long start = System.currentTimeMillis();
        while (mIsDrawing){
            draw();

//            x+=1;
//            y= (int) (100*Math.sin(x*2*Math.PI/180)+400);
//            mPath.lineTo(x,y);
        }
        long end = System.currentTimeMillis();
        if (end - start <100){
            try {
                Thread.sleep(100-(end - start));
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void draw(){
        try {
            //lockCanvas获得canvas对象进行绘制
            mCanvas = mHodler.lockCanvas();
            //surface背景
            mCanvas.drawColor(Color.WHITE);
//            mCanvas.drawLine(0,0,200,200,mPaint);
            mCanvas.drawPath(mPath,mPaint);

        }catch (Exception e){

        }finally {
            if (mCanvas != null){
                //对画布内容进行提交
                mHodler.unlockCanvasAndPost(mCanvas);
            }
        }
    }
    public void reset(){
        mPath.reset();
    }
    public void sendData(){

    }
}
