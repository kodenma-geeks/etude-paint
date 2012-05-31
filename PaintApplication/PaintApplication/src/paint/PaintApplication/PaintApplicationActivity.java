package paint.PaintApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class PaintApplicationActivity extends Activity {
	public static int PAINT_APP = 1;
	PaintView paintView;
	int selectColor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		paintView = new PaintView(this);
		setContentView(paintView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.backId: // 1����߂�{�^��������
			paintView.historyBack();
			break;
		case R.id.clearId: // �N���A�{�^��������
			paintView.clearPathList();
			break;
		case R.id.saveId: // �ۑ��{�^��������
			new AlertDialog.Builder(this)
					.setTitle(R.string.save)
					.setMessage(
							paintView.isSaveToFile(this) ? R.string.save_success
									: R.string.save_fail)
					.setPositiveButton(R.string.ok, null).show();
			break;
		case R.id.colorId: // �J���[�{�^��������

			ColorPickerDialog mColorPickerDialog;

			mColorPickerDialog = new ColorPickerDialog(this,
					new ColorPickerDialog.OnColorChangedListener() {

						public void colorChanged(int color) {
							selectColor = color;
						}
					}, Color.BLACK);

			mColorPickerDialog.show();
			break;
		case R.id.configId: // �ݒ�{�^��������
			Intent it = new Intent(getApplicationContext(), ConfigView.class);
			startActivityForResult(it, PAINT_APP);
			break;
		}

		return true;
	}

	public void onActivityResult(int reqcode, int result, Intent it) {
		if(reqcode == PAINT_APP && result == RESULT_OK ){
			// �e�ݒ荀�ڂ̏���
		}
	}

}