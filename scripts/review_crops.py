#!/usr/bin/env python3
"""
4:5 크롭 결과를 육안으로 검수하는 HTML 페이지를 만든다. (이슈 #207)

원본과 크롭 결과를 나란히 보여주고, 원본 위에 잘려나갈 영역과 검출된 얼굴 박스를
겹쳐 그린다. 어디가 왜 잘렸는지 한눈에 보이므로 파라미터를 조정할 근거가 된다.

사용법:
    python3 scripts/review_crops.py --src profiles/ --out cropped/ --html review.html

crop_faces_4x5.py를 먼저 실행해 cropped/ 와 crop_report.csv 가 있어야 한다.
"""
import argparse, base64, csv, html, io, json, sys
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    sys.exit("pillow가 필요합니다: pip install pillow")

sys.path.insert(0, str(Path(__file__).parent))
from crop_faces_4x5 import detect_faces, crop_box, load_rgb, ASPECT  # noqa: E402

THUMB_W = 300


def thumb_data_uri(img, width=THUMB_W):
    w, h = img.size
    im = img.resize((width, max(1, round(h * width / w))), Image.LANCZOS)
    buf = io.BytesIO()
    im.save(buf, "JPEG", quality=78)
    return "data:image/jpeg;base64," + base64.b64encode(buf.getvalue()).decode()


def build_rows(src_dir, out_dir, face_ratio, eye_line, names):
    report = {}
    rpt_path = out_dir / "crop_report.csv"
    if rpt_path.exists():
        for r in csv.DictReader(open(rpt_path, encoding="utf-8-sig")):
            report[r["file"]] = r

    rows = []
    for p in sorted(src_dir.iterdir()):
        if not p.is_file() or p.name.startswith("."):
            continue
        stem = p.stem
        try:
            img, fmt = load_rgb(p)
        except Exception as e:
            rows.append(dict(id=stem, name=names.get(stem, stem), status="실패",
                             note=str(e), orig=None, crop=None))
            continue

        w, h = img.size
        faces = detect_faces(img)
        face = faces[0] if faces else None
        box = crop_box(w, h, face, face_ratio, eye_line)

        # 원본 위에 겹쳐 그릴 좌표를 백분율로 넘긴다 (CSS로 렌더)
        overlay = dict(
            left=box[0] / w * 100, top=box[1] / h * 100,
            width=(box[2] - box[0]) / w * 100, height=(box[3] - box[1]) / h * 100,
        )
        facebox = None
        if face is not None:
            fx, fy, fw, fh = face
            facebox = dict(left=fx / w * 100, top=fy / h * 100,
                           width=fw / w * 100, height=fh / h * 100)

        cropped_path = out_dir / f"{stem}.webp"
        crop_uri = None
        if cropped_path.exists():
            with Image.open(cropped_path) as ci:
                crop_uri = thumb_data_uri(ci.convert("RGB"), 220)

        r = report.get(p.name, {})
        rows.append(dict(
            id=stem, name=names.get(stem, stem),
            status=r.get("status", "미실행"), note=r.get("note", ""),
            src=f"{w}×{h} {fmt}", faces=len(faces),
            orig=thumb_data_uri(img), crop=crop_uri,
            overlay=overlay, facebox=facebox,
        ))
    return rows


