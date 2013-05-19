import java.net.InetSocketAddress

import scala.util.parsing.json.{JSONArray, JSONObject}


class QueryString(val fromURI : java.net.URI) {
  val string : String = fromURI.getQuery
  val fields : Map[String, String] = extractFields(fromURI)

  private def extractFields(uri : java.net.URI) : Map[String, String] = {
    if (uri.getRawQuery().contains("&")) {
      Map()
    } else {
      Map()
    }
  }
}


object ParseResultToJson {
  /**
    * The result should look like:
    * {
    *  tokens : [],
    *  pos : [],
    *  rels : []
    * }
    */
  def apply(parseResult : ParseResult) : String = {
    val tokenStrings = parseResult.tokens.map(t => t.token)
    val tagStrings = parseResult.tokens.map(t => t.tag match { case Some(s) => s; case None => "" })

    val tokens = JSONArray(tokenStrings)
    val pos = JSONArray(tagStrings)
    val rels = JSONArray(
      parseResult.rels.map(gr => 
        JSONArray(List(gr.relation, gr.headTokenIndex, gr.depTokenIndex))))
    val obj = JSONObject(Map("tokens" -> tokens, "pos" -> pos, "rels" -> rels))

    return obj.toString
  }
}


class ParserServer {

  val parser = new Parser
  val server = createServer


  def handleParseRequest(request : HttpRequest) : HttpResponse = {
    val query = new QueryString(request.uri)
    val sentence = query.string
    val parseResult = parser.parse(sentence)
    val json = parseResult match {
      case Right(result) => ParseResultToJson(result)
      case Left(error) => throw new Exception("Parse error!")
    }

    HttpResponse(headers=List(HttpHeader("Content-Type", "application/json")),
                 code=200,
                 mimeType="application/json",
                 body=json)
  }

  def start() = server.start

  def stop() = server.stop

  private def createServer() : SimpleHttpServer = {
    val default404 = new Default404RequestHandler
    val serverHeaders = new AddHeaderRequestHandler(globalHeaders)
    val routing = new RoutingRequestHandler

    routing.get("/parse")(handleParseRequest)
    routing.get("/test")(r => HttpResponse(headers=List(), code=200, body="test 1-2-3"))
    routing.get("/error")(r => throw new Exception("An error occurred"))

    val handler = new SimpleHttpHandler(routing ==> default404 ==> serverHeaders)
    new SimpleHttpServer(new InetSocketAddress(0), handler)
  }

  private val dateFormat = new java.text.SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", java.util.Locale.US);
  dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
  private def globalHeaders() : List[HttpHeader] = {
    List(HttpHeader("Server", "ParserServer 0.1"),
         HttpHeader("Date", dateFormat.format(new java.util.Date())))
  }
}


object Main {
    def main(args: Array[String]) {
      val server = new ParserServer
      println("Listening on port: " + server.server.listenAddress.getPort().toString())
      server.start
    }
}
