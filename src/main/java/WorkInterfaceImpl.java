import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Created by UA C on 18.09.2017.
 */
public class WorkInterfaceImpl implements WorkInterface {
    private static String DB_PATH = "var/myDb";
    private GraphDatabaseService graphDb ;
    private final int MAX_OCCURRENCES = 2;
    private final int MAX_GRAPH_SIZE = Lbl.values().length*MAX_OCCURRENCES;
    private int countNodes = 0;

    public WorkInterfaceImpl() {
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(DB_PATH));
        registerShutdownHook(graphDb);
    }
    public WorkInterfaceImpl(String DB_PATH) {
        this.DB_PATH = DB_PATH;
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(DB_PATH));
        registerShutdownHook(graphDb);
    }

    @Override
    public void cleanDb() {
        try (Transaction tx = graphDb.beginTx()) {
            graphDb.execute("MATCH (n)\n" +
                    "OPTIONAL MATCH (n)-[r]-()\n" +
                    "DELETE n,r");
            tx.success();
        }
    }

    @Override
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

    @Override
    public void exploreGraph(Node root) {
        if(root == null){
            throw new NullPointerException("root shouldn't be null");
        }
        ArrayList<Node> nodeList = new ArrayList<>();
        nodeList.add(root);
        DBGenerator dbGenerator = new DBGenerator(this);
        dbGenerator.createNodeTree(nodeList);
    }

    @Override
    public void print(Node root, Consumer<Path> consumer) {
        DbPrinter dbPrinter = new DbPrinter(graphDb,root);
        dbPrinter.dbPrint(consumer);
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        Runtime.getRuntime().addShutdownHook(new Thread(()->{graphDb.shutdown();
            new ZipUtils(DB_PATH,DB_PATH+".zip");}) );
    }

    public int getCountNodes() {
        return countNodes;
    }

    public int getMAX_OCCURRENCES() {
        return MAX_OCCURRENCES;
    }

    public int getMAX_GRAPH_SIZE() {
        return MAX_GRAPH_SIZE;
    }

    public void setCountNodes(int countNodes) {
        this.countNodes = countNodes;
    }

    public static String getDbPath() {
        return DB_PATH;
    }

    public GraphDatabaseService getGraphDb() {
        return graphDb;
    }
}
