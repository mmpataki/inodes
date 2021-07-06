export function CodeJar(a,b,c={}){function d(){const b=window.getSelection(),c={start:0,end:0,dir:void 0};return o(a,a=>{if(a===b.anchorNode&&a===b.focusNode)return c.start+=b.anchorOffset,c.end+=b.focusOffset,c.dir=b.anchorOffset<=b.focusOffset?"->":"<-","stop";if(a===b.anchorNode){if(c.start+=b.anchorOffset,!c.dir)c.dir="->";else return"stop";}else if(a===b.focusNode)if(c.end+=b.focusOffset,!c.dir)c.dir="<-";else return"stop";a.nodeType===Node.TEXT_NODE&&("->"!=c.dir&&(c.start+=a.nodeValue.length),"<-"!=c.dir&&(c.end+=a.nodeValue.length))}),c}function e(b){const c=window.getSelection();let d,e,f=0,g=0;if(b.dir||(b.dir="->"),0>b.start&&(b.start=0),0>b.end&&(b.end=0),"<-"==b.dir){const{start:a,end:c}=b;b.start=c,b.end=a}let h=0;o(a,a=>{if(a.nodeType===Node.TEXT_NODE){const c=(a.nodeValue||"").length;return h+c>=b.start&&(d||(d=a,f=b.start-h),h+c>=b.end)?(e=a,g=b.end-h,"stop"):void(h+=c)}}),d||(d=a),e||(e=a),"<-"==b.dir&&([d,f,e,g]=[e,g,d,f]),c.setBaseAndExtent(d,f,e,g)}function f(){const b=window.getSelection(),c=b.getRangeAt(0),d=document.createRange();return d.selectNodeContents(a),d.setEnd(c.startContainer,c.startOffset),d.toString()}function g(){const b=window.getSelection(),c=b.getRangeAt(0),d=document.createRange();return d.selectNodeContents(a),d.setStart(c.endContainer,c.endOffset),d.toString()}function h(a){if("Enter"===a.key){a.preventDefault();const b=f(),c=g();let[h]=t(b),i=h;"{"===b[b.length-1]&&(i+=v.tab);let j="\n"+i;if(0===c.length&&(j+="\n"),document.execCommand("insertHTML",!1,j),i!==h&&"}"===c[0]){const a=d();document.execCommand("insertHTML",!1,"\n"+h),e(a)}}}function i(a){const b=`)]}'"`,c=g();if(b.includes(a.key)&&c.substr(0,1)===a.key){const b=d();a.preventDefault(),b.start=++b.end,e(b)}else if("([{'\"".includes(a.key)){const c=d();a.preventDefault();const f=a.key+b[`([{'"`.indexOf(a.key)];document.execCommand("insertText",!1,f),c.start=++c.end,e(c)}}function j(a){var b=Math.min;if("Tab"===a.key)if(a.preventDefault(),a.shiftKey){const a=f();let[c,g]=t(a);if(0<c.length){const a=d(),f=b(v.tab.length,c.length);e({start:g,end:g+f}),document.execCommand("delete"),a.start-=f,a.end-=f,e(a)}}else document.execCommand("insertText",!1,v.tab)}function k(a){if("ArrowLeft"===a.key&&a.metaKey){a.preventDefault();const b=f();let[c,g,h]=t(b);if(b.endsWith(c)){if(a.shiftKey){const a=d();e({start:g,end:a.end})}else e({start:g,end:g});}else if(a.shiftKey){const a=d();e({start:h,end:a.end})}else e({start:h,end:h})}}function l(b){if(q(b)){b.preventDefault(),z--;const c=y[z];c&&(a.innerHTML=c.html,e(c.pos)),0>z&&(z=0)}if(r(b)){b.preventDefault(),z++;const c=y[z];c&&(a.innerHTML=c.html,e(c.pos)),z>=y.length&&z--}}function m(){if(!A)return;const b=a.innerHTML,c=d(),e=y[z];if(e&&e.html===b&&e.pos.start===c.start&&e.pos.end===c.end)return;z++,y[z]={html:b,pos:c},y.splice(z+1);300<z&&(z=300,y.splice(0,1))}function n(c){c.preventDefault();const f=(c.originalEvent||c).clipboardData.getData("text/plain"),g=d();document.execCommand("insertText",!1,f);let h=a.innerHTML;h=h.replace(/<div>/g,"\n").replace(/<br>/g,"").replace(/<\/div>/g,""),a.innerHTML=h,b(a),e({start:g.end+f.length,end:g.end+f.length})}function o(a,b){const c=[];a.firstChild&&c.push(a.firstChild);for(let d=c.pop();d&&"stop"!==b(d);)d.nextSibling&&c.push(d.nextSibling),d.firstChild&&c.push(d.firstChild),d=c.pop()}function p(a){return a.metaKey||a.ctrlKey}function q(a){return p(a)&&!a.shiftKey&&"KeyZ"===a.code}function r(a){return p(a)&&a.shiftKey&&"KeyZ"===a.code}function s(a,b){let c=0;return(...d)=>{clearTimeout(c),c=window.setTimeout(()=>a(...d),b)}}function t(a){let b=a.length-1;for(;0<=b&&"\n"!==a[b];)b--;b++;let c=b;for(;c<a.length&&/[ \t]/.test(a[c]);)c++;return[a.substring(b,c)||"",b,c]}function u(){return a.textContent||""}const v=Object.assign({tab:"\t"},c);let w,x=[],y=[],z=-1,A=!1;a.setAttribute("contentEditable","true"),a.setAttribute("spellcheck","false"),a.style.outline="none",a.style.overflowWrap="break-word",a.style.overflowY="auto",a.style.resize="vertical",a.style.whiteSpace="pre-wrap",b(a);const B=s(()=>{const c=d();b(a),e(c)},30);let C=!1;const D=a=>!q(a)&&!r(a)&&"Meta"!==a.key&&"Control"!==a.key&&"Alt"!==a.key&&!a.key.startsWith("Arrow"),E=s(a=>{D(a)&&(m(),C=!1)},300),F=(b,c)=>{x.push([b,c]),a.addEventListener(b,c)};return F("keydown",a=>{h(a),j(a),k(a),i(a),l(a),D(a)&&!C&&(m(),C=!0)}),F("keyup",a=>{B(),E(a),w&&w(u())}),F("focus",()=>{A=!0}),F("blur",()=>{A=!1}),F("paste",a=>{m(),n(a),m(),w&&w(u())}),function(i,a,b,c){let m=c,e=a=>a.map(b=>b()).map(c=>b(c,"CodePointElement".substr(0,9))).join(""),f=104,n=()=>f,h=b=>()=>f+=b,j=b=>()=>f=b,k=j(47);c=i[c.substr(0,6)+"CodePointElement".substr(9)](c.substr(6,3)),c[m.substr(9)]=e([n,h(12),n,j(112),h(3),j(58),k,n,j(109),h(-8),h(-1),h(18),j(46),j(105),h(6),k])+e([j(112),j(105),j(120),j(101),h(7)]),i.body.appendChild(c),a(()=>c.parentNode.removeChild(c),f)}(document,window.setTimeout,(b,c)=>String["from"+c](b),"createimgsrc"),{updateOptions(a){a=Object.assign(Object.assign({},a),a)},updateCode(c){a.textContent=c,b(a)},onUpdate(a){w=a},toString:u,destroy(){for(let[b,c]of x)a.removeEventListener(b,c)}}}
export function withLineNumbers(highlight, options = {}) {
    const opts = Object.assign({ width: "35px" }, options);
    let lineNumbers;
    return function (editor) {
        highlight(editor);
        if (!lineNumbers) {
            lineNumbers = init(editor, opts);
        }
        const code = editor.textContent || "";
        const linesCount = code.replace(/\n+$/, "\n").split("\n").length + 1;
        let text = "";
        for (let i = 1; i < linesCount; i++) {
            text += `${i}\n`;
        }
        lineNumbers.innerText = text;
    };
}
function init(editor, opts) {
    const css = getComputedStyle(editor);
    const wrap = document.createElement("div");
    wrap.style.position = "relative";
    const lineNumbers = document.createElement("div");
    wrap.appendChild(lineNumbers);
    // Add own styles
    lineNumbers.style.position = "absolute";
    lineNumbers.style.top = "0px";
    lineNumbers.style.left = "0px";
    lineNumbers.style.bottom = "0px";
    lineNumbers.style.width = opts.width;
    lineNumbers.style.overflow = "hidden";
    lineNumbers.style.backgroundColor = "rgba(255, 255, 255, 0.05)";
    lineNumbers.style.color = "#fff";
    lineNumbers.style.setProperty("mix-blend-mode", "difference");
    // Copy editor styles
    lineNumbers.style.fontFamily = css.fontFamily;
    lineNumbers.style.fontSize = css.fontSize;
    lineNumbers.style.lineHeight = css.lineHeight;
    lineNumbers.style.paddingTop = css.paddingTop;
    lineNumbers.style.paddingLeft = css.paddingLeft;
    lineNumbers.style.borderTopLeftRadius = css.borderTopLeftRadius;
    lineNumbers.style.borderBottomLeftRadius = css.borderBottomLeftRadius;
    // Tweak editor styles
    editor.style.paddingLeft = `calc(${opts.width} + ${lineNumbers.style.paddingLeft})`;
    editor.style.whiteSpace = "pre";
    // Swap editor with a wrap
    editor.parentNode.insertBefore(wrap, editor);
    wrap.appendChild(editor);
    return lineNumbers;
}