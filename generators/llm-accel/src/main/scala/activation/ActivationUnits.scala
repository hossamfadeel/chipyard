package llmaccel.activation

import chisel3._
import chisel3.util._

class GELUApprox(width: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(SInt(width.W))
    val out = Output(SInt(width.W))
  })
  io.out := (io.in >> 1).asSInt + io.in
}

class SwiGLU(width: Int) extends Module {
  val io = IO(new Bundle {
    val x = Input(SInt(width.W))
    val gate = Input(SInt(width.W))
    val out = Output(SInt(width.W))
  })
  io.out := ((io.x * (io.gate >> 1)).asSInt >> 4).asSInt
}
