package de.hadizadeh.positioning.roommodel.android;

import android.content.Context;
import android.graphics.*;
import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.roommodel.model.MapSegment;

/**
 * Map segments for android room model maps
 */
public class ViewerMapSegment extends MapSegment {
    /**
     * width of the outer stroke line
     */
    public static double strokeLineWidth = 1.0D;
    /**
     * size of a stoke with bold width
     */
    public static double boldStrokeLineWidth = 4.0D;
    /**
     * Selected background color
     */
    public static String selectedBackgroundColor = "#5Dffff00";
    /**
     * Line with of crosses
     */
    public static float crossStrokeLineWidth = 4.0f;
    /**
     * Line color of crosses
     */
    public static String crossStrokeLineColor = "#333333";
    /**
     * Width of marked stroke lines
     */
    public static float markedStrokeLineWidth = 4.0f;
    /**
     * Color of marked stroke lines
     */
    public static String markedStrokeLineColor = "#000000";
    /**
     * Background color of marked segments
     */
    public static String markedBackgroundColor = "#ff0000";
    private static boolean drawLines = true;
    private static Bitmap contentTexture;

    private boolean selected;
    private boolean mapped;
    private boolean marked;
    private MappingPoint mappingPoint;

    /**
     * Creates a map segment
     */
    public ViewerMapSegment() {
        super();
    }

    /**
     * Creates a new map segment out of an existing map segment and copies all data from the existing map segment
     *
     * @param copy existing map segment
     */
    public ViewerMapSegment(MapSegment copy) {
        super(copy);
    }

    /**
     * Initializes the data
     *
     * @param context activity context
     */
    public static void init(Context context) {
        if (contentTexture == null) {
            loadContentTexture(context);
        }
    }

    /**
     * renders the map segment
     *
     * @param graphic        graphic object to paint
     * @param originalRow    row number of the map segment
     * @param originalColumn column number of the map segment
     * @param renderRow      row position of the map segment (can be scrolled elsewhere)
     * @param renderColumn   column position of the map segment (can be scrolled elsewhere)
     */
    @Override
    public void render(Object graphic, int originalRow, int originalColumn, int renderRow, int renderColumn) {
        Canvas canvas = (Canvas) graphic;
        Paint strokePaint = new Paint();
        Paint fillPaint = new Paint();
        float x;
        float y;
        int w;
        int h;

        strokePaint.setStyle(Paint.Style.STROKE);
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setColor(Color.parseColor(strokeColor));

        float currentStrokeLineWidth = (float) strokeLineWidth;
//        if (content == null || content.getTitle() == null) {
//            currentStrokeLineWidth = (float) strokeLineWidth;
//        } else {
//            currentStrokeLineWidth = (float) boldStrokeLineWidth;
//            strokePaint.setColor(Color.parseColor(boldStrokeColor));
//        }

        if (material != null) {
            fillPaint.setColor(Color.parseColor(material.getColor()));
        } else {
            fillPaint.setColor(Color.WHITE);
        }

        x = (float) (renderColumn * size) + currentStrokeLineWidth / 2 - 1;
        y = (float) (renderRow * size) + currentStrokeLineWidth / 2 - 1;
        w = (int) (size - currentStrokeLineWidth + 1);
        h = (int) (size - currentStrokeLineWidth + 1);

        if (material != null && material.getTexture() != null) {
            canvas.drawBitmap(Bitmap.createScaledBitmap((Bitmap) material.getTexture(), w, h, false), x, y, fillPaint);
        } else {
            canvas.drawRect(x, y, x + w, y + h, fillPaint);
        }

        strokePaint.setStrokeWidth(currentStrokeLineWidth);
        if (currentStrokeLineWidth > 0) {
            canvas.drawRect(x, y, x + w, y + h, strokePaint);
        }

        if (content != null && content.getTitle() != null) {
            int centerAddition = (int) ((size - size / contentSizeFactor) / 2);
            canvas.drawBitmap(Bitmap.createScaledBitmap(contentTexture, (int) (w / contentSizeFactor), (int) (h / contentSizeFactor), false), x + centerAddition, y + centerAddition, fillPaint);
        }

        if (mapped && drawLines) {
            Paint crossPaint = new Paint();
            crossPaint.setColor(Color.parseColor(crossStrokeLineColor));
            crossPaint.setStrokeWidth(crossStrokeLineWidth);
            canvas.drawLine(x, y, x + w, y + h, crossPaint);
            canvas.drawLine(x, y + h, x + w, y, crossPaint);
        }

        if (selected) {
            Paint selectedPaint = new Paint();
            selectedPaint.setStyle(Paint.Style.FILL);
            selectedPaint.setColor(Color.parseColor(selectedBackgroundColor));
            canvas.drawRect(x, y, x + w, y + h, selectedPaint);
        }

        if (marked) {
            Paint markedPaint = new Paint();
            markedPaint.setStyle(Paint.Style.FILL);
            markedPaint.setColor(Color.parseColor(markedBackgroundColor));
            canvas.drawCircle(x + w / 2, y + w / 2, (float) ((size - currentStrokeLineWidth) / 4), markedPaint);

            markedPaint.setStyle(Paint.Style.STROKE);
            markedPaint.setColor(Color.parseColor(markedStrokeLineColor));
            markedPaint.setStrokeWidth(markedStrokeLineWidth);
            canvas.drawCircle(x + w / 2, y + w / 2, (float) ((size - currentStrokeLineWidth) / 4), markedPaint);
        }
    }

    /**
     * Selects the map segment
     */
    public void select() {
        selected = true;
    }

    /**
     * Unselects the map segment
     */
    public void unselect() {
        selected = false;
    }

    /**
     * Maps the segment
     *
     * @param mappingPoint mapping point
     */
    public void map(MappingPoint mappingPoint) {
        this.mapped = true;
        this.mappingPoint = mappingPoint;
    }

    /**
     * Unmaps the segment
     */
    public void unmap() {
        mapped = false;
    }

    /**
     * Marks the segment
     */
    public void mark() {
        marked = true;
    }

    /**
     * Demarcates the segment
     */
    public void demarcate() {
        marked = false;
    }

    /**
     * Checks if the segment is mapped
     *
     * @return true if it is mapped, else it is not
     */
    public boolean isMapped() {
        return mapped;
    }

    /**
     * Returns the connected mapping point
     *
     * @return mapping point
     */
    public MappingPoint getMappingPoint() {
        return mappingPoint;
    }

    /**
     * Disables all draw lines for map segments
     */
    public static void disableDrawLines() {
        ViewerMapSegment.drawLines = false;
        strokeLineWidth = 0;
    }

    /**
     * Loads the texture
     *
     * @param context activity context
     */
    private static void loadContentTexture(Context context) {
        contentTexture = BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(ViewerMaterial.TEXTURES_PREFIX + "content", "drawable", context.getPackageName()));
    }
}
