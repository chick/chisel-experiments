// See LICENSE for license details.

package examples

import org.scalatest.FreeSpec
import chisel3._
import chisel3.iotesters.PeekPokeTester

class ReadyValidUInt32 extends Bundle {
  val ready = Input(Bool())
  val valid = Output(Bool())
  val data = Output(UInt(32.W))
  // Don't worry about this line and what it does.
//  override def cloneType: this.type = new ReadyValidUInt32().asInstanceOf[this.type]
}

class SlowIncrementer extends Module {
  val io = IO(new Bundle {
    // Number of cycles to delay.
    val delay = Input(UInt(8.W))
    val in = Flipped(new ReadyValidUInt32)
    val out = new ReadyValidUInt32
  })

  val busy = RegInit(false.B)
  val counter1 = RegInit(0.U(8.W))
  val delayReg = Reg(io.delay.cloneType)
  val counter2 = Reg(io.in.data.cloneType)

  io.in.ready := ! busy

  when(io.in.valid && io.in.ready) {
    busy := true.B
    io.out.valid := false.B
    counter1 := 0.U
    counter2 := io.in.data
    delayReg := io.delay
  }.elsewhen(busy && io.out.valid) {
    when(io.out.ready) {

    }
  }.elsewhen(busy) {
    counter1 := counter1 + 1.U
    when(counter1 === delayReg) {
      counter2 := counter2 + 1.U
      counter1 := 0.U
      io.out.valid := true.B
    }
  }

  // YOUR CODE HERE
  // You may find Chisel's Counter class useful.
  // https://chisel.eecs.berkeley.edu/api/#chisel3.util.Counter
}

class SlowIncrementerTester(c: SlowIncrementer) extends PeekPokeTester(c) {

}

class SlowIncSpec extends FreeSpec {
  "should work" in {
    iotesters.Driver.execute(Array.empty[String], () => new SlowIncrementer) { c =>
      new SlowIncrementerTester(c)
    }
  }
}
