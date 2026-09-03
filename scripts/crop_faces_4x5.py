#!/usr/bin/env python3
"""
인물 이미지를 얼굴 기준 4:5 비율로 일괄 크롭한다. (이슈 #205)

  - 얼굴을 검출해 프레임 안에서 관례적인 위치(얼굴 중심이 위에서 42%)에 오도록 배치
  - 얼굴 높이가 프레임 높이의 약 40%가 되도록 확대/축소 배율을 잡는다
  - 원본이 부족해 4:5를 채우지 못하면 가능한 범위로 물러나고, 그래도 안 되면 보고만 하고 건너뛴다
  - 결과와 함께 검수용 컨택시트를 만든다. 자동 크롭은 반드시 눈으로 확인해야 한다

사용법:
    python3 scripts/crop_faces_4x5.py --in raw/ --out cropped/
    python3 scripts/crop_faces_4x5.py --in raw/ --out cropped/ --face-ratio 0.36 --eye-line 0.40

의존성: pillow, opencv-python-headless (4.x/5.x 모두 지원)
        OpenCV 5는 YuNet 모델을 최초 1회 자동으로 내려받는다
"""
import argparse, csv, json, sys
from pathlib import Path

try:
    import cv2
    import numpy as np
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    sys.exit("pillow와 opencv-python-headless가 필요합니다: pip install pillow opencv-python-headless")

TARGET_W, TARGET_H = 1080, 1350          # 4:5
ASPECT = 4 / 5
MIN_SHORT_SIDE = 400                      # 이보다 작으면 확대해도 못 쓴다


def load_rgb(path):
    """WebP/AVIF/PNG가 .jpg 확장자로 섞여 있어도 Pillow가 실제 포맷으로 연다."""
    with Image.open(path) as im:
        return im.convert("RGB"), im.format


YUNET_URL = ("https://github.com/opencv/opencv_zoo/raw/main/models/"
             "face_detection_yunet/face_detection_yunet_2023mar.onnx")
_detector_cache = {}


def _yunet_model_path():
    """YuNet 모델을 사용자 캐시에 받아둔다. OpenCV 5에는 Haar cascade가 없다."""
    cache = Path.home() / ".cache" / "whoreads"
    cache.mkdir(parents=True, exist_ok=True)
    path = cache / "face_detection_yunet_2023mar.onnx"
    if not path.exists():
        import urllib.request
        print(f"얼굴 검출 모델을 내려받는 중... ({path})")
        urllib.request.urlretrieve(YUNET_URL, path)
    return str(path)


def _detect_yunet(arr):
    h, w = arr.shape[:2]
    det = _detector_cache.get("yunet")
    if det is None:
        det = cv2.FaceDetectorYN.create(_yunet_model_path(), "", (w, h),
                                        score_threshold=0.6)
        _detector_cache["yunet"] = det
    det.setInputSize((w, h))
    bgr = cv2.cvtColor(arr, cv2.COLOR_RGB2BGR)
    _, faces = det.detect(bgr)
    if faces is None:
        return []
    # YuNet은 [x, y, w, h, 랜드마크..., score] 형태로 반환한다
    return [tuple(int(v) for v in f[:4]) for f in faces]


