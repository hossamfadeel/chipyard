package llmaccel

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import llmaccel.configs.LLMAcceleratorParams
import llmaccel.systolic.SystolicArray

class SystolicArraySpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SystolicArray"
  it should "produce a done pulse after start" in {
    test(new SystolicArray(LLMAcceleratorParams(systolicDim = 2))) { dut =>
      for (i <- 0 until 2; j <- 0 until 2) {
        dut.io.a(i)(j).poke((i + j + 1).S)
        dut.io.b(i)(j).poke((i + 1).S)
      }
      dut.io.start.poke(true.B)
      dut.clock.step()
      dut.io.start.poke(false.B)
      dut.clock.step()
      dut.io.done.expect(true.B)
    }
  }
}
