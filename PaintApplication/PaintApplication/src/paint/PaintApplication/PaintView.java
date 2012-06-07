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
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
public class PaintView extends View {
	static final int MODE_LINE = -1;
	static final int MODE_STAMP_TRIANGLE = 0;
	static final int MODE_STAMP_RECTANGLE = 1;
	static final int MODE_STAMP_CIRCLE = 2;
	static final int MODE_STAMP_STAR = 3;
	static final int MODE_STAMP_TRIANGLE_DURATION = 4;
	static final int MODE_STAMP_RECTANGLE_DURATION = 5;
	static final int MODE_STAMP_CIRCLE_DURATION = 6;
	static final int MODE_STAMP_STAR_DURATION = 7;
	// フィールド
	private float oldX = 0f; // ひとつ前のX座標保持
	private float oldY = 0f; // ひとつ前のY座標保持
	protected Path path; // パス情報を保持
	private Bitmap bitmap = null; // キャッシュからキャプチャ画像

	protected static int undo = 0; // アンドゥ処理のためのカウント変数
	static boolean undoFlag = true;	// 再描画バグのテストフラグ

	private AllLine pts = null; // 線情報クラスのインスタンス
	ArrayList<AllLine> draw_list = new ArrayList<AllLine>(); // 全てのパス情報を保持
	Paint paint;
	private static int color = Color.WHITE; // 線の色
	private static int thick = 2; // 線の太さ
	private static boolean antiAlias = true;	// アンチエイリアス

	final static int THICK_MAX = 50; 		// 太さの最大値
	final static int THICK_MIN = 1; 		// 太さの最大値	//	浜田追加
	MediaScannerConnection mc; // メディアスキャン
	onBgm onbgm = new onBgm();
	public MediaPlayer mp = null; // BGM用
	public boolean bgmFlag = true; // BGMflag用
	public static Context _context = null; 

//	PaintApplicationActivity paintApplicationActivity = new PaintApplicationActivity();
	PaintApplicationActivity paintAA = (PaintApplicationActivity)_context;
	
	// コンストラクタ
	public PaintView(Context context) {
		super(context);
		_context = context;
		undo = 0;
		path = null;
		paint = null;
	}
//コンストラクタ
	public PaintView(Context context, AttributeSet attrs) {
		  super(context, attrs);
		  undo = 0;
			path = null;
			paint = null;
	}

	// 描画時に呼び出し
	public void onDraw(Canvas canvas) {

		if (pts == null) { // 線が無いときは描画しない
			return;
		}
		if (undoFlag) { 
			paint.setColor(color); // 線の色
			paint.setAntiAlias(antiAlias); // アンチエイリアスの有無
			paint.setStyle(Paint.Style.STROKE); // 線のスタイル（STROKE：図形の輪郭線のみ表示、FILL:塗る）
			paint.setStrokeWidth(thick); // 線の太さ
			paint.setStrokeCap(Paint.Cap.ROUND); // 　線の先端スタイル（ROUND：丸くする）
			paint.setStrokeJoin(Paint.Join.ROUND); // 線と線の接続点のスタイル（ROUND：丸くする）
		}

		for (int i = 0; i < draw_list.size() + undo; i++) {
			Path pt = draw_list.get(i).path;
			Paint pa = draw_list.get(i).paint;
			canvas.drawPath(pt, pa);
		}

		if (undoFlag) { 
			canvas.drawPath(path, paint);
		}
	}

