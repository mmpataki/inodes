const USER_KEY = "user", TOK_KEY = "tok";
let baseUrl = window.location.port == 8000 ? "http://localhost:8080" : ""
let rejectCodeList = [400, 401, 500, 403];

function getBaseUrl() {
    return baseUrl;
}

function _ajax(method, url, data, hdrs, cancelToken) {
    if (getCurrentUser()) {
        if (!hdrs) hdrs = {}
        hdrs['AuthInfo'] = `${getCurrentUser()}:${getCookie(TOK_KEY)}`
    }
    return new Promise((resolve, reject) => {
        var xhttp = new XMLHttpRequest();
        if (cancelToken) {
            cancelToken.cancel = function () {
                xhttp.abort();
                reject(new Error("Cancelled"));
            };
        }
        xhttp.onreadystatechange = function () {
            if (this.readyState == 4 && this.status == 200) {
                let json;
                try { json = JSON.parse(this.responseText); } catch (e) { }
                resolve({
                    response: this.responseText,
                    json,
                    headers: makeHMap(xhttp.getAllResponseHeaders())
                })
            }
            if (this.readyState == 4 && rejectCodeList.includes(this.status)) {
                reject({ message: JSON.parse(this.responseText).message, code: this.status });
            }
        };
        xhttp.onerror = function () {
            reject({ message: JSON.parse(this.responseText).message, code: this.status });
        }
        xhttp.open(method, url, true);
        hdrs && Object.keys(hdrs).forEach(key => xhttp.setRequestHeader(key, hdrs[key]))
        xhttp.send(data);
    });
}

function ajax(method, url, data, hdrs, cancelToken) {
    return _ajax(method, `${baseUrl}${url}`, data, hdrs, cancelToken)
}

function makeHMap(headers) {
    var arr = headers.trim().split(/[\r\n]+/);
    var headerMap = {};
    arr.forEach(function (line) {
        var parts = line.split(': ');
        var header = parts.shift();
        var value = parts.join(': ');
        headerMap[header] = value;
    });
    return headerMap;
}

function get(url, token) {
    return ajax("GET", url, undefined, {}, token);
}

function last(fn) {
    var lastToken = { cancel: function () { } }; // start with no op
    return function () {
        lastToken.cancel();
        var args = Array.prototype.slice.call(arguments);
        args.push(lastToken);
        return fn.apply(this, args);
    };
}

let getLast = last(get);

function post(url, data, hdrs) {
    if (typeof data !== 'string') {
        hdrs = { "Content-Type": "application/json", ...hdrs }
    }
    return ajax('POST', url, JSON.stringify(data), hdrs);
}

function postFile(url, data, hdrs) {
    return ajax('POST', url, data, hdrs);
}

function delet(url) {
    return ajax('DELETE', url);
}

// WYSIWYG
function _get(url, token) {
    return _ajax("GET", url, undefined, {}, token);
}

function _post(url, data, hdrs) {
    return _ajax('POST', url, JSON.stringify(data), hdrs);
}

function _postFile(url, data, hdrs) {
    return _ajax('POST', url, data, hdrs);
}

function _delet(url) {
    return _ajax('DELETE', url);
}
//

function ncors_get(url, data, hdrs) {
    return nocors('GET', url, data, hdrs)
}

function ncors_post(url, data, hdrs) {
    return nocors('POST', url, data, hdrs)
}

function ncors_delete(url, data, hdrs) {
    return nocors('DELETE', url, data, hdrs)
}

function ncors_put(url, data, hdrs) {
    return nocors('PUT', url, data, hdrs)
}

function nocors(method, url, data, headers) {
    return post(
        `/nocors`,
        { method: method, data: JSON.stringify(data), headers, url: url },
        { "Content-Type": "application/json" }
    )
}

function setCookie(name, value, days) {
    var expires = "";
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "") + expires + "; path=/";
}

function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

function eraseCookie(name) {
    document.cookie = name + '=; Max-Age=-99999999;';
}

function g(id) {
    return document.getElementById(id);
}

function c(cl) {
    return document.getElementsByClassName(cl)[0];
}

function getCurrentUser() {
    return getCookie(USER_KEY)
}

let __colors = {
    ERR: { background: '#FF504A', color: 'white', border: 'red' },
    WARN: { background: 'lightorange', color: 'white', border: 'orange' },
    SUCCESS: { background: '#3BB07B', color: 'white', border: 'darkgreen' },
    INFO: { background: '#24326B', color: 'white', border: 'darkblue' }
}

function showError(x) {
    showMessage(x, __colors.ERR)
    console.error(x)
}

function showSuccess(x) {
    showMessage(x, __colors.SUCCESS)
}

function showWarning(x) {
    showMessage(x, __colors.WARN)
}

function showInfo(x) {
    showMessage(x, __colors.INFO)
}

function showMessage(x, col) {
    let d = document.createElement('div')
    d.classList = 'fade-out'
    d.style = `position: fixed; right: 10px; top: 50px; background-color: ${col.background}; border: solid 2px ${col.border}; border-radius: 3px; padding: 10px; color: ${col.color}; font-size: 1em`
    d.innerText = x
    document.body.appendChild(d)
    setTimeout(() => d.remove(), 8000)
}

