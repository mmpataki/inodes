new function () {
    let ajax = (m, u, d) => {
        return new Promise((r, j) => {
            var x = new XMLHttpRequest();
            x.onreadystatechange = function () {
                if (this.readyState == 4 && this.status == 200) {
                    r(this.responseText)
                }
            };
            x.open(m, `${u}`, true);
            x.send(d);
        });
    }
    let get = (u) => ajax("GET", u)
    let post = (u, d) => ajax('POST', u, JSON.stringify(d))
    let render = function (name, spec, elemCreated, container) {
        let e;
        if (!spec.preBuilt) {
            e = document.createElement(spec.ele);
            spec.iden && elemCreated(spec.iden, e)
            if (spec.text) e.innerHTML = spec.text;
            if (spec.classList) {
                e.classList = `${name}-` + spec.classList.split(/\s+/).join(` ${name}-`)
            }
            spec.attribs && Object.keys(spec.attribs).forEach(key => {
                e[key] = spec.attribs[key]
            })
            spec.styles && Object.keys(spec.styles).forEach(key => {
                e.style[key] = spec.styles[key]
            })
            spec.evnts && Object.keys(spec.evnts).forEach(key => {
                e.addEventListener(key, spec.evnts[key])
            })
            if (spec.children) {
                if (spec.children instanceof Function) {
                    spec.children().map(x => e.appendChild(x))
                }
                else spec.children.forEach(child => render(name, child, elemCreated, e))
            }
        } else {
            e = spec.ele;
        }
        if (container) {
            container.appendChild(e)
            return container;
        }
        return e;
    }
    let pnode = document.currentScript.parentNode;
    get('/edcrescreator/resources.json').then(dd => {
        let data = JSON.parse(dd);
        let self = this;
        let $ = function (x) { return self[x].value; }
        let create_resource = function () {
            let d = { ...data[$('resourceType')][$('resourceSource')] };
            d['resourceIdentifier']['resourceName'] = $('resourceName')
            post(
                `/nocorspost`,
                {
                    url: `${$('url')}/access/1/catalog/resources`,
                    data: d,
                    username: `${$('securitydomain')}\\${$('username')}`,
                    password: $('password')
                }
            ).then(alert('Done'))
        }
        let run_resource = function () {
            create_resource();
            post(
                '/nocorspost',
                {
                    url: `${$('url')}/access/2/catalog/resources/jobs/loads`,
                    data: { resourceName: $('resourceType') },
                    username: `${$('securitydomain')}\\${$('username')}`,
                    password: $('password')
                }
            )
        }
        let template = function (obj) {
            return {
                ele: 'div',
                classList: 'applet-resource-creator',
                children: [
                    {
                        ele: 'select',
                        classList: 'input',
                        iden: 'resourceType',
                        children: function () {
                            return Object.keys(data).map(v => { let x = document.createElement('option'); x.innerText = v; return x; })
                        },
                        evnts: {
                            change: function () {
                                Object.keys(data[self['resourceType'].value]).forEach(k => { self['resourceSource'].innerHTML += `<option>${k}</option>` })
                            }
                        }
                    },
                    {
                        ele: 'select',
                        classList: 'input',
                        iden: 'resourceSource',
                    },
                    {
                        ele: 'input',
                        classList: 'input',
                        iden: 'url',
                        attribs: {
                            placeholder: 'ldm url'
                        }
                    },
                    {
                        ele: 'input',
                        classList: 'input',
                        iden: 'resourceName',
                        attribs: {
                            placeholder: 'resource name'
                        }
                    },
                    {
                        ele: 'input',
                        classList: 'input',
                        iden: 'securitydomain',
                        attribs: {
                            placeholder: 'security domain'
                        }
                    },
                    {
                        ele: 'input',
                        classList: 'input',
                        iden: 'username',
                        attribs: {
                            placeholder: 'username'
                        }
                    },
                    {
                        ele: 'input',
                        classList: 'input',
                        iden: 'password',
                        attribs: {
                            placeholder: 'password'
                        }
                    },
                    {
                        ele: 'div',
                        classList: 'input',
                        children: [
                            {
                                ele: 'button',
                                text: 'Create',
                                evnts: {
                                    click: create_resource
                                }
                            },
                            {
                                ele: 'button',
                                text: 'Run',
                                evnts: {
                                    click: run_resource
                                }
                            }
                        ]
                    }
                ]
            }
        }
        pnode.appendChild(render('applet-resource-creator', template(), (id, elem) => {
            self[id] = elem;
        }));
    })
}();