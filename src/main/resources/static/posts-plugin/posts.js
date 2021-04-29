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
            obj = JSON.parse(obj.content)
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
            obj = obj ? JSON.parse(obj.content) : obj
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
                                text: 'Content (Markdown + HTML supported)'
                            },
                            {
                                ele: "textarea",
                                classList: "editor",
                                attribs: {
                                    rows: 20,
                                    value: obj ? obj.content : ""
                                },
                                evnts: {
                                    input: function (e) {
                                        self.showPreview(e);
                                    }
                                }
                            },
                            {
                                ele: 'span',
                                text: 'Preview'
                            },
                            {
                                ele: "div",
                                classList: "preview",
                                evnts: {}
                            },
                            {
                                ele: 'div',
                                classList: 'attach-actions',
                                children: [
                                    {
                                        ele: 'button',
                                        text: 'Attach files',
                                        evnts: {
                                            click: function () {
                                                filePicker(self.attachments)
                                                    .then(fileNames => {
                                                        self.attachments = fileNames
                                                        self.showPreview()
                                                    })
                                            }
                                        }
                                    },
                                    {
                                        ele: 'button',
                                        text: 'Add image',
                                        evnts: {
                                            click: function () {
                                                filePicker(self.attachments)
                                                    .then(fileNames => {
                                                        self.insertImgLinks(fileNames)
                                                        self.showPreview()
                                                    })
                                            }
                                        }
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        }
        let ele = render('posts', renderable(obj));
        this.title = ele.getElementsByTagName('input')[0];
        this.editor = ele.getElementsByTagName('textarea')[0];
        this.preview = ele.getElementsByClassName('posts-preview')[0];
        return ele;
    }

    insertImgLinks(urls) {
        function insertAtCursor(myField, myValue) {
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

        urls.forEach(url => {
            insertAtCursor(this.editor, `\n![${url}](${url})\n`)
        })

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
        let attach = "\n\n" + this.attachments.map(file => `<a href='${file}'><img src='/posts-plugin/attach.png'> ${file.replace(/^.*[\\\/]/, '')}</a>`).join('\n')
        this.preview.innerHTML = this.getTitle(this.title.value) + converter.makeHtml(this.editor.value + attach);
        this.preview.querySelectorAll('pre code').forEach((block) => {
            hljs.highlightBlock(block);
        });
    }
}