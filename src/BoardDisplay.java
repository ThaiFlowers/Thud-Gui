import thud.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by Thai Flowers on 6/11/2017.
 */
public class BoardDisplay extends JPanel {
    private Player player;
    private PlayState playState;

    Color light = Color.WHITE;
    Color dark  = Color.DARK_GRAY;
    Color boardEdge = Color.BLACK;
    Color trollColor = Color.RED;
    Color dwarfColor = Color.BLUE;
    Color stoneColor = Color.BLACK;
    Color selected = Color.YELLOW;
    Color possible = new Color(Color.YELLOW.getRed(), Color.YELLOW.getGreen(), Color.YELLOW.getBlue(), 100);

    Integer[] lastClickCell = new Integer[] {-1,-1};
    Point lastClickPoint;

    Boolean drag = false;
    Integer[] dragStartCell = new Integer[] {-1, -1};
    Point lastDragPoint;

    int padX = 0;
    int padY = 0;
    int boardSide =0;
    int cellSide = 0;
    int cellError = 0;

    public BoardDisplay(Player player, PlayState playState) {
        super();

        setPreferredSize(new Dimension(500, 500));
        setMinimumSize(new Dimension(250, 250));

        swapData(player, playState);
        BoardDisplayMouseAdaptor bdma = new BoardDisplayMouseAdaptor();
        addMouseMotionListener(bdma);
        addMouseListener(bdma);
    }

    public void swapData(Player player, PlayState playState) {
        lastClickCell = new Integer[] {-1,-1};
        lastClickPoint = null;

        this.player = player;
        this.playState = playState;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Board board = player.getBoard();

        boardSide = (getWidth() > getHeight()) ?  getHeight() : getWidth();
        cellSide  = boardSide / 15;
        cellError = boardSide % 15;

        padX = (getWidth()  - boardSide + cellError) / 2;
        padY = (getHeight() - boardSide + cellError) / 2;

        BoardStates turn = playState.getTurn();
        BoardStates cellState;
        boolean ourPiece = false;

        if (BoardPoint.isOnBoard(lastClickCell[0], lastClickCell[1])) {
            cellState = board.getAtPosition(lastClickCell[0], lastClickCell[1]);
            ourPiece = turn.equals(cellState);
        }

        ArrayList<BoardPoint> possibleMoves = null;
        if (ourPiece && BoardPoint.isOnBoard(lastClickCell[0], lastClickCell[1])) {
            possibleMoves = player.getPossibleMoves(playState, new BoardPoint(lastClickCell[0], lastClickCell[1]));
        }

        for (int i=0; i<15; i++) {
            for (int j = 0; j < 15; j++) {
                BoardPoint pos = new BoardPoint(i,j);
                cellState = board.getAtPosition(pos);
                Color curCol = (Math.pow(-1, i + j) == 1) ? dark : light;

                if (ourPiece && i==lastClickCell[0] && j==lastClickCell[1])
                    g.setColor(selected);
                else
                    g.setColor(curCol);

                g.fillRect(j * cellSide + padX, i * cellSide + padY, cellSide, cellSide);
                if (!(cellState == BoardStates.FREE || (drag && i==dragStartCell[0]) && j==dragStartCell[1]))
                    drawPiece(g, cellState, j*cellSide+padX, i*cellSide+padY, cellSide);
                if (possibleMoves!=null && possibleMoves.contains(pos)) {
                    g.setColor(possible);
                    g.fillRect(j * cellSide + padX, i * cellSide + padY, cellSide, cellSide);
                }
            }
        }

        if (drag) {
            cellState = board.getAtPosition(dragStartCell[0], dragStartCell[1]);
            drawPiece(g, cellState, lastDragPoint.x, lastDragPoint.y, cellSide);
        }

        drawEdgeTriangles(g, cellSide, padX, padY);
    }

    private void drawPiece(Graphics g, BoardStates cellState, int i, int j, int cellSide) {
        switch (cellState) {
            case DWARF:
                g.setColor(dwarfColor);
                break;
            case TROLL:
                g.setColor(trollColor);
                break;
            case STONE:
                g.setColor(stoneColor);
                break;
        }
        g.fillRect(i,j,cellSide/2, cellSide/2);
    }

