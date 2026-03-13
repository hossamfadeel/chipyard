package llmaccel.control

import chisel3._
import llmaccel.configs.LLMAcceleratorParams
import llmaccel.util.LLMOpcodes

class InferenceController(p: LLMAcceleratorParams) extends Module {
  val io = IO(new Bundle {
    val start = Input(Bool())
    val opCode = Input(UInt(8.W))
    val matrixStart = Output(Bool())
    val attentionStart = Output(Bool())
    val dmaStart = Output(Bool())
    val quantStart = Output(Bool())
  })

  io.matrixStart := io.start && io.opCode === LLMOpcodes.matmul
  io.attentionStart := io.start && io.opCode === LLMOpcodes.attention
  io.dmaStart := io.start && io.opCode === LLMOpcodes.dma
  io.quantStart := io.start && io.opCode === LLMOpcodes.quant
}
