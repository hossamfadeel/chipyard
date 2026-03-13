package llmaccel.configs

object ChipyardConfigs {
  final case class SoCConfig(name: String, params: LLMAcceleratorParams, hostCore: String)
  val LLMAcceleratorKR260Config = SoCConfig("LLMAcceleratorKR260Config", LLMAcceleratorDefaults.kr260, "Rocket")
  val LLMAcceleratorASICConfig = SoCConfig("LLMAcceleratorASICConfig", LLMAcceleratorDefaults.asic, "BOOM")
}
