object Main {
  def main(args: Array[String]): Unit = {
    val p = 45 :: "hoho" :: HNil
    println(p)
  }
}

case class IceCream(name: String, price: Int, inCone: Boolean)

