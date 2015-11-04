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
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
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

/**
 * FlipView is a ViewGroup (FrameLayout) that is designed to display 2 views/layouts by flipping
 * the front one in favor of the back one, and vice versa. Optionally more views can be
 * displayed in series one after another since it extends {@link android.widget.ViewAnimator}.
 * <br/><br/>
 * Usage is very simple. You just need to add this View to any layout (like you would
 * do with any other View) and you customize the behaviours by assigning values to the
 * optional properties in the layout or programmatically.<br/>
 * Please, refer to those attributes documentation for more details.
 * <p/>
 * <ul>
 * <li>The Views to flip can be many <b>ViewGroups</b> containing an ImageView/TextView or simply more
 * <b>Views</b> (preferable ImageView) or even a combination of these types.<br/><br/>
 * - In case of <b>ViewGroups</b> with an ImageView each, (if present) background drawable
 * and color are assigned to those ViewGroups and the image resources to those ImageViews.
 * In this case the entire ViewGroups (containing the ImageViews) will flip.<br/>
 * Choosing this option, when 2 ViewGroups are configured, a second animation is executed
 * on the rear ImageView after the first flip is consumed.<br/>
 * <b>Note: </b>the library contains already the checked Drawable for the rear image!<br/><br/>
 * - In case of <b>Views</b>, (if present) background drawable and color are assigned
 * to the main ViewGroup (the FlipView) and only the simple views will be shown in series.<br/>
 * Choosing this option, no further animation will be performed on the rear Views.<br/><br/></li>
 * <p/>
 * <li>Optionally, this FlipView supports a {@link PictureDrawable} for SVG loading
 * and assignment <i>front View Only</i>. Remember to change the LayerType to
 * {@link View#LAYER_TYPE_SOFTWARE}.<br/><br/></li>
 * <p/>
 * <li>Not less this FlipView can born already flipped but also flip animation can be disabled
 * but only at design time.<br/><br/></li>
 * <p/>
 * <li>If the custom layout included a TextVIew instead of ImageView as first child, custom text can
 * be displayed. Having such TextView you can assign any text and style for the front View.
 * <br/><br/></li>
 * <p/>
 * <li>Another functionality is to assign to the entire FlipView itself, an <b>initial animation</b>
 * (by default it's a Scale animation and not enabled) in order to reach different combinations
 * of effects:<br/>
 * For instance, having multiples FlipViews on the screen, this animation can be prepared for
 * simultaneous entry effect (all FlipViews will perform the animation at the same time) or
 * for a delayed entry effect (all FlipViews will perform the animation with step delay).
 * <br/><br/></li>
 * <p/>
 * </ul>
 * Finally, when the View is clicked, it will switch its state. The event is
 * propagated with the listener {@link OnFlippingListener#onFlipped(FlipView, boolean)}.
 * You can subscribe to that listener using {@link #setOnFlippingListener(OnFlippingListener)}
 * method.
 *
 * @author Davide Steduto
 * @since 01/11/2015
 */
//@SuppressWarnings("unused")
public class FlipView extends ViewFlipper implements SVGPictureDrawable, View.OnClickListener {

	private static final String TAG = FlipView.class.getSimpleName();
	private static final boolean DEBUG = true;

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
	public static final int FRONT_VIEW_INDEX = 0;

	/**
	 * Child index to access the <i>rear</i> view.
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
	private boolean checked;
	private static boolean enableInitialAnimation = true;
	private Animation initialLayoutAnimation;
	private Animation rearImageAnimation;
	private static float scaleDensity = 1f;
	public static final int
			DEFAULT_INITIAL_DELAY = 500,
			SCALE_STEP_DELAY = 35,
			STOP_LAYOUT_ANIMATION_DELAY = 1500,
			FLIP_INITIAL_DELAY = 250,
			FLIP_DURATION = 100,
			INITIAL_ANIMATION_DURATION = 250,
			REAR_IMAGE_ANIMATION_DURATION = 150;
	private long initialLayoutAnimationDuration,
			mainAnimationDuration,
			rearImageAnimationDuration,
			rearImageAnimationDelay,
			anticipateInAnimationTime;
	static long initialDelay = DEFAULT_INITIAL_DELAY;

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
	 * <li>inAnimation - ViewAnimator, Identifier for the animation to use when a view is shown.</li>
	 * <li>outAnimation - ViewAnimator, Identifier for the animation to use when a view is hidden.</li>
	 * <li>animateFirstView - ViewAnimator, Defines whether to animate the current View when the ViewAnimation is first displayed.</li>
	 * <li>flipInterval - ViewFlipper, Time before next animation.</li>
	 * <li>autoStart - ViewFlipper, When true, automatically start animating.</li>
	 * </ul>
	 *
	 * @param attrs The view's attributes.
	 */
	private void init(AttributeSet attrs) {
		//Read and apply provided attributes
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.FlipView, 0, 0);

