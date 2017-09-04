import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class App1 {
    //DB settings
    private File file = new File("var/myDb");
    private GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(file);
    private final int MAX_OCCURRENCES = 2;
    private final int MAX_GRAPH_SIZE = Lbl.values().length*MAX_OCCURRENCES;
    private int countNodes = 0;

    public static void main(String[] args) {
        App1 app = new App1();
        app.cleanDb();
        Node root = app.createRootNode();
        app.exploreGraph(root);
        app.print(root, 0);
        app.print(root, 1);
    }

    public void exploreGraph(Node root) {
        registerShutdownHook(graphDb);
        if(root == null){
            throw new NullPointerException("root shouldn't be null");
        }
        ArrayList<Node> nodeList = new ArrayList<>();
        nodeList.add(root);
        createNodeTree(nodeList);
    }
    private void createNodeTree(ArrayList<Node> nodeList) {
        if (nodeList.isEmpty()) return;
        if (countNodes >= MAX_GRAPH_SIZE) return;
        nodeList = createRandRelatedNodes(nodeList);
        setRelationsBetweenExistNodes(nodeList);
        createNodeTree(nodeList);
    }
    private void setRelationsBetweenExistNodes(ArrayList<Node> nodeList) {
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterable<Node> allNodes = graphDb.getAllNodes();
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
    private ArrayList<Node> createRandRelatedNodes(ArrayList<Node> nodeList) {
        ArrayList<Node> result = new ArrayList<>();
        try (Transaction tx = graphDb.beginTx()) {
            for (Node n : nodeList) {
                Label nLabel = n.getLabels().iterator().next();
                int maxChilds = Lbl.getMaxChilds(nLabel);
                int childWillbeCreate = getRandom(maxChilds);
                for (int i = 0; i < childWillbeCreate; ++i) {
                    if (countNodes < MAX_GRAPH_SIZE) {
                        Lbl randomLbl = Lbl.getRandomFor(nLabel);
                        if (randomLbl != null) {
                            if (randomLbl.count < MAX_OCCURRENCES) {
                                Node newNode = graphDb.createNode();
                                newNode.addLabel(randomLbl);
                                int idnum = randomLbl.count + 1;
                                newNode.setProperty("id", randomLbl.name().toLowerCase() + idnum);
                                newNode.setProperty("type", randomLbl.name());
                                result.add(newNode);
                                ++randomLbl.count;
                                ++countNodes;
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
    public void print(Node root, int type) {

        if(type<0 || type>1){
            throw new IllegalArgumentException("argument type should be 0 or 1");
        }
        try (Transaction tx = graphDb.beginTx()) {
            Traverser traverseBFS = traverseBFS(root);
            String res = "";
            switch (type) {
                case 0:
                    for (Path nodePath : traverseBFS) {
                        res += "<" + nodePath.endNode().getProperty("id") + "> ";
                    }
                    break;
                case 1:
                    for (Path nodePath : traverseBFS) {
                        res += "{\"id\":\"" + nodePath.endNode().getProperty("id") + "\",\"type\":\"" +
                                nodePath.endNode().getProperty("type") + "\"} ";
                    }
                    break;
            }
            System.out.println(res);
            tx.success();

        }
    }
    private Traverser traverseBFS(Node root) {
        TraversalDescription td = graphDb.traversalDescription().breadthFirst()
                .relationships(Rls.RELATE, Direction.OUTGOING)
                .relationships(Rls.HAS, Direction.OUTGOING)
                .relationships(Rls.CONTAINS, Direction.OUTGOING)
                /*.sort((o1,o2)->(int) (o1.endNode().getId()-o2.endNode().getId()))*/;

        return td.traverse(root);
    }
    public Node createRootNode() {
        Node root;
        try (Transaction tx = graphDb.beginTx()) {
            root = graphDb.createNode();
            root.addLabel(Lbl.A);
            root.setProperty("id", "a1");
            root.setProperty("type", root.getLabels().iterator().next().name());
            tx.success();
        }
        Lbl.A.count++;
        countNodes++;
        return root;
    }
    private void cleanDb() {
        try (Transaction tx = graphDb.beginTx()) {
            graphDb.execute("MATCH (n)\n" +
                    "OPTIONAL MATCH (n)-[r]-()\n" +
                    "DELETE n,r");
            tx.success();
        }
    }
    private static int getRandom(int a) {
        if (a <= 0) return 0;
        return ThreadLocalRandom.current().nextInt(0, a);
    }
    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
                new ZipUtils("var/myDb","zip/myDb.zip");
            }
        } );
    }
}
