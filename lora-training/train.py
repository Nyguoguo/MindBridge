"""LoRA Fine-Tuning Script for Psychological Counseling Model."""
import os
import json
import torch
from datasets import Dataset
from transformers import (
    AutoModelForCausalLM,
    AutoTokenizer,
    TrainingArguments,
    BitsAndBytesConfig,
)
from peft import LoraConfig, get_peft_model, TaskType, PeftModel
from trl import SFTTrainer
import argparse


def load_data(filepath):
    """Load JSONL data and convert to dataset."""
    texts = []
    with open(filepath, "r", encoding="utf-8") as f:
        for line in f:
            item = json.loads(line)
            # Format as chat template
            messages = item["messages"]
            text = ""
            for msg in messages:
                role = msg["role"]
                content = msg["content"]
                if role == "system":
                    text += f"<|system|>\n{content}</s>\n"
                elif role == "user":
                    text += f"<|user|>\n{content}</s>\n"
                elif role == "assistant":
                    text += f"<|assistant|>\n{content}</s>\n"
            texts.append(text)
    return Dataset.from_dict({"text": texts})


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", default="unsloth/Qwen2.5-7B-Instruct-bnb-4bit",
                        help="Base model to fine-tune")
    parser.add_argument("--output", default="./output-lora",
                        help="Output directory for LoRA adapter")
    parser.add_argument("--epochs", type=int, default=3)
    parser.add_argument("--batch-size", type=int, default=2)
    parser.add_argument("--learning-rate", type=float, default=2e-4)
    parser.add_argument("--grad-accum", type=int, default=4,
                        help="Gradient accumulation steps")
    parser.add_argument("--use-4bit", action="store_true", default=True,
                        help="Use 4-bit quantization")
    parser.add_argument("--lora-r", type=int, default=16)
    parser.add_argument("--lora-alpha", type=int, default=32)
    args = parser.parse_args()

    print(f"Loading model: {args.model}")

    # Quantization config
    bnb_config = None
    if args.use_4bit:
        bnb_config = BitsAndBytesConfig(
            load_in_4bit=True,
            bnb_4bit_quant_type="nf4",
            bnb_4bit_compute_dtype=torch.bfloat16,
            bnb_4bit_use_double_quant=True,
        )

    # Load model
    model = AutoModelForCausalLM.from_pretrained(
        args.model,
        quantization_config=bnb_config,
        device_map="auto",
        trust_remote_code=True,
        torch_dtype=torch.bfloat16,
    )

    tokenizer = AutoTokenizer.from_pretrained(args.model, trust_remote_code=True)
    tokenizer.pad_token = tokenizer.eos_token
    tokenizer.padding_side = "right"

    # LoRA config
    lora_config = LoraConfig(
        task_type=TaskType.CAUSAL_LM,
        r=args.lora_r,
        lora_alpha=args.lora_alpha,
        lora_dropout=0.05,
        target_modules=["q_proj", "k_proj", "v_proj", "o_proj", "gate_proj", "up_proj", "down_proj"],
        bias="none",
    )

    model = get_peft_model(model, lora_config)
    model.print_trainable_parameters()

    # Load data
    print("Loading training data...")
    train_dataset = load_data("data/train.jsonl")
    val_dataset = load_data("data/val.jsonl")
    print(f"Train: {len(train_dataset)} samples, Val: {len(val_dataset)} samples")

    # Training args
    training_args = TrainingArguments(
        output_dir=args.output,
        num_train_epochs=args.epochs,
        per_device_train_batch_size=args.batch_size,
        per_device_eval_batch_size=args.batch_size,
        gradient_accumulation_steps=args.grad_accum,
        learning_rate=args.learning_rate,
        warmup_ratio=0.03,
        lr_scheduler_type="cosine",
        logging_steps=5,
        save_strategy="epoch",
        eval_strategy="epoch",
        bf16=True,
        gradient_checkpointing=True,
        optim="paged_adamw_8bit",
        report_to="none",
        load_best_model_at_end=True,
        metric_for_best_model="eval_loss",
    )

    # Trainer
    trainer = SFTTrainer(
        model=model,
        args=training_args,
        train_dataset=train_dataset,
        eval_dataset=val_dataset,
        tokenizer=tokenizer,
        max_seq_length=2048,
        dataset_text_field="text",
    )

    print("Starting training...")
    trainer.train()

    # Save LoRA adapter
    model.save_pretrained(args.output)
    tokenizer.save_pretrained(args.output)
    print(f"LoRA adapter saved to {args.output}")

    # Evaluation
    print("Running evaluation...")
    eval_results = trainer.evaluate()
    print(f"Eval results: {eval_results}")

    # Calculate perplexity
    perplexity = torch.exp(torch.tensor(eval_results["eval_loss"]))
    print(f"Perplexity: {perplexity:.2f}")


if __name__ == "__main__":
    main()
