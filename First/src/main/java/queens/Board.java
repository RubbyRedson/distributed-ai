package queens;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 11/22/2016.
 */
public class Board implements Serializable {
    private int side;
    private List<List<Integer>> columns = new ArrayList<>();

    public Board(int side) {
        this.side = side;
        for (int i = 0; i < side; i++) {
            List<Integer> column = new ArrayList<>();
            for (int j = 0; j < side; j++) {
                column.add(0);
            }
            columns.add(column);
        }
    }

    public boolean checkSafe(int columnNumber, int rowNumber) {
        if (columnNumber >= side || rowNumber >= side) throw new IllegalArgumentException(columnNumber + " " + rowNumber + " " + side);

        return checkColumn(columnNumber) && checkRow(rowNumber) && checkDiagonal(columnNumber, rowNumber);
    }

    private boolean checkColumn(int columnNumber) {
        return !columns.get(columnNumber).contains(1);
    }

    private boolean checkRow(int rowNumber) {
        for (List<Integer> column : columns)
            if (column.get(rowNumber) == 1) return false;
        return true;
    }

    private boolean checkDiagonal(int columnNumber, int rowNumber) {
        for (int column = 0; column < side; column++) {
            for (int row = 0; row < side; row++) {
                if (get(column, row) == 1) {
                    int colDiff = columnNumber - column;
                    int rowDiff = rowNumber - row;
                    if (colDiff == rowDiff) return false;
                }
            }
        }
        return true;
    }

    public void occupy(int columnNumber, int rowNumber) {
        if (columnNumber >= side || rowNumber >= side) throw new IllegalArgumentException(columnNumber + " " + rowNumber + " " + side);
        columns.get(columnNumber).set(rowNumber, 1);
    }

    public void remove(int columnNumber, int rowNumber) {
        if (columnNumber >= side || rowNumber >= side) throw new IllegalArgumentException(columnNumber + " " + rowNumber + " " + side);
        columns.get(columnNumber).set(rowNumber, 0);
    }

    private int get(int columnNumber, int rowNumber) {
        if (columnNumber >= side || rowNumber >= side) throw new IllegalArgumentException(columnNumber + " " + rowNumber + " " + side);
        return columns.get(columnNumber).get(rowNumber);
    }

    public String printOut() {
        StringBuilder sb = new StringBuilder();
        for (int rowIndex = 0; rowIndex < side; rowIndex ++) {
            for (int columnIndex = 0; columnIndex < side; columnIndex++) {
                sb.append(get(columnIndex, rowIndex)).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public boolean checkFinished() {
        int queenCounter = 0;
        for (int column = 0; column < side; column++) {
            for (int row = 0; row < side; row++) {
                if (get(column, row) == 1) {
                    queenCounter++;
                    remove(column, row);
                    boolean safe = checkSafe(column, row);
                    occupy(column, row);
                    if (!safe) return false;
                }
            }
        }
        return queenCounter == side;
    }
}
