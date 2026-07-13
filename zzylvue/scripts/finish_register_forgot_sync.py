# -*- coding: utf-8 -*-
from pathlib import Path

ROOTS = [
    Path(r"D:/vsc-maven/zzyl-project/zzylvue"),
    Path(r"D:/vsc-maven/zzylvue"),
]


def read_text(p: Path) -> tuple[str, str]:
    raw = p.read_bytes()
    try:
        return raw.decode("utf-8"), "utf-8"
    except UnicodeDecodeError:
        return raw.decode("gbk"), "gbk"


def write_utf8(p: Path, text: str):
    p.write_text(text, encoding="utf-8", newline="\n")
    print("wrote utf-8", p)


# timeline
old_role = "name: `\uff08\u89d2\u8272\uff09\u5904\u7406-${prefix}\u5ba1\u6279`,"
new_role = "name: `\u62a4\u7406\u7ec4\u4e3b\u7ba1\u5904\u7406-${prefix}\u5ba1\u6279`,"
old_eval = "name: `\u5ba1\u6279\u89d2\u8272\u5904\u7406-${prefix}\u8bc4\u4f30`,"
new_eval = "name: `\u5ba1\u6279-${prefix}\u8bc4\u4f30`,"
old_cfg = "name: `\u5ba1\u6279\u89d2\u8272\u5904\u7406-${prefix}\u914d\u7f6e`,"
new_cfg = "name: `\u5ba1\u6279-${prefix}\u914d\u7f6e`,"

for root in ROOTS:
    p = root / "src/components/checkout/OperationTimeline.vue"
    text, enc = read_text(p)
    print(p, "enc", enc)
    for old, new in [(old_role, new_role), (old_eval, new_eval), (old_cfg, new_cfg)]:
        c = text.count(old)
        print(" ", repr(old[:30]), "count", c)
        text = text.replace(old, new)
    write_utf8(p, text)

# copy vue.config.js proxy (gbk)
p = Path(r"D:/vsc-maven/zzylvue/vue.config.js")
text, enc = read_text(p)
print("vue.config copy enc", enc, "has register", "'/register'" in text)
needle = "'/login': apiProxy,\n      '/logout': apiProxy,"
repl = "'/login': apiProxy,\n      '/register': apiProxy,\n      '/resetPwd': apiProxy,\n      '/logout': apiProxy,"
if "'/register'" not in text:
    if needle not in text:
        # try CRLF
        needle = "'/login': apiProxy,\r\n      '/logout': apiProxy,"
        repl = "'/login': apiProxy,\r\n      '/register': apiProxy,\r\n      '/resetPwd': apiProxy,\r\n      '/logout': apiProxy,"
    if needle not in text:
        print("WARN login/logout proxy block not found")
        # show nearby
        idx = text.find("/login")
        print(repr(text[idx:idx+80]))
    else:
        text = text.replace(needle, repl, 1)
        write_utf8(p, text)
else:
    print("already has register proxy")

# ensure router not duplicated
for root in ROOTS:
    p = root / "src/router/index.js"
    text, enc = read_text(p)
    count = text.count("/register")
    print(root, "register route count", count)
    if count > 1:
        # collapse duplicate consecutive route lines
        line1 = "  { path: '/register', name: 'register', component: () => import('@/views/Register.vue') },\n"
        line2 = "  { path: '/forgotPassword', name: 'forgotPassword', component: () => import('@/views/ForgotPassword.vue') },\n"
        while text.count(line1) > 1:
            text = text.replace(line1 + line2 + line1 + line2, line1 + line2, 1)
        write_utf8(p, text)
        print(" deduped router")

print("done")
