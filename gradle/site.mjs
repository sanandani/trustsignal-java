import fs from 'node:fs';
import path from 'node:path';
const source = process.argv[2];
const output = process.argv[3];
const escape = (s) => s.replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;');
const inline = (s) => escape(s).replace(/`([^`]+)`/g,'<code>$1</code>').replace(/\*\*([^*]+)\*\*/g,'<strong>$1</strong>').replace(/\*([^*]+)\*/g,'<em>$1</em>').replace(/\[([^\]]+)\]\(([^)]+)\)/g,'<a href="$2">$1</a>');
function render(markdown) {
 const lines=markdown.split(/\r?\n/), out=[]; let paragraph=[], list=null, code=false, codeLines=[];
 const flush=()=>{if(paragraph.length){out.push(`<p>${inline(paragraph.join(' '))}</p>`);paragraph=[];}};
 const closeList=()=>{if(list){out.push(`</${list}>`);list=null;}};
 for(const line of lines){
  if(line.startsWith('```')){flush();closeList();if(code){out.push(`<pre><code>${escape(codeLines.join('\n'))}</code></pre>`);codeLines=[];}code=!code;continue;}
  if(code){codeLines.push(line);continue;}
  if(!line.trim()){flush();closeList();continue;}
  const heading=line.match(/^(#{1,6})\s+(.*)$/);if(heading){flush();closeList();const n=heading[1].length;out.push(`<h${n}>${inline(heading[2])}</h${n}>`);continue;}
  if(/^\|/.test(line)){flush();closeList();out.push(`<pre>${escape(line)}</pre>`);continue;}
  const item=line.match(/^\s*[-*]\s+(.*)$/);if(item){flush();if(list!=='ul'){closeList();list='ul';out.push('<ul>');}out.push(`<li>${inline(item[1])}</li>`);continue;}
  paragraph.push(line.trim());
 }
 flush();closeList();return out.join('\n');
}
function walk(from, into){for(const entry of fs.readdirSync(from,{withFileTypes:true})){const file=path.join(from,entry.name);if(entry.isDirectory()){walk(file,into);continue;}if(!entry.name.endsWith('.md'))continue;const relative=path.relative(source,file).replace(/\.md$/,'.html');const target=path.join(output,relative);fs.mkdirSync(path.dirname(target),{recursive:true});const html=render(fs.readFileSync(file,'utf8'));fs.writeFileSync(target,`<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>${escape(path.basename(relative))}</title><body><main>${html}</main></body></html>`);}}
walk(source,output);
