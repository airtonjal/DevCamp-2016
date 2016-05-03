object session {
  1 + 2
  def abs(x: Double) = if (x < 0) -x else x

  def sqrt(x: Double) = {
    def sqrIter(guess: Double): Double =
      if (isGoodEnough(guess)) guess
      else sqrIter(improve(guess))

    def isGoodEnough(guess: Double) =
      abs(guess * guess - x) < x * 0.0001

    def improve(guess: Double) =
      (guess + x / guess) / 2

    sqrIter(1.0)
  }

  sqrt(3)
  sqrt(1e60)

}