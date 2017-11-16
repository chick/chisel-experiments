// See LICENSE for license details.

package puzzles

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.immutable.ListMap

final class VariableBundle(elts: (String, Vec[UInt])*) extends Record {
  val elements = ListMap(elts map { case (field, elt) => field -> elt.chiselCloneType }: _*)
  def apply(elt: String): Vec[UInt] = elements(elt)
  override def cloneType = (new VecVecBundle(elements.toList: _*)).asInstanceOf[this.type]
}

class SeqIO(val sizes: Array[Int]) extends Module {
  val io = IO(new VariableBundle(Seq.tabulate(sizes.length) { i =>
        s"vec_in_$i" -> Input(Vec(sizes(i), UInt(8.W)))
      } ++
      Seq.tabulate(sizes.length) { i =>
        s"vec_out_$i" -> Output(Vec(sizes(i), UInt(8.W)))
      }:_*
    )
  )

  for(i <- sizes.indices) {
    io(s"vec_out_$i") := io(s"vec_in_$i")
  }
}

class SeqIOTester(c: SeqIO) extends PeekPokeTester(c) {
  for(i <- c.sizes.indices) {
    for(j <- 0 until c.sizes(i)) {
      poke(c.io(s"vec_in_$i")(j), j)
    }
  }

  step(1)

  for(i <- c.sizes.indices) {
    for(j <- 0 until c.sizes(i)) {
      expect(c.io(s"vec_out_$i")(j), j)
    }
  }

}

class SeqIOSpec extends FreeSpec with Matchers {
  "illustrate how to build bundles that have vecs wrapping different sized vecs" in {
    iotesters.Driver.execute(Array.empty[String], () => new SeqIO(Array(1, 2, 3, 4))) { c =>
      new SeqIOTester(c)
    } should be (true)
  }
}
