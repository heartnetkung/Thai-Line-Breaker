package hnk.lib.tlb;

import hnk.seed.thailinebreaker.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Demo extends Activity {
	Button button = null;
	EditText edit = null;
	ThaiLineBreakingTextView result = null;

	public Demo() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);
		button = (Button) findViewById(R.id.button);
		edit = (EditText) findViewById(R.id.editor);
		result = (ThaiLineBreakingTextView) findViewById(R.id.result);
		button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				result.setText2(edit.getText().toString());
			}
		});

		if (savedInstanceState != null) {
			result.setText2(savedInstanceState.getString("saved_string"));
			edit.setText(savedInstanceState.getString("saved_string2"));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("saved_string", result.getText2().toString());
		outState.putString("saved_string2", edit.getText().toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
}
