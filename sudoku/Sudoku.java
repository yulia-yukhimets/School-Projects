import java.io.*;

class PossibleValues {

  private boolean[] contained;  

  public PossibleValues(int size) {
    contained = new boolean[size];
  }

  public boolean isContained(int number) {
    // Lookup in zero-based array.
    return contained[number - 1];
  }

  public void setContained(int number) {
    contained[number - 1] = true;
  }

  public void setNotContained(int number) {
    contained[number - 1] = false;
  }

  public int getSize() {
    return contained.length;
  }

  public PossibleValues intersection(PossibleValues other) {
    PossibleValues result = new PossibleValues(contained.length);
    for (int i = 1; i <= contained.length; ++i) {
      if (isContained(i) || other.isContained(i)) {
        result.setContained(i);
      }
    }
    return result;
  }

  public int[] possibilities() {
    int resultSize = 0;
    for (int i = 1; i <= contained.length; ++i) {
      if (!isContained(i)) ++resultSize;
    }
    int[] result = new int[resultSize];
    int index = 0;
    for (int i = 1; i <= contained.length; ++i) {
      if (!isContained(i)) result[index++] = i;
    }
    return result;
  }
}

class ValueContainer {
  // contained[i] == true iff this container has i + 1. 
  private PossibleValues possible;  
  private Cell[] cells;  

  public ValueContainer(int size) {
    this.possible = new PossibleValues(size);
    this.cells = new Cell[size];
  }

  public void setCell(int index, Cell cell) {
    cells[index] = cell;
  }

  public Cell[] getCells() {
    return cells;
  }

  public PossibleValues getPossibleValues() {
    return possible;
  }
}

class Row extends ValueContainer {
  public Row(int size) {
    super(size);
  }
}

class Column extends ValueContainer {
  public Column(int size) {
    super(size);
  }
}

class Box extends ValueContainer {
  public Box(int size) {
    super(size);
  }
}

class Cell {
  private Integer value;  // Null if value is not yet known.
  private Row row;
  private Column column;
  private Box box;

  // Set a value, or null if the value is not known.
  public void setValue(Integer value) {
    Integer previous = this.value;
    if (previous != null) {
      row.getPossibleValues().setNotContained(previous);
      column.getPossibleValues().setNotContained(previous);
      box.getPossibleValues().setNotContained(previous);
    }
    this.value = value;
    if (value != null) {
      row.getPossibleValues().setContained(value);
      column.getPossibleValues().setContained(value);
      box.getPossibleValues().setContained(value);
    } 
  }

  public Integer getValue() {
    return value;
  }

  public boolean isValueKnown() {
    return value != null;
  }

  public void setRow(Row row) { this.row = row; }
  public void setColumn(Column column) { this.column = column; }
  public void setBox(Box box) { this.box = box; }
  public Row getRow() { return row; }
  public Column getColumn() { return column; }
  public Box getBox() { return box; }

  public int[] possibleValues() {
    return row.getPossibleValues()
      .intersection(column.getPossibleValues())
      .intersection(box.getPossibleValues())
      .possibilities();
  }
}


class Board {
  private Cell[][] board;
  private int rowsPerBox;
  private int columnsPerBox;

  public Board(int rowsPerBox, int columnsPerBox) {
    this.rowsPerBox = rowsPerBox; 
    this.columnsPerBox = columnsPerBox; 
    int size = rowsPerBox * columnsPerBox;
    this.board = new Cell[size][size];
    for (int row = 0; row < size; ++row) {
      for (int column = 0; column < size; ++column) {
        board[row][column] = new Cell();
      }
    }
    initializeReferences();
  }

  public Cell getCell(int row, int column) {
    return board[row][column]; 
  }

  public char toChar(Integer value) {
    if (value == null) return '.';
    else if (value <= 9) return (char) ('0' + value);
    else return (char) ('A' + value);
  }

  public int getSize() {
    return rowsPerBox * columnsPerBox;
  }

  public void print() {
    int size = getSize();
    for (int row = 0; row < size; ++row) {
      for (int column = 0; column < size; ++column) {
        System.out.print(toChar(getCell(row, column).getValue()));
        if (column < size - 1) System.out.print(' ');
      }
      System.out.println();
    }
  }

  private final void initializeReferences() {
    int size = getSize();

    for (int box = 0; box < size; ++box) {
      Box theBox = new Box(size);
      int rowStart = (box % columnsPerBox) * rowsPerBox;
      int columnStart = (box % rowsPerBox) * columnsPerBox;
      for (int row = 0; row < rowsPerBox; ++row) {
        for (int column = 0; column < columnsPerBox; ++column) {
          Cell cell = getCell(rowStart + row, columnStart + column);
          cell.setBox(theBox);
          theBox.setCell(row * columnsPerBox, cell);
        }
      }
    }

    for (int row = 0; row < size; ++row) {
      Row theRow = new Row(size);
      for (int column = 0; column < size; ++column) {
        Cell cell = getCell(row, column);
        cell.setRow(theRow);
        theRow.setCell(column, cell);
      }
    }

    for (int column = 0; column < size; ++column) {
      Column theColumn = new Column(size);
      for (int row = 0; row < size; ++row) {
        Cell cell = getCell(row, column);
        cell.setColumn(theColumn);
        theColumn.setCell(row, cell);
      }
    }
  }
}

class SudokuSolver {

  private Board board;

  public void solve(Board board) {
    Solver solver = new Solver(board);
    solver.solve(0); 
  }

  private class Solver {
    private final Board board;
    private final int size;

    Solver(Board board) {
      this.board = board;
      size = board.getSize();
    }

    void solve(int cellId) {
      if (cellId >= size * size) {
        board.print();
        System.out.println();
        return;
      }
      int row = cellId % size;
      int column = cellId / size;
      Cell cell = board.getCell(row, column);
      if (cell.isValueKnown()) {
        // Move on to the next cell.
        solve(cellId + 1);
        return;
      }
      for (int p : cell.possibleValues()) {
        cell.setValue(p);
        solve(cellId + 1);
      }
      cell.setValue(null);
    }
  }
}

public class Sudoku {
  public Board readBoardFromFile(String filename) throws IOException {
    return readBoard(new FileReader(filename));
  }

  public Board readBoard(Reader input) throws IOException {
    BufferedReader reader = new BufferedReader(input);
    int rowsPerBox = Integer.parseInt(reader.readLine());
    int columnsPerBox = Integer.parseInt(reader.readLine());
    Board board = new Board(rowsPerBox, columnsPerBox);
    int row = 0;
    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
      int column = 0;
      for (int charPos = 0; charPos < line.length(); ++charPos) {
        char c = line.charAt(charPos);
        if (c != ' ') {
          Integer value = parseChar(c);
          board.getCell(row, column).setValue(value);
          ++column;
        }
      }
      ++row;
    }
    return board;
  }

  public Integer parseChar(char c) {
    if (c == '.') return null;
    else if (c >= '0' && c <= '9') {
      return c - '0';
    } else return c - 'A';
  }
  
  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.out.println("Please specify a filename.");
      return;
    }
    Sudoku sudoku = new Sudoku();
    Board board = sudoku.readBoardFromFile(args[0]);
    board.print();
    // Test solving.
    System.out.println("Possible solutions:");
    SudokuSolver solver = new SudokuSolver();
    solver.solve(board);
  }
}
