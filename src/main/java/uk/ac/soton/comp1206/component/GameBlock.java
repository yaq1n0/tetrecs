package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 * <p>
 * Extends Canvas and is responsible for drawing itself.
 * <p>
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * <p>
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };
    private static final Logger logger = LogManager.getLogger(GameBlock.class);
    private final GameBoard gameBoard;

    private final double width;

    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    private final double emptyTileOpacity = 0.2;
    private boolean isCenter = false;
    private boolean isHoverBlock = false;

    /**
     * Create a new single Game Block
     *
     * @param gameBoard the board this block belongs to
     * @param x         the column the block exists in
     * @param y         the row the block exists in
     * @param width     the width of the canvas to render
     * @param height    the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     *
     * @param observable what was updated
     * @param oldValue   the old value
     * @param newValue   the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if (value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }

        // if block is marked as center block, paint a center dot
        if (isCenter()) {
            paintCenter();
        }

        // is block is hovered, change color
        if (isHoverBlock()) {
            paintHover();
        }
    }

    /**
     * paint the center dot for pieceboard
     **/
    public void paintCenter() {
        GraphicsContext gc = getGraphicsContext2D();

        gc.setFill(Color.color(0.75, 0.75, 0.75, 0.75));
        gc.fillOval(width / 10.0, height / 10.0, width * 0.8, height * 0.8);
    }

    /**
     * paint the hovered on circle
     **/
    public void paintHover() {
        GraphicsContext gc = this.getGraphicsContext2D();

        gc.setFill(Color.color(1.0, 1.0, 1.0, 0.5));
        gc.fillOval(width / 10.0, height / 10.0, width * 0.8, height * 0.8);
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0, 0, width, height);

        //Fill
        gc.setFill(Color.color(0.0, 0.0, 0.0, emptyTileOpacity));
        gc.fillRect(0, 0, width, height);

        //Border
        gc.setStroke(Color.color(0.0, 0.0, 0.0, 0.5));
        gc.strokeRect(0.0, 0.0, this.width, this.height);
    }

    /**
     * Handles FadeOutTimer instance
     **/
    public void fadeOut() {
        AnimationTimer fadeOutTimer = new FadeOutTimer();
        fadeOutTimer.start();
    }

    /**
     * Paint this canvas with the given colour
     *
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0, 0, width, height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0, 0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, width, height);
    }

    /**
     * Get the column of this block
     *
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     *
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     *
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     *
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    public GameBoard getGameBoard() {
        return this.gameBoard;
    }

    public boolean isCenter() {
        return isCenter;
    }

    public void setCenter(boolean center) {
        isCenter = center;
        paint();
    }

    public boolean isHoverBlock() {
        return isHoverBlock;
    }

    public void setHoverBlock(boolean hover) {
        isHoverBlock = hover;
        paint();
    }

    /**
     * Implementing FadeOutTimer
     **/
    private class FadeOutTimer extends AnimationTimer {
        private double opacity = 1.0;
        private final double fadeFactor = 0.05; // increase to speed up animation

        @Override
        public void handle(long l) {
            // reduce opacity until opacity matches that of the empty tile
            if (opacity > emptyTileOpacity) {
                paintColor(Color.color(0, 0, 0, opacity));
                opacity -= fadeFactor;
            } else {
                // paint empty when opacity reaches the same as empty tile
                paintEmpty();
                stop();
            }
        }
    }

}
