from pathlib import Path

p = Path(r"D:/vsc-maven/zzyl-project/zzylvue/src/views/CheckinDetail.vue")
t = p.read_text(encoding="utf-8")

marker = '<table class="ability-table">'
idx = t.find(marker)
if idx < 0:
    raise SystemExit("ability-table not found")

end = t.find("</table>", idx)
if end < 0:
    raise SystemExit("closing table not found")
end += len("</table>")

block = t[idx:end]
if "<tbody>" in block:
    raise SystemExit("tbody already present")

# Insert tbody after opening table tag and before closing table tag
inner_start = idx + len(marker)
inner = t[inner_start:end - len("</table>")]
new_block = marker + "\n                    <tbody>" + inner + "                    </tbody>\n                  </table>"
t = t[:idx] + new_block + t[end:]
p.write_text(t, encoding="utf-8", newline="\n")
print("ok: wrapped ability-table with tbody")
