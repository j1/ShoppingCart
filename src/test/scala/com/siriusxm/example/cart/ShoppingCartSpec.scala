package com.siriusxm.example.cart
import zio.{Ref, Schedule, Scope, ZIO}
import zio.test.*
import TestAspect.*

object ShoppingCartSpec extends zio.test.ZIOSpecDefault {
  val Tolerance = 1E-10 // for sum comparisons
  val MaxPrice = 1E5
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("Shopping Cart Suite")(
    test("price lookup test") {
      val priceXor = ProductLookupApi.priceLookup("frosties")
      assertTrue(priceXor.isRight && (priceXor.getOrElse(0f) > 0f))
    },

    test ("property test: random line items")(generateRandomLineItemsTest)
    @@ withLiveRandom @@ parallel @@ repeat(Schedule.recurs(10)),

  )

  private def generateRandomLineItemsTest =
    for {
      entries <- Ref.make(emptyEntries)
      cart = new ShoppingCart(entries)
      lineCounter <- Ref.make(0)
      subtot <- Ref.make(BigDecimal(0))
      _ <- check(Gen.string, Gen.float.filter(p => p >= 0 && p < MaxPrice), Gen.int.filter(_>=0)) { (name, price, count) =>
        for
          _ <- cart.addLineItem(ProductInfo(name, price), count)
          _ <- cart.addLineItem(ProductInfo(name, price),  100) // duplicate
          _ <- lineCounter.update(_ + 2)
          _ <- subtot.update(_ +  price * (count + 100))
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
            && (calcSubTot - cartSubTot).abs < Tolerance
          )
    } yield result
}
