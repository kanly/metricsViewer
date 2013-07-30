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


case class Workstation(frazionario: String, pdl: String)

case class Request(request: String)

case class Method(methodName: String)

case class Service(serviceName: String)

case class Error(code: String)

sealed trait Data
case object  Workstation extends Data
case object  Method extends Data
case object  Service extends Data
case object  Error extends Data