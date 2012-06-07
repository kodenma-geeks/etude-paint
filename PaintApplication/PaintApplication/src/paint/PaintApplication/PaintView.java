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
	// �t�B�[���h
	private float oldX = 0f; // �ЂƂO��X���W�ێ�
	private float oldY = 0f; // �ЂƂO��Y���W�ێ�
	protected Path path; // �p�X����ێ�
	private Bitmap bitmap = null; // �L���b�V������L���v�`���摜

	protected static int undo = 0; // �A���h�D�����̂��߂̃J�E���g�ϐ�
	static boolean undoFlag = true;	// �ĕ`��o�O�̃e�X�g�t���O

	private AllLine pts = null; // �����N���X�̃C���X�^���X
	ArrayList<AllLine> draw_list = new ArrayList<AllLine>(); // �S�Ẵp�X����ێ�
	Paint paint;
	private static int color = Color.WHITE; // ���̐F
	private static int thick = 2; // ���̑���
	private static boolean antiAlias = true;	// �A���`�G�C���A�X

	final static int THICK_MAX = 50; 		// �����̍ő�l
	final static int THICK_MIN = 1; 		// �����̍ő�l	//	�l�c�ǉ�
	MediaScannerConnection mc; // ���f�B�A�X�L����
	onBgm onbgm = new onBgm();
	public MediaPlayer mp = null; // BGM�p
	public boolean bgmFlag = true; // BGMflag�p
	public static Context _context = null; 

//	PaintApplicationActivity paintApplicationActivity = new PaintApplicationActivity();
	PaintApplicationActivity paintAA = (PaintApplicationActivity)_context;
	
	// �R���X�g���N�^
	public PaintView(Context context) {
		super(context);
		_context = context;
		undo = 0;
		path = null;
		paint = null;
	}
//�R���X�g���N�^
	public PaintView(Context context, AttributeSet attrs) {
		  super(context, attrs);
		  undo = 0;
			path = null;
			paint = null;
	}

	// �`�掞�ɌĂяo��
	public void onDraw(Canvas canvas) {

		if (pts == null) { // ���������Ƃ��͕`�悵�Ȃ�
			return;
		}
		if (undoFlag) { 
			paint.setColor(color); // ���̐F
			paint.setAntiAlias(antiAlias); // �A���`�G�C���A�X�̗L��
			paint.setStyle(Paint.Style.STROKE); // ���̃X�^�C���iSTROKE�F�}�`�̗֊s���̂ݕ\���AFILL:�h��j
			paint.setStrokeWidth(thick); // ���̑���
			paint.setStrokeCap(Paint.Cap.ROUND); // �@���̐�[�X�^�C���iROUND�F�ۂ�����j
			paint.setStrokeJoin(Paint.Join.ROUND); // ���Ɛ��̐ڑ��_�̃X�^�C���iROUND�F�ۂ�����j
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

	// ��ʂ̃^�b�`���ɌĂяo��
	public boolean onTouchEvent(MotionEvent e) {

		// �^�b�`�C�x���g���菈��
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN: // �^�b�`���ĉ�ʂ���������

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

		case MotionEvent.ACTION_MOVE: // �^�b�`���Ă��痣���܂ł̈ړ����Ċ�
			undoFlag = true;
			float sq = (float)Math.sqrt((e.getX() - oldX)*(e.getX() - oldX)+(e.getY() - oldY)*(e.getY() - oldY));
			switch (PaintApplicationActivity.mode) {
			case MODE_LINE:
//				oldX += (e.getX() - oldX);
//				oldY += (e.getY() - oldY);
//				path.lineTo(oldX, oldY);
				// ���炩���[�h
				int TOLERANCE = 6;
				if (Math.abs(e.getX() - oldX) >= TOLERANCE || Math.abs(e.getY() - oldY) >= TOLERANCE) {
					path.quadTo(oldX, oldY, (oldX + e.getX()) / 2, (oldY + e.getY()) / 2);
				}
				oldX = e.getX(); oldY = e.getY();
				break;
				// ���@�X�^���v
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
//			draw_list.add(pts);//�l�c
			invalidate();			
			break;
		case MotionEvent.ACTION_UP: // �^�b�`���ė�������
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
			// �L���b�V������L���v�`�����쐬�A���̂��߃L���b�V����ON
			setDrawingCacheEnabled(true);
			bitmap = Bitmap.createBitmap(getDrawingCache());
			// �L���b�V���͂����Ƃ�Ȃ��̂ŃL���b�V����OFF
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

	// 1����߂�
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

	// 1����i��
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

	// �N���A
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

	// �摜�t�@�C����ۑ�
	public boolean isSaveToFile(PaintApplicationActivity paint) {
		// �ۑ���̌���(���݂��Ȃ��ꍇ�͍쐬)
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
		// ��ӂƂȂ�t�@�C�������擾�i�^�C���X�^���v�j
		Date d = new Date();
		String fileName = String.format("%4d%02d%02d-%02d%02d%02d.png",
				(1900 + d.getYear()), d.getMonth() + 1, d.getDate(),
				d.getHours(), d.getMinutes(), d.getSeconds());
		file = new File(path + fileName + ".png");
		// �摜���t�@�C���ɏ�������
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

	// �A���h�D�{�^��Enabled�������\�b�h
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
		return thick;//�l�c
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
//		public MediaPlayer mp = null; // BGM�p
//		public boolean bgmFlag = true; // BGMflag�p

		private Context getContext() {

			return _context;
		}

		public void onBgmran() {
			if (PaintApplicationActivity.bgmFlag == true) {
				int ran = (int) (Math.random() * 10) + 1;
				{
					switch (ran) {
					case 1: // BGM1�Ăяo��
						mp = MediaPlayer.create(getContext(), R.raw.bgm1);
						break;
					case 2: // BGM2�Ăяo��
						mp = MediaPlayer.create(getContext(), R.raw.bgm2);
						break;
					case 3: // BGM3�Ăяo��
						mp = MediaPlayer.create(getContext(), R.raw.bgm3);
						break;
					case 4: // BGM4�Ăяo��
						mp = MediaPlayer.create(getContext(), R.raw.bgm4);
						break;
					case 5: // BGM5�Ăяo��
						mp = MediaPlayer.create(getContext(), R.raw.bgm5);
						break;
					case 6: // BGM6�Ăяo��
						mp = MediaPlayer.create(getContext(), R.raw.bgm6);
						break;
					case 7: // BGM7�Ăяo��
						mp = MediaPlayer.create(getContext(), R.raw.bgm7);
						break;
					case 8: // BGM8�Ăяo��
						mp = MediaPlayer.create(getContext(), R.raw.bgm8);
						break;
					case 9: // BGM9�Ăяo��
						mp = MediaPlayer.create(getContext(), R.raw.bgm9);
						break;
					case 10: // BGM10�Ăяo��
						mp = MediaPlayer.create(getContext(), R.raw.bgm10);
						break;
					default:
						break;
					}
				}
			} else {
				mp = MediaPlayer.create(getContext(), R.raw.bgm0); // BGM�Ȃ�
			}
		}
		// }
	}
}