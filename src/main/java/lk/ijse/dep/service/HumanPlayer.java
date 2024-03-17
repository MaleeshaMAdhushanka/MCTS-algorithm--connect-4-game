package lk.ijse.dep.service;

public class HumanPlayer extends Player {

    public HumanPlayer(Board board){
        super(board);
    }


    //humana player click karahama col ekak automatically invoking wenawa colmn index ekak ekka

    //■ If there is a winner, notify the UI about the winner via BoardUI’s notifyWinner(winner)
   //If there is no winner, checks whether any legal moves exist across the board via board’s
    //existLegalMoves(), if not notify the UI that the game is tied
    @Override
    public void movePiece(int col) {
        if (board.isLegalMove(col)) {
            board.updateMove(col, Piece.BLUE);
            board.getBoardUI().update(col, true);
            Winner winner = board.findWinner();
            if (winner.getWiningPiece() != Piece.EMPTY) {
                board.getBoardUI().notifyWinner(winner);
            } else if (!board.existLegalMoves()) {
                board.getBoardUI().notifyWinner(new Winner(Piece.EMPTY));
            }
        }
    }
}
