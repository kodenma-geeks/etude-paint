package paint.PaintApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;

public class PaintView extends View {
	// フィールド
	private float oldX = 0f; // ひとつ前のX座標保持
	private float oldY = 0f; // ひとつ前のY座標保持
	private Path path = null; // パス情報を保持
	private Bitmap bitmap = null; // キャッシュからキャプチャ画像
	ArrayList<Path> draw_list = new ArrayList<Path>(); // 全てのパス情報を保持

	private int color = Color.WHITE; // 線の色
	private int thick = 2; // 線の太さ

	// コンストラクタ
	public PaintView(Context context) {
		super(context);
	}

	// 描画時に呼び出し
	public void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(color); // 線の色
		paint.setAntiAlias(true); // アンチエイリアスの有無
		paint.setStyle(Paint.Style.STROKE); // 線のスタイル（STROKE：図形の輪郭線のみ表示、FILL:塗る）
		paint.setStrokeWidth(thick); // 線の太さ
		paint.setStrokeCap(Paint.Cap.ROUND); // 　線の先端スタイル（ROUND：丸くする）
		paint.setStrokeJoin(Paint.Join.ROUND); // 線と線の接続点のスタイル（ROUND：丸くする）
		for (int i = 0; i < draw_list.size(); i++) {
			Path pt = draw_list.get(i);
			canvas.drawPath(pt, paint);
		}
		if (path != null) {
			canvas.drawPath(path, paint);
		}
	}

	// 画面のタッチ時に呼び出し
	public boolean onTouchEvent(MotionEvent e) {
		// タッチイベント判定処理
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN: // タッチして画面を押した時
			path = new Path();
			oldX = e.getX();
			oldY = e.getY();
			path.moveTo(oldX, oldY);
			break;
		case MotionEvent.ACTION_MOVE: // タッチしてから離すまでの移動して間
			oldX += (e.getX() - oldX);
			oldY += (e.getY() - oldY);
			path.lineTo(oldX, oldY);
			invalidate();
			break;
		case MotionEvent.ACTION_UP: // タッチして離した時
			oldX = e.getX();
			oldY = e.getY();
			path.lineTo(oldX, oldY);
			draw_list.add(path);

			// キャッシュからキャプチャを作成、そのためキャッシュをON
			setDrawingCacheEnabled(true);
			bitmap = Bitmap.createBitmap(getDrawingCache());
			// キャッシュはもうとらないのでキャッシュをOFF
			setDrawingCacheEnabled(false);

			invalidate();
			break;
		default:
			break;
		}
		return true;
	}

	// 1操作戻る
	public void historyBack() {
		Path previous = draw_list.get(draw_list.size() - 1);
		draw_list.remove(previous);
		previous.reset();
		invalidate();
	}

	// クリア
	public void clearPathList() {
		draw_list.clear();
		oldX = 0f;
		oldY = 0f;
		path = null;
		invalidate();
	}

	// pngファイルとして画像ファイルを保存
	public boolean isSaveToFile(PaintApplicationActivity paa) {
		// 保存先の決定
		String status = Environment.getExternalStorageState();
		File fout;
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			fout = Environment.getDataDirectory();
		} else {
			fout = new File(Environment.getExternalStorageDirectory()
					+ "/PaintApplication/");
			fout.mkdirs();
		}
		// 一意となるファイル名を取得
		Date d = new Date();
		String fname = fout.getAbsolutePath() + "/";
		fname += String.format("%4d%02d%02d-%02d%02d%02d.png",
				(1900 + d.getYear()), d.getMonth()+1, d.getDate(), d.getHours(),
				d.getMinutes(), d.getSeconds());
		// 画像をファイルに書き込む
		try {
			FileOutputStream out = new FileOutputStream(fname);
			bitmap.compress(CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
}