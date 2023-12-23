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

wget --quiet \
    --output-document="${HOME}/bin/git-dora-lead-time-calculator" \
    "https://github.com/ArloL/git-dora-lead-time-calculator/releases/latest/download/git-dora-lead-time-calculator-${platform}"

chmod +x "${HOME}/bin/git-dora-lead-time-calculator"
