package de.hadizadeh.positioning.roommodel.android;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.roommodel.model.MapSegment;

/**
 * Handles every fragment which has a room model map inside
 */
public class MapFragment extends Fragment implements TouchView.OnTouchViewChangeListener {
    protected ViewerMap viewerMap;
    protected Canvas canvas;
    protected TouchView canvasBox;
    protected Spinner floorSp;
    protected ArrayAdapter<String> floorSpAdapter;

    protected boolean loaded;
    protected int currentFloor;
    protected int touchedColumn;
    protected int touchedRow;

    protected int layoutIdFragmentMap;
    protected int layoutIdCanvasBox;
    protected int layoutIdFloorSp;
    protected String floorText;

    /**
     * Sets the needed layout ids
     *
     * @param layoutIdFragmentMap id of the fragment container
     * @param layoutIdCanvasBox   canvas id
     * @param layoutIdFloorSp     id of the spinner for changing the floor (can e 0 if there is no spinner)
     */
    public void setLayoutIds(int layoutIdFragmentMap, int layoutIdCanvasBox, int layoutIdFloorSp) {
        this.layoutIdFragmentMap = layoutIdFragmentMap;
        this.layoutIdCanvasBox = layoutIdCanvasBox;
        this.layoutIdFloorSp = layoutIdFloorSp;
    }

    /**
     * Sets the map data and the floor text
     *
     * @param viewerMap map
     * @param floorText floor text
     */
    public void setData(ViewerMap viewerMap, String floorText) {
        this.viewerMap = viewerMap;
        this.floorText = floorText;
    }

    /**
     * Creates the map fragment
     *
     * @param inflater           layout infalter
     * @param container          container
     * @param savedInstanceState saved instance
     * @return created view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        View view = inflater.inflate(layoutIdFragmentMap, container, false);
        MapSegment.setMaxSize(500);
        ViewerMapSegment.init(getActivity());

        canvasBox = (TouchView) view.findViewById(layoutIdCanvasBox);
        floorSp = (Spinner) view.findViewById(layoutIdFloorSp);

        if (floorSp != null) {
            floorSpAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, android.R.id.text1);
            floorSpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floorSp.setAdapter(floorSpAdapter);
            floorSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long id) {
                    currentFloor = index;
                    viewerMap.setCurrentFloor(currentFloor);
                    render();
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        }

        loaded = false;

        if (savedInstanceState != null) {
            canvasBox.setScale(savedInstanceState.getFloat("extra_scale"));
            canvasBox.setX(savedInstanceState.getFloat("extra_scroll_x"));
            canvasBox.setY(savedInstanceState.getFloat("extra_scroll_y"));
            touchedColumn = savedInstanceState.getInt("extra_touched_column");
            touchedRow = savedInstanceState.getInt("extra_touched_row");
            currentFloor = savedInstanceState.getInt("extra_floor");
        } else {
            canvasBox.setScale(100.0f);
            touchedColumn = -1;
            touchedRow = -1;
        }

        ViewTreeObserver vto = canvasBox.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!loaded) {
                    loaded = true;
                    initCanvas();
                }
            }
        });

        return view;
    }

    /**
     * Saves the current state of the view (floor, scroll and zoom state)
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putFloat("extra_scale", canvasBox.getScale());
        outState.putFloat("extra_scroll_x", canvasBox.getX());
        outState.putFloat("extra_scroll_y", canvasBox.getY());
        outState.putInt("extra_touched_column", touchedColumn);
        outState.putInt("extra_touched_row", touchedRow);
        outState.putInt("extra_floor", currentFloor);
        super.onSaveInstanceState(outState);
    }

    /**
     * Returns the current selected map segment coordinates
     *
     * @return selected mapping point
     */
    protected MappingPoint getSelectedMappingPoint() {
        return new MappingPoint(viewerMap.getLastSelectedColumn(), viewerMap.getLastSelectedRow(), viewerMap.getCurrentFloor() * viewerMap.getFloorHeight());
    }

    /**
     * Initializes the canvas
     */
    protected void initCanvas() {
        canvasBox.setListener(this);
        Bitmap canvasBg = Bitmap.createBitmap(canvasBox.getWidth(), canvasBox.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(canvasBg);
        canvasBox.setBackground(new BitmapDrawable(getResources(), canvasBg));
        loadCanvas();
    }

    /**
     * Loads the canvas
     */
    protected void loadCanvas() {
        if (floorSp != null) {
            floorSpAdapter.clear();
            for (int i = 1; i <= viewerMap.getFloors(); i++) {
                floorSpAdapter.add(i + ". " + floorText);
            }
            floorSpAdapter.notifyDataSetChanged();
            floorSp.setSelection(currentFloor);
        }
        viewerMap.setCurrentFloor(currentFloor);
        viewerMap.render(canvas, 0, 0, 0, 0, null, null, null, null, getActivity());
        onTouchViewChange(canvasBox.getX(), canvasBox.getY(), canvasBox.getScale());
        if (touchedColumn >= 0 && touchedRow >= 0) {
            onTouch((float) ((touchedColumn - viewerMap.getStartColumn()) * MapSegment.getSize()), (float) ((touchedRow - viewerMap.getStartRow()) * MapSegment.getSize()));
        } else {
            render();
        }
        canvasBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    /**
     * Selects a map segment when it has been touched
     *
     * @param x     x coordinate
     * @param y     y coordinate
     * @param scale scale value
     */
    @Override
    public void onTouchViewChange(float x, float y, float scale) {
        MapSegment.setSize(scale);
        int visibleRows = calculateVisibleRows();
        int visibleColumns = calculateVisibleColumns();
        x = Math.max(0, Math.min(x, viewerMap.getColumns() - visibleColumns));
        y = Math.max(0, Math.min(y, viewerMap.getRows() - visibleRows));

        canvasBox.setMaxX(viewerMap.getColumns() - visibleColumns);
        canvasBox.setMaxY(viewerMap.getRows() - visibleRows);
        if (viewerMap != null) {
            viewerMap.render((int) y, (int) x, visibleRows, visibleColumns);
            canvasBox.invalidate();
        }
    }

    /**
     * Calculates the amount of visible rows
     *
     * @return amount of visible rows
     */
    protected int calculateVisibleRows() {
        return (int) (canvasBox.getHeight() / MapSegment.getSize());
    }

    /**
     * Calculates the amount of visible columns
     *
     * @return amount of visible columns
     */
    protected int calculateVisibleColumns() {
        return (int) (canvasBox.getWidth() / MapSegment.getSize());
    }

    /**
     * Handles touch events
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    @Override
    public void onTouch(float x, float y) {
        touchedColumn = viewerMap.calculateColumn(x) + viewerMap.getStartColumn();
        touchedRow = viewerMap.calculateRow(y) + viewerMap.getStartRow();
    }

    /**
     * Unselects all segments of the current map
     */
    public void unselectMap() {
        onTouch(-1, -1);
    }

    /**
     * Renders the map
     */
    public void render() {
        viewerMap.render();
        canvasBox.invalidate();
    }

}
