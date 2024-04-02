package domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import sttp.tapir.derevo.schema
import tofu.logging.derivation.loggable

@derive(loggable, encoder, decoder, schema)
final case class Expense(id: ExpId, description: ExpDescr, amount: ExpAm)