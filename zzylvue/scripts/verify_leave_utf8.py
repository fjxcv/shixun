# -*- coding: utf-8 -*-
from pathlib import Path
import re

p = Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/views/LeaveManage.vue")
t = p.read_text(encoding="utf-8")
m = re.search(r"flowStatus === '([^']+)'", t)
print("flowStatus filter:", repr(m.group(1)) if m else None)
m2 = re.search(r"status: '([^']+)'", t)
print("init status:", repr(m2.group(1)) if m2 else None)
print("filterable:", "filterable" in t)
print("approveVisible:", "approveVisible" in t)
print("pending in file:", "\u5f85\u5ba1\u6279" in t)
print("completed in file:", "\u5df2\u5b8c\u6210" in t)

for name in ["LeaveDetail.vue", "MyTodo.vue", "MyApply.vue"]:
    tt = Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/views") / name
    s = tt.read_text(encoding="utf-8")
    print(name, "OK", "\u5f85\u5ba1\u6279" in s or "\u5f85\u5904\u7406" in s or "\u5168\u90e8" in s)
