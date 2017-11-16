// See LICENSE for license details.

package puzzles

import chisel3._
import chisel3.util.Decoupled

import scala.collection.immutable.ListMap

class DataBundleOrig extends Bundle {
  val x = UInt(4.W)
}

class LoopLatchOrig(val NumInputs: Int,
                    val ArgsOut: Array[Int],
                    val ID: Int)
                extends Module {

  val io = IO(new Bundle {
    val inputArg = Vec(NumInputs, Flipped(Decoupled(new DataBundleOrig())))

    val Args = Vec(for (i <- 0 until ArgsOut.length) yield {
      val arg = Vec(ArgsOut(i), Decoupled(new DataBundleOrig()))
      arg
    })
  })
}

final class VecVecBundle(elts: (String, Data)*) extends Record {
  val elements = ListMap(elts map { case (field, elt) => field -> elt.chiselCloneType }: _*)
  def apply(elt: String): Data = elements(elt)
  override def cloneType = (new VecVecBundle(elements.toList: _*)).asInstanceOf[this.type]
}

class LoopLatch2(val NumInputs: Int,
                 val ArgsOut: Array[Int],
                 val ID: Int)
  extends Module {

  val io = IO(
    new VecVecBundle(
      Seq("inputArg" -> Vec(NumInputs, Flipped(Decoupled(new DataBundleOrig())))) ++
        (ArgsOut.indices).map { i =>
          val arg = Vec(ArgsOut(i), Decoupled(new DataBundleOrig()))
          s"Args_$i" -> arg
        }:_*
    )
  )
}