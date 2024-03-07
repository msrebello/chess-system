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
        if (!positionExists(row, column)) { throw new BoardException("Position not on the board."); }
        return this.pieces[row][column];
    }

    public Piece piece(Position position) {
        if (!positionExists(position)) { throw new BoardException("Position not on the board."); }
        return pieces[position.getRow()][position.getColumn()];
    }

    public void placePiece(Piece piece, Position position) {
        if (thereIsAPiece(position)) { throw new BoardException("There is already piece on position."); }
        this.pieces[position.getRow()][position.getColumn()] = piece;
        piece.position = position;

    }

    private boolean positionExists(int row, int column) {
        return (row >= 0 && row < this.rows) && (column >= 0 && column < this.columns);
    }

    public boolean positionExists(Position position) {
        return this.positionExists(position.getRow(), position.getColumn());
    }

    public boolean thereIsAPiece(Position position) {
        if (!positionExists(position)) { throw new BoardException("Position not on the board."); }
        return piece(position) != null;
    }

    public Piece removePiece(Position position) {
        if (!positionExists(position))
            throw new BoardException("Position not on the board.");
        if (!thereIsAPiece(position))
            return null;
        Piece temp = piece(position);
        temp.position = null;
        this.pieces[position.getRow()][position.getColumn()] = null;

        return temp;
    }
;}
