package gr.forth.ics.isl.LDaQ.CostEstimator.ldaq;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.*;

import java.util.*;

import static gr.forth.ics.isl.LDaQ.CostEstimator.ldaq.Transform.getTriples;

/**
 * @author Fafalios
 */
public class CheckAnswerability {

    public static boolean isLinkedDataAnswerableQuery(Query query) {
        return isLinkedDataAnswerableQuery(query.getQueryPattern());
    }

    public static String getPattern(Element graphPattern) {

        String queryPattern = graphPattern.toString();
        String toReplace = StringUtils.substringBetween(queryPattern, "<", ">");
        while (toReplace != null) {
            StringBuilder temp = new StringBuilder();
            temp.append("<").append(toReplace).append(">");
            queryPattern = queryPattern.replace(temp.toString(), "[U]");
            toReplace = StringUtils.substringBetween(queryPattern, "<", ">");
        }

        String[] lines = StringUtils.split(queryPattern, "\n");
        StringBuilder newQueryPattern = new StringBuilder();
        for (String line : lines) {
            if (line.contains("FILTER ")) {
                int pos1 = line.indexOf("FILTER ");
                String lineToAdd = line.substring(0, pos1);

                newQueryPattern.append(lineToAdd).append("\n");
            } else {
                newQueryPattern.append(line).append("\n");
            }
        }

        lines = StringUtils.split(newQueryPattern.toString(), "\n");
        StringBuilder newQueryPattern2 = new StringBuilder();
        for (String line : lines) {
            if (line.contains("\"")) {
                int pos1 = line.indexOf("\"");
                int ppp2 = line.lastIndexOf("\"");
                int pos2 = line.indexOf(" ", ppp2);

                String toRepl;
                if (pos2 == -1) {
                    toRepl = line.substring(pos1);
                } else {
                    toRepl = line.substring(pos1, pos2);
                }
                String temp = line.replace(toRepl, "[L]");
                newQueryPattern2.append(temp).append("\n");
            } else {
                newQueryPattern2.append(line).append("\n");
            }
        }

        lines = StringUtils.split(newQueryPattern2.toString(), "\n");
        StringBuilder newQueryPattern3 = new StringBuilder();
        for (String line : lines) {
            int pos1 = line.indexOf("?");
            while (pos1 != -1) {

                int pos2 = line.indexOf(" ", pos1);
                int pos3 = line.indexOf(")", pos1);
                int pos4 = line.length();

                if (pos2 == -1) {
                    pos2 = pos4 + 10;
                }
                if (pos3 == -1) {
                    pos3 = pos4 + 10;
                }

                int min = pos2;
                if (pos3 < min) {
                    min = pos3;
                }
                if (pos4 < min) {
                    min = pos4;
                }

                String part1 = line.substring(0, pos1);
                String part2 = "[V]";
                String part3 = line.substring(min);
                StringBuilder lineb = new StringBuilder();
                lineb.append(part1).append(part2).append(part3);
                line = lineb.toString();
                pos1 = lineb.indexOf("?", pos1 + 1);
            }
            newQueryPattern3.append(line).append("\n");
        }
        queryPattern = newQueryPattern3.toString();

        queryPattern = queryPattern.replace(" a", " [U]").replaceAll("[_][:][A-Za-z0-9]+", "[B]").replace("true", "[L]")
                                   .replace("false", "[L]").replaceAll("[-+]?[0-9]*\\.?[0-9]+", "[L]");

        queryPattern = queryPattern.replace("\n", " ");
        while (queryPattern.contains("  ")) {
            queryPattern = queryPattern.replace("  ", " ");
        }
        queryPattern = queryPattern.trim();

        return queryPattern;
    }

