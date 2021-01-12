class instances {
    constructor() {
        this.elems = {}
    }

    getEditor() {
        let self = this;
        this.APP_URLS = {
            ele: "div",
            classList: "app-urls",
            attribs: {},
            children: [
                {
                    ele: "input",
                    classList: "app-tag",
                    attribs: {
                        placeholder: "tag / name",
                        width: "100px"
                    }
                },
                {
                    ele: "input",
                    classList: "app-url",
                    attribs: {
                        placeholder: "url"
                    }
                },
                {
                    ele: "button",
                    classList: "remove-url",
                    text: "-",
                    evnts: {
                        click: function (e) {
                            e.target.parentNode.remove()
                        }
                    }
                }
            ]
        }
        let renderable = {
            ele: "div",
            id: "app-editor",
            attribs: { classList: "pane" },
            children: [
                {
                    ele: "div",
                    children: [
                        {
                            ele: "div",
                            children: [
                                {
                                    ele: "input",
                                    classList: "app-user",
                                    attribs: {
                                        placeholder: "app username"
                                    }
                                },
                                {
                                    ele: "input",
                                    classList: "app-password",
                                    attribs: {
                                        placeholder: "app password"
                                    }
                                }
                            ]
                        },
                        {
                            ele: "div",
                            children: [
                                {
                                    ele: "input",
                                    classList: "box-user",
                                    attribs: {
                                        placeholder: "box username"
                                    }
                                },
                                {
                                    ele: "input",
                                    classList: "box-password",
                                    attribs: {
                                        placeholder: "box password"
                                    }
                                }
                            ]
                        },
                        {
                            ele: "div",
                            children: [
                                {
                                    ele: "input",
                                    classList: "ip-addr",
                                    attribs: {
                                        placeholder: "ip addr"
                                    }
                                },
                                {
                                    ele: "input",
                                    classList: "install-loc",
                                    attribs: {
                                        placeholder: "install location"
                                    }
                                }
                            ]
                        },
                        {
                            ele: "span",
                            classList: "urls-label",
                            text: "URLs"
                        },
                        this.APP_URLS,
                        {
                            ele: "button",
                            text: "Add URL",
                            classList: "add-url",
                            evnts: {
                                click: function (e) {
                                    e.target.parentNode.insertBefore(render('apps', self.APP_URLS), e.target);
                                }
                            }
                        }
                    ]
                }
            ]
        }

        return render('apps', renderable, (id, e) => { self.elems[id] = e })
    }

    getContent() {
        let inputs = this.elems['app-editor'].getElementsByTagName('input');
        let vals = [];
        for (let i = 0; i < inputs.length; i++) {
            vals.push(inputs[i].value);
        }
        let ret = {
            appusername: vals[0],
            apppassword: vals[1],
            boxusername: vals[2],
            boxpassword: vals[3],
            ipaddr: vals[4],
            installloc: vals[5],
            urls: []
        };
        for (let i = 6; i < vals.length; i += 2) {
            ret.urls.push({ tag: vals[i], url: vals[i + 1] })
        }
        return ret;
    }

    getCard(obj) {
        obj = JSON.parse(obj.content)
        let CARD_APP_URL = function (e) {
            return {
                ele: "tr",
                classList: "card-urls",
                children: [
                    {
                        ele: "td",
                        classList: "card-urltag",
                        text: e.tag
                    },
                    {
                        ele: "td",
                        classList: "card-url",
                        children: [
                            {
                                ele: "a",
                                classList: "card-url",
                                attribs: {
                                    href: e.url,
                                    target: "_blank"
                                },
                                text: e.url
                            }
                        ]
                    }
                ]
            }
        }
        let card = {
            ele: "div",
            classList: "card",
            children: [
                {
                    ele: "div",
                    classList: "app-creds",
                    children: [
                        {
                            ele: "span",
                            classList: "card-appcreds",
                            text: "App credentials"
                        },
                        {
                            ele: "span",
                            classList: "card-appusername",
                            text: obj.appusername
                        },
                        {
                            ele: "span",
                            text: "/"
                        },
                        {
                            ele: "span",
                            classList: "card-apppassword",
                            text: obj.apppassword
                        }
                    ]
                },
                {
                    ele: "div",
                    classList: "box-creds",
                    children: [
                        {
                            ele: "span",
                            classList: "card-boxcreds",
                            text: "Box credentials"
                        },
                        {
                            ele: "span",
                            classList: "card-boxusername",
                            text: obj.boxusername
                        },
                        {
                            ele: "span",
                            text: "/"
                        },
                        {
                            ele: "span",
                            classList: "card-boxpassword",
                            text: obj.boxpassword
                        },
                        {
                            ele: "a",
                            classList: "card-boxlogin",
                            attribs: {
                                href: `ssh://${obj.boxusername}:${encodeURIComponent(obj.boxpassword)}@${obj.ipaddr}`
                            },
                            text: "Login"
                        }
                    ]
                },
                {
                    ele: "table",
                    classList: "card-urls-container",
                    children: []
                }
            ]
        }
        obj.urls.forEach(u => {
            card.children[2].children.push(CARD_APP_URL(u))
        })
        return render('apps', card, z => 0);
    }
}