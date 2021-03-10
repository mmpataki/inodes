class goodkb {

    getCard(obj) {
        obj = JSON.parse(obj.content)
        let template = function(obj) {
            return {
                ele: 'div',
                classList: 'container',
                children: [
                    {
                        ele: 'span',
                        classList: 'title',
                        text: obj.title || "<no title, bug?>"
                    },
                    {
                        ele: 'a',
                        classList: 'url',
                        attribs : {
                            href: obj.url || "<no link, bug?>",
                            target: "_blank"
                        },
                        text: obj.url || "<no link, bug?>"
                    }
                ]
            }
        }
        return render('goodkb', template(obj));
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
                        iden: "title",
                        attribs: {
                            placeholder: "title",
                            value: obj ? obj.title : ""
                        }
                    },
                    {
                        ele: "input",
                        classList: "input",
                        iden: "url",
                        attribs: {
                            placeholder: "url",
                            value: obj ? obj.url : ""
                        }
                    }
                ]
            }
        }
        let ele = render('goodkb-ed', renderable(obj), (id, obj) => {
            self[id] = obj
        });
        return ele;
    }

    getContent() {
        return {
            url: this.url.value,
            title: this.title.value
        };
    }

}