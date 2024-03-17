package lk.ijse.dep.service;

import java.util.*;

public class AiPlayer extends Player{
    public AiPlayer(Board board) {
        super(board);
    }

    @Override
    public void movePiece(int col) {

        MCTS mcts=new MCTS(board,4000);
        col=mcts.findTheMove();

        board.updateMove(col,Piece.GREEN);
        board.getBoardUI().update(col,false);
        Winner winner=board.findWinner();
        if (winner.getWinningPiece()!=Piece.EMPTY){
            board.getBoardUI().notifyWinner(winner);
        }
        else if (!board.existLegalMoves()){
            board.getBoardUI().notifyWinner(new Winner(Piece.EMPTY));
        }


    }

    //MCTS Algorithm itself and some Utility Classes
    private static class MCTS {
        private final Board board;

        //Iteration Count
        private final int computations;

        public MCTS(Board board, int computations) {
            this.board = board;
            this.computations = computations;
        }

        private int findTheMove(){
            int count=0;

            Node tree= new Node(board,Piece.BLUE);

            while (count<computations){

                //Selection
                Node selectedNode=selectNode(tree);

                //Expand
                Node nodeToExplore= expandNode(selectedNode);

                //Simulation
                Piece result=randomSimulation(nodeToExplore);

                //Backpropagation
                backPropagation(nodeToExplore,result);


                count++;
            }

            Node bestNode=tree.getChildWithMaxScore();

            return bestNode.getMove();

        }

        //MCTS Required Methods

        private Node selectNode(Node tree) {
             /* Steps:
                1. Repeatedly select most promising legal move
                2. Move to that most promising node.
                3. Stop if the current node is a leaf node
              */

            Node currentNode=tree;
            while (currentNode.getChildren().size()!=0){
                currentNode=findBestNodeWithUCT(currentNode);
            }
            return currentNode;
        }

        private Node expandNode(Node selectedNode) {
            /* Steps:
               1. Randomly choose one of possible moves
               2. Create a child node according to that move.
               3. Add this node to the selected node after SELECTION PHASE to expand search tree
             */

            boolean gameStatus=isTheGameOngoing(selectedNode.getBoard()); //True //Flase
            if (!gameStatus){
                return selectedNode;
            }
            else {
                List<BoardWithIndex> nextLegalMoves=getLegalMoves(selectedNode);
                for (BoardWithIndex nextLegalMove : nextLegalMoves) {
                    Board move = nextLegalMove.getBoard();
                    Node childNode = new Node(move, (selectedNode.getPiece() == Piece.BLUE) ? Piece.GREEN : Piece.BLUE);
                    childNode.setParent(selectedNode);
                    childNode.setMove(nextLegalMove.getIndex());
                    selectedNode.addChild(childNode);
                }
                Random random=new Random();
                int randomIndex=random.nextInt(nextLegalMoves.size());
                return selectedNode.getChildren().get(randomIndex);
            }
        }

        private Piece randomSimulation(Node nodeToExplore) {
            /* Steps:
               1. Simulating game until it is finish (win/lost/draw)
               2. Moves are chosen randomly
               3. Return simulation result
             */
            Board board=copyBoardState(nodeToExplore.getBoard());
            Node node= new Node(board, nodeToExplore.getPiece());
            node.setParent(nodeToExplore.getParent());

            if (node.getBoard().findWinner().getWinningPiece()==Piece.BLUE){
                node.getParent().setScore(Integer.MIN_VALUE);
                return Piece.BLUE;
            }

            while (isTheGameOngoing(node.getBoard())){
                BoardWithIndex nextMove=getRandomNextBoard(node);
                Node child=new Node(nextMove.getBoard(), node.getPiece());
                child.setParent(node);
                child.setMove(nextMove.getIndex());
                node.addChild(child);
                node = child;
            }

            if (node.getBoard().findWinner().getWinningPiece()==Piece.GREEN){
                return Piece.GREEN;
            }
            else if (node.getBoard().findWinner().getWinningPiece()==Piece.BLUE){
                return Piece.BLUE;
            }
            else {
                return Piece.EMPTY; //Draw
            }
        }

        private void backPropagation(Node nodeToExplore, Piece result) {
             /* Steps:
                 Update parent statistics after the simulation playout. For each visited node:
                1. Increase visit count
                2. Increase player score

                this method will work until it reach the parent node
              */

            Node node=nodeToExplore;
            while (node!=null){
                node.incrementVisit();
                if (node.getPiece() ==result){
                    node.incrementScore();
                }
                node= node.getParent();
            }

        }


        //Utility Methods



