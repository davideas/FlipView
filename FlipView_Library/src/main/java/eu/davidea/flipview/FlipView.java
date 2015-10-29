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
import android.widget.ViewFlipper;

/**
 * FlipView is a ViewGroup (FrameLayout) that displays 2 views/layouts by flipping
 * the front one in favor of the back one, and vice versa. Optionally more views can be
 * displayed in series one after another since it extends {@link android.widget.ViewAnimator}.
 * <p/>
 * Usage is very simple. You just need to add this View to any layout (like you would
 * do with any other View) and you are good to go. You can customize the behaviours
 * by assigning values to the optional properties in the layout or programmatically.<br/>
 * Please, refer to those attributes documentation for more details.
 * <p/>
 * The Views to flip can be more <b>ViewGroups</b> containing an ImageView or simply more
 * <b>Views</b> (preferable ImageView) or even a combination of the 2 types.
 * <ul>
 * <li>In case of <b>ViewGroups</b> with an ImageView each, (if present) background drawable
 * and color are assigned to those ViewGroups and the image resources to those ImageViews.
 * In this case the entire ViewGroups (containing the ImageViews) will flip.<br/>
 * Choosing this option, when 2 ViewGroups are configured, a second checkAnimation will be
 * assigned and executed on the rear ImageView after the first flip is consumed.<br/>
 * <b>Note: </b>the library contains already the checked ImageView for the rear image!</li>
 * <li>In case of <b>Views</b>, (if present) background drawable and color are assigned
 * to this ViewGroup (FlipView) and only the simple views will flip.<br/>
 * Choosing this option, no further animation will be performed on the rear Views.</li>
 * </ul>
 * <p/>
 * Optionally, this FlipView supports a {@link PictureDrawable} for SVG loading and assignment
 * for <i>front View Only</i>. Remember to change the LayerType to {@link View#LAYER_TYPE_SOFTWARE}.
 * <p/>
 * Not less this FlipView can born already flipped but also flip animation can be disabled
 * but only at design time.
 * <p/>
 * Another functionality is to assign to the entire FlipView itself, an <b>initial animation</b>
 * (by default is a Scale animation and not used) in order to reach different combinations
 * of effects:<br/>
 * For instance, having multiples FlipViews on the screen, this animation can be prepared for
 * simultaneous entry effect (all FlipViews will perform the animation at the same time) or
 * for a delayed entry effect (all FlipViews will perform the animation with step delay).
 * <p/>
 * Finally, when the View is clicked, it will switch its state. The event is
 * propagated with the listener {@link OnFlippingListener#onFlipped(FlipView, boolean)}.
 * You can subscribe to that listener using {@link #setOnFlippingListener(OnFlippingListener)}
 * method.
 *
 * @author Davide Steduto
 * @since 25/10/2015
 */
//@SuppressWarnings("unused")
public class FlipView extends ViewFlipper implements SVGPictureDrawable, View.OnClickListener {

	private static final String TAG = FlipView.class.getSimpleName();

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
	 * Reference to the ImageView of the FrontLayout
	 */
	private ImageView frontImage;
	private int mFrontImagePadding;

	/**
	 * Reference to the ImageView of the RearLayout
	 */
	private ImageView rearImage;
	private int mRearImagePadding;

	/**
	 * Drawable used for SVG images for the front View Only
 	 */
	private PictureDrawable pictureDrawable;

	/**
	 * Animations attributes
	 */
	private boolean mChecked, mCheckable;
	private Animation initialLayoutAnimation;
	private Animation checkAnimation;
	public static final int
			DEFAULT_INITIAL_DELAY = 500,
			SCALE_DURATION = 250,
			SCALE_STEP_DELAY = 35,
			STOP_LAYOUT_ANIMATION_DELAY = 1500,
			FLIP_INITIAL_DELAY = 250,
			FLIP_DURATION = 125;
	private long initialLayoutAnimationDuration;
	static long initialDelay = DEFAULT_INITIAL_DELAY;
	private static boolean enableInitialAnimation = true;

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
		mChecked = a.getBoolean(R.styleable.FlipView_checked, false);
		mCheckable = a.getBoolean(R.styleable.FlipView_checkable, true);
		enableInitialAnimation = a.getBoolean(R.styleable.FlipView_enableInitialAnimation, false);

		//Apply default OnClickListener
		if (!a.getBoolean(R.styleable.FlipView_disableClickListener, false))
			setOnClickListener(this);

		//FrontView
		int frontLayout = a.getResourceId(R.styleable.FlipView_frontLayout, R.layout.flipview_front);
		Drawable frontBackground = a.getDrawable(R.styleable.FlipView_frontBackground);
		int frontBackgroundColor = a.getColor(R.styleable.FlipView_frontBackgroundColor, Color.GRAY);
		int frontImage = a.getResourceId(R.styleable.FlipView_frontImage, 0);
		mFrontImagePadding = (int) a.getDimension(R.styleable.FlipView_frontImagePadding, 0);
		setFrontLayout(frontLayout);
		setFrontImage(frontImage);
		setChildBackgroundDrawable(FRONT_VIEW_INDEX, frontBackground, frontBackgroundColor);


