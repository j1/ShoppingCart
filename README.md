## Shopping Cart
Implemented using functional scala with zio and sttp libraries

### Testing
```shell
    > sbt test
```
### Design Decisions
* Shopping cart stored in memory using `Ref[T]` which is modified atomically and so
  is thread-safe
* Price data is directly fetched from URLs everytime and not cached
* It is assumed price data doesn't change while the product is in the shopping-cart,
  and any mismatch results in error response

---
## The Problem Statement
Create a shopping cart package (e.g. `com.siriusxm.example.cart`) that facilitates 2 basic capabilities:

1. Add multiple products to the cart. A product has a name and price. You should be able to specify how many of each product is being added to the cart and provide a means to observe the resulting state.
1. Calculate the totals
    1. Cart subtotal (sum of price for all items)
    1. Tax payable, charged at 12.5% on the subtotal
    1. Total payable (subtotal + tax)

Pricing data for each product should be retrieved via an HTTP call. You can find example pricing data for a set of sample products at the URI’s below. You should assume that the product name (lowercase) matches the file name. Use whatever libraries you like to get and parse the JSON. For example OKHttp (https://square.github.io/okhttp/) is a simple client for Java.

## Valid Product Information URLs
- https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/cheerios.json
- https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/cornflakes.json
- https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/frosties.json
- https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/shreddies.json
- https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/weetabix.json

## Sample based on the data
The below is a sample with the correct values you can use to confirm your calculations
```
  Add 2 × cornflakes @ 2.52 each
  Add 1 × weetabix @ 9.98 each
  Subtotal = 15.02
  Tax = 1.88
  Total = 16.90
```

## FAQ
**What other functionality is required?**

No other capabilities are required. This is not a trick question and there is no single correct answer. We prefer simple, well tested solutions over clever solutions. The complexity of your solution should reflect that of the problem. Use whatever external libraries or packages you wish.

**Should I include an app?**

Please do not write a web, desktop, command line or any other kind of app. Your code needs only to be driven by tests and can extend `IOApp`.

**Must I use Cats?**

We often use Cats in our projects, so we've provided all the boilerplate and dependencies in this template project. However, we are more interested in your use of functional programming techniques than the use of a library, so feel free to deviate from what we provide here.

**What about ambiguity?**

If there is any ambiguity please add this in a section added to the bottom of the README and make a choice yourself to resolve the ambiguity.

**How should rounding be handled?**

Prices should be rounded up where required.

**Is there a time limit for completing the exercise?**

There is no time limit for how long the exercise should take for you to complete, but we find most people complete the exercise in around 90 mins.

## Internal Reference

Please do not remove this section, it helps us a lot to have this information available.

Template: scala-sbt
    
