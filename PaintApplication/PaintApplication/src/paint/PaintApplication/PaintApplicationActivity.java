package paint.PaintApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class PaintApplicationActivity extends Activity implements View.OnClickListener {

	// 
	private final int INTENT_FOR_CONFIG_VIEW = 1;
	private final int INTENT_FOR_PAINT_APPLICATION_THICK = 2;
	
	private PaintView paintView;
	ImageView ivUndo;
	ImageView ivRedo;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);// メインにカスタムビューを追加
		paintView = (PaintView)findViewById(R.id.paintView);

		ImageView ivBrush = (ImageView) findViewById(R.id.imageView_brush);
		ImageView ivColor = (ImageView) findViewById(R.id.imageView_color);
		ImageView ivEraser = (ImageView) findViewById(R.id.imageView_eraser);
		ivUndo = (ImageView) findViewById(R.id.imageView_undo);
		ivRedo = (ImageView) findViewById(R.id.imageView_redo);

		ivBrush.setOnClickListener(this);
		ivColor.setOnClickListener(this);
		ivEraser.setOnClickListener(this);
		ivUndo.setOnClickListener(this);
		ivRedo.setOnClickListener(this);

		ivUndo.setEnabled(false);
		ivUndo.setAlpha(128);
		ivRedo.setEnabled(false);
		ivRedo.setAlpha(128);
	}
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.configId: // 設定ボタン押下時
			Intent it = new Intent(getApplicationContext(), ConfigView.class);
			startActivityForResult(it, INTENT_FOR_CONFIG_VIEW);
			break;
		case R.id.clearId: // クリアボタン押下時 (はい/いいえのダイアログ表示)
			AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
			adBuilder.setTitle(R.string.clear);
			adBuilder.setMessage(R.string.all_clear_message);
			adBuilder.setPositiveButton(getString(R.string.yes),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						paintView.clearPathList();
					}
				});
			adBuilder.setNegativeButton(getString(R.string.no),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
			adBuilder.show();
			break;
		case R.id.saveId: // 保存ボタン押下時
			new AlertDialog.Builder(this)
				.setTitle(R.string.save)
				.setMessage(paintView.isSaveToFile(this) ? R.string.save_success : R.string.save_fail)
				.setPositiveButton(R.string.ok, null).show();
			break;
		}
		return true;
	}
	public void onActivityResult(int reqcode, int result, Intent it) {
		// 太さも設定画面からの戻り処理
		switch(reqcode) {
		case INTENT_FOR_PAINT_APPLICATION_THICK:
			if (result == RESULT_OK) {
				int ft = it.getIntExtra("THICK", 2);
				paintView.setThick(ft);
				boolean aa = it.getBooleanExtra("ANTIALIAS", true);
				paintView.setAntiAlias(aa);
			}
			break;
		case INTENT_FOR_CONFIG_VIEW:
			int c = ConfigView.getBgColor(this); // 背景色変更処理
			paintView.setBackgroundColor(c);// 背景色変更処理
			paintView.bgColor =  c;
			paintView.mode = ConfigView.getStamp(this);
			paintView.bgmFlag = ConfigView.getSound(this);
			break;
		}
	}
	// ***********サブメニュークリックイベント*********************************
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imageView_brush:
			paintView.undoFlag = false;
			Intent intent = new Intent(this, PaintApplicationThick.class);
			intent.putExtra("THICK", paintView.getThick());
			intent.putExtra("COLOR", paintView.getColor());
			intent.putExtra("ANTIALIAS", paintView.isAntiAlias());
			startActivityForResult(intent, INTENT_FOR_PAINT_APPLICATION_THICK);
			break;
		case R.id.imageView_color:
			ColorPickerDialog dlg;
			dlg = new ColorPickerDialog(this, new ColorPickerDialog.OnColorChangedListener() {
				public void colorChanged(int color) {
					paintView.setColor(color);
				}
			}, Color.WHITE);
			dlg.show();
			Toast.makeText(getApplicationContext(), Integer.toHexString(paintView.getColor()),Toast.LENGTH_SHORT).show();
			break;
		case R.id.imageView_eraser:
			paintView.eraserMode = !paintView.eraserMode;
			break;
		case R.id.imageView_undo: paintView.historyBack(); break;
		case R.id.imageView_redo: paintView.historyForward(); break;
		}
	}
}