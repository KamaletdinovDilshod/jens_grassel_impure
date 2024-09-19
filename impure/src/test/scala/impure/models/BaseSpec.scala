package impure.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

abstract class BaseSpec extends AnyWordSpec with Matchers with ScalaCheckDrivenPropertyChecks {}
