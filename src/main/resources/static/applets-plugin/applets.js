class applets {
    constructor() {
        this.elems = {}
    }
    getCard(obj) {
        let template = function(obj) {
            obj = JSON.parse(obj.content);
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
                            innerHTML: `
                                (function() {
                                    let condiv = document.createElement('pre')
                                    condiv.classList = 'app-console'
                                    document.currentScript.parentNode.parentNode.appendChild(condiv)
                                    let console = {
                                        log : function(val) {
                                            if (!(typeof val === 'string' || val instanceof String)) {
                                                val = JSON.stringify(val, null, 4)
                                            }
                                            condiv.innerText += val + '\\n'
                                        }
                                    }
                                    let _xxxxx___ = function() {
                                        ${obj.js}
                                    }
                                    _xxxxx___();
                                })();
                            `
                        }
                    }
                ]
            }
        }
        return render('applet', template(obj));
    }

    getEditor(obj) {
        let self = this;
        if(obj) {
            obj = JSON.parse(obj.content);
        }
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
                                text: "Edit",
                                evnts: {
                                    click: function (e) {
                                        self.showEditor(e);
                                    }
                                }
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
                                attribs: {
                                    rows: 20,
                                    value: obj ? obj.html : "",
                                    placeholder: "HTML"
                                },
                                evnts: {}
                            },
                            {
                                ele: "textarea",
                                classList: "editor",
                                attribs: {
                                    rows: 20,
                                    value: obj ? obj.js : "",
                                    placeholder: "Javascript"
                                },
                                evnts: {}
                            },
                            {
                                ele: "div",
                                classList: "preview",
                                evnts: {}
                            }
                        ]
                    }
                ]
            }
        }
        let ele = render('applet', renderable(obj));
        this.editor = ele.getElementsByTagName('textarea')[0];
        this.scripteditor = ele.getElementsByTagName('textarea')[1];
        this.preview = ele.getElementsByClassName('applet-preview')[0];
        return ele;
    }

    getContent() {
        return {
            html: this.editor.value,
            js: this.scripteditor.value
        };
    }

    // private
    showPreview() {
        this.editor.style.display = "none"
        this.scripteditor.style.display = "none"
        this.preview.style.display = "block"
        this.preview.innerHTML =  this.editor.value;
        let scrpt = document.createElement('script');
        scrpt.innerHTML = this.scripteditor.value;
        this.preview.appendChild(scrpt)
    }
    showEditor() {
        this.editor.style.display = "block"
        this.scripteditor.style.display = "block"
        this.preview.style.display = "none"
    }
}