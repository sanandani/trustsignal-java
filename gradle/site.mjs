import fs from 'node:fs';
import path from 'node:path';

const source = path.resolve(process.argv[2]);
const output = path.resolve(process.argv[3]);
const docsRoot = path.basename(output) === 'docs' ? output : path.join(output, 'docs');
const baseUrl = process.env.PAGES_BASE_URL ?? '/trustsignal-java/';
const escapeHtml = (value) => value.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;');
const inline = (value) => escapeHtml(value)
  .replace(/`([^`]+)`/g, '<code>$1</code>')
  .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  .replace(/\*([^*]+)\*/g, '<em>$1</em>')
  .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>');

function renderMarkdown(markdown) {
  const lines = markdown.split(/\r?\n/);
  const output = [];
  let paragraph = [];
  let list = null;
  let codeFence = false;
  let codeLanguage = '';
  let codeLines = [];
  let tableRows = [];
  const flushParagraph = () => {
    if (paragraph.length) output.push(`<p>${inline(paragraph.join(' '))}</p>`);
    paragraph = [];
  };
  const closeList = () => { if (list) output.push(`</${list}>`); list = null; };
  const flushTable = () => {
    if (!tableRows.length) return;
    const cells = (line) => line.trim().replace(/^\||\|$/g, '').split('|').map(cell => cell.trim());
    const header = cells(tableRows[0]);
    const body = tableRows.slice(2);
    output.push('<div class="table-wrap"><table><thead><tr>' + header.map(cell => `<th>${inline(cell)}</th>`).join('') + '</tr></thead><tbody>' +
      body.map(row => '<tr>' + cells(row).map(cell => `<td>${inline(cell)}</td>`).join('') + '</tr>').join('') + '</tbody></table></div>');
    tableRows = [];
  };
  for (const line of lines) {
    if (line.startsWith('```')) {
      flushParagraph(); closeList(); flushTable();
      if (codeFence) {
        output.push(`<pre><code${codeLanguage ? ` class="language-${escapeHtml(codeLanguage)}"` : ''}>${escapeHtml(codeLines.join('\n'))}</code></pre>`);
        codeLines = [];
      } else codeLanguage = line.slice(3).trim();
      codeFence = !codeFence;
      continue;
    }
    if (codeFence) { codeLines.push(line); continue; }
    if (/^\s*\|/.test(line)) { flushParagraph(); closeList(); tableRows.push(line); continue; }
    flushTable();
    if (!line.trim()) { flushParagraph(); closeList(); continue; }
    const heading = line.match(/^(#{1,6})\s+(.*)$/);
    if (heading) {
      flushParagraph(); closeList();
      const level = heading[1].length;
      output.push(`<h${level}>${inline(heading[2])}</h${level}>`);
      continue;
    }
    const bullet = line.match(/^\s*[-*]\s+(.*)$/);
    const numbered = line.match(/^\s*\d+\.\s+(.*)$/);
    if (bullet || numbered) {
      flushParagraph();
      const nextList = bullet ? 'ul' : 'ol';
      if (list !== nextList) { closeList(); list = nextList; output.push(`<${list}>`); }
      output.push(`<li>${inline((bullet ?? numbered)[1])}</li>`);
      continue;
    }
    const quote = line.match(/^>\s?(.*)$/);
    if (quote) { flushParagraph(); closeList(); output.push(`<blockquote>${inline(quote[1])}</blockquote>`); continue; }
    paragraph.push(line.trim());
  }
  flushParagraph(); closeList(); flushTable();
  if (codeFence) output.push(`<pre><code>${escapeHtml(codeLines.join('\n'))}</code></pre>`);
  return output.join('\n');
}

function walk(from, into) {
  fs.mkdirSync(into, { recursive: true });
  for (const entry of fs.readdirSync(from, { withFileTypes: true })) {
    const file = path.join(from, entry.name);
    if (entry.isDirectory()) { walk(file, path.join(into, entry.name)); continue; }
    if (!entry.name.endsWith('.md')) continue;
    const relative = path.relative(source, file).replace(/\.md$/, '.html');
    const target = path.join(output, relative);
    const html = renderMarkdown(fs.readFileSync(file, 'utf8'));
    fs.mkdirSync(path.dirname(target), { recursive: true });
    const title = path.basename(relative, '.html').replaceAll('-', ' ');
    fs.writeFileSync(target, `<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>${escapeHtml(title)} · TrustSignal Java</title><link rel="stylesheet" href="${baseUrl}docs.css"></head><body><header class="top"><a href="${baseUrl}">TrustSignal <b>Java</b></a><nav><a href="${baseUrl}">Home</a><a href="${baseUrl}docs/ARCHITECTURE.html">Docs</a><a href="${baseUrl}api/">API</a><a href="https://github.com/sanandani/trustsignal-java">GitHub</a></nav></header><main class="doc"><aside><strong>Documentation</strong><a href="${baseUrl}docs/ARCHITECTURE.html">Architecture</a><a href="${baseUrl}docs/DESIGN.html">Design</a><a href="${baseUrl}docs/THREAT_MODEL.html">Threat model</a><a href="${baseUrl}docs/ROADMAP.html">Roadmap</a><a href="${baseUrl}docs/adr/0001-no-scalar-score.html">ADR 0001 · No scalar score</a><a href="${baseUrl}docs/adr/0002-capabilities-over-statics.html">ADR 0002 · Capabilities</a><a href="${baseUrl}docs/adr/0003-unverifiable-is-not-invalid.html">ADR 0003 · Unknown state</a></aside><article>${html}</article></main><footer>TrustSignal Java · Conceptual reference implementation · Not empirically validated</footer></body></html>`);
  }
}

