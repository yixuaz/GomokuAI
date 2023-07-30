package chessboardalgo;

import UI.Piece;
import common.BoardToString;
import common.Position;
import consistent.IConsistentAlgo;
import consistent.PositionTranslator;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Deque;

public class ChessboardByteArrayAlgoConsistent extends ChessboardByteArrayAlgo implements IConsistentAlgo {
    @Getter
    private PositionTranslator positionTranslator;
    private Deque<Position> originAllSteps = new ArrayDeque<>();
    public ChessboardByteArrayAlgoConsistent(int size) {
        super(size);
    }

    @Override
    public void setPiece(Piece piece) {
        originAllSteps.offerLast(new Position(piece.getY(), piece.getX()));
        if (allSteps.isEmpty()) {
            if (piece.getY() != 7 || piece.getX() != 7) {
                positionTranslator = PositionTranslator.selectByOrigin(piece.getY(), piece.getX());
            } else {
                super.setPiece(piece);
                return;
            }
        }
        if (positionTranslator == null && allSteps.size() == 1) {
            positionTranslator = PositionTranslator.selectByOrigin(piece.getY(), piece.getX());
        }
        Piece np = positionTranslator.originTranslateThenOrigin(piece);
        allSteps.offerLast(new Position(np.getY(), np.getX()));
        location[getIdx(np.getY(),np.getX())] = (byte) piece.getPlayer().getId();
    }

    @Override
    public boolean isTerminal(Piece piece) {
        if (positionTranslator == null) return false;
        Piece np = positionTranslator.originTranslateThenOrigin(piece);
        return isTerminal(np.getX(), np.getY());
    }

    @Override
    public boolean isLegalMove(Piece piece) {
        if (positionTranslator == null) return true;
        Piece np = positionTranslator.originTranslateThenOrigin(piece);
        return isLegalMove(np.getX(), np.getY());
    }

    @Override
    public ChessboardByteArrayAlgoConsistent clone() {
        ChessboardByteArrayAlgoConsistent cloned = new ChessboardByteArrayAlgoConsistent(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cloned.setPiece(j, i, getValInBoard(j, i));
            }
        }
        cloned.allSteps = new ArrayDeque<>(allSteps);
        cloned.originAllSteps = new ArrayDeque<>(originAllSteps);
        cloned.positionTranslator = positionTranslator;
        return cloned;
    }

    @Override
    public String generateStepsCode() {
        return BoardToString.serialize(originAllSteps);
    }
}