		//Flags
		checked = a.getBoolean(R.styleable.FlipView_checked, false);
		boolean startupAnimation = a.getBoolean(R.styleable.FlipView_enableInitialAnimation, false);
		boolean animateDesignChildViewsOnly = a.getBoolean(R.styleable.FlipView_animateDesignLayoutOnly, false);

		//FrontView
		if (!animateDesignChildViewsOnly) {
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
		}

		if (!animateDesignChildViewsOnly) {
			//RearView
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

		//Display the first rear view at start if requested
		if (checked) flipSilently(true);

		//Init main(Flip) animations
		mainAnimationDuration = a.getInteger(R.styleable.FlipView_animationDuration, FLIP_DURATION);
		rearImageAnimationDuration = a.getInteger(R.styleable.FlipView_rearImageAnimationDuration, REAR_IMAGE_ANIMATION_DURATION);
		rearImageAnimationDelay = a.getInteger(R.styleable.FlipView_rearImageAnimationDelay, (int) rearImageAnimationDuration);
		anticipateInAnimationTime = a.getInteger(R.styleable.FlipView_anticipateInAnimationTime, 0);
		if (!isInEditMode()) {
			//This also initialize the in/out animations
			setMainAnimationDuration(mainAnimationDuration);
			if (a.getBoolean(R.styleable.FlipView_animateRearImage, true))
				setRearImageAnimation(a.getResourceId(R.styleable.FlipView_rearImageAnimation, 0));
		}

		//Save initial animation settings
		initialLayoutAnimationDuration = a.getInteger(R.styleable.FlipView_initialLayoutAnimationDuration, INITIAL_ANIMATION_DURATION);
		setInitialLayoutAnimation(a.getResourceId(R.styleable.FlipView_initialLayoutAnimation, 0));
		//Show initial cascade step animation when view is first rendered
		if (startupAnimation && enableInitialAnimation && !isInEditMode()) {
			animateLayout(getInitialLayoutAnimation());
		}

		a.recycle();

		//Apply default OnClickListener if clickablee
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
		showNext();
	}

	//******************
	// STATIC METHODS **
	//******************

	/**
	 * API 16
	 *
	 * @see Build.VERSION_CODES#JELLY_BEAN
	 */
	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

	/**
	 * API 22
	 *
	 * @see Build.VERSION_CODES#LOLLIPOP
	 */
	public static boolean hasLollipop() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

	/**
	 * Create a Scale animation programmatically.<br/>
	 * Usage of this method helps rendering the page much faster
	 * (it doesn't load the animation file from disk).
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
	 * This Enable and Reset initial layout animation delay to the default {@link #DEFAULT_INITIAL_DELAY}.
	 *
	 * @see #resetLayoutAnimationDelay(boolean, long)
	 * @see #stopLayoutAnimation()
	 */
	public static void resetLayoutAnimationDelay() {
		resetLayoutAnimationDelay(true, DEFAULT_INITIAL_DELAY);
	}

	/**
	 * Reset initial layout animation delay to a custom delay.<br/>
	 * This avoid to continuously increase the next step delay of the next FlipView on the screen!
	 * <p/>
	 * <b>Note: </b>call this method at the beginning of onCreate/onActivityCreated.
	 *
	 * @param enable    optionally future start animation can be disabled
	 * @param nextDelay the new custom initial delay
	 * @see #resetLayoutAnimationDelay()
	 * @see #stopLayoutAnimation()
	 */
	public static void resetLayoutAnimationDelay(boolean enable, long nextDelay) {
		enableInitialAnimation = enable;
		initialDelay = nextDelay;
	}