    public static boolean isLinkedDataAnswerableBGP(Element graphPattern, boolean inUnion, HashSet<Node> boundVars) {

        if (boundVars == null) {
            boundVars = new HashSet<>();
        }
        HashSet<Node> localBoundVars = new HashSet<>();
        localBoundVars.addAll(boundVars);
        HashMap<Node, HashSet<Node>> var2sameTripleVars = new HashMap<>();

        ArrayList<TriplePath> triples = getTriples(graphPattern);
        for (TriplePath triple : triples) {
            Node subject = triple.getSubject();
            Node predicate = triple.getPredicate();
            Node object = triple.getObject();

            if (subject.isURI()) {
                if (object.isVariable()) {
                    localBoundVars.add(object);
                }
                if (predicate.isVariable()) {
                    localBoundVars.add(predicate);
                }
            } else if (object.isURI()) {
                if (subject.isVariable()) {
                    localBoundVars.add(subject);
                }
                if (predicate.isVariable()) {
                    localBoundVars.add(predicate);
                }
            } else if (subject.isVariable() && object.isVariable()) {
                if (localBoundVars.contains(subject)) {
                    localBoundVars.add(object);
                    if (predicate.isVariable()) {
                        localBoundVars.add(predicate);
                    }
                } else if (localBoundVars.contains(object)) {
                    localBoundVars.add(subject);
                    if (predicate.isVariable()) {
                        localBoundVars.add(predicate);
                    }
                } else {
                    if (var2sameTripleVars.containsKey(subject)) {
                        var2sameTripleVars.get(subject).add(object);
                    } else {
                        HashSet<Node> temp = new HashSet<>();
                        temp.add(object);
                        var2sameTripleVars.put(subject, temp);
                    }
                    if (var2sameTripleVars.containsKey(object)) {
                        var2sameTripleVars.get(object).add(subject);
                    } else {
                        HashSet<Node> temp = new HashSet<>();
                        temp.add(subject);
                        var2sameTripleVars.put(object, temp);
                    }
                    if (predicate.isVariable()) {
                        if (var2sameTripleVars.containsKey(predicate)) {
                            var2sameTripleVars.get(predicate).add(subject);
                            var2sameTripleVars.get(predicate).add(object);
                        } else {
                            HashSet<Node> temp = new HashSet<>();
                            temp.add(subject);
                            temp.add(object);
                            var2sameTripleVars.put(predicate, temp);
                        }
                    }
                }
            }
        }

        HashSet<Node> unbindableVariables = new HashSet<>();
        HashSet<Node> V = getAllVariables(graphPattern);
        for (Node v : V) {
            if (!localBoundVars.contains(v)) {
                HashSet<Node> aux = new HashSet<>();
                aux.add(v);

                boolean found = false;
                HashSet<Node> supportVars = new HashSet<>();
                if (var2sameTripleVars.get(v) != null) {
                    supportVars.addAll(var2sameTripleVars.get(v));
                }

                while (containsNewVariable(aux, supportVars)) {
                    HashSet<Node> varsToCheck = new HashSet<>();
                    varsToCheck.addAll(supportVars);
                    varsToCheck.removeAll(aux);

                    aux.addAll(supportVars);

                    for (Node vv : varsToCheck) {
                        if (localBoundVars.contains(vv)) {
                            found = true;
                            break;
                        } else {
                            supportVars.addAll(var2sameTripleVars.get(vv));
                        }
                    }
                    if (found) {
                        break;
                    }
                }
                if (!found) {
                    unbindableVariables.add(v);
                }
            }
        }

        if (!inUnion) {
            boundVars.addAll(localBoundVars);
        }

        return unbindableVariables.isEmpty();
    }

