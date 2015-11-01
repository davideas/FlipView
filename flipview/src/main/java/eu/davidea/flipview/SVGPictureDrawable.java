package eu.davidea.flipview;

import android.graphics.drawable.PictureDrawable;

/**
 * This interface should be called in the AsyncTask to assign the
 * associated PictureDrawable at runtime.
 * Implement this interface to any custom Drawable.
 */
public interface SVGPictureDrawable {

	PictureDrawable getPictureDrawable();
	
	void setPictureDrawable(PictureDrawable drawable);
	
}