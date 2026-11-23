param(
    [Parameter(Mandatory = $true)][int]$Paso,
    [Parameter(Mandatory = $true)][ValidateSet("Emiliano", "Francisco")][string]$Yo,
    [Parameter(Mandatory = $true)][string]$Origen
)

$ErrorActionPreference = "Stop"
$origen = (Resolve-Path $Origen).Path
$plan = Import-Csv -Path (Join-Path $PSScriptRoot "plan.csv") -Encoding UTF8
$fila = $plan | Where-Object { [int]$_.paso -eq $Paso }

if (-not $fila) { throw "No existe el paso $Paso" }
if ($fila.autor -ne $Yo) { throw "El paso $Paso le toca a $($fila.autor), no a $Yo" }

Write-Host "Paso $Paso ($Yo): $($fila.mensaje)"
Write-Host "Git va a firmar como: $(git config user.name) <$(git config user.email)>"

git branch -M main
if (git ls-remote --heads origin main) { git pull --rebase origin main }

foreach ($ruta in $fila.rutas.Split(";")) {
    $fuente = Join-Path $origen $ruta
    $destino = Join-Path (Get-Location).Path $ruta
    if (Test-Path $fuente -PathType Container) {
        New-Item -ItemType Directory -Force -Path $destino | Out-Null
        Copy-Item -Path (Join-Path $fuente "*") -Destination $destino -Recurse -Force
    } else {
        New-Item -ItemType Directory -Force -Path (Split-Path $destino) | Out-Null
        Copy-Item -Path $fuente -Destination $destino -Force
    }
    git add -- $ruta
}

git commit -m $fila.mensaje
git push -u origin main
