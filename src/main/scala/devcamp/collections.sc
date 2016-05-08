import java.net.InetSocketAddress

val l = List(1, 2, 3, 4, 5)
val fruits = List("apples", "oranges", "pears")

//val first = l.head
//val last  = l.last

//l.map(_ * 2)
//
def f(x: Int) = if (x > 2) Some(x) else None
//def double(x: Int) = x * 2

//l.map(f)
//l map f
//l.map(x => f(x))

//l.sortWith((x, y) => x < y)

val a = l.filter(x => x % 2 == 0)

def g(v: Int) = List(v - 1, v, v + 1)

//l.map(x => g(x))//.flatten
//l.map(x => f(x))//.flatten

var s = Set(1, 3, 5, 7)

val m = Map("one" -> 1, "two" -> 2, "three" -> 3)
//m.mapValues(_ * 2)

//def pairEven(x: Int) = if (x % 2 == 0) "p" else "e"
//val p = l.groupBy(pairEven)
//
//val birds = List("Golden Eagle", "Gyrfalcon", "American Robin",
//  "Mountain BlueBird", "Mountain-Hawk Eagle")
//val groupedByFirstLetter = birds.groupBy(_.charAt(0))
//groupedByFirstLetter.keys
//groupedByFirstLetter.values

val host: Option[String] = Some("localhost")
val port: Option[Int]    = Some(80)

val addr: Option[InetSocketAddress] =
  host flatMap { h =>
    port map { p =>
      new InetSocketAddress(h, p)
    }
  }


