def add(a: Int)(b: Int) = a + b

val onePlusFive = add(1)(5)
val addFour = add(4)_
val twoPlusFour = addFour(2)

onePlusFive == twoPlusFour

def addA(x: Int, y: Int, z: Int): Int =
  x + y + z

val a = addA(1, 2, 3)

def addB(x: Int): Int => (Int => Int) =
  y => z => x + y + z

val b1 = addB(1)
val b2 = b1(2)
val b3 = b2(3)