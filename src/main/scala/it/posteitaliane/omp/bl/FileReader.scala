package it.posteitaliane.omp.bl

import akka.actor.{Props, ActorRef, Actor}
import it.posteitaliane.omp.bl.FileReader._
import java.io.{File, RandomAccessFile}
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.bl.FileReader.FileParsed
import it.posteitaliane.omp.bl.JSONProcessor.LineRead
import it.posteitaliane.omp.bl.JSONProcessor.Line
import it.posteitaliane.omp.bl.ProductionEventSource.RegisterListener
import it.posteitaliane.omp.bl.FileReader.ProcessFile
import it.posteitaliane.omp.data.Metric
import it.posteitaliane.omp.bl.FileReader.NewMetric
import it.posteitaliane.omp.bl.FileReader.ProcessLine
import scala.Some


class FileReader extends Actor with Logging {
  this: EventSource =>
  var lineProcessor: ActorRef = context.system.deadLetters

  def receive = eventSourceReceiver orElse {
    case ProcessFile(filename) => self ! ProcessLine(new Reader(filename))
    case ProcessLine(reader) => {
      reader.next match {
        case Some(line) => {
          lineProcessor ! Line(line)
          self ! ProcessLine(reader)
        }
        case None => self ! FileParsed(reader.filePath)
      }
    }
    case LineRead(metric) => sendEvent(NewMetric(metric))
    case FileParsed(filename) => sendEvent(FileSubmitted(filename))

  }

  override def preStart() {
    lineProcessor = context.actorOf(JSONProcessor.props,"jsonizer")
    lineProcessor ! RegisterListener(self)
  }

}

object FileReader {

  def props = Props(new FileReader with ProductionEventSource)

  case class ProcessFile(filename: String)

  case class ProcessLine(file: Reader)

  case class FileParsed(filename: String)

  case class NewMetric(metric: Metric)

  case class FileSubmitted(filename: String)

}

class Reader(filename: String) {
  val raf = new RandomAccessFile(new File(filename), "rw")
  val filePath = filename

  def next: Option[String] = {
    if (raf.length() > raf.getFilePointer) Some(raf.readUTF())
    else None
  }
}


