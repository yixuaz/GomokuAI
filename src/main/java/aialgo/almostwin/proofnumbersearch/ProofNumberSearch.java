package aialgo.almostwin.proofnumbersearch;

import chessboardalgo.IChessboardAIAlgo;

import static aialgo.almostwin.proofnumbersearch.Node.INFINITY;
import static aialgo.almostwin.proofnumbersearch.Node.Status.*;
import static aialgo.almostwin.proofnumbersearch.Node.Type.AND;

public abstract class ProofNumberSearch {

    protected abstract void generateChildren(Node n, IChessboardAIAlgo chessboardAIAlgo);
    protected abstract boolean resourcesAvailable(long startTimeInMs, int count);
    protected abstract void evaluate(Node root, IChessboardAIAlgo chessboardAIAlgo);

    protected Node selectMostProvingNode( Node n, IChessboardAIAlgo chessboardAIAlgo ) {
        while ( n.expanded ) {
            int value = INFINITY;
            Node best = null;
            if ( n.type == AND ) {
                for (Node c : n.children) {
                    if (c.getValue() != UNKNOWN) continue;
                    if ( value > c.disproof ) {
                        best = c;
                        value = c.disproof;
                    }
                }
            } else { /* OR node */
                for (Node c : n.children) {
                    if (c.getValue() != UNKNOWN) continue;
                    if ( value > c.proof ) {
                        best = c;
                        value = c.proof;
                    }
                }
            }
            if (best == null) {
                System.out.println();
            }
            chessboardAIAlgo.setPiece(best.x, best.y, best.color);
            n = best;
        }
        return n;
    }

    protected void expandNode(Node n, IChessboardAIAlgo chessboardAIAlgo) {
        evaluate( n, chessboardAIAlgo.clone());
        if (n.getValue() == UNKNOWN)
            generateChildren( n, chessboardAIAlgo);
//        for (Node c : n.children) {
//            IChessboardAIAlgo cloned = chessboardAIAlgo.clone();
//            cloned.setPiece(c.x, c.y, c.color);
//            setProofAndDisproofNumbers( c );
//            if ( n.type == AND ) {
//                if ( c.disproof == 0 ) break;
//            } else {  /* OR node */
//                if ( c.proof == 0 ) break;
//            }
//        }
        n.expanded = true;
    }

    protected Node updateAncestors( Node n, Node root ) {
        while( n != root ) {
            if (!n.children.isEmpty()) {
                int oldProof = n.proof;
                int oldDisproof = n.disproof;
                setProofAndDisproofNumbers(n);
                if (n.proof == oldProof && n.disproof == oldDisproof)
                    return n;
            }
            n = n.parent;
        }
        setProofAndDisproofNumbers( root );
        return root;
    }

    protected void setProofAndDisproofNumbers( Node n ) {
        if ( n.expanded ) { /* interior node */
            if (n.children.isEmpty()) return;
            if ( n.type == AND ) {
                n.proof = 0;  n.disproof = INFINITY;
                for (Node c : n.children) {
                    n.proof += c.proof;
                    n.proof = Math.min(INFINITY, n.proof);
                    n.disproof = Math.min(n.disproof, c.disproof);
                }
            } else { /* OR node */
                n.proof = INFINITY;  n.disproof = 0;
                for (Node c : n.children) {
                    n.disproof += c.disproof;
                    n.disproof = Math.min(INFINITY, n.disproof);
                    n.proof = Math.min(n.proof, c.proof);
                }
            }
            if (n.proof == 0) n.setValue(PROVEN);
            else if (n.disproof == 0) n.setValue(DISPROVEN);
        } else { /* terminal node or none terminal leaf */
            switch( n.getValue() ) {
                case DISPROVEN: n.proof = INFINITY; n.disproof = 0; break;
                case PROVEN:    n.proof = 0; n.disproof = INFINITY; break;
                case UNKNOWN:   n.proof = 1; n.disproof = 1; break;
            }
        }
    }

    public void pns(Node root, IChessboardAIAlgo aiAlgo) {
        evaluate( root, aiAlgo);
        setProofAndDisproofNumbers( root );
        long startTimeInMs = System.currentTimeMillis();
        int i = 0;
        while ( root.proof != 0 && root.disproof != 0 && resourcesAvailable(startTimeInMs, i) ) {
            IChessboardAIAlgo chessboardAIAlgo = aiAlgo.clone();
            Node mostProving = selectMostProvingNode( root, chessboardAIAlgo);
            expandNode( mostProving, chessboardAIAlgo);
            updateAncestors( mostProving, root );
            i++;
        }
    }


}
