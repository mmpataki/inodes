(function () {

    inodes.lock()

    let A = getUApi();
    let inst = Math.random();

    function makeSearchAndSelectButton(text, itemType, resultTemplate, valuePickedCallback, obj) {
        return new _makeSearchAndSelectButton(text, itemType, resultTemplate, valuePickedCallback, obj)
    }

    function _makeSearchAndSelectButton(text, itemType, resultTemplate, valuePickedCallback, obj) {
        return render('s-and-s-btn', {
            ele: 'div',
            iden: 'container',
            classList: 'container',
            children: [
                {
                    ele: 'div',
                    classList: 'content-and-actions',
                    children: [
                        {
                            ele: 'div',
                            classList: 'content',
                            children: [
                                {
                                    ele: 'div',
                                    iden: 'pickPrompt',
                                    styles: { display: obj ? 'none' : 'block' },
                                    children: [
                                        { ele: 'span', text: 'Pick a ' },
                                        {
                                            ele: 'span', classList: 'lnk', text: text,
                                            evnts: {
                                                click: () => {
                                                    this.searchpane.style.display = 'block'
                                                    this.pickPrompt.style.display = 'none'
                                                }
                                            }
                                        }
                                    ]
                                },
                                {
                                    ele: 'div',
                                    iden: 'searchpane',
                                    classList: 'searchpane',
                                    children: [
                                        {
                                            ele: 'input', classList: 'search-box', label: `Search ${itemType}`,
                                            evnts: {
                                                input: (e) => {
                                                    inodes.search(`%${itemType} ${e.target.value}`)
                                                        .then(resp => JSON.parse(resp.response))
                                                        .then(res => {
                                                            this.searchResults.innerHTML = ""
                                                            res.results.forEach(item => {
                                                                render('s-and-s-btn-search-result', {
                                                                    ele: 'div',
                                                                    classList: 'container',
                                                                    children: [{ ele: resultTemplate(item), preBuilt: true }],
                                                                    evnts: {
                                                                        dblclick: () => {
                                                                            valuePickedCallback && valuePickedCallback(item)
                                                                            this.container.data = item;
                                                                            this.searchpane.style.display = 'none';
                                                                            this.pickedItem.style.display = 'block';
                                                                            this.actions.style.display = 'flex'
                                                                            this.pickedItem.innerHTML = ''
                                                                            render('picked-item', { ele: resultTemplate(item), preBuilt: true }, () => 1, this.pickedItem)
                                                                        }
                                                                    }
                                                                }, () => 1, this.searchResults)
                                                            })
                                                        })
                                                }
                                            }
                                        },
                                        { ele: 'div', iden: 'searchResults', classList: 'search-results' }
                                    ]
                                },
                                {
                                    ele: 'div',
                                    classList: 'picked-item',
                                    iden: 'pickedItem',
                                    children: !obj ? [] : [{ ele: resultTemplate(obj), preBuilt: true }]
                                }
                            ]
                        },
                        {
                            ele: 'div',
                            iden: 'actions',
                            classList: 'actions',
                            styles: { display: obj ? 'flex' : 'none' },
                            children: [
                                {
                                    ele: 'span',
                                    classList: 'action',
                                    attribs: { innerHTML: 'reset', title: 'reset this unit' },
                                    evnts: {
                                        click: () => {
                                            this.container.setAttribute('data', undefined);
                                            valuePickedCallback && valuePickedCallback(undefined)
                                            this.pickPrompt.style.display = 'block'
                                            this.pickedItem.style.display = 'none'
                                            this.actions.style.display = 'none'
                                        }
                                    }
                                }
                            ]
                        },
                    ]
                }
            ]
        }, (id, el) => this[id] = el)
    }

    class WorkflowBuilder {

        constructor(ele, wfm) {
            this.wfm = wfm;
            this.tasks = []
            render('wfbldr', {
                ele: 'div', children: [
                    {
                        ele: 'div', children: [
                            { ele: 'span', iden: 'messages' }
                        ]
                    },
                    {
                        ele: 'div', iden: 'wfEditor', classList: 'wf-editor', children: [
                            { ele: 'div', iden: 'graph', classList: 'graph', styles: { display: 'flex' } },
                            { ele: 'div', classList: 'propseditor', iden: 'propsEle' }
                        ]
                    }
                ]
            }, (id, e) => this[id] = e, ele)
            this.draw()
        }

        getTaskItem(task) {
            let self = this;
            let autoTemplate = (item) => {
                item = JSON.parse(item.content)
                return render('automation-sr', {
                    ele: 'div',
                    classList: 'container',
                    children: [
                        { ele: 'b', text: `${item.name}: ` },
                        { ele: 'i', text: item.description }
                    ],
                    attribs: { data: item }
                })
            }
            return render('wfbldr', {
                ele: 'div',
                classList: 'task-container',
                children: [
                    {
                        ele: 'div',
                        classList: 'task-actions',
                        children: [
                            { ele: 'span', attribs: { innerHTML: '&plus;' }, classList: 'tbbtn', evnts: { click: (e) => this.addTask('up', task) } },
                            { ele: 'span', attribs: { innerHTML: '&#9650;' }, classList: 'tbbtn', evnts: { click: (e) => this.moveTask('up', task) } }
                        ]
                    },
                    {
                        ele: 'div',
                        classList: 'task-and-actions',
                        styles: { display: 'flex', 'align-items': 'center' },
                        children: [
                            {
                                preBuilt: true,
                                ele: makeSearchAndSelectButton('automation task', 'automation', autoTemplate, (v) => { task.automationSpec = v; this.showProps(task) }, task.automationSpec),
                                children: [
                                    { ele: 'input', attribs: { type: 'radio', name: `automation-selected-inst-${inst}` }, styles: { display: 'none' } }
                                ],
                                evnts: {
                                    click: function () {
                                        if (!task.automationSpec) return
                                        let last = document.querySelector('.s-and-s-btn-container > input[type=radio]:checked');
                                        if (last) last.parentNode.classList.remove('automation-sr-container-selected');
                                        let cb = this.querySelector('input[type=radio]')
                                        cb.checked = true;
                                        this.classList.add('automation-sr-container-selected')
                                        self.showProps(task);
                                    }
                                }
                            },
                            {
                                ele: 'div',
                                classList: 'task-actions',
                                children: [
                                    { ele: 'span', attribs: { innerHTML: '&#x2716;', title: 'remove this task' }, classList: 'tbbtn', evnts: { click: () => this.removeTask(task) } }
                                ]
                            }
                        ]
                    },
                    {
                        ele: 'div',
                        classList: 'task-actions',
                        children: [
                            { ele: 'span', attribs: { innerHTML: '&plus;' }, classList: 'tbbtn', evnts: { click: (e) => this.addTask('down', task) } },
                            { ele: 'span', attribs: { innerHTML: '&#9660;' }, classList: 'tbbtn', evnts: { click: (e) => this.moveTask('down', task) } }
                        ]
                    }
                ]
            })
        }

        showProps(task) {
            if (!task.propsManager) {
                task.propsManager = new PropsManager(this.propsEle, JSON.parse(task.automationSpec.content), undefined, (key, value) => {
                    this.updateTitle(task)
                })
            }
            task.propsManager.showProps()
        }

        updateTitle(task) {
            let elem;
            if (!task || !task.uiElem || !(elem = task.uiElem.querySelector('.s-and-s-btn-picked-item > .automation-sr-container'))) return;
            let d = this.getTaskTitle(task);
            if (d) elem.innerHTML = d;
        }

        getTaskTitle(task) {
            if (!task.automationSpec || !task.propsManager) return;
            let automation = JSON.parse(task.automationSpec.content);
            let values = task.propsManager.getValues();
            if (automation.postInputTitleTemplate && values) {
                task.displayName = this.templatize(automation.postInputTitleTemplate, this.normalizeDocsFromInputs(values))
            } else {
                task.displayName = `<b>${task.automationSpec.name}</b> <i>${task.automationSpec.description}</i>`;
            }
            return task.displayName;
        }

        array_move(arr, old_index, new_index) {
            if (new_index >= arr.length) {
                var k = new_index - arr.length + 1;
                while (k--) {
                    arr.push(undefined);
                }
            }
            arr.splice(new_index, 0, arr.splice(old_index, 1)[0]);
            return arr; // for testing
        }

        moveTask(direction, task) {
            let index = this.tasks.indexOf(task)
            if (index == -1) return;
            if (direction == 'up' && index != 0)
                this.array_move(this.tasks, index, index - 1)
            else if (direction == 'down' && index != this.tasks.length - 1)
                this.array_move(this.tasks, index, index + 1)
            this.draw()
        }

        addTask(direction, task) {
            let index = this.tasks.indexOf(task)
            if (index == -1) return;
            this.tasks.push(this.newTask());
            if (direction == 'up')
                this.array_move(this.tasks, this.tasks.length - 1, index)
            else if (direction == 'down' && index != this.tasks.length - 2)
                this.array_move(this.tasks, this.tasks.length - 1, index + 1)
            this.draw()
        }

        removeTask(task) {
            const index = this.tasks.indexOf(task);
            if (index > -1) {
                this.tasks.splice(index, 1);
            }
            this.draw()
        }

        newTask() { return { automationSpec: undefined } }

        reset() {
            this.draw();
            this.messages.innerHTML = ''
        }

        runWf() {

            this.reset()

            let wf = { tasks: [] };

            for (var i = 0; i < this.tasks.length; i++) {
                let task = this.tasks[i];
                if (!task.propsManager) {
                    this.highLightUninitializedTask(task)
                } else {
                    if (task.propsManager.initialized()) {
                        wf.tasks.push({ ...task, inputs: task.propsManager.getValues() })
                    } else {
                        this.highLightUninitializedTask(task)
                    }
                }
            }

            let runnableWf = this.resolve(wf)
            console.log(runnableWf)

            post(
                '/nocors',
                {
                    method: 'POST',
                    url: `http://inedctst01:5000/wf/${getCurrentUser()}`,
                    data: JSON.stringify(runnableWf),
                    headers: { 'Content-Type': 'application/json' }
                },
                { 'Content-Type': 'application/json' }
            ).then(() => this.wfm.listJobs())
        }

        templatize(____scr, ____varMap) {
            let str = `
                (
                    function() {
                        ${Object.keys(____varMap).map(key => `let ${key} = ____varMap['${key}']`).join(';\n')}
                        return \`${____scr}\`
                    }
                )()
            `
            try { return eval(str) } catch (e) { return undefined }
        }

        normalizeDocsFromInputs(inputs) {
            let inps = {}
            Object.keys(inputs).forEach(key => {
                let x = inputs[key]
                inps[key] = (typeof x === 'object' && x !== null) ? JSON.parse(inputs[key].content) : inputs[key]
            })
            return inps;
        }

        resolve(wf) {

            let resolvers = {
                'shellscript': (spec, inputs) => {
                    return { script: this.templatize(spec.script, this.normalizeDocsFromInputs(inputs)) }
                }
            }
            return {
                tasks: wf.tasks.map(task => {
                    let spec = JSON.parse(task.automationSpec.content)
                    return {
                        displayName: task.displayName,
                        automationSpec: spec,
                        inputs: task.inputs,
                        ...resolvers[spec.type](spec, task.inputs)
                    }
                })
            }
        }

        err(msg) {
            this.messages.innerHTML = `<span style='color: red'>${msg}</span>`
        }

        highLightUninitializedTask(task) {
            task.uiElem.querySelector('.s-and-s-btn-container').classList.add('wfbldr-task-container-uninit')
            this.err('few tasks are uninitialized, initialize/delete them before running')
        }

        draw() {
            if (this.tasks.length == 0)
                this.tasks.push(this.newTask())
            this.graph.innerHTML = ''
            render('wfbldr', {
                ele: 'div', classList: 'wf-actions', children: [
                    { ele: 'button', classList: 'wf-action wf-action-run', attribs: { innerHTML: '&#x25B6;', title: 'Run' }, evnts: { click: () => this.runWf() } }
                ]
            }, () => 1, this.graph)
            this.tasks.forEach(task => {
                let taskUIElement = this.getTaskItem(task);
                task.uiElem = taskUIElement;
                render('wfbldr', { ele: taskUIElement, preBuilt: true }, null, this.graph)
                this.updateTitle(task)
            })
        }

        show() { }
        destroy() { }
    }

    function inst_template(item) {
        let inst = JSON.parse(item.content)
        return render('a-i-s-r', {
            ele: 'div',
            classList: 'container',
            children: [
                { ele: 'span', text: 'Owner', classList: 'key' },
                { ele: 'span', text: item.owner, classList: 'value' },
                { ele: 'br' },

                { ele: 'span', text: 'IP address', classList: 'key' },
                { ele: 'span', text: inst.ipaddr, classList: 'value' },
                { ele: 'br' },

                { ele: 'span', text: 'Installation location', classList: 'key' },
                { ele: 'span', text: inst.installloc, classList: 'value' },
                { ele: 'br' },

                {
                    ele: 'div',
                    classList: 'tags',
                    style: {
                        padding: '10px'
                    },
                    attribs: {
                        innerHTML: item.tags.map(t => `<span class="a-i-s-r-tag">${t}</span>`).join(' ')
                    }
                }
            ]
        })
    }

    class PropsManager {

        constructor(ele, spec, inputs, inputChangedCb) {
            this.ele = ele;
            this.spec = spec;
            this.inputs = inputs || {};
            this.inputChangedCb = inputChangedCb;
        }

        initialized() {
            let spec = this.spec
            for (let i = 0; i < spec.inputs.length; i++) {
                const inputName = spec.inputs[i].name;
                if (!(inputName in this.inputs))
                    return false;
            }
            return true;
        }

        getValues() {
            return this.inputs;
        }

        showProps() {

            let obj = this.spec;

            let extraInputs = [
                { name: 'failWfOnTaskFailure', label: 'Fail workflow on this taks failure', type: 'boolean', default: false }
            ];

            /* add some extra inputs */
            extraInputs.forEach(ei => {
                if (obj.inputs.filter(x => x.name == ei.name).length == 0) {
                    obj.inputs.push(ei)
                    this.inputs[ei.name] = ei.default
                }
            })

            this.ele.innerHTML = ''
            render('props', {
                ele: 'div',
                styles: { 'border-top': 'solid 1px black', 'padding': '10px 20px' },
                children: [
                    { ele: 'b', text: 'Properties' },
                    { ele: 'div', iden: 'tab' }
                ]
            }, (id, ele) => this[id] = ele, this.ele)

            let inputChanged = (key, value) => {
                this.inputs[key] = value
                if (this.inputChangedCb) {
                    try {
                        this.inputChangedCb(key, value)
                    } catch (e) { /* don't give a damn */ }
                }
            }

            let inputTypes = {
                'instances': (key) => {
                    return {
                        ele: makeSearchAndSelectButton('instance', 'instance', inst_template, v => inputChanged(key, v), this.inputs[key]),
                        classList: 'props-ed-input',
                        preBuilt: true
                    }
                },
                'string': (key) => {
                    return {
                        ele: 'input',
                        classList: 'ed-input',
                        styles: { width: 'calc(100% - 16px)' },
                        attribs: { value: this.inputs[key] || "" },
                        evnts: { input: function () { inputChanged(key, this.value) } }
                    }
                },
                'boolean': (key) => {
                    return {
                        ele: 'input',
                        classList: 'ed-input',
                        attribs: { type: 'checkbox', checked: this.inputs[key] || false },
                        evnts: { input: function () { inputChanged(key, this.checked) } }
                    }
                }
            }

            tabulate(obj.inputs, this.tab, {
                classPrefix: 'wfbldr-props',
                keys: {
                    'Input property': { vFunc: (iSpec) => iSpec.label || iSpec.name },
                    'Value': {
                        vFunc: (iSpec) => inputTypes[iSpec.type](iSpec.name)
                    }
                }
            })
        }
    }

    class JobsUI {

        constructor(container, wfm) {

            this.wfm = wfm

            render('jobs-ui', {
                ele: 'div', iden: 'jobsView', classList: 'jobs-view', children: [
                    {
                        ele: 'div', classList: 'actions', children: [
                            { ele: 'button', attribs: { innerHTML: '&#x27f2;' }, evnts: { click: () => this.refresh() } }
                        ]
                    },
                    {
                        ele: 'div', iden: 'jobsTable', classList: 'table', children: []
                    }
                ]
            }, (id, ele) => this[id] = ele, container)

        }

        show() {
            this.refresh()
        }

        refresh() {
            post(
                '/nocors',
                {
                    method: 'GET',
                    url: `http://inedctst01:5000/wf/${getCurrentUser()}/list`
                },
                { 'Content-Type': 'application/json' }
            ).then(data => JSON.parse(data.response))
                .then(jobs => {
                    this.jobsTable.innerHTML = ''
                    tabulate(jobs, this.jobsTable, {
                        keys: {
                            Jobs: { vFunc: (x) => ({ ele: 'a', text: x.id, attribs: { href: '#' }, evnts: { click: () => this.wfm.openJob(x.id) } }), keyId: 'id', sortable: true },
                            State: {
                                vFunc: (x) => ({
                                    ele: 'span',
                                    text: x.state.str,
                                    children: (x.state.str != 'running' ? [] : [{ ele: 'img', attribs: { src: '/wait.gif' }, styles: { 'vertical-align': 'middle', 'margin-left': '5px' } }])
                                }),
                                kFunc: (x) => x.state.str,
                                sortable: true
                            },
                            'Start time': { vFunc: (x) => new Date(x.state.starttime).toLocaleString(), kFunc: (x) => x.state.starttime, sortable: true },
                            'End time': { vFunc: (x) => x.state.endtime ? new Date(x.state.endtime).toLocaleString() : '--/--/----', kFunc: (x) => x.state.endtime, sortable: true },
                            '': {
                                vFunc: (x) => {
                                    return {
                                        ele: 'div', children: (x.state.str != 'running' ? [] : [{ ele: 'a', attribs: { href: '#' }, text: 'stop', evnts: { click: () => stopJob(x.id) } }])
                                    }
                                }
                            }
                        }
                    })
                }).catch((e) => {
                    console.log(e);
                    this.jobsTable.innerHTML = `No jobs found. Try [re]-logging in`
                })

            if (!this.refreshTaskId) {
                this.refreshTaskId = setInterval(() => this.refresh(), 5000)
            }
        }

        destroy() {
            if (this.refreshTaskId) {
                clearInterval(this.refreshTaskId)
                this.refreshTaskId = undefined
            }
        }
    }

    function stopJob(job_id) {
        post('/nocors', { method: 'POST', url: `http://inedctst01:5000/wf/${getCurrentUser()}/${job_id}/stop` }, { 'Content-Type': 'application/json' })
            .then(() => showSuccess('Stop request sent'))
    }

    class JobViewer {
        constructor(element, wfm) {
            this.wfm = wfm;
            this.container = element;
            element.innerHTML = ''
        }

        show(job_id) {

            post('/nocors', { method: 'GET', url: `http://inedctst01:5000/wf/${getCurrentUser()}/${job_id}` }, { 'Content-Type': 'application/json' })
                .then(r => JSON.parse(r.response))
                .then(job => {

                    this.container.innerHTML = ''
                    render('view-job', {
                        ele: 'div',
                        styles: { position: 'relative' },
                        children: [
                            { ele: 'div', iden: 'titleContainer' },
                            {
                                ele: 'div', children: [
                                    { ele: 'div', classList: 'graph', iden: 'graphContainer' },
                                    { ele: 'div', classList: 'props', iden: 'props' },
                                    {
                                        ele: 'div', classList: 'log-container', iden: 'logs', children: [
                                            { ele: 'h3', classList: 'logtitle', iden: 'logtitle' },
                                            { ele: 'pre', classList: 'logs', iden: 'logcontent' }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }, (id, ele) => this[id] = ele, this.container)

                    this.job = job;
                    this._show(job, job.state)
                })

            if (!this.refreshTaskId) {
                this.refreshTaskId = setInterval(() => {
                    post('/nocors', { method: 'GET', url: `http://inedctst01:5000/wf/${getCurrentUser()}/${job_id}/status` }, { 'Content-Type': 'application/json' })
                        .then(r => JSON.parse(r.response))
                        .then(state => this._show(this.job, state))
                }, 5000)
            }
        }

        _show(job, statusOb) {
            let loadLog = (url) => {
                post('/nocors', { method: 'GET', url: `http://inedctst01:5000/wf/joblogs/${url.substring(2)}` }, { 'Content-Type': 'application/json' })
                    .then(r => this.logcontent.innerText = r.response)
                this.props.style.display = 'none'
                this.logs.style.display = 'block'
                this.logtitle.innerText = url.substring(7)
            }

            let getTasksUIs = (job, statusObj) => {
                return job.wf.tasks.map(task => {
                    let tstatus = statusObj.taskstatus[task.id]
                    let status = tstatus ? tstatus.status : 'queued'
                    return {
                        ele: 'div',
                        classList: 'task-container-parent',
                        children: [
                            {
                                ele: 'span', classList: 'task-status', text: status,
                                children: (status != 'running' ? [] : [{ ele: 'img', attribs: { src: '/wait.gif' }, styles: { 'vertical-align': 'middle', 'margin-left': '5px' } }])
                            },
                            {
                                ele: 'div',
                                classList: `task-container ${status}`,
                                children: [
                                    { ele: 'span', classList: 'task-name', text: task.displayName },
                                    ...(!tstatus ? [] : [
                                        {
                                            ele: 'div', classList: `task-steps`, children: tstatus.steps.map(step => {
                                                return {
                                                    ele: 'div', children: [
                                                        { ele: 'span', classList: 'task-step-time', text: (new Date(step.updatetime).toLocaleString()) },
                                                        { ele: 'span', classList: 'task-step-name', text: step.str },
                                                    ]
                                                }
                                            })
                                        }
                                    ])
                                ],
                                evnts: {
                                    click: (e) => {
                                        new PropsManager(this.props, task.automationSpec, task.inputs).showProps()
                                        this.logs.style.display = 'none'
                                        this.props.style.display = 'block'
                                    }
                                }
                            },
                            {
                                ele: 'div', classList: 'task-log-links',
                                children: [
                                    { ele: 'a', attribs: { href: '#' }, classList: 'log-links', text: 'stdout', evnts: { click: (e) => { e.stopPropagation(); loadLog(task.stdout) } } },
                                    { ele: 'a', attribs: { href: '#' }, classList: 'log-links', text: 'stderr', evnts: { click: (e) => { e.stopPropagation(); loadLog(task.stderr) } } },
                                ]
                            }
                        ]
                    }


                })
            }

            this.titleContainer.innerHTML = ''
            this.graphContainer.innerHTML = ''

            render('view-job', {
                ele: 'div', children: [
                    { ele: 'h4', text: job.id, styles: { 'display': 'inline-block', 'margin-right': '10px' } },
                    { ele: 'span', text: statusOb.str },
                    {
                        ele: 'div', classList: 'actions', children: [
                            { ele: 'button', attribs: { innerHTML: '&#x25A0;', disabled: statusOb.str != 'running' }, text: 'stop', evnts: { click: () => stopJob(job.id) } }
                        ]
                    },
                ]
            }, (id, ele) => this[id] = ele, this.titleContainer)

            render('view-job', { ele: 'div', classList: 'graph', children: getTasksUIs(job, statusOb) }, (id, ele) => this[id] = ele, this.graphContainer)
        }

        destroy() {
            if (this.refreshTaskId) clearInterval(this.refreshTaskId)
            this.refreshTaskId = undefined
        }
    }

    class WfManager {

        constructor(container) {

            this.WF_BUILDER = 'Workflow Builder'
            this.JOBS_UI = 'Jobs'
            this.JOB_VIEWER = 'View job'

            render('wfmanager', {
                ele: 'div', children: [
                    {
                        ele: 'div', classList: 'panel-title-bar', children: [
                            { ele: 'h3', iden: 'panelTitle' },
                            {
                                ele: 'div', classList: 'panel-switcher', children: [
                                    { ele: 'a', classList: 'panel-switcher-btn', attribs: { href: '#' }, text: this.WF_BUILDER, evnts: { click: () => this.switchPanel(this.WF_BUILDER) } },
                                    { ele: 'a', classList: 'panel-switcher-btn', attribs: { href: '#' }, text: this.JOBS_UI, evnts: { click: () => this.switchPanel(this.JOBS_UI) } }
                                ]
                            }
                        ]
                    },
                    {
                        ele: 'div', iden: 'contentPanel', children: [
                            { ele: 'div', iden: this.WF_BUILDER },
                            { ele: 'div', classList: 'jobs-ui', iden: this.JOBS_UI },
                            { ele: 'div', classList: 'view-job', iden: this.JOB_VIEWER },
                        ]
                    }
                ]
            }, (id, ele) => this[id] = ele, container)
            this.objs = {}
            this.objs[this.WF_BUILDER] = this.wfBuilder = new WorkflowBuilder(this[this.WF_BUILDER], this)
            this.objs[this.JOBS_UI] = this.jobsUI = new JobsUI(this[this.JOBS_UI], this)
            this.objs[this.JOB_VIEWER] = this.jobViewer = new JobViewer(this[this.JOB_VIEWER], this)
            this.switchPanel(this.WF_BUILDER)
        }

        openJob(job_id) {
            this.switchPanel(this.JOB_VIEWER, job_id)
        }

        listJobs() {
            this.switchPanel(this.JOBS_UI)
        }

        switchPanel(pName, arg) {
            [this.WF_BUILDER, this.JOBS_UI, this.JOB_VIEWER].filter(x => x != pName).forEach(p => { this[p].style.display = 'none'; this.objs[p].destroy() })
            this.objs[pName].show(arg)
            this[pName].style.display = 'block'
            this.panelTitle.innerHTML = pName
        }
    }

    let container = A.makeDiv();
    new WfManager(container)
})()