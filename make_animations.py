"""Builds the README animations from the screenshots in ./screenshots."""
from PIL import Image, ImageDraw, ImageFont
import glob, os

W, H = 360, 800           # output phone size
CAP_H = 90                # caption strip
BG = (10, 10, 20)
ACCENT = (245, 200, 66)

def font(size, bold=True):
    for p in ["/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
              "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf"]:
        if os.path.exists(p):
            return ImageFont.truetype(p, size)
    return ImageFont.load_default()

def frame_with_caption(img, title, sub):
    canvas = Image.new("RGB", (W, H + CAP_H), BG)
    canvas.paste(img.resize((W, H), Image.LANCZOS), (0, 0))
    d = ImageDraw.Draw(canvas)
    d.rectangle([0, H, W, H + 3], fill=ACCENT)
    d.text((16, H + 14), title, font=font(20), fill=ACCENT)
    d.text((16, H + 48), sub, font=font(13, False), fill=(200, 200, 220))
    return canvas

# ---------- 1. App tour (cross-fade slideshow) ----------
steps = [
    ("screenshots/01-home.jpg", "1 · Home", "Track mastery per HSK level"),
    ("screenshots/06-sentences.jpg", "2 · Sentences", "Type pinyin + meaning, 60% to advance"),
    ("screenshots/03-test.jpg", "3 · Test", "Pick level, words/sentences, mode"),
    ("screenshots/02-stats.jpg", "4 · Stats", "Accuracy, streak, 7-day charts"),
    ("screenshots/04-settings-top.jpg", "5 · Settings", "Backup, daily goal, display, alarm"),
    ("screenshots/05-settings-bottom.jpg", "6 · More settings", "TTS, day start, reset options"),
]
imgs = [frame_with_caption(Image.open(p).convert("RGB"), t, s) for p, t, s in steps]
frames, durs = [], []
HOLD, FADE_STEPS = 1600, 6
for i, im in enumerate(imgs):
    frames.append(im); durs.append(HOLD)
    nxt = imgs[(i + 1) % len(imgs)]
    for k in range(1, FADE_STEPS):
        frames.append(Image.blend(im, nxt, k / FADE_STEPS)); durs.append(70)
frames = [f.convert("P", palette=Image.ADAPTIVE, colors=128) for f in frames]
frames[0].save("animations/app-tour.gif", save_all=True, append_images=frames[1:],
               duration=durs, loop=0, optimize=True)

# ---------- 2. Settings scroll ----------
top = Image.open("screenshots/04-settings-top.jpg").convert("RGB")
bot = Image.open("screenshots/05-settings-bottom.jpg").convert("RGB")
OFFSET = 2010                      # "Hours 0-12" line: y=2123 (top) == y=113 (bottom)
STATUS, NAV = 100, 200             # fixed bars
long = Image.new("RGB", (1080, 2115 + (2400 - 105)), BG)
long.paste(top.crop((0, 0, 1080, 2115)), (0, 0))
long.paste(bot.crop((0, 105, 1080, 2400)), (0, 2115))
view_h = 2400 - STATUS - NAV
scroll_max = long.height - STATUS - view_h - 0
status_bar = top.crop((0, 0, 1080, STATUS))
nav_bar = top.crop((0, 2400 - NAV, 1080, 2400))
frames = []
positions = list(range(STATUS, long.height - (2400 - NAV) + 1, 60))
# ease in/out
import math
total = len(positions)
def ease(t): return 0.5 - 0.5 * math.cos(math.pi * t)
seq = [ease(i / 29) for i in range(30)] + [1.0] * 8 + [ease(1 - i / 29) for i in range(30)] + [0.0] * 8
y_min, y_max = STATUS, long.height - (2400 - NAV)
for t in seq:
    y = int(y_min + t * (y_max - y_min))
    f = Image.new("RGB", (1080, 2400), BG)
    f.paste(long.crop((0, y, 1080, y + view_h)), (0, STATUS))
    f.paste(status_bar, (0, 0)); f.paste(nav_bar, (0, 2400 - NAV))
    f = f.resize((W, H), Image.LANCZOS)
    frames.append(f.convert("P", palette=Image.ADAPTIVE, colors=128))
frames[0].save("animations/settings-scroll.gif", save_all=True, append_images=frames[1:],
               duration=[60] * len(frames), loop=0, optimize=True)
print("ok")
