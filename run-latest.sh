#!/bin/sh

set -o errexit
set -o nounset
#set -o xtrace

OS="$(uname)"
if [ "${OS}" = "Linux" ]; then
  	platform=linux
elif [ "${OS}" = "Darwin" ]; then
  	platform=macos
else
	platform=windows
fi

cleanup() {
    currentExitCode=$?
    rm -f "./git-dora-lead-time-calculator"
    exit ${currentExitCode}
}

trap cleanup INT TERM EXIT

wget --quiet \
    --output-document="./git-dora-lead-time-calculator" \
    "https://github.com/ArloL/git-dora-lead-time-calculator/releases/latest/download/git-dora-lead-time-calculator-${platform}"

chmod +x "./git-dora-lead-time-calculator"

"./git-dora-lead-time-calculator" --version

"./git-dora-lead-time-calculator" "$@"
