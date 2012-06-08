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
	private class Element {
		public Path path = new Path(); 			// path情報を保持
		public Paint paint = new Paint(); 		// paint情報を保持
		boolean eraser =  false;
		private Element(){
			paint.setColor(color); // 線の色
			paint.setAntiAlias(antiAlias); // アンチエイリアスの有無
			paint.setStyle(Paint.Style.STROKE); // 線のスタイル（STROKE：図形の輪郭線のみ表示、FILL:塗る）
			paint.setStrokeWidth(thick); // 線の太さ
			paint.setStrokeCap(Paint.Cap.ROUND); // 　線の先端スタイル（ROUND：丸くする）
			paint.setStrokeJoin(Paint.Join.ROUND); // 線と線の接続点のスタイル（ROUND：丸くする）
		}
	}
	private final int MODE_LINE = -1;
	private final int MODE_STAMP_TRIANGLE = 0;
	private final int MODE_STAMP_RECTANGLE = 1;
	private final int MODE_STAMP_CIRCLE = 2;
	private final int MODE_STAMP_STAR = 3;
	private final int MODE_STAMP_TRIANGLE_DURATION = 4;
	private final int MODE_STAMP_RECTANGLE_DURATION = 5;
	private final int MODE_STAMP_CIRCLE_DURATION = 6;
	private final int MODE_STAMP_STAR_DURATION = 7;
	
	int mode = MODE_LINE;
	boolean bgmFlag = true;
	private float oldX = 0f; // ひとつ前のX座標保持
	private float oldY = 0f; // ひとつ前のY座標保持
	
	private ArrayList<Element> elements = new ArrayList<Element>(); // 全てのパス情報を保持
	private Element element = null; // 線情報クラスのインスタンス
