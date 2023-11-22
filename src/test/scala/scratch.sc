val m = Map('a' -> 1, 'b' -> 2)
m + (('c',3))

extension[K, V <: Int](m: Map[K, V])
  def inc(c: K, count: V) =
    m.updated(c,
      m.get(c).fold(count)(_+count))

m.inc('a', -1)
m.inc('c', 3)

case class Ch(c: Char, count: Int)

val chMap = Map(('a',1)-> 3)
chMap.inc(('a', 1), 2)