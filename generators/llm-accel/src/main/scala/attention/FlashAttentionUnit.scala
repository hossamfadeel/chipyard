package llmaccel.attention

import chisel3._
import chisel3.util._
import llmaccel.configs.LLMAcceleratorParams

class FlashAttentionIO(p: LLMAcceleratorParams) extends Bundle {
  val start = Input(Bool())
  val seqLen = Input(UInt(16.W))
  val q = Input(Vec(p.numHeads, Vec(p.headDim, SInt(p.dataWidth.W))))
  val k = Input(Vec(p.numHeads, Vec(p.headDim, SInt(p.dataWidth.W))))
  val v = Input(Vec(p.numHeads, Vec(p.headDim, SInt(p.dataWidth.W))))
  val o = Output(Vec(p.numHeads, Vec(p.headDim, SInt(p.accWidth.W))))
  val busy = Output(Bool())
  val done = Output(Bool())
}

class FlashAttentionUnit(p: LLMAcceleratorParams) extends Module {
  private val headIdxWidth = log2Ceil(math.max(2, p.numHeads))
  private val dimIdxWidth = log2Ceil(math.max(2, p.headDim))

  val io = IO(new FlashAttentionIO(p))

  val sIdle :: sScore :: sProject :: sDone :: Nil = Enum(4)
  val state = RegInit(sIdle)
  val headIdx = RegInit(0.U(headIdxWidth.W))
  val dimIdx = RegInit(0.U(dimIdxWidth.W))
  val scoreAcc = RegInit(0.S(p.accWidth.W))
  val scoreReg = RegInit(VecInit(Seq.fill(p.numHeads)(0.S(p.accWidth.W))))
  val outReg = RegInit(VecInit(Seq.fill(p.numHeads)(VecInit(Seq.fill(p.headDim)(0.S(p.accWidth.W))))))

  val qElem = io.q(headIdx)(dimIdx)
  val kElem = io.k(headIdx)(dimIdx)
  val vElem = io.v(headIdx)(dimIdx)
  val scaledScore = (scoreReg(headIdx) >> log2Ceil(math.max(1, p.headDim))).asSInt

  switch(state) {
    is(sIdle) {
      when(io.start) {
        headIdx := 0.U
        dimIdx := 0.U
        scoreAcc := 0.S
        state := sScore
      }
    }

    is(sScore) {
      val nextScore = (scoreAcc + (qElem * kElem).asSInt).asSInt
      when(dimIdx === (p.headDim - 1).U) {
        scoreReg(headIdx) := nextScore
        scoreAcc := 0.S
        dimIdx := 0.U
        when(headIdx === (p.numHeads - 1).U) {
          headIdx := 0.U
          state := sProject
        }.otherwise {
          headIdx := headIdx + 1.U
        }
      }.otherwise {
        scoreAcc := nextScore
        dimIdx := dimIdx + 1.U
      }
    }

    is(sProject) {
      outReg(headIdx)(dimIdx) := (scaledScore + vElem.asSInt).asSInt
      when(dimIdx === (p.headDim - 1).U) {
        dimIdx := 0.U
        when(headIdx === (p.numHeads - 1).U) {
          headIdx := 0.U
          state := sDone
        }.otherwise {
          headIdx := headIdx + 1.U
        }
      }.otherwise {
        dimIdx := dimIdx + 1.U
      }
    }

    is(sDone) {
      state := sIdle
    }
  }

  io.o := outReg
  io.busy := state === sScore || state === sProject
  io.done := state === sDone
}
