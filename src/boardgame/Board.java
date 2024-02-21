package boardgame;

public class Board {

    private int rows;
    private int columns;
    private Piece[][] pieces;

    public Board(int rows, int columns) {

        if (rows < 1 || columns < 1) { throw new BoardException("Error creating board: there must be at least 1 row and 1 column"); }

        this.rows = rows;
        this.columns = columns;
        this.pieces = new Piece[rows][columns];
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Piece piece(int row, int column) {
        if (!PositionExists(row, column)) { throw new BoardException("Position not on the board."); }
        return this.pieces[row][column];
    }

    public Piece piece(Position position) {
        if (!PositionExists(position.getRow(), position.getColumn())) { throw new BoardException("Position not on the board."); }
        return pieces[position.getRow()][position.getColumn()];
    }

    public void placePiece(Piece piece, Position position) {
        if (ThereIsAPiece(position)) { throw new BoardException("There is already piece on position."); }
        this.pieces[position.getRow()][position.getColumn()] = piece;
        piece.position = position;

    }

    private boolean PositionExists(int row, int column) {
        return (row >= 0 && row < this.rows) && (column >= 0 && column < this.columns);
    }

    public boolean PositionExists(Position position) {
        return this.PositionExists(position.getRow(), position.getColumn());
    }

    public boolean ThereIsAPiece(Position position) {
        if (!PositionExists(position)) { throw new BoardException("Position not on the board."); }
        return piece(position) != null;
    }
}
