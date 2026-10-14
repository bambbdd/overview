"""ECOS에서 최신 한국은행 기준금리를 조회하고 JSON으로 출력한다."""

import json
import math
import sys
from datetime import datetime
from urllib.error import URLError
from urllib.parse import quote
from urllib.request import urlopen


YOUR_API_KEY = "MNF9CAFWHB91I73HD01H"  # 발급받은 ECOS API 키로 변경
BASE_URL = "https://ecos.bok.or.kr/api"
STAT_CODE = "722Y001"
ITEM_CODE = "0101000"


def get_json(url):
    with urlopen(url, timeout=15) as response:
        payload = json.loads(response.read().decode("utf-8"))

    if not isinstance(payload, dict):
        raise ValueError("ECOS 응답이 JSON 객체가 아닙니다.")
    if "RESULT" in payload:
        result = payload["RESULT"]
        raise ValueError(
            f"ECOS API 오류: {result.get('CODE')} - {result.get('MESSAGE')}"
        )
    return payload


def fetch_latest_base_rate(api_key):
    if not api_key.strip() or api_key == "YOUR_API_KEY":
        raise ValueError("YOUR_API_KEY 변수에 발급받은 API 키를 입력하세요.")

    encoded_key = quote(api_key, safe="")
    # sample 키는 최대 10행만 조회할 수 있다.
    limit = 10 if api_key == "sample" else 1000
    metadata = get_json(
        f"{BASE_URL}/StatisticItemList/{encoded_key}/json/kr/1/{limit}/{STAT_CODE}"
    )
    items = metadata.get("StatisticItemList", {}).get("row", [])
    dates = [
        item["END_TIME"]
        for item in items
        if item.get("ITEM_CODE") == ITEM_CODE and item.get("CYCLE") == "D"
    ]
    if not dates:
        raise ValueError("기준금리의 최신 일자를 찾을 수 없습니다.")

    latest_date = max(dates)
    datetime.strptime(latest_date, "%Y%m%d")
    payload = get_json(
        f"{BASE_URL}/StatisticSearch/{encoded_key}/json/kr/1/1/"
        f"{STAT_CODE}/D/{latest_date}/{latest_date}/{ITEM_CODE}"
    )
    rows = payload.get("StatisticSearch", {}).get("row", [])
    if not rows:
        raise ValueError("최신 기준금리 데이터가 없습니다.")

    latest = rows[0]
    if latest.get("TIME") != latest_date or latest.get("ITEM_CODE1") != ITEM_CODE:
        raise ValueError("응답의 기준 일자 또는 항목 코드가 조회 조건과 다릅니다.")
    if not math.isfinite(float(latest["DATA_VALUE"])):
        raise ValueError("응답의 기준금리 수치가 유효하지 않습니다.")
    return latest


def main():
    try:
        latest = fetch_latest_base_rate(YOUR_API_KEY)
        print(json.dumps(latest, ensure_ascii=False, indent=2))
        return 0
    except URLError:
        print("ECOS API HTTP 요청에 실패했습니다.", file=sys.stderr)
    except (ValueError, KeyError, TypeError, TimeoutError) as error:
        print(f"테스트 실패: {error}", file=sys.stderr)
    return 1


if __name__ == "__main__":
    sys.exit(main())
