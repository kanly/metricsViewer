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

case class Request(request: String, startTime:Long, endTime:Long, layer:String, success:String) extends DTO

case class Method(methodName: String) extends DTO

case class Service(serviceName: String) extends DTO

case class OmpError(code: String) extends DTO

case class RequestView(ws: Workstation, method: Method, service: Service, error: OmpError, request: Request)

sealed trait Data

case object WorkstationData extends Data

case object MethodData extends Data

case object ServiceData extends Data

case object ErrorData extends Data

case object RequestData extends Data

object Keys {
  val serviceName = "name"
  val methodName = "name"
  val workstationFrazionario = "frazionario"
  val workstationPdl = "pdl"
  val errorCode = "code"
  val thrownByMessage = "message"
  val request="request"
  val requestStart="startTime"
  val requestEnd="endTime"
  val requestSuccess="success"
  val requestLayer="layer"
}