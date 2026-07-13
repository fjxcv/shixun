from pathlib import Path

p = Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/views/CheckoutDetail.vue")
t = p.read_text(encoding="utf-8")
old = 'action-prefix="\u9000\u4f4f" />'
new = 'action-prefix="\u9000\u4f4f" :data="detail" />'
count = t.count(old)
t = t.replace(old, new)
p.write_text(t, encoding="utf-8", newline="\n")
print("replaced", count)
