/*
 * Copyright (C) 2016 Davide Steduto
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.AnimRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * FlipView is a ViewGroup (FrameLayout) that is designed to display 2 views/layouts by flipping
 * the front one in favor of the back one, and vice versa. Optionally more views can be
 * displayed in series one after another since it extends {@link android.widget.ViewAnimator}.
 * <p>Usage is very simple. You just need to add this View to any layout (like you would
 * do with any other View) and you customize the behaviours by assigning values to the
 * optional properties in the layout or programmatically.</p>
 * Please, refer to those attributes documentation for more details.
 * <ul>
 * <li>The Views to flip can be many <b>ViewGroups</b> containing an ImageView/TextView or simply
 * more <b>Views</b> (preferable ImageView) or even a combination of these types:<br><br>
 * 1. In case of <b>ViewGroups</b> with an ImageView each, (if present) background drawable
 * and color are assigned to those ViewGroups and the image resources to those ImageViews.
 * In this case the entire ViewGroups (containing the ImageViews) will flip.<br>
 * Choosing this option, when 2 ViewGroups are configured, a second animation is executed
 * on the rear ImageView after the first flip is consumed.<br>
 * <b>Note:</b> the library contains already the checked Drawable for the rear image!<br><br>
 * 2. In case of <b>Views</b>, (if present) background drawable and color are assigned
 * to the main ViewGroup (the FlipView) and only the simple views will be shown in series.<br>
 * Choosing this option, no further animation will be performed on the rear Views.<br><br></li>
 * <li>FlipView supports a {@link PictureDrawable} for SVG loading and assignment <i>front View
 * Only</i>. LayerType {@link View#LAYER_TYPE_SOFTWARE} is automatically set for you.<br><br></li>
 * <li>FlipView can born already flipped and enabled/disabled programmatically.<br><br></li>
 * <li>If the custom layout is provided with a TextVIew instead of ImageView as first child,
 * custom text can be displayed. Having such TextView, you can assign any text and style for the
 * front View.<br><br></li>
 * <li>Another functionality is to assign, to the entire FlipView, an <b>initial animation</b>
 * (by default it's a Scale animation and not enabled). Different combinations of effects are
 * possible:<br>
 * For instance, having multiples FlipViews on the screen, this animation can be prepared for
 * simultaneous entry effect (all FlipViews will perform the animation at the same time) or
 * for a delayed entry effect (all FlipViews will perform the animation with step delay).</li>
 * </ul>
 * Finally, when the View is clicked, it will switch its state. The event is
 * propagated with the listener {@link OnFlippingListener#onFlipped(FlipView, boolean)}.
 * You can subscribe to that listener using {@link #setOnFlippingListener(OnFlippingListener)}
 * method.
 *
 * @author Davide Steduto
 * @since 01/11/2015 Created
 * <br>06/04/2016 Enable/disable flipping programmatically
 * <br>29/11/2016 Skip flipping if the target view is already visible
 */
@SuppressWarnings("unused")
public class FlipView extends ViewFlipper implements SVGPictureDrawable, View.OnClickListener {

	private static final String TAG = FlipView.class.getSimpleName();
	private static boolean DEBUG = false;

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
	 * Child index to access the <b>front</b> view
	 */
	public static final int FRONT_VIEW_INDEX = 0;

	/**
	 * Child index to access the <b>rear</b> view
	 */
	public static final int REAR_VIEW_INDEX = 1;

	/**
	 * Reference to the TextView of the FrontLayout if exists
	 */
	private TextView frontText;

	/**
	 * Reference to the ImageView of the FrontLayout if exists
	 */
	private ImageView frontImage;
	private int frontImagePadding;

	/**
	 * Reference to the ImageView of the RearLayout if exists
	 */
	private ImageView rearImage;
	private int rearImagePadding;

	/**
	 * Drawable used for SVG images for the front View Only
	 */
	private PictureDrawable pictureDrawable;

	/**
	 * Animations attributes
	 */
	private static boolean enableInitialAnimation = true;
	private Animation initialLayoutAnimation;
	private Animation rearImageAnimation;
	public static final int
			DEFAULT_INITIAL_DELAY = 500,
			SCALE_STEP_DELAY = 35,
			STOP_LAYOUT_ANIMATION_DELAY = 1500,
			FLIP_INITIAL_DELAY = 250,
			FLIP_DURATION = 100,
			INITIAL_ANIMATION_DURATION = 250,
			REAR_IMAGE_ANIMATION_DURATION = 150,
			DEFAULT_INTERVAL = 3000;
	private long initialLayoutAnimationDuration,
			mainAnimationDuration,
			rearImageAnimationDuration,
			rearImageAnimationDelay,
			anticipateInAnimationTime;
	private static long initialDelay = DEFAULT_INITIAL_DELAY;
	private int mFlipInterval = DEFAULT_INTERVAL;

	//****************
	// CONSTRUCTORS **
	//****************

	public FlipView(Context context) {
		super(context);
		init(null);
	}

