package TutorialPloblems

import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util.HasBlackBoxResource

class Black extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val b=  Input(Bool())
    val result = Output(Bool())
  })
  val blackBoxFloatVerilog = "/essam/Black.v"
  setResource(blackBoxFloatVerilog)
}

class BBWrapper extends Module {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val b=  Input(Bool())
    val result = Output(Bool())
  })

  val tb = Module(new Black)
  tb.io.a := io.a
  tb.io.b := io.b
  io.result := tb.io.result
}

class BlackTests(c: BBWrapper) extends PeekPokeTester(c) {
  // FILL THIS IN HERE
  poke(c.io.a, 1)
  poke(c.io.b, 1)
  // FILL THIS IN HERE
  step(1)
  expect(c.io.result, 1)
}

object BlackBox_tbTests {
  def main(args: Array[String]): Unit = {
    iotesters.Driver(() => new BBWrapper, "verilator") { c =>
      new BlackTests(c)
   }
  }
}