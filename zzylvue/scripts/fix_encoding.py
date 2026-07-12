import os
import re
from pathlib import Path

EXTS = {".vue", ".js"}


def cjk_count(text):
    return len(re.findall(r"[\u4e00-\u9fff]", text))


def read_text(path: Path):
    raw = path.read_bytes()
    try:
        return raw.decode("utf-8"), "utf-8"
    except UnicodeDecodeError:
        try:
            return raw.decode("gbk"), "gbk"
        except UnicodeDecodeError:
            # Mixed UTF-8/GBK: decode byte-by-byte, prefer UTF-8 then GBK.
            parts = []
            i = 0
            while i < len(raw):
                for size in (3, 2, 1):
                    chunk = raw[i : i + size]
                    try:
                        parts.append(chunk.decode("utf-8"))
                        i += size
                        break
                    except UnicodeDecodeError:
                        continue
                else:
                    parts.append(raw[i : i + 1].decode("gbk", errors="replace"))
                    i += 1
            return "".join(parts), "mixed"


def process_root(root: Path):
    changed = []
    invalid_before = []
    for dirpath, _, files in os.walk(root):
        for name in files:
            path = Path(dirpath) / name
            if path.suffix not in EXTS:
                continue
            raw = path.read_bytes()
            try:
                raw.decode("utf-8")
            except UnicodeDecodeError:
                invalid_before.append(str(path.relative_to(root)))
            text, src_enc = read_text(path)
            if src_enc in ("gbk", "mixed"):
                path.write_text(text, encoding="utf-8", newline="\n")
                changed.append(str(path.relative_to(root)))
    return changed, invalid_before


def main():
    targets = [
        Path(r"D:\vsc-maven\zzyl-project\zzylvue\src"),
        Path(r"D:\vsc-maven\zzylvue\src"),
    ]
    for root in targets:
        if not root.exists():
            print(f"SKIP missing {root}")
            continue
        changed, invalid_before = process_root(root)
        print(f"=== {root.parent}")
        print(f"invalid utf-8 before: {len(invalid_before)}")
        for p in invalid_before:
            print(f"  - {p}")
        print(f"converted gbk->utf-8: {len(changed)}")
        for p in changed:
            print(f"  + {p}")


if __name__ == "__main__":
    main()
