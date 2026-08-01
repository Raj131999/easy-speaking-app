import json
import re

paragraphs = []

def add_para(p_id, raw_title, text):
    # Clean text
    clean_txt = re.sub(r'\[.*?\]', '', text)
    clean_txt = re.sub(r'\(.*?\)', '', clean_txt)
    clean_txt = re.sub(r'>>', '', clean_txt)
    lines = [line.strip() for line in clean_txt.split('\n') if line.strip()]
    full_text = ' '.join(lines)
    full_text = re.sub(r'\s+', ' ', full_text).strip()
    
    # Clean title
    title_line = re.sub(r'^\d+\.\s*', '', raw_title).strip()
    parts = title_line.split('|')
    main_title = parts[0].strip()
    
    # Title Case if uppercase
    if main_title.isupper():
        words = main_title.split()
        main_title = ' '.join([w.capitalize() if len(w) > 3 or i == 0 else w.lower() for i, w in enumerate(words)])
    
    if len(parts) > 1:
        speaker = parts[1].strip().title()
        final_title = f"{main_title} ({speaker})"
    else:
        final_title = main_title
        
    words_count = len(full_text.split())
    reading_time = max(30, int(words_count / 2.3))
    
    if words_count < 350:
        level = "Beginner"
    elif words_count < 800:
        level = "Intermediate"
    else:
        level = "Advanced"
        
    paragraphs.append({
        "id": p_id,
        "title": final_title,
        "text": full_text,
        "level": level,
        "estimatedReadingTime": reading_time,
        "isCompleted": False,
        "maxAccuracy": 0
    })

print("add_para defined")
