#!/bin/bash
# Helper script to build the VIN SDK AAR, add it to Git, and push with a new tag

set -e

# Setup Java Home (Default Mac Android Studio Location)
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Check if version argument is provided
if [ -z "$1" ]; then
    echo "Usage: ./publish_aar.sh <version_tag>"
    echo "Example: ./publish_aar.sh 1.0.8"
    exit 1
fi

VERSION=$1
AAR_OUTPUT="vinsdk/build/outputs/aar/vinsdk-release.aar"
RELEASE_DIR="vinsdk/release"
RELEASE_FILE="$RELEASE_DIR/vinsdk-$VERSION.aar"

echo "Step 1: Building Release AAR..."
./gradlew :vinsdk:assembleRelease

echo "Step 2: Copying AAR to $RELEASE_FILE..."
mkdir -p "$RELEASE_DIR"
cp "$AAR_OUTPUT" "$RELEASE_FILE"

echo "Step 3: Committing AAR file..."
git add -f "$RELEASE_FILE"
git commit -m "Add compiled AAR for version $VERSION" || echo "No changes to commit"

echo "Step 4: Tagging and pushing to GitHub..."
# Delete remote and local tag if it already exists to overwrite it with the AAR
git tag -d "$VERSION" 2>/dev/null || true
git push origin :refs/tags/"$VERSION" 2>/dev/null || true

# Create the new tag
git tag "$VERSION"
git push origin main
git push origin "$VERSION"

echo "Done! The SDK version $VERSION has been built, committed, and pushed with the AAR!"
