package com.siriusxm.example.cart
import com.siriusxm.example.cart.ShoppingCart.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*
import zio.{ExitCode, Ref, Schedule, Scope, ZIO}

object ShoppingCartSpec extends zio.test.ZIOSpecDefault {
  val Tolerance = 0 // for sum comparisons
  val MaxPrice = 1E5
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("Shopping Cart Suite")(
    test("price lookup test: success") {
      ProductLookupApi.priceLookup("frosties")
        .map(p =>
          assertTrue(p > 0f))
    },

    test("price lookup test: invalid product") {
      val result = ProductLookupApi.priceLookup("*##?!")
      result.isFailure.flatMap(assertTrue(_))
    },

    test ("property test: random line items")(generateRandomLineItemsTest)
    @@ withLiveRandom @@ parallel @@ repeat(Schedule.recurs(10)),

  )

  private def generateRandomLineItemsTest =
    for {
      cart <- ShoppingCart.newCart
      lineCounter <- Ref.make(0)
      subtot <- Ref.make(BigDecimal(0))
      _ <- check(Gen.string.filter(_.nonEmpty), Gen.float.filter(p => p >= 0 && p < MaxPrice), Gen.int.filter(_>=0)) {
        (name, price, count) =>
        for
          _ <-
            cart.addLineItem(ProductInfo(name, price), count)
              *> cart.addLineItem(ProductInfo(name, price), 100)
              *> lineCounter.update(_ + 2)
              *> subtot.update(_ + BigDecimal(price) * (count + 100))
              .ignore
          currentLineCount <- lineCounter.get
          currentCartSize <- cart.numLines
        yield assertTrue(currentCartSize <= currentLineCount)
      }
      result <-
        for
          cartSize <- cart.numLines
          lineCount <- lineCounter.get
          cartSubTot <- cart.subtotal
          calcSubTot <- subtot.get
          _ <- ZIO.logInfo(s"line-count: $lineCount, cart-size: $cartSize, subTot = $cartSubTot")
        yield
          assertTrue(
            cartSize <= lineCount
            && (calcSubTot.setScale(DecimalScale, RoundingMode) - cartSubTot).abs <= Tolerance
          )
    } yield result
}
