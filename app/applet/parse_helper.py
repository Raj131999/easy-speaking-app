import json
import re

# Read from raw text or embed data
# We will write the parser to clean and generate assets/ted_paragraphs.json

def clean_text(text):
    # Remove transcript tags like [music], (Laughter), [applause], (Applause), >>, etc.
    text = re.sub(r'\[.*?\]', '', text)
    text = re.sub(r'\(.*?\)', '', text)
    text = re.sub(r'>>', '', text)
    # Clean multiple spaces and line breaks
    lines = [line.strip() for line in text.split('\n') if line.strip()]
    cleaned = ' '.join(lines)
    cleaned = re.sub(r'\s+', ' ', cleaned)
    return cleaned.strip()

def clean_title(title_line):
    # e.g., "1. THE TRANSFORMATIVE POWER OF “HELLO” | SANDRA CARR | TEDXGARY"
    title_line = re.sub(r'^\d+\.\s*', '', title_line).strip()
    parts = title_line.split('|')
    main_title = parts[0].strip()
    
    # Title case if all caps
    if main_title.isupper():
        # Title case while preserving short words
        words = main_title.split()
        main_title = ' '.join([w.capitalize() if len(w) > 3 or i == 0 else w.lower() for i, w in enumerate(words)])
    
    if len(parts) > 1:
        speaker = parts[1].strip().title()
        return f"{main_title} ({speaker})"
    return main_title

def calculate_time(text):
    word_count = len(text.split())
    # average reading speed ~ 130 wpm = ~2.2 words per sec
    seconds = max(30, int(word_count / 2.2))
    return seconds

def determine_level(word_count):
    if word_count < 350:
        return "Beginner"
    elif word_count < 800:
        return "Intermediate"
    else:
        return "Advanced"

print("Helper functions ready")