        //This Method is to get the next legal moves
        public List<BoardWithIndex> getLegalMoves(Node selectedNode) {

            List<BoardWithIndex> nextMoves = new ArrayList<>();

            //Find the next Player
            Piece nextPiece= (selectedNode.getPiece() ==Piece.BLUE)?Piece.GREEN:Piece.BLUE;

            for (int i = 0; i < 6; i++) {
                if (selectedNode.getBoard().isLegalMove(i)){
                    int raw= selectedNode.getBoard().findNextAvailableSpot(i);
                    Board copyBoard=copyBoardState(selectedNode.getBoard());
                    copyBoard.updateMove(i,raw,nextPiece);
                    BoardWithIndex boardWithIndex=new BoardWithIndex(copyBoard,i);
                    nextMoves.add(boardWithIndex);
                }
            }

            return nextMoves;
        }

        //This Method is to get a random move from available moves
        private BoardWithIndex getRandomNextBoard(Node node) {
            List<BoardWithIndex> legalMoves=getLegalMoves(node);
            Random random=new Random();
            //Maybe If method can be here
            if (legalMoves.isEmpty()) {
                return null;
            }
            int randomIndex=random.nextInt(legalMoves.size());
            return legalMoves.get(randomIndex);
        }

        //This method is to check the game is finished or not
        public boolean isTheGameOngoing(Board board){
            Winner winner=board.findWinner();
            if (winner.getWinningPiece()!=Piece.EMPTY){
                return false;
            } else if (!board.existLegalMoves()) {
                return false;
            }
            return true;
        }

        //This method is to get a copy of a board object
        private Board copyBoardState(Board originalBoard) {
            // Create a new board and copy the state cell by cell
            Board newBoard = new BoardImpl(originalBoard.getBoardUI());
            for (int col = 0; col < Board.NUM_OF_COLS; col++) {
                for (int row = 0; row < Board.NUM_OF_ROWS; row++) {
                    Piece piece = originalBoard.getPieces()[col][row];
                    newBoard.updateMove(col, row, piece);
                }
            }
            return newBoard;
        }


    }

    //To Store Board Type Objects and their indexes
    private static class BoardWithIndex {
        private final Board board;
        private final int index;

        public BoardWithIndex(Board board, int index) {
            this.board = board;
            this.index = index;
        }

        public Board getBoard() {
            return board;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return "BoardWithIndex{" +
                    "board=" + board +
                    ", index=" + index +
                    '}';
        }

    }

    //Node
    private static class Node{
        private Board board;

        private int visit;

        private int score;

        private final List<Node> children = new ArrayList<>();

        private Node parent= null;

        private Piece piece;

        private int move;

        public Node(Board board, Piece piece) {
            this.setBoard(board);
            this.setPiece(piece);
        }

        public Node getChildWithMaxScore() {
            Node result = getChildren().get(0);
            for (int i = 1; i < getChildren().size(); i++) {
                if (getChildren().get(i).getScore() > result.getScore()) {
                    result = getChildren().get(i);
                }
            }
            return result;
        }

        public void addChild(Node node) {
            getChildren().add(node);
        }

        public Board getBoard() {
            return board;
        }


        public void setBoard(Board board) {
            this.board = board;
        }

        public int getVisit() {
            return visit;
        }

        public void incrementVisit() {
            this.visit ++;
        }

        public int getScore() {
            return score;
        }

        public void incrementScore() {
            this.score ++;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public List<Node> getChildren() {
            return children;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public Piece getPiece() {
            return piece;
        }

        public void setPiece(Piece piece) {
            this.piece = piece;
        }

        public int getMove() {
            return move;
        }

        public void setMove(int move) {
            this.move = move;
        }
    }

    //The UTC Formula to find the best nod
    public static Node findBestNodeWithUCT(Node node) {
        List<Node> children = node.getChildren();
        if (children.isEmpty()) {
            return null;
        }

        Node bestNode = null;
        double bestUCTValue = Double.NEGATIVE_INFINITY;
        int parentVisit = node.getVisit();

        for (Node child : children) {
            int nodeVisit = child.getVisit();
            double nodeWinScore = child.getScore();

            if (nodeVisit == 0) {
                return child; // Return the  node immediately if its not visited yet
            }

            double uctValue = (nodeWinScore / (double) nodeVisit)
                    + 1.41 * Math.sqrt(Math.log(parentVisit) / (double) nodeVisit);

            if (uctValue > bestUCTValue) {
                bestUCTValue = uctValue;
                bestNode = child;
            }
        }

        return bestNode;
    }




}
