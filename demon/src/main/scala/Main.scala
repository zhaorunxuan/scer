object Main {
  def main(args: Array[String]): Unit = {
    val g = Tuple.fromProductTyped(IceCream("sun", 4, true))
    println(6 *: g)
  }
}

case class IceCream(name: String, price: Int, inCone: Boolean)