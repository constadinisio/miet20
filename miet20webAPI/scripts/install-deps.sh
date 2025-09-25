#!/bin/bash
set -e

echo "📦 Instalando dependencias del proyecto..."

# Ir a la carpeta de la app
cd /var/www/miet20web/config || exit 1

# --- Node.js ---
if [ -f "package.json" ]; then
  echo "📦 Instalando dependencias Node.js..."
  # Usá npm o yarn según lo que uses en tu proyecto
  npm install --production
fi

# --- PHP Composer ---
if [ -f "composer.json" ]; then
  echo "🎼 Instalando dependencias PHP (Composer)..."
  if ! command -v composer >/dev/null 2>&1; then
    echo "⚠️ Composer no está instalado, instalándolo..."
    EXPECTED_CHECKSUM="$(php -r 'copy("https://composer.github.io/installer.sig", "php://stdout");')"
    php -r "copy('https://getcomposer.org/installer', 'composer-setup.php');"
    ACTUAL_CHECKSUM="$(php -r "echo hash_file('sha384', 'composer-setup.php');")"
    if [ "$EXPECTED_CHECKSUM" != "$ACTUAL_CHECKSUM" ]; then
      >&2 echo 'ERROR: El instalador de Composer no es válido'
      rm composer-setup.php
      exit 1
    fi
    php composer-setup.php --install-dir=/usr/local/bin --filename=composer
    rm composer-setup.php
  fi
  composer install --no-dev --optimize-autoloader
fi

# --- Move Folders Dependencies ---

echo "📦 Moviendo carpetas de las dependencias a la ruta raíz..."

if [ -d "/var/www/miet20web/config/vendor" ]; then
  mv /var/www/miet20web/config/vendor /var/www/miet20web/ || exit 1
  echo "✅ Carpeta vendor movida"
fi

if [ -d "/var/www/miet20web/config/node_modules" ]; then
  mv /var/www/miet20web/config/node_modules /var/www/miet20web/ || exit 1
  echo "✅ Carpeta node_modules movida"
fi

echo "✅ Dependencias instaladas correctamente"
