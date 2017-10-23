// See LICENSE for license details.

package issue631

import chisel3._
import chisel3.util._

/**
  * Debug console designed to work with e.g. Xilinx VIO, where we can output we output
  * one character for each time we toggle a button.
  */
class Console extends Module
{
  val io = IO(new Bundle()
  {
    val toggle = Input(Bool())
    val character = Output(UInt(8.W))
    val characterAvailable = Output(UInt(8.W))
  })

  val helloWorldString = "We choose to go to the moon. We choose to go to the moon in this decade and do the other things, not because they are easy, but because they are hard, because that goal will serve to organize and measure the best of our energies and skills, because that challenge is one that we are willing to accept, one we are unwilling to postpone, and one which we intend to win, and the others, too."
  val helloWorld = VecInit(Array.tabulate(helloWorldString.size)({ i => helloWorldString.getBytes()(i).U(8.W)}))
  val pointer = RegInit(0.U(log2Ceil(helloWorld.length).W))
  val arrivedPointer = RegInit(0.U(log2Ceil(helloWorld.length).W))
  val lastToggle = RegInit(false.B)
  val characterArrived = RegInit(false.B)

  val gotToggle = io.toggle =/= lastToggle
  when (gotToggle)
  {
    lastToggle := io.toggle
    characterArrived := false.B
    pointer := Mux(pointer === helloWorld.size.U - 1.U, 0.U, pointer + 1.U)
    arrivedPointer := pointer
  }

  when (ShiftRegister(gotToggle, 5))
  {
    characterArrived := true.B
  }

  io.character := helloWorld(arrivedPointer)
  io.characterAvailable := characterArrived
}
