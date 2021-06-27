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

    getSafeCard(obj) {
        let self = this;
        let template = function (obj) {
            return {
                ele: 'div',
                classList: 'container',
                children: [
                    { ele: 'h4', text: 'HTML'},
                    {
                        ele: 'pre',
                        iden: 'jscode',
                        classList: 'language-html',
                        styles: { overflow: 'auto', padding: '10px', border: 'solid 1px gray'},
                        attribs: {
                            innerText: html_beautify(obj.html)
                        }
                    },
                    { ele: 'h4', text: 'Javascript'},
                    {
                        ele: 'pre',
                        iden: 'jscode',
                        classList: 'language-js',
                        styles: { overflow: 'auto', padding: '10px', border: 'solid 1px gray'},
                        attribs: {
                            innerText: js_beautify(obj.js)
                        }
                    }
                ]
            }
        }
        let x = render('applet', template(obj), (id, e) => self[id] = e);
        hljs.highlightBlock(this.jscode);
        return x;
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
        let self = this;
        let renderable = function (obj) {
            return {
                ele: "div",
                classList: "pane",
                children: [
                    {
                        ele: "div",
                        classList: "editor-toggler",
                        children: [
                            {
                                ele: "span",
                                classList: "toggle-btn",
                                text: "HTML",
                                evnts: { click: () => self.toggleView('htmleditor') }
                            },
                            {
                                ele: "span",
                                classList: "toggle-btn",
                                text: "Javascript",
                                evnts: { click: () => self.toggleView('jseditor') }
                            },
                            {
                                ele: "span",
                                classList: "toggle-btn",
                                text: "Preview",
                                evnts: {
                                    click: function (e) {
                                        self.showPreview(e);
                                    }
                                }
                            }
                        ]
                    },
                    {
                        ele: "div",
                        classList: "editor-preview",
                        children: [
                            {
                                ele: "textarea",
                                classList: "editor",
                                iden: 'htmleditor',
                                attribs: {
                                    rows: 25,
                                    value: obj ? obj.html : "",
                                    placeholder: "HTML"
                                },
                                evnts: {}
                            },
                            {
                                ele: "textarea",
                                classList: "editor",
                                iden: 'jseditor',
                                attribs: {
                                    rows: 25,
                                    style: 'display: none',
                                    value: obj ? obj.js : "",
                                    placeholder: "Javascript"
                                },
                                evnts: {}
                            },
                            {
                                ele: "div",
                                iden: 'preview',
                                classList: "preview",
                                evnts: {}
                            }
                        ]
                    }
                ]
            }
        }
        let ele = render('applet', renderable(obj), (id, ele) => this[id] = ele);
        return ele;
    }

    getContent() {
        return {
            html: this.htmleditor.value,
            js: this.jseditor.value
        };
    }

    // private
    showPreview() {
        this.toggleView('preview')
        this.preview.innerHTML = this.htmleditor.value;
        let scrpt = document.createElement('script');
        scrpt.innerHTML = this.getScript(this.jseditor.value);
        this.preview.appendChild(scrpt)
    }

    toggleView(id) {
        ['htmleditor', 'jseditor', 'preview'].forEach(x => this[x].style.display = 'none')
        this[id].style.display = "block"
    }
}