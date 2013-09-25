package it.posteitaliane.omp.data


import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.kernel.impl.util.FileUtils
import java.io.File
import org.neo4j.graphdb.{Transaction, Relationship, RelationshipType, Node}
import com.typesafe.scalalogging.slf4j.Logging
import org.neo4j.graphdb.index.Index
import org.neo4j.cypher.javacompat.{ExecutionResult, ExecutionEngine}
import scala.collection.JavaConverters._

private object DAO {
  val dbPath = "target/data"
  val graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath)
  Runtime.getRuntime.addShutdownHook(ShutdownHook)
}

private object ShutdownHook extends Thread {
  override def run() {
    DAO.graphDB.shutdown()
  }
}

trait GraphDB extends Logging {
  private val db = DAO.graphDB
  private var tx: Option[Transaction] = None
  private lazy val indexManager = db.index()
  private val engine = new ExecutionEngine(db)

  def clearDB() {
    FileUtils.deleteRecursively(new File(DAO.dbPath))
  }

  def createNode(properties: Map[String, AnyRef] = Map()): Node = {
    val node = db.createNode()
    properties.foreach {
      case (key, value) => node.setProperty(key, value)
    }
    node
  }

  def cleanIndexes() {
    MWIndex.values.foreach(index => indexFor(index).delete())
  }

  def createOrLoadNode(index: MWIndex, properties: Map[String, String], onCreate: Node => Unit = Node => ()): Node = {
    val currIndex: Index[Node] = indexFor(index)
    val hits = currIndex.get(index.key, index.value)
    hits.size() match {
      case 0 => {
        val node = createNode(properties)
        currIndex.add(node, index.key, index.value)
        onCreate(node)
        node
      }
      case 1 => hits.getSingle
      case x => {
        logger.warn(s"${x} element for index ${index.toString}")
        hits.getSingle
      }
    }
  }


  def addRelationship(firstNode: Node, secondNode: Node, relationshipType: RelTypes, properties: Map[String, AnyRef] = Map()): Relationship = {
    val relationship = firstNode.createRelationshipTo(secondNode, relationshipType)
    properties.foreach {
      case (key, value) => relationship.setProperty(key, value)
    }
    relationship
  }

  def executeQuery(query: String): List[Map[String, AnyRef]] = {
    asScala(engine.execute(query))
  }

  private def asScala(execResult: ExecutionResult): List[Map[String, AnyRef]] = execResult.iterator().asScala.map(_.asScala.toMap).toList

  def indexFor(index: MWIndex): Index[Node] = {
    indexManager.forNodes(index.key)
  }

  def beginTx() {
    tx = Some(db.beginTx())
  }

  def successTx() {
    tx match {
      case Some(transaction) => transaction.success()
      case None => logger.warn("Invoked success on an non-existent transaction")
    }
  }

  def finishTx() {
    tx match {
      case Some(transaction) => {
        transaction.finish()
        tx = None
      }
      case None => logger.warn("Invoked finish on an non-existent transaction")
    }
  }

}

sealed abstract class MWIndex(indexName: String, v: AnyRef) {
  val key = indexName
  val value = v
}

object MWIndex {
  val values = Set(RequestIndex(), MethodIndex(), ServiceIndex(), PostalOfficeIndex(), WorkStationIndex(), ErrorIndex())
}

case class RequestIndex(request: String = "") extends MWIndex("request", request)

case class MethodIndex(methodName: String = "") extends MWIndex("method", methodName)

case class ServiceIndex(serviceName: String = "") extends MWIndex("service", serviceName)

case class PostalOfficeIndex(frazionario: String = "") extends MWIndex("postalOffice", frazionario)

case class WorkStationIndex(frazionarioPdl: (String, String) = ("", "")) extends MWIndex("workstation", frazionarioPdl)

case class ErrorIndex(code: String = "") extends MWIndex("error", code)

sealed abstract class RelTypes(relationshipName: String) extends RelationshipType {
  def name = relationshipName
}

case object Execute extends RelTypes("Execute")

case object ExecutedBy extends RelTypes("ExecutedBy")

case object Own extends RelTypes("Own")

case object ThrownBy extends RelTypes("ThrownBy")