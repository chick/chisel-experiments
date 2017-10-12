// See LICENSE for license details.

package foutse

import chisel3._
import chisel3.iotesters

import scala.util.Random

object Main {
  def main(args: Array[String]): Unit = {
    Random.setSeed(0L)

    val tabHash = (n:Int, w:Double,b:Double) =>{
      val x = Array.fill(n)(2.5)
      val tabHash0 = new Array[Array[Double]](n)
      for( ind <- 0 until n) {
        val vechash1 = new Array[Double](n)
        for( ind2 <- 0 until n) vechash1(ind2) = Random.nextGaussian
        tabHash0(ind) = vechash1
      }
      def hashfunc( x : Array[Double], w:Double,  b:Double, tabHash1: Array[Array[Double]] ) : Double={
        val tabHash = new Array[Double](tabHash1.size)
        for( ind <- tabHash1.indices) {
          var sum = 0.0
          for( ind2 <- 0 until x.size ) {
            sum += ( x(ind2) * tabHash1(ind)(ind2) )
          }
          tabHash(ind) = (sum + b) / w
        }
        tabHash.reduce(_ + _)
      }
      hashfunc(x,w,b,tabHash0)
    }
    println("The hash value is\n" + tabHash(4,2.5,6.1))
  }
}