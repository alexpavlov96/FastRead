package amd.spbstu.fastread;

import java.io.File;
import java.util.concurrent.TimeUnit;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnTouchListener {
   private static final String LOG_TAG = "MyLog";
   // для передачи сообщений о действиях при нажатии кнопок
   ReadingHandler myHandler;
   DownloadBookTask downloadTask;
   Book myBook;
   FileDialog FileOpenDialog;
   boolean isFileDialogChoosen;
   Button btnUp, btnDown;
   float dimension;
   TextView word;
   Button btnPlay, btnStop;
   ProgressDialog dialog, dialog2;
   RelativeLayout rLayout;
   WindowSizeTask winTask;
   int curWord;
   public int language;
   ButtonContainter btnCont;
   SpeedContainter spCont;
    @Override
    protected void onStart() {
        super.onStart();
        //Toast.makeText(getApplicationContext(), "onStart()", Toast.LENGTH_SHORT).show();
        Log.i(LOG_TAG, "onStart()");
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onStart()");
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	myHandler.stop();
    }
        
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putInt("message", myBook.getCurrentWordIndex());
    super.onSaveInstanceState(outState);
  }
  
  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    curWord = savedInstanceState.getInt("message");
    if (myBook.getState())
      myBook.setCurrentWordByIndex(curWord);
      
  }
    
  public class WindowSizeTask extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String... params) {
      while (rLayout.getMeasuredHeight() * rLayout.getMeasuredWidth() == 0 /*|| (rLayout.getMeasuredHeight() == myBook.getHeight()) || (rLayout.getMeasuredWidth() == myBook.getWidth()) */)
      { 
        try {
          TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      int height1 = 0, height2 = 0;
      do 
      { 
        height1 = rLayout.getMeasuredHeight();
        try {
          TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        height2 = rLayout.getMeasuredHeight();
      } while (height1 != height2);
      Log.d("BOOK", "Screen size: " + rLayout.getMeasuredWidth() + " x " + rLayout.getMeasuredHeight());
      return null;
    }
    
    @Override
    protected void onPostExecute(String result) {
            dialog.dismiss();
            myBook.setLayoutSize();
            if (!myBook.getState())
            {      
              if (isFileDialogChoosen)
              {
                FileOpenDialog.chooseFile();
                isFileDialogChoosen = false;
              }
            }
            else
            {
              myBook.createViews(myBook.getTextSize());
              myBook.prepareViews(false);
              myBook.placeViews();
              myBook.curWordWasOutside();
            }
            super.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage(getString(R.string.download));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
            rLayout = (RelativeLayout) findViewById(R.id.myLayout);
            rLayout.removeAllViews();
            super.onPreExecute();
    }
  }

  class DownloadBookTask extends AsyncTask<String, String, String> {
    private String directory;
    public DownloadBookTask( String chosenDir )
    {
      directory = chosenDir;
    }
    protected String doInBackground(String... params) {
      myBook.readFile(directory);
      return null;
    }
    
    @Override
    protected void onPostExecute(String result) {
            dialog2.dismiss();
            
            if (myBook.createViews(myBook.getTextSize()) != -1)
            {
              myBook.prepareViews(false);
              myBook.placeViews();
              myBook.curWordWasOutside();
            }
            else
            {
              Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
              rLayout.removeAllViews();
              word.setText(R.string.chooseBook);
            }
            myBook.finishReadFile();
            super.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
            dialog2 = new ProgressDialog(MainActivity.this);
            dialog2.setMessage(getString(R.string.download));
            dialog2.setIndeterminate(true);
            dialog2.setCancelable(false);
            dialog2.show();
            rLayout.removeAllViews();
            myBook.startReadFile();
            super.onPreExecute();
    }
  }
  
  class WaitingTask extends AsyncTask<String, String, String> {
    
    protected String doInBackground(String... params) {
      while (myBook.getNeedToResize())
      {
        Log.w("SOCKETS", "waiting task!");
        try {
          TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      };
      return null;
    }
    
    @Override
    protected void onPostExecute(String result) {
            new WindowSizeTask().execute();
            super.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
            super.onPreExecute();
    }
  }
  
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      myHandler.stop();
      myBook.checkThready();
      dialog.dismiss();
      // суть: если экран еще не вращался, то запускаем поток, который ждет, пока загрузится книга,
      // чтобы начать WindowSizeTask (ожидание измерения Layout и вывод на экран текста)
      if (!myBook.readFileNotFinished())
        new WindowSizeTask().execute();
      else if (!myBook.getNeedToResize())
      {
        myBook.needToResize();
        new WaitingTask().execute();
      }
      else
        myBook.needToResize();
      
  }
  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	  Log.i("MyLog", "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		rLayout = (RelativeLayout) findViewById(R.id.myLayout);
    rLayout.setOnTouchListener(new OnTouchListener() {
      @SuppressLint("ClickableViewAccessibility")
      @Override
      public boolean onTouch(View view, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Log.e("Coords", "x: " + x + "y: " + y);
        return false;
      }
      
    });
    
    Intent intent = getIntent();
    int language = intent.getIntExtra("language", 0);
    word = (TextView) findViewById(R.id.word);
    word.setText(R.string.chooseBook);
    if (language == 1)
    {
      this.setTitle("Быстрочтец");
    }
    btnPlay = (Button) findViewById(R.id.btnPlay);
    btnStop = (Button) findViewById(R.id.btnStop);
    btnUp = (Button) findViewById(R.id.btnUp);
    btnDown = (Button) findViewById(R.id.btnDown);
    btnCont = new ButtonContainter(btnPlay, btnStop);
    spCont = new SpeedContainter(btnUp, btnDown);
    winTask = new WindowSizeTask();
    try {
      myBook = Book.getBook(getApplicationContext(), rLayout, word, spCont, language); 
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    isFileDialogChoosen = true;
    FileOpenDialog = new FileDialog(MainActivity.this, new FileDialog.FDListener()
    {
      @Override
      public void onChosenDir(String chosenDir) 
      {
        File f = new File(chosenDir);
        if (f.isFile())
        {
          downloadTask = new DownloadBookTask(chosenDir);
          downloadTask.execute();
        }
        else if (f.isDirectory())
        {
          Toast.makeText(MainActivity.this, chosenDir + getString(R.string.isFolder), Toast.LENGTH_LONG).show();
        }
        isFileDialogChoosen = true;
      }
    });
    
    myHandler = new ReadingHandler(myBook, btnCont);

    new WindowSizeTask().execute();
   
    
    OnClickListener oclBtnPlay = new OnClickListener() {
            @Override
            public void onClick(View v) {
              Thread t = new Thread(new Runnable() {
                    public void run() {
                      myHandler.run();
                    }
                  });
                  t.start();
            }
        };
        
        final OnClickListener oclBtnStop = new OnClickListener() {
            @Override
            public void onClick(View v) {
               myHandler.stop();
            }
        };
             
     OnClickListener oclBtnUp = new OnClickListener() {
            @Override
            public void onClick(View v) {
               myBook.changeSpeed(0);
            }
        };
        
        OnClickListener oclBtnDown = new OnClickListener() {
            @Override
            public void onClick(View v) {
              myBook.changeSpeed(1);
            }
        };

        btnPlay.setOnClickListener(oclBtnPlay);
        btnStop.setOnClickListener(oclBtnStop);
        btnUp.setOnClickListener(oclBtnUp);
        btnDown.setOnClickListener(oclBtnDown);
	}
	
  DialogInterface.OnClickListener sizesDialogListener = new DialogInterface.OnClickListener() {
	public void onClick(DialogInterface dialog, int which) {
      ListView lv = ((AlertDialog) dialog).getListView();
      if (which == Dialog.BUTTON_POSITIVE)
      {
    	  switch (lv.getCheckedItemPosition())
    	  {
    	  case 0: case 1: case 2:
    	    myBook.checkThready();
    	    float size = myBook.changeTextSize(lv.getCheckedItemPosition());
    	    rLayout.removeAllViews();
    		  myBook.createViews(size);
    		  myBook.prepareViews(false);
    		  myBook.placeViews();
          myBook.curWordWasOutside();
    		  break;
    	  }
      }
    }
	  };
	  
	  DialogInterface.OnClickListener textColorDialogListener = new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
	        ListView lv = ((AlertDialog) dialog).getListView();
	        if (which == Dialog.BUTTON_POSITIVE)
	        {
	          int color = 0;
	          switch (lv.getCheckedItemPosition())
	          {
	          case 0: 
	            color = Color.BLUE;
	            break;  
	          case 1: 
	            color = Color.RED;
	            break;
	          case 2:
	            color = Color.BLACK;
	            break;
	          case 3:
              color = Color.GRAY;
              break;
	          case 4:
              color = Color.GREEN;
              break;
	          }
            myBook.changeTextColor(color);
	        }
	      }
	    };
	    
	    DialogInterface.OnClickListener backgroundColorDialogListener = new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	          ListView lv = ((AlertDialog) dialog).getListView();
	          if (which == Dialog.BUTTON_POSITIVE)
	          {
	            int color = 0;
	            switch (lv.getCheckedItemPosition())
	            {
	            case 0: 
	              color = getResources().getColor(R.color.MintCream);
	              break;  
	            case 1: 
	              color = getResources().getColor(R.color.LightSteelBlue);
	              break;
	            case 2:
	              color = Color.WHITE;
	              break;
	            }
	            myBook.changeBackgroungColor(color);
	          }
	        }
	      };

	 protected Dialog onCreateDialog(int id) {
	    AlertDialog.Builder adb = new AlertDialog.Builder(this);
	    String[] sizes = {getString(R.string.smallSize), getString(R.string.mediumSize), getString(R.string.bigSize)};
	    String[] colors = {getString(R.string.color_blue), getString(R.string.color_red), getString(R.string.color_black), getString(R.string.color_gray), getString(R.string.color_green)};
	    String[] backgrounds = {getString(R.string.soft), getString(R.string.dark), getString(R.string.white)};
	    switch (id) {
	    case 0:
	      adb.setTitle(getString(R.string.title_size));
	      adb.setSingleChoiceItems(sizes, 1, (android.content.DialogInterface.OnClickListener) sizesDialogListener);
	      adb.setPositiveButton("OK", sizesDialogListener);
	      break;
	    case 1:
	    	adb.setTitle(getString(R.string.title_color));
		    adb.setSingleChoiceItems(colors, 0, (android.content.DialogInterface.OnClickListener) textColorDialogListener);  
		    adb.setPositiveButton("OK", textColorDialogListener);
		    break;
	    case 2:
        adb.setTitle(getString(R.string.title_background));
        adb.setSingleChoiceItems(backgrounds, 0, (android.content.DialogInterface.OnClickListener) backgroundColorDialogListener);  
        adb.setPositiveButton("OK", backgroundColorDialogListener);
        break;
	    }
	    return adb.create();
	  }
		 
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		  getMenuInflater().inflate(R.menu.main, menu);
		  return super.onCreateOptionsMenu(menu);   
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	  myHandler.stop();
        switch (item.getItemId())
        {
        case R.id.action_size:
        	showDialog(0);
        	break;
        case R.id.action_color:
        	showDialog(1);
        	break;
        case R.id.action_back:
          showDialog(2);
          break;
        case R.id.action_open:
        	FileOpenDialog.chooseFile();
        	break;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("ClickableViewAccessibility")
	  @Override
    public boolean onTouch(View v, MotionEvent event) {
     
      return false;
    }
}
