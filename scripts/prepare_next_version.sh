#! /bin/bash

# DEPENDS ON gren:
# curl https://raw.githubusercontent.com/creationix/nvm/v0.33.8/install.sh | bash
# source ~/.nvm/nvm.sh
# nvm install v8.11.1
# npm install github-release-notes -g

# Version
NEXT_VERSION=$1

function syntax {
  echo "Syntax:  $1 NEXT_VERSION"
  echo "Example: $1 2.3.0"
}

if [[ -z "$NEXT_VERSION" ]]; then
  syntax $0
  exit 1
fi


cat << EOF
################################
# Prepare for next version
################################
EOF

# Updating RODA Maven modules with next version SNAPSHOT
mvn versions:set versions:commit -DnewVersion=$NEXT_VERSION-SNAPSHOT

# Commit Maven version update
git add -u
git commit -m "Setting version $NEXT_VERSION-SNAPSHOT"

# Push commits
git push
