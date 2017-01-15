package de.hadizadeh.positioning.roommodel.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Manages menu items of the main activity
 */
public class MenuItem {

    private String title;
    private Bitmap image;

    /**
     * Creates a menu item
     *
     * @param title title text
     * @param image image
     */
    public MenuItem(String title, Bitmap image) {
        this.title = title;
        this.image = image;
    }

    /**
     * Creates a menu item
     *
     * @param context    activity context
     * @param title      title text
     * @param imageResId id of the image ressource
     */
    public MenuItem(Context context, String title, int imageResId) {
        this(title, BitmapFactory.decodeResource(context.getResources(), imageResId));
    }

    /**
     * Returns the title text
     *
     * @return title text
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title text
     *
     * @param title title text
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the image
     *
     * @return image
     */
    public Bitmap getImage() {
        return image;
    }

    /**
     * Sets the image
     *
     * @param image images
     */
    public void setImage(Bitmap image) {
        this.image = image;
    }
}
