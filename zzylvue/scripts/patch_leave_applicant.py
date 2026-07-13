# -*- coding: utf-8 -*-
"""Patch LeaveManage save() to fill applicant from /loadInfo."""
from pathlib import Path

ROOTS = [
    Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/views/LeaveManage.vue"),
    Path(r"D:/vsc-maven/zzylvue/src/views/LeaveManage.vue"),
]

OLD = """function save() {
  if (!form.elderName || !form.elderIdcard) {
    ElMessage.warning('\\u8bf7\\u9009\\u62e9\\u8001\\u4eba')
    return
  }
  if (!form.startTime || !form.expectReturnTime) {
    ElMessage.warning('\\u8bf7\\u586b\\u5199\\u8bf7\\u5047\\u65f6\\u95f4')
    return
  }
  if (!form.reason) {
    ElMessage.warning('\\u8bf7\\u586b\\u5199\\u8bf7\\u5047\\u539f\\u56e0')
    return
  }
  axios.post('/leave/save', { ...form }).then(res => {"""

# Actual file has real unicode chars not escaped - match those
OLD2 = (
    "function save() {\n"
    "  if (!form.elderName || !form.elderIdcard) {\n"
    "    ElMessage.warning('\u8bf7\u9009\u62e9\u8001\u4eba')\n"
    "    return\n"
    "  }\n"
    "  if (!form.startTime || !form.expectReturnTime) {\n"
    "    ElMessage.warning('\u8bf7\u586b\u5199\u8bf7\u5047\u65f6\u95f4')\n"
    "    return\n"
    "  }\n"
    "  if (!form.reason) {\n"
    "    ElMessage.warning('\u8bf7\u586b\u5199\u8bf7\u5047\u539f\u56e0')\n"
    "    return\n"
    "  }\n"
    "  axios.post('/leave/save', { ...form }).then(res => {"
)

NEW = (
    "async function save() {\n"
    "  if (!form.elderName || !form.elderIdcard) {\n"
    "    ElMessage.warning('\u8bf7\u9009\u62e9\u8001\u4eba')\n"
    "    return\n"
    "  }\n"
    "  if (!form.startTime || !form.expectReturnTime) {\n"
    "    ElMessage.warning('\u8bf7\u586b\u5199\u8bf7\u5047\u65f6\u95f4')\n"
    "    return\n"
    "  }\n"
    "  if (!form.reason) {\n"
    "    ElMessage.warning('\u8bf7\u586b\u5199\u8bf7\u5047\u539f\u56e0')\n"
    "    return\n"
    "  }\n"
    "  try {\n"
    "    const info = await axios.get('/loadInfo')\n"
    "    const name = info.data?.realname || info.data?.uname || ''\n"
    "    if (name) {\n"
    "      form.applicant = name\n"
    "      form.creator = name\n"
    "    }\n"
    "  } catch (e) { /* ignore */ }\n"
    "  axios.post('/leave/save', { ...form }).then(res => {"
)

for p in ROOTS:
    if not p.exists():
        continue
    t = p.read_text(encoding="utf-8")
    if OLD2 not in t:
        print("pattern not found in", p)
        # show around save
        idx = t.find("function save()")
        print(repr(t[idx:idx+350]))
        continue
    p.write_text(t.replace(OLD2, NEW, 1), encoding="utf-8", newline="\n")
    print("patched", p)
