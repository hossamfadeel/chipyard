package llmaccel.configs

import chisel3._

case class LLMAcceleratorParams(
  systolicDim: Int = 16,
  dataWidth: Int = 8,
  accWidth: Int = 32,
  hiddenDim: Int = 4096,
  numHeads: Int = 32,
  headDim: Int = 128,
  kvCacheEntries: Int = 2048,
  weightBufferBytes: Int = 512 * 1024,
  activationBufferBytes: Int = 256 * 1024,
  kvBankBytes: Int = 1024 * 1024,
  supportFP16: Boolean = true,
  supportBF16: Boolean = true,
  supportINT8: Boolean = true,
  supportINT4: Boolean = true,
  supportFP8: Boolean = false,
  supportMQA: Boolean = true,
  supportGQA: Boolean = true,
  supportSlidingWindow: Boolean = true,
  supportPagedAttention: Boolean = true,
  dmaMaxBurstBytes: Int = 256,
  maxBatchSize: Int = 8,
  maxSequenceLength: Int = 4096,
  enableECC: Boolean = false,
  enablePerfCounters: Boolean = true,
  tileId: Int = 0
)

object LLMAcceleratorDefaults {
  val kr260: LLMAcceleratorParams = LLMAcceleratorParams(
    systolicDim = 16,
    dataWidth = 8,
    accWidth = 32,
    kvCacheEntries = 1024,
    maxBatchSize = 4,
    maxSequenceLength = 2048,
    enableECC = false
  )

  val asic: LLMAcceleratorParams = LLMAcceleratorParams(
    systolicDim = 32,
    dataWidth = 8,
    accWidth = 32,
    kvCacheEntries = 4096,
    maxBatchSize = 8,
    maxSequenceLength = 8192,
    enableECC = true
  )
}

object PrecisionKind {
  val INT4  = 0.U(3.W)
  val INT8  = 1.U(3.W)
  val FP16  = 2.U(3.W)
  val BF16  = 3.U(3.W)
  val FP8   = 4.U(3.W)
}
