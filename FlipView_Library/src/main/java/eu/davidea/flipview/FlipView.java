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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ViewFlipper;

/**
 * FlipView is a View that displays 2 views by flipping the front view in favor
 * of the back view, and vice versa.
 * <p/>
 * Usage is very simple. You just need to add the view to any layout (like you
 * would do with any other View) and you are good to go. You can customize the behaviours
 * by assigning values to the needed properties in the layout or programmatically.
 * Please, refer to those attributes documentation.
 * <p/>
 * By default, when the View is clicked, it will switch its state. The event is
 * propagated with the listener {@link OnFlippingListener#onFlipped(FlipView, boolean)}.
 * You can subscribe to that listener using {@link #setOnFlippingListener(OnFlippingListener)}
 * method.
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
	 * Use this to apply a default resource value.
	 */
	public static final int DEFAULT_RESOURCE = 0;
	public static final int
			SCALE_INITIAL_DELAY = 500,
			SCALE_DURATION = 250,
			SCALE_STEP_DELAY = 35,
			SCALE_STOP_ANIMATION_DELAY = 1500,
			FLIP_INITIAL_DELAY = 250,
			FLIP_DURATION = 125;

	/**
	 * Accept & Main start Animation
	 */
	private Animation checkAnimation =
			AnimationUtils.loadAnimation(getContext(), R.anim.scale);

	/* CONSTRUCTORS **************************************************/

	public FlipView(Context context) {
		super(context);
		init(null);
	}

	public FlipView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	/**
	 * Already part of the extended classes:
	 * <ul>
	 * <li>ViewAnimator_inAnimation - Identifier for the animation to use when a view is shown.</li>
	 * <li>ViewAnimator_outAnimation - Identifier for the animation to use when a view is hidden.</li>
	 * <li>ViewAnimator_animateFirstView - Defines whether to animate the current View when the ViewAnimation is first displayed.</li>
	 * <li>ViewFlipper_flipInterval - Time before next animation.</li>
	 * <li>ViewFlipper_autoStart - When true, automatically start animating.</li>
	 * </ul>
	 *
	 * @param attrs The view's attributes.
	 */
	private void init(AttributeSet attrs) {
		//Necessary for SVG rendering
		//setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		//Read and apply provided attributes
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.FlipView, 0, 0);

		//Init all animations
		long duration = a.getInteger(R.styleable.FlipView_flipAnimationDuration, FLIP_DURATION);
		initInAnimation(duration);
		initOutAnimation(duration);
		duration = a.getInteger(R.styleable.FlipView_startLayoutAnimationDuration, SCALE_DURATION);
		setLayoutAnimationController(a.getResourceId(R.styleable.FlipView_startLayoutAnimation, R.anim.layout_animation));
		setLayoutAnimationDuration(duration);

		//FrontView
		int frontLayout = a.getResourceId(R.styleable.FlipView_frontLayout, R.layout.flipview_front);
		Drawable frontBackground = a.getDrawable(R.styleable.FlipView_frontBackground);
		int frontBackgroundColor = a.getColor(R.styleable.FlipView_frontBackgroundColor, Color.GRAY);
		int frontImage = a.getResourceId(R.styleable.FlipView_frontImage, 0);
		setFrontLayout(frontLayout);

		//RearView
		int rearLayout = a.getResourceId(R.styleable.FlipView_rearLayout, R.layout.flipview_rear);
		Drawable rearBackground = a.getDrawable(R.styleable.FlipView_rearBackground);
		int rearBackgroundColor = a.getColor(R.styleable.FlipView_rearBackgroundColor, Color.DKGRAY);
		int rearImage = a.getResourceId(R.styleable.FlipView_rearImage, 0);
		setRearLayout(rearLayout);

		a.recycle();

	}

	/* LISTENER ****************************************************/

	public void setOnFlippingListener(OnFlippingListener listener) {
		this.mFlippingListener = listener;
	}

	/* ANIMATIONS ***********************************************************/

	private void initInAnimation(long duration) {
		if (getInAnimation() == null)
			setInAnimation(getContext(), R.anim.grow_from_middle);
		getInAnimation().setDuration(duration);
	}

	private void initOutAnimation(long duration) {
		if (getOutAnimation() == null)
			setOutAnimation(getContext(), R.anim.shrink_to_middle);
		getOutAnimation().setDuration(duration);
	}

	/**
	 * Set the duration of the state change animation.
	 *
	 * @param duration The flip animation duration in milliseconds.
	 */
	public void setFlipAnimationDuration(long duration) {
		initInAnimation(duration);
		initOutAnimation(duration);
	}

	/**
	 * Get the duration of the state change animation.
	 *
	 * @return The animation duration in milliseconds.
	 */
	public long getFlipAnimationDuration() {
		return getInAnimation().getDuration();
	}

	@Override
	public LayoutAnimationController getLayoutAnimation() {
		return super.getLayoutAnimation();
	}

	public void setLayoutAnimationController(int layoutAnimationControllerResId) {
		LayoutAnimationController layoutAnimationController =
				AnimationUtils.loadLayoutAnimation(getContext(),
				layoutAnimationControllerResId > 0 ? layoutAnimationControllerResId : R.anim.layout_animation);
		super.setLayoutAnimation(layoutAnimationController);
	}

	public void setLayoutAnimationDuration(long duration) {
		getLayoutAnimation().getAnimation().setDuration(duration);
	}

	public Animation getCheckAnimation() {
		return checkAnimation;
	}

	public void setCheckAnimation(int checkedAnimationResId) {
		setCheckAnimation(AnimationUtils.loadAnimation(getContext(),
				checkedAnimationResId > 0 ? checkedAnimationResId : R.anim.scale));
	}

	public void setCheckAnimation(Animation acceptAnimation) {
		this.checkAnimation = acceptAnimation;
	}

	/* LAYOUT AND VIEWS **************************************************/

	/**
	 * Get the View being displayed on the <i>front</i>. The front view is
	 * displayed when the component is in state "not checked".
	 *
	 * @return The <i>front</i> view.
	 */
	public View getFrontLayout() {
		return getChildAt(FRONT_VIEW_INDEX);
	}

	/**
	 * Set the front view to be displayed when this component is in state <i>not checked</i>.
	 * If an invalid resource or {@link #DEFAULT_RESOURCE} is
	 * passed, then the default view will be applied.
	 *
	 * @param layoutResId The layout resource identifier.
	 */
	public void setFrontLayout(int layoutResId) {
		setFrontLayout(LayoutInflater.from(getContext()).inflate(layoutResId, this, false));
	}

	/**
	 * Set the front view to be displayed when this component is in state <i>not checked</i>.
	 * The provided <i>view</i> must not be {@code null}, or
	 * an IllegalArgumentException will be thrown.
	 *
	 * @param view The view. Must not be {@code null}.
	 */
	public void setFrontLayout(@NonNull View view) {
		setView(view, FRONT_VIEW_INDEX);
	}

	/**
	 * Get the View being displayed on the <i>rear</i>. The rear view is
	 * displayed when the component is in state "checked".
	 *
	 * @return The <i>rear</i> view.
	 */
	public View getRearLayout() {
		return getChildAt(REAR_VIEW_INDEX);
	}

	/**
	 * Set the rear view to be displayed when this component is in state <i>checked</i>.
	 * If an invalid resource or {@link #DEFAULT_RESOURCE} is
	 * passed, then the default view will be applied.
	 *
	 * @param layoutResId The layout resource identifier.
	 */
	public void setRearLayout(int layoutResId) {
		setRearLayout(LayoutInflater.from(getContext()).inflate(layoutResId, this, false));
	}

	/**
	 * Set the rear view to be displayed when this component is in state <i>checked</i>.
	 * The provided <i>view</i> must not be {@code null}, or
	 * an IllegalArgumentException will be thrown.
	 *
	 * @param view The view. Must not be {@code null}.
	 */
	public void setRearLayout(@NonNull View view) {
		setView(view, REAR_VIEW_INDEX);
	}

	private void setView(View view, int index) {
		if (view == null)
			throw new IllegalArgumentException("The provided view must not be null");

		super.removeViewAt(index);
		super.addView(view, index, super.generateDefaultLayoutParams());
	}

}