package llmaccel.memory

import chisel3._
import chisel3.util._
import llmaccel.configs.LLMAcceleratorParams

class KVRequest(p: LLMAcceleratorParams) extends Bundle {
  val addr = UInt(log2Ceil(p.kvCacheEntries).W)
  val write = Bool()
  val data = SInt(p.accWidth.W)
}

class PagedKVCache(p: LLMAcceleratorParams) extends Module {
  val io = IO(new Bundle {
    val req = Flipped(Decoupled(new KVRequest(p)))
    val resp = Decoupled(SInt(p.accWidth.W))
    val hitCount = Output(UInt(32.W))
    val missCount = Output(UInt(32.W))
  })

  val mem = SyncReadMem(p.kvCacheEntries, SInt(p.accWidth.W))
  val valid = RegInit(VecInit(Seq.fill(p.kvCacheEntries)(false.B)))
  val hitCount = RegInit(0.U(32.W))
  val missCount = RegInit(0.U(32.W))

  io.req.ready := true.B
  io.resp.valid := RegNext(io.req.fire && !io.req.bits.write, false.B)
  io.resp.bits := mem.read(io.req.bits.addr, io.req.fire && !io.req.bits.write)

  when(io.req.fire) {
    when(io.req.bits.write) {
      mem.write(io.req.bits.addr, io.req.bits.data)
      valid(io.req.bits.addr) := true.B
    }.otherwise {
      when(valid(io.req.bits.addr)) { hitCount := hitCount + 1.U }
        .otherwise { missCount := missCount + 1.U }
    }
  }

  io.hitCount := hitCount
  io.missCount := missCount
}
