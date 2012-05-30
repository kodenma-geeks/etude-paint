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
	// �t�B�[���h
	private float oldX = 0f; // �ЂƂO��X���W�ێ�
	private float oldY = 0f; // �ЂƂO��Y���W�ێ�
	private Path path = null; // �p�X����ێ�
	private Bitmap bitmap = null; // �L���b�V������L���v�`���摜
	ArrayList<Path> draw_list = new ArrayList<Path>(); // �S�Ẵp�X����ێ�

	private int color = Color.WHITE; // ���̐F
	private int thick = 2; // ���̑���

	// �R���X�g���N�^
	public PaintView(Context context) {
		super(context);
	}

	// �`�掞�ɌĂяo��
	public void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(color); // ���̐F
		paint.setAntiAlias(true); // �A���`�G�C���A�X�̗L��
		paint.setStyle(Paint.Style.STROKE); // ���̃X�^�C���iSTROKE�F�}�`�̗֊s���̂ݕ\���AFILL:�h��j
		paint.setStrokeWidth(thick); // ���̑���
		paint.setStrokeCap(Paint.Cap.ROUND); // �@���̐�[�X�^�C���iROUND�F�ۂ�����j
		paint.setStrokeJoin(Paint.Join.ROUND); // ���Ɛ��̐ڑ��_�̃X�^�C���iROUND�F�ۂ�����j
		for (int i = 0; i < draw_list.size(); i++) {
			Path pt = draw_list.get(i);
			canvas.drawPath(pt, paint);
		}
		if (path != null) {
			canvas.drawPath(path, paint);
		}
	}

	// ��ʂ̃^�b�`���ɌĂяo��
	public boolean onTouchEvent(MotionEvent e) {
		// �^�b�`�C�x���g���菈��
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN: // �^�b�`���ĉ�ʂ���������
			path = new Path();
			oldX = e.getX();
			oldY = e.getY();
			path.moveTo(oldX, oldY);
			break;
		case MotionEvent.ACTION_MOVE: // �^�b�`���Ă��痣���܂ł̈ړ����Ċ�
			oldX += (e.getX() - oldX);
			oldY += (e.getY() - oldY);
			path.lineTo(oldX, oldY);
			invalidate();
			break;
		case MotionEvent.ACTION_UP: // �^�b�`���ė�������
			oldX = e.getX();
			oldY = e.getY();
			path.lineTo(oldX, oldY);
			draw_list.add(path);

			// �L���b�V������L���v�`�����쐬�A���̂��߃L���b�V����ON
			setDrawingCacheEnabled(true);
			bitmap = Bitmap.createBitmap(getDrawingCache());
			// �L���b�V���͂����Ƃ�Ȃ��̂ŃL���b�V����OFF
			setDrawingCacheEnabled(false);

			invalidate();
			break;
		default:
			break;
		}
		return true;
	}

	// 1����߂�
	public void historyBack() {
		Path previous = draw_list.get(draw_list.size() - 1);
		draw_list.remove(previous);
		previous.reset();
		invalidate();
	}

	// �N���A
	public void clearPathList() {
		draw_list.clear();
		oldX = 0f;
		oldY = 0f;
		path = null;
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
				(1900 + d.getYear()), d.getMonth()+1, d.getDate(), d.getHours(),
				d.getMinutes(), d.getSeconds());
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

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
}