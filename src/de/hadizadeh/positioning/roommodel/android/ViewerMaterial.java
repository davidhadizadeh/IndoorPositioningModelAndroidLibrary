package de.hadizadeh.positioning.roommodel.android;

import android.content.Context;
import android.graphics.BitmapFactory;
import de.hadizadeh.positioning.roommodel.model.Material;

/**
 * Manages the materials of map segments
 */
public class ViewerMaterial extends Material {
    /**
     * File prefix for textures
     */
    public static final String TEXTURES_PREFIX = "texture_";

    private Context context;

    /**
     * Creates a material
     *
     * @param context          activity context
     * @param name             name of the material
     * @param presentationName display name of the material, will be shown to the user
     * @param color            default background color of the material
     * @param textColor        text color of the material
     */
    public ViewerMaterial(Context context, String name, String presentationName, String color, String textColor) {
        super(name);
        this.context = context;
        this.presentationName = presentationName;
        this.color = color;
        this.textColor = textColor;
        this.loadTexture();
    }

    /**
     * Loads the texture of the material
     */
    @Override
    protected void loadTexture() {
        texture = BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(TEXTURES_PREFIX + name, "drawable", context.getPackageName()));
    }
}
