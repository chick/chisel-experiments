// See LICENSE for license details.

package issue631

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, TesterOptionsManager}
import java.io.File

import chisel3.util.{ShiftRegister, log2Ceil}

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

class ConsoleTests(c: Console) extends PeekPokeTester(c)
{
  poke(c.io.toggle, 0)
  reset(1)
  val l = c.helloWorldString.length()
  for (i <- 0 to l * 2)
  {
    poke(c.io.toggle, (i + 1) % 2)
    step(1)
    expect(c.io.characterAvailable, 0)
    step(5)
    expect(c.io.characterAvailable, 1)
    expect(c.io.character, c.helloWorldString(i % l).toInt)
  }
}

/**
  * Test *only* with Verilator, firrtl fails: https://github.com/freechipsproject/chisel3/issues/631
  */
class ConsoleTester extends ChiselFlatSpec {
  val verbose = true
  behavior of "Console"
  val optionsManager = new TesterOptionsManager {
    testerOptions = testerOptions.copy(
      backendName = "firrtl",
//      waveform = Option(new File("console.vcd")),
      isVerbose = verbose
    )
  }
  Driver.execute(() => new Console(), optionsManager)(c =>
    new ConsoleTests(c)) should be (true)
}

object generate_debug_console extends App {
  chisel3.Driver.execute(args, () => new Console)
}
