#!/bin/bash

# Configuration
REMOTE_HOST="hostinger-vps"
REMOTE_DIR="/var/www/ibnalsham.prolabtech.net"
TARGET="${1:-backend}"

# Ensure we are in the script's directory (backend/)
cd "$(dirname "$0")"

echo "=================================================="
echo "🚀 Deploying Ibn Alsham ($TARGET) to $REMOTE_HOST"
echo "=================================================="

# 1. Sync only changed files to VPS (no --delete, incremental)
echo "🔄 Uploading only changed files to VPS..."
rsync -avz --update --itemize-changes \
    --exclude '.env' \
    --exclude '.env.production' \
    --exclude '.git' \
    --exclude 'vendor' \
    --exclude 'node_modules' \
    --exclude 'storage/app/public/*' \
    --exclude 'storage/framework/cache/data/*' \
    --exclude 'storage/framework/sessions/*' \
    --exclude 'storage/framework/views/*.php' \
    --exclude 'storage/logs/*.log' \
    --exclude 'deploy.sh' \
    ./ $REMOTE_HOST:$REMOTE_DIR/

# 2. Remote commands
echo "🧹 Cleaning up and optimizing on VPS..."
ssh $REMOTE_HOST "cd $REMOTE_DIR && \
    chown -R www-data:www-data . && \
    chmod -R 775 storage bootstrap/cache && \
    sudo -u www-data php artisan optimize:clear && \
    sudo -u www-data php artisan migrate --force && \
    sudo -u www-data php artisan config:cache && \
    sudo -u www-data php artisan route:cache && \
    sudo -u www-data php artisan view:cache"

echo "=================================================="
echo "✅ Deployment finished successfully!"
echo "=================================================="
