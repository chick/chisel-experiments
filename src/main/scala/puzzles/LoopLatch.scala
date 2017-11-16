// See LICENSE for license details.

package puzzles

import chisel3._
import chisel3.util.Decoupled

import scala.collection.immutable.ListMap

class ControlBundle() extends Bundle {
  val x = UInt(4.W)
}

class LiveInNode(NumOuts: Int, ID: Int) extends Module {
  val io = IO(new Bundle {
    val InData = Flipped(Decoupled(new ControlBundle()))
    val Finish = Flipped(Decoupled(new ControlBundle()))
    val pred   = Flipped(Decoupled(Bool()))
    val Out    = Decoupled(Bool())
  })
}

class LoopStart(val NumInputs: Int,
                val ArgsOut: Array[Int],
                val ID: Int) extends Module {

  val io = IO(new Bundle {
    val inputArg = Vec(NumInputs, Flipped(Decoupled(new ControlBundle())))
    val Args     = Vec(ArgsOut.length, Vec(ArgsOut(0), Decoupled(new ControlBundle())))


    val Out = new VecVecBundle(
      Seq("inputArg" -> Vec(NumInputs, Flipped(Decoupled(new ControlBundle())))) ++
        (ArgsOut.indices).map { i =>
          val arg = Vec(ArgsOut(i), Decoupled(new ControlBundle()))
          s"Args_$i" -> arg
        }:_*
    )

    val pSignal = Vec(NumInputs, Flipped(Decoupled(Bool())))

    val Finish = Vec(NumInputs, Flipped(Decoupled(new ControlBundle())))

  }
  )

  val Args = for (i <- 0 until NumInputs) yield {
    val arg = Module(new LiveInNode(NumOuts = 1, ID = i))
    arg
  }

  //Iterating over each loop element and connect them to the IO
  for (i <- 0 until NumInputs) {
    Args(i).io.InData <> io.inputArg(i)
    Args(i).io.Finish <> io.Finish(i)
    Args(i).io.pred   <> io.pSignal(i)
  }

  for (i <- 0 until ArgsOut.length) {
    //io.Out("Args_0")(i) <> Args(i).io.Out
  }
}