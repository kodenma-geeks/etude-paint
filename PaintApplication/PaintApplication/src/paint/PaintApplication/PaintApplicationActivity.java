package paint.PaintApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class PaintApplicationActivity extends Activity {
	PaintView paintView;
	int selectColor;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		paintView = new PaintView(this);
		setContentView(paintView);
	}
//
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
		case R.id.backId: // 1操作戻るボタン押下時
			paintView.historyBack();
			break;
		case R.id.clearId: // クリアボタン押下時
			paintView.clearPathList();
			break;
		case R.id.saveId: // 保存ボタン押下時
			new AlertDialog.Builder(this)
					.setTitle(R.string.save)
					.setMessage(
							paintView.isSaveToFile(this) ? R.string.save_success
									: R.string.save_fail)
					.setPositiveButton(R.string.ok, null).show();
			break;
		case R.id.colorId: // カラーボタン押下時

			ColorPickerDialog mColorPickerDialog;

			mColorPickerDialog = new ColorPickerDialog(this,
					new ColorPickerDialog.OnColorChangedListener() {
						
						public void colorChanged(int color) {
							selectColor = color;
						}
					}, Color.BLACK);

			mColorPickerDialog.show();
			break;
		}
		return true;
	}
}