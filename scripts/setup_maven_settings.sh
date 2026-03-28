#!/bin/bash
# Creates ~/.m2/settings.xml using GITHUB_MAVEN_USER and GITHUB_MAVEN_PASSWORD
# environment variables for GitHub Packages authentication.

set -e

if [ -z "$GITHUB_MAVEN_USER" ] || [ -z "$GITHUB_MAVEN_PASSWORD" ]; then
  echo "Error: GITHUB_MAVEN_USER and GITHUB_MAVEN_PASSWORD must be set." >&2
  exit 1
fi

mkdir -p "$HOME/.m2"

cat > "$HOME/.m2/settings.xml" << EOF
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>${GITHUB_MAVEN_USER}</username>
      <password>${GITHUB_MAVEN_PASSWORD}</password>
    </server>
  </servers>
</settings>
EOF

echo "Maven settings.xml written to $HOME/.m2/settings.xml"
