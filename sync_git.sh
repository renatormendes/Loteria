#!/bin/bash

# --- Configurações por Mr. Magic ---
# AJUSTE AQUI: Coloque a URL completa do seu repositório (ex: .../renatormendes/Loteria.git)
REPO_URL="https://github.com"
EMAIL_GIT="renatormendes@hotmail.com"
NOME_GIT="renatormendes"

echo "--- Iniciando Sincronização Mágica com Git ---"

# 1. Configura Identidade (Resolve o erro "Author identity unknown")
git config --global user.email "$EMAIL_GIT"
git config --global user.name "$NOME_GIT"

# 2. Inicializa se necessário
if [ ! -d ".git" ]; then
    echo "[+] Inicializando repositório..."
    git init
    git remote add origin "$REPO_URL"
fi

# Garante que estamos na branch 'main' (Resolve o erro "src refspec main does not match")
git branch -M main

# 3. Processo de Upload
echo "[+] Adicionando arquivos..."
git add .

echo "[+] Realizando Commit..."
git commit -m "Update LottoGen Pro - $(date +'%d/%m/%Y %H:%M')"

echo "[+] Sincronizando com o servidor..."
git pull origin main --rebase

echo "[+] Subindo para o GitHub..."
# O Git pedirá seu Usuário e o Token de Acesso (PAT) nesta etapa
git push -u origin main

echo "--- Processo Concluído! ---"
