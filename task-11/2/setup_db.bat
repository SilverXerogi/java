@echo off
echo Установка базы данных PostgreSQL...

echo Удаление старой базы данных bookstore...
psql -U postgres -c "DROP DATABASE IF EXISTS bookstore;"

echo Создание базы данных bookstore...
psql -U postgres -c "CREATE DATABASE bookstore;"

echo Запуск скрипта schema.sql...
psql -U postgres -d bookstore -f schema.sql

echo Запуск скрипта data.sql...
psql -U postgres -d bookstore -f data.sql

echo Установка завершена.
pause