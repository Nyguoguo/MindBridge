"""Evaluate fine-tuned model on psychological state recognition accuracy."""
import json
import torch
import argparse
from transformers import AutoModelForCausalLM, AutoTokenizer
from peft import PeftModel
from datasets import Dataset


PSYCH_TEST_CASES = [
    {
        "input": "我最近总是失眠，心情很差，觉得什么都没意思。我是不是抑郁了？",
        "expected_category": "depression",
        "expected_signals": ["失眠", "情绪低落", "兴趣丧失"],
    },
    {
        "input": "每次考试前我就紧张到胃疼，整晚睡不着。",
        "expected_category": "anxiety",
        "expected_signals": ["考试焦虑", "躯体化", "睡眠障碍"],
    },
    {
        "input": "我已经连续一周每天只睡三个小时了，白天完全没精神。",
        "expected_category": "insomnia",
        "expected_signals": ["严重失眠", "日间功能受损"],
    },
    {
        "input": "我觉得活着好累，有时候想就这样睡着不要醒过来了。",
        "expected_category": "high_risk",
        "expected_signals": ["自杀意念", "绝望感", "生命威胁"],
    },
    {
        "input": "最近开始锻炼后，我感觉好多了，睡眠质量也提高了。",
        "expected_category": "normal",
        "expected_signals": ["积极改善", "健康行为"],
    },
    {
        "input": "我在宿舍无法和任何人交流，一说话就脸红心跳。",
        "expected_category": "social_anxiety",
        "expected_signals": ["社交回避", "躯体反应", "人际关系困难"],
    },
    {
        "input": "压力好大，但是和朋友聊完之后觉得好多了。",
        "expected_category": "normal",
        "expected_signals": ["社会支持", "情绪调节"],
    },
    {
        "input": "我觉得我什么都做不好，在班级里最差的那个就是我。",
        "expected_category": "depression",
        "expected_signals": ["低自尊", "自我否定", "抑郁认知"],
    },
]

ASSESSMENT_PROMPT = """请分析以下用户消息的心理状态，按以下格式输出：
类别: [depression/anxiety/insomnia/high_risk/normal/social_anxiety]
风险等级: [LOW/MEDIUM/HIGH]
识别信号: [具体的关键词或短语]
建议: [专业建议]

用户消息: {user_input}
"""


def evaluate_case(model, tokenizer, case):
    """Evaluate a single test case."""
    prompt = ASSESSMENT_PROMPT.format(user_input=case["input"])
    messages = [{"role": "user", "content": prompt}]
    text = tokenizer.apply_chat_template(messages, tokenize=False)

    inputs = tokenizer(text, return_tensors="pt").to(model.device)
    with torch.no_grad():
        outputs = model.generate(**inputs, max_new_tokens=256, temperature=0.1, do_sample=True)
    response = tokenizer.decode(outputs[0][inputs["input_ids"].shape[1]:], skip_special_tokens=True)

    # Check if expected category is in response
    category_match = case["expected_category"] in response.lower()
    signal_match = any(signal in response for signal in case["expected_signals"])

    return {
        "input": case["input"][:50],
        "expected": case["expected_category"],
        "response": response[:200],
        "category_correct": category_match,
        "signals_detected": signal_match,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-model", default="unsloth/Qwen2.5-7B-Instruct-bnb-4bit")
    parser.add_argument("--adapter", default="./output-lora")
    parser.add_argument("--merge", action="store_true", help="Merge adapter with base model")
    args = parser.parse_args()

    print(f"Loading base model: {args.base_model}")
    model = AutoModelForCausalLM.from_pretrained(
        args.base_model, device_map="auto", torch_dtype=torch.bfloat16, trust_remote_code=True)
    tokenizer = AutoTokenizer.from_pretrained(args.base_model, trust_remote_code=True)

    print(f"Loading LoRA adapter: {args.adapter}")
    model = PeftModel.from_pretrained(model, args.adapter)
    if args.merge:
        model = model.merge_and_unload()
        print("Adapter merged with base model")

    print(f"\nEvaluating {len(PSYCH_TEST_CASES)} test cases...\n")
    results = []
    correct = 0
    signals_correct = 0

    for case in PSYCH_TEST_CASES:
        result = evaluate_case(model, tokenizer, case)
        results.append(result)
        if result["category_correct"]:
            correct += 1
        if result["signals_detected"]:
            signals_correct += 1
        status = "PASS" if result["category_correct"] else "FAIL"
        print(f"[{status}] {result['input']}...")
        print(f"  Expected: {case['expected_category']} | Response: {result['response'][:100]}...")
        print()

    accuracy = correct / len(PSYCH_TEST_CASES) * 100
    signal_accuracy = signals_correct / len(PSYCH_TEST_CASES) * 100
    print(f"{'='*60}")
    print(f"Category Recognition Accuracy: {accuracy:.1f}% ({correct}/{len(PSYCH_TEST_CASES)})")
    print(f"Signal Detection Accuracy: {signal_accuracy:.1f}% ({signals_correct}/{len(PSYCH_TEST_CASES)})")
    print(f"{'='*60}")
    print(f"Target: 90% | Gap: {90 - accuracy:.1f}%")


if __name__ == "__main__":
    main()
