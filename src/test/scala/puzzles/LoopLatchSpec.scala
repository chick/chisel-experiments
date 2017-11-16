// See LICENSE for license details.

package puzzles

import chisel3._
import chisel3.iotesters.PeekPokeTester

import org.scalatest.{FreeSpec, Matchers}


// scalastyle:off magic.number
class LoopLatchSpecTester(c: LoopStart) extends PeekPokeTester(c) {
}

// scalastyle:off magic.number
class LoopLatchSpec extends FreeSpec with Matchers {
  "LoopLatchSpec should pass a basic test" in {
    iotesters.Driver.execute(Array.empty[String], () => new LoopStart(4, Array(1,2,3,4), 3)) { c =>
      new LoopLatchSpecTester(c)
    } should be(true)
  }
}
