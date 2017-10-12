// See LICENSE for license details.

package foutse

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.internal.firrtl.KnownBinaryPoint
import chisel3.iotesters.PeekPokeTester
import chisel3.util.log2Ceil

//scalastyle:off magic.number
class Sort(val inputSize: Int, val outputSize: Int, fixedType: FixedPoint, reverseSort: Boolean = false)
  extends Module {
  val io = IO(new Bundle {
    val inputs    = Input(Vec(inputSize, fixedType))
    val newInputs = Input(Bool())
    val outputs   = Output(Vec(outputSize, fixedType))
    val sortDone  = Output(Bool())
  })

  val sortReg      = Reg(Vec(inputSize, FixedPoint(64.W,16.BP)))
  val busy         = RegInit(false.B)
  val sortCounter  = RegInit(0.U(log2Ceil(inputSize).W))
  val isEvenCycle  = RegInit(false.B)

  when(io.newInputs) {
    // when parent module loads new inputs to be sorted, we load registers
    sortReg.zip(io.inputs).foreach { case (reg, in) => reg := in }

    busy := true.B
    sortCounter := 0.U
    isEvenCycle := false.B
  }
  .elsewhen(busy) {
    isEvenCycle := ! isEvenCycle

    sortCounter := sortCounter + 1.U
    when(sortCounter > inputSize.U) {
      busy := false.B
    }

    when(isEvenCycle) {
      sortReg.toList.sliding(2, 2).foreach {
        case regA :: regB :: Nil =>
          when(regA > regB) {
            regA := regB
            regB := regA
          }
        case _ =>
      }
    }
    .otherwise {
      sortReg.tail.toList.sliding(2, 2).foreach {
        case regA :: regB :: Nil =>
          when(regA > regB) {
            regA := regB
            regB := regA
          }
        case _ =>
      }
    }
  }

  io.sortDone := ! busy

  val orderedRegs = if(reverseSort) sortReg.reverse else sortReg
  io.outputs.zip(orderedRegs).foreach { case (out, reg) =>
    out := reg
  }
}

class SortTester(c: Sort) extends PeekPokeTester(c) {


  def showOutputs(): Unit = {
    for(i <- 0 until c.outputSize) {
      print(f"${peekFixedPoint(c.io.outputs(i))}%10.5f ")
    }
    println()
  }
  for(i <- 0 until c.inputSize) {
    pokeFixedPoint(c.io.inputs(i), (c.inputSize - i).toDouble / 2.0)
  }
  poke(c.io.newInputs, 1)
  step(1)

  poke(c.io.newInputs, 0)
  step(1)

  while(peek(c.io.sortDone) == 0) {
    showOutputs()
    step(1)
  }

  showOutputs()


}

object SortTest {
  def main(args: Array[String]): Unit = {
    iotesters.Driver.execute(args, () => new Sort(5, 5, FixedPoint(16.W, 8.BP))) { c =>
      new SortTester(c)
    }

    iotesters.Driver.execute(
      Array.empty[String],
      () => new Sort(20, 5, FixedPoint(16.W, 8.BP), reverseSort = true)
    ) { c =>
      new SortTester(c)
    }
  }
}