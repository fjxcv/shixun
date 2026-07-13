# -*- coding: utf-8 -*-
from __future__ import annotations
import re, shutil
from pathlib import Path

ROOT = Path(r"D:\vsc-maven\zzyl-project")
JAVA = ROOT / "zzyl" / "src" / "main" / "java" / "com" / "soft"
VUE = ROOT / "zzylvue" / "src"
MIRROR = Path(r"D:\vsc-maven\zzylvue\src")
ESC = re.compile(r"\\u([0-9a-fA-F]{4})")


def unescape(text: str) -> str:
    return ESC.sub(lambda m: chr(int(m.group(1), 16)), text)


def count_esc(text: str) -> int:
    return len(ESC.findall(text))


def W(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8", newline="\n")


def unescape_tree(root: Path, suffixes: set[str]):
    out = []
    for p in root.rglob("*"):
        if p.suffix not in suffixes:
            continue
        raw = p.read_text(encoding="utf-8")
        n = count_esc(raw)
        if not n:
            continue
        W(p, unescape(raw))
        out.append((str(p), n))
    return out


def ensure_before(text: str, needle: str, comment: str) -> str:
    idx = text.find(needle)
    if idx < 0:
        raise SystemExit("missing needle: " + needle[:120])
    window = text[max(0, idx - 280) : idx]
    marker = comment.strip().splitlines()[0][:20]
    if marker in window:
        return text
    return text[:idx] + comment + "\n" + text[idx:]


def ensure_class(text: str, sig: str, javadoc: str) -> str:
    # Prefer a meaningful line as marker; avoid bare "/**" which matches any javadoc.
    lines = [ln.strip() for ln in javadoc.strip().splitlines() if ln.strip() and ln.strip() != "/**" and ln.strip() != "*/"]
    marker = (lines[0] if lines else javadoc.strip().splitlines()[0])[:24]
    if marker in text:
        return text
    if sig not in text:
        raise SystemExit("missing class sig")
    return text.replace(sig, javadoc + "\n" + sig, 1)


def inject_vue_comment(path: Path, comment: str) -> bool:
    text = path.read_text(encoding="utf-8")
    marker = comment.strip().splitlines()[0][:16]
    if marker in text:
        return False
    m = re.search(r"(<script(?:\s[^>]*)?>\s*\n)", text)
    if not m:
        raise SystemExit("no script in " + str(path))
    W(path, text[: m.end()] + comment + "\n" + text[m.end() :])
    return True


def fix_checkout_pojo() -> None:
    p = JAVA / "pojo" / "Checkout.java"
    t = p.read_text(encoding="utf-8")
    t = ensure_class(
        t,
        '@Data\n@TableName("t_checkout")\npublic class Checkout {',
        "/**\n"
        " * \u9000\u4f4f\u4e1a\u52a1\u5b9e\u4f53\uff0c\u5bf9\u5e94\u8868 t_checkout\u3002\n"
        " * step / stepStatus / flowStatus \u63cf\u8ff0\u591a\u6b65\u9aa4\u5ba1\u6279\u8fdb\u5ea6\u3002\n"
        " */",
    )
    pairs = [
        ("    private String docNo;", "    /** \u5355\u636e\u53f7\uff0c\u5982 TZ+\u65f6\u95f4\u6233 */\n    private String docNo;"),
        ("    private String elderName;", "    /** \u8001\u4eba\u59d3\u540d */\n    private String elderName;"),
        ("    private LocalDate checkoutDate;", "    /** \u8ba1\u5212\u9000\u4f4f\u65e5\u671f */\n    private LocalDate checkoutDate;"),
        ("    private String reason;", "    /** \u9000\u4f4f\u539f\u56e0 */\n    private String reason;"),
        ("    private String applicant;", "    /** \u7533\u8bf7\u4eba\uff08\u4e0e\u767b\u5f55 realname \u5bf9\u9f50\uff09 */\n    private String applicant;"),
        ("    private Integer step;", "    /** \u5f53\u524d\u6d41\u7a0b\u6b65\u9aa4\u53f7 */\n    private Integer step;"),
        ("    private String stepStatus;", "    /** \u6b65\u9aa4\u72b6\u6001\uff1a\u8fdb\u884c\u4e2d/\u5df2\u5b8c\u6210/\u5df2\u5173\u95ed */\n    private String stepStatus;"),
        ("    private String flowStatus;", "    /** \u6d41\u7a0b\u72b6\u6001\uff1a\u7533\u8bf7\u4e2d/\u5df2\u5b8c\u6210/\u5df2\u5173\u95ed */\n    private String flowStatus;"),
        ("    private String approvalResult;", "    /** \u6700\u8fd1\u4e00\u6b21\u5ba1\u6279\u7ed3\u679c */\n    private String approvalResult;"),
        ("    private BigDecimal refundAmount;", "    /** \u5e94\u9000/\u5b9e\u9000\u91d1\u989d */\n    private BigDecimal refundAmount;"),
        ("    private String creator;", "    /** \u521b\u5efa\u4eba */\n    private String creator;"),
    ]
    for old, new in pairs:
        if new.split("\n")[0] in t:
            continue
        if old in t:
            t = t.replace(old, new, 1)
    W(p, t)
    print("Checkout.java annotated")


def fix_data_init() -> None:
    p = JAVA / "config" / "DataInitRunner.java"
    t = unescape(p.read_text(encoding="utf-8"))
    t = ensure_class(
        t,
        "@Component\npublic class DataInitRunner implements CommandLineRunner {",
        "/**\n"
        " * \u542f\u52a8\u65f6\u6570\u636e\u521d\u59cb\u5316\uff1a\u8865\u9f50\u8868\u7ed3\u6784\u3001\u6f14\u793a\u8d26\u53f7/\u697c\u5c42\u5e8a\u4f4d\u7b49\u57fa\u7840\u6570\u636e\u3002\n"
        " * clearWorkflowDemoData \u4f1a\u6e05\u7406\u534f\u540c\u76f8\u5173\u6f14\u793a\u79cd\u5b50\uff0c\u907f\u514d\u6c61\u67d3\u300c\u6211\u7684\u5f85\u529e/\u7533\u8bf7\u300d\u3002\n"
        " */",
    )
    new = (
        "    /**\n"
        "     * \u6e05\u7406\u534f\u540c\u76f8\u5173\u6f14\u793a\u79cd\u5b50\uff08\u5165\u9000/\u8bf7\u5047\uff09\uff0c\u907f\u514d\u6c61\u67d3\u5f85\u529e\u4e0e\u7533\u8bf7\u5217\u8868\u3002\n"
        "     * \u5220\u9664\u5386\u53f2\u811a\u672c\u5199\u5165\u7684 QJ2048/TZ2048 \u524d\u7f00\u6f14\u793a\u5355\uff0c\u4ee5\u53ca\u56fa\u5b9a\u8eab\u4efd\u8bc1\u524d\u7f00\u7684\u5165\u4f4f\u6f14\u793a\u5355\u3002\n"
        "     */"
    )
    short = "    /** \u6e05\u7406\u534f\u540c\u76f8\u5173\u6f14\u793a\u79cd\u5b50\uff08\u5165\u9000/\u8bf7\u5047\uff09\uff0c\u907f\u514d\u6c61\u67d3\u5f85\u529e\u4e0e\u7533\u8bf7\u5217\u8868 */"
    if short in t:
        t = t.replace(short, new)
    elif "\u5220\u9664\u5386\u53f2\u811a\u672c" not in t:
        t = ensure_before(t, "    private void clearWorkflowDemoData() {", new)
    marker = "// \u4e0d\u518d\u79cd\u5b50\u8bf7\u5047/\u9000\u4f4f/\u5165\u4f4f\u6f14\u793a\u6570\u636e\uff0c\u534f\u540c\u5f85\u529e\u4e0e\u7533\u8bf7\u4ee5\u771f\u5b9e\u4e1a\u52a1\u5355\u636e\u4e3a\u51c6"
    better = "// \u4e0d\u518d\u79cd\u5b50\u8bf7\u5047/\u9000\u4f4f/\u5165\u4f4f\u6f14\u793a\u6570\u636e\uff1b\u534f\u540c\u5f85\u529e\u4e0e\u7533\u8bf7\u4ee5\u771f\u5b9e\u4e1a\u52a1\u5355\u636e\u4e3a\u51c6\uff0c\u907f\u514d\u4e0e clearWorkflowDemoData \u6e05\u7406\u7b56\u7565\u51b2\u7a81"
    if marker in t and "\u6e05\u7406\u7b56\u7565\u51b2\u7a81" not in t:
        t = t.replace(marker, better)
    W(p, t)
    print("DataInitRunner ok esc=", count_esc(t))


def annotate_missing_vue() -> None:
    comments = {
        "views/CheckoutApply.vue": (
            "/**\n"
            " * \u9000\u4f4f\u7533\u8bf7\u9875\uff1a\u586b\u5199\u5fc5\u586b\u4fe1\u606f\u540e POST /checkout/save\uff0c\u540e\u7aef\u8865\u5168\u7533\u8bf7\u4eba\u5e76\u8fdb\u5165 step=2\u3002\n"
            " */"
        ),
        "views/CheckoutDetail.vue": (
            "/**\n"
            " * \u9000\u4f4f\u8be6\u60c5\u4e0e\u6b65\u9aa4\u63a8\u8fdb\uff1a\u6309 step \u5c55\u793a\u4fe1\u606f\u533a/\u5ba1\u6279\u533a\uff0c\u8c03\u7528 /checkout/updateStep\u3002\n"
            " * \u5ba1\u6279\u8282\u70b9\u4e3a 2/5/6\uff1b\u7ec8\u6001\u4e0d\u53ef\u518d\u64cd\u4f5c\u3002\n"
            " */"
        ),
        "views/HomeView.vue": (
            "/**\n"
            " * \u767b\u5f55\u9875\uff1a\u63d0\u4ea4\u8d26\u53f7\u5bc6\u7801\u5230 /login\uff1b\u9875\u811a\u63d0\u4f9b\u5fd8\u8bb0\u5bc6\u7801\u4e0e\u6ce8\u518c\u5165\u53e3\u3002\n"
            " */"
        ),
        "components/checkout/OperationTimeline.vue": (
            "/**\n"
            " * \u9000\u4f4f\u64cd\u4f5c\u65f6\u95f4\u7ebf\uff1a\u6839\u636e step / flowStatus \u6e32\u67d3\u5404\u8282\u70b9\u5b8c\u6210\u6001\u4e0e\u5f53\u524d\u8282\u70b9\u3002\n"
            " * \u300c\u5f85\u5ba1\u6279\u300d\u7b49\u6587\u6848\u4e0e\u540e\u7aef\u6b65\u9aa4\u8bed\u4e49\u5bf9\u5e94\u3002\n"
            " */"
        ),
        "components/checkout/InfoSections.vue": (
            "/**\n"
            " * \u9000\u4f4f\u8be6\u60c5\u4fe1\u606f\u5206\u533a\u5c55\u793a\uff08\u8001\u4eba\u3001\u5408\u540c\u3001\u8d26\u5355\u7b49\u53ea\u8bfb\u533a\u5757\uff09\u3002\n"
            " */"
        ),
    }
    for rel, comment in comments.items():
        if inject_vue_comment(VUE / Path(rel), comment):
            print("vue commented", rel)
        else:
            print("vue already", rel)


def sync_mirror() -> None:
    if not MIRROR.exists():
        print("no mirror")
        return
    rels = [
        "views/CheckoutApply.vue",
        "views/CheckoutDetail.vue",
        "views/HomeView.vue",
        "views/MyApply.vue",
        "views/MyTodo.vue",
        "views/LeaveManage.vue",
        "views/LeaveDetail.vue",
        "views/Register.vue",
        "views/ForgotPassword.vue",
        "components/checkout/OperationTimeline.vue",
        "components/checkout/InfoSections.vue",
        "views/RoomTypeSetting.vue",
    ]
    for rel in rels:
        src, dst = VUE / Path(rel), MIRROR / Path(rel)
        if src.exists():
            dst.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, dst)
            print("synced", rel)


