package llmaccel.quant

import chisel3._

class QuantizationEngine(inWidth: Int, outWidth: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(SInt(inWidth.W))
    val scale = Input(UInt(8.W))
    val zeroPoint = Input(SInt(outWidth.W))
    val out = Output(SInt(outWidth.W))
  })
  io.out := ((io.in >> io.scale(2, 0)).asSInt + io.zeroPoint).asSInt
}
