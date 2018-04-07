[![Download](https://api.bintray.com/packages/davideas/maven/flipview/images/download.svg) ](https://bintray.com/davideas/maven/flipview/_latestVersion)
[![API](https://img.shields.io/badge/API-14%2B-green.svg?style=flat)](https://android-arsenal.com/api?level=14)
[![Licence](https://img.shields.io/badge/Licence-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Methods and Size](https://img.shields.io/badge/Methods%20and%20Size-core:%20190%20%7C%2034%20KB-e91e63.svg)](http://www.methodscount.com/?lib=eu.davidea%3Aflipview%3A1.1.3)

# FlipView

###### GMail-like View & beyond - v1.1.3 built on 2017.03.07

#### Concept
FlipView is a ViewGroup (FrameLayout) that is designed to display 2 views/layouts by flipping
the front one in favor of the back one, and vice versa. Optionally more views can be
displayed in series one after another or can cycle with a interval.

Usage is very simple. You just need to add this View to any layout and you customize the behaviours
by assigning values to the optional properties in the layout or programmatically.
Please, refer to those attributes documentation for more details.

Not less, FlipView extends `android.widget.ViewFlipper` that extends `android.widget.ViewAnimator`,
which means you can call all public functions of these two Android views.

#### Main features
- Visible during design time ;-)
- Custom In/Out animation + entry animation + rear ImageView animation
- Custom layout, ImageView & TextView for front layout.
- Custom layout, ImageView for rear layout.
- Custom background Drawable & color.
- AutoStart cycle animation with custom interval.
- PictureDrawable for SVG resources.

# Showcase
![Showcase1](/showcase/showcase1.gif) ![Showcase2](/showcase/showcase2.gif)

# Setup
Import the library into your project using JCenter
```
dependencies {
	implementation 'eu.davidea:flipview:1.1.3'
}
```
#### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!

# Usage
Supported attributes with _default_ values:
``` xml
<eu.davidea.flipview.FlipView
	xmlns:app="http://schemas.android.com/apk/res-auto"
	android usual attrs
	(see below).../>
```
|**ViewAnimator**||
|:---|:---|
| `android:inAnimation="@anim/grow_from_middle_x_axis"` | Identifier for the animation to use when a view is shown.
| `android:outAnimation="@anim/shrink_to_middle_x_axis"` | Identifier for the animation to use when a view is hidden.
| `android:animateFirstView="true"` | Defines whether to animate the current View when the ViewAnimation is first displayed.

|**ViewFlipper**||
|:---|:---|
| `android:autoStart="false"` | When true, automatically start animating.
| `android:flipInterval="3000"` | Time before next animation.

| **FlipView** ||
|:---|:---|
| `android:clickable="false"` | **(!!)** Set this if you want view reacts to the tap and animate it.
| `app:checked="false"` | Whether or not this component is showing rear layout at startup.
| `app:animateDesignLayoutOnly="false"` | true, animate front and rear layouts from settings + child views; false, exclude all layouts and animate _only_ child views from design layout if any. (This attribute cannot be changed at runtime).
| `app:animationDuration="100"` | Set the main animation duration.
| `app:anticipateInAnimationTime="0"` | Anticipate the beginning of InAnimation, this time is already subtracted from the main duration (new delay is: main duration - anticipation time).
| `app:enableInitialAnimation="false"` | Whether or not the initial animation should start at the beginning.
| `app:initialLayoutAnimation="@anim/scale_up"` | Starting animation.
| `app:initialLayoutAnimationDuration="250"` | Starting animation duration.
| `app:frontLayout="@layout/flipview_front"` | Front view layout resource (for checked state -> false).
| `app:frontBackground="<OvalShape Drawable generated programmatically>"` | Front drawable resource (for checked state -> false).
| `app:frontBackgroundColor="<Color.GRAY set programmatically>"` | Front view color resource (for checked state -> false).
| `app:frontImage="@null"` | Front image resource (for checked state -> false).
| `app:frontImagePadding="0dp"` | Front image padding.
| `app:rearLayout="@layout/flipview_rear"` | Rear view layout resource (for checked state -> true).
| `app:rearBackground="<OvalShape Drawable generated programmatically>"` | Rear drawable resource (for checked state -> true).
| `app:rearBackgroundColor="Color.GRAY set programmatically"` | Rear view color resource (for checked state -> true).
| `app:rearImage="@drawable/ic_action_done"` | Rear accept image resource.
| `app:rearImagePadding="0dp"` | Rear image padding.
| `app:animateRearImage="true"` | Whether or not the rear image should animate.
| `app:rearImageAnimation="@anim/scale_up"` | Rear image animation.
| `app:rearImageAnimationDuration="150"` | Rear image animation duration.
| `app:rearImageAnimationDelay="animationDuration"` | Rear image animation delay (depends the animation/duration it can be smart setting a specific delay. For GMail effect set this to 0).

|**Non changeable values** (in ms)||
|:---|:---|
| `DEFAULT_INITIAL_DELAY = 500` | This gives enough time to the activity to load all tree views before starting cascade initial animation.
| `SCALE_STEP_DELAY = 35` | This gives an acceptable nice loading effect.
| `STOP_LAYOUT_ANIMATION_DELAY = 1500` | This gives enough time to perform all entry animations but to stop further animations when screen is fully rendered.

# Limitations
- Transparency has a little glitch when used with elevation, you could see shadow In the shape: more transparent the color is more visible the shadow is.
- Using layer type _software_ on the entire layout it removes the shadow/elevation.
- Stroke and background color on custom Drawable should be preset by the user: too complex to determine the type of the Drawable used in order to change its color.

# Change Log
###### Latest release
[v1.1.3](https://github.com/davideas/FlipView/releases) - 2017.03.07

###### Old releases
[v1.1.2](https://github.com/davideas/FlipView/releases/tag/1.1.2) - 2016.11.30 |
[v1.1.1](https://github.com/davideas/FlipView/releases/tag/1.1.1) - 2016.04.07<br/>
[v1.1.0](https://github.com/davideas/FlipView/releases/tag/1.1.0) - 2015.11.05 |
[v1.0.0](https://github.com/davideas/FlipView/releases/tag/1.0.0) - 2015.11.01

# License

    Copyright 2015-2018 Davide Steduto

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
