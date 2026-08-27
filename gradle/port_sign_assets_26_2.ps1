param()

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$projectRoot = Split-Path -Parent $PSScriptRoot
$namespaceRoot = Join-Path $projectRoot 'src\generated\resources\assets\farmersdelight'
$mainTextureRoot = Join-Path $projectRoot 'src\main\resources\assets\farmersdelight\textures'
$blockStateRoot = Join-Path $namespaceRoot 'blockstates'
$blockModelRoot = Join-Path $namespaceRoot 'models\block'
$blockTextureRoot = Join-Path $mainTextureRoot 'block'
$standingGuiRoot = Join-Path $mainTextureRoot 'gui\signs'

New-Item -ItemType Directory -Force $blockStateRoot, $blockModelRoot, $blockTextureRoot, $standingGuiRoot | Out-Null

$colors = @(
    '', 'white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink',
    'gray', 'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black'
)

function Write-JsonFile([string] $path, [object] $value) {
    $json = $value | ConvertTo-Json -Depth 12
    [System.IO.File]::WriteAllText($path, $json + [Environment]::NewLine, [System.Text.UTF8Encoding]::new($false))
}

function Copy-Pixels(
    [System.Drawing.Bitmap] $source,
    [System.Drawing.Bitmap] $target,
    [int] $sourceX,
    [int] $sourceY,
    [int] $width,
    [int] $height,
    [int] $targetX,
    [int] $targetY
) {
    for ($y = 0; $y -lt $height; $y++) {
        for ($x = 0; $x -lt $width; $x++) {
            $target.SetPixel($targetX + $x, $targetY + $y, $source.GetPixel($sourceX + $x, $sourceY + $y))
        }
    }
}

function Save-Png([System.Drawing.Bitmap] $bitmap, [string] $path) {
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
}

function Convert-StandingTexture([string] $sourcePath, [string] $texturePath, [string] $guiPath) {
    $source = [System.Drawing.Bitmap]::FromFile($sourcePath)
    try {
        $target = [System.Drawing.Bitmap]::new(32, 32, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            # Board: top, bottom, west, north, east and south faces.
            Copy-Pixels $source $target 2 0 24 2 0 0
            Copy-Pixels $source $target 26 0 24 2 0 28
            Copy-Pixels $source $target 0 2 2 12 24 16
            Copy-Pixels $source $target 2 2 24 12 0 16
            Copy-Pixels $source $target 26 2 2 12 24 2
            Copy-Pixels $source $target 28 2 24 12 0 2

            # Post faces.
            Copy-Pixels $source $target 2 16 2 14 28 16
            Copy-Pixels $source $target 4 16 2 14 30 0
            Copy-Pixels $source $target 6 16 2 14 28 0
            Copy-Pixels $source $target 0 16 2 14 30 16
            Copy-Pixels $source $target 4 14 2 2 28 30
            Save-Png $target $texturePath
        }
        finally {
            $target.Dispose()
        }

        # The 26.2 sign editor uses a dedicated 24x26 front preview.
        $gui = [System.Drawing.Bitmap]::new(24, 26, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            Copy-Pixels $source $gui 2 2 24 12 0 0
            Copy-Pixels $source $gui 2 16 2 14 11 12
            Save-Png $gui $guiPath
        }
        finally {
            $gui.Dispose()
        }
    }
    finally {
        $source.Dispose()
    }
}

function Convert-HangingTexture([string] $sourcePath, [string] $texturePath) {
    $source = [System.Drawing.Bitmap]::FromFile($sourcePath)
    try {
        $target = [System.Drawing.Bitmap]::new(32, 32, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            # Wall support plank.
            Copy-Pixels $source $target 4 0 16 4 0 0
            Copy-Pixels $source $target 20 0 16 4 0 9
            Copy-Pixels $source $target 0 4 4 2 16 7
            Copy-Pixels $source $target 4 4 16 2 0 7
            Copy-Pixels $source $target 20 4 4 2 16 4
            Copy-Pixels $source $target 24 4 16 2 0 4

            # Normal and attached chains.
            Copy-Pixels $source $target 0 6 3 6 22 7
            Copy-Pixels $source $target 6 6 3 4 28 8
            Copy-Pixels $source $target 14 6 12 6 20 0

            # Board: west, south, east, north, top and bottom faces.
            Copy-Pixels $source $target 0 14 2 10 0 16
            Copy-Pixels $source $target 18 14 14 10 2 16
            Copy-Pixels $source $target 16 14 2 10 16 16
            Copy-Pixels $source $target 2 14 14 10 18 16
            Copy-Pixels $source $target 2 12 14 2 2 14
            Copy-Pixels $source $target 16 12 14 2 2 26
            Save-Png $target $texturePath
        }
        finally {
            $target.Dispose()
        }
    }
    finally {
        $source.Dispose()
    }
}

function New-Model([string] $parent, [string] $texture, [string] $particle) {
    return [ordered]@{
        parent = $parent
        textures = [ordered]@{
            all = $texture
            particle = $particle
        }
    }
}

