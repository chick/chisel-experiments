// See LICENSE for license details.

package bbexample


import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util.HasBlackBoxResource

class BBAnd extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val b=  Input(Bool())
    val result = Output(Bool())
  })
  val blackBoxFloatVerilog = "/bbexample/BBAnd.v"
  setResource(blackBoxFloatVerilog)
}

class BBAndWrapper extends Module {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val b=  Input(Bool())
    val result = Output(Bool())
  })

  val tb = Module(new BBAnd)
  tb.io.a := io.a
  tb.io.b := io.b
  io.result := tb.io.result
}


class BBAndTester(c: BBAndWrapper) extends PeekPokeTester(c) {
  // FILL THIS IN HERE
  poke(c.io.a, 1)
  poke(c.io.b, 1)
  // FILL THIS IN HERE
  step(1)
  expect(c.io.result, 1)
}

object BBAndRunner {
  def main(args: Array[String]): Unit = {
    iotesters.Driver(() => new BBAndWrapper, "verilator") { c =>
      new BBAndTester(c)
    }
  }
}
