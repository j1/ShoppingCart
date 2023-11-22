package com.siriusxm.example.cart
import zio.{Ref, Schedule, Scope, ZIO}
import zio.test.*
import TestAspect.*

object ShoppingCartSpec extends zio.test.ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("Shopping Cart Suite")(
    test ("property test: random line items")(generateRandomLineItemsTest)
    @@ parallel @@ repeat(Schedule.recurs(10))

  )

  private def generateRandomLineItemsTest =
    for {
      entries <- Ref.make(emptyEntries)
      cart = new ShoppingCart(entries)
      lineCounter <- Ref.make(0)
      _ <- check(Gen.string, Gen.float, Gen.int) { (name, price, count) =>
        for
          _ <- cart.addLineItem(ProductInfo(name, price), count)
          _ <- cart.addLineItem(ProductInfo(name, price), count*2) // duplicate
          _ <- lineCounter.update(_ + 2)
          currentLineCount <- lineCounter.get
          currentCartSize <- cart.numLines
        yield assertTrue(currentCartSize <= currentLineCount)
      }
      result <-
        for
          cartSize <- cart.numLines
          lineCount <- lineCounter.get
          _ <- ZIO.logInfo(s"line-count: $lineCount, cart-size: $cartSize")
        yield
          assertTrue(cartSize <= lineCount) // account for duplicate line-items merged
    } yield result
}
