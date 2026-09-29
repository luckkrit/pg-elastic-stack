#!/usr/bin/env bash
set -uo pipefail
cd "$(dirname "$0")/.."

STAMP=/tmp/redeploy-cargo.stamp
LOCK=/tmp/redeploy-cargo.lock

# --- debounce: ประทับเวลารอบนี้ แล้วรอ ถ้ามีรอบใหม่มาทับ ให้เลิก ---
MY_ID="$$-$(date +%s%N)"
echo "$MY_ID" > "$STAMP"
sleep 1.5
if [[ "$(cat "$STAMP")" != "$MY_ID" ]]; then
  exit 0   # มี save ใหม่กว่านี้แล้ว ให้รอบนั้นทำแทน
fi

# --- lock: กันไม่ให้ redeploy ซ้อนกัน (รอคิวแทนที่จะข้าม) ---
exec 9>"$LOCK"
flock 9

# ถ้ามี save ใหม่เข้ามาระหว่างรอคิว ให้รอบนั้นทำแทน
if [[ "$(cat "$STAMP")" != "$MY_ID" ]]; then
  exit 0
fi

./mvnw -q -o package cargo:redeploy
echo "Redeployed at $(date +%T)"