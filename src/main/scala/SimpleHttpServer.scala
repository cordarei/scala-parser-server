/**
  * Inspiration/references:
  * - http://stackoverflow.com/questions/4161460/bootstrapping-a-web-server-in-scala/6432180#6432180
  * - http://matt.might.net/articles/pipelined-nonblocking-extensible-web-server-with-coroutines/ 
  *
  */


import java.io.{InputStreamReader, BufferedReader}
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}

import scala.collection.JavaConversions._


trait HttpMethod

case object GET extends HttpMethod

case object POST extends HttpMethod


case class HttpHeader(val name : String, val value : String)


case class HttpRequest(val headers : List[HttpHeader],
                       val uri : java.net.URI,
                       val method : HttpMethod,
                       val body : String)


case class HttpResponse(val headers : List[HttpHeader],
                        val code : Int = 200,
                        val mimeType : String = "",
                        val body : String = "")

case class InternalServerError(val statusCode : Int = 500,
                               val message : String = "",
                               val responseBody : String = "") extends Exception {
}


abstract class RequestHandler {
  def apply(request : HttpRequest, response : Option[HttpResponse]) : Option[HttpResponse]

  def ==> (handler : RequestHandler) : RequestHandler = {
    val that = this
    new RequestHandler {
      def apply(request : HttpRequest, response : Option[HttpResponse]) : Option[HttpResponse] = {
        val newResponse = that(request, response)
        handler(request, newResponse)
      }
    }
  }
}


/** A request handler to add some headers to all responses. Should come last. */
class AddHeaderRequestHandler(val headers : () => List[HttpHeader]) extends RequestHandler {
  def apply(request : HttpRequest, response : Option[HttpResponse]) : Option[HttpResponse] = {
    response match {
      case Some(r) => Some(r.copy(headers = r.headers ++ headers()))
      case None => None
    }
  }
}


class Default404RequestHandler extends RequestHandler {
  val defaultBody =
    """<html>
      <head>
      </head>
      <body>
        No matching resource.
      </body>
    </html>"""

  val defaultResponse = HttpResponse(headers = List(), code = 404, body = defaultBody)

  def apply(request : HttpRequest, response : Option[HttpResponse]) : Option[HttpResponse] = {
    response match {
      case Some(r) => response
      case None => Some(defaultResponse)
    }
  }
}


class RoutingRequestHandler extends RequestHandler {
  private val routeMaps = new collection.mutable.HashMap[String, (HttpRequest) => HttpResponse]()

  def get(path : String)(action : (HttpRequest) => HttpResponse) = routeMaps += path -> action

  def internalErrorResponse(ex : InternalServerError) = HttpResponse(
    List[HttpHeader](),
    ex.statusCode,
    ex.responseBody)

  def apply(request : HttpRequest, response : Option[HttpResponse]) : Option[HttpResponse] = {
    val path = request.uri.getPath
    routeMaps.get(path) match {
      case Some(action) => try {
        Some(action(request))
      } catch {
        case ex : InternalServerError => {
          println(ex.message)
          Some(internalErrorResponse(ex))
        }
      }
      case None => None
    }
  }
}


class SimpleHttpHandler(val handleRequests : RequestHandler) extends HttpHandler {

  def getVerb(ex : HttpExchange) = ex.getRequestMethod() match {
    case "GET" => GET
    case "POST" => POST
    case m => throw new Exception("Unsupported HTTP method: " + m)
  }

  def headersToList(hdrs : com.sun.net.httpserver.Headers) = 
    (hdrs.toList.map { kv => kv._2.map((v) => HttpHeader(kv._1, v)) }).flatten

  def getRequestBody(ex : HttpExchange) : String = {
    val s = scala.io.Source.fromInputStream(ex.getRequestBody())
    s mkString
  }

  def makeRequest(ex : HttpExchange) = HttpRequest(headers = headersToList(ex.getRequestHeaders()),
                                                   uri = ex.getRequestURI(),
                                                   method = getVerb(ex),
                                                   body = getRequestBody(ex))

  def handle(exchange : HttpExchange) {
    val response = handleRequests(makeRequest(exchange), None)
    response match {
      case Some(r) => {
        val bytesToSend = r.body.getBytes
        for (h <- r.headers) { exchange.getResponseHeaders().add(h.name, h.value) }
        exchange.sendResponseHeaders(r.code, bytesToSend.size)
        exchange.getResponseBody().write(bytesToSend)
        exchange.close()
      }
      case None => exchange.close()
    }
  }
}


class SimpleHttpServer(val address : InetSocketAddress, val handler : HttpHandler) {
  private val server = HttpServer.create(address, 0)
  server.createContext("/", handler)

  private val threadPool = Executors.newFixedThreadPool(10)
  server.setExecutor(threadPool)

  /** the real address the server is listening on; allows using port 0 in address */
  val listenAddress = server.getAddress()
  
  def start = server.start()

  def stop = server.stop(0)
}


object SimpleServerDemo {
  private val gmtFormat = 
    new java.text.SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", 
      java.util.Locale.US);

  gmtFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

  def runServer() {
    val globalHeaderGen = () => {
      List(HttpHeader("Server", "SimpleHttpServerDemo"),
        HttpHeader("Date", gmtFormat.format(new java.util.Date())))
    }

    val routing = new RoutingRequestHandler()
    routing.get("/test")((r) => HttpResponse(headers=List(), body="response from test/ route"))
    val default404 = new Default404RequestHandler()
    val addHeaders = new AddHeaderRequestHandler(globalHeaderGen)

    val handler = new SimpleHttpHandler(routing ==> default404 ==> addHeaders)

    val svr = new SimpleHttpServer(new InetSocketAddress(0), handler)

    println(svr.listenAddress.toString)

    svr.start
  }
}
