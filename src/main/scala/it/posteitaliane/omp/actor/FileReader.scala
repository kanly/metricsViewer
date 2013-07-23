package it.posteitaliane.omp.actor

import akka.actor.{Props, ActorRef, Actor}
import it.posteitaliane.omp.actor.FileReader.{FileParsed, ProcessLine, LineRead, ProcessFile}
import java.io.{File, RandomAccessFile}
import it.posteitaliane.omp.actor.LineProcessor.Line
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.data.Metric
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import it.posteitaliane.omp.actor.MetricViewer.NewMetric


class FileReader extends Actor with Logging {
  var lineProcessor: ActorRef = context.system.deadLetters

  def receive = {
    case ProcessFile(filename) => self ! ProcessLine(new Reader(filename))
    case ProcessLine(reader) => {
      reader.next match {
        case Some(line) => {
          lineProcessor ! Line(line)
          self ! ProcessLine(reader)
        }
        case None => self ! FileParsed(reader.filepath)
      }
    }
    case LineRead(metric) => context.parent ! NewMetric(metric)

  }

  override def preStart() {
    lineProcessor = context.actorOf(LineProcessor.props)
  }

}

object FileReader {

  def props = Props(new FileReader)

  case class ProcessFile(filename: String)

  case class LineRead(line: Metric)

  case class ProcessLine(file: Reader)

  case class FileParsed(filename: String)

}

class Reader(filename: String) {
  val raf = new RandomAccessFile(new File(filename), "rw")
  val filepath = filename

  def next: Option[String] = {
    if (raf.length() > raf.getFilePointer) Some(raf.readUTF())
    else None
  }
}

class LineProcessor extends Actor with Logging {

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

  def receive = {
    case Line(line) => {
      val value: Metric = mapper.readValue(line, classOf[Metric])
      sender ! LineRead(value)
    }

  }

}

object LineProcessor {

  case class Line(line: String)

  def props = Props(new LineProcessor)

}
