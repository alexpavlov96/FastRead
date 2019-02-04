package amd.spbstu.fastread;

import android.graphics.Color;
import android.widget.Button;

public class ButtonContainter {
	Button btnPlay, btnStop;
	ButtonContainter( Button btnP, Button btnS )
	{
	  btnPlay = btnP;
	  btnStop = btnS;
	  btnPlay.setBackgroundColor(Color.rgb(50, 150, 50));
      btnStop.setBackgroundColor(Color.rgb(150, 50, 50));
	}
}
