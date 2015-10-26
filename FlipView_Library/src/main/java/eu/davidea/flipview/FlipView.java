/*
 * Copyright (C) 2015 Davide Steduto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.davidea.flipview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

/**
 * FlipView is a View that displays 2 views by flipping the front view in favor
 * of the back view, and vice versa.
 * <p/>
 * Usage is very simple. You just need to add the view to any layout (like you
 * would do with any other View) and you are good to go. Of course, if you want
 * further customizations, you can assign values to the needed properties in the
 * layout or programmatically. Please, refer to those attributes documentation.
 * <p/>
 * By default, when the View is clicked, it will switch its state. The event is
 * is propagated with the listener {@link OnFlippingListener#onFlipped(FlipView, boolean)}.
 * Subscribe to that listener using {@link #setOnFlippingListener(OnFlippingListener)} method.
 *
 * @author Davide Steduto
 */
public class FlipView extends ViewFlipper {

	/**
	 * Custom Listener
	 */
	public interface OnFlippingListener {
		void onFlipped(FlipView flipView, boolean checked);
	}

	/**
	 * View listener
	 */
	private OnFlippingListener mFlippingListener = EMPTY_LISTENER;
	private static final OnFlippingListener EMPTY_LISTENER = new OnFlippingListener() {
		@Override
		public void onFlipped(FlipView flipView, boolean checked) {
		}
	};

	/**
	 * Child index to access the <i>front</i> view.
	 */
	private static final int FRONT_VIEW_INDEX = 0;

	/**
	 * Child index to access the <i>rear</i> view.
	 */
	private static final int REAR_VIEW_INDEX = 1;

	/**
	 * Accept Animation
	 */
	private Animation acceptAnimation = AnimationUtils.loadAnimation(
			getContext(), R.anim.scale);

	/**
	 * Styleable attributes
	 */
	private int rearColor;
	private int acceptImageResource;

	public FlipView(Context context) {
		super(context);
		init(null);
	}

	public FlipView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		//Necessary for SVG rendering
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		//Read and apply provided attributes
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.FlipView, 0, 0);


		a.getResourceId(R.styleable.FlipView_frontLayout, 0);
		a.getResourceId(R.styleable.FlipView_rearLayout, 0);

//		addView(LayoutInflater.from(getContext()).inflate(R.layout.flipview_front, this, false), FRONT_VIEW_INDEX);
//		addView(LayoutInflater.from(getContext()).inflate(R.layout.flipview_rear, this, false), REAR_VIEW_INDEX);
//		setRearColorResource(a.getResourceId(R.styleable.FlipView_rearColor, 0));
//		setAcceptImageResource(a.getResourceId(R.styleable.FlipView_rearAcceptImage, 0));
	}


	public void setOnFlippingListener(OnFlippingListener listener) {
		this.mFlippingListener = listener;
	}

}