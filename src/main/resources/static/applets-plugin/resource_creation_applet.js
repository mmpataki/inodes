new function () {
    let self = this;
    let pnode = document.currentScript.parentNode;
    let template = function () {
        return {
            ele: 'div',
            children: [
                {
                    ele: 'div',
                    classList: 'resource-search',
                    children: [
                        {
                            ele: 'h3',
                            text: 'Search resource config'
                        },
                        {
                            ele: 'input',
                            classList: 'res-search-input',
                            attribs: {
                                placeholder: 'search for resource config'
                            },
                            evnts: {
                                input: function () {
                                    let restemplate = function (conf) {
                                        let resInfo = JSON.parse(conf.content);
                                        let name = resInfo.value.resourceIdentifier.resourceName;
                                        let comment = conf.comment
                                        let tags = conf.tags
                                        return {
                                            ele: 'div',
                                            classList: 'container',
                                            children: [
                                                {
                                                    ele: 'input',
                                                    attribs: {
                                                        type: 'checkbox',
                                                        xdata: conf
                                                    },
                                                    styles: {
                                                        float: 'right'
                                                    }
                                                },
                                                {
                                                    ele: 'b',
                                                    text: name + ": "
                                                },
                                                {
                                                    ele: 'span',
                                                    text: comment
                                                },
                                                {ele: 'br'},
                                                {
                                                    ele: 'input',
                                                    attribs: {
                                                        type: 'text',
                                                        placeholder: 'A new resource name'
                                                    }
                                                },
                                                {
                                                    ele: 'div',
                                                    style: {
                                                        padding: '10px'
                                                    },
                                                    attribs: {
                                                        innerHTML: "<br/>" + tags.map(t => `<span class="res-searchresult-tag">${t}</span>`).join(' ')
                                                    }
                                                }
                                            ]
                                        }
                                    }
                                    inodes.search('%edcresconfig ' + this.value).then(d => {
                                        let confs = JSON.parse(d.response).results;
                                        let out = self['res-search-results']
                                        out.innerHTML = ''
                                        confs.forEach(conf => {
                                            out.appendChild(render('rescreator-ressearchresult', restemplate(conf)))
                                        })
                                    })
                                }
                            }
                        },
                        {
                            ele: 'div',
                            iden: 'res-search-results',
                            classList: 'search-results-ui'
                        }
                    ]
                },
                {
                    ele: 'div',
                    classList: 'instance-search',
                    children: [
                        { ele: 'h3', text: 'Search instance' },
                        {
                            ele: 'div',
                            iden: 'instance-searchui',
                            children: [
                                {
                                    ele: 'input',
                                    classList: 'instance-search-input',
                                    attribs: {
                                        placeholder: 'search for instance'
                                    },
                                    evnts: {
                                        input: function () {
                                            let inst_template = function (inst) {
                                                return {
                                                    ele: 'div',
                                                    classList: 'container',
                                                    children: [
                                                        {
                                                            ele: 'input',
                                                            attribs: {
                                                                type: 'checkbox',
                                                                xdata: inst
                                                            },
                                                            styles: {
                                                                float: 'right'
                                                            }
                                                        },
                                                        {
                                                            ele: 'a',
                                                            attribs: {
                                                                href: inst.url
                                                            },
                                                            styles: {
                                                                'text-decoration': 'none'
                                                            },
                                                            text: inst.url
                                                        },
                                                        { ele: 'br' },
                                                        {
                                                            ele: 'span',
                                                            style: {
                                                                display: 'block'
                                                            },
                                                            text: inst.comment || 'A dummy comment for display'
                                                        },
                                                        {
                                                            ele: 'div',
                                                            style: {
                                                                padding: '10px'
                                                            },
                                                            attribs: {
                                                                innerHTML: "<br/>" + inst.tags.map(t => `<span class="inst-searchresult-tag">${t}</span>`).join(' ')
                                                            }
                                                        }
                                                    ]
                                                }
                                            }
                                            inodes.search('%instances ' + this.value).then(d => {
                                                let confs = JSON.parse(d.response).results;
                                                let out = self['instance-search-results']
                                                out.innerHTML = ''
                                                confs.forEach(conf => {
                                                    let instance = JSON.parse(conf.content);
                                                    let inst = {
                                                        url: instance.urls.map(x => [x.tag.toLowerCase(), x]).filter(x => x[0].includes('ldm') || x[0].includes('edc') || x[0].includes('catalog')).map(x => x[1].url)[0],
                                                        tags: conf.tags.concat([conf.owner]),
                                                        comment: instance.comment,
                                                        completeInstance: instance
                                                    }
                                                    if (inst.url) {
                                                        console.log(inst)
                                                        out.appendChild(render('instance-ressearchresult', inst_template(inst)))
                                                    }
                                                })
                                            })
                                        }
                                    }
                                },
                                {
                                    ele: 'div',
                                    iden: 'instance-search-results',
                                    classList: 'search-results-ui'
                                }
                            ]
                        }
                    ]
                },
                {
                    ele: 'button',
                    text: 'Create',
                    classList: 'create-btn',
                    evnts: {
                        click: function () {
                            let create_resource = function (res, inst) {
                                d = { ...res }
                                console.log(d)
                                post(
                                    '/nocors',
                                    {
                                        method: 'POST',
                                        url: `${inst.url}/access/1/catalog/resources`,
                                        data: JSON.stringify(d),
                                        headers: {
                                            'Content-Type': 'application/json',
                                            'Authorization': 'Basic ' + btoa(`${inst.appusername}:${inst.apppassword}`)
                                        }
                                    },
                                    {
                                        'Content-Type': 'application/json',
                                    }
                                ).then(alert('Done'))
                            }
                            let run_resource = function () {
                                create_resource();
                                post(
                                    '/nocors',
                                    {
                                        method: 'POST',
                                        url: `${$('url')}/access/1/catalog/resources`,
                                        data: JSON.stringify({ resourceName: $('resourceName') }),
                                        headers: {
                                            'Content-Type': 'application/json',
                                            'Authorization': 'Basic ' + btoa(`${$('username')}:${$('password')}`)
                                        }
                                    },
                                    {
                                        'Content-Type': 'application/json',
                                    }
                                )
                            }
                            
                            let cb_resources = self['res-search-results'].querySelectorAll('input[type=checkbox]')
                            let resources = []
                            for (let i = 0; i < cb_resources.length; i++) {
                                if(!cb_resources[i].checked) continue

                                let res = JSON.parse(cb_resources[i].xdata.content).value;
                                let newname = cb_resources[i].parentNode.querySelector('input[type=text]').value;
                                console.log(newname);
                                res = {...res};
                                if(newname && newname.trim() != '') {
                                    res.resourceIdentifier.resourceName = newname
                                }
                                res.resourceIdentifier.description = 'Created with &#x2764; by resourcecreator applet'
                                resources.push(res)
                            }

                            let cb_instances = self['instance-search-results'].querySelectorAll('input[type=checkbox]')
                            let instances = []
                            for (let i = 0; i < cb_instances.length; i++) {
                                if(!cb_instances[i].checked) continue
                                let xdata = cb_instances[i].xdata
                                xdata = {
                                    url: xdata.url.replace('/ldmadmin', '').replace('/ldmcatalog', ''),
                                    appusername : xdata.completeInstance.appusername,
                                    apppassword : xdata.completeInstance.apppassword
                                }
                                instances.push(xdata)
                            }

                            console.log(resources, instances)

                            resources.forEach(res => {
                                instances.forEach(inst => {
                                    create_resource(res, inst);
                                })
                            })
                        }
                    }
                }
            ],
        }
    }
    pnode.appendChild(render('rescreator', template(), (k, v) => {
        self[k] = v
    }))
}();