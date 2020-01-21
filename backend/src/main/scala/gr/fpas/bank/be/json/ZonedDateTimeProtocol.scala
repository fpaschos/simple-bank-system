package gr.fpas.bank.be.json

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

import spray.json.{DefaultJsonProtocol, JsNumber, JsValue, RootJsonFormat, deserializationError}

/**
 * Original source:
 *  https://github.com/xebialabs-community/xl-release-stress-tests/blob/master/data-generator/src/main/scala/com/xebialabs/xlrelease/json/ZonedDateTimeProtocol.scala
 */
private [json] trait ZonedDateTimeProtocol extends DefaultJsonProtocol {
  implicit object ZonedDateTimeProtocol extends RootJsonFormat[ZonedDateTime] {

    private val formatter = DateTimeFormatter.ISO_OFFSET_TIME.withZone(ZoneId.systemDefault)

    def write(obj: ZonedDateTime): JsValue = {
      JsNumber(obj.toInstant.toEpochMilli)
    }

    def read(json: JsValue): ZonedDateTime = json match {
      case JsNumber(s) => try {
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(s.longValue), ZoneId.systemDefault())
      } catch {
        case t: Throwable => error(s)
      }
      case _ =>
        error(json.toString())
    }

    def error(v: Any): ZonedDateTime = {
      val example = formatter.format(ZonedDateTime.now())
      deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }
  }
}