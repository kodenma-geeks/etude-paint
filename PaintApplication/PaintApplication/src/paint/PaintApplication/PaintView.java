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
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class PaintView extends View {
	// �`��G�������g�̌`��
	private final int MODE_LINE = -1;
	private final int MODE_STAMP_TRIANGLE = 0;
	private final int MODE_STAMP_RECTANGLE = 1;
	private final int MODE_STAMP_CIRCLE = 2;
	private final int MODE_STAMP_STAR = 3;
	private final int MODE_STAMP_TRIANGLE_DURATION = 4;
	private final int MODE_STAMP_RECTANGLE_DURATION = 5;
	private final int MODE_STAMP_CIRCLE_DURATION = 6;
	private final int MODE_STAMP_STAR_DURATION = 7;
	// Move�C�x���g��������̋��e�l
	private final int TOLERANCE = 3;
	
	// �`��G�������g
	private class Element {
		private Path path = new Path(); 						// path����ێ�
		private Paint paint = new Paint(); 						// paint����ێ�
		public boolean eraser =  eraserMode;					// �����S�����ǂ���
		private Element(){
			paint.setColor(eraserMode? bgColor : brushColor);	// �`��G�������g�̐F
			paint.setAntiAlias(antiAlias);						// �A���`�G�C���A�X�̗L��
			paint.setStyle(Paint.Style.STROKE);					// ��̃X�^�C���iSTROKE�F�}�`�̗֊s��̂ݕ\���AFILL:�h��j
			paint.setStrokeWidth(thickness);					// ��̑���
			paint.setStrokeCap(Paint.Cap.ROUND);				// �@��̐�[�X�^�C���iROUND�F�ۂ�����j
			paint.setStrokeJoin(Paint.Join.ROUND);				// ��Ɛ�̐ڑ��_�̃X�^�C���iROUND�F�ۂ�����j
		}
	}
	private Element element = null;			// �`��G�������g
	private ArrayList<Element> elements = new ArrayList<Element>();
	// �C�x���g�ʒu
	private PointF oldPos  = new PointF();	// �O��̃C�x���g�ʒu
	private PointF newPos  = new PointF();	// ����̃C�x���g�ʒu
	private PointF downPos = new PointF();	// ACTION_DOWN�̃C�x���g�ʒu
	// �e�탂�[�h
	private int elementMode = MODE_LINE;	// �`��G�������g�̌`��
	boolean eraserMode = false; 	// �����S�����[�h
	private boolean bgmMode = true;			// ��ʉ��o�̓��[�h
	// �e�푮��
	private int bgColor = Color.BLACK;		// �w�i�F
	private int brushColor = Color.WHITE;	// �u���V�̐F
	private int thickness = 2;				// �u���V�̑���
	private boolean antiAlias = true;		// �u���V�̃A���`�G�C���A�X
	// Undo, Redo, �֘A
	private int undo = 0;					// �A���h�D�����̂��߂̃J�E���g�ϐ�
	private boolean undoFlag = true;		// �ĕ`��o�O�̃e�X�g�t���O
	private ImageView ivUndo;				// Undo�{�^��
	private ImageView ivRedo;				// Rndo�{�^��
	ImageView ivEraser;				// Eraser�{�^��
	// ���̑�
	private BgmPlayer bgmPlayer;			// ��ʉ��o�̓I�u�W�F�N�g
	private MediaScannerConnection mc;		// ���f�B�A�X�L���i�ւ̃R�l�N�^

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
	int getBgColor()				{ return bgColor; }
	// �r���[�̃��C�A�E�g�m�莞
	@Override public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Activity parent = (Activity)getContext();
		ivUndo = (ImageView)parent.findViewById(R.id.imageView_undo);
		ivRedo = (ImageView)parent.findViewById(R.id.imageView_redo);
		ivEraser = (ImageView)parent.findViewById(R.id.imageView_eraser);
		setButtonEnabled(ivUndo, false);
		setButtonEnabled(ivRedo, false);
	}
	@Override public void onDraw(Canvas canvas) {
		if (element != null) { // �J�����g�̕`��G�������g�������Ƃ��͕`�悵�Ȃ�
			for (int i=0; i<elements.size() + undo; i++) {
				Element e = elements.get(i);
				if (e.eraser) e.paint.setColor(bgColor);
				canvas.drawPath(e.path, e.paint);
			}
			if (undoFlag) canvas.drawPath(element.path, element.paint);
		}
	}
	public boolean onTouchEvent(MotionEvent e) {
		newPos.x = e.getX();
		newPos.y = e.getY();
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN: // �^�b�`���ĉ�ʂ���������
			element = new Element();
			element.path.moveTo(newPos.x, newPos.y);
			downPos.x = newPos.x;
			downPos.y = newPos.y;
			
			if (bgmMode) bgmPlayer.start();
			break;
		case MotionEvent.ACTION_MOVE: // �^�b�`���Ă��痣���܂ł̈ړ����Ċ�
			undoFlag = true;
			switch (elementMode) {
			case MODE_LINE:
				// �ړ����������e�l�ȉ��̏ꍇ�͕`�悵�Ȃ�
				if (Math.abs(newPos.x - oldPos.x) >= TOLERANCE || Math.abs(newPos.y - oldPos.y) >= TOLERANCE) {
					// ���炩�Ȑ��`�悷��
					element.path.quadTo(oldPos.x, oldPos.y, (oldPos.x + newPos.x)/2, (oldPos.y + newPos.y)/2);
				}
				break;
			default:
				drawStamp(elementMode, element, downPos, newPos);
				break;
			}
			invalidate();			
			break;
		case MotionEvent.ACTION_UP: // �^�b�`���ė�������
			switch (elementMode) {
			case MODE_LINE:	element.path.lineTo(newPos.x, newPos.y);	break;
			default:		elementMode = MODE_LINE;					break;
			}
			// UNDO�̌�ɐV�����������݂����ꂽ�ۂ́A�Â������I�u�W�F�N�g�̍폜���s��
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
	// 1����߂�
	public void historyBack() {
		undo--;
		undoFlag = false;
		setButtonEnabled(ivRedo, true);
		if (elements.size() + undo == 0) setButtonEnabled(ivUndo, false);
		invalidate();
	}
	// 1����i��
	public void historyForward() {
		undo++;
		undoFlag = false;
		setButtonEnabled(ivUndo, true);
		if (undo == 0) setButtonEnabled(ivRedo, false);
		invalidate();
	}
	// �N���A
	public void clearPathList() {
		undo = 0;
		elements.clear();
		element = null;
		invalidate();
		setButtonEnabled(ivUndo, false);
		setButtonEnabled(ivRedo, false);
		eraserMode = false;
	}
	// Undo, Redo�{�^����enable/disable����
	private void setButtonEnabled(ImageView v, boolean enable){
		v.setEnabled(enable);
		v.setAlpha(enable? 255 : 128);
	}
	// �X�^���v�}�`��`�悷��B (m:mode, e:element, o:old position, n:new position)
	private void drawStamp(int m, Element e, PointF o, PointF n) {
		float sq = (float)Math.sqrt((n.x - o.x)*(n.x - o.x)+(n.y - o.y)*(n.y - o.y));
		switch (m) {
		case MODE_STAMP_TRIANGLE:	e.path.reset(); // ����break���Ȃ��B
		case MODE_STAMP_TRIANGLE_DURATION:
			e.path.moveTo(o.x, o.y-sq);
			e.path.lineTo(o.x-sq, o.y+sq);
			e.path.lineTo(o.x+sq, o.y+sq);
			e.path.lineTo(o.x, o.y-sq);		
			break;
		case MODE_STAMP_RECTANGLE:	e.path.reset(); // ����break���Ȃ��B
		case MODE_STAMP_RECTANGLE_DURATION:
			e.path.moveTo(o.x-sq, o.y-sq);
			e.path.lineTo(o.x+sq, o.y-sq);
			e.path.lineTo(o.x+sq, o.y+sq);
			e.path.lineTo(o.x-sq, o.y+sq);
			e.path.lineTo(o.x-sq, o.y-sq);
			break;
		case MODE_STAMP_CIRCLE:		e.path.reset(); // ����break���Ȃ��B
		case MODE_STAMP_CIRCLE_DURATION:
			e.path.addCircle(o.x, o.y, sq, Direction.CW);	
			break;
		case MODE_STAMP_STAR:		e.path.reset(); // ����break���Ȃ��B
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
	// �摜�t�@�C����ۑ�
	public boolean isSaveToFile(PaintApplicationActivity paint) {
		// �L���b�V������L���v�`�����쐬�A���̂��߃L���b�V����ON
		setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(getDrawingCache());
		// �L���b�V���͂����Ƃ�Ȃ��̂ŃL���b�V����OFF
		setDrawingCacheEnabled(false);
		// �ۑ���̌���(���݂��Ȃ��ꍇ�͍쐬)
		File file;
		String path = Environment.getExternalStorageDirectory() + "/PaintApplication/";
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			file = new File(path);
			file.mkdirs();
		} else {
			file = Environment.getDataDirectory();
		}
		Date d = new Date();
		// ��ӂƂȂ�t�@�C�������擾�i�^�C���X�^���v�j
		String fileName = String.format("%4d%02d%02d-%02d%02d%02d",
				(1900 + d.getYear()), d.getMonth() + 1, d.getDate(),
				d.getHours(), d.getMinutes(), d.getSeconds());
		file = new File(path + fileName + ".png");
		try { // �摜���t�@�C���ɏ�������
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
	// ���f�B�A�X�L���i�ɃX�L����������
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