class posts {

    constructor() {
        this.elems = {}
        this.extensions = [{
            type: 'output',
            regex: new RegExp(`<([A-Za-z]+)>`, 'g'),
            replace: `<$1 class="shdn-$1">`
        }]
        this.attachments = []
    }

    getCard(obj) {
        let pre = document.createElement('div');
        pre.classList = "preview"
        var converter = new showdown.Converter({
            extensions: this.extensions,
            noHeaderId: true // important to add this, else regex match doesn't work
        })
        converter.setFlavor('github');
        try {
            obj.attachments = obj.attachments || []
            obj.content += "\n\n" + obj.attachments.map(file => `<a href='${file}'><img src='/posts-plugin/attach.png'> ${file.replace(/^.*[\\\/]/, '')}</a>`).join('\n')
            pre.innerHTML = this.getTitle(obj.title) + converter.makeHtml(obj.content);
            pre.querySelectorAll('pre code').forEach((block) => {
                hljs.highlightBlock(block);
            });
        } catch (e) { pre.innerHTML = `${JSON.stringify(obj)}`; console.log(obj) }
        return pre;
    }

    getTitle(x) {
        return `<h3 class='posts-card-title'>${x}</h3>`
    }

    getTags() { return [] }

    getEditor(obj) {
        let self = this;
        let renderable = function (obj) {
            self.attachments = obj ? (obj.attachments || []) : []
            return {
                ele: "div",
                attribs: { classList: "pane" },
                children: [
                    {
                        ele: "div",
                        attribs: { classList: "editor-preview" },
                        children: [
                            {
                                ele: 'span',
                                text: 'Title'
                            },
                            {
                                ele: 'input',
                                classList: 'title',
                                iden: 'title',
                                attribs: {
                                    value: obj ? obj.title : ""
                                },
                                evnts: {
                                    input: function (e) {
                                        self.showPreview(e);
                                    }
                                }
                            },
                            {
                                ele: 'span',
                                attribs: {
                                    innerHTML: `Content (<a href='https://guides.github.com/features/mastering-markdown/' target='_blank' title='Markdown tutorial'>Markdown</a> + HTML supported)`
                                }
                            },
                            {
                                ele: 'div',
                                styles: { display: 'flex', maxHeight: '450px'},
                                children: [
                                    {
                                        ele: 'div',
                                        styles: { width: "50%", display: "block", padding: "0px", margin: "0px" },
                                        children: [
                                            {
                                                ele: 'div',
                                                classList: 'editor-controls',
                                                children: [
                                                    {
                                                        ele: 'select',
                                                        attribs: { style: 'vertical-align: text-bottom;' },
                                                        children: [
                                                            { ele: 'option', text: 'Header 1' },
                                                            { ele: 'option', text: 'Header 2' },
                                                            { ele: 'option', text: 'Header 3' },
                                                            { ele: 'option', text: 'Header 4' }
                                                        ],
                                                        evnts: {
                                                            change: function () {
                                                                let V = { "Header 1": '#', "Header 2": '##', "Header 3": '###', "Header 4": '####' }
                                                                self.surround(V[this.value] + " ", " ")
                                                            }
                                                        }
                                                    },
                                                    {
                                                        ele: 'b', text: 'B', classList: 'ctrl txt-ctrl ctrl-bold', attribs: { src: '/posts-plugin/bold.png' },
                                                        evnts: { click: function (e) { self.surround("__") } }
                                                    },
                                                    {
                                                        ele: 'i', text: 'I', classList: 'ctrl txt-ctrl ctrl-italic', attribs: { src: '/posts-plugin/bold.png' },
                                                        evnts: { click: function (e) { self.surround("_") } }
                                                    },
                                                    {
                                                        ele: 'u', text: 'U', classList: 'ctrl txt-ctrl ctrl-underline', attribs: { src: '/posts-plugin/bold.png' },
                                                        evnts: { click: function (e) { self.surround("<u>", "</u>") } }
                                                    },
                                                    {
                                                        ele: 'img',
                                                        classList: 'ctrl img-ctrl',
                                                        attribs: { src: '/posts-plugin/image.png', title: 'Insert images' },
                                                        evnts: {
                                                            click: function () {
                                                                filePicker([], 'Pick an image')
                                                                    .then(fileNames => {
                                                                        self.insertImgLinks(fileNames)
                                                                        self.showPreview()
                                                                    })
                                                            }
                                                        }
                                                    },
                                                    {
                                                        ele: 'img',
                                                        classList: 'ctrl img-ctrl',
                                                        attribs: { src: '/posts-plugin/attach24.png', title: 'Attach files' },
                                                        evnts: {
                                                            click: function () {
                                                                filePicker([], 'Pick a file')
                                                                    .then(fileNames => {
                                                                        self.attachments = [...new Set(self.attachments.concat(fileNames))]
                                                                        self.updateAttachmentView()
                                                                    })
                                                            }
                                                        }
                                                    },
                                                    {
                                                        ele: 'img',
                                                        classList: 'ctrl img-ctrl',
                                                        attribs: { src: '/posts-plugin/code.png', title: 'Insert code snippet' },
                                                        evnts: { click: function (e) { self.surround("```\n", "\n```") } }
                                                    },
                                                    {
                                                        ele: 'img',
                                                        classList: 'ctrl img-ctrl',
                                                        attribs: { src: '/posts-plugin/quote.png', title: 'Insert quote' },
                                                        evnts: { click: function (e) { self.surround("\n> ", " ") } }
                                                    },
                                                    {
                                                        ele: 'img',
                                                        classList: 'ctrl img-ctrl',
                                                        attribs: { src: '/posts-plugin/bullet.png', title: 'List' },
                                                        evnts: { click: function (e) { self.listview("- ") } }
                                                    },
                                                    {
                                                        ele: 'img',
                                                        classList: 'ctrl img-ctrl',
                                                        attribs: { src: '/posts-plugin/numbered-bullet.png', title: 'List' },
                                                        evnts: { click: function (e) { self.listview("1. ") } }
                                                    },
                                                    {
                                                        ele: 'img',
                                                        classList: 'ctrl img-ctrl',
                                                        attribs: { src: '/posts-plugin/table.png', title: 'List' },
                                                        evnts: { click: function (e) { self.insertAtCursor(self.editor, "Col1 | Col2\n-------|--------\n1 | 2\n3 | 4"); self.showPreview() } }
                                                    },
                                                ]
                                            },
                                            {
                                                ele: "textarea",
                                                classList: "editor",
                                                iden: 'editor',
                                                attribs: {
                                                    rows: 25,
                                                    value: obj ? obj.content : ""
                                                },
                                                evnts: { input: () =>self.showPreview() }
                                            }
                                        ]
                                    },
                                    {
                                        ele: 'div',
                                        styles: { width: "50%", display: "block", padding: "0px 5px", margin: "0px", overflow: 'auto', border: 'solid 1px lightgray' },
                                        children: [
                                            {
                                                ele: "div",
                                                classList: "preview",
                                                iden: 'preview',
                                                text: 'Preview'
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                ele: 'div',
                                iden: 'attachmentsView',
                                classList: 'attachments-view'
                            }
                        ]
                    }
                ]
            }
        }
        let ele = render('posts', renderable(obj), (id, ele) => self[id] = ele);
        return ele;
    }

    listview(typ) {
        var sel = this.getInputSelection(this.editor), val = this.editor.value;
        if (sel.start == sel.end) return
        this.editor.value = val.slice(0, sel.start) + "\n" + typ + val.slice(sel.start, sel.end).replaceAll(/(\n[\t ]*)/gi, "$1" + typ) + "\n" + val.slice(sel.end);
        this.showPreview()
    }

    getInputSelection(el) {
        var start = 0, end = 0, normalizedValue, range,
            textInputRange, len, endRange;

        if (typeof el.selectionStart == "number" && typeof el.selectionEnd == "number") {
            start = el.selectionStart;
            end = el.selectionEnd;
        } else {
            range = document.selection.createRange();

            if (range && range.parentElement() == el) {
                len = el.value.length;
                normalizedValue = el.value.replace(/\r\n/g, "\n");

                // Create a working TextRange that lives only in the input
                textInputRange = el.createTextRange();
                textInputRange.moveToBookmark(range.getBookmark());

                // Check if the start and end of the selection are at the very end
                // of the input, since moveStart/moveEnd doesn't return what we want
                // in those cases
                endRange = el.createTextRange();
                endRange.collapse(false);

                if (textInputRange.compareEndPoints("StartToEnd", endRange) > -1) {
                    start = end = len;
                } else {
                    start = -textInputRange.moveStart("character", -len);
                    start += normalizedValue.slice(0, start).split("\n").length - 1;

                    if (textInputRange.compareEndPoints("EndToEnd", endRange) > -1) {
                        end = len;
                    } else {
                        end = -textInputRange.moveEnd("character", -len);
                        end += normalizedValue.slice(0, end).split("\n").length - 1;
                    }
                }
            }
        }

        return {
            start: start,
            end: end
        };
    }

    surround(pref, sufx) {
        sufx = sufx || pref
        var sel = this.getInputSelection(this.editor), val = this.editor.value;
        if (sel.start == sel.end) return
        this.editor.value = val.slice(0, sel.start) + pref + val.slice(sel.start, sel.end) + sufx + val.slice(sel.end);
        this.showPreview()
    }

    insertAtCursor(myField, myValue) {
        //IE support
        if (document.selection) {
            myField.focus();
            sel = document.selection.createRange();
            sel.text = myValue;
        }
        //MOZILLA and others
        else if (myField.selectionStart || myField.selectionStart == '0') {
            var startPos = myField.selectionStart;
            var endPos = myField.selectionEnd;
            myField.value = myField.value.substring(0, startPos)
                + myValue
                + myField.value.substring(endPos, myField.value.length);
        } else {
            myField.value += myValue;
        }
    }

    insertImgLinks(urls) {
        urls.forEach(url => {
            this.insertAtCursor(this.editor, `![${url}](${url})\n`)
        })
        this.showPreview()
    }

    getContent() {
        let ret = {
            title: this.title.value,
            content: this.editor.value,
            attachments: this.attachments
        };
        this.attachments = []
        return ret
    }

    // private
    showPreview() {
        showdown.setFlavor('github');
        var converter = new showdown.Converter()
        this.preview.innerHTML = this.getTitle(this.title.value) + converter.makeHtml(this.editor.value);
        this.preview.querySelectorAll('pre code').forEach((block) => {
            hljs.highlightBlock(block);
        });
    }

    updateAttachmentView() {
        let self = this
        this.attachmentsView.innerHTML = '<b>Attachments</b><br/><br/>  '
        self.attachmentsView.style.display = self.attachments.length > 0 ? 'block' : 'none'
        render('attachments-view', {
            ele: 'div',
            children: this.attachments.map(file => {
                return {
                    ele: 'div',
                    classList: 'attachment',
                    children: [
                        {
                            ele: 'a',
                            classList: 'link',
                            attribs: { href: file },
                            children: [
                                { ele: 'img', attribs: { src: '/posts-plugin/attach.png' } },
                                { ele: 'span', text: file.replace(/^.*[\\\/]/, '') }
                            ]
                        },
                        {
                            ele: 'span',
                            classList: 'deletebtn',
                            attribs: { innerHTML: '&#10006;' },
                            evnts: {
                                click: function () {
                                    self.attachments.splice(self.attachments.indexOf(file), 1);
                                    this.parentNode.remove()
                                    self.attachmentsView.style.display = self.attachments.length > 0 ? 'block' : 'none'
                                }
                            }
                        }
                    ]
                }
            })
        }, () => 0, this.attachmentsView)
    }

    getCopyContent(doc) {
        return this.getCard(doc).outerHTML + "<br/>";
    }
}
