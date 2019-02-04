package amd.spbstu.fastread;


import android.app.Activity;
import android.os.Bundle;
import android.view.*;
//import android.widget.*;

import java.util.Locale;
import android.util.Log;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.content.Intent;
import android.content.res.*;
import android.graphics.*;
//import android.view.ViewGroup.LayoutParams;

// ****************************************************************************

public class ActivityMain extends Activity implements  OnCompletionListener, View.OnTouchListener 
{
	// ********************************************
	// CONST
	// ********************************************
	public static final int	VIEW_INTRO		= 0;
	public static final int	VIEW_GAME       = 1;
	
	
	// *************************************************
	// DATA
	// *************************************************
	int						m_viewCur = -1;
	
	AppIntro				m_app;
	ViewIntro			    m_viewIntro;
	
	
	// screen dim
	int						m_screenW;
	int						m_screenH;
	int language;
    

	// *************************************************
	// METHODS
	// *************************************************
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        //overridePendingTransition(0, 0);
        // No Status bar
        final Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Application is never sleeps
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        m_screenW = point.x;
        m_screenH = point.y;
        
        Log.d("THREE", "Screen size is " + String.valueOf(m_screenW) + " * " +  String.valueOf(m_screenH) );
        
        // Detect language
        String strLang = Locale.getDefault().getDisplayLanguage();

        if (strLang.equalsIgnoreCase("english"))
        {
        	Log.d("THREE", "LOCALE: English");
        	language = AppIntro.LANGUAGE_ENG;
        }
        else if (strLang.equalsIgnoreCase("русский"))
        {
        	Log.d("THREE", "LOCALE: Russian");
        	language = AppIntro.LANGUAGE_RUS;
        }
        else
        {
        	Log.d("THREE", "LOCALE unknown: " + strLang);
        	language = AppIntro.LANGUAGE_UNKNOWN;       	
        }
        m_app = new AppIntro(this, language);
        setView(VIEW_INTRO);
        
		
	}
	public void setView(int viewID)
	{
		if (m_viewCur == viewID)
		{
			Log.d("THREE", "setView: already set");
			return;
		}
	
		m_viewCur = viewID;
		if (m_viewCur == VIEW_INTRO)
		{
	        m_viewIntro = new ViewIntro(this);
	        setContentView(m_viewIntro);
		}
		if (m_viewCur == VIEW_GAME)
		{
			Intent intent = new Intent(ActivityMain.this, MainActivity.class);
			intent.putExtra("language", language);
		    startActivity(intent);
		    m_viewCur = VIEW_INTRO;
		}
	}

	protected void onPostCreate(Bundle savedInstanceState) 
	{
		super.onPostCreate(savedInstanceState);
	}
    public void onCompletion(MediaPlayer mp) 
    {
    	Log.d("THREE", "onCompletion: Video play is completed");
    }
	
	
    public boolean onTouch(View v, MotionEvent evt)
    {
    	int x = (int)evt.getX();
    	int y = (int)evt.getY();
    	int touchType = AppIntro.TOUCH_DOWN;
    	
    	
		if (evt.getAction() == MotionEvent.ACTION_MOVE)
			touchType = AppIntro.TOUCH_MOVE;
		if (evt.getAction() == MotionEvent.ACTION_UP)
			touchType = AppIntro.TOUCH_UP;
		
		if (m_viewCur == VIEW_INTRO)
    	  return m_viewIntro.onTouch(x, y, touchType);
		return true;
    }
    public boolean onKeyDown(int keyCode, KeyEvent evt)
    {
    	boolean ret = super.onKeyDown(keyCode, evt);
    	return ret;
    }
    public AppIntro getApp()
    {
    	return m_app;
    }
    
	protected void onResume()
	{
		super.onResume();
		if (m_viewCur == VIEW_INTRO)
			m_viewIntro.start();
	}
	protected void onPause()
	{
		if (m_viewCur == VIEW_INTRO)
			m_viewIntro.stop();
	
		super.onPause();
	}
	protected void onDestroy()
	{
		super.onDestroy();
	}
	public void onConfigurationChanged(Configuration confNew)
	{
		super.onConfigurationChanged(confNew);
		m_viewIntro.onConfigurationChanged(confNew);
	}
}