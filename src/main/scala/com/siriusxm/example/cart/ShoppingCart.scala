package com.siriusxm.example.cart

import zio.{Ref, UIO}

class ShoppingCart(data: Ref[Entries]) {
  def addLineItem(product: ProductInfo, count: Int): UIO[Unit] =
    data.update(entries =>
      entries.updated(product, count + countOf(entries.get(product))))
    
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