		if (mCheckable) {
			//RearView
			int rearLayout = a.getResourceId(R.styleable.FlipView_rearLayout, R.layout.flipview_rear);
			Drawable rearBackground = a.getDrawable(R.styleable.FlipView_rearBackground);
			int rearBackgroundColor = a.getColor(R.styleable.FlipView_rearBackgroundColor, Color.GRAY);
			int rearImage = a.getResourceId(R.styleable.FlipView_rearImage, R.drawable.ic_action_done);
			mRearImagePadding = (int) a.getDimension(R.styleable.FlipView_rearImagePadding, 0);
			addRearLayout(rearLayout);
			setRearImage(rearImage);
			setChildBackgroundDrawable(REAR_VIEW_INDEX, rearBackground, rearBackgroundColor);

			//Init Flip animations
			long duration = a.getInteger(R.styleable.FlipView_flipAnimationDuration, FLIP_DURATION);
			initInAnimation(duration);
			initOutAnimation(duration);
			setCheckAnimation(R.anim.scale_in);

			//Display rear view at start if requested
			if (mChecked) flipSilently(true);
		}

		//Show initial cascade step animation when view is first rendered
		if (enableInitialAnimation) {
			long duration = a.getInteger(R.styleable.FlipView_initialLayoutAnimationDuration, SCALE_DURATION);
			setInitialLayoutAnimationDuration(duration);
			setInitialLayoutAnimation(a.getResourceId(R.styleable.FlipView_initialLayoutAnimation, 0));

			//setAlpha(0.0f);
			if (!isInEditMode())
				animateLayout(getInitialLayoutAnimation());
		}

