package paint.PaintApplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class ConfigView extends Activity {
	Intent it;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		
		it = getIntent();
		
	}
	
	class PaintAppClickListener implements OnClickListener{
		public void onClick(View v){			
			if(v.getId() == R.id.btn_apply){	// �K�p�{�^������
				//�e�ݒ荀�ڂ�Class���w�肷��
				// Intent it = new Intent(getApplicationContext(),***.class);
				//
				setResult(RESULT_OK, it);	// ���ʂ����C����ʂɕԂ�
				finish();	// ��ʂ����		
			}
		}
	}
}
