import re
from pathlib import Path

p = Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/views/CheckinApply.vue")
t = p.read_text(encoding="utf-8")
replacement = """const form = reactive({
  elderName: '',
  elderIdcard: '',
  elderPhone: '',
  gender: '',
  birthDate: '',
  age: null,
  address: '',
  ethnicity: '',
  politicalStatus: '',
  religion: '',
  maritalStatus: '',
  educationLevel: '',
  incomeSource: '',
  medicalInsurance: '',
  hobbies: '',
  medicalInsuranceNo: '',
  applicant: '',
  creator: '',
  checkinDate: ''
})"""
t2, n = re.subn(r"const form = reactive\(\{[\s\S]*?\}\)", replacement, t, count=1)
if n != 1:
    raise SystemExit(f"replace failed: {n}")
p.write_text(t2, encoding="utf-8", newline="\n")
print("ok")
