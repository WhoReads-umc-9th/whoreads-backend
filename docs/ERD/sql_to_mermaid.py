import re
from pathlib import Path

SQL_PATH = "docs/ERD/WhoReads.sql"
ERD_PATH = "docs/ERD/ERD.md"
ERD_CLOUD_URL = "https://www.erdcloud.com/d/vymgaEnwdvs8Pf2HL"


def parse_existing_labels(erd_path):
    """기존 ERD.md에서 관계 라벨을 추출하여 보존"""
    labels = {}
    path = Path(erd_path)
    if not path.exists():
        return labels
    text = path.read_text(encoding="utf-8")
    for m in re.finditer(r'(\w+)\s+\|\|--o\{\s+(\w+)\s*:\s*"([^"]+)"', text):
        labels[(m.group(1), m.group(2))] = m.group(3)
    return labels


def normalize_type(raw_type):
    """SQL 타입을 Mermaid용으로 변환"""
    raw = raw_type.strip()
    upper = raw.upper()

    # VARCHAR(N) → varchar / varchar_N
    vm = re.match(r'VARCHAR\((\d+)\)', upper)
    if vm:
        length = vm.group(1)
        return "varchar" if length == "255" else f"varchar_{length}"
    if upper == "VARCHAR":
        return "varchar"

    # DATETIME(6) → datetime
    if upper.startswith("DATETIME"):
        return "datetime"

    # TINYINT(1) → tinyint
    if upper.startswith("TINYINT"):
        return "tinyint"

    # ENUM(...) → enum
    if upper.startswith("ENUM"):
        return "enum"

    # BIGINT, INT, TEXT, JSON, TIME, etc.
    base = re.match(r'(\w+)', upper)
    return base.group(1).lower() if base else raw.lower()


def parse_sql(sql_path):
    """SQL 파싱 → 테이블 목록 + FK 관계"""
    raw = Path(sql_path).read_text(encoding="utf-8")

    # DELIMITER 이후 제거 (트리거 등)
    raw = re.split(r'DELIMITER\s', raw, flags=re.IGNORECASE)[0]

    # 주석 제거
    raw = re.sub(r'--[^\n]*', '', raw)
    raw = re.sub(r'/\*.*?\*/', '', raw, flags=re.DOTALL)

    # CREATE INDEX 제거
    raw = re.sub(r'CREATE\s+INDEX\s+[^;]*;', '', raw, flags=re.IGNORECASE)

    tables = {}
    relationships = []

    for m in re.finditer(
        r'CREATE\s+TABLE\s+[`"]?(\w+)[`"]?\s*\((.*?)\)\s*(?:ENGINE|;)',
        raw, re.IGNORECASE | re.DOTALL,
    ):
        tname = m.group(1)
        body = m.group(2)

        # PK 추출
        pk_m = re.search(r'PRIMARY\s+KEY\s*\(([^)]+)\)', body, re.IGNORECASE)
        pk_cols = set()
        if pk_m:
            pk_cols = {c.strip().strip('`"') for c in pk_m.group(1).split(',')}

        # FK 추출: {fk_col: parent_table}
        fk_map = {}
        for fk_m in re.finditer(
            r'FOREIGN\s+KEY\s*\(\s*[`"]?(\w+)[`"]?\s*\)\s*REFERENCES\s+[`"]?(\w+)[`"]?',
            body, re.IGNORECASE,
        ):
            fk_col, parent = fk_m.group(1), fk_m.group(2)
            fk_map[fk_col] = parent
            relationships.append((parent, tname))

        # 여러 줄에 걸친 ENUM 정의를 한 줄로 합치기
        collapsed = body
        while True:
            merged = re.sub(r"(ENUM\s*\([^)]*?)\n\s*", r"\1 ", collapsed)
            if merged == collapsed:
                break
            collapsed = merged

        # 컬럼 파싱
        columns = []
        for line in collapsed.split('\n'):
            line = line.strip().rstrip(',')
            if not line:
                continue
            if re.match(
                r'(?:PRIMARY|CONSTRAINT|UNIQUE\s+KEY|KEY\s|INDEX|FOREIGN)',
                line, re.IGNORECASE,
            ):
                continue

            # 컬럼 이름 추출
            col_m = re.match(r'[`"]?(\w+)[`"]?\s+(.*)', line)
            if not col_m:
                continue

            col_name = col_m.group(1)
            rest = col_m.group(2)

            # 타입 추출 — ENUM(...) 은 괄호 깊이로 처리
            if rest.upper().startswith('ENUM'):
                depth, end = 0, 0
                for i, c in enumerate(rest):
                    if c == '(':
                        depth += 1
                    elif c == ')':
                        depth -= 1
                        if depth == 0:
                            end = i + 1
                            break
                type_str = rest[:end]
                after_type = rest[end:]
            else:
                type_m = re.match(r'(\w+(?:\([^)]*\))?)', rest)
                if not type_m:
                    continue
                type_str = type_m.group(1)
                after_type = rest[len(type_str):]

            col_type = normalize_type(type_str)
            is_not_null = bool(re.search(r'NOT\s+NULL', after_type, re.IGNORECASE))
            is_unique = bool(re.search(r'\bUNIQUE\b', after_type, re.IGNORECASE))

            # 플래그 및 어노테이션
            flag = ""
            annotation = ""
            if col_name in pk_cols:
                flag = "PK"
            elif col_name in fk_map:
                flag = "FK"
                if is_unique:
                    annotation = '"Unique"'
            else:
                parts = []
                if is_not_null:
                    parts.append("NOT_NULL")
                if is_unique:
                    parts.append("Unique")
                if parts:
                    annotation = f'"{", ".join(parts)}"'

            entry = f"{col_type} {col_name}"
            if flag:
                entry += f" {flag}"
            if annotation:
                entry += f" {annotation}"
            columns.append(entry)

        tables[tname] = columns

    # 중복 제거 (순서 유지)
    relationships = list(dict.fromkeys(relationships))
    return tables, relationships


def generate_erd(tables, relationships, labels):
    """ERD.md 생성"""
    lines = [
        "# WhoReads ERD",
        "",
        f"> ERD Cloud: {ERD_CLOUD_URL}",
        "",
        "```mermaid",
        "erDiagram",
    ]

    for parent, child in relationships:
        label = labels.get((parent, child), "references")
        lines.append(f'    {parent} ||--o{{ {child} : "{label}"')

    lines.append("")

    for tname, cols in tables.items():
        lines.append(f"    {tname} {{")
        for col in cols:
            lines.append(f"        {col}")
        lines.append("    }")
        lines.append("")

    lines.append("```")
    return "\n".join(lines) + "\n"


if __name__ == "__main__":
    print(f"[*] {SQL_PATH} 파싱 중...")

    labels = parse_existing_labels(ERD_PATH)
    tables, rels = parse_sql(SQL_PATH)
    result = generate_erd(tables, rels, labels)

    Path(ERD_PATH).write_text(result, encoding="utf-8")
    print(f"[*] {ERD_PATH} 업데이트 완료 ({len(tables)}개 테이블, {len(rels)}개 관계)")
