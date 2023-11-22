package com.siriusxm.example.cart

import zio.{Ref, UIO}

class ShoppingCart(data: Ref[Entries]) {
  val TaxRate = 0.125d
  def addLineItem(product: ProductInfo, count: Int): UIO[Unit] =
    data.update(entries => {
      val existing = countOf(entries.get(product))
      entries.updated(product, count + existing) })

  def subtotal: UIO[BigDecimal] = for
    map <- data.get
    subtot =  map.foldLeft(BigDecimal(0)){case (sum, (prodInfo, count)) => sum + 1.0d * prodInfo.price * count }
  yield subtot

  def taxPayable: UIO[BigDecimal] = subtotal.map(_ * TaxRate)

  def totalPayable: UIO[BigDecimal] = for
    subtot <- this.subtotal
    tax <- this.taxPayable
  yield subtot + tax

  //Tax payable, charged at 12.5% on the subtotal
  //Total payable (subtotal + tax)
  def numLines: UIO[Int] = data.get.map(_.size)
  private def countOf(opt: Option[Int]) = opt match
    case None => 0
    case Some(i) => i
}

/** TODO Validate - price in the key in case price changes between adding items */
type Entries = Map[ProductInfo, Int]
val emptyEntries = Map.empty[ProductInfo, Int]
case class ProductInfo(title: String, price: Float)

type Error = String
