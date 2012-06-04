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
	public static int PAINT_APP = 1;
	PaintView paintView;
	public static int selectColor;
	int color;
	int futosa;
	final static int FUTOSA_MAX = 30; // 太さの最大値
	Intent it;
	static int mode = PaintView.MODE_LINE;
	// static int mode = PaintView.MODE_STAMP_CIRCLE;

	// サブメニューアイコン画像宣言

	ImageView ivBrush;
	ImageView ivColor;
	ImageView ivEraser;
	ImageView ivUndo;
	ImageView ivRedo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		paintView = new PaintView(this);
		setContentView(R.layout.main);//メインにカスタムビューを追加
		paintView = (PaintView) findViewById(R.id.paintView);

		ImageView ivBrush = (ImageView) findViewById(R.id.imageView_brush);
		ImageView ivColor = (ImageView) findViewById(R.id.imageView_color);
		ImageView ivEraser = (ImageView) findViewById(R.id.imageView_eraser);
		ImageView ivUndo = (ImageView) findViewById(R.id.imageView_undo);
		ImageView ivRedo = (ImageView) findViewById(R.id.imageView_redo);

		ivBrush.setOnClickListener(this);
		ivColor.setOnClickListener(this);
		ivEraser.setOnClickListener(this);
		ivUndo.setOnClickListener(this);
		ivRedo.setOnClickListener(this);

		// it = getIntent();
		// mode = it.getIntExtra("mode", 0);
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
		case R.id.settingId: // 1操作戻るボタン押下時
			/* paintView.historyBack(); */
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
					.setMessage(
							paintView.isSaveToFile(this) ? R.string.save_success
									: R.string.save_fail)
					.setPositiveButton(R.string.ok, null).show();
			break;
		/*
		 * case R.id.colorId: // カラーボタン押下時 ColorPickerDialog mColorPickerDialog;
		 * 
		 * mColorPickerDialog = new ColorPickerDialog(this, new
		 * ColorPickerDialog.OnColorChangedListener() {
		 * 
		 * public void colorChanged(int color) { selectColor = color;
		 * PaintView.setColor(color); } }, Color.WHITE);
		 * 
		 * mColorPickerDialog.show();
		 * 
		 * break; case R.id.configId: // 設定ボタン押下時 Intent it = new
		 * Intent(getApplicationContext(), ConfigView.class);
		 * startActivityForResult(it, PAINT_APP); break;
		 */
		}

		return true;
	}

	public void onActivityResult(int reqcode, int result, Intent it) {
		// 各設定項目の処理
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		// ===========背景色変更処理ここから=================================
		String resStrBgcolor = prefs.getString(
				getString(R.string.conf_bgcolor_key), "");
		int resIntBgColor = Integer.parseInt(resStrBgcolor);

		switch (resIntBgColor) {
		case 1:
			paintView.setBackgroundColor(Color.YELLOW);
			break;
		case 2:
			paintView.setBackgroundColor(Color.BLUE);
			break;
		case 3:
			paintView.setBackgroundColor(Color.RED);
			break;
		case 4:
			paintView.setBackgroundColor(Color.GREEN);
			break;
		case 5:
			paintView.setBackgroundColor(Color.WHITE);
			break;
		default:
			paintView.setBackgroundColor(Color.BLACK);
			break;
		}
		// ===========背景色変更処理ここまで=================================
	}

	// ***********サブメニュークリックイベント*********************************
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imageView_brush:
			Toast.makeText(this, "brush Click", Toast.LENGTH_SHORT).show();
			break;

		case R.id.imageView_color:
			Toast.makeText(this, "color Click", Toast.LENGTH_SHORT).show();
			break;

		case R.id.imageView_eraser:
			Toast.makeText(this, "eraser Click", Toast.LENGTH_SHORT).show();
			break;

		case R.id.imageView_undo:
			Toast.makeText(this, "undo Click", Toast.LENGTH_SHORT).show();
			break;

		case R.id.imageView_redo:
			Toast.makeText(this, "redo Click", Toast.LENGTH_SHORT).show();
			break;

		}

	}

}