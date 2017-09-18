import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import java.util.function.Consumer;

/**
 * Created by UA C on 18.09.2017.
 */
public class DbPrinter {
    private GraphDatabaseService graphDb;
    private Node root;

    public DbPrinter(GraphDatabaseService graphDb, Node root) {
        this.graphDb = graphDb;
        this.root = root;
    }

    public void dbPrint(Consumer<Path> consumer){
        try (Transaction tx = graphDb.beginTx()) {
            Traverser traverseBFS = traverseBFS(root);
            traverseBFS.forEach(consumer::accept);
            System.out.println();
            tx.success();
        }
    }
    private Traverser traverseBFS(Node root) {
        TraversalDescription td = graphDb.traversalDescription().breadthFirst()
                .relationships(Rls.RELATE, Direction.OUTGOING)
                .relationships(Rls.HAS, Direction.OUTGOING)
                .relationships(Rls.CONTAINS, Direction.OUTGOING);
        return td.traverse(root);
    }
}
