class instances {
    constructor() {
        this.elems = {}
    }

    getEditor(obj) {
        let self = this;
        let APP_URLS = function (url) {
            return {
                ele: "div",
                classList: "app-urls",
                attribs: {},
                children: [
                    {
                        ele: "input",
                        classList: "app-tag",
                        attribs: {
                            placeholder: "tag / name",
                            width: "100px",
                            value: url ? url.tag : ""
                        }
                    },
                    {
                        ele: "input",
                        classList: "app-url",
                        attribs: {
                            placeholder: "url",
                            value: url ? url.url : ""
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
        }
        let renderable = function (obj) {
            if (obj) {
                obj = JSON.parse(obj.content)
            }
            return {
                ele: "div",
                id: "app-editor",
                attribs: { classList: "pane" },
                children: [
                    {
                        ele: 'h3',
                        text: 'Your dockers'
                    },
                    {
                        ele: 'div',
                        iden: 'suggestions',
                        classList: 'suggestions'
                    },
                    {
                        ele: 'h3',
                        text: 'Manual instance entry'
                    },
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
                                            placeholder: "app username",
                                            value: obj ? obj.appusername : ""
                                        }
                                    },
                                    {
                                        ele: "input",
                                        classList: "app-password",
                                        attribs: {
                                            placeholder: "app password",
                                            value: obj ? obj.apppassword : ""
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
                                            placeholder: "box username",
                                            value: obj ? obj.boxusername : ""
                                        }
                                    },
                                    {
                                        ele: "input",
                                        classList: "box-password",
                                        attribs: {
                                            placeholder: "box password",
                                            value: obj ? obj.boxpassword : ""
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
                                            placeholder: "ip addr",
                                            value: obj ? obj.ipaddr : ""
                                        }
                                    },
                                    {
                                        ele: "input",
                                        classList: "install-loc",
                                        attribs: {
                                            placeholder: "install location",
                                            value: obj ? obj.installloc : ""
                                        }
                                    }
                                ]
                            },
                            {
                                ele: "span",
                                classList: "urls-label",
                                text: "URLs"
                            },
                            ...(obj ? obj.urls.map(u => APP_URLS(u)) : []),
                            {
                                ele: "button",
                                text: "Add URL",
                                classList: "add-url",
                                evnts: {
                                    click: function (e) {
                                        e.target.parentNode.insertBefore(render('apps', APP_URLS()), e.target);
                                    }
                                }
                            }
                        ]
                    }
                ]
            }
        }

        let docker_instance = function (inst) {
            return {
                ele: 'div',
                classList: 'instance',
                children: [
                    {
                        ele: 'span',
                        classList: 'text',
                        text: inst.CONTAINER_ID
                    },
                    {
                        ele: 'span',
                        classList: 'text',
                        text: inst.DESCRIPTION
                    },
                    {
                        ele: 'span',
                        classList: 'text',
                        text: `tag: ${inst.PVERSION}`
                    },
                    {
                        ele: 'span',
                        classList: 'text',
                        text: `Case# : ${inst.CASENUM}`
                    },
                    {
                        ele: 'button',
                        text: 'Import',
                        evnts: {
                            click: function () {
                                console.log('importing ', inst)
                                post(
                                    '/nocors',
                                    {
                                        method: 'GET',
                                        data: JSON.stringify({ 'CONTAINER_ID': inst.CONTAINER_ID }),
                                        headers: { 'Content-Type': 'application/json' },
                                        url: 'http://10.23.32.45/labconsole/api/v1/getdockerMetadata'
                                    },
                                    { 'Content-Type': 'application/json' }
                                ).then(resp => {
                                    resp = JSON.parse(JSON.parse(resp.response).data[0].META_DATA)
                                    let O = {};
                                    Object.keys(resp).forEach(k => {
                                        O[k] = {}
                                        resp[k].forEach(elem => {
                                            O[k][Object.keys(elem)[0]] = elem[Object.keys(elem)[0]]
                                        })
                                    })
                                    console.log(O)
                                    let urls = [];
                                    let meta = [];

                                    Object.keys(O['URL details']).forEach(k => {
                                        if (k.toLowerCase().includes('url')) {
                                            urls.push({ tag: k, url: O['URL details'][k] })
                                        } else {
                                            meta.push({ key: k, value: O['URL details'][k] })
                                        }
                                    })

                                    self.importedContent = {
                                        appusername: O['URL details']['Username'],
                                        apppassword: O['URL details']['Username'],
                                        boxusername: O['Docker Host and credentials']['User'],
                                        boxpassword: O['Docker Host and credentials']['Password'],
                                        ipaddr: O['Docker Host and credentials']['Host name'],
                                        installloc: O['URL details']['EDC installation'],
                                        urls: urls,
                                        meta: meta
                                    }

                                    self.importedTags = [
                                        O['Docker Host and credentials']['Cloud'].toLowerCase(),
                                        O['Docker Host and credentials']['Template'].toLowerCase(),
                                    ];
                                    console.log(self.importedContent)

                                    inodes.post()
                                })
                            }
                        }
                    }
                ]
            }
        }

        let ret = render('apps', renderable(obj), (id, e) => { self.elems[id] = e })

        // get suggestions from dockers list
        post(
            '/nocors',
            {
                method: 'GET',
                url: 'http://iservernext/labconsole/api/v1/getdockers',
                data: JSON.stringify({ email: getCurrentUser() }),
                headers: { 'Content-Type': 'application/json' }
            },
            { 'Content-Type': 'application/json' }
        ).then((resp) => {
            let instances = JSON.parse(resp.response).data
            instances.forEach(inst => {
                self.elems.suggestions.appendChild(render('docker', docker_instance(inst), x => x))
            })
            if (instances.length == 0) {
                self.elems.suggestions.innerHTML += 'Ooops.. looks like you don\'t have any active docker instances.'
            }
        })

        return ret;
    }

    getContent() {
        if (this.importedContent) {
            let x = this.importedContent;
            this.importedContent = undefined;
            return x;
        }
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
        let CARD_META_URL = function (k, v, elem) {
            return {
                ele: "tr",
                classList: "card-metadata",
                children: [
                    {
                        ele: "td",
                        classList: "card-metatag",
                        text: k
                    },
                    {
                        ele: "td",
                        classList: "card-meta",
                        children: [
                            {
                                ele: elem,
                                classList: "card-meta",
                                attribs: {
                                    href: v,
                                    target: "_blank"
                                },
                                text: v
                            }
                        ]
                    }
                ]
            }
        }
        let card_url_template = function (k, v) {
            return {
                ele: 'span',
                classList: 'urltag',
                children: [
                    {
                        ele: 'a',
                        text: k,
                        attribs: { href: v, target: "_blank" },
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
                    classList: 'details',
                    children: [
                        {
                            ele: "div",
                            classList: 'box-creds',
                            children: [
                                {
                                    ele: "span",
                                    classList: "card-boxcreds",
                                    text: "Hostname / IP"
                                },
                                {
                                    ele: "span",
                                    classList: "card-boxusername",
                                    text: `${obj.ipaddr}`
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
                                    text: `${obj.boxusername} / ${obj.boxpassword}`
                                },

                            ]
                        },
                        {
                            ele: "div",
                            classList: 'box-creds',
                            children: [
                                {
                                    ele: "span",
                                    classList: "card-boxcreds",
                                    text: "Installation location"
                                },
                                {
                                    ele: "span",
                                    classList: "card-boxusername",
                                    text: `${obj.installloc}`
                                }
                            ]
                        },
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
                                    text: `${obj.appusername} / ${obj.apppassword}`
                                }
                            ]
                        },
                        {
                            ele: 'a',
                            text: '...',
                            styles: {
                                cursor: 'pointer',
                                display: obj.meta ? 'block' : 'none',
                                'margin-left': '5px'
                            },
                            attribs: {
                                title: 'Other metadata'
                            },
                            evnts: {
                                click: function () {
                                    this.nextSibling.style.display = this.nextSibling.style.display == 'none' ? 'block' : 'none';
                                }
                            }
                        },
                        {
                            ele: "table",
                            classList: "card-metadata-container",
                            styles: { display: 'none' },
                            children: []
                        },
                        {
                            ele: "div",
                            classList: "card-url-container",
                            children: []
                        },
                    ]
                },
                {
                    ele: 'div',
                    classList: "icons",
                    children: [
                        {
                            ele: "a",
                            classList: "card-boxlogin",
                            attribs: {
                                href: `ssh://${obj.boxusername}:${encodeURIComponent(obj.boxpassword)}@${obj.ipaddr}`,
                                innerHTML: `<img alt='SSH' src="./instances-plugin/images/putty.png" style="width: 20px"/>`,
                                title: 'SSH'
                            }
                        },
                        {
                            ele: "a",
                            classList: "card-boxlogin",
                            attribs: {
                                href: `scp://${obj.boxusername}:${encodeURIComponent(obj.boxpassword)}@${obj.ipaddr}`,
                                innerHTML: `<img alt='SCP' src="./instances-plugin/images/winscp.jpg" style="width: 20px"/>`,
                                title: 'SCP'
                            }
                        },
                        {
                            ele: "a",
                            classList: "card-boxlogin",
                            attribs: {
                                href: `ms-rd://full%20address:s:${obj.ipaddr}`,
                                innerHTML: `<img alt='RDP' src="./instances-plugin/images/rdp.png" style="width: 20px"/>`,
                                title: 'RDP'
                            }
                        }
                    ]
                }
            ]
        }
        let start = 6;
        obj.urls.forEach(u => {
            card.children[0].children[start].children.push(card_url_template(u.tag, u.url))
        })
        if (obj.meta) {
            obj.meta.forEach(u => {
                card.children[0].children[start - 1].children.push(CARD_META_URL(u.key, u.value, 'span'))
            })
        }
        return render('instances', card, e => 0);
    }

    getTags() {
        if (this.importedTags) {
            let t = this.importedTags;
            this.importedTags = undefined;
            return t;
        }
        return [];
    }
}