//	private Element workingElement = null; // 線情報クラスのインスタンス
	
	private int undo = 0; // アンドゥ処理のためのカウント変数
	boolean undoFlag = true;	// 再描画バグのテストフラグ

	private int color = Color.WHITE; // 線の色
	private int thick = 2; // 線の太さ
	private boolean antiAlias = true;	// アンチエイリアス

	private MediaScannerConnection mc; // メディアスキャン
	private onBgm onbgm = new onBgm();
	private MediaPlayer mp = null; // BGM用
	private static Context _context = null; 

	PaintApplicationActivity paintAA;
	
	public PaintView(Context context) {
		this(context, null);
	}
	public PaintView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paintAA = (PaintApplicationActivity)getContext();
		undo = 0;
	}
	public void onDraw(Canvas canvas) {

		if (element == null) { // 線が無いときは描画しない
			return;
		}
		for (int i = 0; i < elements.size() + undo; i++) {
			Path pt = elements.get(i).path;
			Paint pa = elements.get(i).paint;
			canvas.drawPath(pt, pa);
		}
		if (undoFlag) { 
			canvas.drawPath(element.path, element.paint);
		}
	}
	public boolean onTouchEvent(MotionEvent e) {
		// タッチイベント判定処理
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN: // タッチして画面を押した時
			element = new Element();

			oldX = e.getX();
			oldY = e.getY();
			element.path.moveTo(oldX, oldY);

//			onbgm.onBgmran();
//		    mp.setLooping(true);
//			mp.start();
			break;

		case MotionEvent.ACTION_MOVE: // タッチしてから離すまでの移動して間
			undoFlag = true;
			float sq = (float)Math.sqrt((e.getX() - oldX)*(e.getX() - oldX)+(e.getY() - oldY)*(e.getY() - oldY));
			switch (mode) {
			case MODE_LINE:
				// 滑らかモード
				int TOLERANCE = 6;
				if (Math.abs(e.getX() - oldX) >= TOLERANCE || Math.abs(e.getY() - oldY) >= TOLERANCE) {
					element.path.quadTo(oldX, oldY, (oldX + e.getX()) / 2, (oldY + e.getY()) / 2);
				}
				oldX = e.getX(); oldY = e.getY();
				break;
				// △　スタンプ
			case MODE_STAMP_TRIANGLE:
				element.path.reset();
				element.path.moveTo(oldX, oldY-50f*(sq/50));
				element.path.lineTo(oldX-50f*(sq/50), oldY+20f*(sq/50));
				element.path.lineTo(oldX+50f*(sq/50), oldY+20f*(sq/50));
				element.path.lineTo(oldX, oldY-50f*(sq/50));		
				break;
			// ???@?X?^???v
			case MODE_STAMP_RECTANGLE:
				element.path.reset();
				element.path.moveTo(oldX-50f*(sq/50), oldY-50f*(sq/50));
				element.path.lineTo(oldX+50f*(sq/50), oldY-50f*(sq/50));
				element.path.lineTo(oldX+50f*(sq/50), oldY+50f*(sq/50));
				element.path.lineTo(oldX-50f*(sq/50), oldY+50f*(sq/50));
				element.path.lineTo(oldX-50f*(sq/50), oldY-50f*(sq/50));
				break;
			// ???@?X?^???v
			case MODE_STAMP_CIRCLE:
				element.path.reset();
				element.path.addCircle(oldX, oldY, 50f*(sq/50), Direction.CW);	
				break;
			// ???@?X?^???v
			case MODE_STAMP_STAR:
				element.path.reset();
				float theta = (float)(Math.PI * 72 / 180);
				float r = 50f;
				PointF center = new PointF(oldX, oldY);
				float dx1 = (float)(r*Math.sin(theta));
				float dx2 = (float)(r*Math.sin(2*theta));
				float dy1 = (float)(r*Math.cos(theta));
				float dy2 = (float)(r*Math.cos(2*theta));
				element.path.moveTo(center.x, center.y-r*(sq/50));
				element.path.lineTo(center.x-dx2*(sq/50), center.y-dy2*(sq/50));
				element.path.lineTo(center.x+dx1*(sq/50), center.y-dy1*(sq/50));
				element.path.lineTo(center.x-dx1*(sq/50), center.y-dy1*(sq/50));
				element.path.lineTo(center.x+dx2*(sq/50), center.y-dy2*(sq/50));
				element.path.lineTo(center.x, center.y-r*(sq/50));
				break;
				
			case MODE_STAMP_TRIANGLE_DURATION:
				element.path.moveTo(oldX, oldY-50f*(sq/50));
				element.path.lineTo(oldX-50f*(sq/50), oldY+20f*(sq/50));
				element.path.lineTo(oldX+50f*(sq/50), oldY+20f*(sq/50));
				element.path.lineTo(oldX, oldY-50f*(sq/50));		
				break;
			// ???@?X?^???v
			case MODE_STAMP_RECTANGLE_DURATION:
				element.path.moveTo(oldX-50f*(sq/50), oldY-50f*(sq/50));
				element.path.lineTo(oldX+50f*(sq/50), oldY-50f*(sq/50));
				element.path.lineTo(oldX+50f*(sq/50), oldY+50f*(sq/50));
				element.path.lineTo(oldX-50f*(sq/50), oldY+50f*(sq/50));
				element.path.lineTo(oldX-50f*(sq/50), oldY-50f*(sq/50));
				break;
			// ???@?X?^???v
			case MODE_STAMP_CIRCLE_DURATION:
				element.path.addCircle(oldX, oldY, 50f*(sq/50), Direction.CW);	
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
				element.path.moveTo(center.x, center.y-r*(sq/50));
				element.path.lineTo(center.x-dx2*(sq/50), center.y-dy2*(sq/50));
				element.path.lineTo(center.x+dx1*(sq/50), center.y-dy1*(sq/50));
				element.path.lineTo(center.x-dx1*(sq/50), center.y-dy1*(sq/50));
				element.path.lineTo(center.x+dx2*(sq/50), center.y-dy2*(sq/50));
				element.path.lineTo(center.x, center.y-r*(sq/50));
				break;
			default:
				break;
			}
			invalidate();			
			break;
		case MotionEvent.ACTION_UP: // タッチして離した時
			switch (mode) {
			case MODE_LINE:
				oldX = e.getX();
				oldY = e.getY();
				element.path.lineTo(oldX, oldY);
				break;
			default:
				break;
			}
			// UNDOの後に新しい書き込みがされた際の、古い履歴オブジェクトの削除を行う
			while (undo < 0) {
				Element previous = elements.remove(elements.size() - 1);
				undo++;
			}
			elements.add(element);
			mode = MODE_LINE;
			invalidate();
			
			paintAA.ivUndo = (ImageView) paintAA.findViewById(R.id.imageView_undo);
			paintAA.ivRedo = (ImageView) paintAA.findViewById(R.id.imageView_redo);
			setButtonEnabled(paintAA.ivUndo,true);
			setButtonEnabled(paintAA.ivRedo,false);

//			try {
//				mp.stop();
//			} catch (Exception er) {
//			} finally {
//				mp.release();
//			}
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
		if (elements.size() + undo == 0) {
			setButtonEnabled(paintAA.ivUndo,false);
		}
		undoFlag = false;
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
		invalidate();
	}

	// クリア
	public void clearPathList() {
		elements.clear();
		element = null;
		undo = 0;
		oldX = 0f;
		oldY = 0f;
		setButtonEnabled(paintAA.ivUndo,false);
		setButtonEnabled(paintAA.ivRedo,false);
		invalidate();
	}

	// 画像ファイルを保存
	public boolean isSaveToFile(PaintApplicationActivity paint) {
		// キャッシュからキャプチャを作成、そのためキャッシュをON
		setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(getDrawingCache());
		// キャッシュはもうとらないのでキャッシュをOFF
		setDrawingCacheEnabled(false);
		
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
					public void onMediaScannerConnected() {
						mc.scanFile(file, "image/png");
					}
					public void onScanCompleted(String path, Uri uri) {
						mc.disconnect();
					}
				});
		mc.connect();
	}

	int getColor()				{ return color; }
	void setColor(int c)		{ color = c; }
	int getThick()				{ return thick; }
	void setThick(int futosa)	{ thick = futosa; }
	boolean isAntiAlias()		{ return antiAlias; }
	void setAntiAlias(boolean aa){ antiAlias = aa; }
	
	public class onBgm {
//		public MediaPlayer mp = null; // BGM用
//		public boolean bgmFlag = true; // BGMflag用

//		private Context getContext() {
//
//			return _context;
//		}

		public void onBgmran() {
			if (bgmFlag == true) {
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