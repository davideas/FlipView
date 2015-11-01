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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Handler;
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
 *
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
 *
 * <li>Optionally, this FlipView supports a {@link PictureDrawable} for SVG loading
 * and assignment <i>front View Only</i>. Remember to change the LayerType to
 * {@link View#LAYER_TYPE_SOFTWARE}.<br/><br/></li>
 *
 * <li>Not less this FlipView can born already flipped but also flip animation can be disabled
 * but only at design time.<br/><br/></li>
 *
 * <li>If the custom layout included a TextVIew instead of ImageView as first child, custom text can
 * be displayed. Having such TextView you can assign any text and style for the front View.
 * <br/><br/></li>
 *
 * <li>Another functionality is to assign to the entire FlipView itself, an <b>initial animation</b>
 * (by default it's a Scale animation and not enabled) in order to reach different combinations
 * of effects:<br/>
 * For instance, having multiples FlipViews on the screen, this animation can be prepared for
 * simultaneous entry effect (all FlipViews will perform the animation at the same time) or
 * for a delayed entry effect (all FlipViews will perform the animation with step delay).
 * <br/><br/></li>
 *
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
	private static final int FRONT_VIEW_INDEX = 0;

	/**
	 * Child index to access the <i>rear</i> view.
	 */
	private static final int REAR_VIEW_INDEX = 1;

	/**
	 * Use this to apply a default resource value.
	 */
	public static final int DEFAULT_RESOURCE = 0;

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
	public static final int
			DEFAULT_INITIAL_DELAY = 500,
			SCALE_STEP_DELAY = 35,
			STOP_LAYOUT_ANIMATION_DELAY = 1500,
			FLIP_INITIAL_DELAY = 250,
			FLIP_DURATION = 100,
			INITIAL_ANIMATION_DURATION = 250,
			REAR_IMAGE_ANIMATION_DURATION = 150;
	private long initialLayoutAnimationDuration,
			rearImageAnimationDuration,
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
		//Necessary for SVG rendering
		//setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		//Read and apply provided attributes
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.FlipView, 0, 0);

		//Flags
		checked = a.getBoolean(R.styleable.FlipView_checked, false);
		enableInitialAnimation = a.getBoolean(R.styleable.FlipView_enableInitialAnimation, false);
		boolean animateDesignChildViewsOnly = a.getBoolean(R.styleable.FlipView_animateDesignLayoutOnly, false);

		//FrontView
		if (!animateDesignChildViewsOnly) {
			int frontLayout = a.getResourceId(R.styleable.FlipView_frontLayout, R.layout.flipview_front);
			Drawable frontBackground = a.getDrawable(R.styleable.FlipView_frontBackground);
			int frontBackgroundColor = a.getColor(R.styleable.FlipView_frontBackgroundColor, 0);
			int frontImage = a.getResourceId(R.styleable.FlipView_frontImage, 0);
			frontImagePadding = (int) a.getDimension(R.styleable.FlipView_frontImagePadding, 0);
			setFrontLayout(frontLayout);
			setChildBackgroundDrawable(FRONT_VIEW_INDEX, frontBackground, frontBackgroundColor);
			setFrontImage(frontImage);
		}


		if (!animateDesignChildViewsOnly) {
			//RearView
			int rearLayout = a.getResourceId(R.styleable.FlipView_rearLayout, R.layout.flipview_rear);
			Drawable rearBackground = a.getDrawable(R.styleable.FlipView_rearBackground);
			int rearBackgroundColor = a.getColor(R.styleable.FlipView_rearBackgroundColor, 0);
			int rearImage = a.getResourceId(R.styleable.FlipView_rearImage, R.drawable.ic_action_done);
			rearImagePadding = (int) a.getDimension(R.styleable.FlipView_rearImagePadding, 0);
			addRearLayout(rearLayout);
			setChildBackgroundDrawable(REAR_VIEW_INDEX, rearBackground, rearBackgroundColor);
			setRearImage(rearImage);
		}

		//Init main(Flip) animations
		long duration = a.getInteger(R.styleable.FlipView_animationDuration, FLIP_DURATION);
		rearImageAnimationDuration = a.getInteger(R.styleable.FlipView_rearImageAnimationDuration, REAR_IMAGE_ANIMATION_DURATION);
		anticipateInAnimationTime = a.getInteger(R.styleable.FlipView_anticipateInAnimationTime, 0);
		if (!isInEditMode()) {
			//This also initialize the in/out animations
			setMainAnimationDuration(duration);
			if (a.getBoolean(R.styleable.FlipView_animateRearImage, true))
				setRearImageAnimation(a.getResourceId(R.styleable.FlipView_rearImageAnimation, 0));
		}

		//Display rear view at start if requested
		if (checked) flipSilently(true);

		//Apply default OnClickListener if clickable
		if (isClickable()) setOnClickListener(this);

		//Show initial cascade step animation when view is first rendered
		if (enableInitialAnimation) {
			duration = a.getInteger(R.styleable.FlipView_initialLayoutAnimationDuration, INITIAL_ANIMATION_DURATION);
			setInitialLayoutAnimationDuration(duration);
			setInitialLayoutAnimation(a.getResourceId(R.styleable.FlipView_initialLayoutAnimation, 0));

			if (!isInEditMode())
				animateLayout(getInitialLayoutAnimation());
		}

		a.recycle();
	}

	//*************
	// LISTENERS **
	//*************

	public void setOnFlippingListener(OnFlippingListener listener) {
		this.mFlippingListener = listener;
	}

	@Override
	public void onClick(View v) {
		flip();
	}

	//******************
	// STATIC METHODS **
	//******************

	public static ShapeDrawable createOvalDrawable() {
		ShapeDrawable shapeDrawable = new ShapeDrawable();
		shapeDrawable.setShape(new OvalShape());
		return shapeDrawable;
	}

	public static Animation createScaleAnimation(long duration) {
		ScaleAnimation scale_up = new ScaleAnimation(0, 1.0f, 0, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		scale_up.setDuration(duration);
		scale_up.setInterpolator(new DecelerateInterpolator());
		scale_up.setStartOffset(initialDelay += SCALE_STEP_DELAY);
		return scale_up;
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
	 * <b>Note: </b>call this method at the beginning of onCreate/onActivityCreated
	 *
	 * @param enable optionally future start animation can be disabled.
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
	 * screen is fully rendered.
	 * <p/>
	 * <b>Note: </b>this time has been calculated to 1 second and half (1500ms).<br/>
	 * Call this method at the end of onCreate/onActivityCreated
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
	 * Override to always display content in design mode
	 */
	@Override
	public void setInAnimation(Context context, int resourceID) {
		if (!isInEditMode()) super.setInAnimation(context, resourceID);
	}
	/*
	 * Override to always display content in design mode
	 */
	@Override
	public void setOutAnimation(Context context, int resourceID) {
		if (!isInEditMode()) super.setOutAnimation(context, resourceID);
	}

	private void initInAnimation(long duration) {
		if (getInAnimation() == null)
			setInAnimation(getContext(), R.anim.grow_from_middle_x_axis);
		getInAnimation().setDuration(duration);
		if (anticipateInAnimationTime > duration) anticipateInAnimationTime = duration;
		getInAnimation().setStartOffset(duration - anticipateInAnimationTime);
	}

	private void initOutAnimation(long duration) {
		if (getOutAnimation() == null)
			setOutAnimation(getContext(), R.anim.shrink_to_middle_x_axis);
		getOutAnimation().setDuration(duration);
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

	public void setInitialLayoutAnimation(int animationResId) {
		try {
			setInitialLayoutAnimation(animationResId > 0 ?
					AnimationUtils.loadAnimation(getContext(), animationResId) :
							createScaleAnimation(initialLayoutAnimationDuration));
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Initial animation with id " + animationResId + " could not be found. Initial animation cannot be set!");
		}
	}

	public void setInitialLayoutAnimation(Animation initialLayoutAnimation) {
		this.initialLayoutAnimation = initialLayoutAnimation;
		initialLayoutAnimation.setDuration(initialLayoutAnimationDuration);
		initialLayoutAnimation.setInterpolator(new DecelerateInterpolator());
		initialLayoutAnimation.setStartOffset(initialDelay += SCALE_STEP_DELAY);
	}

	/**
	 * @return the animation of the rear ImageView
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
			Log.e(TAG, "Rear animation with id " + animationResId + " could not be found. Rear animation cannot be set!");
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
		if (DEBUG) Log.d(TAG, "Setting mainAnimationDuration="+duration);
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
	 * Execute the flip animation with no delay
	 */
	final public void flip() {
		flip(0L);
	}

	/**
	 * Execute the flip animation with a custom delay.
	 *
	 * @param delay any custom delay
	 */
	final public void flip(long delay) {
		flip(!this.checked, delay);
	}

	/**
	 * Set the state of this component to the given value, applying the
	 * corresponding rear animation, if possible.
	 *
	 * @param showRear <i>true</i> to show back image, <i>false</i> to show front image
	 * @param delay any custom delay
	 */
	final public void flip(final boolean showRear, long delay) {
		if (DEBUG) Log.d(TAG, "Flip! With delay="+delay);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				checked = showRear;
				showNext();
				if (checked && rearImage != null && rearImageAnimation != null) {
					rearImage.setAlpha(0f);//This avoids to see a glitch of the rear image
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							rearImage.setAlpha(1f);
							rearImage.startAnimation(rearImageAnimation);
						}
					}, getInAnimation().getDuration());//Wait InAnimation completion before to start rearImageAnimation
				}
				mFlippingListener.onFlipped(FlipView.this, checked);
			}
		}, delay);
	}

	/**
	 * Show rear view immediately without any animations.
	 *
	 * @param showRear <i>true</i> to show back image, <i>false</i> to show front image
	 */
	final public void flipSilently(boolean showRear) {
		Animation inAnimation = getInAnimation();
		Animation outAnimation = getOutAnimation();
		this.checked = showRear;
		setInAnimation(null);
		setOutAnimation(null);
		if (showRear) {
			showNext();
			setInAnimation(inAnimation);
			setOutAnimation(outAnimation);
		}
	}

	public boolean isFlipped() {
		return checked;
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
	 * If an invalid resource or {@link #DEFAULT_RESOURCE} is
	 * passed, then the default view will be applied.
	 *
	 * @param layoutResId The layout resource identifier.
	 */
	public void setFrontLayout(int layoutResId) {
		if (layoutResId == R.layout.flipview_front)
			if (DEBUG) Log.d(TAG, "Adding Inner FrontLayout");
		else
			if (DEBUG) Log.d(TAG, "Setting FrontLayout "+layoutResId);
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
		if (viewGroup.getChildAt(0) instanceof ImageView) {
			if (DEBUG) Log.d(TAG, "Found ImageView in the ViewGroup");
			frontImage = (ImageView) viewGroup.getChildAt(0);
		} else if (viewGroup.getChildAt(0) instanceof TextView) {
			if (DEBUG) Log.d(TAG, "Found TextView in the ViewGroup");
			frontText = (TextView) viewGroup.getChildAt(0);
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
	 * If an invalid resource or {@link #DEFAULT_RESOURCE} is
	 * passed, then the default view will be applied.
	 *
	 * @param layoutResId The layout resource identifier.
	 */
	public void addRearLayout(int layoutResId) {
		if (layoutResId == R.layout.flipview_rear)
			if (DEBUG) Log.d(TAG, "Adding Inner RearLayout");
		else
			if (DEBUG) Log.d(TAG, "Adding RearLayout "+layoutResId);
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
		int index = getChildCount(); //By default suppose it's already our rear View
		if (DEBUG) Log.d(TAG, "RearLayout index="+index);
		//If the View is another ViewGroup use it as new our rear View to flip
		if (view instanceof ViewGroup) {
			if (DEBUG) Log.d(TAG, "RearLayout is a ViewGroup");
			viewGroup = (ViewGroup) view;
			index = 0; //Override the index: use the first position to locate the ImageView in this ViewGroup
		}
		//If any ImageView is provided, reference to this rear ImageView is saved
		if (viewGroup.getChildAt(index) instanceof ImageView) {
			if (DEBUG) Log.d(TAG, "Found ImageView in the ViewGroup");
			rearImage = (ImageView) viewGroup.getChildAt(index);
		} else if (index > 2) {
			rearImage = null; //Rollback in case multiple views are added (user must provide already the image in each layout added)
		}
		//Watch out! User can add first the rear view and after the front view that must be
		// always at position 0. While all rear views start from index = 1.
		setView(view, getChildCount() == 0 ? REAR_VIEW_INDEX : getChildCount());
	}

	private void setView(View view, int index) {
		if (view == null)
			throw new IllegalArgumentException("The provided view must not be null");

		if (DEBUG) Log.d(TAG, "Setting view " + view.getId() + " at index " + index);

		if (super.getChildAt(index) != null)
			super.removeViewAt(index);
		super.addView(view, index, super.generateDefaultLayoutParams());
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
		try {
			this.rearImage.setPadding(rearImagePadding, rearImagePadding,
					rearImagePadding, rearImagePadding);
			this.rearImage.setImageResource(imageResId);
		} catch (Resources.NotFoundException e) {
			Log.w(TAG, "No rear resource image id " + imageResId + " found. Image cannot be set!");
		}
	}

	public void setChildBackgroundDrawable(int index, int drawableResId, int color) {
		try {
			setChildBackgroundDrawable(index,
					getContext().getResources().getDrawable(drawableResId), color);
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Resource with id " + drawableResId + " could not be found. Drawable cannot be set!");
		}
	}

	public void setChildBackgroundDrawable(int index, Drawable drawable, int color) {
		if (drawable == null) {
			drawable = createOvalDrawable();
			if (color == 0) color = Color.GRAY; //If no colors is provided by the user
		}
		if (getChildAt(index) != null) {
			drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			getChildAt(index).setBackground(drawable);
		}
	}

	public Drawable getChildBackgroundDrawable(int index) {
		return getChildAt(index).getBackground();
	}

}