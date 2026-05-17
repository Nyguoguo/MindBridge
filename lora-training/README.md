# MindBridge LoRA Fine-Tuning

## Overview

Fine-tune a large language model using LoRA (Low-Rank Adaptation) on psychological counseling conversation data to improve psychological state recognition accuracy to 90%.

## Quick Start

```bash
# 1. Install dependencies
pip install -r requirements.txt

# 2. Generate training data
python generate_data.py

# 3. Train LoRA adapter (requires ~16GB GPU VRAM with 4-bit quantization)
python train.py --epochs 3 --batch-size 2 --output ./output-lora

# 4. Evaluate accuracy
python evaluate.py --adapter ./output-lora
```

## Training Arguments

| Argument | Default | Description |
|----------|---------|-------------|
| `--model` | `unsloth/Qwen2.5-7B-Instruct-bnb-4bit` | Base model |
| `--epochs` | 3 | Training epochs |
| `--batch-size` | 2 | Per-device batch size |
| `--lora-r` | 16 | LoRA rank |
| `--lora-alpha` | 32 | LoRA alpha |
| `--learning-rate` | 2e-4 | Learning rate |

## Integration with MindBridge

After training, deploy the fine-tuned model to Ollama:

```bash
# 1. Merge LoRA with base model
python evaluate.py --adapter ./output-lora --merge

# 2. Create Ollama Modelfile
cat > Modelfile << 'EOF'
FROM ./merged-model
TEMPLATE """{{ if .System }}<|system|>
{{ .System }}</s>
{{ end }}{{ if .Prompt }}<|user|>
{{ .Prompt }}</s>
{{ end }}<|assistant|>
"""
EOF

# 3. Deploy to Ollama
ollama create mindbridge-psych --file ./Modelfile

# 4. Update application.yml
# spring.ai.ollama.chat.model: mindbridge-psych
```
