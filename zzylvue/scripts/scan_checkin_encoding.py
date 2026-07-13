# -*- coding: utf-8 -*-
from pathlib import Path

files = [
    Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/views/CheckinProcess.vue"),
    Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/views/CheckinDetail.vue"),
    Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/views/CheckinApply.vue"),
    Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/components/checkin/InfoSectionsCheckin.vue"),
    Path(r"D:/vsc-maven/zzylvue/src/views/CheckinProcess.vue"),
]

needles = [
    "\u5355\u636e\u7f16\u53f7",
    "\u53d1\u8d77\u5165\u4f4f",
    "\u8001\u4eba\u59d3\u540d",
    "\u8865\u8db3\u7533\u8bf7",
    "\u5065\u5eb7\u8bc4\u4f30",
]

for p in files:
    if not p.exists():
        print("MISSING", p)
        continue
    raw = p.read_bytes()
    print("===", p.name, "parent=", p.parent.parent.name if p.parent.name == "views" else p.parent.name, "bytes", len(raw))
    for enc in ["utf-8", "gbk"]:
        try:
            t = raw.decode(enc)
        except UnicodeDecodeError as e:
            print(f"  {enc}: FAIL")
            continue
        cjk = sum(1 for c in t if "\u4e00" <= c <= "\u9fff")
        hits = {n: (n in t) for n in needles}
        print(f"  {enc}: OK cjk={cjk} hits={hits}")
        if enc == "utf-8":
            for line in t.splitlines():
                if any("\u4e00" <= c <= "\u9fff" for c in line):
                    print("  sample_hex:", line.strip()[:40].encode("unicode_escape").decode())
                    break
