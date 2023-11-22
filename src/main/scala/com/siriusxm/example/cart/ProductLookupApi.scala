package com.siriusxm.example.cart

import com.typesafe.scalalogging.Logger
import sttp.client4.UriContext
import sttp.client4.quick.*
import sttp.client4.ziojson.*
import zio.json.{DeriveJsonDecoder, JsonDecoder}

object ProductLookupApi:
  private val baseUrl = "https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main"

  private val logger = Logger[ProductLookupApi.type]
  val jsonDecoder: JsonDecoder[ProductInfo] = DeriveJsonDecoder.gen[ProductInfo]

  private val req
  = quickRequest
  def priceLookup(productTitle: String): Either[String, Float] =
    req.get(uri"$baseUrl/${productTitle.toLowerCase}.json")
      .response(asString)
      .send().body.flatMap(s => jsonDecoder.decodeJson(s)).map(_.price)

