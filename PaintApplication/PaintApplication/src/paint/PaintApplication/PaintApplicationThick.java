package paint.PaintApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


public class PaintApplicationThick extends Activity implements OnSeekBarChangeListener {

	private final int WIDTH_MARGIN = 50;
	private final int HEIGHT = 200;
	private final int THICK_MAX = 50; 		// 太さの最大値
	private final int THICK_MIN = 1; 		// 太さの最小値

	private TextView seekText;   
	private Button okBtn, canclBtn;
	private RadioButton rbt, rbf;
	private Paint paint = new Paint(); 

	private Intent it;
	private int thick;
	private int color;
	private int bgColor;
	private boolean antiAlias;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		it = getIntent();
		thick = it.getIntExtra("THICK", 2);
		color = it.getIntExtra("COLOR", Color.WHITE);
		bgColor = it.getIntExtra("BGCOLOR", Color.BLACK);
		antiAlias = it.getBooleanExtra("ANTIALIAS", true); 

		SampleLineView lv = new SampleLineView(this);
		lv.setBackgroundColor(bgColor);


		// limit設定のためのシークバー
		SeekBar seekBar = new SeekBar(this);   
	    seekBar.setMax(THICK_MAX - THICK_MIN);
	    seekBar.setProgress(thick + THICK_MIN);	// シークバーの初期表示

		seekText = new TextView(this);   
		seekText.setText(getResources().getString(R.string.line_thick) + thick);	// シークバーの初期値表示

		okBtn = new Button(this);
		okBtn.setText(R.string.strconf_apply);
		okBtn.setOnClickListener(new SampleClickListener());

		canclBtn = new Button(this);
		canclBtn.setText(R.string.strconf_cancel);
		canclBtn.setOnClickListener(new SampleClickListener());

		rbt = new RadioButton(this);
		rbf = new RadioButton(this);

		rbt.setText(R.string.antialias_on);
		rbf.setText(R.string.antialias_off);
		RadioGroup rg = new RadioGroup(this);
		rg.addView(rbt);
		rg.addView(rbf);

		if(antiAlias){
			rbt.setChecked(true);	///　アンチエイリアスありボタンをオン
		} else{
			rbf.setChecked(true);	///　アンチエイリアスなしボタンをオン
		}

		ll.addView(seekBar);
		ll.addView(rg);
		ll.addView(seekText);

		ll.addView(okBtn);
		ll.addView(canclBtn);
		ll.addView(lv);

		seekBar.setOnSeekBarChangeListener(this);   
		rbt.setOnClickListener(new SampleClickListener());
		rbf.setOnClickListener(new SampleClickListener());
	}
 
	// シークバー表示
	public void onProgressChanged(SeekBar seekBar, int index, boolean fromUser){   
		if(fromUser){ 
			index += THICK_MIN;
			seekText.setText(getResources().getString(R.string.line_thick) + index);   
			thick = index;
			paint.setStrokeWidth(thick);
		}   
	}
	public void onStartTrackingTouch(SeekBar seek){}   
	public void onStopTrackingTouch(SeekBar seekBar){}

	// ボタンの処理
	class SampleClickListener implements OnClickListener {
		public void onClick(View v) {
			if (v == rbt) {
				antiAlias = true;
			} else if (v == rbf) {
				antiAlias = false;
			} else if (v == okBtn) {
				if (thick < THICK_MIN) thick = THICK_MIN;
				it.putExtra("THICK", thick);		// 同じインテントでなければならないの？
				it.putExtra("ANTIALIAS", antiAlias); 
				setResult(RESULT_OK, it);      	 
				finish();
			} else if (v == canclBtn) {
		        setResult(RESULT_CANCELED, it);
		        finish();
			}
		}
	}
	class SampleLineView extends View {
		SampleLineView(Context context) { super(context); }
		// ディスプレイのインスタンス生成
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();

		public void onDraw(Canvas cv){
			paint.setColor(color);					// 線の色
			paint.setAntiAlias(antiAlias);			// アンチエイリアスの有無
			paint.setStyle(Paint.Style.STROKE); 	// 線のスタイル（STROKE：図形の輪郭線のみ表示、FILL:塗る）
			paint.setStrokeCap(Paint.Cap.ROUND);	// 線の先端スタイル（ROUND：丸くする）
			paint.setStrokeJoin(Paint.Join.ROUND);	// 線と線の接続点のスタイル（ROUND：丸くする）
			paint.setStrokeWidth(thick);			// 線の太さ
			cv.drawLine(WIDTH_MARGIN, HEIGHT, disp.getWidth()-WIDTH_MARGIN, HEIGHT, paint);
			invalidate();
	    }    
	}
}