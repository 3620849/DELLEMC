import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;

import java.util.function.Consumer;

/**
 * Created by UA C on 18.09.2017.
 */
public interface WorkInterface {
        void cleanDb();
        Node createRootNode();
        void exploreGraph(Node root);
        void print(Node root, Consumer<Path> consumer);
}
