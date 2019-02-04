package amd.spbstu.fastread;

import java.util.concurrent.TimeUnit;

import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

public class ReadingHandler extends Handler {
	private final int STATUS_STOP = 0;
	private final int STATUS_RUN = 1;
	private final int STATUS_ADD_WORD = 2;
	private Book myBook;
	private double coef = 1;
	private ButtonContainter myBtnCont;
	private boolean sendingMessagesLoop = false;
	public ReadingHandler(Book book, ButtonContainter btnCont) {
	  Log.w("BOOK", "CREATING new ReadingHandler");
		myBook = book;
		myBtnCont = btnCont;
		sendEmptyMessage(STATUS_STOP);
	}
	
	double coefMeasure( int length )
	{
	  if (length < 8)
	    return 1;
	  if (length > 16)
	    return 4;
    return length * 1.0 / 4;
	}
	
	public void run()
	{
	  try {
          sendEmptyMessage(STATUS_RUN);
          sendingMessagesLoop = true;
          while (sendingMessagesLoop)
          {
            sendEmptyMessage(STATUS_ADD_WORD);
            coef = coefMeasure(myBook.getNextWordSize());
            //Log.w("SOCKETS", "coef = " + coef);
     	      TimeUnit.MILLISECONDS.sleep((long) (myBook.getCurSpeed() * coef));
          }
 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
	
	public void stop()
	{
   	  sendEmptyMessage(STATUS_STOP);
   	  sendingMessagesLoop = false;
	}
	
	@Override
	public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
        case STATUS_STOP:
        	myBtnCont.btnStop.setEnabled(false);
        	myBtnCont.btnPlay.setEnabled(true);
        	myBtnCont.btnPlay.setBackgroundColor(Color.rgb(50, 150, 50));
        	myBtnCont.btnStop.setBackgroundColor(Color.GRAY); 
          break;
        case STATUS_RUN:
        	myBtnCont.btnStop.setEnabled(true);
        	myBtnCont.btnPlay.setEnabled(false);
        	myBtnCont.btnStop.setBackgroundColor(Color.rgb(150, 50, 50));
        	myBtnCont.btnPlay.setBackgroundColor(Color.GRAY);
        	break;
        case STATUS_ADD_WORD:
          if (!myBook.getState())
          {
            Log.d("BOOK", "END runReader");
            stop();
          }
          else
          {
            Log.w("BOOK", "START runReader");
            if (myBook.getCurrentWordIndex() >= myBook.getNumOfWords())
            {
              myBook.switchOff();
              Log.d("BOOK", "END runReader");
              stop();
              break;
            }
            myBook.checkRedownload();
            if (myBook.turnPage())
            {
              myBook.placeViews();
            }
            myBook.increment();
            myBook.setCurrentWord();
          }
          break;
       }
     }
	}
