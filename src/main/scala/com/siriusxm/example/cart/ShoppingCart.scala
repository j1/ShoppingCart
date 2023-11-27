package com.siriusxm.example.cart

import zio.*

object ShoppingCart:
  //Tax payable, charged at 12.5% on the subtotal
  val TaxRate = 0.125d
  val DecimalScale = 2 // money rounded to 2 decimal places
  val RoundingMode = BigDecimal.RoundingMode.UP

  type Entries = Map[ProductTitle, (Price, Count)]
  type ProductTitle = String
  type Price = Float
  type Count = Int
  val EmptyEntries = Map.empty[ProductTitle, (Price, Count)]

  def newCart: UIO[ShoppingCart] = Ref.make(EmptyEntries).map(new ShoppingCart(_))

class ShoppingCart(data: Ref[ShoppingCart.Entries]) {
  import ShoppingCart.*
  def addLineItem(title: String, count: Int): Task[Unit] =
    ProductLookupApi.priceLookup(title)
      .flatMap{ price =>
        addLineItem(ProductInfo(title, price), count)
      }

  def addLineItem(product: ProductInfo, addCount: Int): Task[Unit] =
    val result = data.modify(entries => { // updates atomically
      val existing = entries.get(product.title)
      existing.fold(
        (None, entries.updated(product.title, (product.price, addCount)))
      ) { case (existingPrice, existingCount) =>
        if (product.price == existingPrice)
          (None, entries.updated(product.title, (existingPrice, existingCount + addCount)))
        else
          val err = Some(new RuntimeException("Price in the Cart shold not be changed in the middle"))
          (err, entries)
      }
    })
    result.flatMap{
      case None => ZIO.succeed(())
      case Some(e) => ZIO.fail(e)
    }

  def subtotal: UIO[BigDecimal] = for
    map <- data.get
    subtot =  map.foldLeft(BigDecimal(0)) {
      case (sum, (title, (price, count))) => sum + BigDecimal(price) * count }
  yield subtot.setScale(2, RoundingMode)

  def taxPayable: UIO[BigDecimal] = subtotal.map(_ * TaxRate)

  def totalPayable: UIO[BigDecimal] = for
    subtot <- this.subtotal
    tax <- this.taxPayable
  yield subtot + tax

  def numLines: UIO[Int] = data.get.map(_.size)
}

case class ProductInfo(title: String, price: Float)
