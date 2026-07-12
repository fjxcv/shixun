# -*- coding: utf-8 -*-
import re
from pathlib import Path

path = Path(r"D:\vsc-maven\zzyl-project\zzylvue\src\views\Workbench.vue")
text = path.read_text(encoding="utf-8")
m = re.search(r'header="([^"]+)"', text)
s = m.group(1)
lines = []
lines.append("before: " + s)

for method in ["utf8_gbk", "gbk_utf8", "latin1", "cp1252"]:
    try:
        if method == "utf8_gbk":
            fixed = s.encode("utf-8").decode("gbk")
        elif method == "gbk_utf8":
            fixed = s.encode("gbk").decode("utf-8")
        elif method == "latin1":
            fixed = s.encode("latin1").decode("utf-8")
        else:
            fixed = s.encode("cp1252").decode("utf-8")
        lines.append(f"{method} -> {fixed}")
    except Exception as e:
        lines.append(f"{method} ERR {e}")

for method in ["utf8_gbk"]:
    try:
        fixed = text.encode("utf-8").decode("gbk")
        cjk = len(re.findall(r"[\u4e00-\u9fff]", fixed))
        lines.append(f"whole file utf8_gbk cjk={cjk}")
        lines.append(fixed[400:520])
    except Exception as e:
        lines.append(f"whole file ERR {e}")

Path(r"D:\vsc-maven\zzyl-project\zzylvue\scripts\test_out.txt").write_text("\n".join(lines), encoding="utf-8")
