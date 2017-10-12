// See LICENSE for license details.

package bootcamp

import chisel3._
import chisel3.iotesters.PeekPokeTester

/** Sort4 returns the max of it's 3 arguments
  */
class Sort4 extends Module {
  val io = IO(new Bundle {
    val in0 = Input(UInt(16.W))
    val in1 = Input(UInt(16.W))
    val in2 = Input(UInt(16.W))
    val in3 = Input(UInt(16.W))
    val out0 = Output(UInt(16.W))
    val out1 = Output(UInt(16.W))
    val out2 = Output(UInt(16.W))
    val out3 = Output(UInt(16.W))
    val widthOut = Output(UInt(16.W))
  })

  val row10 = Wire(UInt(16.W))
  val row11 = Wire(UInt(16.W))
  val row12 = Wire(UInt(16.W))
  val row13 = Wire(UInt(16.W))

  val www = if(io.in0.widthKnown) io.in0.getWidth else 0

  io.in0.widthOption match {
    case Some(w) => io.widthOut := w.U
    case None => io.widthOut := Wire(0.U)
  }

  when(io.in0 < io.in1) {
    row10 := io.in0            // preserve first two elements
    row11 := io.in1
  }.otherwise {
    row10 := io.in1            // swap first two elements
    row11 := io.in0
  }

  when(io.in2 < io.in3) {
    row12 := io.in2            // preserve last two elements
    row13 := io.in3
  }.otherwise {
    row12 := io.in3            // swap last two elements
    row13 := io.in2
  }

  val row21 = Wire(UInt(16.W))
  val row22 = Wire(UInt(16.W))

  when(row11 < row12) {
    row21 := row11            // preserve middle 2 elements
    row22 := row12
  }.otherwise {
    row21 := row12            // swap middle two elements
    row22 := row11
  }

  when(row10 < row21) {
    io.out0 := row10            // preserve first two elements
    io.out1 := row21
  }.otherwise {
    io.out0 := row21            // swap first two elements
    io.out1 := row10
  }

  when(row10 < row21) {
    io.out0 := row10            // preserve first two elements
    io.out1 := row21
  }.otherwise {
    io.out0 := row21            // swap first two elements
    io.out1 := row10
  }

  when(row22 < row13) {
    io.out2 := row22            // preserve first two elements
    io.out3 := row13
  }.otherwise {
    io.out2 := row13            // swap first two elements
    io.out3 := row22
  }
}

// verify that the max of the three inputs is correct
class Sort4Tester(c: Sort4) extends PeekPokeTester(c) {
  poke(c.io.in0, 3)
  poke(c.io.in1, 6)
  poke(c.io.in2, 9)
  poke(c.io.in3, 12)
  expect(c.io.out0, 3)
  expect(c.io.out1, 6)
  expect(c.io.out2, 9)
  expect(c.io.out3, 12)

  poke(c.io.in0, 13)
  poke(c.io.in1, 4)
  poke(c.io.in2, 6)
  poke(c.io.in3, 1)
  expect(c.io.out0, 1)
  expect(c.io.out1, 4)
  expect(c.io.out2, 6)
  expect(c.io.out3, 13)

  expect(c.io.widthOut, 16)
}

// Scala Code: Calling Driver to instantiate Simple, SimpleTester, and execute the test
// Don't worry about understanding this code, it is very complicated Scala

object Sort4 extends App {
  val works = iotesters.Driver.execute(args, () => new Sort4) {
    c => new Sort4Tester(c)
  }
  assert(works) // Scala Code: if works == false, will throw an error
  println("SUCCESS!!") // Scala Code: if we get here, our tests passed!
}