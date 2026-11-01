# Convierte los PDFs de docs/ a texto plano.
# Uso: .\convertir.ps1 [-In <carpeta>] [-Out <carpeta>]
param(
    [string]$In = (Join-Path $PSScriptRoot '..\..\docs'),
    [string]$Out = (Join-Path $PSScriptRoot 'salida')
)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

if (-not (Test-Path (Join-Path $PSScriptRoot 'node_modules'))) {
    Write-Host 'Instalando dependencias (primera ejecución)...'
    npm install --no-audit --no-fund
    if ($LASTEXITCODE -ne 0) { throw 'Falló npm install.' }
}

node convert.mjs --in $In --out $Out
if ($LASTEXITCODE -ne 0) { throw 'Falló la conversión de PDFs.' }
