/**
 * pdf-to-txt
 * Extrae el texto de todos los PDFs de un directorio (recursivamente)
 * y guarda un .txt por cada uno, conservando la estructura de carpetas.
 *
 * Uso:
 *   node convert.mjs
 *   node convert.mjs --in ../../docs --out ./salida
 *   node convert.mjs --in "C:\ruta\a\pdfs" --out "C:\ruta\salida"
 */
import { createRequire } from 'node:module';
import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const require = createRequire(import.meta.url);
const __dirname = path.dirname(fileURLToPath(import.meta.url));

// pdfjs-dist expone el build "legacy" para Node.
let pdfjs;
try {
  pdfjs = await import('pdfjs-dist/legacy/build/pdf.mjs');
} catch {
  pdfjs = require('pdfjs-dist/legacy/build/pdf.js');
}

const { getDocument } = pdfjs;

function parseArgs(argv) {
  const args = { in: path.resolve(__dirname, '../../docs'), out: path.resolve(__dirname, './salida') };
  for (let i = 2; i < argv.length; i++) {
    if (argv[i] === '--in' && argv[i + 1]) args.in = path.resolve(process.cwd(), argv[++i]);
    else if (argv[i] === '--out' && argv[i + 1]) args.out = path.resolve(process.cwd(), argv[++i]);
    else if (argv[i] === '--help' || argv[i] === '-h') {
      console.log('Uso: node convert.mjs [--in <carpeta>] [--out <carpeta>]');
      process.exit(0);
    }
  }
  return args;
}

async function walk(dir) {
  const entries = await fs.readdir(dir, { withFileTypes: true });
  const files = [];
  for (const entry of entries) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) files.push(...(await walk(full)));
    else if (entry.isFile() && entry.name.toLowerCase().endsWith('.pdf')) files.push(full);
  }
  return files;
}

function safeRelativeName(name) {
  return name.replace(/[<>:"/\\|?*\u0000-\u001F]/g, '_');
}

function pageText(items) {
  let text = '';
  for (const item of items) {
    if (typeof item.str !== 'string') continue;
    text += item.str;
    if (item.hasEOL) text += '\n';
  }
  // Normaliza finales de línea y limita líneas en blanco consecutivas.
  return text
    .replace(/\r\n?/g, '\n')
    .replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]/g, '') // caracteres de control
    .replace(/[ \t]+\n/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim();
}

async function convertPdf(file, inputRoot, outputRoot) {
  const data = new Uint8Array(await fs.readFile(file));
  const loadingTask = getDocument({
    data,
    useSystemFonts: true,
    isEvalSupported: false,
    disableFontFace: true,
  });
  const pdf = await loadingTask.promise;

  const pages = [];
  let emptyPages = 0;
  for (let pageNumber = 1; pageNumber <= pdf.numPages; pageNumber++) {
    const page = await pdf.getPage(pageNumber);
    const content = await page.getTextContent();
    const text = pageText(content.items);
    if (!text) emptyPages++;
    pages.push(`=== PÁGINA ${pageNumber} ===\n${text || '[Sin texto extraíble en esta página]'}`);
    page.cleanup();
  }

  const rel = path.relative(inputRoot, file);
  const relSafe = rel.split(/[\\/]/).map(safeRelativeName).join(path.sep);
  const outFile = path.join(outputRoot, relSafe.replace(/\.pdf$/i, '.txt'));
  await fs.mkdir(path.dirname(outFile), { recursive: true });

  const header = [
    `Origen: ${rel}`,
    `Páginas: ${pdf.numPages}`,
    `Extraído: ${new Date().toISOString()}`,
    `Motor: pdfjs-dist`,
    ''.padEnd(70, '='),
    '',
  ].join('\n');

  const body = header + pages.join('\n\n');
  await fs.writeFile(outFile, body, 'utf8');
  if (typeof loadingTask.destroy === 'function') await loadingTask.destroy();

  return {
    file: rel,
    outFile,
    pages: pdf.numPages,
    chars: body.length,
    emptyPages,
  };
}

async function main() {
  const { in: inputRoot, out: outputRoot } = parseArgs(process.argv);
  await fs.mkdir(outputRoot, { recursive: true });

  const pdfs = await walk(inputRoot);
  if (pdfs.length === 0) {
    console.log(`No se encontraron PDFs en: ${inputRoot}`);
    return;
  }

  console.log(`Entrada : ${inputRoot}`);
  console.log(`Salida  : ${outputRoot}`);
  console.log(`PDFs    : ${pdfs.length}\n`);

  let failed = 0;
  for (const [index, file] of pdfs.entries()) {
    const rel = path.relative(inputRoot, file);
    process.stdout.write(`[${index + 1}/${pdfs.length}] ${rel} ... `);
    try {
      const result = await convertPdf(file, inputRoot, outputRoot);
      const warning = result.emptyPages > 0 ? ` (${result.emptyPages} pág. sin texto)` : '';
      console.log(`OK -> ${path.relative(process.cwd(), result.outFile)} [${result.pages} pág., ${result.chars} chars]${warning}`);
    } catch (error) {
      failed++;
      console.log(`ERROR: ${error?.message ?? error}`);
    }
  }

  console.log(`\nListo. Convertidos: ${pdfs.length - failed}/${pdfs.length}.`);
  if (failed > 0) process.exitCode = 1;
}

await main();
