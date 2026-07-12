# -*- coding: utf-8 -*-
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src"
markers = ["\u20ac", "\ufffd", "\u00e6", "\u0080", "\u009d"]
found = []
for dp, _, fs in os.walk(ROOT):
    for fn in fs:
        if not fn.endswith((".vue", ".js")):
            continue
        p = Path(dp) / fn
        t = p.read_text(encoding="utf-8")
        hits = [m for m in markers if m in t]
        if hits:
            found.append((str(p.relative_to(ROOT)), hits))
        # common mojibake tail
        if "ву" in t or "Д1д7" in t:
            found.append((str(p.relative_to(ROOT)), ["euro_or_replacement"]))

out = Path(__file__).resolve().parent / "scan_result.txt"
lines = [f"{rel}: {hits}" for rel, hits in found]
out.write_text("\n".join(lines) if lines else "all clean", encoding="utf-8")
