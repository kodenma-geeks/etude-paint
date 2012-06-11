package paint.PaintApplication;

import java.util.Random;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.util.Log;

public class BgmPlayer {
	private static final String TAG = BgmPlayer.class.getSimpleName();
	
	private Context context;
	private MediaPlayer mediaPlayer;
	private int[] bgmIds;
	private Random rand = new Random();
	
	BgmPlayer(Context c) {
		context = c;
		TypedArray ar = context.getResources().obtainTypedArray(R.array.bgms);
		bgmIds = new int[ar.length()];
		for (int i=0; i<ar.length(); i++) bgmIds[i] = ar.getResourceId(i, 0);
	}
	void start() {
		int index = rand.nextInt(bgmIds.length);
		mediaPlayer = MediaPlayer.create(context, bgmIds[index]);
		mediaPlayer.setLooping(true);
		mediaPlayer.start();
	}
	void stop() {
		try {
			mediaPlayer.stop();
		} catch (Exception e) {
			Log.e(TAG, "メディアプレーヤーのstopに失敗しました。", e);
		} finally {
			mediaPlayer.release();
		}
	}
}
