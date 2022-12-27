package impure.models

import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

abstract class BaseSpec extends WordSpec with MustMatchers with ScalaCheckPropertyChecks {}