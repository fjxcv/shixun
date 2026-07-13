# -*- coding: utf-8 -*-
from pathlib import Path
import re

t = Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/views/LeaveManage.vue").read_text(encoding="utf-8")
m = re.search(r"flowStatus === '([^']+)'", t)
s = m.group(1)
print("hex:", s.encode("utf-8").hex())
print("eq completed:", s == "\u5df2\u5b8c\u6210")
print("eq pending:", s == "\u5f85\u5ba1\u6279")

# all status string literals in comparisons
for m in re.finditer(r"(?:===|status:\s*)'([^']+)'", t):
    val = m.group(1)
    if any("\u4e00" <= c <= "\u9fff" for c in val):
        print(repr(val), val.encode("utf-8").hex())
