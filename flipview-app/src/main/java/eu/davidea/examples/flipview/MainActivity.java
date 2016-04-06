package eu.davidea.examples.flipview;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import eu.davidea.flipview.FlipView;
import eu.davidea.utils.Utils;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity {

	private static final String HTTPS = "https://";
	private static final String URL = "github.com/davideas/FlipView";
	private static final int ITEMS = 15;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Enable DEBUG logs at runtime
		FlipView.enableLogs(true);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, URL, Snackbar.LENGTH_LONG)
						.setAction("Github", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setData(Uri.parse(HTTPS+URL));
								startActivity(Intent.createChooser(intent, getString(R.string.intent_chooser)));
							}
						}).show();
			}
		});

		initializeRecyclerView();

		//Example to set background color to the front layout programmatically
		FlipView flipView = (FlipView) findViewById(R.id.flip_horizontal_oval_view_big);
		flipView.setChildBackgroundColor(FlipView.FRONT_VIEW_INDEX, Color.RED);

		//Handling flipping programmatically
		AppCompatCheckBox enableCheckBox = (AppCompatCheckBox) findViewById(R.id.flag_enable);
		enableCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//FlipView to enable/disable
				findViewById(R.id.flip_layout).setEnabled(!isChecked);
				FlipView flipView = (FlipView) findViewById(R.id.flip_horizontal_oval_view_locked);
				flipView.setClickable(isChecked);//View was set not clickable in the layout!

				if (isChecked) {
					flipView.setChildBackgroundColor(0, getResources().getColor(R.color.colorAccent));
					flipView.setFrontImage(R.drawable.ic_lock_open_white_24dp);
					findViewById(R.id.triangle).setBackgroundResource(R.drawable.triangle_red);
				} else {
					flipView.setChildBackgroundDrawable(0, R.drawable.circle_light_stroke);
					flipView.setFrontImage(R.drawable.ic_lock_white_24dp);
					findViewById(R.id.triangle).setBackgroundResource(R.drawable.triangle_green);
				}
				Log.d(MainActivity.class.getSimpleName(), isChecked ? "Layout pinned, FlipView unlocked" : "Layout auto-flip, FlipView locked");
			}
		});
	}

	private void initializeRecyclerView() {
		FlipViewAdapter adapter = new FlipViewAdapter(ITEMS);
		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new GridLayoutManager(this, ITEMS/3));
		recyclerView.setHasFixedSize(true);
		recyclerView.setAdapter(adapter);
	}

	public void showList(View view) {
		CheckBox checkBoxReset = (CheckBox) findViewById(R.id.checkbox_reset);
		if (checkBoxReset.isChecked()) FlipView.resetLayoutAnimationDelay();
		initializeRecyclerView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_about) {
			MessageDialog.newInstance(
					R.drawable.ic_info_grey600_24dp,
					getString(R.string.about_title),
					getString(R.string.about_body,
							Utils.getVersionName(this),
							Utils.getVersionCode(this)) )
					.show(getFragmentManager(), MessageDialog.TAG);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}