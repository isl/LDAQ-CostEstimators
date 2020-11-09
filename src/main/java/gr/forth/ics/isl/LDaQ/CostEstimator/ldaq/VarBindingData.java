package gr.forth.ics.isl.LDaQ.CostEstimator.ldaq;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.TupleSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fafalios
 */
public class VarBindingData {

    private Node subjectOrObject;
    private List<Node> predicates;
    private List<TupleSlot> positionsInTriple;
    private List<Node> elementUsedToBindOrJoin;

    public VarBindingData(Node subjectOrObject, Node predicate, TupleSlot positionInTriple, Node otherNode) {
        this.predicates = new ArrayList<>();
        this.positionsInTriple = new ArrayList<>();
        this.elementUsedToBindOrJoin = new ArrayList<>();

        this.subjectOrObject = subjectOrObject;
        if (predicate != null) predicates.add(predicate);
        if (positionInTriple != null) this.positionsInTriple.add(positionInTriple);
        if (otherNode != null) this.elementUsedToBindOrJoin.add(otherNode);
    }

    public Node getSubjectOrObject() {
        return subjectOrObject;
    }

    public void setSubjectOrObject(Node subjectOrObject) {
        this.subjectOrObject = subjectOrObject;
    }

    public List<Node> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<Node> predicates) {
        this.predicates = predicates;
    }

    public List<TupleSlot> getPositionsInTriple() {
        return positionsInTriple;
    }

    public void setPositionsInTriple(List<TupleSlot> positionsInTriple) {
        this.positionsInTriple = positionsInTriple;
    }

    public List<Node> getOtherNode() {
        return elementUsedToBindOrJoin;
    }

    public void setOtherNode(List<Node> otherNode) {
        this.elementUsedToBindOrJoin = otherNode;
    }

    public void addPredicate(Node predicate) {
        this.predicates.add(predicate);
    }

    public void addPositionInTriple(TupleSlot positionInTriple) {
        this.positionsInTriple.add(positionInTriple);
    }

    public void addOtherNode(Node otherNode) {
        this.elementUsedToBindOrJoin.add(otherNode);
    }

    @Override
    public String toString() {
        final String sb = "VarBindingData{" + "subjectOrObject=" + subjectOrObject +
                          ", predicates=" + predicates +
                          ", positionsInTriple=" + positionsInTriple +
                          ", elementUsedToBindOrJoin=" + elementUsedToBindOrJoin +
                          '}';
        return sb;
    }
}
