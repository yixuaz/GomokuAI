package aialgo.almostwin.proofnumbersearch;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static aialgo.almostwin.proofnumbersearch.Node.Type.AND;
import static aialgo.almostwin.proofnumbersearch.Node.Type.OR;

public class Node {


    public enum Type {
        AND,
        OR
    }
    public enum Status {
        DISPROVEN,
        PROVEN,
        UNKNOWN
    }
    public static final int INFINITY = Integer.MAX_VALUE >> 1;

    public final int y;
    public final int x;
    public final int color;
    public final Type type;

    protected final Node parent;
    public final List<Node> children = new ArrayList<>();


    protected boolean expanded;
    protected int proof;
    protected int disproof;
    public final int step;
    @Getter
    private Status value;

    public void setValue(Status value) {
        this.value = value;
        switch( value ) {
            case DISPROVEN: proof = INFINITY; disproof = 0; break;
            case PROVEN:    proof = 0; disproof = INFINITY; break;
        }
    }

    public Node(Node parent, int y, int x, int color, int step) {
        this.parent = parent;
        this.y = y;
        this.x = x;
        this.color = color;
        if (parent != null)
            assert parent.color == 3 - color;
        this.type = (parent == null || parent.type == AND) ? OR : AND;
        this.proof = this.disproof = 1;
        this.value = Status.UNKNOWN;
        this.step = step;
    }

    public Node findChildren(int y, int x) {
        for (Node node : children) {
            if (node.y == y && node.x == x) return node;
        }
        return null;
    }

    public Node findWinMove() {
        for (Node node : children) {
            if (node.value == Status.PROVEN) return node;
        }
        throw new IllegalStateException("!!");
    }

    @Override
    public String toString() {
        return "Node{" +
                "y=" + y +
                ", x=" + x +
                ", type=" + type +
                ", proof=" + proof +
                ", disproof=" + disproof +
                ", step=" + step +
                ", value=" + value +
                '}';
    }
}
