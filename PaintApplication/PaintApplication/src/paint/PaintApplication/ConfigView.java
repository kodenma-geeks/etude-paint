package paint.PaintApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ConfigView extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
	}

	public static Integer getBgColor(Context context) {
		int resIntBgColor = Integer.parseInt(PreferenceManager
				.getDefaultSharedPreferences(context).getString(
						"conf_bgcolor_key", ""));

		switch (resIntBgColor) {
		case 1:
			resIntBgColor = Color.YELLOW;
			break;
		case 2:
			resIntBgColor = Color.BLUE;
			break;
		case 3:
			resIntBgColor = Color.GREEN;
			break;
		case 4:
			resIntBgColor = Color.WHITE;
			break;
		case 5:
			resIntBgColor = Color.BLACK;
			break;
		}
		return resIntBgColor;
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		if (getString(R.string.conf_bgcolor_title).equals(key)) {
			editor.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
