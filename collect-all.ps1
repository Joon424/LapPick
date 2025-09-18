param(
  [switch]$ExportDDL = $false,
  [string]$OracleUser     = $env:ORACLE_USER,
  [string]$OraclePassword = $env:ORACLE_PASSWORD,
  [string]$OracleConnect  = $env:ORACLE_CONNECT   # 예: localhost:1521/XEPDB1
)

$ErrorActionPreference = 'Stop'
$Root    = (Get-Location).Path
$OutDir  = Join-Path $Root 'share-full'
$ZipPath = Join-Path $Root 'lappick-review.zip'
$MaxImageMB = 2

if (Test-Path $OutDir) { Remove-Item $OutDir -Recurse -Force }
if (Test-Path $ZipPath) { Remove-Item $ZipPath -Force }
New-Item -ItemType Directory -Path $OutDir | Out-Null

$includePaths = @(
  'pom.xml','mvnw','mvnw.cmd','.mvn',
  'src\main\java','src\main\resources','src\test\java','src\test\resources'
)
$excludeDirNames = @('.git','.idea','.metadata','target','node_modules','upload')
$excludeExtensions = @('.class','.jar','.war','.zip','.7z','.rar','.log','.tmp')

function Should-ExcludeFile($f){
  if ($excludeExtensions -contains $f.Extension.ToLower()) { return $true }
  foreach($ex in $excludeDirNames){ if ($f.FullName -match "\\$([regex]::Escape($ex))\\") { return $true } }
  return $false
}
function Copy-With-Filters($src,$dst){
  if (!(Test-Path $src)) { return }
  Get-ChildItem -LiteralPath $src -Recurse -Force | ForEach-Object {
    if ($_.PSIsContainer) { return }
    if (Should-ExcludeFile $_) { return }
    $sizeMB = [math]::Round(($_.Length/1MB),2)
    if ($sizeMB -gt $MaxImageMB -and $_.Extension -match '\.(png|jpg|jpeg|gif|bmp|webp|mp4|mov|avi)$') { return }
    $rel = $_.FullName.Substring($Root.Length).TrimStart('\','/')
    $dest = Join-Path $OutDir $rel
    New-Item -ItemType Directory -Path (Split-Path $dest) -Force | Out-Null
    Copy-Item -LiteralPath $_.FullName -Destination $dest -Force
  }
}
foreach($p in $includePaths){ Copy-With-Filters (Join-Path $Root $p) $OutDir }

# 민감정보 마스킹(복사본에만 적용)
$maskFileTypes = '*.properties','*.yml','*.yaml'
$maskPatterns = @(
  '(?i)(^\s*spring\.datasource\.username\s*[:=]\s*).+$',
  '(?i)(^\s*spring\.datasource\.password\s*[:=]\s*).+$',
  '(?i)(^\s*spring\.datasource\.url\s*[:=]\s*).+$',
  '(?i)(^\s*spring\.mail\.password\s*[:=]\s*).+$',
  '(?i)(^\s*jwt\.secret.*\s*[:=]\s*).+$',
  '(?i)(^\s*oauth2\..*client-secret\s*[:=]\s*).+$',
  '(?i)(^\s*username:\s*).+$',
  '(?i)(^\s*password:\s*).+$',
  '(?i)(^\s*url:\s*).+$'
)
Get-ChildItem -Path $OutDir -Recurse -Include $maskFileTypes | ForEach-Object {
  $t = Get-Content -LiteralPath $_.FullName -Raw
  foreach($pat in $maskPatterns){ $t = [regex]::Replace($t,$pat,'$1***') }
  Set-Content -LiteralPath $_.FullName -Value $t -Encoding UTF8
}

# 보조자료 생성
cmd /c "tree `"$OutDir`" /F" | Out-File -Encoding UTF8 (Join-Path $Root 'project-tree.txt')
try { .\mvnw.cmd -q dependency:tree        | Out-File -Encoding UTF8 (Join-Path $Root 'dep-tree.txt') } catch {}
try { .\mvnw.cmd -q help:effective-pom     | Out-File -Encoding UTF8 (Join-Path $Root 'effective-pom.xml') } catch {}
"## java -version"  | Out-File -Encoding UTF8 (Join-Path $Root 'env.txt')
try { & java -version 2>&1 | Out-File -Append -Encoding UTF8 (Join-Path $Root 'env.txt') } catch {}
"`n## mvnw -v"      | Out-File -Append -Encoding UTF8 (Join-Path $Root 'env.txt')
try { .\mvnw.cmd -v 2>&1 | Out-File -Append -Encoding UTF8 (Join-Path $Root 'env.txt') } catch {}

# DDL 안내 + 자동 내보내기(선택)
Write-Host "※ (선택) SQL*Plus에서 DDL 내보내기:" -ForegroundColor Yellow
$u = $OracleUser; if ([string]::IsNullOrWhiteSpace($u)) { $u='MISO' }
$c = $OracleConnect; if ([string]::IsNullOrWhiteSpace($c)) { $c='localhost:1521/XEPDB1' }
Write-Host ("   sqlplus {0}/비번@{1} @export_ddl.sql" -f $u,$c) -ForegroundColor Yellow
Write-Host "   -> 같은 폴더에 ddl.sql 생성됨. -ExportDDL 옵션 사용 시 자동 실행 + ZIP 포함." -ForegroundColor Yellow

$ddlSql = @'
SET LONG 100000
SET PAGESIZE 0
SET HEADING OFF
SPOOL ddl.sql
SELECT DBMS_METADATA.GET_DDL(object_type, object_name)
  FROM user_objects
 WHERE object_type IN ('TABLE','SEQUENCE','VIEW','INDEX','TRIGGER')
 ORDER BY object_type, object_name;
SPOOL OFF
'@
$ddlPath = Join-Path $Root 'export_ddl.sql'
$ddlSql | Out-File -Encoding UTF8 $ddlPath
$ddlFilePath = Join-Path $Root 'ddl.sql'

if ($ExportDDL) {
  # PS 5.1 호환: ?. 연산자 사용 안 함
  $sqlplus = $null
  $cmd = Get-Command sqlplus -ErrorAction SilentlyContinue
  if ($cmd) { $sqlplus = $cmd.Source }

  if (-not $sqlplus) {
    $candidates = @(
      "C:\oracle\product\*\dbhome_*\BIN\sqlplus.exe",
      "C:\app\*\product\*\dbhome*\BIN\sqlplus.exe",
      "C:\Program Files\Oracle\*\BIN\sqlplus.exe",
      "C:\oraclexe\app\oracle\product\21c\server\BIN\sqlplus.exe"
    )
    foreach ($pattern in $candidates) {
      $item = Get-Item $pattern -ErrorAction SilentlyContinue | Select-Object -First 1
      if ($item) { $sqlplus = $item.FullName; break }
    }
  }

  if ($sqlplus -and -not [string]::IsNullOrWhiteSpace($OracleUser) -and -not [string]::IsNullOrWhiteSpace($OraclePassword) -and -not [string]::IsNullOrWhiteSpace($OracleConnect)) {
    Write-Host "▶ DDL 자동 추출 실행 중..." -ForegroundColor Cyan
    $scriptArg = '@' + $ddlPath   # @"$ddlPath" 대신 안전하게 전달
    & $sqlplus -S "$OracleUser/$OraclePassword@$OracleConnect" $scriptArg | Out-Null

    if (Test-Path $ddlFilePath) { Write-Host "✔ ddl.sql 생성 완료 → ZIP 포함" -ForegroundColor Green }
    else { Write-Warning "ddl.sql 생성이 확인되지 않습니다. export_ddl.sql을 수동 실행해 보세요." }
  } else {
    Write-Warning "DDL 자동 추출이 비활성화됨(프로그램 또는 접속정보 미설정/미탐색). 수동 실행 가이드 참고."
  }
}

# ZIP 패키징(ddl.sql 있으면 포함)
$zipInputs = @($OutDir,(Join-Path $Root 'project-tree.txt'),(Join-Path $Root 'dep-tree.txt'),
               (Join-Path $Root 'effective-pom.xml'),(Join-Path $Root 'env.txt'))
if (Test-Path $ddlFilePath) { $zipInputs += $ddlFilePath }
$zipInputs = $zipInputs | Where-Object { Test-Path $_ }
Compress-Archive -Path $zipInputs -DestinationPath $ZipPath -Force

Write-Host "`n✅ 준비 완료: $ZipPath" -ForegroundColor Green
Write-Host "이 ZIP을 채팅에 업로드해 주세요." -ForegroundColor Green
