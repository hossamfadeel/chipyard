package llmaccel.control

import chisel3._
import chisel3.util._
import llmaccel.util.DMACommand

class DMAEngine extends Module {
  val io = IO(new Bundle {
    val cmd = Flipped(Decoupled(new DMACommand))
    val busy = Output(Bool())
    val completedBytes = Output(UInt(32.W))
  })
  val busyReg = RegInit(false.B)
  val bytesReg = RegInit(0.U(32.W))
  io.cmd.ready := !busyReg
  when(io.cmd.fire) {
    busyReg := true.B
    bytesReg := io.cmd.bits.bytes
  }.elsewhen(busyReg) {
    busyReg := false.B
  }
  io.busy := busyReg
  io.completedBytes := bytesReg
}
