package llmaccel.activation

import chisel3._

class RMSNorm(width: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(8, SInt(width.W)))
    val out = Output(Vec(8, SInt(width.W)))
  })
  io.out := io.in
}

class LayerNorm(width: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(8, SInt(width.W)))
    val out = Output(Vec(8, SInt(width.W)))
  })
  io.out := io.in
}