	/**
	 * Stop and Reset layout animation after {@link #STOP_LAYOUT_ANIMATION_DELAY}.<br/>
	 * This gives the time to perform all entry animations but to stop further animations when
	 * screen is fully rendered: ALL Views will not perform initial animation anymore
	 * until a new reset.
	 * <p/>
	 * <b>Note: </b>the delay time has been identified at 1 second and half (1500ms).<br/>
	 * Call this method at the end of onCreate/onActivityCreated.
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

	/*
	 * Override to always display content in design mode.
	 */
	@Override
	public void setInAnimation(Context context, int resourceID) {
		if (!isInEditMode()) super.setInAnimation(context, resourceID);
	}

	/*
	 * Override to always display content in design mode.
	 */
	@Override
	public void setOutAnimation(Context context, int resourceID) {
		if (!isInEditMode()) super.setOutAnimation(context, resourceID);
	}

	private void initInAnimation(long duration) {
		if (getInAnimation() == null)
			this.setInAnimation(getContext(), R.anim.grow_from_middle_x_axis);
		super.getInAnimation().setDuration(duration);
		if (anticipateInAnimationTime > duration) anticipateInAnimationTime = duration;
		super.getInAnimation().setStartOffset(duration - anticipateInAnimationTime);
	}

	private void initOutAnimation(long duration) {
		if (getOutAnimation() == null)
			this.setOutAnimation(getContext(), R.anim.shrink_to_middle_x_axis);
		super.getOutAnimation().setDuration(duration);
	}

	public void animateLayout(Animation layoutAnimation) {
		startAnimation(layoutAnimation);
	}

	/**
	 * @return the Animation of this FlipView layout.
	 */
	public Animation getInitialLayoutAnimation() {
		return this.initialLayoutAnimation;
	}

