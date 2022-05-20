sealed trait HList

final case class ::[+H, +T <: HList](head : H, tail : T) extends HList {
  override def toString = head match {
    case _: ::[_, _] => s"($head) :: $tail"
    case _ => s"$head :: $tail"
  }
}

sealed trait HNil extends HList {
  def ::[H](h: H): H :: HNil = new ::(h, this)
  override def toString = "HNil"
}

object HNil extends HNil

final class HListOps[L <: HList](l: L) {
  def ::[H](h: H): H :: L = new::(h,l)
}

object HList {
  implicit def hlistOps[L <: HList](l: L): HListOps[L] = new HListOps(l)
}

