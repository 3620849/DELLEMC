import org.neo4j.graphdb.*;


public class App1 {

     public static void main(String[] args) {
         WorkInterface app = new WorkInterfaceImpl("var/myDb");
        app.cleanDb();
        Node root = app.createRootNode();
        app.exploreGraph(root);
        app.print(root,node ->  System.out.print("<"+node.endNode().getProperty("id")+"> "));
        app.print(root,node ->  System.out.print("{\"id\":\"" + node.endNode().getProperty("id") + "\",\"type\":\"" +
                node.endNode().getProperty("type") + "\"} "));
    }

}
