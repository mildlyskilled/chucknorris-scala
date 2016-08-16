import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.stream.ActorMaterializer
import com.mildlyskilled.model._
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JObject
import org.json4s.native.JsonMethods._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

object ChuckNorris {

  def main(args: Array[String]): Unit = {

    var uri = "http://api.icndb.com/jokes"
    val randomRegex = "--random=[\\d+]{1,3}".r
    val idRegex = "--id=[\\d+]{1,5}".r

    implicit val formats = DefaultFormats

    def extractItem(args: Array[String], regex: Regex): Int = {
      args.find(opt => regex.pattern.matcher(opt).matches()) match {
        case Some(item) => item.split("=").tail.head.toInt
        case None => 0
      }
    }

    def jsonMapper(json: String, multiple: Boolean): Try[ChuckNorrisEntry] = {
      {
        if (multiple) {
          Try(parse(json).extract[JokeEntry])
        } else {
          Try(parse(json).extract[JokeEntries])
        }
      }
    }

    def help: List[String] = {
      List(
        "--help: Display this help message and quit",
        "--random=<number>: Retrieve <number> of random jokes",
        "--id=<number>: Retrieve joke with id = <number>"
      )
    }

    if (args.contains("--help")) {
      help foreach println
    } else {

      val randomCount = extractItem(args, randomRegex)

      uri = randomCount match {
        case 0 => uri
        case _ => s"$uri/random/${randomCount.toString}"
      }

      if (args.contains("-r")) {
        uri = s"$uri/random/1"
      }

      val jokeId = extractItem(args, idRegex)
      if (jokeId > 0) {
        uri = s"$uri/${jokeId.toString}"
      }

      implicit val system = ActorSystem()
      implicit val materializer = ActorMaterializer()
      implicit val executionContext = system.dispatcher
      val timeout = 300.millis

      val httpRequest: HttpRequest = HttpRequest(
        HttpMethods.GET,
        Uri(uri)
      )

      val request = Http().singleRequest(httpRequest)

      request.flatMap { response =>
        if (response.status.intValue() != 200) {
          println(response.entity.dataBytes.runForeach(_.utf8String))
          Future(Unit)
        } else {
          response.entity.dataBytes
            .scan("")((acc, curr) => if (acc.contains("\r\n")) curr.utf8String else acc + curr.utf8String)
            .map(json => jsonMapper(json, jokeId > 0))
            .runForeach {
              case Success(entry) => println(entry)
              case Failure(x) => x.getMessage
            }
        }
      }
    }
  }
}
