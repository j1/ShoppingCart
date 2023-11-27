package com.siriusxm.example.cart

import com.typesafe.scalalogging.Logger
import sttp.client4.{ResponseException, UriContext}
import sttp.client4.quick.*
import sttp.client4.httpclient.zio.HttpClientZioBackend
import sttp.client4.ResponseException
import sttp.client4.ziojson.*
import zio.json.{DeriveJsonDecoder, JsonDecoder}
import zio.*

object ProductLookupApi:
  private val baseUrl = "https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main"

  private val logger = Logger[ProductLookupApi.type]
  given JsonDecoder[ProductInfo] = DeriveJsonDecoder.gen[ProductInfo]
  
  private val req
  = basicRequest
  
  def priceLookup(productTitle: String): Task[Float] =
    for
      backend <- HttpClientZioBackend()
      resp <-
        req.get(uri"$baseUrl/${productTitle.toLowerCase}.json")
        .response(asJson[ProductInfo])
        .send(backend)
      ret <-
        ZIO.fromEither(resp.body.map(_.price))
    yield ret


