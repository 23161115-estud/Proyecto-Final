param(
    [string]$Repo = "23161115-estud/Proyecto-Final",
    [string]$UsuarioEmiliano = "",
    [string]$UsuarioFrancisco = ""
)

$ErrorActionPreference = "Stop"
$owner = $Repo.Split("/")[0]

gh auth status
gh auth refresh -s project,repo

gh repo edit $Repo --visibility public --accept-visibility-change-consequences

if ($UsuarioFrancisco -and $UsuarioFrancisco -ne $owner) {
    gh api -X PUT "repos/$Repo/collaborators/$UsuarioFrancisco" -f permission=push | Out-Null
}
if ($UsuarioEmiliano -and $UsuarioEmiliano -ne $owner) {
    gh api -X PUT "repos/$Repo/collaborators/$UsuarioEmiliano" -f permission=push | Out-Null
}

$etiquetas = @{
    "backend"       = "1D5E4B"
    "frontend"      = "B02A5B"
    "base-de-datos" = "0E8A16"
    "comunicacion"  = "F2A900"
    "despliegue"    = "5319E7"
    "diseno"        = "D93F0B"
    "documentacion" = "0075CA"
    "pruebas"       = "FBCA04"
}
foreach ($nombre in $etiquetas.Keys) {
    gh label create $nombre --color $etiquetas[$nombre] --repo $Repo --force | Out-Null
}

$creado = gh project create --owner $owner --title "OaxInvest - Tablero" --format json | ConvertFrom-Json
$numero = $creado.number
$proyecto = gh project view $numero --owner $owner --format json | ConvertFrom-Json

gh project edit $numero --owner $owner --visibility PUBLIC | Out-Null
gh project link $numero --owner $owner --repo $Repo | Out-Null

$campos = gh project field-list $numero --owner $owner --format json | ConvertFrom-Json
$estado = $campos.fields | Where-Object { $_.name -eq "Status" }

$columnas = @("Backlog", "To Do", "In Progress", "In Review", "Done")
$colores = @("GRAY", "BLUE", "YELLOW", "ORANGE", "GREEN")
$opciones = @()
for ($i = 0; $i -lt $columnas.Count; $i++) {
    $opciones += @{ name = $columnas[$i]; color = $colores[$i]; description = "" }
}
$consulta = 'mutation($fieldId: ID!, $opts: [ProjectV2SingleSelectFieldOptionInput!]!) { updateProjectV2Field(input: { fieldId: $fieldId, singleSelectOptions: $opts }) { projectV2Field { ... on ProjectV2SingleSelectField { id } } } }'
$cuerpo = @{ query = $consulta; variables = @{ fieldId = $estado.id; opts = $opciones } } | ConvertTo-Json -Depth 10
$archivo = [System.IO.Path]::GetTempFileName()
[System.IO.File]::WriteAllText($archivo, $cuerpo, (New-Object System.Text.UTF8Encoding $false))
gh api graphql --input $archivo | Out-Null
Remove-Item $archivo

$campos = gh project field-list $numero --owner $owner --format json | ConvertFrom-Json
$estado = $campos.fields | Where-Object { $_.name -eq "Status" }
$idOpcion = @{}
foreach ($o in $estado.options) { $idOpcion[$o.name] = $o.id }

$filas = Import-Csv -Path (Join-Path $PSScriptRoot "issues.csv") -Encoding UTF8
foreach ($fila in $filas) {
    $usuario = if ($fila.responsable -eq "Emiliano") { $UsuarioEmiliano } else { $UsuarioFrancisco }
    $argumentos = @("issue", "create", "--repo", $Repo, "--title", $fila.titulo, "--body", $fila.descripcion, "--label", $fila.etiquetas)
    if ($usuario) { $argumentos += @("--assignee", $usuario) }
    $url = (gh @argumentos)
    if ($LASTEXITCODE -ne 0 -and $usuario) {
        $url = (gh issue create --repo $Repo --title $fila.titulo --body $fila.descripcion --label $fila.etiquetas)
    }
    $url = "$url".Trim()
    $item = gh project item-add $numero --owner $owner --url $url --format json | ConvertFrom-Json
    gh project item-edit --id $item.id --project-id $proyecto.id --field-id $estado.id --single-select-option-id $idOpcion[$fila.estado] | Out-Null
    if ($fila.estado -eq "Done") { gh issue close $url --repo $Repo | Out-Null }
    Start-Sleep -Milliseconds 800
}

Write-Host "Tablero listo: $($proyecto.url)"
