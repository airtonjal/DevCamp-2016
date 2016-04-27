object exercise {
  def factorial(n: Int): Int = {
    def fact(mult: Int, n: Int): Int =
      if (n == 0) mult
      else fact(mult * n, n - 1)
    fact(1, n)
  }
  factorial(1)
  factorial(2)
  factorial(3)
  factorial(4)
  factorial(5)
  factorial(6)
  factorial(7)
  factorial(8)
  factorial(9)
  factorial(10)

}