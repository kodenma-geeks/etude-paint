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

		it = getIntent();						// �C���e���g�̎���
		thick = it.getIntExtra("THICK", 2);



		// limit�ݒ�̂��߂̃V�[�N�o�[
		seekBar = new SeekBar(this);   
	    seekBar.setMax(PaintView.THICK_MAX - 1);
	    seekBar.setProgress(thick+1);	// �V�[�N�o�[�̏����\��

		seekText = new TextView(this);   
		seekText.setText(String.valueOf(thick));					// �V�[�N�o�[�̏����l�\��
		

		okBtn = new Button(this);
		okBtn.setText("�ݒ�");
		okBtn.setOnClickListener(new SampleClickListener());

		canclBtn = new Button(this);
		canclBtn.setText("�L�����Z��");
		canclBtn.setOnClickListener(new SampleClickListener());

		ll.addView(seekBar);
		ll.addView(seekText);
		seekBar.setOnSeekBarChangeListener(this);   

		
		ll.addView(okBtn);
		ll.addView(canclBtn);

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
			index += 1;
			seekText.setText(Integer.toString(index));   
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
