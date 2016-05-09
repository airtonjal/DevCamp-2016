case class Point(x: Int, y: Int, z: Int = 0)

val p1 = Point(x = 1, y = 2)
val p2 = p1.copy(z = 42)

p2.y
p2.x

p1.equals(p1.copy())

abstract class Term
case class Var(name: String) extends Term
case class Fun(arg: String, body: Term) extends Term
case class App(f: Term, v: Term) extends Term

Fun("x", Fun("y", App(Var("x"), Var("y"))))

val x = Var("xName")
println(x.name)
