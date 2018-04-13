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
  echo "Syntax:  $1 RELEASE_VERSION NEXT_VERSION"
  echo "Example: $1 2.2.0 2.3.0"
}

if [[ -z "$RELEASE_VERSION" ]]; then
  syntax $0
  exit 1
fi

if [[ -z "$NEXT_VERSION" ]]; then
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
git commit -m "Setting version $RELEASE_VERSION"

# Create temporary tag
git tag -a "$RELEASE_TAG" -m "Version $RELEASE_VERSION"

# Push tag
git push origin "$RELEASE_TAG"

# Generate release draft
gren release --draft

cat << EOF
################################
# Accept release (manual)
################################

Please go to the release link above, review and accept release.
EOF

read -p "Press ENTER to continue"

cat << EOF
################################
# Update changelog
################################
EOF

# Generate changelog
gren changelog --override

# Commit changelog
git add CHANGELOG.md
git commit -m "Updating changelog"

# Updating tag
git tag -d "$RELEASE_TAG"
git tag -a "$RELEASE_TAG" -m "Version $RELEASE_VERSION"

# Push tag
git push --force origin "$RELEASE_TAG"

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