	public void setInitialLayoutAnimation(int animationResId) {
		try {
			setInitialLayoutAnimation(animationResId > 0 ?
					AnimationUtils.loadAnimation(getContext(), animationResId) :
					createScaleAnimation());//Usage of the method it's faster (not read from disk)
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Initial animation with id " + animationResId
					+ " could not be found. Initial animation cannot be set!");
		}
	}

	/**
	 * Custom initial layout animation.<br/>
	 * <b>Note:</b> Duration, startOffset will be overridden by the current settings:<br/>
	 * duration = initialLayoutAnimationDuration;<br/>
	 * startOffset = initialDelay += SCALE_STEP_DELAY.
	 *
	 * @param initialLayoutAnimation the new initial animation
	 */
	final public void setInitialLayoutAnimation(Animation initialLayoutAnimation) {
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

	public void setRearImageAnimation(int animationResId) {
		try {
			setRearImageAnimation(AnimationUtils.loadAnimation(getContext(),
					animationResId > 0 ? animationResId : R.anim.scale_up));
			if (DEBUG) Log.d(TAG, "Rear animation is active!");
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Rear animation with id " + animationResId
					+ " could not be found. Rear animation cannot be set!");
		}
	}

	public void setRearImageAnimation(Animation rearAnimation) {
		this.rearImageAnimation = rearAnimation;
		if (rearImageAnimationDuration > 0)
			this.rearImageAnimation.setDuration(rearImageAnimationDuration);
	}

	/**
	 * Get the duration of the flip animation.
	 *
	 * @return The duration in milliseconds
	 */
	public long getMainAnimationDuration() {
		return getInAnimation().getDuration();
	}

	/**
	 * Set the duration of the main animation.
	 *
	 * @param duration The duration in milliseconds
	 */
	public void setMainAnimationDuration(long duration) {
		if (DEBUG) Log.d(TAG, "Setting mainAnimationDuration=" + duration);
		mainAnimationDuration = duration;
		initInAnimation(duration);
		initOutAnimation(duration);
	}

	/**
	 * Get the duration of the initial animation when the View is first displayed.
	 *
	 * @return The duration in milliseconds
	 */
	public long getInitialLayoutAnimationDuration() {
		return initialLayoutAnimationDuration;
	}

	/**
	 * Set the duration of the initial animation when the View is first displayed.
	 *
	 * @param duration The duration in milliseconds
	 */
	public void setInitialLayoutAnimationDuration(long duration) {
		if (DEBUG) Log.d(TAG, "Setting initialLayoutAnimationDuration=" + duration);
		this.initialLayoutAnimationDuration = duration;
		if (initialLayoutAnimation != null)
			initialLayoutAnimation.setDuration(duration);
	}

	/**
	 * Get the duration of the animation of the rear ImageView.
	 *
	 * @return The duration in milliseconds
	 */
	public long getRearImageAnimationDuration() {
		return rearImageAnimationDuration;
	}

	/**
	 * Set the duration of the animation of the rear ImageView.
	 *
	 * @param duration The duration in milliseconds
	 */
	public void setRearImageAnimationDuration(long duration) {
		if (DEBUG) Log.d(TAG, "Setting rearImageAnimationDuration=" + duration);
		this.rearImageAnimationDuration = duration;
		if (rearImageAnimation != null)
			rearImageAnimation.setDuration(duration);
	}

	//************************
	// PERFORMING ANIMATION **
	//************************

	/**
	 * Flip the current View and display to the next View now!
	 */
	@Override
	final public void showNext() {
		this.showNext(0L);
	}

	/**
	 * Flip the current View and display to the next View with a delay.
	 *
	 * @param delay any custom delay
	 */
	final public void showNext(long delay) {
		if (DEBUG) Log.d(TAG, "showNext " + (getDisplayedChild() + 1) + " delay=" + delay);
		this.flip(getDisplayedChild() + 1, delay);
	}

	/**
	 * Flip the current View and display to the previous View now!
	 */
	@Override
	final public void showPrevious() {
		this.showPrevius(0L);
	}

	/**
	 * Flip the current View and display to the previous View with a delay.<br/>
	 * If the previous
	 *
	 * @param delay any custom delay
	 */
	final public void showPrevius(long delay) {
		if (DEBUG) Log.d(TAG, "showPrevius " + (getDisplayedChild() - 1) + " delay=" + delay);
		this.flip(getDisplayedChild() - 1, delay);
	}

	public boolean isFlipped() {
		return checked;
	}

	/**
	 * Convenience method for layout that has only 2 child Views!<br/>
	 * Execute the flip animation with No delay.
	 *
	 * @param showRear <i>true</i> to show back View, <i>false</i> to show front View
	 */
	final public void flip(boolean showRear) {
		flip(showRear, 0L);
	}

	/**
	 * Convenience method for layout that has only 2 child Views!<br/>
	 * Execute the flip animation with a custom delay.
	 *
	 * @param showRear <i>true</i> to show back View, <i>false</i> to show front View
	 * @param delay    any custom delay
	 */
	final public void flip(boolean showRear, long delay) {
		flip(showRear ? REAR_VIEW_INDEX : FRONT_VIEW_INDEX, delay);
	}

	/**
	 * Set the state of this component to the given value, performing the
	 * corresponding main animation and, if it exists, the rear Image animation.
	 *
	 * @param whichChild the progressive index of the child View (first View has index=0).
	 * @param delay      any custom delay
	 */
	final public void flip(final int whichChild, long delay) {
		if (DEBUG)
			Log.d(TAG, "Flip! whichChild=" + whichChild + ", previousChild=" + getDisplayedChild() + ", delay=" + delay);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
			checked = (whichChild == FRONT_VIEW_INDEX || getDisplayedChild() == FRONT_VIEW_INDEX);
				setDisplayedChild(whichChild);//start main animation
				animateRearImageIfNeeded();
				mFlippingListener.onFlipped(FlipView.this, checked);
			}
		}, delay);
	}

	private void animateRearImageIfNeeded() {
		if (checked && rearImage != null && rearImageAnimation != null) {
			rearImage.setAlpha(0f);//This avoids to see a glitch of the rear image
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					rearImage.setAlpha(1f);
					rearImage.startAnimation(rearImageAnimation);
				}
			}, rearImageAnimationDelay);//Wait InAnimation completion before to start rearImageAnimation?
		}
	}

	/**
	 * Convenience method for layout that has only 2 child Views.
	 *
	 * @param showRear <i>true</i> to show back View, <i>false</i> to show front View
	 * @see #flipSilently(int)
	 */
	final public void flipSilently(boolean showRear) {
		flipSilently(showRear ? REAR_VIEW_INDEX : FRONT_VIEW_INDEX);
	}

	/**
	 * Show a specific View immediately skipping any animations.
	 *
	 * @param whichChild the index of the child view to display (first View has index=0).
	 */
	final public void flipSilently(int whichChild) {
		if (DEBUG) Log.d(TAG, "flipSilently whichChild=" + whichChild);
		whichChild = checkIndex(whichChild);
		Animation inAnimation = super.getInAnimation();
		Animation outAnimation = super.getOutAnimation();
		super.setInAnimation(null);
		super.setOutAnimation(null);
		checked = (whichChild > FRONT_VIEW_INDEX);
		super.setDisplayedChild(whichChild);
		super.setInAnimation(inAnimation);
		super.setOutAnimation(outAnimation);
	}

	/**
	 * This checks that the index is never negative or bigger than the actual child Views!<br/>
	 * - if negative: first child View id displayed;<br/>
	 * - if bigger than actual Views: last child View is displayed.<br/><br/>
	 * <i>The logic is different than {@link #setDisplayedChild(int)} where:</i><br/>
	 * - if negative: last child View is displayed;<br/>
	 * - if bigger than actual Views: first child View is displayed.
	 *
	 * @param whichChild the index of the child View to display
	 * @return the New index of the child View to display
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
	 *
	 * @param layoutResId The layout resource identifier.
	 */
	public void setFrontLayout(int layoutResId) {
		if (layoutResId == R.layout.flipview_front) {
			if (DEBUG) Log.d(TAG, "Adding Inner FrontLayout");
		} else if (DEBUG) Log.d(TAG, "Setting FrontLayout " + layoutResId);
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
		ViewGroup viewGroup = this;
		//If the View is another ViewGroup use it as front View to flip
		if (view instanceof ViewGroup) {
			if (DEBUG) Log.d(TAG, "FrontLayout is a ViewGroup");
			viewGroup = (ViewGroup) view;
		}
		//If any ImageView at first position is provided, reference to this front ImageView is saved.
		if (viewGroup.getChildAt(FRONT_VIEW_INDEX) instanceof ImageView) {
			if (DEBUG) Log.d(TAG, "Found ImageView in the ViewGroup");
			frontImage = (ImageView) viewGroup.getChildAt(FRONT_VIEW_INDEX);
		} else if (viewGroup.getChildAt(FRONT_VIEW_INDEX) instanceof TextView) {
			if (DEBUG) Log.d(TAG, "Found TextView in the ViewGroup");
			frontText = (TextView) viewGroup.getChildAt(FRONT_VIEW_INDEX);
		}
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
	 *
	 * @param layoutResId The layout resource identifier.
	 */
	public void addRearLayout(int layoutResId) {
		if (layoutResId == R.layout.flipview_rear) {
			if (DEBUG) Log.d(TAG, "Adding Inner RearLayout");
		} else if (DEBUG) Log.d(TAG, "Adding RearLayout " + layoutResId);
		addRearLayout(LayoutInflater.from(getContext()).inflate(layoutResId, this, false));
	}

	/**
	 * Set the rear view to be displayed when this component is in state <i>checked</i>.
	 * The provided <i>view</i> must not be {@code null}, or
	 * an IllegalArgumentException will be thrown.
	 *
	 * @param view The view. Must not be {@code null}.
	 */
	public void addRearLayout(@NonNull View view) {
		ViewGroup viewGroup = this;
		//Assign current count as our Index for rear View in case multiples views are added.
		int whichChild = getChildCount(); //By default suppose it's already our rear View
		if (DEBUG) Log.d(TAG, "RearLayout index=" + whichChild);
		//If the View is another ViewGroup use it as new our rear View to flip
		if (view instanceof ViewGroup) {
			if (DEBUG) Log.d(TAG, "RearLayout is a ViewGroup");
			viewGroup = (ViewGroup) view;
			whichChild = 0; //Override the index: use the first position to locate the ImageView in this ViewGroup
		}
		//If any ImageView is provided, reference to this rear ImageView is saved
		if (viewGroup.getChildAt(whichChild) instanceof ImageView) {
			if (DEBUG) Log.d(TAG, "Found ImageView in the ViewGroup");
			rearImage = (ImageView) viewGroup.getChildAt(whichChild);
		} else if (whichChild > 2) {
			rearImage = null; //Rollback in case multiple views are added (user must provide already the image in each layout added)
		}
		//Watch out! User can add first the rear view and after the front view that must be
		// always at position 0. While all rear views start from index = 1.
		setView(view, getChildCount() == 0 ? REAR_VIEW_INDEX : getChildCount());
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void setView(View view, int whichChild) {
		if (view == null)
			throw new IllegalArgumentException("The provided view must not be null");

		if (DEBUG) Log.d(TAG, "Setting view at index " + whichChild);

		if (super.getChildAt(whichChild) != null)
			super.removeViewAt(whichChild);
		super.addView(view, whichChild, super.generateDefaultLayoutParams());
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

	@Override
	public PictureDrawable getPictureDrawable() {
		return pictureDrawable;
	}

	@Override
	public void setPictureDrawable(PictureDrawable drawable) {
		pictureDrawable = drawable;
		if (this.frontImage == null) {
			Log.w(TAG, "ImageView not found in the first child of the FrontLayout. Image cannot be set!");
			return;
		}
		frontImage.setImageDrawable(pictureDrawable);
	}

	public void setImageBitmap(Bitmap bitmap) {
		if (this.frontImage == null) {
			Log.w(TAG, "ImageView not found in the first child of the FrontLayout. Bitmap cannot be set!");
			return;
		}
		frontImage.setImageBitmap(bitmap);
	}

	public void setFrontImage(int imageResId) {
		if (this.frontImage == null) {
			Log.w(TAG, "ImageView not found in the first child of the FrontLayout. Image cannot be set!");
			return;
		}
		if (imageResId == 0) return;
		try {
			this.frontImage.setPadding(frontImagePadding, frontImagePadding,
					frontImagePadding, frontImagePadding);
			this.frontImage.setImageResource(imageResId);
		} catch (Resources.NotFoundException e) {
			Log.w(TAG, "No front resource image id " + imageResId + " found. No Image can be set!");
		}
	}

	public void setFrontText(CharSequence text) {
		if (this.frontText == null) {
			Log.w(TAG, "TextView not found in the first child of the FrontLayout. Text cannot be set!");
			return;
		}
		frontText.setText(text);
	}

	public void setRearImage(int imageResId) {
		if (this.rearImage == null) {
			Log.w(TAG, "ImageView not found in the child of the RearLayout. Image cannot be set!");
			return;
		}
		if (imageResId == 0) return;
		try {
			this.rearImage.setPadding(rearImagePadding, rearImagePadding,
					rearImagePadding, rearImagePadding);
			this.rearImage.setImageResource(imageResId);
		} catch (Resources.NotFoundException e) {
			Log.w(TAG, "No rear resource image id " + imageResId + " found. Image cannot be set!");
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@SuppressWarnings("deprecation")
	public void setChildBackgroundDrawable(int whichChild, int drawableResId) {
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
		setChildBackgroundDrawable(whichChild, createOvalDrawable(color));
	}

	//*******************
	// SHAPE DRAWABLES **
	//*******************

	private static ShapeDrawable createShapeDrawable(int color, Shape shape) {
		ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
		shapeDrawable.getPaint().setColor(color);
		shapeDrawable.getPaint().setAntiAlias(true);
		shapeDrawable.getPaint().setStyle(Paint.Style.FILL);
		return shapeDrawable;
	}

	/**
	 * @param color the desired color
	 * @return ShapeDrawable with Oval shape
	 */
	public static ShapeDrawable createOvalDrawable(@ColorInt int color) {
		return createShapeDrawable(color, new OvalShape());
	}

	/**
	 * @param color      the desired color
	 * @param startAngle the angle (in degrees) where the arc begins
	 * @param sweepAngle the sweep angle (in degrees).
	 *                   Anything equal to or greater than 360 results in a complete circle/oval.
	 * @return ShapeDrawable with Arc shape
	 */
	public static ShapeDrawable createArcShapeDrawable(
			@ColorInt int color, float startAngle, float sweepAngle) {
		return createShapeDrawable(color, new ArcShape(startAngle, sweepAngle));
	}

	/**
	 * RoundRectShape constructor.
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
	 * @return ShapeDrawable with RoundRect shape
	 */
	public static ShapeDrawable createRoundRectShapeDrawable(
			@ColorInt int color, float[] outerRadii, RectF inset, float[] innerRadii) {
		return createShapeDrawable(color, new RoundRectShape(outerRadii, inset, innerRadii));
	}

}