function New-WallVariants([string] $model) {
    return [ordered]@{
        variants = [ordered]@{
            'facing=east' = [ordered]@{ model = $model; y = 270 }
            'facing=north' = [ordered]@{ model = $model; y = 180 }
            'facing=south' = [ordered]@{ model = $model }
            'facing=west' = [ordered]@{ model = $model; y = 90 }
        }
    }
}

foreach ($color in $colors) {
    $colorPrefix = if ($color) { $color + '_' } else { '' }
    $entitySuffix = if ($color) { '_' + $color } else { '' }

    $standingId = $colorPrefix + 'canvas_sign'
    $standingWallId = $colorPrefix + 'canvas_wall_sign'
    $hangingId = $colorPrefix + 'hanging_canvas_sign'
    $hangingWallId = $colorPrefix + 'wall_hanging_canvas_sign'

    $standingEntityTexture = Join-Path $mainTextureRoot ('entity\signs\canvas' + $entitySuffix + '.png')
    $hangingEntityTexture = Join-Path $mainTextureRoot ('entity\signs\hanging\canvas' + $entitySuffix + '.png')
    $standingTexture = Join-Path $blockTextureRoot ($standingId + '.png')
    $hangingTexture = Join-Path $blockTextureRoot ($hangingId + '.png')
    $standingGuiTexture = Join-Path $standingGuiRoot ('canvas' + $entitySuffix + '.png')

    Convert-StandingTexture $standingEntityTexture $standingTexture $standingGuiTexture
    Convert-HangingTexture $hangingEntityTexture $hangingTexture

    $standingTextureId = 'farmersdelight:block/' + $standingId
    $hangingTextureId = 'farmersdelight:block/' + $hangingId

    $standingVariants = [ordered]@{}
    for ($rotation = 0; $rotation -lt 16; $rotation++) {
        $quarter = $rotation % 4
        $model = 'farmersdelight:block/' + $standingId + '_rot_' + $quarter
        $variant = [ordered]@{ model = $model }
        $yRotation = [Math]::Floor($rotation / 4) * 90
        if ($yRotation -ne 0) { $variant.y = $yRotation }
        $standingVariants['rotation=' + $rotation] = $variant
    }
    Write-JsonFile (Join-Path $blockStateRoot ($standingId + '.json')) ([ordered]@{ variants = $standingVariants })
    Write-JsonFile (Join-Path $blockStateRoot ($standingWallId + '.json')) (New-WallVariants ('farmersdelight:block/' + $standingWallId))

    for ($quarter = 0; $quarter -lt 4; $quarter++) {
        Write-JsonFile (Join-Path $blockModelRoot ($standingId + '_rot_' + $quarter + '.json')) `
            (New-Model ('minecraft:block/template_sign_rot_' + $quarter) $standingTextureId 'minecraft:block/spruce_planks')
    }
    Write-JsonFile (Join-Path $blockModelRoot ($standingWallId + '.json')) `
        (New-Model 'minecraft:block/template_wall_sign' $standingTextureId 'minecraft:block/spruce_planks')

    $hangingVariants = [ordered]@{}
    foreach ($attached in @('false', 'true')) {
        for ($rotation = 0; $rotation -lt 16; $rotation++) {
            $quarter = $rotation % 4
            $modelPart = if ($attached -eq 'true') { '_attached_rot_' } else { '_rot_' }
            $model = 'farmersdelight:block/' + $hangingId + $modelPart + $quarter
            $variant = [ordered]@{ model = $model }
            $yRotation = [Math]::Floor($rotation / 4) * 90
            if ($yRotation -ne 0) { $variant.y = $yRotation }
            $hangingVariants['attached=' + $attached + ',rotation=' + $rotation] = $variant
        }
    }
    Write-JsonFile (Join-Path $blockStateRoot ($hangingId + '.json')) ([ordered]@{ variants = $hangingVariants })
    Write-JsonFile (Join-Path $blockStateRoot ($hangingWallId + '.json')) (New-WallVariants ('farmersdelight:block/' + $hangingWallId))

    for ($quarter = 0; $quarter -lt 4; $quarter++) {
        Write-JsonFile (Join-Path $blockModelRoot ($hangingId + '_rot_' + $quarter + '.json')) `
            (New-Model ('minecraft:block/template_hanging_sign_rot_' + $quarter) $hangingTextureId 'minecraft:block/stripped_spruce_log')
        Write-JsonFile (Join-Path $blockModelRoot ($hangingId + '_attached_rot_' + $quarter + '.json')) `
            (New-Model ('minecraft:block/template_attached_hanging_sign_rot_' + $quarter) $hangingTextureId 'minecraft:block/stripped_spruce_log')
    }
    Write-JsonFile (Join-Path $blockModelRoot ($hangingWallId + '.json')) `
        (New-Model 'minecraft:block/template_wall_hanging_sign' $hangingTextureId 'minecraft:block/stripped_spruce_log')
}

Write-Output 'Generated Minecraft 26.2 canvas-sign blockstates, models and textures.'
