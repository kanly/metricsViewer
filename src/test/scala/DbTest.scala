import org.neo4j.cypher.javacompat.{ExecutionResult, ExecutionEngine}
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.Node

object DbTest extends App {

  val graphDB = new GraphDatabaseFactory().newEmbeddedDatabase("/home/kanly/projects/metricsViewer/target/data")
  Runtime.getRuntime.addShutdownHook(ShutdownHook)

  val engine = new ExecutionEngine(graphDB)
  val result: ExecutionResult = engine.execute( """START wor=node:workstation('*:*') RETURN wor""")
  val kindsOfFruit = result.columnAs("wor")
  while (kindsOfFruit.hasNext()) {
    Node kindOfFruit = kindsOfFruit.next();
    System.out.println("Kind #" + kindOfFruit.getId());
    for (String propertyKey : kindOfFruit.getPropertyKeys()) {
      System.out.println("\t" + propertyKey + " : " +
        kindOfFruit.getProperty(propertyKey));
    }
  }
}

object ShutdownHook extends Thread {
  override def run() {
    DbTest.graphDB.shutdown()
  }
}
