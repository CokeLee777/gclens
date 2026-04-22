#!/bin/sh
set -e

REPO="cokelee777/gclens"
SHARE_DIR="/usr/local/share/gclens"
BIN_DIR="/usr/local/bin"
USE_SUDO=""

run_cmd() {
  if [ -n "$USE_SUDO" ]; then
    sudo "$@"
  else
    "$@"
  fi
}

if ! command -v java >/dev/null 2>&1; then
  echo "Error: java not found on PATH. Please install a JDK and retry." >&2
  exit 1
fi

if [ ! -w "$BIN_DIR" ]; then
  if command -v sudo >/dev/null 2>&1; then
    USE_SUDO="sudo"
  else
    SHARE_DIR="${HOME}/.local/share/gclens"
    BIN_DIR="${HOME}/.local/bin"
    echo "Note: sudo not available. Installing to ${BIN_DIR}"
    echo "      Add it to your PATH if not already: export PATH=\"\$PATH:${BIN_DIR}\""
  fi
fi

VERSION=$(curl -fsSL "https://api.github.com/repos/${REPO}/releases/latest" \
  | grep '"tag_name"' | head -1 | cut -d'"' -f4)

if [ -z "$VERSION" ]; then
  echo "Error: could not determine latest gclens version" >&2
  exit 1
fi

echo "Installing gclens ${VERSION}..."

# Strip leading 'v' from tag defensively; current tags are bare (e.g. 0.1.0)
JAR_VERSION="${VERSION#v}"
JAR_URL="https://github.com/${REPO}/releases/download/${VERSION}/gclens-cli-${JAR_VERSION}.jar"
TMP_JAR=$(mktemp /tmp/gclens-XXXXXX.jar)
trap 'rm -f "$TMP_JAR"' EXIT INT TERM

curl -fsSL "$JAR_URL" -o "$TMP_JAR"
[ -s "$TMP_JAR" ] || { echo "Error: downloaded JAR is empty or download failed" >&2; exit 1; }

run_cmd mkdir -p "$SHARE_DIR"
run_cmd cp "$TMP_JAR" "$SHARE_DIR/gclens.jar"
run_cmd chmod 644 "$SHARE_DIR/gclens.jar"

run_cmd mkdir -p "$BIN_DIR"
TMP_WRAPPER=$(mktemp /tmp/gclens-wrapper-XXXXXX)
printf '#!/bin/sh\nexec java -jar "%s/gclens.jar" "$@"\n' "$SHARE_DIR" > "$TMP_WRAPPER"
run_cmd install -m 755 "$TMP_WRAPPER" "$BIN_DIR/gclens"
rm -f "$TMP_WRAPPER"

echo "gclens ${VERSION} installed to ${BIN_DIR}/gclens"
echo "Run: gclens --help"
