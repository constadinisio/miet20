@echo off
echo Generando Tailwind...
npx tailwindcss -c ./config/tailwind.config.js -i ./public/input.css -o ./public/output.css
pause