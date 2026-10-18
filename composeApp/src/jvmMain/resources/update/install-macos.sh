#!/bin/sh
# Replaces the running WaveLift.app with the app inside a downloaded DMG, then relaunches it.
# Usage: install-macos.sh <pid> <dmg> <app bundle>

PID="$1"
DMG="$2"
APP="$3"
DIR=$(dirname "$APP")
NEW="$DIR/.WaveLift-update.app"
OLD="$DIR/.WaveLift-old.app"
MNT=$(mktemp -d "${TMPDIR:-/tmp}/wavelift-dmg.XXXXXX")

echo "$(date) updating $APP from $DMG"

fail() {
    echo "$(date) error: $1"
    hdiutil detach -quiet "$MNT" 2>/dev/null
    rmdir "$MNT" 2>/dev/null
    rm -rf "$NEW"
    open "$APP"
    exit 1
}

# Wait up to 60 seconds for the app to quit
i=0
while kill -0 "$PID" 2>/dev/null && [ "$i" -lt 120 ]; do
    sleep 0.5
    i=$((i + 1))
done

hdiutil attach -quiet -nobrowse -readonly -noautoopen -mountpoint "$MNT" "$DMG" || fail "could not mount $DMG"
SRC=$(find "$MNT" -maxdepth 1 -name "*.app" | head -n 1)
[ -n "$SRC" ] || fail "no app found in $DMG"

rm -rf "$NEW" "$OLD"
ditto "$SRC" "$NEW" || fail "could not copy the new app"
hdiutil detach -quiet "$MNT"
rmdir "$MNT" 2>/dev/null

mv "$APP" "$OLD" || fail "could not move the old app aside"
if ! mv "$NEW" "$APP"; then
    mv "$OLD" "$APP"
    fail "could not move the new app into place"
fi
rm -rf "$OLD" "$DMG"

echo "$(date) done"
open "$APP"
