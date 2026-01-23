import re
import sys
import os

def parse_sql_to_mermaid(sql_content):
    mermaid_lines = ["erDiagram"]

    # 1. 테이블 추출 (CREATE TABLE `tableName` ...)
    # 대소문자 무시, 백틱 처리
    table_pattern = re.compile(r"CREATE\s+TABLE\s+`?(\w+)`?", re.IGNORECASE)

    # 테이블 안의 컬럼 추출 로직을 위한 분할
    # SQL 문을 ';' 기준으로 나누어 처리
    statements = sql_content.split(';')

    tables = {} # 테이블 정보 저장 {table_name: [columns...]}
    relationships = [] # 관계 정보 저장

    for statement in statements:
        statement = statement.strip()
        if not statement:
            continue

        # 테이블 찾기
        table_match = table_pattern.search(statement)
        if table_match:
            table_name = table_match.group(1)
            tables[table_name] = []

            # 괄호 안의 내용(컬럼 정의) 추출
            content_match = re.search(r"\((.*)\)", statement, re.DOTALL)
            if content_match:
                columns_block = content_match.group(1)
                # 줄바꿈으로 나누기
                lines = columns_block.split('\n')

                for line in lines:
                    line = line.strip()
                    if not line or line.startswith(('PRIMARY', 'KEY', 'CONSTRAINT', ')')):
                        continue

                    # 컬럼명과 타입 추출 (간단한 파싱)
                    # 예: `id` bigint NOT NULL...
                    parts = line.replace('`', '').split()
                    if len(parts) >= 2:
                        col_name = parts[0]
                        col_type = parts[1]

                        # PK 확인 (단순 문자열 체크)
                        is_pk = "PK" if "PRIMARY KEY" in statement and col_name in statement else ""
                        if "id" == col_name and "PRIMARY KEY" not in statement:
                            # 보통 id는 PK이므로 명시가 없어도 처리 (약식)
                            is_pk = "PK"

                        # FK 추론 로직 (사용자님의 스키마 스타일에 맞춤)
                        # 예: member_id -> member 테이블 참조
                        if col_name.endswith("_id") and col_name != "id":
                            target_table = col_name.replace("_id", "")
                            # 관계 추가 (나중에 테이블 존재 여부 확인 후 확정)
                            relationships.append((target_table, table_name))
                            is_fk = "FK"
                        else:
                            is_fk = ""

                        tables[table_name].append(f"{col_type} {col_name} {is_pk} {is_fk}")

    # 2. 관계(Relationship) 생성
    # Mermaid 문법: table1 ||--o{ table2 : "label"
    # 여기서는 단순하게 ||--o{ 로 통일 (1:N 가정)

    # 존재하는 테이블끼리만 관계 연결
    table_names = set(tables.keys())

    for target, source in relationships:
        # 대소문자 문제 해결을 위해 검색 (User -> member 매칭 등은 추가 로직 필요하나 여기서는 정확한 이름 매칭)
        # 업로드한 SQL에서는 member_id -> member 로 정확히 매칭됨
        if target in table_names and source in table_names:
            mermaid_lines.append(f"    {target} ||--o{{ {source} : \"has\"")

    mermaid_lines.append("")

    # 3. 테이블 정의 생성
    for table_name, columns in tables.items():
        mermaid_lines.append(f"    {table_name} {{")
        for col in columns:
            mermaid_lines.append(f"        {col}")
        mermaid_lines.append("    }")

    return "\n".join(mermaid_lines)

if __name__ == "__main__":
    # 1. 현재 실행 중인 스크립트(sql_to_mermaid.py)의 절대 경로를 구합니다.
    current_script_path = os.path.abspath(__file__)

    # 2. 스크립트가 있는 폴더(scripts) 경로를 구합니다.
    scripts_dir = os.path.dirname(current_script_path)

    # 3. 프로젝트 루트 폴더(WhoReads)는 scripts 폴더의 상위 폴더입니다.
    project_root = os.path.dirname(scripts_dir)

    # 4. 프로젝트 루트를 기준으로 SQL 파일 경로를 합칩니다.
    file_path = os.path.join(project_root, 'docs', 'ERD', 'WhoReads.sql')

    # 저장할 MD 파일 경로도 똑같이 프로젝트 루트 기준으로 잡습니다.
    output_path = os.path.join(project_root, 'docs', 'ERD', 'ERD.md')

    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            sql = f.read()

        mermaid_output = parse_sql_to_mermaid(sql)

        # 결과 출력 (GitHub Actions에서는 파일로 저장)
        print(mermaid_output)

        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(f"```mermaid\n{mermaid_output}\n```")

    except FileNotFoundError:
        print(f"Error: File {file_path} not found.")