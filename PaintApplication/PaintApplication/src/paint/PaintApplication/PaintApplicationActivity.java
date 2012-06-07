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

public class PaintApplicationActivity extends Activity implements
		View.OnClickListener {

	public static final int SAMPLE_APP = 2;
	public static int PAINT_APP = 1;
	PaintView paintView;
//	public static int selectColor;
//	int color;
//	int thick;
//	final static int FUTOSA_MAX = 50; // �����̍ő�l
//	Intent it;
	static int mode = PaintView.MODE_LINE;
	// static int mode = PaintView.MODE_STAMP_CIRCLE;

	// �T�u���j���[�A�C�R���摜�錾

	ImageView ivBrush;
	ImageView ivColor;
	ImageView ivEraser;
	ImageView ivUndo;
	ImageView ivRedo;
//	static ImageView ivUndo;
//	static ImageView ivRedo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		paintView = new PaintView(this);
		setContentView(R.layout.main);// ���C���ɃJ�X�^���r���[��ǉ�
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

		ivUndo.setEnabled(false);
		ivUndo.setAlpha(128);
		ivRedo.setEnabled(false);
		ivRedo.setAlpha(128);

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
		case R.id.configId: // �ݒ�{�^��������

			Intent it = new Intent(getApplicationContext(), ConfigView.class);
			startActivityForResult(it, PAINT_APP);
			break;

		case R.id.clearId: // �N���A�{�^�������� (�͂�/�������̃_�C�A���O�\��)
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
			
		case R.id.saveId: // �ۑ��{�^��������
			new AlertDialog.Builder(this)
					.setTitle(R.string.save)
					.setMessage(
							paintView.isSaveToFile(this) ? R.string.save_success
									: R.string.save_fail)
					.setPositiveButton(R.string.ok, null).show();
			break;
		/*
		 * case R.id.colorId: // �J���[�{�^�������� ColorPickerDialog mColorPickerDialog;
		 * 
		 * mColorPickerDialog = new ColorPickerDialog(this, new
		 * ColorPickerDialog.OnColorChangedListener() {
		 * 
		 * public void colorChanged(int color) { selectColor = color;
		 * PaintView.setColor(color); } }, Color.WHITE);
		 * 
		 * mColorPickerDialog.show();
		 * 
		
		 */
//		// ����
//		case R.id.futosaId: // �����{�^��������
//			Intent intent = new Intent(PaintApplicationActivity.this,
//					PaintApplicationFutosa.class);
//			intent.putExtra("THICK", PaintView.getFutosa());
//			startActivityForResult(intent, SAMPLE_APP);
//			break;
		}

		return true;
	}

	public void onActivityResult(int reqcode, int result, Intent it) {
		// �������ݒ��ʂ���̖߂菈��
		if (reqcode == SAMPLE_APP) {
			if (result == RESULT_OK) {
				int ft = it.getIntExtra("THICK", 2);
				boolean aa = it.getBooleanExtra("ANTIALIAS", true);
				PaintView.setThick(ft);
				PaintView.setAntiAlias(aa);
			}
		} else{
			paintView.setBackgroundColor(ConfigView.getBgColor(this)); // �w�i�F�ύX����
			mode = ConfigView.getStamp(this);
		}
	}

	// ***********�T�u���j���[�N���b�N�C�x���g*********************************
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imageView_brush:
			PaintView.undoFlag = false;
			Intent intent = new Intent(PaintApplicationActivity.this, PaintApplicationThick.class);
			intent.putExtra("THICK", PaintView.getThick());
			intent.putExtra("ANTIALIAS", PaintView.isAntiAlias());
			startActivityForResult(intent, SAMPLE_APP);
			break;
		case R.id.imageView_color:
			ColorPickerDialog mColorPickerDialog;
			mColorPickerDialog = new ColorPickerDialog(this, new ColorPickerDialog.OnColorChangedListener() {
				public void colorChanged(int color) {
//					selectColor = color;
					PaintView.setColor(color);
				}
			}, Color.WHITE);
			mColorPickerDialog.show();
			Toast.makeText(getApplicationContext(), Integer.toHexString(PaintView.getColor()),Toast.LENGTH_SHORT).show();

			break;
		case R.id.imageView_eraser:
			Toast.makeText(this, "�܂��ł�", Toast.LENGTH_SHORT).show();
			break;
		case R.id.imageView_undo: paintView.historyBack(); break;
		case R.id.imageView_redo: paintView.historyForward(); break;
		}
	}

}