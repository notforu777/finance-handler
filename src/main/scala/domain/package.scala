import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.Read
import io.estatico.newtype.macros.newtype
import sttp.tapir.{Codec, CodecFormat, Schema}
import tofu.logging.derivation.loggable

package object domain {
  @derive(loggable, encoder, decoder)
  @newtype
  case class ExpId(value: Long)

  object ExpId {
    implicit val read: Read[ExpId] = Read[Long].map(ExpId.apply)
    implicit val schema: Schema[ExpId] =
      Schema.schemaForLong.map(long => Some(ExpId(long)))(_.value)
    implicit val codec: Codec[String, ExpId, CodecFormat.TextPlain] =
      Codec.long.map(ExpId(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class ExpDescr(value: String)

  object ExpDescr {
    implicit val read: Read[ExpDescr] = Read[String].map(ExpDescr.apply)
    implicit val schema: Schema[ExpDescr] =
      Schema.schemaForString.map(string => Some(ExpDescr(string)))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class ExpAm(value: Double)

  object ExpAm {
    implicit val read: Read[ExpId] = Read[Double].map(ExpAm.apply)
    implicit val schema: Schema[ExpAm] =
      Schema.schemaForDouble.map(long => Some(ExpAm(long)))(_.value)
    implicit val codec: Codec[String, ExpAm, CodecFormat.TextPlain] =
      Codec.double.map(ExpAm(_))(_.value)
  }

}
