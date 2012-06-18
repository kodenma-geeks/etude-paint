package paint.PaintApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

public class PaintApplicationActivity extends Activity {

	private final int INTENT_FOR_CONFIG_VIEW = 1;
	private final int INTENT_FOR_PAINT_APPLICATION_THICK = 2;
	private PaintView paintView;
	/*private ImageView ivEraser;*/

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		paintView = (PaintView)findViewById(R.id.paintView);
		new SubMenuClickListener(); // サブメニューボタンに関しては全てこのリスナーに任せる。anonymousなオブジェクトでよい。
		
		/*ivEraser = (ImageView) findViewById(R.id.imageView_eraser);*/
	}
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.configId:
			Intent it = new Intent(getApplicationContext(), ConfigView.class);
			startActivityForResult(it, INTENT_FOR_CONFIG_VIEW);
			break;
		case R.id.clearId:
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
		case R.id.saveId:
			new AlertDialog.Builder(this)
				.setTitle(R.string.save)
				.setMessage(paintView.isSaveToFile(this) ? R.string.save_success : R.string.save_fail)
				.setPositiveButton(R.string.ok, null).show();
			break;
		}
		return true;
	}
	public void onActivityResult(int reqcode, int result, Intent it) {
		switch(reqcode) {
		case INTENT_FOR_PAINT_APPLICATION_THICK:
			if (result == RESULT_OK) {
				paintView.setThickness(it.getIntExtra("THICK", 2));
				paintView.setAntiAlias(it.getBooleanExtra("ANTIALIAS", true));
			}
			break;
		case INTENT_FOR_CONFIG_VIEW:
			paintView.setBgColor(ConfigView.getBgColor(this));
			paintView.setBgmMode(ConfigView.getSound(this));
			paintView.setElementMode(ConfigView.getStamp(this));
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = prefs.edit();
			editor.clear(); 
			editor.commit();
			break;
		}
	}
	// サブメニューボタンのリスナー
	private class SubMenuClickListener implements View.OnClickListener {
		private SubMenuClickListener() {
			TypedArray tArray = getResources().obtainTypedArray(R.array.subMenuButtons);
			for (int i=0; i<tArray.length(); i++) {
				int resourceId = tArray.getResourceId(i, 0);
				ImageView imageView = (ImageView)findViewById(resourceId);
				imageView.setOnClickListener(this);
			}
		}
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.imageView_brush:
				Intent intent = new Intent(PaintApplicationActivity.this, PaintApplicationThick.class);
				intent.putExtra("THICK", paintView.getThickness());
				intent.putExtra("COLOR", paintView.getBrushColor());
				intent.putExtra("ANTIALIAS", paintView.isAntiAlias());
				intent.putExtra("BGCOLOR", paintView.getBgColor());
				startActivityForResult(intent, INTENT_FOR_PAINT_APPLICATION_THICK);
				break;
			case R.id.imageView_color:
				ColorPickerDialog dlg;
				dlg = new ColorPickerDialog(PaintApplicationActivity.this, new ColorPickerDialog.OnColorChangedListener() {
					public void colorChanged(int color) {
						paintView.setBrushColor(color);
					}
				}, paintView.getBrushColor());
				dlg.show();
				Toast.makeText(getApplicationContext(), Integer.toHexString(paintView.getBrushColor()),Toast.LENGTH_SHORT).show();
				break;
			case R.id.imageView_eraser:	
				paintView.toggleEraserMode();
				
				ImageView roll = (ImageView)v;
				RotateAnimation  anim = new RotateAnimation(0,360,roll.getWidth()/2,roll.getHeight()/2);
				anim.setDuration(1000);
				roll.startAnimation(anim);
				
				if (paintView.eraserMode) {
					paintView.ivEraser.setImageResource(R.drawable.eraser);
				Toast.makeText(getApplicationContext(), "消しゴムモード", Toast.LENGTH_LONG).show();

				} else {
					paintView.ivEraser.setImageResource(R.drawable.pen);
				Toast.makeText(getApplicationContext(), "描画モード", Toast.LENGTH_LONG).show();
				}
				break;

	
			case R.id.imageView_undo:	paintView.historyBack();		break;
			case R.id.imageView_redo:	paintView.historyForward();		break;
			}
		}
	}
}