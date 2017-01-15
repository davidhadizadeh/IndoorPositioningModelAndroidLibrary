package de.hadizadeh.positioning.roommodel.android;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for the main menu
 */
public class MenuListAdapter extends ArrayAdapter<MenuItem> {
    private Context context;
    private int layoutResourceId;
    private MenuItem[] data;
    private int tvId;
    private int ivId;

    /**
     * Creates the menu list adapter
     *
     * @param context          activity context
     * @param layoutResourceId id of the layout
     * @param data             data
     * @param tvId             id of the textview
     * @param ivId             id of the image view
     */
    public MenuListAdapter(Context context, int layoutResourceId, MenuItem[] data, int tvId, int ivId) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
        this.tvId = tvId;
        this.ivId = ivId;
    }

    /**
     * Returns the current view
     *
     * @param position    selected position
     * @param convertView convert view
     * @param parent      parent view
     * @return current view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }
        MenuItem item = getItem(position);
        ImageView itemIv = (ImageView) convertView.findViewById(ivId);
        TextView itemTv = (TextView) convertView.findViewById(tvId);
        itemIv.setImageBitmap(item.getImage());
        itemTv.setText(item.getTitle());
        return convertView;
    }
}
