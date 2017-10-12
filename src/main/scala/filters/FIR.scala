// See LICENSE for license details.

package filters

import chisel3._

class FIR(taps: Seq[Int]) extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(16.W))
    val output = Output(UInt(16.W))
  })

  io.output := taps.foldLeft(io.input){ case (result, tap) =>
    val reg = Reg(UInt()) * result
    reg := tap.U * result
    reg
  }
}