function trender(template, obj, methods) {
    let d = document.createElement('div')
    d.innerHTML = Sqrl.render(template.trim(), obj)
    return d.childNodes[0]
}

function render(name, spec, elemCreated, container) {
    let x = _render(name, spec, elemCreated, container);
    _fireRenderedCallBacks(spec);
    return x;
}

function _fireRenderedCallBacks(spec) {
    if (spec && spec.evnts && spec.evnts.rendered)
        spec.evnts.rendered(spec._______elem)
    delete spec['_______elem']
    spec.children && spec.children.forEach(child => _fireRenderedCallBacks(child))
}

function _render(name, spec, elemCreated, container) {
    if (Array.isArray(spec)) {
        spec.forEach(s => render(name, s, elemCreated, container))
        return container
    }
    let e;
    if (!spec.preBuilt) {
        e = document.createElement(spec.ele);
    } else {
        e = spec.ele;
    }
    spec._______elem = e;
    spec.iden && elemCreated && elemCreated(spec.iden, e)
    if (spec.text) e.innerText = spec.text;
    if (spec.html) e.innerHTML = spec.html;
    if (spec.classList) {
        spec.classList.split(/\s+/).map(x => e.classList.add(x[0] == '$' ? x.substring(1) : `${name}-${x}`))
    }
    spec.styles && Object.keys(spec.styles).forEach(key => { e.style[key] = spec.styles[key] })
    spec.evnts && Object.keys(spec.evnts).forEach(key => { e.addEventListener(key, spec.evnts[key]) })
    if (spec.children) {
        if (spec.children instanceof Function) spec.children().forEach(x => e.appendChild(x))
        else spec.children.forEach(child => _render(name, child, elemCreated, e))
    }
    if (spec.value) e.value = spec.value
    if (spec.title) e.title = spec.title
    spec.attribs && Object.keys(spec.attribs).forEach(key => {
        e[key] = spec.attribs[key]
    })
    if (container) {
        let lbl;
        if (spec.label || spec.postlabel) {
            let rgid = "id_" + Math.random();
            e.id = rgid
            lbl = document.createElement('label')
            spec.labelStyle && (lbl.style = spec.labelStyle)
            lbl.innerHTML = spec.label || spec.postlabel
            lbl.setAttribute('for', rgid)
        }
        if (spec.label) container.appendChild(lbl)
        container.appendChild(e)
        if (spec.postlabel) container.appendChild(lbl)
        return container;
    }
    return e;
}

