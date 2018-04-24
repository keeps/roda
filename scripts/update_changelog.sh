#! /bin/bash

# DEPENDS ON gren:
# curl https://raw.githubusercontent.com/creationix/nvm/v0.33.8/install.sh | bash
# source ~/.nvm/nvm.sh
# nvm install v8.11.1
# npm install github-release-notes -g

# Version
RELEASE_VERSION=$1

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
# Update changelog
################################
EOF

RELEASE_TAG="v$RELEASE_VERSION"

# Generate changelog
gren changelog --override

# Commit changelog
git add CHANGELOG.md
git commit -m "Updating changelog [ci skip]"

# Updating tag
git tag -d "$RELEASE_TAG"
git tag -a "$RELEASE_TAG" -m "Version $RELEASE_VERSION"

# Push tag
git push --force origin "$RELEASE_TAG"
