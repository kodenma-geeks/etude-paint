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
	private final int THICK_MAX = 50; 		// �����̍ő�l
	private final int THICK_MIN = 1; 		// �����̍ő�l
	
//	private SeekBar seekBar;
	private TextView seekText;   
	private Button okBtn, canclBtn;
	private RadioButton rbt, rbf;
	private Paint paint = new Paint(); 

	private Intent it;
	private int thick;
	private int color;
	private boolean antiAlias;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		it = getIntent();
		thick = it.getIntExtra("THICK", 2);
		color = it.getIntExtra("COLOR", Color.WHITE);
		antiAlias = it.getBooleanExtra("ANTIALIAS", true); 

		SampleLineView lv = new SampleLineView(this);

		// limit�ݒ�̂��߂̃V�[�N�o�[
		SeekBar seekBar = new SeekBar(this);   
	    seekBar.setMax(THICK_MAX - THICK_MIN);
	    seekBar.setProgress(thick + THICK_MIN);	// �V�[�N�o�[�̏����\��

		seekText = new TextView(this);   
		seekText.setText(getResources().getString(R.string.line_thick) + thick);		// �V�[�N�o�[�̏����l�\��

		okBtn = new Button(this);
		okBtn.setText(R.string.menu_config);
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
			rbt.setChecked(true);	//�@�A���`�G�C���A�X����{�^�����I��
		} else{
			rbf.setChecked(true);	//�@�A���`�G�C���A�X�Ȃ��{�^�����I��
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

//    public void onDraw(Canvas canvas){ // �Q��ONDRAW�����邪�A�����K�v
//		paint.setColor(Color.WHITE); // ���̐F
//		paint.setAntiAlias(true); // �A���`�G�C���A�X�̗L��
//		paint.setStyle(Paint.Style.STROKE); // ���̃X�^�C���iSTROKE�F�}�`�̗֊s���̂ݕ\���AFILL:�h��j
//		paint.setStrokeCap(Paint.Cap.ROUND); // �@���̐�[�X�^�C���iROUND�F�ۂ�����j
//		paint.setStrokeJoin(Paint.Join.ROUND); // ���Ɛ��̐ڑ��_�̃X�^�C���iROUND�F�ۂ�����j
//
//		canvas.drawLine(100, 500, 400, 500, paint);
//    }    
	// �V�[�N�o�[�\��
	public void onProgressChanged(SeekBar seekBar, int index, boolean fromUser){   
		if(fromUser){ 
//			thick = index + THICK_MIN;
//			seekText.setText(getResources().getString(R.string.line_thick) + thick);   
//			paint.setStrokeWidth(thick); // ���̑���
			index += THICK_MIN;
			seekText.setText(getResources().getString(R.string.line_thick) + index);   
			thick = index;
			
			paint.setStrokeWidth(thick);
		}   
	}
	public void onStartTrackingTouch(SeekBar seek){}   
	public void onStopTrackingTouch(SeekBar seekBar){}

	// �{�^���̏���
	class SampleClickListener implements OnClickListener {
		public void onClick(View v) {
			if (v == rbt) {
				antiAlias = true;
			} else if (v == rbf) {
				antiAlias = false;
			} else if (v == okBtn) {
				if (thick < THICK_MIN) thick = THICK_MIN;
		        it.putExtra("THICK", thick); // �����C���e���g�łȂ���΂Ȃ�Ȃ��́H
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
		// �f�B�X�v���C�̃C���X�^���X����
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();

		public void onDraw(Canvas cv){
			paint.setColor(color); // ���̐F
			paint.setAntiAlias(antiAlias); // �A���`�G�C���A�X�̗L��
			paint.setStyle(Paint.Style.STROKE); // ���̃X�^�C���iSTROKE�F�}�`�̗֊s���̂ݕ\���AFILL:�h��j
			paint.setStrokeCap(Paint.Cap.ROUND); // �@���̐�[�X�^�C���iROUND�F�ۂ�����j
			paint.setStrokeJoin(Paint.Join.ROUND); // ���Ɛ��̐ڑ��_�̃X�^�C���iROUND�F�ۂ�����j
			paint.setStrokeWidth(thick); // ���̑���
			cv.drawLine(WIDTH_MARGIN, HEIGHT, disp.getWidth()-WIDTH_MARGIN, HEIGHT, paint);
			invalidate();
	    }    
	}
}
