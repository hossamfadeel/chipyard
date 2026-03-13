package llmaccel.util

import chisel3._
import chisel3.util._
import llmaccel.configs.LLMAcceleratorParams

class MatrixCommand(p: LLMAcceleratorParams) extends Bundle {
  val m = UInt(16.W)
  val n = UInt(16.W)
  val k = UInt(16.W)
  val precision = UInt(3.W)
  val sparse = Bool()
}

class AttentionCommand(p: LLMAcceleratorParams) extends Bundle {
  val seqLen = UInt(16.W)
  val numHeads = UInt(8.W)
  val headDim = UInt(16.W)
  val groupedQuery = Bool()
  val multiQuery = Bool()
  val slidingWindow = Bool()
  val ropeEnable = Bool()
}

class DMACommand extends Bundle {
  val srcAddr = UInt(64.W)
  val dstAddr = UInt(64.W)
  val bytes = UInt(32.W)
  val stride = UInt(16.W)
  val repeat = UInt(16.W)
}

class PerfCounters extends Bundle {
  val cycles = UInt(64.W)
  val matrixOps = UInt(64.W)
  val attentionOps = UInt(64.W)
  val dmaBytes = UInt(64.W)
  val kvHits = UInt(64.W)
  val kvMisses = UInt(64.W)
}

class AcceleratorStatus extends Bundle {
  val busy = Bool()
  val error = Bool()
  val done = Bool()
}

class LLMAcceleratorIO(p: LLMAcceleratorParams) extends Bundle {
  val start = Input(Bool())
  val opCode = Input(UInt(8.W))
  val matrixCmd = Input(new MatrixCommand(p))
  val attentionCmd = Input(new AttentionCommand(p))
  val dmaCmd = Input(new DMACommand)
  val status = Output(new AcceleratorStatus)
  val perf = Output(new PerfCounters)
}

object LLMOpcodes {
  val nop       = 0.U(8.W)
  val matmul    = 1.U(8.W)
  val attention = 2.U(8.W)
  val layernorm = 3.U(8.W)
  val quant     = 4.U(8.W)
  val dma       = 5.U(8.W)
}
