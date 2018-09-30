package eu.davidea.examples.flipview;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import eu.davidea.flipview.FlipView;

public class FlipViewAdapter extends RecyclerView.Adapter<FlipViewAdapter.FlipViewHolder> {

	private static final String TAG = FlipViewAdapter.class.getSimpleName();
	private List<String> mItems = new ArrayList<>();

	public FlipViewAdapter(int item_count) {
		for (int i = 1; i <= item_count; i++) {
			mItems.add(String.valueOf(i));
		}
	}

	public String getItem(int position) {
		return mItems.get(position);
	}
		
	@Override
	public FlipViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.recycler_view_item, parent, false);
		Log.d(TAG, "onCreateViewHolder");
		return new FlipViewHolder(view);
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	@Override
	public void onBindViewHolder(FlipViewHolder holder, int position) {
		Log.d(TAG, "Binding position "+position);
		holder.mFlipView.setFrontText(getItem(position));
	}

	/**
	 * Provide a reference to the views for each data item.
	 * Complex data items may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	static final class FlipViewHolder extends RecyclerView.ViewHolder {
		
		FlipView mFlipView;

		public FlipViewHolder(View view) {
			super(view);
			this.mFlipView = (FlipView) view.findViewById(R.id.flip_view);
		}



	}
	
}