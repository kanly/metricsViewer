package it.posteitaliane.omp.data


case class Metric(
                   id: Long,
                   layer: String,
                   methodName: String,
                   serviceName: String,
                   frazionario: String,
                   pdl: String,
                   request: String,
                   success: Boolean,
                   startTime: Long,
                   endTime: Long,
                   errorMessage: String,
                   errorCode: String)

trait DTO

case class Workstation(frazionario: String, pdl: String) extends DTO

case class Request(request: String) extends DTO

case class Method(methodName: String) extends DTO

case class Service(serviceName: String) extends DTO

case class OmpError(code: String) extends DTO

sealed trait Data

case object WorkstationData extends Data

case object MethodData extends Data

case object ServiceData extends Data

case object ErrorData extends Data

object Keys {
  val serviceName = "name"
  val methodName = "name"
  val workstationFrazionario = "frazionario"
  val workstationPdl = "pdl"
  val errorCode = "code"
  val thrownByMessage = "message"
}