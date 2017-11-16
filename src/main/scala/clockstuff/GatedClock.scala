// See LICENSE for license details.

package clockstuff

import chisel3._
import chisel3.experimental.withClock

class GatedCounter extends Module {
  val io = IO(new Bundle {
    val count = Output(UInt(32.W))
  })

  val counter = RegInit(0.U(32.W))

  counter := counter + 1.U

  io.count := counter
}

class HasGatedCounter extends Module {
  val io = IO(new Bundle {
    val enable = Input(Bool())
    val count  = Output(UInt(32.W))
  })

  val clock2 = (clock.asUInt()(0) & io.enable).asClock()

  withClock(clock2) {
    val counterModule = Module(new GatedCounter)

    io.count := counterModule.io.count
  }
}
