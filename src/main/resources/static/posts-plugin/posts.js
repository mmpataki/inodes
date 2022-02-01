class posts {

    getCard(obj) {
        let content =
            (obj.title ? `## ${obj.title}\n` : "")
            + obj.content
            + "\n\n"
            + (obj.attachments || []).map(file => `<a href='${file}'><i class='fa fa-paperclip'></i> ${file.replace(/^.*[\\\/]/, '')}</a>`).join('\n')
        return render('', makeMarkdownViewer('mdviewer', content))
    }

    getTags() { return [] }

    getEditor(obj) {
        return render('posts', {
            ele: "div", classList: '$p10x0', children: [
                {
                    ele: 'div', styles: { height: '500px', margin: '0px 0px 30px 0px' }, children: [{
                        label: 'Content (markdown supported)', ...makeMarkDownEditor('editor', obj ? obj.content : "", "", files => {
                            obj.attachments = [...new Set(obj.attachments.concat(files))]
                            this.updateAttachmentView(obj.attachments)
                        })
                    }]
                },
                { ele: 'div', iden: 'attachmentsView', classList: 'attachments-view' }
            ]
        }, (id, ele) => this[id] = ele)
    }

    getContent() {
        return { content: this.editor.mdeditor.getMarkdown(), attachments: this.attachments }
    }

    showPreview() {
        showdown.setFlavor('github');
        var converter = new showdown.Converter()
        this.preview.innerHTML = converter.makeHtml(this.editor.value);
        this.preview.querySelectorAll('pre code').forEach((block) => hljs.highlightBlock(block));
    }

    updateAttachmentView(attachments) {
        this.attachmentsView.innerHTML = ''
        attachmentsView.style.display = attachments.length > 0 ? 'block' : 'none'
        render('attachments-view', this.attachments.map(file => ({
            ele: 'div', classList: 'attachment', children: [
                { ele: 'i', attribs: { classList: 'fa fa-paperclip' } },
                { ele: 'a', classList: 'link', attribs: { href: file }, text: file.replace(/^.*[\\\/]/, '') },
                {
                    ele: 'span', classList: 'deletebtn', attribs: { innerHTML: '&#10006;' },
                    evnts: {
                        click: function () {
                            attachments.splice(attachments.indexOf(file), 1);
                            this.parentNode.remove()
                            attachmentsView.style.display = attachments.length > 0 ? 'block' : 'none'
                        }
                    }
                }
            ]
        })), () => 0, this.attachmentsView)
    }

    getCopyContent(obj) {
        return this.getCard(obj).outerHTML + "<br/>";
    }
}
