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

	SeekBar seekBar;   
	TextView seekText;   

	Button okBtn, canclBtn;
	RadioButton rbt, rbf;
	RadioGroup rg;

	Intent it;
	int thick;
	boolean antiAlias;
	Paint paint = new Paint(); 
//	Canvas cv = new Canvas();
	
	final int WIDTH_MARGIN = 50;
	final int HEIGHT = 200;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		it = getIntent();						// �C���e���g�̎���
		thick = it.getIntExtra("THICK", 2);
		antiAlias = it.getBooleanExtra("ANTIALIAS", true); 

		SampleLineView lv = new SampleLineView(this);


		// limit�ݒ�̂��߂̃V�[�N�o�[
		seekBar = new SeekBar(this);   
	    seekBar.setMax(PaintView.THICK_MAX - PaintView.THICK_MIN);
	    seekBar.setProgress(thick + PaintView.THICK_MIN);	// �V�[�N�o�[�̏����\��


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
		rg = new RadioGroup(this);
		rg.addView(rbt);
		rg.addView(rbf);

		if(antiAlias){
			rbt.setChecked(true);	//�@�A���`�G�C���A�X����{�^�����I��
		}
		else{
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

    public void onDraw(Canvas canvas){
		paint.setColor(Color.WHITE); // ���̐F
		paint.setAntiAlias(true); // �A���`�G�C���A�X�̗L��
		paint.setStyle(Paint.Style.STROKE); // ���̃X�^�C���iSTROKE�F�}�`�̗֊s���̂ݕ\���AFILL:�h��j
		paint.setStrokeCap(Paint.Cap.ROUND); // �@���̐�[�X�^�C���iROUND�F�ۂ�����j
		paint.setStrokeJoin(Paint.Join.ROUND); // ���Ɛ��̐ڑ��_�̃X�^�C���iROUND�F�ۂ�����j

		canvas.drawLine(100, 500, 400, 500, paint);

    }    
	// �V�[�N�o�[�\��
	public void onProgressChanged(SeekBar seekBar,int index,boolean fromUser){   
		if(fromUser){ 
			index += PaintView.THICK_MIN;
			seekText.setText(getResources().getString(R.string.line_thick) + index);   
			thick = index;
			
			paint.setStrokeWidth(thick); // ���̑���

		}   
	}
	public void onStartTrackingTouch(SeekBar seek){   
	}   

	public void onStopTrackingTouch(SeekBar seekBar){   
	}


	// �{�^���̏���
	class SampleClickListener implements OnClickListener {

		public void onClick(View v) {
			if (v == rbt) {
				antiAlias = true;
			}
			else if (v == rbf) {
				antiAlias = false;
			}
			else if (v == okBtn) {
		        it.putExtra("THICK", thick);
				it.putExtra("ANTIALIAS", antiAlias); 

		        setResult(RESULT_OK, it);      	 
		        finish();
			}
			else if (v == canclBtn) {
		        setResult(RESULT_CANCELED, it);
		        finish();
			}
		}
	}
	
	class SampleLineView extends View {

		public SampleLineView(Context context) {
			super(context);
			// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
		}

		// �E�B���h�E�}�l�[�W���̃C���X�^���X�擾
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		// �f�B�X�v���C�̃C���X�^���X����
		Display disp = wm.getDefaultDisplay();

		public void onDraw(Canvas cv){
			paint.setColor(PaintView.getColor()); // ���̐F
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
