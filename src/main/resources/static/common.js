const USER_KEY = "user", TOK_KEY = "tok";
let baseUrl = window.location.port == 5001 ? "http://inedctst01:8080" : ""
let rejectCodeList = [400, 401, 500, 403];
function getBaseUrl() {
    return baseUrl;
}
function ajax(method, url, data, hdrs, cancelToken) {
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
                resolve({
                    response: this.responseText,
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
        xhttp.open(method, `${baseUrl}${url}`, true);
        hdrs && Object.keys(hdrs).forEach(key => xhttp.setRequestHeader(key, hdrs[key]))
        xhttp.send(data);
    });
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
    return ajax('POST', url, JSON.stringify(data), hdrs);
}
function postFile(url, data, hdrs) {
    return ajax('POST', url, data, hdrs);
}
function delet(url) {
    return ajax('DELETE', url);
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

function showMessage(x, col) {
    let d = document.createElement('div')
    d.classList = 'fade-out'
    d.style = `position: fixed; right: 10px; top: 10px; background-color: ${col.background}; border: solid 2px ${col.border}; border-radius: 3px; padding: 10px; color: ${col.color}; font-size: 1em`
    d.innerText = x
    document.body.appendChild(d)
    setTimeout(() => d.remove(), 8000)
}

function render(name, spec, elemCreated, container) {
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
        let lbl;
        if (spec.label || spec.postlabel) {
            let rgid = "id_" + Math.random();
            e.id = rgid
            lbl = document.createElement('label')
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

function callWithWaitUI(element, func) {
    return new _callWithWaitUI(element, func);
}
function _callWithWaitUI(element, func) {
    element.style.position = "relative";
    let overlay = render('loader', {
        ele: 'div',
        attribs: {
            style: 'position: absolute; top: 0; left: 0; right: 0; bottom: 0; background-color: white; opacity: 0.5'
        },
        children: [
            {
                ele: 'span',
                attribs: { style: `position: absolute; top: ${element.clientHeight / 2 - 10}px; left: ${element.clientWidth / 2 - 50}px` },
                children: [
                    { ele: 'img', attribs: { src: '/wait.gif', style: `height: 20px; width: 20px` } },
                    { ele: 'span', iden: 'loadTxt', attribs: { style: 'margin-left: 10px' }, text: 'Loading' }
                ]
            }
        ]
    }, (id, ele) => this[id] = ele);
    element.appendChild(overlay);
    let done = () => overlay.remove();
    let updateText = (txt) => this.loadTxt.innerText = txt;
    try {
        func(done, updateText);
    } catch {
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
        if (typeof (a[k]) == 'string') {
            return (sortConf[currSortKey] == 'asc') ? a[k].localeCompare(b[k]) : b[k].localeCompare(a[k]);
        }
        return (sortConf[currSortKey] == 'asc') ? a[k] - b[k] : b[k] - a[k];
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
        callWithWaitUI(self.fp, (done) => {
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
                    { ele: 'span', classList: 'button', iden: 'backbtn', attribs: { title: 'Back', innerHTML: '&larr;' }, evnts: { click: () => this.back() } },
                    { ele: 'span', classList: 'title', text: '', iden: 'title' }
                ]
            },
            { ele: 'div', classList: 'errmsg', iden: 'errmsg' },
            { ele: 'div', classList: 'content', iden: 'storyBoard' },
            {
                ele: 'div', classList: 'bottombar', children: [
                    { ele: 'button', text: 'Next', iden: 'nextbtn', evnts: { click: () => this.next() } }
                ]
            }
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
        this.nextbtn.disabled = true
        let storyPack = stack[stack.length - 1]
        if (!storyPack.story.isCompleted()) {
            this.errmsg.innerHTML = storyPack.story.getErrMsg();
            this.errmsg.style.display = 'block'
            return;
        }
        let preDestroy = storyPack.story.preDestroy || (() => new Promise((resolve) => resolve()))
        preDestroy()
            .then(() => {
                let next = { storyClass: storyPack.story.nextStoryClass(), args: storyPack.story.moral() }
                if (!next) return;
                this.openStory(next.storyClass, next.args)
            })
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