    private void drawEdgeTriangles(Graphics g, int cellSide, int padX, int padY) {
        g.setColor(boardEdge);
        int[] triag1Xs = new int[] {padX, padX+cellSide*5, padX};
        int[] triag1Ys = new int[] {padY, padY, padY+cellSide*5};
        g.fillPolygon(triag1Xs, triag1Ys, 3);

        int[] triag2Xs = new int[] {padX+cellSide*15, padX+cellSide*10, padX+cellSide*15};
        int[] triag2Ys = new int[] {padY, padY, padY+cellSide*5};
        g.fillPolygon(triag2Xs, triag2Ys, 3);

        int[] triag3Xs = new int[] {padX, padX+cellSide*5, padX};
        int[] triag3Ys = new int[] {padY+15*cellSide, padY+cellSide*15, padY+cellSide*10};
        g.fillPolygon(triag3Xs, triag3Ys, 3);

        int[] triag4Xs = new int[] {padX+cellSide*15, padX+cellSide*10, padX+cellSide*15};
        int[] triag4Ys = new int[] {padY+15*cellSide, padY+cellSide*15, padY+cellSide*10};
        g.fillPolygon(triag4Xs, triag4Ys, 3);
    }

    private class BoardDisplayMouseAdaptor extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (e.getButton() == MouseEvent.BUTTON1) {
                lastClickPoint = e.getPoint();
            }

            boolean xOkay = (padX < lastClickPoint.x && lastClickPoint.x < (padX+15*boardSide));
            boolean yOkay = (padY < lastClickPoint.y && lastClickPoint.y < (padY+15*boardSide));
            if (xOkay && yOkay) {

                // remember format is row x col (y is row, x is col)
                lastClickCell[0] = Math.floorDiv(lastClickPoint.y-padY, cellSide);
                lastClickCell[1] = Math.floorDiv(lastClickPoint.x-padX, cellSide);

            }

            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            lastDragPoint = e.getPoint();

            boolean xOkay = (padX < lastDragPoint.x && lastDragPoint.x < (padX+15*boardSide));
            boolean yOkay = (padY < lastDragPoint.y && lastDragPoint.y < (padY+15*boardSide));
            if (xOkay && yOkay) {

                dragStartCell[0] = Math.floorDiv(lastDragPoint.y - padY, cellSide);
                dragStartCell[1] = Math.floorDiv(lastDragPoint.x - padX, cellSide);

                BoardStates turn = playState.getTurn();
                Board board = player.getBoard();
                drag = turn.equals(board.getAtPosition(dragStartCell[0], dragStartCell[1]));
            }
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);

            lastDragPoint = e.getPoint();
            int[] dragEndCell = new int[2];

            boolean xOkay = (padX < lastDragPoint.x && lastDragPoint.x < (padX+15*boardSide));
            boolean yOkay = (padY < lastDragPoint.y && lastDragPoint.y < (padY+15*boardSide));
            if (xOkay && yOkay) {

                dragEndCell[0] = Math.floorDiv(lastDragPoint.y - padY, cellSide);
                dragEndCell[1] = Math.floorDiv(lastDragPoint.x - padX, cellSide);
            }

            BoardPoint startPos = new BoardPoint(dragStartCell[0], dragStartCell[1]);
            BoardPoint endPos = new BoardPoint(dragEndCell[0], dragEndCell[1]);

            if (!startPos.equals(endPos)) {

                String moveString;
                if (playState.isRemoveTurn()) {
                    moveString = String.format("R %s", endPos.toString());
                }
                else if (playState.isTurn(BoardStates.DWARF) && player.getBoard().getAtPosition(endPos).equals(BoardStates.TROLL)) {
                    moveString = String.format("H %s %s", startPos.toString(), endPos.toString());
                }
                else if (playState.isTurn(BoardStates.TROLL) &&
                        ((Math.abs(endPos.getRow()-startPos.getRow())>1) || (Math.abs(endPos.getCol()-startPos.getCol()))>1)) {
                    moveString = String.format("S %s %s", startPos.toString(), endPos.toString());
                }
                else{
                    moveString = String.format("M %s %s", startPos.toString(), endPos.toString());
                }
                try {
                    player.play(playState, moveString);
                } catch (IllegalArgumentException ex) {
                    System.err.println(ex.getMessage());
                }
            }

            lastDragPoint = null;
            lastClickPoint = null;
            lastClickCell = new Integer[] {-1,-1};
            dragStartCell = new Integer[] {-1, -1};
            drag = false;
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            lastDragPoint = e.getPoint();
            repaint();
        }
    }
}