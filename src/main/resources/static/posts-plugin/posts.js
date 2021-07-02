class posts {

    constructor() {
        this.elems = {}
        this.attachments = []
    }

    getCard(obj) {
        let content =
            (obj.title ? `# ${obj.title}\n` : "")
            + obj.content
            + "\n\n"
            + (obj.attachments || []).map(file => `<a href='${file}'><i class='fa fa-paperclip'></i> ${file.replace(/^.*[\\\/]/, '')}</a>`).join('\n')
        return render('', makeMarkdownViewer('mdviewer', content))
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
                                ele: 'div',
                                styles: { height: '450px' },
                                children: [
                                    makeMarkDownEditor('editor', obj ? obj.content : "", "", files => {
                                        self.attachments = [...new Set(self.attachments.concat(files))]
                                        self.updateAttachmentView()
                                    })
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

    getContent() {
        let ret = {
            content: this.editor.mdeditor.getMarkdown(),
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
                                { ele: 'i', attribs: { classList: 'fa fa-paperclip' } },
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
