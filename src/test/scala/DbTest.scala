import org.neo4j.cypher.javacompat.{ExecutionResult, ExecutionEngine}
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.{Relationship, Node}
import scala.collection.JavaConverters._

object DbTest extends App {

  val graphDB = new GraphDatabaseFactory().newEmbeddedDatabase("/home/kanly/projects/metricsViewer/target/data")
  Runtime.getRuntime.addShutdownHook(ShutdownHook)
  val workstationToRequest = """START wor=node:workstation('*:*') MATCH wor-[exe:Execute]->req RETURN wor,exe,req"""
  val workstations = """START wor=node:workstation('*:*') RETURN wor"""
  val engine = new ExecutionEngine(graphDB)
  val result: ExecutionResult = engine.execute(workstationToRequest)

  val list: List[Map[String, AnyRef]] = result.iterator().asScala.map(_.asScala.toMap).toList

  println(list.mkString("\n"))

  val node = list(1)("wor").asInstanceOf[Node]
  println(node.getProperty("frazionario"))
  println(node.getProperty("pdl"))



  list.foreach(nod => {
    println(nod)
    println()
    nod.foreach{
      case(key,element)=>{
        println(s"Element: $key")
        element match {
          case n:Node => printNodeContent(n)
          case r:Relationship => printRelationship(r)
        }

      }
    }
    println("""**************************************************""")
  })

  def printNodeContent(node: Node) {
    println(s"nodeId:${node.getId}")
    node.getPropertyKeys.asScala.foreach(key => println(s"$key => ${node.getProperty(key)}"))
    println("""---------------------------------""")
  }

  def printRelationship(relationship: Relationship) {
    println(s"${relationship.getType.name} relationship from ${relationship.getStartNode.getId} to ${relationship.getEndNode.getId}. Id: ${relationship.getId}. ")
    relationship.getPropertyKeys.asScala.foreach(key =>  println(s"$key => ${relationship.getProperty(key)}"))
    println("""---------------------------------""")
  }

}

object ShutdownHook extends Thread {
  override def run() {
    DbTest.graphDB.shutdown()
  }
}
