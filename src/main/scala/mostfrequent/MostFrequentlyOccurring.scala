// See LICENSE for license details.

package mostfrequent

import chisel3._
import chisel3.iotesters.PeekPokeTester
import sort.CombinationalSortIndexAndTake

/**
  * A surprisingly complicated way of determining the most frequently occurring number in a
  * Vec of UInt's.  Ties will generally pick the last value that sits at the highest index of
  * all tied elements.
  * @param elementCount  Number of elements to choose from
  * @param uIntSize      Width of element containers (perhaps this should be maxValue)
  */
class MostFrequentlyOccurring(val elementCount: Int, val uIntSize: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(Vec(elementCount, UInt(uIntSize.W)))
    val enable = Input(Bool())
    val mostFrequentlyOccurringValue = Output(UInt(uIntSize.W))
    val done = Output(Bool())
  })

  /**
    * First we compute sums, a list of how many occurrences of the number at index occur from that index
    * to the right end of the inputs.
    * We need to create a temp with a known size to make sure we get UInt's big enough to contain sum (why is this?)
    */

  val sums = io.input.indices.map { index =>
    io.input.drop(index).map { x =>
      (x === io.input(index)).asUInt
    }.reduce(_ +& _)
  }

  printf("Sums: " + ("%d  " * sums.length) + "\n", sums:_* )

  /** next we sort an array of indices using them as keys to the number of occurrences
    *  for the value at that index in the inputs
    * The last value of that sorted list is the the index with the most occurrences.
    * So return the value of that inputs at that index
    */
  val maxSelector = Module(new CombinationalSortIndexAndTake(sums.size, sums.size, UInt(uIntSize.W)))
  maxSelector.io.inputs := sums

  io.mostFrequentlyOccurringValue := io.input(maxSelector.io.outputs.last)
}

class MostFrequentlyOccurringTester(c: MostFrequentlyOccurring) extends PeekPokeTester(c) {
  val inputVectors = Seq(
    Seq(1, 1, 1, 2, 2, 3, 4, 1, 5, 2, 4, 7).take(c.elementCount),
    Seq(1, 1, 1, 3, 3, 3, 2, 2, 2, 2, 2, 2).take(c.elementCount),
    Seq(1, 4, 1, 4, 2, 4, 2, 3, 4, 3, 3, 4).take(c.elementCount),
    Seq(1, 3, 1, 3, 2, 3, 2, 3, 4, 3, 3, 4).take(c.elementCount)
  )
  val expectedValues = Seq(1, 2, 4, 3)

  inputVectors.zip(expectedValues).foreach { case (inputVector, expectedValue) =>
    println("inputs" + inputVector.map { x => f"$x%4d"}.mkString(" "))

    inputVector.zipWithIndex.foreach { case (value, index) =>
      poke(c.io.input(index), value)
    }
    step(1)

    if(peek(c.io.mostFrequentlyOccurringValue) != expectedValue) {
      println(s"ERROR: MFO value is ${peek(c.io.mostFrequentlyOccurringValue)} Should be $expectedValue\n\n\n")
    }
    else {
      println(s"MFO value is ${peek(c.io.mostFrequentlyOccurringValue)}\n\n\n")
    }
    expect(c.io.mostFrequentlyOccurringValue, expectedValue)

    step(1)
  }
}

object MostFrequentlyOccurring {
  def main(args: Array[String]): Unit = {
    iotesters.Driver.execute(Array.empty, () => new MostFrequentlyOccurring(elementCount = 10, uIntSize = 8)) { c =>
//    iotesters.Driver.execute(Array("--backend-name", "verilator"), () => new MostFrequentlyOccurring(elementCount = 10, uIntSize = 8)) { c =>
      new MostFrequentlyOccurringTester(c)
    }
  }
}