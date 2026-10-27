#!/usr/bin/env python3
"""
Generate Chinese TTS audio files for Iqra Chinese sentences.

SETUP (run once):
    pip install gtts pydub

USAGE:
    python generate_sentence_audio.py

Output: 20 MP3 files in ./sentence_audio/
Copy them to: app/src/main/res/raw/

Each file replaces the silent placeholder already in the project.
"""

import os
import time

SENTENCES = [
    ("s_s1_1", "我喜欢喝茶。"),
    ("s_s1_2", "你好吗？"),
    ("s_s1_3", "他是我的朋友。"),
    ("s_s1_4", "今天天气很好。"),
    ("s_s1_5", "我们去吃饭吧。"),
    ("s_s1_6", "这本书很好看。"),
    ("s_s1_7", "她叫什么名字？"),
    ("s_s1_8", "今天是星期几？"),
    ("s_s2_1", "我每天早上跑步。"),
    ("s_s2_2", "这个超市的东西很便宜。"),
    ("s_s2_3", "今天天气不好，下雨了。"),
    ("s_s2_4", "我坐地铁去上班。"),
    ("s_s2_5", "你的手机号码是多少？"),
    ("s_s3_1", "我认为这个问题很难解决。"),
    ("s_s3_2", "这次机会对我来说非常重要。"),
    ("s_s3_3", "你应该多锻炼，注意身体健康。"),
    ("s_s4_1", "这项政策对社会发展有很大影响。"),
    ("s_s4_2", "只有通过不断学习，才能提高自己的能力。"),
    ("s_s5_1", "他的逻辑思维非常清晰，能快速解决复杂问题。"),
    ("s_s6_1", "夕阳的余晖蔓延在蜿蜒的山路上，令人惆怅。"),
]

def generate():
    try:
        from gtts import gTTS
    except ImportError:
        print("ERROR: gtts not installed. Run: pip install gtts")
        return

    out_dir = "sentence_audio"
    os.makedirs(out_dir, exist_ok=True)

    for i, (name, text) in enumerate(SENTENCES, 1):
        out_path = os.path.join(out_dir, f"{name}.mp3")
        print(f"[{i}/{len(SENTENCES)}] Generating: {name} — {text}")
        try:
            tts = gTTS(text=text, lang='zh-TW', slow=False)
            tts.save(out_path)
            print(f"  ✅ Saved: {out_path} ({os.path.getsize(out_path)} bytes)")
            time.sleep(0.5)  # polite delay to avoid rate limiting
        except Exception as e:
            print(f"  ❌ Error: {e}")

    print(f"\nDone! {len(SENTENCES)} files in ./{out_dir}/")
    print("\nNext steps:")
    print("1. Copy all *.mp3 files from sentence_audio/ to:")
    print("   app/src/main/res/raw/")
    print("2. Rebuild the app")
    print("3. On MIUI devices without TTS, sentences will now play from bundled audio")

if __name__ == "__main__":
    generate()
