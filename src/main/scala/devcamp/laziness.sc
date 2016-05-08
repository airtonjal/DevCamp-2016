class Y { lazy val y = { Thread.sleep(4000); 13 } }
class X { val x = { Thread.sleep(4000); 15 } }

val y = new Y
val x = new X

//y.y
//y.y
//y.y
