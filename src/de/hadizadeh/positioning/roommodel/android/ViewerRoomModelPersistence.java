package de.hadizadeh.positioning.roommodel.android;


import android.content.res.Resources;
import de.hadizadeh.positioning.content.exceptions.ContentPersistenceException;
import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.roommodel.ContentController;
import de.hadizadeh.positioning.roommodel.Map;
import de.hadizadeh.positioning.roommodel.RoomModelPersistence;
import de.hadizadeh.positioning.roommodel.model.ContentElement;

import java.io.IOException;
import java.util.List;

/**
 * Manages the persistence for the room models on android
 */
public class ViewerRoomModelPersistence extends RoomModelPersistence {
    private ContentController contentController;
    private List<MappingPoint> mappingPoints;

    /**
     * Creates a persistence manager
     *
     * @param contentController content controller
     * @param mappingPoints     all mapping points
     */
    public ViewerRoomModelPersistence(ContentController contentController, List<MappingPoint> mappingPoints) {
        this.contentController = contentController;
        this.mappingPoints = mappingPoints;
    }

    /**
     * Saves a room model map to a file
     *
     * @param filename filename
     * @param map      room model map
     * @throws IOException                 if the file could not be created
     * @throws ContentPersistenceException if the content of the map is incorrect
     */
    @Override
    public void save(String filename, Map map) throws IOException, ContentPersistenceException {
        throw new Resources.NotFoundException();
    }

    /**
     * Removes all connected positions from file
     */
    @Override
    protected void removeAllPositions() {
    }

    /**
     * Add a position to a content (connect)
     *
     * @param content      content
     * @param mappingPoint position (coordinates)
     */
    @Override
    protected void addPosition(ContentElement content, MappingPoint mappingPoint) {
    }

    /**
     * Loads a content of a position (coordinates)
     *
     * @param mappingPoint position (coordinates)
     * @return content
     */
    @Override
    protected ContentElement getContent(MappingPoint mappingPoint) {
        return contentController.getContent(mappingPoint);
    }

    /**
     * Creates a room model map of a defined size
     *
     * @param rows        amount of rows
     * @param columns     amount of columns
     * @param floors      amount of floors
     * @param floorHeight height of each floor
     * @return room model map
     */
    @Override
    protected Map createMap(int rows, int columns, int floors, int floorHeight) {
        return new ViewerMap(rows, columns, floors, floorHeight, mappingPoints);
    }
}
