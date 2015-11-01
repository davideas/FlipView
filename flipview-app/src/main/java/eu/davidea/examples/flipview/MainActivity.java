package eu.davidea.examples.flipview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import eu.davidea.flipview.FlipView;
import eu.davidea.examples.utils.Utils;

public class MainActivity extends AppCompatActivity {

	private static final String HTTPS = "https://";
	private static final String URL = "github.com/davideas/FlipView";
	private static final int ITEMS = 15;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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