	public FlipView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	/**
	 * Already part of the extended views:
	 * <ul>
	 * <li>{@code inAnimation}: from {@code ViewAnimator}, identifier for the animation to use when a view is shown.</li>
	 * <li>{@code outAnimation}: from {@code ViewAnimator}, identifier for the animation to use when a view is hidden.</li>
	 * <li>{@code animateFirstView}: from {@code ViewAnimator}, defines whether to animate the current View when the ViewAnimation is first displayed.</li>
	 * <li>{@code flipInterval}: from {@code ViewFlipper}, time before next animation.</li>
	 * <li>{@code autoStart}: from {@code ViewFlipper}, when true, automatically start animating.</li>
	 * </ul>
	 *
	 * @param attrs The view's attributes.
	 */
	private void init(AttributeSet attrs) {
		// Read and apply provided attributes
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.FlipView, 0, 0);

		// Flags
		boolean checked = a.getBoolean(R.styleable.FlipView_checked, false);
		boolean startupAnimation = a.getBoolean(R.styleable.FlipView_enableInitialAnimation, false);
		boolean animateDesignChildViewsOnly = a.getBoolean(R.styleable.FlipView_animateDesignLayoutOnly, false);

		if (!animateDesignChildViewsOnly) {
			// FrontView
			int frontLayout = a.getResourceId(R.styleable.FlipView_frontLayout, R.layout.flipview_front);
			Drawable frontBackground = a.getDrawable(R.styleable.FlipView_frontBackground);
			int frontBackgroundColor = a.getColor(R.styleable.FlipView_frontBackgroundColor, Color.GRAY);
			int frontImage = a.getResourceId(R.styleable.FlipView_frontImage, 0);
			frontImagePadding = (int) a.getDimension(R.styleable.FlipView_frontImagePadding, 0);
			setFrontLayout(frontLayout);
			if (frontBackground == null)
				setChildBackgroundColor(FRONT_VIEW_INDEX, frontBackgroundColor);
			else setChildBackgroundDrawable(FRONT_VIEW_INDEX, frontBackground);
			setFrontImage(frontImage);

			// RearView
			int rearLayout = a.getResourceId(R.styleable.FlipView_rearLayout, R.layout.flipview_rear);
			Drawable rearBackground = a.getDrawable(R.styleable.FlipView_rearBackground);
			int rearBackgroundColor = a.getColor(R.styleable.FlipView_rearBackgroundColor, Color.GRAY);
			int rearImage = a.getResourceId(R.styleable.FlipView_rearImage, R.drawable.ic_action_done);
			rearImagePadding = (int) a.getDimension(R.styleable.FlipView_rearImagePadding, 0);
			addRearLayout(rearLayout);
			if (rearBackground == null)
				setChildBackgroundColor(REAR_VIEW_INDEX, rearBackgroundColor);
			else setChildBackgroundDrawable(REAR_VIEW_INDEX, rearBackground);
			setRearImage(rearImage);
		}

		// Display the first rear view at start if requested
		if (checked) flipSilently(true);

		// Init main(Flip) animations
		mainAnimationDuration = a.getInteger(R.styleable.FlipView_animationDuration, FLIP_DURATION);
		rearImageAnimationDuration = a.getInteger(R.styleable.FlipView_rearImageAnimationDuration, REAR_IMAGE_ANIMATION_DURATION);
		rearImageAnimationDelay = a.getInteger(R.styleable.FlipView_rearImageAnimationDelay, (int) mainAnimationDuration);
		anticipateInAnimationTime = a.getInteger(R.styleable.FlipView_anticipateInAnimationTime, 0);
		if (!isInEditMode()) {
			//This also initialize the in/out animations
			setMainAnimationDuration(mainAnimationDuration);
			if (a.getBoolean(R.styleable.FlipView_animateRearImage, true))
				setRearImageAnimation(a.getResourceId(R.styleable.FlipView_rearImageAnimation, 0));
		}

		// Save initial animation settings
		initialLayoutAnimationDuration = a.getInteger(R.styleable.FlipView_initialLayoutAnimationDuration, INITIAL_ANIMATION_DURATION);
		setInitialLayoutAnimation(a.getResourceId(R.styleable.FlipView_initialLayoutAnimation, 0));
		// Show initial cascade step animation when view is first rendered
		if (startupAnimation && enableInitialAnimation && !isInEditMode()) {
			animateLayout(getInitialLayoutAnimation());
		}

		a.recycle();

