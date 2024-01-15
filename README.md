# git-dora-lead-time-calculator

A project to calculate the DORA metric lead time with the info from a git repo.

# Quickstart

To run the latest version

`/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/ArloL/git-dora-lead-time-calculator/HEAD/run-latest.sh)"`

# Example

Checkout this repository and execute:
```
$ /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/ArloL/git-dora-lead-time-calculator/HEAD/run-latest.sh) v0.0.9 v0.0.8 2024-01-15T18:50:40+01:00"

Deploy time in UTC: 2024-01-15T17:50:40Z
Commit of release: 5b150b4ae14ab56a43eeab0da4931cd57bcc95dd: Bump org.codehaus.mojo:flatten-maven-plugin from 1.5.0 to 1.6.0
Commit of previous release: d3ee472d7354cf7c13d74ce2cc41f8ed9626cbe9: Bump actions/upload-artifact from 4.0.0 to 4.1.0
Considering commit 5b150b4ae14ab56a43eeab0da4931cd57bcc95dd with author time of 2024-01-15T01:31:03Z and message: Bump org.codehaus.mojo:flatten-maven-plugin from 1.5.0 to 1.6.0
Average between author and deploy times: PT16H19M37S
```

# Install

To install the latest version to `~/bin/`

`/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/ArloL/git-dora-lead-time-calculator/HEAD/install-latest.sh)"`
