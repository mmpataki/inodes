// with instance search
new function () {
    let self = this;
    let pnode = document.currentScript.parentNode;
    let template = function () {
        return {
            ele: 'div',
            children: [
                {
                    ele: 'fieldset',
                    classList: 'resource-search',
                    children: [
                        {
                            ele: 'legend',
                            text: 'Search resource config'
                        },
                        {
                            ele: 'div',
                            iden: 'instance-searchui',
                            children: [
                                {
                                    ele: 'input',
                                    classList: 'res-search-input',
                                    attribs: {
                                        placeholder: 'search for instance'
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
                                                        {
                                                            ele: 'span',
                                                            text: "<br/>" + JSON.stringify(tags)
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
                        }
                    ]
                },
                {
                    ele: 'fieldset',
                    classList: 'instance-search',
                    children: [
                        {
                            ele: 'legend',
                            text: 'EDC instance details'
                        },
                        {
                            ele: 'div',
                            classList: 'instance-type-selector',
                            children: [
                                {
                                    ele: 'input',
                                    label: 'Search an instance',
                                    attribs: {
                                        type: 'radio',
                                        name: 'instance-type',
                                        checked: true,
                                        id: 'rescreator_instance_type_onboarded'
                                    },
                                    evnts: {
                                        click: function () {
                                            self['instance-searchui'].style.display = "block"
                                            self['instance-inputui'].style.display = "none"
                                        }
                                    }
                                },
                                {
                                    ele: 'input',
                                    label: 'Provide instance details',
                                    attribs: {
                                        type: 'radio',
                                        name: 'instance-type',
                                        id: 'rescreator_instance_type_input'
                                    },
                                    evnts: {
                                        click: function () {
                                            self['instance-inputui'].style.display = "block"
                                            self['instance-searchui'].style.display = "none"
                                        }
                                    }
                                }
                            ]
                        },
                        {
                            ele: 'div',
                            iden: 'instance-searchui',
                            children: [
                                {
                                    ele: 'input',
                                    attribs: {
                                        placeholder: 'search for instance'
                                    },
                                    evnts: {
                                        input: function () {
                                            inodes.search('%edcresconfig ' + this.value).then(d => {
                                                let confs = JSON.parse(d.response);
                                                console.log(confs)
                                            })
                                        }
                                    }
                                },
                                {
                                    ele: 'div',
                                    classList: 'search-results-ui'
                                }
                            ]
                        },
                        {
                            ele: 'div',
                            iden: 'instance-inputui',
                            styles: {
                                display: 'none'
                            },
                            children: [
                                {
                                    ele: 'input',
                                    attribs: {
                                        placeholder: 'url'
                                    }
                                },
                                {
                                    ele: 'input',
                                    attribs: {
                                        placeholder: 'username'
                                    }
                                },
                                {
                                    ele: 'input',
                                    attribs: {
                                        placeholder: 'password'
                                    }
                                },
                                {
                                    ele: 'input',
                                    attribs: {
                                        placeholder: 'resourcename'
                                    }
                                }
                            ]
                        }
                    ]
                },
                {
                    ele: 'button',
                    text: 'Submit',
                    evnts: {
                        click: function() {
                            let resources = [];
                            
                            let chkboxes = self['res-search-results'].getElementsByTagName('input')
                            for (let i = 0; i < chkboxes.length; i++) {
                                resources.push(chkboxes[i].xdata);
                            }

                            let chkboxes = self[]
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