fs.mkdirSync(output, { recursive: true });
const landingPath = path.join(path.dirname(docsRoot), 'index.html');
if (fs.existsSync(landingPath)) fs.writeFileSync(landingPath, fs.readFileSync(landingPath, 'utf8').replaceAll('__SITE_BASE__', baseUrl));
fs.rmSync(path.join(output, 'docs'), { recursive: true, force: true });
fs.rmSync(path.join(output, 'docs.css'), { force: true });
walk(source, docsRoot);
fs.writeFileSync(path.join(path.dirname(docsRoot), 'docs.css'), `:root{font-family:Inter,ui-sans-serif,system-ui,-apple-system,"Segoe UI",sans-serif;color:#202a35;background:#f5f7f9;font-synthesis:none;text-rendering:optimizeLegibility}*{box-sizing:border-box}body{margin:0}.top{height:64px;display:flex;align-items:center;justify-content:space-between;padding:0 max(calc((100vw - 1180px)/2),24px);background:#101922;border-bottom:1px solid #263543;color:#edf3f8}.top>a{font-weight:750;text-decoration:none;color:inherit;letter-spacing:-.04em}.top b{color:#c3f36b}.top nav{display:flex;gap:1.4rem}.top nav a{font-size:.82rem;text-decoration:none;color:#bdc7d0}.top nav a:hover{color:#c3f36b}.doc{max-width:1180px;margin:2.4rem auto;padding:0 24px;display:grid;grid-template-columns:220px minmax(0,780px);gap:4rem;align-items:start}.doc aside{position:sticky;top:24px;border-right:1px solid #dce2e7;padding:10px 24px 10px 0;display:flex;flex-direction:column;gap:.9rem}.doc aside strong{font-size:.77rem;text-transform:uppercase;letter-spacing:.09em;color:#637080;margin-bottom:.4rem}.doc aside a{font-size:.82rem;color:#576576;text-decoration:none;line-height:1.4}.doc aside a:hover{color:#2363a4}.doc article{min-width:0;background:#fff;border:1px solid #e3e8ed;border-radius:10px;padding:clamp(24px,4vw,54px);box-shadow:0 8px 32px #1c34410a;line-height:1.75;font-size:1rem;color:#394654}.doc article h1,.doc article h2,.doc article h3,.doc article h4{color:#17232e;line-height:1.25;letter-spacing:-.035em}.doc article h1{font-size:2.4rem;margin:.1rem 0 2rem}.doc article h2{font-size:1.55rem;margin:2.4rem 0 .8rem;border-bottom:1px solid #edf0f3;padding-bottom:.5rem}.doc article h3{font-size:1.15rem;margin:1.8rem 0 .5rem}.doc article p{margin:.8rem 0}.doc article a{color:#1767a9;text-decoration-thickness:1px;text-underline-offset:3px}.doc article ul,.doc article ol{padding-left:1.4rem}.doc article li{padding:.18rem 0}.doc article code{font-family:ui-monospace,SFMono-Regular,Menlo,monospace;font-size:.86em;background:#f0f3f6;color:#a43b32;padding:.14em .35em;border-radius:4px}.doc article pre{overflow:auto;background:#111a23;color:#d8e4ef;border-radius:7px;padding:1rem 1.15rem;line-height:1.6;font-size:.83rem}.doc article pre code{padding:0;color:inherit;background:none}.doc article blockquote{margin:1rem 0;padding:.4rem 1rem;border-left:3px solid #b2d780;background:#f7faef;color:#59654b}.table-wrap{overflow:auto;margin:1.2rem 0}.doc article table{border-collapse:collapse;width:100%;font-size:.87rem}.doc article th,.doc article td{border:1px solid #dfe5e9;padding:.65rem .75rem;text-align:left;vertical-align:top}.doc article th{background:#f1f5f7;color:#273440;font-weight:700}.doc article tr:nth-child(even) td{background:#fafbfc}footer{max-width:1180px;margin:2rem auto;padding:1.5rem 24px;color:#7c8792;border-top:1px solid #dce2e7;font-size:.75rem}@media(max-width:760px){.doc{display:block;margin:1rem auto}.doc aside{position:static;border:1px solid #e3e8ed;border-radius:8px;background:#fff;padding:1rem;margin-bottom:1rem;display:grid;grid-template-columns:1fr 1fr}.doc aside strong{grid-column:1/-1}.doc article{padding:24px}.doc article h1{font-size:1.9rem}.top{padding:0 16px}.top nav{gap:.7rem}.top nav a{font-size:.7rem}}`);
