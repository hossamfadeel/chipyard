package chipyard.llmaccel

import chisel3._
import chisel3.util._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tile._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.rocket._
import org.chipsalliance.cde.config.Parameters
import llmaccel.configs.LLMAcceleratorDefaults
import llmaccel.tile.LLMAcceleratorTile

class LLMAccelRoCC(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(opcodes) {
  override lazy val module = new LLMAccelRoCCModule(this)
}

class LLMAccelRoCCModule(outer: LLMAccelRoCC)(implicit p: Parameters) extends LazyRoCCModuleImp(outer) {
  val accel = Module(new LLMAcceleratorTile(LLMAcceleratorDefaults.kr260))
  val cmdValid = io.cmd.valid
  val funct = io.cmd.bits.inst.funct

  accel.io.start := cmdValid
  accel.io.opCode := funct(7,0)
  accel.io.matrixCmd.m := io.cmd.bits.rs1(15,0)
  accel.io.matrixCmd.n := io.cmd.bits.rs1(31,16)
  accel.io.matrixCmd.k := io.cmd.bits.rs2(15,0)
  accel.io.matrixCmd.precision := 1.U
  accel.io.matrixCmd.sparse := false.B
  accel.io.attentionCmd.seqLen := io.cmd.bits.rs1(15,0)
  accel.io.attentionCmd.numHeads := 1.U
  accel.io.attentionCmd.headDim := 1.U
  accel.io.attentionCmd.groupedQuery := false.B
  accel.io.attentionCmd.multiQuery := false.B
  accel.io.attentionCmd.slidingWindow := false.B
  accel.io.attentionCmd.ropeEnable := false.B
  accel.io.dmaCmd.srcAddr := io.cmd.bits.rs1
  accel.io.dmaCmd.dstAddr := io.cmd.bits.rs2
  accel.io.dmaCmd.bytes := 64.U
  accel.io.dmaCmd.stride := 0.U
  accel.io.dmaCmd.repeat := 0.U

  io.cmd.ready := true.B
  io.busy := accel.io.status.busy
  io.interrupt := false.B
  io.mem.req.valid := false.B
  io.mem.invalidate_lr := false.B
  io.mem.req.bits := DontCare
  io.resp.valid := RegNext(cmdValid, false.B)
  io.resp.bits.rd := RegNext(io.cmd.bits.inst.rd)
  io.resp.bits.data := Cat(0.U(32.W), accel.io.perf.dmaBytes)
}
