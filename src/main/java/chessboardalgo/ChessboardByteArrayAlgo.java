package chessboardalgo;

import UI.Piece;
import common.BoardToString;
import common.Position;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Deque;

public class ChessboardByteArrayAlgo implements IChessboardAlgo {
    @Getter
    protected final byte[] location;
    @Getter
    protected final int size;

    protected Deque<Position> allSteps;

    public ChessboardByteArrayAlgo(int size) {
        this.size = size;
        location = new byte[size * size];
        allSteps = new ArrayDeque<>();
    }

    @Override
    public ChessboardByteArrayAlgo clone() {
        ChessboardByteArrayAlgo cloned = new ChessboardByteArrayAlgo(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cloned.setPiece(j, i, getValInBoard(j, i));
            }
        }
        cloned.allSteps.addAll(allSteps);
        return cloned;
    }

    @Override
    public boolean isLegalMove(Piece p) {
        return isLegalMove(p.getX(), p.getY());
    }

    @Override
    public boolean isLegalMove(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size && location[getIdx(y,x)] == 0;
    }

    @Override
    public void setPiece(Piece piece) {
        allSteps.offerLast(new Position(piece.getY(), piece.getX()));
        location[getIdx(piece.getY(),piece.getX())] = (byte) piece.getPlayer().getId();
    }

    public void setPiece(int x, int y, int color) {
        location[getIdx(y,x)] = (byte) color;
    }


    @Override
    public Deque<Position> getAllSteps() {
        return new ArrayDeque<>(allSteps);
    }

    public boolean isTerminal(int x, int y) {
        int playerId = location[getIdx(y,x)];
        assert playerId != 0;
        int sum = 0;
        for (int i = x - 4; i <= x + 4; i++) {
            if (i < 0) continue;
            if (i >= size) break;
            if (location[getIdx(y,i)] != playerId) {
                sum = 0;
            } else {
                if (++sum == 5) return true;
            }
        }
        sum = 0;
        for (int i = y - 4; i <= y + 4; i++) {
            if (i < 0) continue;
            if (i >= size) break;
            if (location[getIdx(i,x)] != playerId) {
                sum = 0;
            } else {
                if (++sum == 5) return true;
            }
        }
        sum = 0;
        for (int i = -4; i <= 4; i++) {
            int nx = x + i, ny = y + i;
            if (ny < 0 || nx < 0) continue;
            if (ny >= size || nx >= size) break;
            if (location[getIdx(ny,nx)] != playerId) {
                sum = 0;
            } else {
                if (++sum == 5) return true;
            }
        }
        sum = 0;
        for (int i = -4; i <= 4; i++) {
            int nx = x + i, ny = y - i;
            if (ny >= size || nx < 0) continue;
            if (ny < 0 || nx >= size) break;
            if (location[getIdx(ny,nx)] != playerId) {
                sum = 0;
            } else {
                if (++sum == 5) return true;
            }
        }

        return false;
    }

    public int steps() {
        return allSteps.size();
    }

    @Override
    public boolean isTerminal(Piece piece) {
        return isTerminal(piece.getX(), piece.getY());
    }

    public int getValInBoard(int x, int y) {
        if (x < 0 || x >= size || y < 0 || y >= size) throw new IllegalArgumentException();
        return location[getIdx(y,x)];
    }

    public boolean hasNeighbor(int x, int y, int distance, int count) {
        if (x < 0 || x >= size || y < 0 || y >= size) throw new IllegalArgumentException();
        int sx = x - distance, ex = x + distance;
        int sy = y - distance, ey = y + distance;
        for (int i = sy; i <= ey; i++) {
            if (i < 0 || i >= size) continue;
            for (int j = sx; j <= ex; j++) {
                if (j < 0 || j >= size) continue;
                if (i == y && j == x) continue;
                if (location[getIdx(i,j)] != 0) count--;
                if (count <= 0) return true;
            }
        }
        return false;
    }

    public int getIdx(int y, int x) {
        return y * size + x;
    }


    public void print() {
        System.out.println("-----board-------");
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++)
                System.out.print(location[getIdx(i, j)] + "\t");
            System.out.println();
        }
    }

    public String generateStepsCode() {
        return BoardToString.serialize(this.getAllSteps());
    }
}
