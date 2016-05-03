def measure[T](func: => T):T = {
  val start = System.currentTimeMillis()
  val result = func
  val elapsed = System.currentTimeMillis() - start
  println("The execution of this call took: %s s".format(elapsed.toFloat / 1000))
  result
}

def heavyComputation = "abcdefghij".permutations.size

measure(heavyComputation)
measure((1 to 10).foreach(i => heavyComputation))
measure((1 to 10).par.foreach(i => heavyComputation))
