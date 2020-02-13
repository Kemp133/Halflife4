import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class HalfLifeView extends Group {
    public final static double CELL_WIDTH = 20.0;

    @FXML private int rowCount;
    @FXML private int columnCount;

    /**
     * for map item to be added &
     * Initializes the values of the image instance variables from files
     */
    public HalfLifeView() {

    }

    /**
     * Constructs an empty grid of ImageViews
     */
    private void initializeGrid() {

    }

    /** Updates the view to reflect the state of the model
     * should be the actual part to refresh game
     * waiting fo updated
     *
     * @param
     */
    public void update() {

    }

    public int getRowCount() {
        return this.rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
        this.initializeGrid();
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
        this.initializeGrid();
    }
}

