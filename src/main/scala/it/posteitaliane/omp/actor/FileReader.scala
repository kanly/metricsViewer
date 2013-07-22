package it.posteitaliane.omp.actor

import akka.actor.{Props, ActorRef, Actor}
import it.posteitaliane.omp.actor.FileReader.ProcessFile
import java.io.{File, RandomAccessFile}
import it.posteitaliane.omp.actor.LineProcessor.Line
import com.typesafe.scalalogging.slf4j.Logging


class FileReader extends Actor with Logging {
  var lineProcessor: ActorRef = context.system.deadLetters

  def receive = {
    case ProcessFile(filename) => {
      val raf = new RandomAccessFile(new File(filename), "rw")
      raf.seek(0)
      while (raf.length() > raf.getFilePointer) {
        val line = raf readUTF()
        lineProcessor ! Line(line)
      }
    }
  }

  override def preStart() {
    lineProcessor = context.actorOf(LineProcessor.props)
  }

}

object FileReader {

  case class ProcessFile(filename: String)


}

class LineProcessor extends Actor with Logging {
  def receive = {
    case Line(line) => {
      // TODO process and save line
      logger.info(line)
    }

  }

}

object LineProcessor {

  case class Line(line: String)

  def props = Props(new LineProcessor)

}
