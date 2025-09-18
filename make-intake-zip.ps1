<# 
  make-intake-zip.ps1
  - LapPick/mini 프로젝트용 “코드 인수 번들” 자동 생성 스크립트
  - Windows 11 + PowerShell 5/7 이상 가정
#>

param(
  [string]$ProjectRoot = (Get-Location).Path,   # 현재 폴더를 프로젝트 루트로 가정
  [string]$BundleName  = "LapPick_intake",      # 산출 zip 이름 prefix
  [switch]$IncludeTests                          # src/test/java 포함 여부(옵션)
)

# 0) 공통 준비 -----------------------------------------------------------------
$ErrorActionPreference = "Stop"
$ts        = Get-Date -Format "yyyyMMdd_HHmmss"
$staging   = Join-Path $ProjectRoot ".bundle_tmp_$ts"
$destZip   = Join-Path $ProjectRoot ("{0}_{1}.zip" -f $BundleName, $ts)

Write-Host "==> ProjectRoot : $ProjectRoot"
Write-Host "==> Staging     : $staging"
Write-Host "==> Output Zip  : $destZip"

# 1) 스테이징 폴더 생성 ---------------------------------------------------------
New-Item -ItemType Directory -Force -Path $staging | Out-Null

function Copy-IfExists {
  param(
    [Parameter(Mandatory=$true)][string]$Path,
    [Parameter(Mandatory=$true)][string]$Dest
  )
  if (Test-Path $Path) {
    $parent = Split-Path -Parent $Dest
    if (!(Test-Path $parent)) { New-Item -ItemType Directory -Force -Path $parent | Out-Null }
    Copy-Item -Path $Path -Destination $Dest -Recurse -Force
    Write-Host "  Copied => $Path"
  } else {
    Write-Host "  (skip)  $Path (not found)"
  }
}

# 2) 필수 파일/폴더만 화이트리스트 방식으로 복사 --------------------------------
# 루트 파일
Copy-IfExists -Path (Join-Path $ProjectRoot "pom.xml")         -Dest (Join-Path $staging "pom.xml")
Copy-IfExists -Path (Join-Path $ProjectRoot "mvnw")            -Dest (Join-Path $staging "mvnw")
Copy-IfExists -Path (Join-Path $ProjectRoot "mvnw.cmd")        -Dest (Join-Path $staging "mvnw.cmd")
Copy-IfExists -Path (Join-Path $ProjectRoot ".mvn")            -Dest (Join-Path $staging ".mvn")
Copy-IfExists -Path (Join-Path $ProjectRoot "README*")         -Dest (Join-Path $staging ".")       # 있으면 복사
Copy-IfExists -Path (Join-Path $ProjectRoot "LICENSE*")        -Dest (Join-Path $staging ".")

# 소스 디렉터리
Copy-IfExists -Path (Join-Path $ProjectRoot "src\main\java")     -Dest (Join-Path $staging "src\main\java")
Copy-IfExists -Path (Join-Path $ProjectRoot "src\main\resources")-Dest (Join-Path $staging "src\main\resources")
Copy-IfExists -Path (Join-Path $ProjectRoot "src\main\webapp")   -Dest (Join-Path $staging "src\main\webapp")

if ($IncludeTests.IsPresent) {
  Copy-IfExists -Path (Join-Path $ProjectRoot "src\test\java")   -Dest (Join-Path $staging "src\test\java")
}

# 3) application*.properties 비밀값 자동 마스킹 ---------------------------------
function Mask-SecretsIn-Properties {
  param([string]$FilePath)

  if (!(Test-Path $FilePath)) { return }

  Write-Host "  Masking secrets in   : $FilePath"
  $content = Get-Content $FilePath -Raw

  # 패턴 정의(좌변 키=정규식 키, 우변 값=치환값). 필요 시 추가하세요.
  $map = @{
    'spring\.datasource\.password' = '****'
    'spring\.datasource\.username' = '${SPRING_DATASOURCE_USERNAME}'  # 계정은 ENV로 유도
    'spring\.mail\.password'       = '****'
    'spring\.mail\.username'       = '${SPRING_MAIL_USERNAME}'
  }

  foreach ($k in $map.Keys) {
    $replacement = $map[$k]
    # "키 = 값" 형태를 보수적으로 탐지해 값만 치환
    $pattern = "(?m)^\s*($k\s*=\s*).*$"
    $content = [regex]::Replace($content, $pattern, "`$1$replacement")
  }

  # 저장(UTF-8). 필요 시 BOM 원하면 -Encoding UTF8BOM
  Set-Content -Path $FilePath -Value $content -Encoding UTF8
}

Get-ChildItem -Path (Join-Path $staging "src\main\resources") -Recurse -Filter "application*.properties" |
  ForEach-Object { Mask-SecretsIn-Properties -FilePath $_.FullName }

# 4) 보조자료 생성(프로젝트 트리/의존성 트리/매니페스트) -------------------------
# 4-1) 파일 매니페스트(상대경로, 크기, 수정시간)
$manifest = Get-ChildItem -Recurse $staging | ForEach-Object {
  $rel = $_.FullName.Substring($staging.Length).TrimStart('\','/')
  "{0}`t{1}`t{2}" -f $rel, $_.Length, $_.LastWriteTime.ToString("yyyy-MM-dd HH:mm:ss")
}
$manifest | Set-Content -Path (Join-Path $staging "bundle_manifest.txt") -Encoding UTF8

# 4-2) 프로젝트 트리 (cmd의 tree 활용)
$treeOut = Join-Path $staging "project_tree.txt"
cmd /c "tree /F `"$staging`" > `"$treeOut`"" | Out-Null

# 4-3) Maven 의존성 트리 (mvnw가 있으면 그것을 우선 사용)
$depTree = Join-Path $staging "dep_tree.txt"
try {
  Push-Location $ProjectRoot
  if (Test-Path (Join-Path $ProjectRoot "mvnw.cmd")) {
    cmd /c ".\mvnw.cmd -q dependency:tree -Dscope=compile > `"$depTree`""
  } else {
    cmd /c "mvn -q dependency:tree -Dscope=compile > `"$depTree`""
  }
} catch {
  "Failed to generate dependency tree: $($_.Exception.Message)" | Set-Content $depTree
} finally {
  Pop-Location
}

# 5) Zip 압축 -------------------------------------------------------------------
if (Test-Path $destZip) { Remove-Item $destZip -Force }
Compress-Archive -Path (Join-Path $staging "*") -DestinationPath $destZip -CompressionLevel Optimal

# 6) 임시 폴더 정리 & 결과 알림 ---------------------------------------------------
Remove-Item $staging -Recurse -Force
Write-Host ""
Write-Host "✅ 번들 생성 완료: $destZip"