function randColor() {
    var letters = '0123456789ABCDEF';
    var color = '#';
    for (var i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

function callWithWaitUI(element, func) {
    return new _callWithWaitUI(element, func);
}
function _callWithWaitUI(element, func) {
    element.style.position = "relative";
    let overlay = render('loader', {
        ele: 'div',
        attribs: {
            style: 'position: absolute; display: flex; position: absolute; inset: 0px; align-items: center; opacity: 0.9; justify-content: center; background-color: white; z-index: 100000'
        },
        children: [
            {
                ele: 'span', children: [
                    { ele: 'img', attribs: { src: '/wait.gif', style: `height: 20px; width: 20px` } },
                    { ele: 'span', iden: 'loadTxt', attribs: { style: 'opacity: 20px; padding : 10px' }, text: 'Loading' }
                ]
            }
        ]
    }, (id, ele) => this[id] = ele);
    element.appendChild(overlay);
    let done = () => overlay.remove();
    let updateText = (txt) => this.loadTxt.innerText = txt;
    try {
        func(done, updateText);
    } catch (e) {
        console.error(e)
        done();
    }
}

function tabulate(arr, ele, spec) {
    return new _tabulate(arr, ele, spec)
}

function _tabulate(arr, ele, spec) {

    if (arr.length < 1) return null;

    let sortConf = {}, currSortKey = spec.defaultSortKey
    let attribs = { classList: 'tabulate-cell' };

    Object.keys(arr[0]).forEach(key => sortConf[key] = 'asc')

    let getTr = (row) => {
        return {
            ele: 'tr',
            classList: `row`,
            children: Object.keys(spec.keys).map(key => {
                let kObj = spec.keys[key];
                let props = {}
                if (kObj.vFunc) {
                    let val = kObj.vFunc(row)
                    props = (typeof (val) == 'string') ? { text: val } : { children: [val] }
                } else if (kObj.keyId) {
                    props = { text: "" + row[kObj.keyId] }
                }
                return { ele: 'td', classList: `cell cell-${key.toLowerCase().replaceAll(/([^A-Za-z0-9])+/ig, '-')}`, attribs, ...props }
            }),
            evnts: spec.rowEvents
        }
    }

    let sortFunc = (a, b) => {
        let k = spec.keys[currSortKey].keyId
        let kf = spec.keys[currSortKey].kFunc
        let v1 = kf ? kf(a) : a[k]
        let v2 = kf ? kf(b) : b[k]
        if (typeof (v1) == 'string') {
            return (sortConf[currSortKey] == 'asc') ? v1.localeCompare(v2) : v2.localeCompare(v1);
        }
        return (sortConf[currSortKey] == 'asc') ? v1 - v2 : v2 - v1;
    }

    let sortBy = (key) => {
        if (!spec.keys[key].sortable)
            return;
        currSortKey = key;
        sortConf[key] = sortConf[key] == 'asc' ? 'dsc' : 'asc';
        this.tab.remove()
        renderTab();
    }

    let getSortbtn = (key) => {
        if (!spec.keys[key].sortable) return ''
        if (key == currSortKey) {
            return (sortConf[key] == 'asc' ? '<span title="Sorted ascending">&#x25B2;</span>' : '<span title="Sorted descending">&#x25BC;</span>');
        } else {
            return `<span style='position: absolute; top: 6px'>&#x25BE;</span><span style='position: absolute; top: 0px'>&#x25B4;</span>`
        }
    }

    let renderTab = () => {
        if (currSortKey)
            arr.sort(sortFunc)
        this.tab = render(spec.classPrefix, {
            ele: 'table',
            classList: 'table',
            children: [
                {
                    ele: 'tr',
                    children: Object.keys(spec.keys).map(key => {
                        return {
                            ele: 'th',
                            attribs,
                            children: [
                                { ele: 'span', text: key },
                                {
                                    ele: 'span',
                                    attribs: {
                                        style: 'position: relative; cursor: pointer',
                                        innerHTML: getSortbtn(key)
                                    },
                                    evnts: { click: () => sortBy(key) }
                                }
                            ]
                        }
                    })
                },
                ...(arr.map(row => getTr(row)))
            ]
        }, () => 0)
        ele.appendChild(this.tab)
    }
    renderTab()
    return this.tab
}

function filePicker(selectedFiles, title) {
    return new _filePicker(selectedFiles, title);
}

function _filePicker(selectedFiles, title) {
    let self = this;
    title = title ? title : 'Pick a file'
    function updateThumbnail(file, renameFile) {
        let thumbnailElement = self.thumbnail;
        self.helperLabel.style.display = 'none'
        let fName = renameFile ? `${+(new Date())}_${file.name}` : file.name;
        if (file.type.startsWith("image/")) {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => {
                thumbnailElement.style.backgroundImage = `url('${reader.result}')`;
            };
        } else {
            thumbnailElement.style.backgroundImage = null;
            thumbnailElement.innerHTML = fName;
        }
        let fd = new FormData();
        fd.append("file", file, fName);
        callWithWaitUI(self.fileUploadContainer, (done) => {
            postFile(`/files`, fd, {})
                .then(e => { self.file = e.response })
                .then(e => showSuccess('Uploaded !'))
                .then(e => refreshUserFileList())
                .catch(e => showError(e.message))
                .finally(e => done())
        })
    }
    function showFiles(files) {
        self.userFiles.innerHTML = ''
        let spec = {
            defaultSortKey: 'Last Modified Time',
            classPrefix: 'user-file',
            keys: {
                'Choosen': {
                    vFunc: (file) => {
                        return {
                            ele: 'input',
                            classList: 'select-box',
                            attribs: {
                                type: 'checkbox',
                                value: file.path,
                                checked: selectedFiles ? selectedFiles.includes(file.path) : false
                            }
                        }
                    }
                },
                'Name': { keyId: 'name', sortable: true },
                'Last Modified Time': {
                    vFunc: (file) => new Date(file.mtime).toLocaleString(),
                    sortable: true,
                    keyId: 'mtime'
                },
                'Size': {
                    keyId: 'size', sortable: true,
                    vFunc: (file) => {
                        let pow = Math.floor(Math.floor(Math.log2(file.size)) / 10);
                        return { ele: 'span', text: `${Math.round(file.size / (Math.pow(2, pow * 10)))} ${{ 0: 'Bytes', 1: 'KB', 2: 'MB', 3: 'GB' }[pow]}`, attribs: { title: `${file.size} bytes` } }
                    }
                },
                'Preview': {
                    vFunc: (file) => {
                        return {
                            ele: 'a',
                            text: 'Preview',
                            attribs: { href: '#' },
                            evnts: {
                                click: function () {
                                    let img = document.createElement('img')
                                    img.style = 'max-width: 200px; max-height: 200px'
                                    img.src = file.path
                                    this.parentNode.appendChild(img)
                                    this.remove()
                                }
                            }
                        }
                    }
                },
                'Actions': {
                    vFunc: (file) => {
                        return {
                            ele: 'a',
                            attribs: { href: '#' },
                            text: 'delete',
                            evnts: {
                                click: function () {
                                    if (confirm(`Sure you want to delete ${file.name}?`)) {
                                        delet(`/files?file=${encodeURIComponent(file.name)}`)
                                            .then(x => this.parentNode.parentNode.remove())
                                            .then(x => showSuccess('Deleted successfully'))
                                            .catch(x => showError(x.message))
                                    }
                                }
                            }
                        }
                    }
                },
            },
            rowEvents: {
                click: function () {
                    this.querySelector('input[type=checkbox]').checked = !this.querySelector('input[type=checkbox]').checked
                }
            }
        }
        tabulate(files, self.userFiles, spec)
    }
    function refreshUserFileList() {
        callWithWaitUI(self.userFiles, (done) => {
            get('/allmyfiles')
                .then(resp => JSON.parse(resp.response))
                .then(files => showFiles(files))
                .then(e => done())
        })
    }
    return new Promise((resolve, reject) => {
        let fp;
        self.fp = fp = render('inodes-file-picker', {
            ele: 'div',
            classList: 'container',
            children: [
                { ele: 'h3', text: title, attribs: { style: 'position: absolute; top: 0px; left: 10px' } },
                { ele: 'h4', text: 'Upload files' },
                {
                    ele: 'div',
                    classList: 'fileupload-container',
                    children: [
                        {
                            ele: 'div',
                            classList: 'fileupload-zone',
                            iden: 'fileUploadContainer',
                            children: [
                                {
                                    ele: 'div',
                                    iden: 'thumbnail',
                                    classList: 'preview'
                                },
                                {
                                    ele: 'span',
                                    iden: 'helperLabel',
                                    children: [
                                        { ele: 'span', text: 'Drag / Paste / ' },
                                        { ele: 'a', attribs: { href: '#' }, text: 'Browse', evnts: { click: () => self.fileInput.click() } },
                                        { ele: 'span', text: ' the files.' }
                                    ],
                                    classList: 'helper-label'
                                }
                            ],
                            evnts: {
                                dragover: function (e) {
                                    e.preventDefault();
                                    this.classList.add("drop-zone--over");
                                },
                                dragleave: () => { this.classList.remove("drop-zone--over") },
                                dragend: () => { this.classList.remove("drop-zone--over") },
                                drag: (e) => {
                                    e.preventDefault();
                                    if (e.dataTransfer.files.length) {
                                        self.fileInput.files = e.dataTransfer.files;
                                        updateThumbnail(e.dataTransfer.files[0]);
                                    }
                                    this.classList.remove("drop-zone--over");
                                },
                                drop: (e) => {
                                    e.preventDefault();
                                    if (e.dataTransfer.files.length) {
                                        self.fileInput.files = e.dataTransfer.files;
                                        updateThumbnail(e.dataTransfer.files[0]);
                                    }
                                    this.classList.remove("drop-zone--over");
                                },
                                paste: (e) => {
                                    updateThumbnail(e.clipboardData.files[0], true)
                                }
                            }
                        },
                        { ele: 'iframe', attribs: { name: 'hehe', style: 'display: none' } }, // to stop browser redirect after a post
                        {
                            ele: "input",
                            iden: 'fileInput',
                            attribs: {
                                type: "file",
                                name: "file",
                                style: "display: none"
                            },
                            evnts: {
                                change: function () {
                                    if (this.files.length) {
                                        updateThumbnail(this.files[0]);
                                    }
                                }
                            }
                        }
                    ]
                },
                { ele: 'h4', text: 'Your files' },
                {
                    ele: 'div',
                    iden: 'userFiles',
                    classList: 'user-files'
                },
                {
                    ele: 'div',
                    classList: 'actions',
                    children: [
                        {
                            ele: 'button',
                            classList: 'btn',
                            text: 'Select',
                            evnts: {
                                click: function () {
                                    let f = [], cbs = self.userFiles.getElementsByClassName('user-file-select-box');
                                    for (let i = 0; i < cbs.length; i++) {
                                        if (cbs[i].checked)
                                            f.push(cbs[i].value)
                                    }
                                    fp.remove()
                                    resolve(f)
                                }
                            }
                        },
                        {
                            ele: 'button',
                            classList: 'btn',
                            text: 'Cancel',
                            evnts: {
                                click: function () {
                                    fp.remove()
                                    reject("cancelled")
                                }
                            }
                        }
                    ]
                }
            ]
        }, (id, ele) => self[id] = ele)
        document.body.appendChild(fp)
        refreshUserFileList()
    })
}

let tagifySpecs = {
    user: {
        idKey: 'value',
        tagTemplate: (ug, t) => {
            return `
                <tag title="${ug.type} ${ug.name}" contenteditable='false' spellcheck='false' tabIndex="-1"
                    class="${t.settings.classNames.tag} ${ug.class ? ug.class : ""}" ${t.getAttributes(ug)}>
                    <x title='remove' class='tagify__tag__removeBtn' role='button' aria-label='remove tag'></x>
                    <div>
                        ${ug.type == 'user' ? '&#x1F464;' : '&#x1F465;'}
                        <span class='tagify__tag-text'>${ug.name}</span>
                    </div>
                </tag>
                `
        },
        dropdownTemplate: ug => {
            if (ug.type == 'user') {
                return `<div class='tagify__dropdown__item'>&#x1F464; ${ug.name} (${ug.id.substring(2)})</div>`;
            } else {
                return `<div class='tagify__dropdown__item'>&#x1F465; ${ug.name}</div>`;
            }
        },
        searchKeys: ['value', 'name'],
        url: function (term) {
            return `${baseUrl}/auth/find-ug-like?term=${term}`
        },
        responseHandler: function (resp) {
            return resp.map(x => {
                return {
                    ...x, value: x.id, type: x.id.startsWith('u') ? 'user' : 'group', name: ((!x.name
                        || x.name == 'null') ? x.id.substring(2) : x.name)
                }
            })
        },
        whiteList: true
    },
    tag: {
        idKey: 'name',
        tagTemplate: function (tagData, t) {
            return `
                <tag title="${tagData.name}" contenteditable='false' spellcheck='false' tabIndex="-1"
                    class="${t.settings.classNames.tag}" ${t.getAttributes(tagData)}>
                    <x title='remove' class='tagify__tag__removeBtn' role='button' aria-label='remove tag'></x>
                    <div description="${tagData.description}">
                        <span class='tagify__tag-text'>${tagData.name}</span>
                    </div>
                </tag>
                `
        },
        dropdownTemplate: function (tagData) {
            return `<div class='tagify__dropdown__item'>${tagData.name}</div>`;
        },
        searchKeys: ['value'],
        url: function (term) {
            return `${baseUrl}/find-tags-like?term=${term}`;
        },
        responseHandler: function (resp) {
            return resp.map(x => { return { ...x, value: x.name } })
        },
        whiteList: false
    }
}

function tagify(inputElement, specKey) {
    let spec = tagifySpecs[specKey];
    let tagifyThings = new Tagify(inputElement, {
        tagTextProp: spec.idKey,
        enforceWhitelist: spec.whiteList,
        skipInvalid: spec.whiteList,
        dropdown: { closeOnSelect: true, enabled: 0, searchKeys: spec.searchKeys },
        autoComplete: { rightKey: true },
        delimiters: ' ',
        templates: {
            tag: function (d) {
                return spec.tagTemplate(d, this)
            },
            dropdownItem: function (d) {
                return spec.dropdownTemplate(d, this)
            }
        },
        whitelist: [],
        delimiter: ' '
    })
    let controller;
    tagifyThings.on('input', function onInputTags(e) {
        var value = e.detail.value;
        if (spec.inputHandler) {
            value = spec.inputHandler(specKey, value)
            if (!value) return;
        }
        tagifyThings.settings.whitelist.length = 0;
        controller && controller.abort();
        controller = new AbortController();
        tagifyThings.loading(true).dropdown.hide.call(tagifyThings)
        fetch(spec.url(value), { signal: controller.signal })
            .then(RES => RES.json())
            .then(RES => spec.responseHandler(RES))
            .then(function (whitelist) {
                tagifyThings.settings.whitelist.splice(0, whitelist.length, ...whitelist)
                tagifyThings.loading(false).dropdown.show.call(tagifyThings);
            })
    })
    return tagifyThings
}

function StoryTeller(storyBoardElement) {

    let stack = [];
    render('storyboard', {
        ele: 'div',
        children: [
            {
                ele: 'div', classList: 'titlebar', children: [
                    { ele: 'button', classList: 'backbtn', iden: 'backbtn', html: '&larr; Back', evnts: { click: () => this.back() } },
                    { ele: 'span', classList: 'title', text: '', iden: 'title' },
                    { ele: 'button', html: 'Next &rarr;', classList: 'nextbtn', iden: 'nextbtn', evnts: { click: () => this.next() } }
                ]
            },
            { ele: 'div', classList: 'errmsg', iden: 'errmsg' },
            { ele: 'div', classList: 'content', iden: 'storyBoard' }
        ]
    }, (id, ele) => this[id] = ele, storyBoardElement);

    this.openStory = (storyClass, args) => {
        let story = { story: new storyClass(args) }
        stack.push(story);
        this.tell(story)
        this.nextbtn.disabled = false
    }

    this.back = () => {
        stack.pop();
        if (stack.length > 0)
            this.tell(stack[stack.length - 1])
        this.nextbtn.disabled = false
    }

    this.next = () => {
        let storyPack = stack[stack.length - 1]
        if (!storyPack.story.isCompleted()) {
            this.errmsg.innerHTML = storyPack.story.getErrMsg();
            this.errmsg.style.display = 'block'
            return;
        }
        this.nextbtn.disabled = true
        try {
            let preDestroy = storyPack.story.preDestroy ? storyPack.story.preDestroy() : Promise.resolve(0)
            preDestroy.then(_ => {
                let next = { storyClass: storyPack.story.nextStoryClass(), args: storyPack.story.moral() }
                if (!next) return;
                this.openStory(next.storyClass, next.args)
            })
        } finally {
            this.nextbtn.disabled = false
        }
    }

    this.tell = (storyPack) => {
        let story = storyPack.story
        if (!storyPack.ele)
            storyPack.ele = story.tell();
        this.storyBoard.innerHTML = ""
        this.errmsg.style.display = 'none'
        this.nextbtn.style.display = (!story.nextStoryClass) ? "none" : 'block';
        this.backbtn.style.display = stack.length < 2 ? 'none' : 'inline-block';
        this.title.innerHTML = storyPack.story.title();
        this.storyBoard.appendChild(storyPack.ele)
    }

    this.currentStory = () => {
        if (stack.length > 0)
            return stack[stack.length - 1].story;
    }

}

function makeSearchAndSelectButton(text, itemType, valuePickedCallback, obj, showTags = false) {
    return new _makeSearchAndSelectButton(text, itemType, valuePickedCallback, obj, showTags)
}

function _makeSearchAndSelectButton(text, itemType, valuePickedCallback, obj, showTags = false) {
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
                                        ele: 'a', classList: 'lnk', attribs: { href: '#' }, text: text,
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
                                evnts: {
                                    rendered: ele => {
                                        new Searcher(ele, app, {
                                            size: 'min',
                                            pageSize: 10,
                                            pickableResults: true,
                                            prefix: `%${itemType}`,
                                            showTags,

                                            resultPickedCallback: (r) => {
                                                console.log(r)
                                                valuePickedCallback(r)
                                                this.container.data = r;
                                                this.searchpane.style.display = 'none';
                                                this.pickedItem.style.display = 'block';
                                                this.actions.style.display = 'flex'
                                                this.pickedItem.innerHTML = ''
                                                new SmallInode(this.pickedItem, app, r, showTags)
                                            }
                                        })
                                    }
                                }
                            },
                            {
                                ele: 'div',
                                classList: 'picked-item',
                                iden: 'pickedItem',
                                evnts: {
                                    rendered: ele => {
                                        if (obj) {
                                            new SmallInode(ele, app, obj, showTags)
                                        }
                                    }
                                }
                            }
                        ]
                    },
                    {
                        ele: 'div', iden: 'actions', classList: 'actions', styles: { display: obj ? 'flex' : 'none' }, styles: { top: 5, right: 5 },
                        children: [
                            {
                                ele: 'span',
                                classList: 'action',
                                attribs: { innerHTML: '<i class="fa fa-times"></i>', title: 'reset this unit' },
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

/**
 * makes an HTML element fullscreen
 * requirement:
 *      `ele` shouldn't have any siblings
 */
function makeFullScreen(ele) {
    let x = document.createElement('div')
    x.classList = 'fullscreen-inode'
    document.body.appendChild(x)

    let cardParent = ele.parentNode, closeBtn = document.createElement('span')
    closeBtn.classList = 'closefullscreen-btn'
    closeBtn.innerHTML = '&#x2715;'
    closeBtn.addEventListener('click', function () {
        ele.remove();
        cardParent.appendChild(ele)
        x.remove();
        document.querySelector('.content').style.display = 'block'
    })

    document.querySelector('.content').style.display = 'none'
    x.appendChild(ele)
    x.appendChild(closeBtn)
}

let clipboardDiv;
function copyFormatted(html) {
    if (!clipboardDiv) {
        clipboardDiv = document.createElement('div');
        clipboardDiv.style.fontSize = '12pt'; // Prevent zooming on iOS
        // Reset box model
        clipboardDiv.style.border = '0';
        clipboardDiv.style.padding = '0';
        clipboardDiv.style.margin = '0';
        // Move element out of screen 
        clipboardDiv.style.position = 'fixed';
        clipboardDiv.style['right'] = '-9999px';
        clipboardDiv.style.top = (window.pageYOffset || document.documentElement.scrollTop) + 'px';
        // more hiding
        clipboardDiv.setAttribute('readonly', '');
        clipboardDiv.style.opacity = 0;
        clipboardDiv.style.pointerEvents = 'none';
        clipboardDiv.style.zIndex = -1;
        clipboardDiv.setAttribute('tabindex', '0'); // so it can be focused
        clipboardDiv.innerHTML = '';
        document.body.appendChild(clipboardDiv);
    }
    clipboardDiv.innerHTML = html;
    var focused = document.activeElement;
    clipboardDiv.focus();

    window.getSelection().removeAllRanges();
    var range = document.createRange();
    range.setStartBefore(clipboardDiv.firstChild);
    range.setEndAfter(clipboardDiv.lastChild);
    window.getSelection().addRange(range);

    var ok = false;
    try {
        if (document.execCommand('copy')) ok = true; else console.log('execCommand returned false !');
    } catch (err) {
        console.log('execCommand failed ! exception ' + err);
    }

    focused.focus();
}


function makeMarkDownEditor(id, value, classList, attachCallBack) {
    return {
        ele: 'div', iden: id, classList: `${classList}`, styles: { fontSize: 'inherit' },
        evnts: {
            rendered: e => {
                const Editor = toastui.Editor;
                const editor = new Editor({
                    el: e,
                    height: '100%',
                    initialEditType: 'wysiwyg',
                    previewStyle: 'vertical',
                    initialValue: value || "",
                    usageStatistics: false,
                    linkAttribute: {
                        target: '_blank',
                        contenteditable: 'false',
                        rel: 'noopener noreferrer'
                    }
                })

                e.mdeditor = editor

                editor.removeToolbarItem('image')
                editor.removeToolbarItem('toggleScrollSync')

                let imgAdder = _ => filePicker([], 'Pick an image').then(fileNames => fileNames.forEach(fn => editor.insertText(`\n![Put your description here](${fn})\n`)))

                editor.addCommand('markdown', 'insertimage', imgAdder)
                editor.addCommand('wysiwyg', 'insertimage', imgAdder)
                editor.addCommand('markdown', 'attach', function () { filePicker([], 'Pick files to attach').then(attachCallBack) })
                editor.addCommand('wysiwyg', 'attach', function () { filePicker([], 'Pick files to attach').then(attachCallBack) })

                editor.insertToolbarItem({ groupIndex: 3, itemIndex: 2 }, {
                    name: 'inodeimage',
                    tooltip: 'Insert image',
                    command: 'insertimage',
                    className: 'image toastui-editor-toolbar-icons'
                })

                editor.insertToolbarItem({ groupIndex: 3, itemIndex: 3 }, {
                    name: 'inodeattachment',
                    tooltip: 'Add attachment',
                    command: 'attach',
                    className: 'fa fa-paperclip'
                })
            }
        }
    }
}

function makeMarkdownViewer(id, mdcontent, classList) {
    return {
        ele: 'div', iden: id, classList: `${classList} $markdown-body`, styles: { fontSize: 'inherit' }, evnts: {
            rendered: e => {
                showdown.setFlavor('github')
                var converter = new showdown.Converter({
                    openLinksInNewWindow: true
                })
                e.innerHTML = converter.makeHtml(mdcontent)
                e.querySelectorAll('pre code').forEach(block => {
                    makeCodeViewer(block.parentNode)
                })
            }
        }
    }
}

function makeCodeViewer(ele) {
    hljs.highlightBlock(ele)
    makeCopyable(ele)
}

function makeExpandable(expandBtnHtml, expandedContent) {
    return [
        {
            ele: 'span', html: expandBtnHtml, evnts: {
                click: function () {
                    this.nextElementSibling.style.display = this.nextElementSibling.style.display == 'none' ? 'block' : 'none';
                }
            }
        },
        { ...expandedContent, styles: { ...expandedContent.styles, display: 'none' } }
    ]
}

function makeCodeEditor(id, lang, value, classList, label) {
    return {
        ele: 'div', label, classList: `${classList} $inodes-code-editor`, children: [
            {
                ele: 'div', iden: id, styles: { fontFamily: 'monospace' }, evnts: {
                    rendered: e => {
                        e.textContent = value || ""
                        hljs.getLanguage('md')
                        import('/codeeditor/codejar.js').then(_ => e.ceditor = new _.CodeJar(e, _.withLineNumbers(_ => _.innerHTML = hljs.highlight(lang, _.textContent).value)))
                    }
                }
            }
        ]
    }
}

function makeCopyable(ele) {
    if (ele.querySelector('.copier-btn'))
        return
    ele.style.position = 'relative'
    ele.classList.add('copyable')
    render('copier', {
        ele: 'div', classList: 'btn-container', children: [{ ele: 'i', classList: 'btn $far $fa-copy', title: 'copy to clipboard' }],
        evnts: { click: _ => { copyFormatted(ele.outerHTML); showInfo('copied') } }
    }, 0, ele)
}

function MD5(d) { var r = M(V(Y(X(d), 8 * d.length))); return r.toLowerCase() }; function M(d) { for (var _, m = "0123456789ABCDEF", f = "", r = 0; r < d.length; r++)_ = d.charCodeAt(r), f += m.charAt(_ >>> 4 & 15) + m.charAt(15 & _); return f } function X(d) { for (var _ = Array(d.length >> 2), m = 0; m < _.length; m++)_[m] = 0; for (m = 0; m < 8 * d.length; m += 8)_[m >> 5] |= (255 & d.charCodeAt(m / 8)) << m % 32; return _ } function V(d) { for (var _ = "", m = 0; m < 32 * d.length; m += 8)_ += String.fromCharCode(d[m >> 5] >>> m % 32 & 255); return _ } function Y(d, _) { d[_ >> 5] |= 128 << _ % 32, d[14 + (_ + 64 >>> 9 << 4)] = _; for (var m = 1732584193, f = -271733879, r = -1732584194, i = 271733878, n = 0; n < d.length; n += 16) { var h = m, t = f, g = r, e = i; f = md5_ii(f = md5_ii(f = md5_ii(f = md5_ii(f = md5_hh(f = md5_hh(f = md5_hh(f = md5_hh(f = md5_gg(f = md5_gg(f = md5_gg(f = md5_gg(f = md5_ff(f = md5_ff(f = md5_ff(f = md5_ff(f, r = md5_ff(r, i = md5_ff(i, m = md5_ff(m, f, r, i, d[n + 0], 7, -680876936), f, r, d[n + 1], 12, -389564586), m, f, d[n + 2], 17, 606105819), i, m, d[n + 3], 22, -1044525330), r = md5_ff(r, i = md5_ff(i, m = md5_ff(m, f, r, i, d[n + 4], 7, -176418897), f, r, d[n + 5], 12, 1200080426), m, f, d[n + 6], 17, -1473231341), i, m, d[n + 7], 22, -45705983), r = md5_ff(r, i = md5_ff(i, m = md5_ff(m, f, r, i, d[n + 8], 7, 1770035416), f, r, d[n + 9], 12, -1958414417), m, f, d[n + 10], 17, -42063), i, m, d[n + 11], 22, -1990404162), r = md5_ff(r, i = md5_ff(i, m = md5_ff(m, f, r, i, d[n + 12], 7, 1804603682), f, r, d[n + 13], 12, -40341101), m, f, d[n + 14], 17, -1502002290), i, m, d[n + 15], 22, 1236535329), r = md5_gg(r, i = md5_gg(i, m = md5_gg(m, f, r, i, d[n + 1], 5, -165796510), f, r, d[n + 6], 9, -1069501632), m, f, d[n + 11], 14, 643717713), i, m, d[n + 0], 20, -373897302), r = md5_gg(r, i = md5_gg(i, m = md5_gg(m, f, r, i, d[n + 5], 5, -701558691), f, r, d[n + 10], 9, 38016083), m, f, d[n + 15], 14, -660478335), i, m, d[n + 4], 20, -405537848), r = md5_gg(r, i = md5_gg(i, m = md5_gg(m, f, r, i, d[n + 9], 5, 568446438), f, r, d[n + 14], 9, -1019803690), m, f, d[n + 3], 14, -187363961), i, m, d[n + 8], 20, 1163531501), r = md5_gg(r, i = md5_gg(i, m = md5_gg(m, f, r, i, d[n + 13], 5, -1444681467), f, r, d[n + 2], 9, -51403784), m, f, d[n + 7], 14, 1735328473), i, m, d[n + 12], 20, -1926607734), r = md5_hh(r, i = md5_hh(i, m = md5_hh(m, f, r, i, d[n + 5], 4, -378558), f, r, d[n + 8], 11, -2022574463), m, f, d[n + 11], 16, 1839030562), i, m, d[n + 14], 23, -35309556), r = md5_hh(r, i = md5_hh(i, m = md5_hh(m, f, r, i, d[n + 1], 4, -1530992060), f, r, d[n + 4], 11, 1272893353), m, f, d[n + 7], 16, -155497632), i, m, d[n + 10], 23, -1094730640), r = md5_hh(r, i = md5_hh(i, m = md5_hh(m, f, r, i, d[n + 13], 4, 681279174), f, r, d[n + 0], 11, -358537222), m, f, d[n + 3], 16, -722521979), i, m, d[n + 6], 23, 76029189), r = md5_hh(r, i = md5_hh(i, m = md5_hh(m, f, r, i, d[n + 9], 4, -640364487), f, r, d[n + 12], 11, -421815835), m, f, d[n + 15], 16, 530742520), i, m, d[n + 2], 23, -995338651), r = md5_ii(r, i = md5_ii(i, m = md5_ii(m, f, r, i, d[n + 0], 6, -198630844), f, r, d[n + 7], 10, 1126891415), m, f, d[n + 14], 15, -1416354905), i, m, d[n + 5], 21, -57434055), r = md5_ii(r, i = md5_ii(i, m = md5_ii(m, f, r, i, d[n + 12], 6, 1700485571), f, r, d[n + 3], 10, -1894986606), m, f, d[n + 10], 15, -1051523), i, m, d[n + 1], 21, -2054922799), r = md5_ii(r, i = md5_ii(i, m = md5_ii(m, f, r, i, d[n + 8], 6, 1873313359), f, r, d[n + 15], 10, -30611744), m, f, d[n + 6], 15, -1560198380), i, m, d[n + 13], 21, 1309151649), r = md5_ii(r, i = md5_ii(i, m = md5_ii(m, f, r, i, d[n + 4], 6, -145523070), f, r, d[n + 11], 10, -1120210379), m, f, d[n + 2], 15, 718787259), i, m, d[n + 9], 21, -343485551), m = safe_add(m, h), f = safe_add(f, t), r = safe_add(r, g), i = safe_add(i, e) } return Array(m, f, r, i) } function md5_cmn(d, _, m, f, r, i) { return safe_add(bit_rol(safe_add(safe_add(_, d), safe_add(f, i)), r), m) } function md5_ff(d, _, m, f, r, i, n) { return md5_cmn(_ & m | ~_ & f, d, _, r, i, n) } function md5_gg(d, _, m, f, r, i, n) { return md5_cmn(_ & f | m & ~f, d, _, r, i, n) } function md5_hh(d, _, m, f, r, i, n) { return md5_cmn(_ ^ m ^ f, d, _, r, i, n) } function md5_ii(d, _, m, f, r, i, n) { return md5_cmn(m ^ (_ | ~f), d, _, r, i, n) } function safe_add(d, _) { var m = (65535 & d) + (65535 & _); return (d >> 16) + (_ >> 16) + (m >> 16) << 16 | 65535 & m } function bit_rol(d, _) { return d << _ | d >>> 32 - _ }
