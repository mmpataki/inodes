class applets {
    constructor() {
        this.elems = {}
    }
    getCard(obj) {
        let self = this;
        let template = function (obj) {
            return {
                ele: 'div',
                classList: 'container',
                children: [
                    {
                        ele: 'div',
                        classList: 'card-preview',
                        attribs: {
                            innerHTML: obj.html,
                        }
                    },
                    {
                        ele: 'script',
                        attribs: {
                            innerHTML: self.getScript(obj.js)
                        }
                    }
                ]
            }
        }
        return render('applet', template(obj));
    }

    getSafeCard() {
        return render('applet', { ele: 'div', text: "This content needs review, please click 'edit' and review the content" }, (id, e) => self[id] = e)
    }

    getScript(js) {
        return `
        (function() {
            let title = document.createElement('a')
            title.innerHTML = "show console"
            title.classList = 'app-console-title'
            title.addEventListener('click', function () {
                condiv.style.display = condiv.style.display != 'block' ? 'block' : 'none'
                title.innerHTML = title.innerHTML.includes('show') ? 'hide console' : 'show console'
            })
            let condiv = document.createElement('pre')
            condiv.classList = 'app-console'
            document.currentScript.parentNode.parentNode.appendChild(title)
            document.currentScript.parentNode.parentNode.appendChild(condiv)
            let console = {
                log : function(val) {
                    title.style.display = 'block'
                    if (!(typeof val === 'string' || val instanceof String)) {
                        val = JSON.stringify(val, null, 4)
                    }
                    condiv.innerText += val + '\\n'
                }
            }
            let _xxxxx___ = function() {
                ${js}
            }
            _xxxxx___();
        })();`
    }

    getEditor(obj) {
        return render('applet', {
            ele: "div", classList: "$p10x0", children: [
                { ele: "input", attribs: { type: "radio", name: "applet-view-type", checked: true }, classList: "chtml", postlabel: "html" },
                { ele: "input", attribs: { type: "radio", name: "applet-view-type" }, classList: "cjs", postlabel: "javascript" },
                { ele: "input", attribs: { type: "radio", name: "applet-view-type" }, classList: "preview", postlabel: "preview", evnts: { change: _ => this.showPreview() } },
                makeCodeEditor('htmleditor', 'html', obj ? obj.html : "", 'ehtml'),
                makeCodeEditor('jseditor', 'js', obj ? obj.js : "", 'ejs'),
                { ele: "div", iden: 'preview', classList: "epreview" }
            ]
        }, (id, ele) => this[id] = ele)
    }

    getContent() {
        return { html: this.htmleditor.ceditor.toString(), js: this.jseditor.ceditor.toString() };
    }

    showPreview() {
        this.preview.innerHTML = this.htmleditor.ceditor.toString();
        let scrpt = document.createElement('script');
        scrpt.innerHTML = this.getScript(this.jseditor.ceditor.toString());
        this.preview.appendChild(scrpt)
    }
}