def _detect_haar(arr):
    gray = cv2.equalizeHist(cv2.cvtColor(arr, cv2.COLOR_RGB2GRAY))
    base = Path(cv2.data.haarcascades)
    for name in ("haarcascade_frontalface_default.xml",
                 "haarcascade_frontalface_alt2.xml",
                 "haarcascade_profileface.xml"):
        cascade = cv2.CascadeClassifier(str(base / name))
        if cascade.empty():
            continue
        found = cascade.detectMultiScale(gray, scaleFactor=1.08, minNeighbors=5,
                                         minSize=(max(24, gray.shape[1] // 20),) * 2)
        if len(found):
            return [tuple(int(v) for v in f) for f in found]
    return []


def detect_faces(pil_img):
    """
    얼굴을 (x, y, w, h) 리스트로 반환하며 큰 순으로 정렬한다.
    OpenCV 5는 YuNet(DNN), 4는 Haar cascade를 쓴다.
    """
    arr = np.array(pil_img)
    if hasattr(cv2, "FaceDetectorYN"):
        faces = _detect_yunet(arr)
    elif hasattr(cv2, "CascadeClassifier"):
        faces = _detect_haar(arr)
    else:
        raise RuntimeError("사용 가능한 얼굴 검출기가 없습니다.")
    # 가장 큰 얼굴을 인물로 본다 (뒤쪽 인물·군중 배제)
    return sorted(faces, key=lambda f: f[2] * f[3], reverse=True)


def crop_box(img_w, img_h, face, face_ratio, eye_line):
    """
    얼굴이 프레임에서 원하는 크기·위치에 오도록 4:5 크롭 영역을 구한다.
    영상이 모자라면 비율을 유지한 채 최대 크기로 물러난 뒤 경계 안으로 민다.
    """
    if face is not None:
        fx, fy, fw, fh = face
        want_h = fh / face_ratio
        want_w = want_h * ASPECT
        cx = fx + fw / 2
        cy = fy + fh / 2 - want_h * (eye_line - 0.5)   # 얼굴 중심을 eye_line 위치로
    else:
        # 얼굴을 못 찾으면 중앙에서, 인물 사진 관례상 살짝 위쪽을 잡는다
        want_h = min(img_h, img_w / ASPECT)
        want_w = want_h * ASPECT
        cx, cy = img_w / 2, img_h * 0.45

    # 원본 밖으로 나가지 않도록 축소
    scale = min(1.0, img_w / want_w, img_h / want_h)
    want_w *= scale
    want_h *= scale

    left = cx - want_w / 2
    top = cy - want_h / 2
    left = max(0, min(left, img_w - want_w))
    top = max(0, min(top, img_h - want_h))
    return (int(round(left)), int(round(top)),
            int(round(left + want_w)), int(round(top + want_h)))


def process(path, out_dir, face_ratio, eye_line, min_short):
    row = {"file": path.name, "status": "", "note": "",
           "src_w": 0, "src_h": 0, "src_fmt": "", "faces": 0, "upscaled": ""}
    try:
        img, fmt = load_rgb(path)
    except Exception as e:
        row["status"], row["note"] = "실패", f"열기 오류: {e}"
        return row

    w, h = img.size
    row.update(src_w=w, src_h=h, src_fmt=fmt or "?")

    if min(w, h) < min_short:
        row["status"] = "건너뜀"
        row["note"] = f"해상도 부족 ({w}x{h}) — 재수집 대상"
        return row

    faces = detect_faces(img)
    row["faces"] = len(faces)
    face = faces[0] if len(faces) else None

    box = crop_box(w, h, face, face_ratio, eye_line)
    cropped = img.crop(box)
    cw, ch = cropped.size

    if cw < TARGET_W:
        row["upscaled"] = f"{cw}px→{TARGET_W}px"
    cropped = cropped.resize((TARGET_W, TARGET_H), Image.LANCZOS)

    out_dir.mkdir(parents=True, exist_ok=True)
    out_path = out_dir / f"{path.stem}.webp"
    cropped.save(out_path, "WEBP", quality=88, method=6)

    if face is None:
        row["status"] = "검수필요"
        row["note"] = "얼굴 미검출 — 중앙 기준으로 잘랐음"
    elif len(faces) > 1:
        row["status"] = "검수필요"
        row["note"] = f"얼굴 {len(faces)}개 검출 — 가장 큰 얼굴 사용"
    else:
        row["status"] = "성공"
    return row


def contact_sheet(out_dir, rows, path, cols=10, thumb_w=150):
    """검수용 컨택시트. 상태별로 테두리 색을 달리해 문제 건이 눈에 띄게 한다."""
    items = [r for r in rows if r["status"] in ("성공", "검수필요")]
    if not items:
        return None
    tw, th = thumb_w, int(thumb_w / ASPECT)
    pad, label_h = 8, 16
    rows_n = (len(items) + cols - 1) // cols
    sheet = Image.new("RGB",
                      (cols * (tw + pad) + pad, rows_n * (th + pad + label_h) + pad),
                      (24, 26, 24))
    draw = ImageDraw.Draw(sheet)
    try:
        font = ImageFont.truetype("/System/Library/Fonts/Supplemental/Arial.ttf", 11)
    except Exception:
        font = ImageFont.load_default()

    for i, r in enumerate(items):
        c, rr = i % cols, i // cols
        x = pad + c * (tw + pad)
        y = pad + rr * (th + pad + label_h)
        try:
            with Image.open(out_dir / f"{Path(r['file']).stem}.webp") as im:
                sheet.paste(im.resize((tw, th), Image.LANCZOS), (x, y))
        except Exception:
            continue
        color = (200, 70, 50) if r["status"] == "검수필요" else (70, 130, 95)
        draw.rectangle([x, y, x + tw - 1, y + th - 1], outline=color, width=2)
        draw.text((x, y + th + 2), Path(r["file"]).stem[:20], fill=(190, 195, 190), font=font)

    sheet.save(path, quality=90)
    return path


def main():
    ap = argparse.ArgumentParser(description="인물 이미지를 얼굴 기준 4:5로 일괄 크롭")
    ap.add_argument("--in", dest="src", required=True, help="원본 디렉토리")
    ap.add_argument("--out", dest="dst", required=True, help="출력 디렉토리")
    ap.add_argument("--face-ratio", type=float, default=0.40,
                    help="얼굴 높이가 프레임 높이에서 차지할 비율 (기본 0.40, 작을수록 인물이 작게)")
    ap.add_argument("--eye-line", type=float, default=0.42,
                    help="얼굴 중심을 놓을 세로 위치 (기본 0.42, 위에서부터의 비율)")
    ap.add_argument("--min-short-side", type=int, default=MIN_SHORT_SIDE,
                    help=f"이보다 짧은 변을 가진 이미지는 건너뜀 (기본 {MIN_SHORT_SIDE})")
    args = ap.parse_args()

    src, dst = Path(args.src), Path(args.dst)
    files = sorted(p for p in src.iterdir()
                   if p.is_file() and not p.name.startswith("."))
    if not files:
        sys.exit(f"{src}에 파일이 없습니다.")

    rows = []
    for p in files:
        r = process(p, dst, args.face_ratio, args.eye_line, args.min_short_side)
        rows.append(r)
        mark = {"성공": "OK", "검수필요": "  검수", "건너뜀": "  건너뜀", "실패": "  실패"}[r["status"]]
        print(f"{mark:6s} {p.name}  {r['note']}")

    report = dst / "crop_report.csv"
    with open(report, "w", newline="", encoding="utf-8-sig") as f:
        wtr = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        wtr.writeheader()
        wtr.writerows(rows)

    sheet = contact_sheet(dst, rows, dst / "contact_sheet.jpg")

    from collections import Counter
    c = Counter(r["status"] for r in rows)
    print("\n" + "=" * 52)
    for k in ("성공", "검수필요", "건너뜀", "실패"):
        if c[k]:
            print(f"  {k:6s} {c[k]:4d}건")
    print(f"\n  출력    {dst}/ ({TARGET_W}x{TARGET_H} WebP)")
    print(f"  리포트  {report}")
    if sheet:
        print(f"  컨택시트 {sheet}  ← 반드시 눈으로 확인할 것")


if __name__ == "__main__":
    main()
