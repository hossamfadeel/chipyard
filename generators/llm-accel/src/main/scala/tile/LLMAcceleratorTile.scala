package llmaccel.tile

import chisel3._
import chisel3.util._
import llmaccel.configs.LLMAcceleratorParams
import llmaccel.util._
import llmaccel.systolic.SystolicArray
import llmaccel.attention.FlashAttentionUnit
import llmaccel.memory.PagedKVCache
import llmaccel.quant.QuantizationEngine
import llmaccel.control.{DMAEngine, InferenceController}

class LLMAcceleratorTile(p: LLMAcceleratorParams) extends Module {
  val io = IO(new LLMAcceleratorIO(p))

  val controller = Module(new InferenceController(p))
  val systolic = Module(new SystolicArray(p))
  val attention = Module(new FlashAttentionUnit(p))
  val kvCache = Module(new PagedKVCache(p))
  val dma = Module(new DMAEngine)
  val quant = Module(new QuantizationEngine(p.accWidth, p.dataWidth))

  controller.io.start := io.start
  controller.io.opCode := io.opCode

  systolic.io.start := controller.io.matrixStart
  for (i <- 0 until p.systolicDim; j <- 0 until p.systolicDim) {
    systolic.io.a(i)(j) := (i + j + 1).S
    systolic.io.b(i)(j) := (if (i == j) 1 else 0).S
  }

  attention.io.start := controller.io.attentionStart
  attention.io.seqLen := io.attentionCmd.seqLen
  for (h <- 0 until p.numHeads; d <- 0 until p.headDim) {
    attention.io.q(h)(d) := ((h + d + 1) % 8).S
    attention.io.k(h)(d) := ((d + 1) % 8).S
    attention.io.v(h)(d) := ((h + 1) % 8).S
  }

  val kvIssued = RegInit(false.B)
  when(io.start) { kvIssued := false.B }
  kvCache.io.req.valid := controller.io.attentionStart && !kvIssued
  kvCache.io.req.bits.addr := 0.U
  kvCache.io.req.bits.write := true.B
  kvCache.io.req.bits.data := attention.io.o(0)(0)
  when(kvCache.io.req.fire) { kvIssued := true.B }
  kvCache.io.resp.ready := true.B

  dma.io.cmd.valid := controller.io.dmaStart
  dma.io.cmd.bits := io.dmaCmd

  quant.io.in := systolic.io.c(0)(0)
  quant.io.scale := 1.U
  quant.io.zeroPoint := 0.S

  io.status.busy := systolic.io.busy || attention.io.busy || dma.io.busy
  io.status.error := false.B
  io.status.done := systolic.io.done || attention.io.done || (!dma.io.busy && controller.io.dmaStart)

  val cycleCounter = RegInit(0.U(64.W))
  val matrixCounter = RegInit(0.U(64.W))
  val attentionCounter = RegInit(0.U(64.W))
  when(io.start || io.status.busy) { cycleCounter := cycleCounter + 1.U }
  when(systolic.io.busy) { matrixCounter := matrixCounter + 1.U }
  when(attention.io.busy) { attentionCounter := attentionCounter + 1.U }
  io.perf.cycles := cycleCounter
  io.perf.matrixOps := matrixCounter
  io.perf.attentionOps := attentionCounter
  io.perf.dmaBytes := dma.io.completedBytes
  io.perf.kvHits := kvCache.io.hitCount
  io.perf.kvMisses := kvCache.io.missCount
}
