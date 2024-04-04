#! /bin/bash

# DEPENDS ON gren:
# curl https://raw.githubusercontent.com/creationix/nvm/v0.33.8/install.sh | bash
# source ~/.nvm/nvm.sh
# nvm install v8.11.1
# npm install github-release-notes -g

# Version
RELEASE_VERSION=$1
NEXT_VERSION=$2

function syntax {
  echo "Syntax:  $1 RELEASE_VERSION"
  echo "Example: $1 2.2.0"
}

if [[ -z "$RELEASE_VERSION" ]]; then
  syntax $0
  exit 1
fi

cat << EOF
################################
# Release version
################################
EOF

RELEASE_TAG="v$RELEASE_VERSION"

# Ensure all classes have license header
mvn license:format

# Updating RODA Maven modules
mvn versions:set versions:commit -DnewVersion=$RELEASE_VERSION

# Commit Maven version update
git add -u
git commit -S -m "Setting version $RELEASE_VERSION"

# Create tag
git tag -s -a "$RELEASE_TAG" -m "Version $RELEASE_VERSION"

# Push tag
git push origin "$RELEASE_TAG"
