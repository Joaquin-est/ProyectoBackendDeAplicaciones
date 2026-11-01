# pdf-to-txt

Herramienta local para convertir los PDFs de `docs/` a texto plano (`.txt`) y poder analizarlos con herramientas de texto o IA.

## Requisitos

- Node.js 20 o superior (probado con Node 24).
- No requiere Python, Java ni instalar Poppler.

## Uso rápido

Desde esta carpeta:

```powershell
.\convertir.ps1
```

El script instala las dependencias la primera vez (si falta `node_modules`) y convierte todos los PDFs de `docs/` en `tools/pdf-to-txt/salida/`.

## Uso manual

```powershell
npm install
node convert.mjs --in ../../docs --out ./salida
```

Parámetros:

| Parámetro | Por defecto | Descripción |
|---|---|---|
| `--in` | `../../docs` | Carpeta de entrada; busca PDFs recursivamente. |
| `--out` | `./salida` | Carpeta de salida; replica la estructura de entrada. |

## Salida

- Un `.txt` por cada `.pdf`, con el mismo nombre y la misma subcarpeta.
- Cada archivo incluye encabezado con origen, cantidad de páginas y fecha de extracción.
- El texto se separa con marcadores `=== PÁGINA n ===`.
- Si una página es una imagen escaneada, se indica `[Sin texto extraíble en esta página]`. Esta herramienta no hace OCR.

## Estructura

```
tools/pdf-to-txt/
├── convert.mjs        # Extractor basado en pdfjs-dist
├── convertir.ps1      # Wrapper de un solo paso para Windows
├── package.json
├── package-lock.json
├── .gitignore         # Ignora node_modules/ y salida/
└── salida/            # Textos generados (no versionar)
```

## Notas

- `salida/` y `node_modules/` están ignorados por Git.
- Los PDFs originales no se modifican.
- Si se agregan PDFs nuevos a `docs/`, basta con volver a ejecutar el script.
