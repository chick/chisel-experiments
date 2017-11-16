// See LICENSE for license details.

package clockstuff

import chisel3.iotesters.{Driver, PeekPokeTester}
import org.scalatest.{FreeSpec, Matchers}

class GatedClockTesters(c: HasGatedCounter) extends PeekPokeTester(c) {
  poke(c.io.enable, 0)

  for(_ <- 0 until 10) {
    println(s"counter is ${peek(c.io.count)}")
    step(1)
  }

  poke(c.io.enable, 1)

  for(_ <- 0 until 10) {
    println(s"counter is ${peek(c.io.count)}")
    step(1)
  }

}

// scalastyle:off magic.number
class GateClockSpec extends FreeSpec with Matchers {
  "GateClockSpec should pass a basic test with interpreter" in {
    Driver.execute(Array.empty, () => new HasGatedCounter) { c =>
      new GatedClockTesters(c)
    }
    true should be(true)
  }
  "GateClockSpec should pass a basic test with verilator" in {
    Driver.execute(Array("--backend-name", "verilator"), () => new HasGatedCounter) { c =>
      new GatedClockTesters(c)
    }
    true should be(true)
  }
}
