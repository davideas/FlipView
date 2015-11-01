# Flip View

###### Gmail like View - Master branch: v1 of 2015.11.01

#### Concept
FlipView is a ViewGroup (FrameLayout) that displays 2 views/layouts by flipping
the front one in favor of the back one, and vice versa. Optionally more views can be
displayed in series one after another since it extends {@link android.widget.ViewAnimator}.

Usage is very simple. You just need to add this View to any layout (like you would
do with any other View) and you are good to go. You can customize the behaviours
by assigning values to the optional properties in the layout or programmatically.

Please, refer to those attributes documentation for more details.

#### Main functionalities


# Screenshots & Video


#Setup
Ultra simple:
Import the library into your project using Gradle.
```
compile project (':FlipView_Library')
```
#### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!

# Change Log
###### v1 - 2015.11.01 (Initial release)
- Initial LayoutAnimation & Initial LayoutAnimationDuration
  Reset & stop LayoutAnimationDelay
- Custom In&Out Animation
  Rear ImageAnimation & Rear ImageAnimationDuration
- MainAnimationDuration
- Flip & flipSilently
- Custom FrontLayout & several custom RearLayout
- Create BitmapFrom, PictureDrawable & ImageBitmap
- Custom FrontImage, custom FrontText &, custom RearImage
- Custom Child BackgroundDrawable & color
- Create inner OvalDrawable, ScaleAnimation
- Some animation and Drawables already included into he project, so you can start to test it
- OnFlippingListener, inner onClick
- Example Activity

###### Old releases
See [releases](https://github.com/davideas/FlexibleAdapter/releases) for old versions.

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
