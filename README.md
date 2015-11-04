[![Download](https://api.bintray.com/packages/davideas/maven/flipview/images/download.svg) ](https://bintray.com/davideas/maven/flipview/_latestVersion)

# FlipView

###### Gmail like View & beyond - Master branch: v1.1 of 2015.11.04

#### Concept
FlipView is a ViewGroup (FrameLayout) that is designed to display 2 views/layouts by flipping
the front one in favor of the back one, and vice versa. Optionally more views can be
displayed in series one after another or can cycle with a interval.

Usage is very simple. You just need to add this View to any layout and you customize the behaviours
by assigning values to the optional properties in the layout or programmatically.
Please, refer to those attributes documentation for more details.

Not less, FlipView extends `android.widget.ViewFlipper` that extends `android.widget.ViewAnimator`,
which means you can call all public functions of these two Android views.

#### Main functionalities
- Visible during design time ;-)
- Custom In/Out animation + Entry animation + Rear ImageView animation
- Custom layout, ImageView & TextView for front layout.
- Custom layout, ImageView for rear layout.
- Custom background Drawable & color.
- Autostart cycle animation with custom interval.
- PictureDrawable for SVG resources.

# Showcase
![Showcase1](/showcase/showcase1.gif) ![Showcase2](/showcase/showcase2.gif)

#Setup
Import the library into your project using Gradle with JCenter
```
dependencies {
	compile 'eu.davidea:flipview:1.1.0'
}
```
Using bintray.com
```
repositories {
	maven { url "http://dl.bintray.com/davideas/maven" }
}
dependencies {
	compile 'eu.davidea:flipview:1.1.0@aar'
}
```
#### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!

#Usage
Supported attributes with _default_ values:
``` xml
<eu.davidea.flipview.FlipView
	xmlns:app="http://schemas.android.com/apk/res-auto"
	android usual attrs
	(see below).../>
```
**ViewAnimator**
- `android:inAnimation="@anim/grow_from_middle_x_axis"` - Identifier for the animation to use when a view is shown.
- `android:outAnimation="@anim/shrink_to_middle_x_axis"` - Identifier for the animation to use when a view is hidden.
- `android:animateFirstView="true"` - Defines whether to animate the current View when the ViewAnimation is first displayed.

**ViewFlipper**
- `android:autoStart="false"` - When true, automatically start animating.
- `android:flipInterval="3000"` - Time before next animation.

**FlipView**
- `android:clickable="false"` - (!!) Set this if you want view react to the taps and animate it.
- `app:checked="false"` - Whether or not this component is flipped at startup
- `app:animateDesignLayoutOnly="false"` - false, if main animation should be applied only to the child views from design layout; true, to use inner layout.
- `app:animationDuration="100"` - Set the main animation duration.
- `app:anticipateInAnimationTime="0"` - Anticipate the beginning of the InAnimation.
- `app:enableInitialAnimation="false"` - Whether or not the initial animation should start at the beginning.
- `app:initialLayoutAnimation="@anim/scale_up"` - Starting animation.
- `app:initialLayoutAnimationDuration="250"` - Starting animation duration.
- `app:animateRearImage="true"` - Use default rear image animation.
- `app:frontLayout="@layout/flipview_front"` - Front view layout resource (for checked state -> false).
- `app:frontBackground="<OvalShape Drawable generated programmatically>"` - Front drawable resource (for checked state -> false).
- `app:frontBackgroundColor="<Color.GRAY set programmatically>"` - Front view color resource (for checked state -> false).
- `app:frontImage="@null"` - Front image resource (for checked state -> false).
- `app:frontImagePadding="0dp"` - Front image padding.
- `app:rearLayout="@layout/flipview_rear"` - Rear view layout resource (for checked state -> true).
- `app:rearBackground="<OvalShape Drawable generated programmatically>"` - Rear drawable resource (for checked state -> true).
- `app:reartBackgroundColor="Color.GRAY set programmatically"` - Rear view color resource (for checked state -> true).
- `app:rearImage="@drawable/ic_action_done"` - Rear accept image resource.
- `app:rearImagePadding="0dp"` - Rear image padding.
- `app:rearImageAnimation="@anim/scale_up"` - Rear image animation.
- `app:rearImageAnimationDuration="150"` - Rear image animation duration.
- `app:rearImageAnimationDelay="duration"` - Rear image animation delay (depends the animation/duration it can be smart setting a custom delay).

**Not changeable values** (in ms)
- `DEFAULT_INITIAL_DELAY = 500` - This gives time to the activity to load.
- `SCALE_STEP_DELAY = 35` - This gives an acceptable nice loading effect.
- `STOP_LAYOUT_ANIMATION_DELAY = 1500` - This gives the time to perform all entry animations but to stop further animations when screen is fully rendered.

# Limitations
- Transparency has an [issue](https://code.google.com/p/android/issues/detail?id=78248) constraint for the shadow, you loose the elevation value.
- Stroke and background color on custom Drawable should be preset by the user. 

# Change Log
###### v1.1.0 - 2015.11.04
- New attribute `rearImageAnimationDelay`.
- Fixed bugs #4 #5 #6.
- Overridden `showNext()` method from `ViewAnimator`. Now it performs the flip accordingly with the existing
  settings and register its state.
- Since the FlipView uses shapes to define its border and shadows, one can use `frontBackground` & `rearBackground`
  which always override any custom color set with `frontBackgroundColor` & `rearBackgroundColor`:
  To give a color (with a transparency) and a custom shape (with stroke) you should assign a Drawable resource with the
  desired shape and color (#2 #3) or to assign this resource at design time to the `android:background` of the custom layout.
- Desired color with a transparency of the inner drawable (OvalShape) can be assigned at design time & runtime (#2 #3) with a limitation.
- New ShapeDrawables static methods (Oval, Arc, RoundRect).
- Adapted example to show Autostart and how 2 layouts can be animated.

###### v1.0.0 - 2015.11.01 (Initial release)
- Initial LayoutAnimation & Initial LayoutAnimationDuration.
  Reset & stop LayoutAnimationDelay.
- Custom In&Out Animation.
  Rear ImageAnimation & Rear ImageAnimationDuration.
- MainAnimationDuration.
- Flip & flipSilently.
- Custom FrontLayout & several custom RearLayout.
- Create BitmapFrom, PictureDrawable & ImageBitmap.
- Custom FrontImage, custom FrontText &, custom RearImage.
- Custom Child BackgroundDrawable & color.
- Create inner OvalDrawable, ScaleAnimation.
- Some animation and Drawables already included into he project, so you can start to test it.
- OnFlippingListener, inner onClick.
- SVGPictureDrawable interface for asynchronous loading the SVG resource.
- Example Activity.

###### Old releases
See [releases](https://github.com/davideas/FlipView/releases) for old versions.

# License

    Copyright 2015 Davide Steduto

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
