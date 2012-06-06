package paint.PaintApplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


public class PaintApplicationFutosa extends Activity implements OnSeekBarChangeListener {

	SeekBar seekBar;   
	TextView seekText;   

	Button okBtn, canclBtn;

	Intent it;
	int thick;
	Paint paint = new Paint(); 
	Canvas cv = new Canvas();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		it = getIntent();						// インテントの受取り
		thick = it.getIntExtra("THICK", 2);



		// limit設定のためのシークバー
		seekBar = new SeekBar(this);   
	    seekBar.setMax(PaintView.THICK_MAX - 1);
	    seekBar.setProgress(thick+1);	// シークバーの初期表示

		seekText = new TextView(this);   
		seekText.setText(String.valueOf(thick));					// シークバーの初期値表示
		

		okBtn = new Button(this);
		okBtn.setText("設定");
		okBtn.setOnClickListener(new SampleClickListener());

		canclBtn = new Button(this);
		canclBtn.setText("キャンセル");
		canclBtn.setOnClickListener(new SampleClickListener());

		ll.addView(seekBar);
		ll.addView(seekText);
		seekBar.setOnSeekBarChangeListener(this);   

		
		ll.addView(okBtn);
		ll.addView(canclBtn);

	}

    public void onDraw(Canvas canvas){
		paint.setColor(Color.WHITE); // 線の色
		paint.setAntiAlias(true); // アンチエイリアスの有無
		paint.setStyle(Paint.Style.STROKE); // 線のスタイル（STROKE：図形の輪郭線のみ表示、FILL:塗る）
		paint.setStrokeCap(Paint.Cap.ROUND); // 　線の先端スタイル（ROUND：丸くする）
		paint.setStrokeJoin(Paint.Join.ROUND); // 線と線の接続点のスタイル（ROUND：丸くする）

		canvas.drawLine(100, 500, 400, 500, paint);

    }    
	// シークバー表示
	public void onProgressChanged(SeekBar seekBar,int index,boolean fromUser){   
		if(fromUser){ 
			index += 1;
			seekText.setText(Integer.toString(index));   
			thick = index;
			
			paint.setStrokeWidth(thick); // 線の太さ

		}   
	}
	public void onStartTrackingTouch(SeekBar seek){   
	}   

	public void onStopTrackingTouch(SeekBar seekBar){   
	}


	// ボタンの処理
	class SampleClickListener implements OnClickListener {

		public void onClick(View v) {
			if (v == okBtn) {
		        it.putExtra("THICK", thick);
		        setResult(RESULT_OK, it);      	 
		        finish();
			} else if (v == canclBtn) {
		        setResult(RESULT_CANCELED, it);
		        finish();
			}
		}
	}
}
