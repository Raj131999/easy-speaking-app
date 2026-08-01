import re

with open("conversations_generated.kt", "r") as f:
    new_conversations_code = f.read()

with open("app/src/main/java/com/example/data/InitialData.kt", "r") as f:
    initial_data = f.read()

# Replace val conversations = listOf(...)
pattern = r"    val conversations = listOf\(\n[\s\S]*?\n    \)"
updated_data = re.sub(pattern, new_conversations_code, initial_data)

with open("app/src/main/java/com/example/data/InitialData.kt", "w") as f:
    f.write(updated_data)

print("Updated InitialData.kt successfully!")
