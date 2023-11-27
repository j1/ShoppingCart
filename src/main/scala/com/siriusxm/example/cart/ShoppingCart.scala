package com.siriusxm.example.cart

import zio.*

class ShoppingCart(data: Ref[Entries]) {
  //Tax payable, charged at 12.5% on the subtotal
  val TaxRate = 0.125d

  def addLineItem(title: String, count: Int): Task[Unit] =
    ProductLookupApi.priceLookup(title)
      .flatMap{ price =>
        addLineItem(ProductInfo(title, price), count)
      }

  def addLineItem(product: ProductInfo, count: Int): UIO[Unit] =
    data.update(entries => { // updates atomically
      val existing = countOf(entries.get(product))
      entries.updated(product, count + existing) })

  def subtotal: UIO[BigDecimal] = for
    map <- data.get
    subtot =  map.foldLeft(BigDecimal(0)) {
      case (sum, (prodInfo, count)) => sum + BigDecimal(prodInfo.price) * count }
  yield subtot

  def taxPayable: UIO[BigDecimal] = subtotal.map(_ * TaxRate)

  def totalPayable: UIO[BigDecimal] = for
    subtot <- this.subtotal
    tax <- this.taxPayable
  yield subtot + tax

  def numLines: UIO[Int] = data.get.map(_.size)
  private def countOf(opt: Option[Int]) = opt match
    case None => 0
    case Some(i) => i
}

/** TODO Validate - price in the key in case price changes between adding items */
type Entries = Map[ProductInfo, Int]
val emptyEntries = Map.empty[ProductInfo, Int]
case class ProductInfo(title: String, price: Float)
