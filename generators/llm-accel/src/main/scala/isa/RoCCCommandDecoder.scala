package llmaccel.isa

import chisel3._
import llmaccel.util.LLMOpcodes

class RoCCCommandDecoder extends Module {
  val io = IO(new Bundle {
    val funct = Input(UInt(8.W))
    val isLLMOp = Output(Bool())
  })
  io.isLLMOp := io.funct === LLMOpcodes.matmul || io.funct === LLMOpcodes.attention || io.funct === LLMOpcodes.layernorm || io.funct === LLMOpcodes.quant || io.funct === LLMOpcodes.dma
}
