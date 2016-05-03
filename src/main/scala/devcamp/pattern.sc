sealed abstract class Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(width: Double, height: Double) extends Shape
case class Triangle(base: Double, height: Double) extends Shape

def area(shape: Shape): Double = {
  shape match {
    case Circle(radius) => math.Pi * math.pow(radius, 2.0)
    case Rectangle(1, height) => height
    case Rectangle(width, 1) => width
    case Rectangle(width, height) => width * height
    case Triangle(0, _) | Triangle(_, 0) => 0
    case Triangle(base, height) => height * base / 2
  }
}

area(Triangle(10, 2))
