package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

public class PieceBoard extends GameBoard {

    public PieceBoard(double width, double height) {
        super(3, 3, width, height);
    }

    /**
     * display any 3x3 gamepiece instance
     **/
    public void displayPiece(GamePiece gamePiece) {
        int[][] blocks = gamePiece.getBlocks();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (blocks[col][row] > 0) {
                    grid.set(col, row, gamePiece.getValue());
                }
            }
        }
    }

    /**
     * clear the pieceboard
     **/
    public void clear() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                grid.set(col, row, 0);
            }
        }
    }

    /**
     * drawing the center dot
     **/
    public void drawCenterDot(boolean bool) {
        double midX = Math.ceil((double) this.getRows() / 2.0) - 1.0;
        double midY = Math.ceil((double) this.getCols() / 2.0) - 1.0;
        this.getGameBlocks()[(int) midX][(int) midY].setCenter(bool);
    }
}
