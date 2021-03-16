class xdocs {

    getCard(obj) {
        obj = JSON.parse(obj.content)
        let template = function(obj) {
            return {
                ele: 'div',
                classList: 'container',
                children: [
                    {
                        ele: 'span',
                        classList: 'name',
                        text: obj.name || "<no name, bug?>"
                    },
                    {
                        ele: 'span',
                        classList: 'version',
                        text: obj.version || "<no version, bug?>"
                    },
                    {
                        ele: 'span',
                        classList: 'description',
                        text: obj.description || "<no description, bug?>"
                    },
                    {
                        ele: 'a',
                        classList: 'url',
                        attribs : {
                            href: obj.url ? `${getBaseUrl()}${obj.url}` : "<no url, bug?>",
                            target: "_blank"
                        },
                        text: obj.url || "<no link, bug?>"
                    }
                ]
            }
        }
        return render('xdocs', template(obj));
    }

    getEditor(obj) {
        let self = this;
        if(obj) {
            obj = JSON.parse(obj.content);
        }
        let renderable = function (obj) {
            return {
                ele: "div",
                children: [
                    {
                        ele: "input",
                        classList: "input",
                        iden: "name",
                        attribs: {
                            placeholder: "name (some identifier)",
                            value: obj ? obj.name : ""
                        }
                    },
                    {
                        ele: "textarea",
                        classList: "textarea",
                        iden: "description",
                        attribs: {
                            placeholder: "describe what this xdoc set is",
                            value: obj ? obj.description : ""
                        }
                    },
                    {
                        ele: "select",
                        classList: "select",
                        iden: "version",
                        children : [
                            {ele: "option", value: "10.2.1", text: "10.2.1"},
                            {ele: "option", value: "10.2.1", text: "10.2.2"},
                            {ele: "option", value: "10.2.1", text: "10.2.2 HF1"},
                            {ele: "option", value: "10.2.1", text: "10.4.0"},
                            {ele: "option", value: "10.2.1", text: "10.4.1"},
                            {ele: "option", value: "10.2.1", text: "10.5.0"},
                        ]
                    },
                    {
                        ele: "form",
                        classList: "fileupload",
                        evnts: {
                            submit: function(e) {
                                e.preventDefault();
                            }
                        },
                        children: [
                            {
                                ele: "input",
                                attribs: {
                                    type: "file",
                                    name: "file"
                                }
                            },
                            {
                                ele: "button",
                                text: "upload",
                                evnts: {
                                    click: function() {
                                        let fd = new FormData(this.parentNode);
                                        postFile(`/files`, fd, {})
                                            .then(e => {self.file = e.response})
                                            .then(e => showSuccess('Uploaded !'))
                                            .catch(e => showError(e.message))
                                    }
                                }
                            }
                        ]
                    }
                ]
            }
        }
        let ele = render('xdocs-ed', renderable(obj), (id, obj) => {
            self[id] = obj
        });
        return ele;
    }

    getContent() {
        return {
            name: this.name.value,
            description: this.description.value,
            url: `/files?file=${this.file}`,
            version: this.version.value
        };
    }

}