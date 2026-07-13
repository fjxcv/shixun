# -*- coding: utf-8 -*-
from pathlib import Path

b = Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/views/Register.vue").read_bytes()
assert "\u6ce8\u518c\u8d26\u53f7".encode("utf-8") in b
assert b"axios.post('/register'" in b
b2 = Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/components/checkout/OperationTimeline.vue").read_bytes()
assert "\u62a4\u7406\u7ec4\u4e3b\u7ba1\u5904\u7406".encode("utf-8") in b2
assert "\uff08\u89d2\u8272\uff09".encode("utf-8") not in b2
print("utf8 asserts ok")
t = b2.decode("utf-8")
for line in t.splitlines():
    if "name:" in line and "prefix" in line:
        print(line.strip())
