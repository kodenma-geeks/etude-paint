package paint.PaintApplication;

import android.app.*;
import android.content.*;
//import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class SettingMenu extends Activity
{
	Button bt1, bt2, bt3, bt4;
	int mode;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);
		
		bt1 = new Button(this);
		bt2 = new Button(this);
		bt3 = new Button(this);
		bt4 = new Button(this);

		bt1.setText("Å¢");
		bt2.setText("Å†");
		bt3.setText("Åõ");
		bt4.setText("Åô");
		
		ll.addView(bt1);
		ll.addView(bt2);
		ll.addView(bt3);
		ll.addView(bt4);
	
		bt1.setOnClickListener(new SampleClickListener());
		bt2.setOnClickListener(new SampleClickListener());
		bt3.setOnClickListener(new SampleClickListener());
		bt4.setOnClickListener(new SampleClickListener());
    }
	class SampleClickListener implements OnClickListener
    {
		public void onClick(View v)
    	{
			Intent it = new Intent(SettingMenu.this, PaintApplicationActivity.class);

			if(v == bt1)
			{
				mode = 0;
			}
			else if(v == bt2)
			{
				mode = 1;
			}
			else if(v == bt3)
			{
				mode = 2;
			}
			else if(v == bt4)
			{
				mode = 3;
			}
			it.putExtra("mode", mode);
			startActivity(it);
    	}
	}

}
