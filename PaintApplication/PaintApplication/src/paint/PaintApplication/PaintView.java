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
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

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
	protected Path path = null; // �p�X����ێ�
	private Bitmap bitmap = null; // �L���b�V������L���v�`���摜

	protected static int undo = 0; // �A���h�D�����̂��߂̃J�E���g�ϐ�

	private AllLine pts = null; // �����N���X�̃C���X�^���X
	ArrayList<AllLine> draw_list = new ArrayList<AllLine>(); // �S�Ẵp�X����ێ�
	protected Paint paint = null;
	private static int color = Color.WHITE; // ���̐F
	private static int futosa = 2; // ���̑���

	onBgm onbgm = new onBgm();
	public MediaPlayer mp = null; // BGM�p
	public boolean bgmFlag = true; // BGMflag�p
	public static Context _context = null; 

	// �R���X�g���N�^
	public PaintView(Context context) {
		super(context);
		_context = context;
	}
//�R���X�g���N�^
	public PaintView(Context context, AttributeSet attrs) {
		  super(context, attrs);
	}

	// �`�掞�ɌĂяo��
	public void onDraw(Canvas canvas) {

		if (pts == null) { // ���������Ƃ��͕`�悵�Ȃ�
			return;
		}
		pts.paint.setColor(color); // ���̐F
		pts.paint.setAntiAlias(true); // �A���`�G�C���A�X�̗L��
		pts.paint.setStyle(Paint.Style.STROKE); // ���̃X�^�C���iSTROKE�F�}�`�̗֊s���̂ݕ\���AFILL:�h��j
		pts.paint.setStrokeWidth(futosa); // ���̑���
		pts.paint.setStrokeCap(Paint.Cap.ROUND); // �@���̐�[�X�^�C���iROUND�F�ۂ�����j
		pts.paint.setStrokeJoin(Paint.Join.ROUND); // ���Ɛ��̐ڑ��_�̃X�^�C���iROUND�F�ۂ�����j
		for (int i = 0; i < draw_list.size() + undo; i++) {
			Path pt = draw_list.get(i).path;
			Paint pa = draw_list.get(i).paint;
			canvas.drawPath(pt, pa);
		}
		// if (pts.path != null) {
		if (path != null) {
			canvas.drawPath(path, pts.paint);
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

			oldX = e.getX();
			oldY = e.getY();
			path.moveTo(oldX, oldY);

			onbgm.onBgmran();
		    mp.setLooping(true);
			mp.start();
			break;

		case MotionEvent.ACTION_MOVE: // �^�b�`���Ă��痣���܂ł̈ړ����Ċ�
			switch (PaintApplicationActivity.mode) {
			case MODE_LINE:
				oldX += (e.getX() - oldX);
				oldY += (e.getY() - oldY);
				path.lineTo(oldX, oldY);
				break;
			// ���@�X�^���v
			case MODE_STAMP_TRIANGLE:
				path.reset();
				pts.path.moveTo(oldX, oldY - 50);
				pts.path.lineTo(oldX - 50f, oldY + 20f);
				pts.path.lineTo(oldX + 50f, oldY + 20f);
				pts.path.lineTo(oldX, oldY - 50f);
				break;
			// ���@�X�^���v
			case MODE_STAMP_RECTANGLE:
				path.reset();
				pts.path.moveTo(oldX - 50f, oldY - 50f);
				pts.path.lineTo(oldX + 50f, oldY - 50f);
				pts.path.lineTo(oldX + 50f, oldY + 50f);
				pts.path.lineTo(oldX - 50f, oldY + 50f);
				pts.path.lineTo(oldX - 50f, oldY - 50f);
				break;
			// ���@�X�^���v
			case MODE_STAMP_CIRCLE:
				path.reset();
				pts.path.addCircle(oldX, oldY, 50f, Direction.CW);
				break;
			// ���@�X�^���v
			case MODE_STAMP_STAR:
				path.reset();
				float theta = (float) (Math.PI * 72 / 180);
				float r = 50f;
				PointF center = new PointF(oldX, oldY);
				float dx1 = (float) (r * Math.sin(theta));
				float dx2 = (float) (r * Math.sin(2 * theta));
				float dy1 = (float) (r * Math.cos(theta));
				float dy2 = (float) (r * Math.cos(2 * theta));
				pts.path.moveTo(center.x, center.y - r);
				pts.path.lineTo(center.x - dx2, center.y - dy2);
				pts.path.lineTo(center.x + dx1, center.y - dy1);
				pts.path.lineTo(center.x - dx1, center.y - dy1);
				pts.path.lineTo(center.x + dx2, center.y - dy2);
				pts.path.lineTo(center.x, center.y - r);
				break;
				// ���@�X�^���v
				case MODE_STAMP_TRIANGLE_DURATION:
					pts.path.moveTo(oldX, oldY - 50);
					pts.path.lineTo(oldX - 50f, oldY + 20f);
					pts.path.lineTo(oldX + 50f, oldY + 20f);
					pts.path.lineTo(oldX, oldY - 50f);
					break;
				// ���@�X�^���v
				case MODE_STAMP_RECTANGLE_DURATION:
					pts.path.moveTo(oldX - 50f, oldY - 50f);
					pts.path.lineTo(oldX + 50f, oldY - 50f);
					pts.path.lineTo(oldX + 50f, oldY + 50f);
					pts.path.lineTo(oldX - 50f, oldY + 50f);
					pts.path.lineTo(oldX - 50f, oldY - 50f);
					break;
				// ���@�X�^���v
				case MODE_STAMP_CIRCLE_DURATION:
					pts.path.addCircle(oldX, oldY, 50f, Direction.CW);
					break;
				// ���@�X�^���v
				case MODE_STAMP_STAR_DURATION:
					theta = (float) (Math.PI * 72 / 180);
					r = 50f;
					center = new PointF(oldX, oldY);
					dx1 = (float) (r * Math.sin(theta));
					dx2 = (float) (r * Math.sin(2 * theta));
					dy1 = (float) (r * Math.cos(theta));
					dy2 = (float) (r * Math.cos(2 * theta));
					pts.path.moveTo(center.x, center.y - r);
					pts.path.lineTo(center.x - dx2, center.y - dy2);
					pts.path.lineTo(center.x + dx1, center.y - dy1);
					pts.path.lineTo(center.x - dx1, center.y - dy1);
					pts.path.lineTo(center.x + dx2, center.y - dy2);
					pts.path.lineTo(center.x, center.y - r);
					break;
			default:
				break;
			}
			draw_list.add(pts);
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

			while (undo < 0) {
				AllLine previous = draw_list.get(draw_list.size() - 1);
				draw_list.remove(previous);
				previous.reset();
				invalidate();
				undo++;
			}

			draw_list.add(pts);

			// �L���b�V������L���v�`�����쐬�A���̂��߃L���b�V����ON
			setDrawingCacheEnabled(true);
			bitmap = Bitmap.createBitmap(getDrawingCache());
			// �L���b�V���͂����Ƃ�Ȃ��̂ŃL���b�V����OFF
			setDrawingCacheEnabled(false);

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
	// public void historyBack() {
	// AllLine previous = null;
	// if(draw_list.size() > 0){
	// previous = draw_list.get(draw_list.size() - 1);
	// }
	// pts.path = null; //shima
	// draw_list.remove(previous);
	// previous.reset();
	// invalidate();
	// }
	public void historyBack() {
		if (draw_list.size() + undo == 0) {
			// .setEnabled(false);
			return;
		}
		undo--;
		// path = pts.path;
		path = null;
		invalidate();
	}

	// 1����i��
	public void historyForward() {
		if (undo == 0) {
			// .setEnabled(false);
			return;
		}
		path = draw_list.get(draw_list.size() + undo).path;
		paint = draw_list.get(draw_list.size() + undo).paint;
		undo++;
		invalidate();
	}

	// �N���A
	public void clearPathList() {
		draw_list.clear();
		pts = null;
		undo = 0;
		oldX = 0f;
		oldY = 0f;
		invalidate();
	}

	// png�t�@�C���Ƃ��ĉ摜�t�@�C����ۑ�
	public boolean isSaveToFile(PaintApplicationActivity paa) {
		// �ۑ���̌���
		String status = Environment.getExternalStorageState();
		File fout;
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			fout = Environment.getDataDirectory();
		} else {
			fout = new File(Environment.getExternalStorageDirectory()
					+ "/PaintApplication/");
			fout.mkdirs();
		}
		// ��ӂƂȂ�t�@�C�������擾
		Date d = new Date();
		String fname = fout.getAbsolutePath() + "/";
		fname += String.format("%4d%02d%02d-%02d%02d%02d.png",
				(1900 + d.getYear()), d.getMonth() + 1, d.getDate(),
				d.getHours(), d.getMinutes(), d.getSeconds());
		// �摜���t�@�C���ɏ�������
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

	public static int getColor() {
		return color;
	}

	public static void setColor(int color) {
		PaintView.color = color;
	}

	public static int getFutosa() {
		return color;
	}

	public static void setFutosa(int futosa) {
		PaintView.futosa = futosa;
	}

	public class onBgm {
//		public MediaPlayer mp = null; // BGM�p
//		public boolean bgmFlag = true; // BGMflag�p

		private Context getContext() {

			return _context;
		}

		public void onBgmran() {
			if (bgmFlag == true) {
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