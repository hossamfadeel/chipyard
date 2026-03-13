package llmaccel

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import llmaccel.configs.LLMAcceleratorParams
import llmaccel.memory.PagedKVCache

class KVCacheSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "PagedKVCache"
  it should "support write then read" in {
    test(new PagedKVCache(LLMAcceleratorParams(kvCacheEntries = 16))) { dut =>
      dut.io.req.valid.poke(true.B)
      dut.io.req.bits.addr.poke(3.U)
      dut.io.req.bits.write.poke(true.B)
      dut.io.req.bits.data.poke(42.S)
      dut.clock.step()
      dut.io.req.bits.write.poke(false.B)
      dut.io.resp.ready.poke(true.B)
      dut.clock.step(2)
    }
  }
}
