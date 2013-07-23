package it.posteitaliane.omp.data

case class Metric(
                   id: Long,
                   layer:String,
                   methodName: String,
                   serviceName: String,
                   frazionario: String,
                   pdl: String,
                   request: String,
                   success: Boolean,
                   startTime: Long,
                   endTime: Long,
                   errorMessage: String,
                   errorCode:String)
/*
   "errorMessage":"",
   "layer":"FE",
   "endTime":1374497639989,
   "pdl":2,
   "serviceName":"interfacciadispacciattesi",
   "id":null,
   "timestamp":"Mon Jul 22 14:53:59 CEST 2013",
   "startTime":1374497639976,
   "contestoSDP":"{\"codiceFrazionario\":\"55218\",\"pdlFisica\":2,\"dataContabile\":\"2013-07-22T14:53:51.192Z\",\"userIdOperatore\":\"Pippo\",\"progressivoPDL\":1,\"profiloOperatore\":1,\"progressivoComparto\":10000,\"codiceFase\":\"XONI\",\"codiceCanale\":\"SPO\"}",
   "request":"{\"pragma\":\"no-cache\",\"Cache-Control\":\"no-cache\"}",
   "errorCode":"",
   "frazionario":"55218",
   "methodName":"infodispacciattesi",
   "success":true
 */
