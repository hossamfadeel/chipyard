# Chipyard Local Generator Wiring

This directory is a working copy of the top-level `generators/llm-accel` project, mirrored into the Chipyard checkout so that local integration experiments can reference it directly.

The current integration pass adds a lightweight RoCC wrapper under `chipyard/generators/chipyard/src/main/scala/llmaccel/LLMAccelRoCC.scala` and a Chipyard-style config fragment under `chipyard/generators/chipyard/src/main/scala/config/LLMAcceleratorConfigs.scala`.

This pass should be considered an initial wiring layer rather than a complete diplomatic, TileLink-coherent, or production-validated integration.
