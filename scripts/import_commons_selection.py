#!/usr/bin/env python3
"""
commons_final_v2.html에서 내보낸 CSV를 읽어, 선택한 원본을 profiles/에 받고
DB에 넣을 출처 메타데이터(csv)를 만든다. (이슈 #207, #208)

사용법:
    python3 scripts/import_commons_selection.py --csv commons_selection.csv --out profiles/

이후 흐름:
    1) 이 스크립트로 profiles/{id}.{ext} 다운로드 + image_sources.csv 생성
    2) 나머지(유튜브 CC 등)는 profiles/{id}.{ext}로 수동 추가
    3) python3 scripts/crop_faces_4x5.py --in profiles/ --out cropped/
    4) S3 업로드 후, image_sources.csv를 이용해 최종 UPDATE SQL 생성
"""
import argparse, csv, sys, urllib.request
from pathlib import Path

UA = {'User-Agent': 'WhoReads-image-import/1.0'}

# 커먼즈 표기 → ImageLicense enum (Celebrity.java 참고)
LICENSE_MAP = {
    'public domain': 'PUBLIC_DOMAIN',
    'cc0': 'CC0',
}

def map_license(raw):
    """
    'CC BY-SA 3.0', 'CC BY 2.0 kr' 처럼 지역/버전이 붙어도 매칭한다.
    매핑 실패 시 None을 반환해 수동 확인 대상으로 표시한다 (예: GFDL, GODL-India).
    """
    if not raw:
        return None
    key = raw.strip().lower()
    if key in LICENSE_MAP:
        return LICENSE_MAP[key]
    if 'by-sa' in key or ('by' in key and 'sa' in key):
        return 'CC_BY_SA'
    if key.startswith('cc by') or key.startswith('cc-by'):
        return 'CC_BY'
    return None  # GFDL, GODL-India 등 enum에 없는 라이선스 — 수동 확인 필요


def guess_ext(url):
    for ext in ('.jpg', '.jpeg', '.png', '.webp', '.gif', '.tif', '.tiff'):
        if url.lower().split('?')[0].endswith(ext):
            return '.jpg' if ext == '.jpeg' else ext
    return '.jpg'


def main():
    ap = argparse.ArgumentParser(description="커먼즈 선택 CSV를 profiles/로 다운로드하고 출처 메타데이터를 만든다")
    ap.add_argument("--csv", required=True, help="commons_final_v2.html에서 내보낸 CSV")
    ap.add_argument("--out", default="profiles", help="다운로드 대상 디렉토리 (기본: profiles/)")
    ap.add_argument("--force", action="store_true", help="이미 있는 파일도 다시 받는다")
    args = ap.parse_args()

    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)

    rows = list(csv.DictReader(open(args.csv, encoding='utf-8-sig')))
    print(f"선택 {len(rows)}건")

    meta_rows = []
    need_review = []
    ok, skipped, failed = 0, 0, 0

    for r in rows:
        cid = r['id']
        url = r.get('download_url', '').strip()
        if not url:
            print(f"  건너뜀 {cid} {r['name']}: 다운로드 URL 없음")
            skipped += 1
            continue

        ext = guess_ext(url)
        dest = out / f"{cid}{ext}"
        if dest.exists() and not args.force:
            print(f"  이미 있음 {cid} {r['name']} -> {dest.name}")
        else:
            try:
                req = urllib.request.Request(url, headers=UA)
                dest.write_bytes(urllib.request.urlopen(req, timeout=60).read())
                print(f"  받음    {cid} {r['name']} -> {dest.name}")
            except Exception as e:
                print(f"  실패    {cid} {r['name']}: {e}")
                failed += 1
                continue

        lic = map_license(r.get('license', ''))
        if lic is None:
            need_review.append((cid, r['name'], r.get('license', '')))
            lic = 'UNKNOWN'

        meta_rows.append(dict(
            id=cid, name=r['name'],
            image_source_url=r.get('source_page', ''),
            image_author=r.get('artist', ''),
            image_license=lic,
            raw_license=r.get('license', ''),
        ))
        ok += 1

    meta_path = out / "image_sources.csv"
    with open(meta_path, 'w', newline='', encoding='utf-8-sig') as f:
        wtr = csv.DictWriter(f, fieldnames=['id','name','image_source_url','image_author','image_license','raw_license'])
        wtr.writeheader(); wtr.writerows(meta_rows)

    print(f"\n완료 {ok} / 건너뜀 {skipped} / 실패 {failed}")
    print(f"메타데이터: {meta_path}")
    if need_review:
        print(f"\n⚠️  라이선스 매핑 실패 {len(need_review)}건 — image_license가 UNKNOWN으로 채워짐, 수동 확인 필요:")
        for cid, name, raw in need_review:
            print(f"   {cid} {name}: {raw!r}")


if __name__ == "__main__":
    main()
