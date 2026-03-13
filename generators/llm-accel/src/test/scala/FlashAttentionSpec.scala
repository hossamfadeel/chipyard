package llmaccel

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import llmaccel.attention.FlashAttentionUnit
import llmaccel.configs.LLMAcceleratorParams

class FlashAttentionSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "FlashAttentionUnit"
  it should "emit done after start" in {
    test(new FlashAttentionUnit(LLMAcceleratorParams(numHeads = 2, headDim = 4))) { dut =>
      dut.io.start.poke(true.B)
      dut.io.seqLen.poke(4.U)
      for (h <- 0 until 2; d <- 0 until 4) {
        dut.io.q(h)(d).poke(1.S)
        dut.io.k(h)(d).poke(1.S)
        dut.io.v(h)(d).poke(1.S)
      }
      dut.clock.step()
      dut.io.start.poke(false.B)
      dut.clock.step()
      dut.io.done.expect(true.B)
    }
  }
}