		a.recycle();
	}

	/* LISTENERS ****************************************************/

	public void setOnFlippingListener(OnFlippingListener listener) {
		this.mFlippingListener = listener;
	}


	@Override
	public void onClick(View v) {
		flip();
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

	public static Animation getScaleAnimation() {
		ScaleAnimation scale_in = new ScaleAnimation(0, 1.0F, 0, 1.0F,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		scale_in.setDuration(SCALE_DURATION);
		scale_in.setInterpolator(new DecelerateInterpolator());
		scale_in.setStartOffset(initialDelay += SCALE_STEP_DELAY);
		return scale_in;
	}

	public void animateLayout(Animation layoutAnimation) {
		//setAlpha(1.0f);
		startAnimation(layoutAnimation);
	}

	/**
	 * Get the duration of the flip animation.
	 *
	 * @return The animation duration in milliseconds.
	 */
	public long getFlipAnimationDuration() {
		return getInAnimation().getDuration();
	}

	/**
	 * Set the duration of the flip animation.
	 *
	 * @param duration The flip animation duration in milliseconds.
	 */
	public void setFlipAnimationDuration(long duration) {
		initInAnimation(duration);
		initOutAnimation(duration);
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

	/**
	 * Set the duration when the View is first displayed.
	 *
	 * @return The duration in milliseconds of the start Animation
	 */
	public long getInitialLayoutAnimationDuration() {
		return initialLayoutAnimationDuration;
	}

	/**
	 * Set the duration when the View is first displayed.
	 *
	 * @param duration The duration in milliseconds of the start Animation
	 */
	public void setInitialLayoutAnimationDuration(long duration) {
		Log.d(TAG, "Setting initialLayoutAnimationDuration="+duration);
		this.initialLayoutAnimationDuration = duration;
	}

	/**
	 * @return the Animation of this FlipView layout
	 */
	public Animation getInitialLayoutAnimation() {
		return this.initialLayoutAnimation;
	}

	public void setInitialLayoutAnimation(int animationResId) {
		setInitialLayoutAnimation(animationResId > 0 ?
				AnimationUtils.loadAnimation(getContext(), animationResId) : getScaleAnimation());
	}

	public void setInitialLayoutAnimation(Animation initialLayoutAnimation) {
		initialLayoutAnimation.setDuration(initialLayoutAnimationDuration);
		this.initialLayoutAnimation = initialLayoutAnimation;
	}

	/**
	 * @return the animation of the rear ImageView
	 */
	public Animation getCheckAnimation() {
		return checkAnimation;
	}

	public void setCheckAnimation(int checkedAnimationResId) {
		setCheckAnimation(AnimationUtils.loadAnimation(getContext(),
				checkedAnimationResId > 0 ? checkedAnimationResId : R.anim.scale_in));
	}

	public void setCheckAnimation(Animation acceptAnimation) {
		this.checkAnimation = acceptAnimation;
	}

	/* PERFORMING ANIMATION **********************************************/

	public boolean isCheckable() {
		return mCheckable;
	}

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
		flip(!this.mChecked, delay);
	}

	/**
	 * Set the state of this component to the given value, applying the
	 * corresponding rear animation, if possible.
	 *
	 * @param showRear <i>true</i> to show back image, <i>false</i> to show front image
	 * @param delay any custom delay
	 */
	final public void flip(final boolean showRear, long delay) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mChecked = showRear;
				setDisplayedChild(mChecked ? REAR_VIEW_INDEX : FRONT_VIEW_INDEX);
				if (mChecked && rearImage != null)
					rearImage.startAnimation(checkAnimation);
				mFlippingListener.onFlipped(FlipView.this, mChecked);
			}
		}, delay);
	}

	/**
	 * Show rear view immediately without flip animation.
	 *
	 * @param showRear <i>true</i> to show back image, <i>false</i> to show front image
	 */
	final public void flipSilently(boolean showRear) {
		Animation inAnimation = getInAnimation();
		Animation outAnimation = getOutAnimation();
		this.mChecked = showRear;
		setInAnimation(null);
		setOutAnimation(null);
		if (showRear) {
			setDisplayedChild(1);
			setInAnimation(inAnimation);
			setOutAnimation(outAnimation);
		}
	}

	public boolean isFlipped() {
		return mChecked;
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
		Log.d(TAG, "Setting FrontLayout "+layoutResId);
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
			Log.d(TAG, "FrontLayout is a ViewGroup");
			viewGroup = (ViewGroup) view;
		}
		//If any ImageView at first position is provided, reference to this front ImageView is saved.
		if (viewGroup.getChildAt(0) instanceof ImageView) {
			Log.d(TAG, "Found ImageView in the ViewGroup");
			frontImage = (ImageView) viewGroup.getChildAt(0);
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
		Log.d(TAG, "Adding RearLayout "+layoutResId);
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
		Log.d(TAG, "RearLayout index="+index);
		//If the View is another ViewGroup use it as new our rear View to flip
		if (view instanceof ViewGroup) {
			Log.d(TAG, "RearLayout is a ViewGroup");
			viewGroup = (ViewGroup) view;
			index = 0; //Override the index: use the first position to locate the ImageView in this ViewGroup
		}
		//If any ImageView is provided, reference to this rear ImageView is saved
		if (viewGroup.getChildAt(index) instanceof ImageView) {
			Log.d(TAG, "Found ImageView in the ViewGroup");
			rearImage = (ImageView) viewGroup.getChildAt(index);
		} else if (index > 2) rearImage = null; //Rollback in case multiple views are added (user must provide already the image in each layout added)
		//Watch out! User can add first the rear view and after the front view that must be
		// always at position 0. While all rear views start from index = 1.
		setView(view, getChildCount() == 0 ? REAR_VIEW_INDEX : getChildCount());
	}

	private void setView(View view, int index) {
		if (view == null)
			throw new IllegalArgumentException("The provided view must not be null");

		Log.d(TAG, "Setting view "+view.getId()+" at index " + index);

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
			this.frontImage.setPadding(mFrontImagePadding, mFrontImagePadding,
					mFrontImagePadding ,mFrontImagePadding);
			this.frontImage.setImageResource(imageResId);
		} catch (Resources.NotFoundException e) {
			Log.w(TAG, "No front resource image id " + imageResId + " found. No Image can be assigned");
		}
	}

	public void setRearImage(int imageResId) {
		if (this.rearImage == null) {
			Log.w(TAG, "ImageView not found in the child of the RearLayout. Image cannot be set!");
			return;
		}
		try {
			this.frontImage.setPadding(mRearImagePadding, mRearImagePadding,
					mRearImagePadding, mRearImagePadding);
			this.rearImage.setImageResource(imageResId);
		} catch (Resources.NotFoundException e) {
			Log.w(TAG, "No rear resource image id " + imageResId + " found. No Image can be assigned");
		}
	}

	public ShapeDrawable createOvalDrawable() {
		ShapeDrawable shapeDrawable = new ShapeDrawable();
		shapeDrawable.setShape(new OvalShape());
		return shapeDrawable;
	}

	public void setChildBackgroundDrawable(int index, int drawableResId, int color) {
		try {
			setChildBackgroundDrawable(index,
					getContext().getResources().getDrawable(drawableResId), color);
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Resource with id " + drawableResId + " could not be found. No drawable can be assigned");
		}
	}

	public void setChildBackgroundDrawable(int index, Drawable drawable, int color) {
		if (drawable == null) drawable = createOvalDrawable();
		if (getChildAt(index) != null) {
			drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			getChildAt(index).setBackground(drawable);
		}
	}

	public Drawable getChildBackgroundDrawable(int index) {
		return getChildAt(index).getBackground();
	}

}