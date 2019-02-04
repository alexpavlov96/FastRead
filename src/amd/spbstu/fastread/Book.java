package amd.spbstu.fastread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import amd.spbstu.fastread.R.color;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class Book {
  private Context context;
  private int stringIndex = 0;
  private int listIndex = 0;
  private static Lock lock;
  private final static int thresHold = 1500;
  private static int currentIndexOffset = 0;
  private static int currentStartDownloading = thresHold;
  class DownloadHandler extends Handler {

    private int start;
    private int end;
    private boolean running = true;
    public DownloadHandler( int st, int ed ) {
      Log.w("BOOK", "CREATING new DownloadHandler");
      start = st;
      end = ed;
    }
   
    public void run() throws InterruptedException
    {
      for (stringIndex = start; stringIndex < numOfWords && listIndex < end && running; stringIndex++)
      {
         download();
      }
      try {
          if (lock.tryLock(10, TimeUnit.MILLISECONDS)) {
            currentStartDownloading += thresHold;
          }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    
    public void stop()
    {
      Log.w("BOOK", "STOPING DownloadHandler");
      running = false;
    }
    
    @Override
    public void handleMessage(android.os.Message msg) {
          switch (msg.what) {
          }
    }
  }
  
  ArrayList<RelativeLayout.LayoutParams> params, spaceParams;
  ArrayList<TextView> currentPlacedTextViews;
  ArrayList<RelativeLayout.LayoutParams> currentPlacedParams;
  static ArrayList<TextView> currentShowedTextViews;
  ArrayList<RelativeLayout.LayoutParams> currentShowedParams;
  ArrayList<TextView> space;
  TextView textViews[];
  RelativeLayout.LayoutParams textParams[];
  private DownloadHandler downloadHandler = null;
  private static int startWord = 0;
  private static int startWordToPrepare = 0;
	private static int curWord = 0;
  private static int startOffset = 0;
  private final int MIN_SPEED = 400;
  private final int MED_SPEED = 200;
  private final int BIG_SPEED = 100;
  private int curSpeed = MED_SPEED; // 100-200-400
	private static LinkedList<TextView> tv;
  private LinkedList<Integer> wordWidth;
	private RelativeLayout rLayout;
	private static OnTouchListener onTouch;
	private float textSize;
	private float mediumSize;
	private String fullBook[] = {" "};
	private int numOfWords = 0;
	private float spaceWidth = 0;
  private TextView word;
  private int language;
  private boolean doWeNeedToResize = false;
  private boolean doWeStartedreadFile = false;
  private static boolean isSpeedableUp = true;
  private static boolean isSpeedableDown = true;
  private static SpeedContainter spCont;
  private boolean isDownloaded = false;
  private static Book book;
  private static int counter = 0;
  private int width;
  private int height;
  private static int textColor = Color.BLUE;
  public boolean getState() {return isDownloaded;}
  public static void destroyCounter() {counter = 0;}
  private Book() {}
  static Book getBook(Context cnt, RelativeLayout rl, TextView word, SpeedContainter spC, int language) throws InterruptedException {
    if (counter == 0)
    {
      Log.w("BOOK", "CREATING new Book");
      book = new Book();
      counter++;
      book.mediumSize = book.textSize = (float) (word.getTextSize() / 2.5);
    }
    else
    {
      Log.w("BOOK", "FINDING old Book");
      spCont.btnUp.setEnabled(isSpeedableUp);
      spCont.btnDown.setEnabled(isSpeedableDown);
      spCont.btnUp.setBackgroundColor(isSpeedableUp ? Color.WHITE : Color.GRAY);
      spCont.btnDown.setBackgroundColor(isSpeedableDown ? Color.WHITE : Color.GRAY);		        
    }      
    book.context = cnt;
    book.rLayout = rl;
    book.width = rl.getMeasuredWidth();
    book.height = rl.getMeasuredHeight();
  	book.word = word;
  	book.language = language;
  	lock = new ReentrantLock();
  	onTouch = new OnTouchListener() {

      @SuppressLint("ClickableViewAccessibility")
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          break;         
          case MotionEvent.ACTION_UP:
            TextView text = (TextView)v;
            text.setTextColor(color.BlueViolet);
            for (int i = startWord; i < startWord + currentShowedTextViews.size() / 2; i++)
            {
              TextView t = tv.get(i - startOffset - currentIndexOffset);
              if (t.getCurrentTextColor() == color.BlueViolet)
              {
                t.setTextColor(textColor);
                curWord = i;
                book.setCurrentWord();
              }
              else
                t.setTextColor(Color.BLACK);
            }
            break;
          }
          return true;
      } 
  	};
  	
  	spCont = spC;
  	return book;
  }
  
  void changeTextColor( int color )
  {
    textColor = color;
    tv.get(curWord - startOffset - currentIndexOffset).setTextColor(textColor);
    if (curWord - startOffset - currentIndexOffset > 0)
      tv.get(curWord - startOffset - currentIndexOffset - 1).setTextColor(Color.BLACK);
  }
  
  void changeBackgroungColor( int color )
  {
    rLayout.setBackgroundColor(color);
    word.setBackgroundColor(color);
  }
  
  public float getTextSize()
  {
    return textSize;
  }
  
  public int getWidth()
  {
    return width;
  }
  
  public int getHeight()
  {
    return height;
  }
  
  void checkThready()
  {
    if (downloadHandler == null)
      return;
    downloadHandler.stop();
  }
  
  public int getNumOfWords()
  {
    return numOfWords;  
  }
  
  public void setLayoutSize()
  {
    book.width = rLayout.getMeasuredWidth();
    book.height = rLayout.getMeasuredHeight(); 
  }

  boolean turnPage()
  {
    if (curWord - startWord == (currentShowedTextViews.size() - 1) / 2)
    {
      startWord += (currentShowedTextViews.size() / 2);
      return true;
    }
    return false;
  }
  
  boolean download()
  {
    TextView t = new TextView(context);
    t.setText(fullBook[stringIndex]);
    t.setTextSize(textSize);
    t.setTextColor(Color.BLACK);
    t.setOnTouchListener(onTouch);
    t.measure(0, 0);   
    int realWidth = 0;
    while ((realWidth = t.getMeasuredWidth()) > width)
    {
      if (realWidth / width > 5)
      {
        isDownloaded = false;
        return false;
      }
      String text = t.getText().toString();
      String firstHalf;
      int normSize = 0;
      int index = 1;
      TextView normText = null;
      while (normSize < width - spaceWidth)
      {
        TextView nT = new TextView(context);
        nT.setTextSize(textSize);
        nT.setTextColor(Color.BLACK);
        firstHalf = new String(text.substring(0, index++));
        nT.setText(firstHalf);
        nT.measure(0, 0);
        normSize = nT.getMeasuredWidth();
        normText = nT;
      }
      normText.setOnTouchListener(onTouch);
      tv.add(normText);
      wordWidth.add(normSize);
      listIndex++;
      String lastHalf = text.substring(index, text.length());
      t = new TextView(context);
      t.setTextSize(textSize);
      t.setTextColor(Color.BLACK);  
      t.setText(lastHalf);
      t.measure(0, 0);
    }
    t.setOnTouchListener(onTouch);
    tv.add(t);
    wordWidth.add(realWidth);
    listIndex++;
    return true;
    
  }
  
  void checkRedownload()
  {
    if (startWord - startOffset >= thresHold + currentIndexOffset)
    {
      Log.d("BOOK", "checkRedownload(), startWord = " + startWord);
      try {
        if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
          for (int i = 0; i < thresHold; i++)
          {
            tv.removeFirst();
            wordWidth.removeFirst();
          }
          currentIndexOffset += thresHold;
        }
      } catch (InterruptedException e) {
          e.printStackTrace();
      } finally {
          lock.unlock();
      }
      downloadHandler = new DownloadHandler(currentStartDownloading, currentStartDownloading + thresHold);       
      executeThread();
    }
  }
  
  void executeThread()
  {
    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          downloadHandler.run();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    t.start();
  }
  
  int createViews( float size )
  {
    if (!isDownloaded)
      return 0;
    Log.w("BOOK", "START createViews");
    numOfWords = fullBook.length;
    // из списка еще не было удалений
    currentIndexOffset = 0;
    currentStartDownloading = thresHold + startWord;
    startOffset = startWord;
    if (mediumSize == 0)
      mediumSize = size;
    textSize = size;
    listIndex = 0;
    if (numOfWords == 0)
    {
      Log.e("BOOK", "empty book");
      return 0;
    }
    TextView space = new TextView(context);
    space.setText("O");
    space.setTextSize(textSize);
    space.measure(0, 0);
    spaceWidth = space.getMeasuredWidth();
    
    tv = new LinkedList<TextView>();
    wordWidth = new LinkedList<Integer>();
    for (stringIndex = startOffset; stringIndex < numOfWords; stringIndex++)
    {
      if (!download())
        return -1;
      if (listIndex >= currentStartDownloading - 1)
      {
        Log.w("BOOK", "Thresholding! i = " + stringIndex + ", word = " + fullBook[stringIndex]);
        downloadHandler = new DownloadHandler(currentStartDownloading, currentStartDownloading + thresHold);
        executeThread();
        break;
      }
    }
    return 1;
  }
  
  void curWordWasOutside()
  {
    boolean choice = false;
    while (curWord > startWord + currentShowedTextViews.size() / 2)
    {
      startWord += currentShowedTextViews.size() / 2;
      choice = true;
    } 
    if (choice)
    {
      prepareViews(false);
      placeViews();
    }
  }
  
  void prepareViewsFunction()
  {
    numOfWords = fullBook.length;
    params = new ArrayList<RelativeLayout.LayoutParams>();
    spaceParams = new ArrayList<RelativeLayout.LayoutParams>();
    space = new ArrayList<TextView>();
    currentPlacedParams = new ArrayList<RelativeLayout.LayoutParams>();
    currentPlacedTextViews = new ArrayList<TextView>();
    int currentIntexOfTextView = 0;
    float sumX = 0;
    float sumY = 0; 
    boolean addRule = false;
    int indexAbove = 0;
    final int SPACE_ID_START = 100000;
    int gotHeight = 0;
    for (int i = 0; ; i++)
    {
      if (i + startWordToPrepare >= numOfWords)
      {
        break;
      }
      currentIntexOfTextView = startWordToPrepare + i - startOffset - currentIndexOffset;
      RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      RelativeLayout.LayoutParams spaceParam = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      TextView oneSpace = new TextView(context);
      TextView gotTextView = tv.get(currentIntexOfTextView);
      int gotWidth = wordWidth.get(currentIntexOfTextView);
      
      if (i == 0)
        gotHeight = gotTextView.getMeasuredHeight();
      gotTextView.setId(currentIntexOfTextView + 1);
      oneSpace.setText("O");
      oneSpace.setVisibility(View.INVISIBLE);
      oneSpace.setTextSize(textSize);
      oneSpace.setId(SPACE_ID_START + currentIntexOfTextView);
      space.add(oneSpace);
      
      if (sumX + gotWidth >= width)
      {
        sumY += gotHeight; 
        if (sumY + gotHeight >= height)
        {
          break;
        }
        sumX = gotWidth + spaceWidth;
        // i = 0 означает, что слово первое на странице
        if (i > 0)
        {
          TextView prevTextView = tv.get(currentIntexOfTextView - 1); 
          param.addRule(RelativeLayout.BELOW, prevTextView.getId());
          spaceParam.addRule(RelativeLayout.BELOW, prevTextView.getId());
          addRule = true;
          // —ейчас id = cur + 1, т.е. предыдущий будет cur
          indexAbove = currentIntexOfTextView;
        }
      }
      else
      {
        sumX += (gotWidth + spaceWidth);
        if (i > 0)
        {
          if (addRule)
          {
            param.addRule(RelativeLayout.BELOW, indexAbove);
            spaceParam.addRule(RelativeLayout.BELOW, indexAbove);
            addRule = false;
          }
          TextView prevTextView = tv.get(currentIntexOfTextView - 1); 
          //Log.i("BOOK", "placement. getting: " + (startWord + i - 1 - startOffset - currentIndexOffset) + " from " + tv.size());
          param.addRule(RelativeLayout.RIGHT_OF, space.get(i - 1).getId());
          param.addRule(RelativeLayout.ALIGN_BOTTOM, prevTextView.getId());
          spaceParam.addRule(RelativeLayout.ALIGN_BOTTOM, prevTextView.getId());
        }
      }
      
      //rLayout.addView(gotTextView, param);
      currentPlacedTextViews.add(gotTextView);
      currentPlacedParams.add(param);
      
      spaceParam.addRule(RelativeLayout.RIGHT_OF, gotTextView.getId());
      
      //rLayout.addView(space.get(i), spaceParam);
      currentPlacedTextViews.add(space.get(i));
      currentPlacedParams.add(spaceParam);
      
      params.add(param);
      spaceParams.add(param);
    }
    //setCurrentWord();
  }
  
  void prepareViews( final boolean choice )
  {
    if (!isDownloaded)
      return;
    Log.w("BOOK", "START prepareViews");
    if (choice)
    {
      Thread t = new Thread(new Runnable() {
        public void run() { 
            startWordToPrepare = startWord + currentShowedTextViews.size() / 2;
            prepareViewsFunction();
          }
        }
      );
      t.start();
    }
    else
    {
      startWordToPrepare = startWord;
      prepareViewsFunction();
    }
  }
  
  void placeViews()
  {
    if (!isDownloaded)
      return;
    Log.w("BOOK", "START placeViews");

    rLayout.removeAllViews();

    int size = currentPlacedTextViews.size();
    textViews = new TextView[size];
    currentShowedTextViews = new ArrayList<>(currentPlacedTextViews);
    currentShowedParams = new ArrayList<>(currentPlacedParams);
    textViews = currentShowedTextViews.toArray(textViews);
    textParams = new RelativeLayout.LayoutParams[size];
    textParams = currentShowedParams.toArray(textParams);


    for (int i = 0; i < size; i++)
      rLayout.addView(textViews[i], textParams[i]);
    setCurrentWord();
    prepareViews(true);
  }

   void changeSpeed( int code )
   {
     switch (code)
     {
     case 0:
    	 curSpeed /= 2; 
    	 if (curSpeed <= BIG_SPEED)
    	 {
    	   spCont.btnUp.setEnabled(false);
    	   isSpeedableUp = false;
    	   spCont.btnUp.setBackgroundColor(Color.GRAY);
    	   curSpeed = BIG_SPEED;
    	 }
    	 else
    	 {
    	   spCont.btnUp.setEnabled(true);
    	   isSpeedableUp = true;
    	   spCont.btnUp.setBackgroundColor(Color.WHITE);
    	 }
    	 if (curSpeed < MIN_SPEED)
    	 {
    	   spCont.btnDown.setEnabled(true);
      	 isSpeedableDown = true;
      	 spCont.btnDown.setBackgroundColor(Color.WHITE);
    	 }
    break;
    case 1:
    	curSpeed *= 2;
       	 if (curSpeed >= MIN_SPEED)
       	 {
       	   spCont.btnDown.setEnabled(false);
       	   isSpeedableDown = false;
       	   spCont.btnDown.setBackgroundColor(Color.GRAY);
       	   curSpeed = MIN_SPEED;
       	 }
       	 else
       	 {
       	  spCont.btnDown.setEnabled(true);
       	  isSpeedableDown = true;
       	  spCont.btnDown.setBackgroundColor(Color.WHITE);
       	 }
       	 if (curSpeed > BIG_SPEED)
       	 {
       	   spCont.btnUp.setEnabled(true);
       	   isSpeedableUp = true;
       	   spCont.btnUp.setBackgroundColor(Color.WHITE);
       	 }
	      break;
	    }
   }
	   
   float changeTextSize( int code )
   {
     switch (code)
     {
     case 0:
    	 textSize = (float) (mediumSize / 1.25);
    	 break;
     case 1:
    	 textSize = mediumSize;
    	 break;
     case 2:
    	 textSize = (float) (mediumSize * 1.25);
    	 break;
     }
     return textSize;
   }
   
   boolean getNeedToResize()
   {
     return doWeNeedToResize;  
   }
   
   void needToResize()
   {
     doWeNeedToResize = true;  
   }
   
   boolean readFileNotFinished()
   {
     return doWeStartedreadFile;
   }
   
   void startReadFile()
   {
     doWeStartedreadFile = true;
   }
   
   void finishReadFile()
   {
     doWeStartedreadFile = false;
   }
   
   /* WHEN NEW BOOK IS OPENED. DETECTS CODING */
   void readFile( String chosenDir ) { 
     Log.w("BOOK", "START readFile");
     LinkedList<String> charsetsToBeTestedList = new LinkedList<String>();
     File f = new File(chosenDir);
     SortedMap<String, Charset> charsets = Charset.availableCharsets();
     Set<String> names = charsets.keySet();
     for (Iterator<String> e = names.iterator(); e.hasNext();) {
       String name = (String) e.next();
       Charset charset1 = (Charset) charsets.get(name);
       Set<String> aliases = charset1.aliases();
       for (Iterator<String> ee = aliases.iterator(); ee.hasNext();) {
         String str = (String)ee.next();
         charsetsToBeTestedList.add(str);
       }
     }
     CharsetDetector cd = new CharsetDetector();
     String[] charsetsToBeTested = charsetsToBeTestedList.toArray(new String[charsetsToBeTestedList.size()]);
     Charset charset = cd.detectCharset(f, charsetsToBeTested);
     
	   try {
			   StringBuffer fileData = new StringBuffer();
			   BufferedReader reader;
        if (charset == null) {
			     Log.e("BOOK", "!!!Unrecognized charset!!!");
			     reader = new BufferedReader(new InputStreamReader(
               new FileInputStream(chosenDir), "UTF-8"));
	        }
			    else
			    {
			      if (charset.toString().indexOf("ISO") != -1 || charset.toString().indexOf("cp") != -1)
			      {
			        reader = new BufferedReader(new InputStreamReader(new FileInputStream(chosenDir), "cp1251"));
			      }
			      else if (charset.toString().indexOf("Big5") != -1)
			        reader = new BufferedReader(new InputStreamReader(new FileInputStream(chosenDir), "UTF-8"));
			      else
			        reader = new BufferedReader(new InputStreamReader(new FileInputStream(chosenDir), charset));
			    } 
			    // the name of the c
	        char[] buf = new char[1024];
	        int numRead=0;
	        while((numRead=reader.read(buf)) != -1) {
	            String readData = String.valueOf(buf, 0, numRead);
	            fileData.append(readData);
	        } 
	        reader.close();
	    	  fullBook = fileData.toString().replaceAll("\n", " ").split(" ");
	    	  isDownloaded = true;
       } catch (FileNotFoundException e1) {
         e1.printStackTrace();
       } catch (IOException e1) {
         e1.printStackTrace();
       }
	     startOffset = currentIndexOffset = curWord = startWord = 0;
       Log.w("BOOK", "FINISHED readFile, fullBook.length = " + fullBook.length + ", coding = " + charset.toString());
       doWeNeedToResize = false;
   }
   
   public int getCurSpeed() {
     return curSpeed;
   }
   
   public int getLanguage() {
	   return language;
   }
   
   void increment()
   {
     curWord++;  
   }
   
   void decrement()
   {
     curWord--;
   }
   
   void setCurrentWord()
   {
     if (curWord - startOffset - currentIndexOffset < 0 || curWord - startOffset - currentIndexOffset >= tv.size())
     {
       Log.e("BOOK", "tv.size = " + tv.size() + ", but index = " + (curWord - startOffset - currentIndexOffset));
       return;
     }
	   word.setText(tv.get(curWord - startOffset - currentIndexOffset).getText().toString());
	   if (curWord - 1 - startOffset - currentIndexOffset >= 0)
	     tv.get(curWord - 1 - startOffset - currentIndexOffset).setTextColor(Color.BLACK);
	   tv.get(curWord - startOffset - currentIndexOffset).setTextColor(textColor);
   }
   
   String getCurrentWord()
   {
     return tv.get(curWord - startOffset - currentIndexOffset).getText().toString();
   }
   
   int getNextWordSize()
   {
     int index = curWord - startOffset - currentIndexOffset + 1;
     if (index >= tv.size())
       return 0;
     return tv.get(index).getText().toString().length();
   }
   
   int getCurrentWordIndex()
   {
     return curWord;
   }
   
   void setCurrentWordByIndex( int l )
   {
     curWord = l;
   }
   
   void switchOff()
   {
     isDownloaded = false;
     rLayout.removeAllViews();
     word.setText("");
   }
}
