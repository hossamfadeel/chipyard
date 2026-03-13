package llmaccel.systolic

import chisel3._
import chisel3.util._
import llmaccel.configs.LLMAcceleratorParams

class ProcessingElement(dataWidth: Int, accWidth: Int) extends Module {
  val io = IO(new Bundle {
    val a = Input(SInt(dataWidth.W))
    val b = Input(SInt(dataWidth.W))
    val d = Input(SInt(accWidth.W))
    val valid = Input(Bool())
    val out = Output(SInt(accWidth.W))
    val outValid = Output(Bool())
  })

  val accum = RegInit(0.S(accWidth.W))
  when(io.valid) {
    accum := (io.d + (io.a * io.b).asSInt).asSInt
  }
  io.out := accum
  io.outValid := RegNext(io.valid, false.B)
}

class SystolicArray(p: LLMAcceleratorParams) extends Module {
  private val idxWidth = log2Ceil(math.max(2, p.systolicDim))

  val io = IO(new Bundle {
    val start = Input(Bool())
    val a = Input(Vec(p.systolicDim, Vec(p.systolicDim, SInt(p.dataWidth.W))))
    val b = Input(Vec(p.systolicDim, Vec(p.systolicDim, SInt(p.dataWidth.W))))
    val c = Output(Vec(p.systolicDim, Vec(p.systolicDim, SInt(p.accWidth.W))))
    val busy = Output(Bool())
    val done = Output(Bool())
  })

  val sIdle :: sCompute :: sDone :: Nil = Enum(3)
  val state = RegInit(sIdle)
  val rowIdx = RegInit(0.U(idxWidth.W))
  val colIdx = RegInit(0.U(idxWidth.W))
  val kIdx = RegInit(0.U(idxWidth.W))
  val partialSum = RegInit(0.S(p.accWidth.W))
  val resultReg = RegInit(VecInit(Seq.fill(p.systolicDim)(VecInit(Seq.fill(p.systolicDim)(0.S(p.accWidth.W))))))

  val aElem = io.a(rowIdx)(kIdx)
  val bElem = io.b(kIdx)(colIdx)
  val mac = (aElem * bElem).asSInt

  switch(state) {
    is(sIdle) {
      when(io.start) {
        rowIdx := 0.U
        colIdx := 0.U
        kIdx := 0.U
        partialSum := 0.S
        state := sCompute
      }
    }

    is(sCompute) {
      val nextSum = (partialSum + mac).asSInt
      when(kIdx === (p.systolicDim - 1).U) {
        resultReg(rowIdx)(colIdx) := nextSum
        partialSum := 0.S
        kIdx := 0.U
        when(colIdx === (p.systolicDim - 1).U) {
          colIdx := 0.U
          when(rowIdx === (p.systolicDim - 1).U) {
            rowIdx := 0.U
            state := sDone
          }.otherwise {
            rowIdx := rowIdx + 1.U
          }
        }.otherwise {
          colIdx := colIdx + 1.U
        }
      }.otherwise {
        partialSum := nextSum
        kIdx := kIdx + 1.U
      }
    }

    is(sDone) {
      state := sIdle
    }
  }

  io.c := resultReg
  io.busy := state === sCompute
  io.done := state === sDone
}
