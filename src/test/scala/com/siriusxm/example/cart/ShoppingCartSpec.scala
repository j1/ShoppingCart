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
    test("ReadMe sample data test") {
      for
        cart <- ShoppingCart.newCart
        _ <- cart.addLineItem("cornflakes", 2)
        _ <- cart.addLineItem("weetabix", 1)
        subtotal <- cart.subtotal
        tax <- cart.taxPayable
        total <- cart.totalPayable
      yield assertTrue((subtotal, tax, total) == (15.02, 1.88, 16.90))
      /*
      Add
      2 × cornflakes
      @2.52 each
      Add
      1 × weetabix
      @9.98 each
      Subtotal = 15.02
      Tax = 1.88
      Total = 16.90
       */
    },

    test ("property test: random line items")(generateRandomLineItemsTest)
    @@ withLiveRandom @@ parallel @@ repeat(Schedule.recurs(10)),

    test("price lookup test: success") {
      ProductLookupApi.priceLookup("frosties")
        .map(p =>
          assertTrue(p > 0f))
    },

    test("price lookup test: invalid product") {
      val result = ProductLookupApi.priceLookup("*##?!")
      result.isFailure.flatMap(assertTrue(_))
    },

    test("attempt to change price in the middle of cart") {
      val result = for
        cart <- ShoppingCart.newCart
        _ <- cart.addLineItem(ProductInfo("pencil", 1.0), 5)
        _ <- cart.addLineItem(ProductInfo("pencil", 2.0), 5)
      yield ()
      result.isFailure.flatMap(assertTrue(_))
    },
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
