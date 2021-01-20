class edcresconfig {
    constructor() {
        this.elems = {}
    }
    getCard(obj) {
        obj = JSON.parse(obj.content).value
        let getScannerConfig = function(sc) {
            let out = {
                ele: 'table',
                classList: 'tree-item',
                children: []
            }
            sc.configOptions.forEach(c => {
                out.children.push({
                    ele: 'tr',
                    children: [
                        { ele: 'td', children: [{ ele: 'i', text: c.optionId}]},
                        { ele: 'td', children: [{ ele: 'b', text: c.optionValues.join(',')}]}
                    ]
                })
            })
            return out;
        }
        let getScannerConfigs = function(confs) {
            let out = [];
            confs.forEach(x => out.push({
                ele: 'div',
                classList : `${x.enabled ? "scanner-enabled" : "scanner-disabled"}`,
                children: [
                    {
                        ele: 'div',
                        classList: 'tree-item',
                        children: [
                            {
                                ele: 'b',
                                text: `${x.scanner.providerTypeName} (${x.scanner.providerTypeId})`
                            }
                        ]
                    },
                    {
                        ele: 'div',
                        classList: 'tree-item',
                        children: [
                            getScannerConfig(x)
                        ]
                    }
                ]
            }));
            return out;
        }
        let template = function(res) {
            console.log(res)
            return {
                ele: 'div',
                classList: 'container',
                children: [
                    {
                        ele: 'div',
                        classList: 'res-identifier',
                        children: [
                            {
                                ele: 'div',
                                styles: { float: "left", fontWeight: "bold", fontSize: "1.2em"},
                                text: `${res.resourceIdentifier.resourceName}`
                            },
                            {
                                ele: 'div',
                                styles: { float: "right"},
                                text: `${res.resourceIdentifier.resourceTypeName} (${res.resourceIdentifier.resourceTypeId})`
                            },
                            { ele: 'br'},
                            {
                                ele: 'p',
                                text: `${res.resourceIdentifier.description}`
                            }
                        ]
                    },
                    {
                        ele: 'div',
                        classList: 'tree-item',
                        children: [
                            ...getScannerConfigs(res.scannerConfigurations)
                        ]
                    }
                ]
            }
        }
        return render('edcresconfig', template(obj));
    }

    getEditor(obj) {
        let self = this;
        if(obj) {
            obj = JSON.parse(obj.content);
        }
        let renderable = function (obj) {
            let c = obj ? obj.config : undefined;
            return {
                ele: "div",
                children: [
                    {
                        ele: "input",
                        classList: "input",
                        iden: "url",
                        attribs: {
                            placeholder: "edc url",
                            value: c ? c.url : ""
                        }
                    },
                    {
                        ele: "input",
                        classList: "input",
                        iden: "user",
                        attribs: {
                            placeholder: "username",
                            value: c ? c.username : ""
                        }
                    },
                    {
                        ele: "input",
                        classList: "input",
                        iden: "password",
                        attribs: {
                            placeholder: "password",
                            value: c ? c.password : ""
                        },
                    },
                    {
                        ele: "input",
                        classList: "input",
                        iden: "resourcename",
                        attribs: {
                            placeholder: "resource name",
                            value: c ? c.resourcename : ""
                        },
                    },
                    {
                        ele: 'textarea',
                        iden: 'comment',
                        classList: 'comment',
                        attribs: {
                            placeholder: "comment",
                            value: c ? (c.comment ? c.comment : "") : ""
                        },
                    },
                    {
                        ele: "button",
                        classList: "button",
                        text: "Get resource config",
                        evnts: {
                            click: () => {
                                self.loadResource().then(
                                    x => {
                                        self.value = x
                                        self['rcpreview'].appendChild(
                                            self.getCard(
                                                {
                                                    content: JSON.stringify(
                                                        {
                                                            value: x
                                                        }
                                                    )
                                                }
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    },
                    {
                        ele: 'div',
                        iden: 'rcpreview'
                    }
                ]
            }
        }
        let ele = render('edcresconfig', renderable(obj), (id, obj) => {
            self[id] = obj
        });
        this.editor = ele.getElementsByTagName('textarea')[0];
        this.scripteditor = ele.getElementsByTagName('textarea')[1];
        this.preview = ele.getElementsByClassName('applet-preview')[0];
        return ele;
    }

    loadResource() {
        let v = (x) => this[x].value
        return new Promise((s, f) => {
            post(
                `/nocors`,
                {
                    method: "GET",
                    headers: {
                        Authorization: "Basic " + btoa(v('user') + ":" + v('password'))
                    },
                    url: `${v('url')}/access/1/catalog/resources/${v('resourcename')}?sensitiveOptions=true`
                },
                {
                    "Content-Type": "application/json"
                }
            ).then(x => s(JSON.parse(x.response)))
             .catch(x => f(x))
        })
    }

    getContent() {
        if(!this.value) {
            alert('Get the resource config first')
            return;
        }
        let self = this
        let v = (x) => self[x].value
        return {
            value: self.value,
            config : {
                url : v('url'),
                username: v('user'),
                password: v('password'),
                resourcename: v('resourcename'),
                comment : v('comment')
            },
        };
    }

}