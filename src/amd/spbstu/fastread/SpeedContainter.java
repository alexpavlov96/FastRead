package amd.spbstu.fastread;

import android.graphics.Color;
import android.widget.Button;

public class SpeedContainter {
	Button btnUp, btnDown;
	SpeedContainter( Button btnU, Button btnD )
	{
	  btnUp = btnU;
	  btnDown = btnD;
	  btnUp.setBackgroundColor(Color.WHITE);
	  btnDown.setBackgroundColor(Color.WHITE);
	}
}