def verify() -> None:
    print("=== VERIFY remaining escapes ===")
    left = 0
    for root, suf in ((JAVA, {".java"}), (VUE, {".vue", ".js", ".ts"})):
        for p in sorted(root.rglob("*")):
            if p.suffix not in suf:
                continue
            n = count_esc(p.read_text(encoding="utf-8"))
            if n:
                left += 1
                print("  LEFT", n, p)
    leave = (JAVA / "controller" / "LeaveController.java").read_text(encoding="utf-8")
    assert "\u5f85\u5ba1\u6279" in leave and "\\u5f85" not in leave
    home = (VUE / "views" / "HomeView.vue").read_text(encoding="utf-8")
    assert "\u767b\u5f55\u9875" in home
    init = (JAVA / "config" / "DataInitRunner.java").read_text(encoding="utf-8")
    assert "\u987e\u5ef7\u70ec" in init and count_esc(init) == 0
    co = (JAVA / "pojo" / "Checkout.java").read_text(encoding="utf-8")
    assert "\u9000\u4f4f\u4e1a\u52a1\u5b9e\u4f53" in co
    print("SPOT OK, left_files=", left)


def main() -> None:
    print("--- unescape java ---")
    for path, n in unescape_tree(JAVA, {".java"}):
        print("  unescaped", n, path)
    print("--- unescape vue ---")
    for path, n in unescape_tree(VUE, {".vue", ".js", ".ts"}):
        print("  unescaped", n, path)
    fix_data_init()
    fix_checkout_pojo()
    annotate_missing_vue()
    sync_mirror()
    verify()


if __name__ == "__main__":
    main()