def render(rows, params, path):
    counts = {}
    for r in rows:
        counts[r["status"]] = counts.get(r["status"], 0) + 1

    cards = []
    for r in rows:
        if not r["orig"]:
            cards.append(f'<article class="card" data-status="실패"><h3>{html.escape(r["name"])}</h3>'
                         f'<p class="note">{html.escape(r["note"])}</p></article>')
            continue
        ov, fb = r["overlay"], r["facebox"]
        face_div = ""
        if fb:
            face_div = (f'<i class="face" style="left:{fb["left"]:.2f}%;top:{fb["top"]:.2f}%;'
                        f'width:{fb["width"]:.2f}%;height:{fb["height"]:.2f}%"></i>')
        crop_img = (f'<img src="{r["crop"]}" alt="크롭 결과">' if r["crop"]
                    else '<div class="missing">결과 없음</div>')
        cards.append(f'''<article class="card" data-status="{html.escape(r["status"])}" data-name="{html.escape(r["name"])}">
  <header><span class="id">{html.escape(r["id"])}</span><h3>{html.escape(r["name"])}</h3>
    <span class="badge s-{html.escape(r["status"])}">{html.escape(r["status"])}</span></header>
  <div class="pair">
    <figure class="orig"><div class="wrap"><img src="{r["orig"]}" alt="원본">
      <i class="keep" style="left:{ov["left"]:.2f}%;top:{ov["top"]:.2f}%;width:{ov["width"]:.2f}%;height:{ov["height"]:.2f}%"></i>
      {face_div}</div><figcaption>원본 {html.escape(r["src"])} · 얼굴 {r["faces"]}</figcaption></figure>
    <figure class="res"><div class="wrap">{crop_img}</div><figcaption>4:5 결과</figcaption></figure>
  </div>
  {f'<p class="note">{html.escape(r["note"])}</p>' if r["note"] else ''}
</article>''')

    filters = "".join(
        f'<button type="button" data-f="{html.escape(s)}">{html.escape(s)}'
        f'<span class="cnt">{n}</span></button>'
        for s, n in sorted(counts.items(), key=lambda kv: -kv[1]))

    doc = f'''<!doctype html><html lang="ko"><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>4:5 크롭 검수</title>
<style>
:root{{--bg:#EDEFEA;--card:#FBFCFA;--line:#D5DACF;--ink:#1B211C;--ink2:#5A645B;
--keep:#B23A28;--face:#2E7D6B;--ok:#3B6E58;--warn:#966A16;--skip:#4A6E86}}
@media(prefers-color-scheme:dark){{:root{{--bg:#141819;--card:#1D2224;--line:#333B3D;
--ink:#E7ECE7;--ink2:#A3ADA5}}}}
*{{box-sizing:border-box}}
body{{margin:0;background:var(--bg);color:var(--ink);
font:14px/1.55 "IBM Plex Sans KR","Apple SD Gothic Neo",system-ui,sans-serif}}
.bar{{position:sticky;top:0;z-index:5;background:var(--bg);border-bottom:1px solid var(--line);
padding:14px 20px;display:flex;gap:12px;align-items:center;flex-wrap:wrap}}
h1{{font-size:16px;margin:0;font-weight:600}}
.params{{font:12px/1.5 ui-monospace,Menlo,monospace;color:var(--ink2)}}
.filters{{display:flex;gap:6px;margin-left:auto;flex-wrap:wrap}}
.filters button{{font:inherit;font-size:12.5px;border:1px solid var(--line);background:var(--card);
color:var(--ink2);border-radius:3px;padding:5px 10px;cursor:pointer;display:flex;gap:6px}}
.filters button[aria-pressed=true]{{background:var(--ink);color:var(--bg);border-color:var(--ink)}}
.cnt{{opacity:.65;font-variant-numeric:tabular-nums}}
main{{padding:18px 20px 60px;display:grid;gap:14px;
grid-template-columns:repeat(auto-fill,minmax(430px,1fr))}}
.card{{background:var(--card);border:1px solid var(--line);border-radius:5px;padding:12px}}
.card header{{display:flex;align-items:center;gap:8px;margin-bottom:9px}}
.card h3{{font-size:14.5px;margin:0;font-weight:600;flex:1}}
.id{{font:11px ui-monospace,monospace;color:var(--ink2)}}
.badge{{font-size:11px;padding:2px 7px;border-radius:3px;color:#fff}}
.s-성공{{background:var(--ok)}}.s-검수필요{{background:var(--warn)}}
.s-건너뜀{{background:var(--skip)}}.s-실패,.s-미실행{{background:var(--keep)}}
.pair{{display:grid;grid-template-columns:1fr 150px;gap:10px;align-items:start}}
.wrap{{position:relative;background:#0002;border-radius:3px;overflow:hidden;line-height:0}}
.wrap img{{width:100%;display:block}}
.keep{{position:absolute;border:2px solid var(--keep);box-shadow:0 0 0 9999px rgba(0,0,0,.42)}}
.face{{position:absolute;border:1.5px dashed var(--face)}}
figcaption{{font:11px ui-monospace,monospace;color:var(--ink2);margin-top:5px;line-height:1.4}}
.missing{{aspect-ratio:4/5;display:grid;place-items:center;font-size:12px;color:var(--ink2)}}
.note{{margin:9px 0 0;font-size:12.5px;color:var(--warn)}}
.legend{{font-size:12px;color:var(--ink2);display:flex;gap:14px;flex-wrap:wrap}}
.legend i{{display:inline-block;width:13px;height:2px;vertical-align:middle;margin-right:4px}}
</style>
<div class="bar">
  <h1>4:5 크롭 검수</h1>
  <span class="params">{html.escape(params)}</span>
  <span class="legend"><span><i style="background:var(--keep)"></i>남는 영역</span>
    <span><i style="background:var(--face)"></i>검출된 얼굴</span></span>
  <span class="filters"><button type="button" data-f="all" aria-pressed="true">전체<span class="cnt">{len(rows)}</span></button>{filters}</span>
</div>
<main id="grid">{''.join(cards)}</main>
<script>
document.querySelectorAll('.filters button').forEach(b=>b.addEventListener('click',()=>{{
  document.querySelectorAll('.filters button').forEach(o=>o.setAttribute('aria-pressed',String(o===b)));
  const f=b.dataset.f;
  document.querySelectorAll('.card').forEach(c=>{{c.hidden=(f!=='all'&&c.dataset.status!==f);}});
}}));
</script></html>'''
    path.write_text(doc, encoding="utf-8")


def main():
    ap = argparse.ArgumentParser(description="4:5 크롭 결과 육안 검수 페이지 생성")
    ap.add_argument("--src", required=True, help="원본 디렉토리 (예: profiles/)")
    ap.add_argument("--out", required=True, help="크롭 결과 디렉토리 (예: cropped/)")
    ap.add_argument("--html", default="review.html", help="생성할 HTML 경로")
    ap.add_argument("--names", help="id→이름 매핑 JSON (선택)")
    ap.add_argument("--face-ratio", type=float, default=0.40)
    ap.add_argument("--eye-line", type=float, default=0.42)
    args = ap.parse_args()

    names = {}
    if args.names and Path(args.names).exists():
        for r in json.load(open(args.names, encoding="utf-8")):
            names[str(r["id"])] = r["name"]

    rows = build_rows(Path(args.src), Path(args.out), args.face_ratio, args.eye_line, names)
    params = f"--face-ratio {args.face_ratio} --eye-line {args.eye_line}"
    out = Path(args.html)
    render(rows, params, out)
    print(f"{len(rows)}건 · {out.resolve()}")


if __name__ == "__main__":
    main()
