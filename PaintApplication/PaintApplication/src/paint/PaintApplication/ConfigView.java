package paint.PaintApplication;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ConfigView extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
	}
}
