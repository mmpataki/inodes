class posts {

    constructor() {
        this.elems = {}
    }

    getCard(obj) {
        let pre = document.createElement('div');
        pre.classList = "preview"
        showdown.setFlavor('github');
        var converter = new showdown.Converter()
        try {
            obj = JSON.parse(obj.content)
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

    getContent() {
        return {
            title: this.title.value,
            content: this.editor.value
        };
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
}