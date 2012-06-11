package paint.PaintApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
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
	// 描画エレメントの形状
	private final int MODE_LINE = -1;
	private final int MODE_STAMP_TRIANGLE = 0;
	private final int MODE_STAMP_RECTANGLE = 1;
	private final int MODE_STAMP_CIRCLE = 2;
	private final int MODE_STAMP_STAR = 3;
	private final int MODE_STAMP_TRIANGLE_DURATION = 4;
	private final int MODE_STAMP_RECTANGLE_DURATION = 5;
	private final int MODE_STAMP_CIRCLE_DURATION = 6;
	private final int MODE_STAMP_STAR_DURATION = 7;
	// Moveイベント発生判定の許容値
	private final int TOLERANCE = 3;
	
	// 描画エレメント
	private class Element {
		private Path path = new Path(); 						// path情報を保持
		private Paint paint = new Paint(); 						// paint情報を保持
		private boolean eraser =  eraserMode;					// 消しゴムかどうか
		private Element(){
			paint.setColor(eraserMode? bgColor : brushColor);	// 描画エレメントの色
			paint.setAntiAlias(antiAlias);						// アンチエイリアスの有無
			paint.setStyle(Paint.Style.STROKE);					// 線のスタイル（STROKE：図形の輪郭線のみ表示、FILL:塗る）
			paint.setStrokeWidth(thickness);					// 線の太さ
			paint.setStrokeCap(Paint.Cap.ROUND);				// 　線の先端スタイル（ROUND：丸くする）
			paint.setStrokeJoin(Paint.Join.ROUND);				// 線と線の接続点のスタイル（ROUND：丸くする）
		}
	}
	private Element element = null;			// 描画エレメント
	private ArrayList<Element> elements = new ArrayList<Element>();
	// イベント位置
	private PointF oldPos  = new PointF();	// 前回のイベント位置
	private PointF newPos  = new PointF();	// 今回のイベント位置
	private PointF downPos = new PointF();	// ACTION_DOWNのイベント位置
	// 各種モード
	private int elementMode = MODE_LINE;	// 描画エレメントの形状
	private boolean eraserMode = false; 	// 消しゴムモード
	private boolean bgmMode = true;			// 効果音出力モード
	// 各種属性
	private int bgColor = Color.BLACK;		// 背景色
	private int brushColor = Color.WHITE;	// ブラシの色
	private int thickness = 2;				// ブラシの太さ
	private boolean antiAlias = true;		// ブラシのアンチエイリアス
	// Undo, Redo, 関連
	private int undo = 0;					// アンドゥ処理のためのカウント変数
	private boolean undoFlag = true;		// 再描画バグのテストフラグ
	private ImageView ivUndo;				// Undoボタン
	private ImageView ivRedo;				// Rndoボタン
	// その他
	private BgmPlayer bgmPlayer;			// 効果音出力オブジェクト
	private MediaScannerConnection mc;		// メディアスキャナへのコネクタ

	public PaintView(Context context) { this(context, null); }
	public PaintView(Context context, AttributeSet attrs) {
		super(context, attrs);
		bgmPlayer = new BgmPlayer(getContext());
	}
	// setter & getter methods
	void toggleEraserMode()			{ eraserMode = !eraserMode; }
	void setElementMode(int mode)	{ elementMode = mode; }
	void setBgmMode(boolean mode)	{ bgmMode = mode; }
	void setBgColor(int c)			{ setBackgroundColor(bgColor = c); }
	void setBrushColor(int c)		{ brushColor = c; }
	void setThickness(int t)		{ thickness = t; }
	void setAntiAlias(boolean a)	{ antiAlias = a; }
	int getBrushColor()				{ return brushColor; }
	int getThickness()				{ return thickness; }
	boolean isAntiAlias()			{ return antiAlias; }
	// ビューのレイアウト確定時
	@Override public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Activity parent = (Activity)getContext();
		ivUndo = (ImageView)parent.findViewById(R.id.imageView_undo);
		ivRedo = (ImageView)parent.findViewById(R.id.imageView_redo);
		setButtonEnabled(ivUndo, false);
		setButtonEnabled(ivRedo, false);
	}
	@Override public void onDraw(Canvas canvas) {
		if (element != null) { // カレントの描画エレメントが無いときは描画しない
			for (int i=0; i<elements.size() + undo; i++) {
				Element e = elements.get(i);
				canvas.drawPath(e.path, e.paint);
			}
			if (undoFlag) { 
				canvas.drawPath(element.path, element.paint);
			}
		}
	}
	public boolean onTouchEvent(MotionEvent e) {
		newPos.x = e.getX();
		newPos.y = e.getY();
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN: // タッチして画面を押した時
			element = new Element();
			element.path.moveTo(newPos.x, newPos.y);
			downPos.x = newPos.x;
			downPos.y = newPos.y;
			
			if (bgmMode) bgmPlayer.start();
			break;
		case MotionEvent.ACTION_MOVE: // タッチしてから離すまでの移動して間
			undoFlag = true;
			switch (elementMode) {
			case MODE_LINE:
				// 移動距離が許容値以下の場合は描画しない
				if (Math.abs(newPos.x - oldPos.x) >= TOLERANCE || Math.abs(newPos.y - oldPos.y) >= TOLERANCE) {
					// 滑らかな線を描画する
					element.path.quadTo(oldPos.x, oldPos.y, (oldPos.x + newPos.x)/2, (oldPos.y + newPos.y)/2);
				}
				break;
			default:
				drawStamp(elementMode, element, downPos, newPos);
				break;
			}
			invalidate();			
			break;
		case MotionEvent.ACTION_UP: // タッチして離した時
			switch (elementMode) {
			case MODE_LINE:
				element.path.lineTo(newPos.x, newPos.y);
				break;
			}
			// UNDOの後に新しい書き込みがされた際の、古い履歴オブジェクトの削除を行う
			while (undo < 0) {
				elements.remove(elements.size() - 1);
				undo++;
			}
			elements.add(element);
			invalidate();
			
			setButtonEnabled(ivUndo,true);
			setButtonEnabled(ivRedo,false);
			if (bgmMode) bgmPlayer.stop();
			break;
		default:
			break;
		}
		oldPos.x = newPos.x;
		oldPos.y = newPos.y;
		return true;
	}
	// 1操作戻る
	public void historyBack() {
		undo--;
		undoFlag = false;
		setButtonEnabled(ivRedo, true);
		if (elements.size() + undo == 0) setButtonEnabled(ivUndo, false);
		invalidate();
	}
	// 1操作進む
	public void historyForward() {
		undo++;
		undoFlag = false;
		setButtonEnabled(ivUndo, true);
		if (undo == 0) setButtonEnabled(ivRedo, false);
		invalidate();
	}
	// クリア
	public void clearPathList() {
		undo = 0;
		elements.clear();
		element = null;
		invalidate();
		setButtonEnabled(ivUndo, false);
		setButtonEnabled(ivRedo, false);
	}
	// Undo, Redoボタンのenable/disable処理
	private void setButtonEnabled(ImageView v, boolean enable){
		v.setEnabled(enable);
		v.setAlpha(enable? 255 : 128);
	}
	// スタンプ図形を描画する。 (m:mode, e:element, o:old position, n:new position)
	private void drawStamp(int m, Element e, PointF o, PointF n) {
		float sq = (float)Math.sqrt((n.x - o.x)*(n.x - o.x)+(n.y - o.y)*(n.y - o.y));
		switch (m) {
		case MODE_STAMP_TRIANGLE:	e.path.reset(); // 敢てbreakしない。
		case MODE_STAMP_TRIANGLE_DURATION:
			e.path.moveTo(o.x, o.y-sq);
			e.path.lineTo(o.x-sq, o.y+sq);
			e.path.lineTo(o.x+sq, o.y+sq);
			e.path.lineTo(o.x, o.y-sq);		
			break;
		case MODE_STAMP_RECTANGLE:	e.path.reset(); // 敢てbreakしない。
		case MODE_STAMP_RECTANGLE_DURATION:
			e.path.moveTo(o.x-sq, o.y-sq);
			e.path.lineTo(o.x+sq, o.y-sq);
			e.path.lineTo(o.x+sq, o.y+sq);
			e.path.lineTo(o.x-sq, o.y+sq);
			e.path.lineTo(o.x-sq, o.y-sq);
			break;
		case MODE_STAMP_CIRCLE:		e.path.reset(); // 敢てbreakしない。
		case MODE_STAMP_CIRCLE_DURATION:
			e.path.addCircle(o.x, o.y, sq, Direction.CW);	
			break;
		case MODE_STAMP_STAR:		e.path.reset(); // 敢てbreakしない。
		case MODE_STAMP_STAR_DURATION:
			float theta = (float)(Math.PI * 72 / 180);
			float dx1 = (float)(Math.sin(theta));
			float dx2 = (float)(Math.sin(2*theta));
			float dy1 = (float)(Math.cos(theta));
			float dy2 = (float)(Math.cos(2*theta));
			PointF center = new PointF(o.x, o.y);
			e.path.moveTo(center.x, center.y-sq);
			e.path.lineTo(center.x-dx2*sq, center.y-dy2*sq);
			e.path.lineTo(center.x+dx1*sq, center.y-dy1*sq);
			e.path.lineTo(center.x-dx1*sq, center.y-dy1*sq);
			e.path.lineTo(center.x+dx2*sq, center.y-dy2*sq);
			e.path.lineTo(center.x, center.y-sq);
			break;
		default:
			break;
		}
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
		String path = Environment.getExternalStorageDirectory() + "/PaintApplication/";
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			file = new File(path);
			file.mkdirs();
		} else {
			file = Environment.getDataDirectory();
		}
		Date d = new Date();
		// 一意となるファイル名を取得（タイムスタンプ）
		String fileName = String.format("%4d%02d%02d-%02d%02d%02d.png",
				(1900 + d.getYear()), d.getMonth() + 1, d.getDate(),
				d.getHours(), d.getMinutes(), d.getSeconds());
		file = new File(path + fileName + ".png");
		try { // 画像をファイルに書き込む
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
	// メディアスキャナにスキャンさせる
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
}