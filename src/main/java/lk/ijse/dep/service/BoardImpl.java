package lk.ijse.dep.service;

public class BoardImpl implements Board {

    private final Piece[][] pieces;

    private final BoardUI boardUI;


    public BoardImpl(BoardUI boardUI) {
        this.boardUI = boardUI;
        this.pieces = new Piece[6][5];



        for (int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces[i].length; j++) {
                pieces[i][j]=Piece.EMPTY;
            }
        }

    }
    //getters dala thiye

    public Piece[][] getPieces() {
        return pieces;
    }

    public BoardUI getBoardUI() {
        return this.boardUI;
    }


    //free space nethnm -1 return karanwa free empty space thiye nam i return karanwa
    //rows 5 i col  awa eka piesess eke col ekata set wela thiyenne

    @Override
    public int findNextAvailableSpot(int col) {
        for (int i = 0; i < pieces[col].length; i++) {
            if (pieces[col][i] == Piece.EMPTY) {
                return i;
            }
        }
        return -1;

    }

    //should return boolean values current move eka legal nam return karanwa booean represent ekak
    //true the false the

    @Override
    public boolean isLegalMove(int col) {
        int index = findNextAvailableSpot(col);
        return index != -1;
    }

    //4 connect wela thibunath meka check karanwa singale spot ekak thiyeda kiyala ita passe true return karanwa

    @Override
    public boolean existLegalMoves() {
        for (int i = 0; i < pieces.length; i++) {
            if (isLegalMove(i)) {
                return true;
            }

        }
        return false;
    }
    //specified col 1 row eka thamai update karanwa

    @Override
    public void updateMove(int col, Piece move) {
        int index = findNextAvailableSpot(col);
        pieces[col][index] = move;
    }

    @Override
    public void updateMove(int col, int row, Piece move) {
        pieces[col][row] = move;
    }



    //player 4 k connect karalada kiyala balanne
    @Override
    public Winner findWinner() {
        int count = 0;

        //vertically peththata

        for (int i = 0; i < pieces.length; i++){
            for (int j = 0; j < pieces[i].length-1; j++){
                if (pieces[i][j]==pieces[i][j+1]){  // i-0  j=3                0 4
                    count++;
                    if (count==3 && pieces[i][j]!=Piece.EMPTY){
                        return new Winner(pieces[i][j],i,(j-2),i,(j+1));
                    }
                }
                else{
                    count=0;
                }
            }
            count=0;
        }


        count = 0;

        //Horizontally

        for (int i = 0; i < pieces[0].length; i++){
            for (int j = 0; j < pieces.length-1; j++){
                if (pieces[j][i]==pieces[j+1][i]){
                    count++;
                    if (count==3 && pieces[j][i]!=Piece.EMPTY){
                        return  new Winner(pieces[j][i],(j-2),i,(j+1),i);
                    }
                }
                else{
                    count=0;
                }
            }
            count=0;
        }
        return new Winner(Piece.EMPTY);

    }


}


