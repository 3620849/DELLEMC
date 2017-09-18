import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by UA C on 18.09.2017.
 */
public class DBGenerator {
    WorkInterfaceImpl work;
    public DBGenerator(WorkInterfaceImpl work) {
    this.work= work;
    }

    public void createNodeTree(ArrayList<Node> nodeList) {
        if (nodeList.isEmpty()) return;
        if (work.getCountNodes()>= work.getMAX_GRAPH_SIZE()) return;
        nodeList = createRandRelatedNodes(nodeList);
        setRelationsBetweenExistNodes(nodeList);
        createNodeTree(nodeList);
    }

    private ArrayList<Node> createRandRelatedNodes(ArrayList<Node> nodeList) {
        ArrayList<Node> result = new ArrayList<>();
        try (Transaction tx = work.getGraphDb().beginTx()) {
            for (Node n : nodeList) {
                Label nLabel = n.getLabels().iterator().next();
                int maxChilds = Lbl.getMaxChilds(nLabel);
                int childWillbeCreate = getRandom(maxChilds);
                for (int i = 0; i < childWillbeCreate; ++i) {
                    if (work.getCountNodes()< work.getMAX_GRAPH_SIZE()) {
                        Lbl randomLbl = Lbl.getRandomFor(nLabel);
                        if (randomLbl != null) {
                            if (randomLbl.count < work.getMAX_OCCURRENCES()) {
                                Node newNode = work.getGraphDb().createNode();
                                newNode.addLabel(randomLbl);
                                int idnum = randomLbl.count + 1;
                                newNode.setProperty("id", randomLbl.name().toLowerCase() + idnum);
                                newNode.setProperty("type", randomLbl.name());
                                result.add(newNode);
                                ++randomLbl.count;
                                work.setCountNodes(work.getCountNodes()+1);
                                n.createRelationshipTo(newNode, Rls.getType(nLabel.name(), randomLbl.name()));
                            }
                        }
                    } else {
                        return result;
                    }
                }
            }
            tx.success();
        }
        return result;
    }
    private void setRelationsBetweenExistNodes(ArrayList<Node> nodeList) {
        try (Transaction tx = work.getGraphDb().beginTx()) {
            ResourceIterable<Node> allNodes = work.getGraphDb().getAllNodes();
            for (Node currentN : nodeList) {
                Node next = allNodes.iterator().next();
                if (!next.getProperty("id").equals(currentN.getProperty("id"))) {
                    String currentLbl = currentN.getLabels().iterator().next().name();
                    String nextLabel = next.getLabels().iterator().next().name();
                    Rls type = Rls.getType(currentLbl, nextLabel);
                    if (type != null) {
                        currentN.createRelationshipTo(next, type);
                    }
                    tx.success();
                }
            }
        }
    }
    private static int getRandom(int a) {
        if (a <= 0) return 0;
        return ThreadLocalRandom.current().nextInt(0, a);
    }
}