    public static boolean isLinkedDataAnswerableQuery(Element queryPattern) {
        ArrayList<Element> allElements = getTripleAndUnionElements(queryPattern);
        ArrayList<Element> pending = new ArrayList<>();
        HashSet<Node> B = new HashSet<>();

        for (Element el : allElements) {
            if (el.getClass() == ElementPathBlock.class) {
                boolean is = CheckAnswerability.isLinkedDataAnswerableBGP(el, false, B);
                if (!is) {
                    pending.add(el);
                }
            } else {
                List<Element> unionPatterns = ((ElementUnion) el).getElements();
                boolean allOk = true;
                HashSet<Node> unionVars = new HashSet<>();
                for (Element unionEl : unionPatterns) {
                    if (!CheckAnswerability.isLinkedDataAnswerableBGP(unionEl, true, B)) {
                        allOk = false;
                        break;
                    }
                    unionVars.addAll(CheckAnswerability.getAllVariables(unionEl));
                }
                if (allOk) {
                    B.addAll(unionVars);
                } else {
                    pending.add(el);
                }
            }
        }

        while (!pending.isEmpty()) {
            ArrayList<Element> toremove = new ArrayList<>();

            boolean newResolved = false;
            for (Element pendingEl : pending) {
                if (pendingEl.getClass() == ElementPathBlock.class) {
                    boolean is = CheckAnswerability.isLinkedDataAnswerableBGP(pendingEl, false, B);
                    if (is) {
                        newResolved = true;
                        toremove.add(pendingEl);
                    }
                } else {
                    List<Element> unionPatterns = ((ElementUnion) pendingEl).getElements();
                    boolean allOk = true;
                    HashSet<Node> unionVars = new HashSet<>();
                    for (Element unionEl : unionPatterns) {
                        if (!CheckAnswerability.isLinkedDataAnswerableBGP(unionEl, true, B)) {
                            allOk = false;
                            break;
                        }
                        unionVars.addAll(CheckAnswerability.getAllVariables(unionEl));
                    }
                    if (allOk) {
                        newResolved = true;
                        B.addAll(unionVars);
                        toremove.add(pendingEl);
                    }
                }
            }
            pending.removeAll(toremove);
            if (!newResolved) {
                return false;
            }
        }

        return true;
    }

    public static boolean containsNewVariable(HashSet<Node> main, HashSet<Node> coming) {
        for (Node n : coming) {
            if (!main.contains(n)) {
                return true;
            }
        }
        return false;

    }

    public static HashSet<Node> getAllVariables(Element graphPattern) {
        HashSet<Node> V = new HashSet<>();
        ElementWalker.walk(graphPattern, new ElementVisitorBase() {

            @Override
            public void visit(ElementPathBlock el) {
                ListIterator<TriplePath> triples = el.getPattern().iterator();
                while (triples.hasNext()) {
                    TriplePath tp = triples.next();

                    Node subject = tp.getSubject();
                    Node predicate = tp.getPredicate();
                    Node object = tp.getObject();

                    if (subject.isVariable()) {
                        V.add(subject);
                    }
                    if (predicate.isVariable()) {
                        V.add(predicate);
                    }
                    if (object.isVariable()) {
                        V.add(object);
                    }
                }
            }

        });
        return V;
    }

    public static ArrayList<Element> getTripleAndUnionElements(Element graphPattern) {
        ArrayList<Element> queryElements = getMainElements(graphPattern);

        ArrayList<Element> allElements = new ArrayList<>();
        for (Element element : queryElements) {
            fillListOfElements(element, allElements);
        }
        return allElements;
    }

    public static ArrayList<Element> getMainElements(Element graphPattern) {
        ArrayList<Element> elements = new ArrayList<>();
        ElementWalker.walk(graphPattern, new ElementVisitorBase() {
            @Override
            public void visit(ElementGroup group) {
                elements.clear();
                group.getElements().stream().forEach((ee) -> {
                    elements.add(ee);
                });

            }

        });

        return elements;
    }

    public static void fillListOfElements(Element element, ArrayList<Element> allElements) {
        if (element.getClass() == ElementPathBlock.class) {
            ArrayList<TriplePath> triplesEl = getTriples(element);
            for (TriplePath t : triplesEl) {
                ElementPathBlock singleTripleElement = new ElementPathBlock();
                singleTripleElement.addTriple(t);
                allElements.add(singleTripleElement);
            }
        } else if (element.getClass() == ElementUnion.class) {
            allElements.add(element);
        } else if (element.getClass() == ElementGroup.class) {
            List<Element> eelems = ((ElementGroup) element).getElements();
            for (Element eel : eelems) {
                fillListOfElements(eel, allElements);
            }
        } else if (element.getClass() == ElementOptional.class) {
            Element optEl = ((ElementOptional) element).getOptionalElement();
            fillListOfElements(optEl, allElements);
            //System.out.println(optEl.toString());
            //System.out.println("---");
            //System.out.println(element.toString());
        } else if (element.getClass() != ElementFilter.class) {
            System.out.println("*** NOT CONSIDERED ELEMENT: " + element.getClass().getName());
            System.out.println("    GRAPH ELEMENT: ");
            System.out.println(element.toString());
            System.out.println("=========================");
        }
    }

}