		// Apply default OnClickListener if clickable
		if (isClickable()) setOnClickListener(this);
	}

	//*************
	// LISTENERS **
	//*************

	public void setOnFlippingListener(OnFlippingListener listener) {
		this.mFlippingListener = listener;
	}

	@Override
	public void onClick(View v) {
		this.showNext();
	}

	/**
	 * {@inheritDoc}
	 * <p>By setting {@code true} will set the click listener to this view, by setting
	 * {@code false} will remove the click listener completely.</p>
	 *
	 * @param clickable true to make the view clickable, false otherwise
	 */
	@Override
	public void setClickable(boolean clickable) {
		super.setClickable(clickable);
		if (clickable) setOnClickListener(this);
		else setOnClickListener(null);
	}

	/**
	 * {@inheritDoc}
	 * <p><b>Note:</b> If the view was set as auto-start <u>and</u> {@link #setFlipInterval(int)}
	 * has not been called, re-enabling the view, it will have the default initial 3000ms delay.</p>
	 *
	 * @param enabled true if this view is enabled and flip active, false otherwise.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (isAutoStart()) {
			if (!enabled) stopFlipping();
			else postDelayed(new Runnable() {
				@Override
				public void run() {
					if (isEnabled()) startFlipping();
				}
			}, mFlipInterval);
		}
	}

	//******************
	// STATIC METHODS **
	//******************

	/**
	 * Calls this method once, to enable or disable DEBUG logs.
	 * <p>DEBUG logs are disabled by default.</p>
	 *
	 * @param enable true to show DEBUG logs, false to hide them.
	 */
	public static void enableLogs(boolean enable) {
		DEBUG = enable;
	}

	/**
	 * API 22
	 *
	 * @return true if the current Android version is Lollipop
	 * @see Build.VERSION_CODES#LOLLIPOP_MR1
	 */
	public static boolean hasLollipop() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
	}

	/**
	 * Creates a Scale animation programmatically.
	 * <p>Usage of this method helps rendering the page much faster (it doesn't load the
	 * animation file from disk).</p>
	 *
	 * @return {@link ScaleAnimation} relative to self with pivotXY at 50%
	 */
	public static Animation createScaleAnimation() {
		return new ScaleAnimation(0, 1.0f, 0, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
	}

	/**
	 * Convenience method for {@link #resetLayoutAnimationDelay(boolean, long)}.
	 * <p>This method enables and resets initial layout animation delay to the default
	 * {@link #DEFAULT_INITIAL_DELAY}.</p>
	 * <b>Note:</b> Call this method at the beginning of {@code onCreate/onActivityCreated}.
	 *
	 * @see #resetLayoutAnimationDelay(boolean, long)
	 * @see #stopLayoutAnimation()
	 */
	public static void resetLayoutAnimationDelay() {
		resetLayoutAnimationDelay(true, DEFAULT_INITIAL_DELAY);
	}

	/**
	 * Reset initial layout animation delay to a custom delay. This avoid to continuously
	 * increase the next step delay of the next FlipView on the screen!
	 * <p><b>Note:</b> Call this method at the beginning of {@code onCreate/onActivityCreated}.</p>
	 *
	 * @param enable    optionally future start animation can be disabled
	 * @param nextDelay the new custom initial delay
	 * @see #resetLayoutAnimationDelay()
	 * @see #stopLayoutAnimation()
	 */
	public static void resetLayoutAnimationDelay(boolean enable, @IntRange(from = 0) long nextDelay) {
		enableInitialAnimation = enable;
		initialDelay = nextDelay;
	}

	/**
	 * Stops and Resets layout animation after {@link #STOP_LAYOUT_ANIMATION_DELAY}.
	 * <p>This gives the time to perform all entry animations but to stop further animations when
	 * screen is fully rendered: ALL Views will not perform initial animation anymore
	 * until a new reset.</p>
	 * <b>Note:</b>
	 * <br>- The delay time has been identified at 1.5 seconds (1500ms).
	 * <br>- Call this method at the end of {@code onCreate/onActivityCreated}.
	 *
	 * @see #resetLayoutAnimationDelay()
	 * @see #resetLayoutAnimationDelay(boolean, long)
	 */
	public static void stopLayoutAnimation() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				resetLayoutAnimationDelay(false, DEFAULT_INITIAL_DELAY);
			}
		}, STOP_LAYOUT_ANIMATION_DELAY);
	}

	//**************
	// ANIMATIONS **
	//**************

	@Override
	public void setFlipInterval(@IntRange(from = 0) int milliseconds) {
		super.setFlipInterval(milliseconds);
		mFlipInterval = milliseconds;
	}

	/*
	 * Override to always display content in design mode.
	 */
	@Override
	public void setInAnimation(Context context, @AnimRes int animationResId) {
		if (!isInEditMode()) super.setInAnimation(context, animationResId);
	}

	/*
	 * Override to always display content in design mode.
	 */
	@Override
	public void setOutAnimation(Context context, @AnimRes int animationResId) {
		if (!isInEditMode()) super.setOutAnimation(context, animationResId);
	}

	private void initInAnimation(@IntRange(from = 0) long duration) {
		if (getInAnimation() == null)
			this.setInAnimation(getContext(), R.anim.grow_from_middle_x_axis);
		super.getInAnimation().setDuration(duration);
		super.getInAnimation().setStartOffset(anticipateInAnimationTime > duration ?
				duration : duration - anticipateInAnimationTime);
	}

	private void initOutAnimation(@IntRange(from = 0) long duration) {
		if (getOutAnimation() == null)
			this.setOutAnimation(getContext(), R.anim.shrink_to_middle_x_axis);
		super.getOutAnimation().setDuration(duration);
	}

	public void animateLayout(Animation layoutAnimation) {
		startAnimation(layoutAnimation);
	}

	/**
	 * @return the Animation of this FlipView layout
	 */
	public Animation getInitialLayoutAnimation() {
		return this.initialLayoutAnimation;
	}

	public void setInitialLayoutAnimation(@AnimRes int animationResId) {
		try {
			setInitialLayoutAnimation(animationResId > 0 ?
					AnimationUtils.loadAnimation(getContext(), animationResId) :
					createScaleAnimation()); //Usage of the method it's faster (not read from disk)
			if (DEBUG) Log.d(TAG, "Initial animation is active!");
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Initial animation with id " + animationResId
					+ " could not be found. Initial animation cannot be set!");
		}
	}

	/**
	 * Custom initial layout animation.<br>
	 * <p><b>Note:</b> Duration and startOffset will be overridden by the following settings:</p>
	 * {@code duration = initialLayoutAnimationDuration;}<br>
	 * {@code startOffset = initialDelay += SCALE_STEP_DELAY}.
	 *
	 * @param initialLayoutAnimation the new initial animation
	 */
	public final void setInitialLayoutAnimation(Animation initialLayoutAnimation) {
		this.initialLayoutAnimation = initialLayoutAnimation;
		initialLayoutAnimation.setDuration(initialLayoutAnimationDuration);
		initialLayoutAnimation.setStartOffset(initialDelay += SCALE_STEP_DELAY);
		if (initialLayoutAnimation.getInterpolator() == null)
			initialLayoutAnimation.setInterpolator(new DecelerateInterpolator());
	}

	/**
	 * @return the animation of the rear ImageView.
	 */
	public Animation getRearImageAnimation() {
		return rearImageAnimation;
	}

	public void setRearImageAnimation(@AnimRes int animationResId) {
		try {
			setRearImageAnimation(AnimationUtils.loadAnimation(getContext(),
					animationResId > 0 ? animationResId : R.anim.scale_up));
			if (DEBUG) Log.d(TAG, "Rear animation is active!");
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Rear animation with id " + animationResId + " could not be found. Rear animation cannot be set!");
		}
	}

	public void setRearImageAnimation(Animation rearAnimation) {
		this.rearImageAnimation = rearAnimation;
		if (rearImageAnimationDuration > 0)
			this.rearImageAnimation.setDuration(rearImageAnimationDuration);
	}

	/**
	 * Gets the duration of the flip animation.
	 *
	 * @return the duration in milliseconds
	 */
	public long getMainAnimationDuration() {
		return getInAnimation().getDuration();
	}

	/**
	 * Sets the duration of the main animation.
	 *
	 * @param duration The duration in milliseconds
	 */
	public void setMainAnimationDuration(@IntRange(from = 0) long duration) {
		if (DEBUG) Log.d(TAG, "Setting mainAnimationDuration=" + duration);
		mainAnimationDuration = duration;
		initInAnimation(duration);
		initOutAnimation(duration);
	}

	/**
	 * Gets the duration of the initial animation when the View is first displayed.
	 *
	 * @return the duration in milliseconds
	 */
	public long getInitialLayoutAnimationDuration() {
		return initialLayoutAnimationDuration;
	}

	/**
	 * Sets the duration of the initial animation when the View is first displayed.
	 *
	 * @param duration The duration in milliseconds
	 */
	public void setInitialLayoutAnimationDuration(@IntRange(from = 0) long duration) {
		if (DEBUG) Log.d(TAG, "Setting initialLayoutAnimationDuration=" + duration);
		this.initialLayoutAnimationDuration = duration;
		if (initialLayoutAnimation != null)
			initialLayoutAnimation.setDuration(duration);
	}

	/**
	 * Gets the duration of the animation of the rear ImageView.
	 *
	 * @return the duration in milliseconds
	 */
	public long getRearImageAnimationDuration() {
		return rearImageAnimationDuration;
	}

	/**
	 * Sets the duration of the animation of the rear ImageView.
	 *
	 * @param duration The duration in milliseconds
	 */
	public void setRearImageAnimationDuration(@IntRange(from = 0) long duration) {
		if (DEBUG) Log.d(TAG, "Setting rearImageAnimationDuration=" + duration);
		this.rearImageAnimationDuration = duration;
		if (rearImageAnimation != null)
			rearImageAnimation.setDuration(duration);
	}

	/**
	 * Get the anticipation time for InAnimation.
	 *
	 * @return the anticipation time in milliseconds
	 */
	public long getAnticipateInAnimationTime() {
		return anticipateInAnimationTime;
	}

	/**
	 * Sets the anticipation time for InAnimation: don't wait OutAnimation completion.
	 * Depends by the effect that user desires, he can anticipate the entrance of rear layout.
	 * <p>
	 * - New delay is: the current {@link #mainAnimationDuration} anticipation time.<br>
	 * - Max value is: the current mainAnimationDuration.
	 * </p>
	 * Default value is 0.
	 *
	 * @param time the anticipation time in milliseconds
	 */
	public void setAnticipateInAnimationTime(@IntRange(from = 0) long time) {
		if (DEBUG) Log.d(TAG, "Setting anticipateInAnimationTime=" + time);
		this.anticipateInAnimationTime = time;
	}

	/**
	 * Gets the start animation delay of the rear ImageView.
	 *
	 * @return the delay in milliseconds
	 */
	public long getRearImageAnimationDelay() {
		return rearImageAnimationDelay;
	}

	/**
	 * Sets the start animation delay of the rear ImageView.
	 *
	 * @param delay the delay in milliseconds
	 */
	public void setRearImageAnimationDelay(@IntRange(from = 0) long delay) {
		if (DEBUG) Log.d(TAG, "Setting rearImageAnimationDelay=" + delay);
		this.rearImageAnimationDelay = delay;
	}

	//************************
	// PERFORMING ANIMATION **
	//************************

	/**
	 * Flips the current View and display to the next View now!
	 * <p>Command ignored if the view is disabled.</p>
	 *
	 * @see #setEnabled(boolean)
	 */
	@Override
	public final void showNext() {
		this.showNext(0L);
	}

	/**
	 * Flips the current View and display to the next View with a delay.
	 * <p>Command ignored if the view is disabled.</p>
	 *
	 * @param delay any custom delay
	 * @see #setEnabled(boolean)
	 */
	public final void showNext(long delay) {
		if (DEBUG) Log.d(TAG, "showNext " + (getDisplayedChild() + 1) + " delay=" + delay);
		this.flip(getDisplayedChild() + 1, delay);
	}

	/**
	 * Flips the current View and display to the previous View now!
	 * <p>Command ignored if the view is disabled.</p>
	 *
	 * @see #setEnabled(boolean)
	 */
	@Override
	public final void showPrevious() {
		this.showPrevious(0L);
	}

	/**
	 * Flips the current View and display to the previous View with a delay.
	 * <p>Command ignored if the view is disabled.</p>
	 *
	 * @param delay any custom delay
	 * @see #setEnabled(boolean)
	 */
	public final void showPrevious(long delay) {
		if (DEBUG) Log.d(TAG, "showPrevious " + (getDisplayedChild() - 1) + " delay=" + delay);
		this.flip(getDisplayedChild() - 1, delay);
	}

	public boolean isFlipped() {
		return getDisplayedChild() > FRONT_VIEW_INDEX;
	}

	/**
	 * Convenience method for layout that has only 2 child Views!
	 * <p>Execute the flip animation with No delay.</p>
	 * Command ignored if the view is disabled.
	 *
	 * @param showRear {@code true} to show back View, {@code false} to show front View
	 * @see #setEnabled(boolean)
	 */
	public final void flip(boolean showRear) {
		flip(showRear, 0L);
	}

	/**
	 * Convenience method for layout that has only 2 child Views!
	 * <p>Execute the flip animation with a custom delay.</p>
	 * Command ignored if the view is disabled.
	 *
	 * @param showRear {@code true} to show back View, {@code false} to show front View
	 * @param delay    any custom delay
	 * @see #setEnabled(boolean)
	 */
	public final void flip(boolean showRear, long delay) {
		flip(showRear ? REAR_VIEW_INDEX : FRONT_VIEW_INDEX, delay);
	}

	/**
	 * Sets the state of this component to the given value, performing the
	 * corresponding main animation and, if it exists, the rear Image animation.
	 * <p>Command ignored if the view is disabled.</p>
	 *
	 * @param whichChild the progressive index of the child View (first View has index=0).
	 * @param delay      any custom delay
	 * @see #setEnabled(boolean)
	 */
	public final void flip(final int whichChild, @IntRange(from = 0) long delay) {
		if (!isEnabled()) {
			if (DEBUG) Log.w(TAG, "Can't flip while view is disabled");
			return;
		}
		final int childIndex = checkIndex(whichChild);
		if (DEBUG) {
			Log.d(TAG, "Flip! whichChild=" + childIndex + ", previousChild=" + getDisplayedChild() + ", delay=" + delay);
		}
		// Issue #7 - Don't flip if the target child is the one currently displayed
		if (childIndex == getDisplayedChild()) {
			if (DEBUG) Log.w(TAG, "Already flipped to same whichChild=" + whichChild);
			return;
		}
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				setDisplayedChild(childIndex); //Start main animation
				animateRearImageIfNeeded();
				mFlippingListener.onFlipped(FlipView.this, isFlipped());
			}
		}, delay);
	}

	private void animateRearImageIfNeeded() {
		if (isFlipped() && rearImage != null && rearImageAnimation != null) {
			rearImage.setAlpha(0f); //Alpha 0 and Handler are needed to avoid the glitch of the rear image
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					rearImage.setAlpha(1f);
					rearImage.startAnimation(rearImageAnimation);
				}
			}, rearImageAnimationDelay); //Wait InAnimation completion before to start rearImageAnimation?
		}
	}

	/**
	 * Convenience method for layout that has only 2 child Views, no animation will be performed.
	 * <p>Command is always performed even if the view is disabled.</p>
	 *
	 * @param showRear {@code true} to show back View, {@code false} to show front View
	 * @see #flipSilently(int)
	 */
	public final void flipSilently(boolean showRear) {
		flipSilently(showRear ? REAR_VIEW_INDEX : FRONT_VIEW_INDEX);
	}

	/**
	 * Shows a specific View immediately, no animation will be performed.
	 * <p>Command is always performed even if the view is disabled.</p>
	 *
	 * @param whichChild the index of the child view to display (first View has {@code index=0}).
	 */
	public final void flipSilently(int whichChild) {
		if (DEBUG) Log.d(TAG, "flipSilently whichChild=" + whichChild);
		whichChild = checkIndex(whichChild);
		Animation inAnimation = super.getInAnimation();
		Animation outAnimation = super.getOutAnimation();
		super.setInAnimation(null);
		super.setOutAnimation(null);
		super.setDisplayedChild(whichChild);
		super.setInAnimation(inAnimation);
		super.setOutAnimation(outAnimation);
	}

	/**
	 * Checks that, the index is never negative or bigger than the actual number of child Views.
	 * <p>
	 * - if negative: first child View is displayed;<br>
	 * - if bigger than actual Views: last child View is displayed.
	 * </p>
	 * The logic is different than {@link #setDisplayedChild(int)}, where:<br>
	 * - if negative: last child View is displayed;<br>
	 * - if bigger than actual Views: first child View is displayed.
	 *
	 * @param whichChild the index of the child View to display
	 * @return the new index of the child View to display
	 */
	private int checkIndex(int whichChild) {
		if (whichChild < FRONT_VIEW_INDEX) return FRONT_VIEW_INDEX;
		if (whichChild > getChildCount()) return getChildCount();
		return whichChild;
	}

	//********************
	// LAYOUT AND VIEWS **
	//********************

	/**
	 * Gets the View being displayed on the <i>front</i>. The front view is
	 * displayed when the component is in state "not checked".
	 *
	 * @return the front view
	 * @see #setFrontLayout(int)
	 * @see #setFrontLayout(View)
	 */
	public View getFrontLayout() {
		return getChildAt(FRONT_VIEW_INDEX);
	}

	/**
	 * Sets the front view to be displayed when this component is in state <i>not checked</i>.
	 * <p>The front view can be a ViewGroup.</p>
	 *
	 * @param layoutResId the layout resource identifier.
	 * @see #getFrontLayout()
	 * @see #setFrontLayout(View)
	 */
	public void setFrontLayout(@LayoutRes int layoutResId) {
		if (layoutResId == R.layout.flipview_front) {
			if (DEBUG) Log.d(TAG, "Adding inner FrontLayout");
		} else if (DEBUG) Log.d(TAG, "Setting user FrontLayout " + layoutResId);
		setFrontLayout(LayoutInflater.from(getContext()).inflate(layoutResId, this, false));
	}

	/**
	 * Sets the front view to be displayed when this component is in state <i>not checked</i>.
	 * <p>The provided view must be not {@code null}, or {@code IllegalArgumentException} will
	 * be raised.</p>
	 * The front view can be a ViewGroup.
	 *
	 * @param view the front view. Must not be {@code null}
	 * @see #getFrontLayout()
	 * @see #setFrontLayout(int)
	 */
	public void setFrontLayout(@NonNull View view) {
		ViewGroup viewGroup = this;
		// If the View is another ViewGroup use it as front View to flip
		if (view instanceof ViewGroup) {
			if (DEBUG) Log.d(TAG, "FrontLayout is a ViewGroup");
			viewGroup = (ViewGroup) view;
		}
		// If any ImageView at first position is provided, reference to this front ImageView is saved.
		if (viewGroup.getChildAt(FRONT_VIEW_INDEX) instanceof ImageView) {
			if (DEBUG) Log.d(TAG, "Found ImageView in FrontLayout");
			frontImage = (ImageView) viewGroup.getChildAt(FRONT_VIEW_INDEX);
		} else if (viewGroup.getChildAt(FRONT_VIEW_INDEX) instanceof TextView) {
			if (DEBUG) Log.d(TAG, "Found TextView in FrontLayout");
			frontText = (TextView) viewGroup.getChildAt(FRONT_VIEW_INDEX);
		}
		this.addView(view, FRONT_VIEW_INDEX);
	}

	/**
	 * Gets the View being displayed on the <i>rear</i>. The rear view is
	 * displayed when the component is in state "checked".
	 *
	 * @return the rear view
	 * @see #addRearLayout(int)
	 * @see #addRearLayout(View)
	 */
	public View getRearLayout() {
		return getChildAt(REAR_VIEW_INDEX);
	}

	/**
	 * Adds the rear view to be displayed when this component is in state <i>checked</i>.
	 * <p>The rear view can be a ViewGroup.</p>
	 *
	 * @param layoutResId the layout resource identifier.
	 * @throws IllegalArgumentException if the provided view is null
	 * @see #getRearLayout()
	 * @see #addRearLayout(View)
	 */
	public void addRearLayout(@LayoutRes int layoutResId) {
		if (layoutResId == R.layout.flipview_rear) {
			if (DEBUG) Log.d(TAG, "Adding inner RearLayout");
		} else if (DEBUG) Log.d(TAG, "Adding user RearLayout " + layoutResId);
		addRearLayout(LayoutInflater.from(getContext()).inflate(layoutResId, this, false));
	}

	/**
	 * Adds the rear view to be displayed when this component is in state <i>checked</i>.
	 * <p>The provided view must be not {@code null}, or {@code IllegalArgumentException} will
	 * be raised.</p>
	 * The rear view can be a ViewGroup.
	 *
	 * @param view the rear view. Must not be {@code null}
	 * @throws IllegalArgumentException if the provided view is null
	 * @see #getRearLayout()
	 * @see #addRearLayout(int)
	 */
	public void addRearLayout(@NonNull View view) {
		ViewGroup viewGroup = this;
		// Assign current count as our Index for rear View in case multiples views are added.
		int whichChild = getChildCount(); //By default suppose it's already our rear View
		if (DEBUG) Log.d(TAG, "RearLayout index=" + whichChild);
		// If the View is another ViewGroup use it as new our rear View to flip
		if (view instanceof ViewGroup) {
			if (DEBUG) Log.d(TAG, "RearLayout is a ViewGroup");
			viewGroup = (ViewGroup) view;
			whichChild = 0; //Override the index: use the first position to locate the ImageView in this ViewGroup
		}
		// If any ImageView is provided, reference to this rear ImageView is saved
		if (viewGroup.getChildAt(whichChild) instanceof ImageView) {
			if (DEBUG) Log.d(TAG, "Found ImageView in the RearLayout");
			rearImage = (ImageView) viewGroup.getChildAt(whichChild);
		} else if (whichChild > 2) {
			rearImage = null; //Rollback in case multiple views are added (user must provide already the image in each layout added)
		}
		// Watch out! User can add first the rear view and after the front view that must be
		// always at position 0. While all rear views start from index = 1.
		this.addView(view, getChildCount() == 0 ? REAR_VIEW_INDEX : getChildCount());
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public void addView(@NonNull View view, int whichChild) {
		if (view == null) {
			throw new IllegalArgumentException("The provided view must not be null");
		}
		if (DEBUG) Log.d(TAG, "Setting child view at index " + whichChild);
		if (super.getChildAt(whichChild) != null) {
			super.removeViewAt(whichChild);
		}
		super.addView(view, whichChild, super.generateDefaultLayoutParams());
	}

	/**
	 * Gets the front TextView if present in the layout.
	 *
	 * @return the front TextView, null if not added in the layout
	 * @see #setFrontText(CharSequence)
	 */
	public TextView getFrontTextView() {
		return this.frontText;
	}

	/**
	 * Sets the front text of the TextView that must be present in the layout.
	 * <p>The text will be displayed for the <i>unchecked</i> status.</p>
	 *
	 * @param text the new text for the TextView
	 * @see #getFrontTextView()
	 */
	public void setFrontText(@Nullable CharSequence text) {
		if (this.frontText == null) {
			Log.e(TAG, "TextView not found in the first child of the FrontLayout. Text cannot be set!");
			return;
		}
		frontText.setText(text);
	}

	/**
	 * Gets the current front ImageView for the <i>unchecked</i> status.
	 *
	 * @return the current front ImageView
	 * @see #setFrontImage(int)
	 * @see #setFrontImageBitmap(Bitmap)
	 */
	public ImageView getFrontImageView() {
		return this.frontImage;
	}

	/**
	 * Sets the front image for the <i>unchecked</i> status.
	 *
	 * @param imageResId must be a valid image resourceId
	 * @see #getFrontImageView()
	 * @see #setFrontImageBitmap(Bitmap)
	 */
	public void setFrontImage(int imageResId) {
		if (this.frontImage == null) {
			// Avoid the warning message if image is correctly null because of a TextView
			if (this.frontText == null) {
				Log.e(TAG, "ImageView not found in the first child of the FrontLayout. Image cannot be set!");
			}
			return;
		}
		if (imageResId == 0) {
			Log.e(TAG, "Invalid imageResId=0");
			return;
		}
		try {
			this.frontImage.setPadding(frontImagePadding, frontImagePadding,
					frontImagePadding, frontImagePadding);
			this.frontImage.setImageResource(imageResId);
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "No front resource image id " + imageResId + " found. No Image can be set!");
		}
	}

	/**
	 * Sets a bitmap for the front image for the <i>unchecked</i> status.
	 *
	 * @param bitmap the bitmap
	 * @see #getFrontImageView()
	 * @see #setFrontImage(int)
	 */
	public void setFrontImageBitmap(Bitmap bitmap) {
		if (this.frontImage == null) {
			Log.e(TAG, "ImageView not found in the first child of the FrontLayout. Bitmap cannot be set!");
			return;
		}
		frontImage.setImageBitmap(bitmap);
	}

	/**
	 * Gets the current rear ImageView for the <i>checked</i> status.
	 *
	 * @return the current rear ImageView
	 * @see #setRearImage(int)
	 * @see #setRearImageBitmap(Bitmap)
	 */
	public ImageView getRearImageView() {
		return this.rearImage;
	}

	/**
	 * Sets the rear image for the <i>checked</i> status.
	 *
	 * @param imageResId must be a valid image resourceId
	 * @see #getRearImageView()
	 * @see #setRearImageBitmap(Bitmap)
	 */
	public void setRearImage(int imageResId) {
		if (this.rearImage == null) {
			Log.e(TAG, "ImageView not found in the child of the RearLayout. Image cannot be set!");
			return;
		}
		if (imageResId == 0) {
			Log.e(TAG, "Invalid imageResId=0");
			return;
		}
		try {
			this.rearImage.setPadding(rearImagePadding, rearImagePadding,
					rearImagePadding, rearImagePadding);
			this.rearImage.setImageResource(imageResId);
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "No rear resource image id " + imageResId + " found. Image cannot be set!");
		}
	}

	/**
	 * Sets a bitmap for the rear image for the <i>checked</i> status.
	 *
	 * @param bitmap the bitmap
	 * @see #getRearImageView()
	 * @see #setRearImage(int)
	 */
	public void setRearImageBitmap(Bitmap bitmap) {
		if (this.rearImage == null) {
			Log.e(TAG, "ImageView not found in the child of the RearLayout. Bitmap cannot be set!");
			return;
		}
		rearImage.setImageBitmap(bitmap);
	}

	//***********************************
	// PICTURE DRAWABLE IMPLEMENTATION **
	//***********************************

	@Override
	public PictureDrawable getPictureDrawable() {
		return pictureDrawable;
	}

	/**
	 * {@link #LAYER_TYPE_SOFTWARE} is automatically set for you, only on the ImageView reference.
	 *
	 * @param drawable The SVG Drawable
	 */
	@Override
	public void setPictureDrawable(PictureDrawable drawable) {
		pictureDrawable = drawable;
		if (this.frontImage == null) {
			Log.w(TAG, "ImageView not found in the first child of the FrontLayout. Image cannot be set!");
			return;
		}
		frontImage.setLayerType(LAYER_TYPE_SOFTWARE, null);
		frontImage.setImageDrawable(pictureDrawable);
	}

	public Bitmap createBitmapFrom(PictureDrawable pictureDrawable, float size) {
		int radius = (int) Math.ceil(
				TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,
						getResources().getDisplayMetrics()));
		Bitmap bitmap = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888);
		pictureDrawable.setBounds(0, 0, radius, radius);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawPicture(pictureDrawable.getPicture(), pictureDrawable.getBounds());
		return bitmap;
	}

	//************************
	// BACKGROUND DRAWABLES **
	//************************

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@SuppressWarnings("deprecation")
	public void setChildBackgroundDrawable(int whichChild, @DrawableRes int drawableResId) {
		try {
			setChildBackgroundDrawable(whichChild,
					hasLollipop() ? getContext().getDrawable(drawableResId) :
							getContext().getResources().getDrawable(drawableResId));
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Resource with id " + drawableResId + " could not be found. Drawable cannot be set!");
		}
	}

	@SuppressWarnings("deprecation")
	public void setChildBackgroundDrawable(int whichChild, Drawable drawable) {
		if (getChildAt(whichChild) != null) {
			getChildAt(whichChild).setBackgroundDrawable(drawable);
		}
	}

	public Drawable getChildBackgroundDrawable(int whichChild) {
		return getChildAt(whichChild).getBackground();
	}

	public void setChildBackgroundColor(int whichChild, @ColorInt int color) {
		setChildBackgroundDrawable(whichChild, createOvalShapeDrawable(color));
	}

	//*******************
	// SHAPE DRAWABLES **
	//*******************

	/**
	 * Helper for OvalShape constructor.
	 *
	 * @param color the desired color
	 * @return {@code ShapeDrawable} with Oval shape
	 */
	public static ShapeDrawable createOvalShapeDrawable(@ColorInt int color) {
		return createShapeDrawable(color, new OvalShape());
	}

	/**
	 * Helper for ArcShape constructor.
	 *
	 * @param color      the desired color
	 * @param startAngle the angle (in degrees) where the arc begins
	 * @param sweepAngle the sweep angle (in degrees).
	 *                   Anything equal to or greater than 360 results in a complete circle/oval.
	 * @return {@code ShapeDrawable} with Arc shape
	 */
	public static ShapeDrawable createArcShapeDrawable(
			@ColorInt int color, float startAngle, float sweepAngle) {
		return createShapeDrawable(color, new ArcShape(startAngle, sweepAngle));
	}

	/**
	 * Helper for RoundRectShape constructor.
	 * Specifies an outer (round)rect and an optional inner (round)rect.
	 *
	 * @param color      the desired color
	 * @param outerRadii An array of 8 radius values, for the outer roundrect.
	 *                   The first two floats are for the top-left corner (remaining pairs correspond clockwise).
	 *                   For no rounded corners on the outer rectangle, pass null.
	 * @param inset      A RectF that specifies the distance from the inner rect to each side of the outer rect.
	 *                   For no inner, pass null.
	 * @param innerRadii An array of 8 radius values, for the inner roundrect.
	 *                   The first two floats are for the top-left corner (remaining pairs correspond clockwise).
	 *                   For no rounded corners on the inner rectangle, pass null.
	 *                   If inset parameter is null, this parameter is ignored.
	 * @return {@code ShapeDrawable} with RoundRect shape
	 */
	public static ShapeDrawable createRoundRectShapeDrawable(
			@ColorInt int color, float[] outerRadii, RectF inset, float[] innerRadii) {
		return createShapeDrawable(color, new RoundRectShape(outerRadii, inset, innerRadii));
	}

	/**
	 * Helper method to create ShapeDrawables at runtime.
	 */
	private static ShapeDrawable createShapeDrawable(@ColorInt int color, Shape shape) {
		ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
		shapeDrawable.getPaint().setColor(color);
		shapeDrawable.getPaint().setAntiAlias(true);
		shapeDrawable.getPaint().setStyle(Paint.Style.FILL);
		return shapeDrawable;
	}

}