	// 画面のタッチ時に呼び出し
	public boolean onTouchEvent(MotionEvent e) {

		// タッチイベント判定処理
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN: // タッチして画面を押した時

			pts = new AllLine();
			pts.paint = new Paint();
			pts.path = new Path();
			path = new Path();
			paint = new Paint();

			oldX = e.getX();
			oldY = e.getY();
			path.moveTo(oldX, oldY);

			onbgm.onBgmran();
		    mp.setLooping(true);
			mp.start();
			break;

		case MotionEvent.ACTION_MOVE: // タッチしてから離すまでの移動して間
			undoFlag = true;
			float sq = (float)Math.sqrt((e.getX() - oldX)*(e.getX() - oldX)+(e.getY() - oldY)*(e.getY() - oldY));
			switch (PaintApplicationActivity.mode) {
			case MODE_LINE:
//				oldX += (e.getX() - oldX);
//				oldY += (e.getY() - oldY);
//				path.lineTo(oldX, oldY);
				// 滑らかモード
				int TOLERANCE = 6;
				if (Math.abs(e.getX() - oldX) >= TOLERANCE || Math.abs(e.getY() - oldY) >= TOLERANCE) {
					path.quadTo(oldX, oldY, (oldX + e.getX()) / 2, (oldY + e.getY()) / 2);
				}
				oldX = e.getX(); oldY = e.getY();
				break;
				// △　スタンプ
			case MODE_STAMP_TRIANGLE:
				path.reset();
				path.moveTo(oldX, oldY-50f*(sq/50));
				path.lineTo(oldX-50f*(sq/50), oldY+20f*(sq/50));
				path.lineTo(oldX+50f*(sq/50), oldY+20f*(sq/50));
				path.lineTo(oldX, oldY-50f*(sq/50));		
				break;
			// ???@?X?^???v
			case MODE_STAMP_RECTANGLE:
				path.reset();
				path.moveTo(oldX-50f*(sq/50), oldY-50f*(sq/50));
				path.lineTo(oldX+50f*(sq/50), oldY-50f*(sq/50));
				path.lineTo(oldX+50f*(sq/50), oldY+50f*(sq/50));
				path.lineTo(oldX-50f*(sq/50), oldY+50f*(sq/50));
				path.lineTo(oldX-50f*(sq/50), oldY-50f*(sq/50));
				break;
			// ???@?X?^???v
			case MODE_STAMP_CIRCLE:
				path.reset();
				path.addCircle(oldX, oldY, 50f*(sq/50), Direction.CW);	
				break;
			// ???@?X?^???v
			case MODE_STAMP_STAR:
				path.reset();
				float theta = (float)(Math.PI * 72 / 180);
				float r = 50f;
				PointF center = new PointF(oldX, oldY);
				float dx1 = (float)(r*Math.sin(theta));
				float dx2 = (float)(r*Math.sin(2*theta));
				float dy1 = (float)(r*Math.cos(theta));
				float dy2 = (float)(r*Math.cos(2*theta));
				path.moveTo(center.x, center.y-r*(sq/50));
				path.lineTo(center.x-dx2*(sq/50), center.y-dy2*(sq/50));
				path.lineTo(center.x+dx1*(sq/50), center.y-dy1*(sq/50));
				path.lineTo(center.x-dx1*(sq/50), center.y-dy1*(sq/50));
				path.lineTo(center.x+dx2*(sq/50), center.y-dy2*(sq/50));
				path.lineTo(center.x, center.y-r*(sq/50));
				break;
				
			case MODE_STAMP_TRIANGLE_DURATION:
				path.moveTo(oldX, oldY-50f*(sq/50));
				path.lineTo(oldX-50f*(sq/50), oldY+20f*(sq/50));
				path.lineTo(oldX+50f*(sq/50), oldY+20f*(sq/50));
				path.lineTo(oldX, oldY-50f*(sq/50));		
				break;
			// ???@?X?^???v
			case MODE_STAMP_RECTANGLE_DURATION:
				path.moveTo(oldX-50f*(sq/50), oldY-50f*(sq/50));
				path.lineTo(oldX+50f*(sq/50), oldY-50f*(sq/50));
				path.lineTo(oldX+50f*(sq/50), oldY+50f*(sq/50));
				path.lineTo(oldX-50f*(sq/50), oldY+50f*(sq/50));
				path.lineTo(oldX-50f*(sq/50), oldY-50f*(sq/50));
				break;
			// ???@?X?^???v
			case MODE_STAMP_CIRCLE_DURATION:
				path.addCircle(oldX, oldY, 50f*(sq/50), Direction.CW);	
				break;
			// ???@?X?^???v
			case MODE_STAMP_STAR_DURATION:
				theta = (float)(Math.PI * 72 / 180);
				r = 50f;
				center = new PointF(oldX, oldY);
				dx1 = (float)(r*Math.sin(theta));
				dx2 = (float)(r*Math.sin(2*theta));
				dy1 = (float)(r*Math.cos(theta));
				dy2 = (float)(r*Math.cos(2*theta));
				path.moveTo(center.x, center.y-r*(sq/50));
				path.lineTo(center.x-dx2*(sq/50), center.y-dy2*(sq/50));
				path.lineTo(center.x+dx1*(sq/50), center.y-dy1*(sq/50));
				path.lineTo(center.x-dx1*(sq/50), center.y-dy1*(sq/50));
				path.lineTo(center.x+dx2*(sq/50), center.y-dy2*(sq/50));
				path.lineTo(center.x, center.y-r*(sq/50));
				break;
			default:
				break;
			}
//			draw_list.add(pts);//浜田
			invalidate();			
			break;
		case MotionEvent.ACTION_UP: // タッチして離した時
			switch (PaintApplicationActivity.mode) {
			case MODE_LINE:
				oldX = e.getX();
				oldY = e.getY();
				path.lineTo(oldX, oldY);
				break;
			default:
				break;
			}

			pts.path = path;
			pts.paint = paint;

			while (undo < 0) {
				AllLine previous = draw_list.get(draw_list.size() - 1);
				draw_list.remove(previous);
				previous.reset();
				invalidate();
				undo++;
			}

			pts.path = path;
			pts.paint = paint;
			draw_list.add(pts);
			paintAA.ivUndo = (ImageView) paintAA.findViewById(R.id.imageView_undo);
			paintAA.ivRedo = (ImageView) paintAA.findViewById(R.id.imageView_redo);

			
			setButtonEnabled(paintAA.ivUndo,true);
			setButtonEnabled(paintAA.ivRedo,false);
			// キャッシュからキャプチャを作成、そのためキャッシュをON
			setDrawingCacheEnabled(true);
			bitmap = Bitmap.createBitmap(getDrawingCache());
			// キャッシュはもうとらないのでキャッシュをOFF
			setDrawingCacheEnabled(false);
			PaintApplicationActivity.mode = MODE_LINE;
			invalidate();
			try {
				mp.stop();
			} catch (Exception er) {
			} finally {
				mp.release();
			}
			break;
		default:
			break;
		}
		return true;
	}

	// 1操作戻る
	public void historyBack() {
		undo--;
		setButtonEnabled(paintAA.ivRedo,true);
		if (draw_list.size() + undo == 0) {
			setButtonEnabled(paintAA.ivUndo,false);
		}
		undoFlag = false;

//		paintApplicationActivity.ivRedo.setEnabled(true);
//		paintApplicationActivity.ivRedo.setAlpha(255);
		invalidate();
	}

	// 1操作進む
	public void historyForward() {
		undo++;
		setButtonEnabled(paintAA.ivUndo,true);
		if (undo == 0) {
			setButtonEnabled(paintAA.ivRedo,false);
		}
		undoFlag = false;
//		path = draw_list.get(draw_list.size() + undo).path;
//		paint = draw_list.get(draw_list.size() + undo).paint;
		invalidate();
	}

	// クリア
	public void clearPathList() {
		draw_list.clear();
		pts = null;
		undo = 0;
		oldX = 0f;
		oldY = 0f;
		setButtonEnabled(paintAA.ivUndo,false);
		setButtonEnabled(paintAA.ivRedo,false);
		invalidate();
	}

	// 画像ファイルを保存
	public boolean isSaveToFile(PaintApplicationActivity paint) {
		// 保存先の決定(存在しない場合は作成)
		File file;
		String path = Environment.getExternalStorageDirectory()
				+ "/PaintApplication/";
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			file = Environment.getDataDirectory();
		} else {
			file = new File(path);
			file.mkdirs();
		}
		// 一意となるファイル名を取得（タイムスタンプ）
		Date d = new Date();
		String fileName = String.format("%4d%02d%02d-%02d%02d%02d.png",
				(1900 + d.getYear()), d.getMonth() + 1, d.getDate(),
				d.getHours(), d.getMinutes(), d.getSeconds());
		file = new File(path + fileName + ".png");
		// 画像をファイルに書き込む
		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			this.mediaScanExecute(paint, file.getPath());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// アンドゥボタンEnabled処理メソッド
	public static void setButtonEnabled(ImageView btn, boolean alpha){
		if(alpha){
			btn.setEnabled(true);
			btn.setAlpha(255);
		}
		else{
			btn.setEnabled(false);
			btn.setAlpha(128);		
		}
	}
	void mediaScanExecute(PaintApplicationActivity paint, final String file) {
		mc = new MediaScannerConnection(paint,
				new MediaScannerConnection.MediaScannerConnectionClient() {
					public void onScanCompleted(String path, Uri uri) {
						mc.disconnect();
					}

					public void onMediaScannerConnected() {
						mc.scanFile(file, "image/png");
					}
				});
		mc.connect();
	}


	public static int getColor() {
		return color;
	}

	public static void setColor(int color) {
		PaintView.color = color;
	}

	public static int getThick() {
		return thick;//浜田
	}

	public static void setThick(int futosa) {
		if(futosa < THICK_MIN){
			futosa = THICK_MIN;
		}
		PaintView.thick = futosa;
	}
	public static boolean isAntiAlias() {
		return antiAlias;
	}

	public static void setAntiAlias(boolean aa) {
		PaintView.antiAlias = aa;
	}

	public class onBgm {
//		public MediaPlayer mp = null; // BGM用
//		public boolean bgmFlag = true; // BGMflag用

		private Context getContext() {

			return _context;
		}

		public void onBgmran() {
			if (PaintApplicationActivity.bgmFlag == true) {
				int ran = (int) (Math.random() * 10) + 1;
				{
					switch (ran) {
					case 1: // BGM1呼び出し
						mp = MediaPlayer.create(getContext(), R.raw.bgm1);
						break;
					case 2: // BGM2呼び出し
						mp = MediaPlayer.create(getContext(), R.raw.bgm2);
						break;
					case 3: // BGM3呼び出し
						mp = MediaPlayer.create(getContext(), R.raw.bgm3);
						break;
					case 4: // BGM4呼び出し
						mp = MediaPlayer.create(getContext(), R.raw.bgm4);
						break;
					case 5: // BGM5呼び出し
						mp = MediaPlayer.create(getContext(), R.raw.bgm5);
						break;
					case 6: // BGM6呼び出し
						mp = MediaPlayer.create(getContext(), R.raw.bgm6);
						break;
					case 7: // BGM7呼び出し
						mp = MediaPlayer.create(getContext(), R.raw.bgm7);
						break;
					case 8: // BGM8呼び出し
						mp = MediaPlayer.create(getContext(), R.raw.bgm8);
						break;
					case 9: // BGM9呼び出し
						mp = MediaPlayer.create(getContext(), R.raw.bgm9);
						break;
					case 10: // BGM10呼び出し
						mp = MediaPlayer.create(getContext(), R.raw.bgm10);
						break;
					default:
						break;
					}
				}
			} else {
				mp = MediaPlayer.create(getContext(), R.raw.bgm0); // BGMなし
			}
		}
		// }
	}
}