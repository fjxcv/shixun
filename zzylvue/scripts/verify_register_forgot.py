# -*- coding: utf-8 -*-
from pathlib import Path

p1 = Path(r"D:/vsc-maven/zzyl-project/zzylvue")
p2 = Path(r"D:/vsc-maven/zzylvue")

for root in [p1, p2]:
    print("===", root)
    for rel in [
        "src/views/Register.vue",
        "src/views/ForgotPassword.vue",
        "src/router/index.js",
        "src/views/HomeView.vue",
        "vue.config.js",
        "src/components/checkout/OperationTimeline.vue",
    ]:
        p = root / rel
        print(rel, "exists=", p.exists())
        if not p.exists():
            continue
        raw = p.read_bytes()
        enc = "utf-8"
        try:
            text = raw.decode("utf-8")
        except UnicodeDecodeError:
            text = raw.decode("gbk")
            enc = "gbk"
        checks = []
        if "Register" in rel:
            checks.append(("title", "\u6ce8\u518c\u8d26\u53f7" in text))
        if "Forgot" in rel:
            checks.append(("title", "\u5fd8\u8bb0\u5bc6\u7801" in text))
        if "router" in rel:
            checks.append(("register route", "/register" in text))
            checks.append(("forgot route", "/forgotPassword" in text))
        if "HomeView" in rel:
            checks.append(("forgot link", "forgotPassword" in text))
            checks.append(("register link", "/register" in text))
        if "vue.config" in rel:
            checks.append(("register proxy", "'/register'" in text))
            checks.append(("resetPwd proxy", "'/resetPwd'" in text))
        if "OperationTimeline" in rel:
            checks.append(("old role", "\uff08\u89d2\u8272\uff09" in text))
            checks.append(("new nursing", "\u62a4\u7406\u7ec4\u4e3b\u7ba1\u5904\u7406" in text))
            checks.append(("old approve role", "\u5ba1\u6279\u89d2\u8272\u5904\u7406" in text))
        print(" ", enc, checks)
