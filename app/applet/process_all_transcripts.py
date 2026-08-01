import json
import re
import os
import sys

script_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.append(script_dir)

from para_builder import add_para, paragraphs

files = [
    os.path.join(script_dir, "raw_transcripts_1_5.txt"),
    os.path.join(script_dir, "raw_transcripts_6_15.txt"),
    os.path.join(script_dir, "raw_transcripts_16_25.txt"),
    os.path.join(script_dir, "raw_transcripts_26_35.txt"),
    os.path.join(script_dir, "raw_transcripts_36_45.txt"),
    os.path.join(script_dir, "raw_transcripts_46_55.txt"),
    os.path.join(script_dir, "raw_transcripts_56_60.txt")
]

current_id = 1

for file_path in files:
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        continue
    
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()
    
    sections = content.split("## ")
    for sec in sections:
        sec = sec.strip()
        if not sec:
            continue
        
        lines = sec.split("\n", 1)
        raw_title = lines[0].strip()
        text_body = lines[1].strip() if len(lines) > 1 else ""
        
        add_para(current_id, raw_title, text_body)
        current_id += 1

output_dir = "app/src/main/assets"
os.makedirs(output_dir, exist_ok=True)
output_file = os.path.join(output_dir, "ted_paragraphs.json")

with open(output_file, "w", encoding="utf-8") as f:
    json.dump(paragraphs, f, indent=2, ensure_ascii=False)

print(f"Successfully processed {len(paragraphs)} paragraphs into {output_file}")
