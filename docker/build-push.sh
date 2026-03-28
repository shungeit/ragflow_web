#!/bin/bash
# Build and push images to Aliyun Container Registry
# Usage: ./build-push.sh [version]
# Example: ./build-push.sh v1.0

set -e

VERSION="${1:-v1.0}"
REGISTRY="registry.cn-hangzhou.aliyuncs.com/sxsnamespace01"
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

BACKEND_IMAGE="${REGISTRY}/ragflow-admin-backend:${VERSION}"
FRONTEND_IMAGE="${REGISTRY}/ragflow-admin-frontend:${VERSION}"

echo "========================================="
echo "  RAGFlow Admin - Build & Push Images"
echo "  Version: ${VERSION}"
echo "========================================="

echo ""
echo "[1/4] Building backend image..."
docker build -t "${BACKEND_IMAGE}" "${PROJECT_DIR}/backend"

echo ""
echo "[2/4] Building frontend image..."
docker build -t "${FRONTEND_IMAGE}" "${PROJECT_DIR}/frontend"

echo ""
echo "[3/4] Pushing backend image..."
docker push "${BACKEND_IMAGE}"

echo ""
echo "[4/4] Pushing frontend image..."
docker push "${FRONTEND_IMAGE}"

echo ""
echo "========================================="
echo "  Done! Images pushed:"
echo "  ${BACKEND_IMAGE}"
echo "  ${FRONTEND_IMAGE}"
echo "========================================="
