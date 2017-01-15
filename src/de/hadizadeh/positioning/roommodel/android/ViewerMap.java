package de.hadizadeh.positioning.roommodel.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.TextView;
import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.roommodel.Map;
import de.hadizadeh.positioning.roommodel.model.MapSegment;

import java.util.List;

/**
 * Map for room models on android
 */
public class ViewerMap extends Map {
    private Canvas canvas;
    private int startRow;
    private int startColumn;
    private int visibleRows;
    private int visibleColumns;
    private TextView currentXLb;
    private TextView currentYLb;
    private TextView currentMaterialLb;
    private TextView currentContentLb;
    private Context context;

    private int lastSelectedRow;
    private int lastSelectedColumn;
    private MappingPoint lastMarkedPosition;

    /**
     * Creates the viewer map
     *
     * @param rows          amount of rows
     * @param columns       amount of colums
     * @param floors        amount of floors
     * @param floorHeight   height of each floor
     * @param mappingPoints mapping points
     */
    public ViewerMap(int rows, int columns, int floors, int floorHeight, List<MappingPoint> mappingPoints) {
        super(rows, columns, floors, floorHeight);
        for (MappingPoint mappingPoint : mappingPoints) {
            if (mappingPoint.getZ() / floorHeight < floors && mappingPoint.getY() < rows && mappingPoint.getX() < columns) {
                map(mappingPoint);
            }
        }
    }

    /**
     * Returns the canvas for drawing
     *
     * @return canvas
     */
    @Override
    public Object getCanvas() {
        return canvas;
    }

    /**
     * Renders the map
     */
    @Override
    public void render() {
        render(canvas, startRow, startColumn, visibleRows, visibleColumns, currentXLb, currentYLb, currentMaterialLb, currentContentLb, context);
    }

    /**
     * Renders the map for an explicit area
     *
     * @param startRow       Starting row for rendering
     * @param startColumn    Starting column for rendering
     * @param visibleRows    Amount of rows to render
     * @param visibleColumns Amount of columns to render
     */
    @Override
    public void render(int startRow, int startColumn, int visibleRows, int visibleColumns) {
        render(canvas, startRow, startColumn, visibleRows, visibleColumns, currentXLb, currentYLb, currentMaterialLb, currentContentLb, context);
    }

    /**
     * Renders the map for an explicit area
     *
     * @param mapCanvas         canvas
     * @param startRow          Starting row for rendering
     * @param startColumn       Starting column for rendering
     * @param visibleRows       Amount of rows to render
     * @param visibleColumns    Amount of columns to render
     * @param currentXLb        Label for x values (can be null if not needed)
     * @param currentYLb        label for y values (can be null if not needed)
     * @param currentMaterialLb label for material value (can be null if not needed)
     * @param currentContentLb  label for content value (can be null if not needed)
     * @param context           activity context
     */
    @Override
    public void render(Object mapCanvas, int startRow, int startColumn, int visibleRows, int visibleColumns, Object currentXLb, Object currentYLb, Object currentMaterialLb, Object currentContentLb, Object context) {
        this.canvas = (Canvas) mapCanvas;
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.visibleRows = visibleRows;
        this.visibleColumns = visibleColumns;
        this.currentXLb = (TextView) currentXLb;
        this.currentYLb = (TextView) currentYLb;
        this.currentMaterialLb = (TextView) currentMaterialLb;
        this.currentContentLb = (TextView) currentContentLb;
        this.context = (Context) context;
        canvas.drawColor(Color.WHITE);

        render(canvas, startRow, startColumn, visibleRows, visibleColumns);
    }

    /**
     * Returns the starting row of rendering
     *
     * @return row
     */
    public int getStartRow() {
        return startRow;
    }

    /**
     * Returns the starting column for rendering
     *
     * @return starting column
     */
    public int getStartColumn() {
        return startColumn;
    }

    /**
     * Creates a map segment
     *
     * @return map segment
     */
    @Override
    public MapSegment createMapSegment() {
        return new ViewerMapSegment();
    }

    /**
     * Copies a map segment
     *
     * @param mapSegment segment to copy
     * @return map segment
     */
    @Override
    public MapSegment copyMapSegment(MapSegment mapSegment) {
        return new ViewerMapSegment(mapSegment);
    }

    /**
     * Handles on touch events for the map
     *
     * @param x touched x
     * @param y touched y
     * @return selected map segment
     */
    public ViewerMapSegment onTouch(float x, float y) {
        ((ViewerMapSegment) mapSegments[currentFloor][lastSelectedRow][lastSelectedColumn]).unselect();
        if (x >= 0 && y >= 0) {
            int row = startRow + calculateRow(y);
            int column = startColumn + calculateColumn(x);
            if (row >= 0 && column >= 0 && row < rows && column < columns) {
                ((ViewerMapSegment) mapSegments[currentFloor][row][column]).select();
                lastSelectedRow = row;
                lastSelectedColumn = column;
            }
        }
        return (ViewerMapSegment) mapSegments[currentFloor][lastSelectedRow][lastSelectedColumn];
    }

    /**
     * Returns the last selected row
     *
     * @return last selected row
     */
    public int getLastSelectedRow() {
        return lastSelectedRow;
    }

    /**
     * Returns the last selected column
     *
     * @return last selected column
     */
    public int getLastSelectedColumn() {
        return lastSelectedColumn;
    }

    /**
     * Maps the the signal data for a mapping point (marks them)
     *
     * @param mappingPoint mapping point
     */
    public void map(MappingPoint mappingPoint) {
        ViewerMapSegment viewerMapSegment = (ViewerMapSegment) mapSegments[mappingPoint.getZ() / floorHeight][mappingPoint.getY()][mappingPoint.getX()];
        viewerMapSegment.map(mappingPoint);
    }

    /**
     * Unmmaps a point
     *
     * @param mappingPoint mapping point
     */
    public void unmap(MappingPoint mappingPoint) {
        ViewerMapSegment viewerMapSegment = (ViewerMapSegment) mapSegments[mappingPoint.getZ() / floorHeight][mappingPoint.getY()][mappingPoint.getX()];
        viewerMapSegment.unmap();
    }

    /**
     * Unmaps all mapping points
     */
    public void unmapAll() {
        for (int floor = 0; floor < floors; ++floor) {
            for (int row = 0; row < rows; ++row) {
                for (int column = 0; column < columns; ++column) {
                    ((ViewerMapSegment) this.mapSegments[floor][row][column]).unmap();
                }
            }
        }
    }

    /**
     * Marks a point
     *
     * @param mappingPoint mapping point
     */
    public void mark(MappingPoint mappingPoint) {
        demarcate();
        ViewerMapSegment viewerMapSegment = (ViewerMapSegment) mapSegments[mappingPoint.getZ() / floorHeight][mappingPoint.getY()][mappingPoint.getX()];
        viewerMapSegment.mark();
        lastMarkedPosition = mappingPoint;
    }

    /**
     * Demarcates the last marked point
     */
    public void demarcate() {
        if (lastMarkedPosition != null) {
            ViewerMapSegment viewerMapSegment = (ViewerMapSegment) mapSegments[lastMarkedPosition.getZ() / floorHeight][lastMarkedPosition.getY()][lastMarkedPosition.getX()];
            viewerMapSegment.demarcate();
        }
    }
}
