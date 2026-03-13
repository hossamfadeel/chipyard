package chipyard.config

import org.chipsalliance.cde.config.{Config, Parameters}
import freechips.rocketchip.tile._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.diplomacy._
import chipyard.llmaccel.LLMAccelRoCC

class WithLLMAccelRoCC(op: OpcodeSet = OpcodeSet.custom3) extends Config((site, here, up) => {
  case BuildRoCC => up(BuildRoCC) ++ Seq((p: Parameters) => {
    LazyModule(new LLMAccelRoCC(op)(p))
  })
})

class LLMAcceleratorRocketConfig extends Config(
  new chipyard.config.WithSystemBusWidth(128) ++
  new chipyard.config.WithPeripheryBusFrequency(100.0) ++
  new chipyard.config.WithMemoryBusFrequency(100.0) ++
  new chipyard.config.WithControlBusFrequency(100.0) ++
  new chipyard.config.WithLLMAccelRoCC(OpcodeSet.custom3) ++
  new chipyard.config.WithNBigCores(1) ++
  new chipyard.config.AbstractConfig)
