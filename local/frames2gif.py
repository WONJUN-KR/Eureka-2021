"""screencast 프레임(jpg) + 시각(times.json) → GIF. 실제 시간 간격을 지켜서 재생 속도가 녹화와 같게."""
import json
import sys
from pathlib import Path

from PIL import Image

src, out = Path(sys.argv[1]), sys.argv[2]
WIDTH = int(sys.argv[3]) if len(sys.argv) > 3 else 800
FPS = int(sys.argv[4]) if len(sys.argv) > 4 else 12
times = json.loads((src / "times.json").read_text())
files = sorted(src.glob("*.jpg"))
# 녹화 시각 기준으로 FPS 간격마다 한 장씩만 골라 용량을 줄임 (화면이 안 바뀌는 구간은 한 장이 길게 유지)
picked, last = [], None
for i, t in enumerate(times[: len(files)]):
    if last is None or t - last >= 1000 / FPS:
        picked.append(i)
        last = t
frames, durations = [], []
for n, i in enumerate(picked):
    im = Image.open(files[i]).convert("RGB")
    im = im.resize((WIDTH, round(im.height * WIDTH / im.width)), Image.LANCZOS)
    frames.append(im.quantize(colors=256, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.FLOYDSTEINBERG))
    nxt = times[picked[n + 1]] if n + 1 < len(picked) else times[i] + 1500
    durations.append(max(40, nxt - times[i]))
frames[0].save(out, save_all=True, append_images=frames[1:], duration=durations, loop=0, optimize=True, disposal=1)
size = Path(out).stat().st_size / 1024 / 1024
print(f"{Path(out).name}: {len(frames)}프레임, {sum(durations)/1000:.1f}초, {size:.1f}MB")
