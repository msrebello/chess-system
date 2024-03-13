package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private Board board;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;
    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();


    public ChessMatch() {
        this.board = new Board(8, 8);
        this.turn = 1;
        this.currentPlayer = Color.WHITE;
        this.initialSetup();
    }

    public int getTurn() {
        return this.turn;
    }

    public Color getCurrentPlayer() {
        return this.currentPlayer;
    }

    public boolean getCheck() {
        return this.check;
    }

    public boolean getCheckMate() {
        return this.checkMate;
    }

    public ChessPiece getEnPassantVulnerable() { return this.enPassantVulnerable; }
    public ChessPiece getPromoted() { return promoted; }
    public ChessPiece[][] getPieces() {
        ChessPiece[][] mat = new ChessPiece[this.board.getRows()][this.board.getColumns()];
        for (int i = 0; i < this.board.getRows(); i++) {
            for (int j = 0; j < this.board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) this.board.piece(i, j);
            }
        }
        return mat;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return this.board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();
        validateSourcePosition(source);
        validateTargetPosition(source, target);
        Piece capturedPiece = makeMove(source, target);

        if (testCheck(this.currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("You can't put yourself in check");
        }

        ChessPiece movedPiece = (ChessPiece) this.board.piece(target);

        promoted = null;
        if (movedPiece instanceof Pawn) {
            if ((movedPiece.getColor() == Color.WHITE && target.getRow() == 0) || (movedPiece.getColor() == Color.BLACK && target.getRow() == 7)) {
                promoted = (ChessPiece)board.piece(target);
                promoted = replacePromotedPiece("Q");
            }
        }

        this.check = testCheck(opponent(currentPlayer));

        if (testCheckMate(opponent(currentPlayer)))
            this.checkMate = true;
        else
            nextTurn();

        // enPassant
        if (movedPiece instanceof Pawn && (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2))
            this.enPassantVulnerable = movedPiece;
        else
            this.enPassantVulnerable = null;

        return (ChessPiece) capturedPiece;
    }

    private Piece makeMove(Position source, Position target) {
        ChessPiece p = (ChessPiece) this.board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = this.board.removePiece(target);
        this.board.placePiece(p, target);

        if (capturedPiece != null) {
            this.piecesOnTheBoard.remove(capturedPiece);
            this.capturedPieces.add(capturedPiece);
        }

        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) this.board.removePiece(sourceT);
            this.board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) this.board.removePiece(sourceT);
            this.board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        // En Passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) {
                Position pawnPosition;
                if (p.getColor() == Color.WHITE)
                    pawnPosition = new Position(target.getRow() + 1, target.getColumn());
                else
                    pawnPosition = new Position(target.getRow() - 1, target.getColumn());

                capturedPiece = this.board.removePiece(pawnPosition);
                this.capturedPieces.add(capturedPiece);
                this.piecesOnTheBoard.remove(capturedPiece);
            }
        }

        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece) {
        ChessPiece p = (ChessPiece) this.board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);

        if (capturedPiece != null) {
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }

        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getRow() + 3);
            Position targetT = new Position(source.getRow(), source.getRow() + 1);
            ChessPiece rook = (ChessPiece) this.board.removePiece(targetT);
            this.board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getRow() - 4);
            Position targetT = new Position(source.getRow(), source.getRow() - 1);
            ChessPiece rook = (ChessPiece) this.board.removePiece(targetT);
            this.board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        // En Passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == this.enPassantVulnerable) {
                ChessPiece pawn = (ChessPiece) this.board.removePiece(target);
                Position pawnPosition;
                if (p.getColor() == Color.WHITE)
                    pawnPosition = new Position(3, target.getColumn());
                else
                    pawnPosition = new Position(4, target.getColumn());

                this.board.placePiece(pawn,pawnPosition);
            }
        }
    }

    public ChessPiece replacePromotedPiece(String type) {
        if (this.promoted == null) {
            throw new IllegalStateException("There is no piece to be promoted");
        }
        if (!type.equals("B") && !type.equals("N") && !type.equals("R") & !type.equals("Q")) {
            return this.promoted;
        }

        Position pos = this.promoted.getChessPosition().toPosition();
        Piece p = this.board.removePiece(pos);
        this.piecesOnTheBoard.remove(p);

        ChessPiece newPiece = newPiece(type, this.promoted.getColor());
        this.board.placePiece(newPiece, pos);
        this.piecesOnTheBoard.add(newPiece);

        return newPiece;
    }

    private ChessPiece newPiece(String type, Color color) {
        if (type.equals("B")) return new Bishop(board, color);
        if (type.equals("N")) return new Knight(board, color);
        if (type.equals("Q")) return new Queen(board, color);
        return new Rook(board, color);
    }
    private void validateSourcePosition(Position position) {
        if (!this.board.thereIsAPiece(position)) {
            throw new ChessException("There is no piece on source position.");
        }
        if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) {
            throw new ChessException("The chosen piece is not yours");
        }
        if (!this.board.piece(position).isThereAnyPossibleMove()) {
            throw new ChessException("There is no possible moves for the chosen piece.");
        }
    }

    private void validateTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target))
            throw new ChessException("The chosen piece can't move to target position.");
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private void placeNewPiece(char column, int row, ChessPiece piece) {
        this.board.placePiece(piece, new ChessPosition(column, row).toPosition());
        this.piecesOnTheBoard.add(piece);
    }

    private void initialSetup() {
        placeNewPiece('a', 1, new Rook(this.board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(this.board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(this.board, Color.WHITE));
        placeNewPiece('d', 1, new Queen(this.board, Color.WHITE));
        placeNewPiece('e', 1, new King(this.board, Color.WHITE, this));
        placeNewPiece('f', 1, new Bishop(this.board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(this.board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(this.board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(this.board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(this.board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(this.board, Color.WHITE, this));
        placeNewPiece('d', 2, new Pawn(this.board, Color.WHITE, this));
        placeNewPiece('e', 2, new Pawn(this.board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(this.board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(this.board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(this.board, Color.WHITE, this));

        placeNewPiece('a', 8, new Rook(this.board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(this.board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(this.board, Color.BLACK));
        placeNewPiece('d', 8, new Queen(this.board, Color.BLACK));
        placeNewPiece('e', 8, new King(this.board, Color.BLACK, this));
        placeNewPiece('f', 8, new Bishop(this.board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(this.board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(this.board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(this.board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(this.board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(this.board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(this.board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(this.board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(this.board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(this.board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(this.board, Color.BLACK, this));

    }

    private Color opponent(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(Color color) {
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).toList();
        for (Piece p : list) {
            if (p instanceof King) {
                return (ChessPiece) p;
            }
        }
        throw new IllegalStateException("There is no " + color + " king on the board.");
    }

    private boolean testCheck(Color color) {
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == opponent(color)).toList();
        for (Piece p : opponentPieces) {
            boolean[][] mat = p.possibleMoves();
            if (mat[kingPosition.getRow()][kingPosition.getColumn()])
                return true;
        }
        return false;
    }

    private boolean testCheckMate(Color color) {
        if (!testCheck(color))
            return false;
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).toList();
        for (Piece p : list) {
            boolean[][] mat = p.possibleMoves();
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if (mat[i][j]) {
                        Position source = ((ChessPiece) p).getChessPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean testCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);
                        if (!testCheck)
                            return false;
                    }
                }
            }
        }
        return true;
    }
}
