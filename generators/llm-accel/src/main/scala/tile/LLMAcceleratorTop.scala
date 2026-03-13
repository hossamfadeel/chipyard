package llmaccel.tile

import chisel3._
import llmaccel.configs.LLMAcceleratorDefaults

class LLMAcceleratorTop extends Module {
  val io = IO(new Bundle {
    val start = Input(Bool())
    val done = Output(Bool())
  })
  val tile = Module(new LLMAcceleratorTile(LLMAcceleratorDefaults.kr260))
  tile.io.start := io.start
  tile.io.opCode := 1.U
  tile.io.matrixCmd.m := 16.U
  tile.io.matrixCmd.n := 16.U
  tile.io.matrixCmd.k := 16.U
  tile.io.matrixCmd.precision := 1.U
  tile.io.matrixCmd.sparse := false.B
  tile.io.attentionCmd.seqLen := 1.U
  tile.io.attentionCmd.numHeads := 1.U
  tile.io.attentionCmd.headDim := 1.U
  tile.io.attentionCmd.groupedQuery := false.B
  tile.io.attentionCmd.multiQuery := false.B
  tile.io.attentionCmd.slidingWindow := false.B
  tile.io.attentionCmd.ropeEnable := false.B
  tile.io.dmaCmd.srcAddr := 0.U
  tile.io.dmaCmd.dstAddr := 0.U
  tile.io.dmaCmd.bytes := 0.U
  tile.io.dmaCmd.stride := 0.U
  tile.io.dmaCmd.repeat := 0.U
  io.done := tile.io.status.done
}
