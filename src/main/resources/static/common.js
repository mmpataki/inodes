const USER_KEY = "user", TOK_KEY = "tok";
let baseUrl = window.location.port == 5001 ? "http://localhost:8080" : ""
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

function filePicker(selectedFiles) {
    let self = this;
    function refreshUserFileList() {
        get('/allmyfiles')
            .then(resp => JSON.parse(resp.response))
            .then(files => {
                console.log(files)
                let tab = render('user-file-tab', {
                    ele: 'table',
                    classList: 'le',
                    children: [
                        {
                            ele: 'tr',
                            children: [
                                { ele: 'th', text: 'Choosen' },
                                { ele: 'th', text: 'Name' },
                                { ele: 'th', text: 'Last Modified Time' },
                                { ele: 'th', text: 'Size' },
                                { ele: 'th', text: 'Path' },
                                { ele: 'th', text: 'Actions' }
                            ]
                        }
                    ]
                })
                self.userFiles.innerHTML = ''
                self.userFiles.appendChild(tab)
                files.slice(1, files.length).forEach(file => {
                    let path = `/u/files/${files[0].name}/${file.name}`
                    render('user-file', {
                        ele: 'tr',
                        classList: 'row',
                        children: [
                            {
                                ele: 'td',
                                children: [
                                    {
                                        ele: 'input',
                                        classList: 'select-box',
                                        attribs : {
                                            type: 'checkbox',
                                            value: path,
                                            checked: selectedFiles ? selectedFiles.includes(path) : false
                                        }
                                    }
                                ]
                            },
                            { ele: 'td', text: file.name },
                            { ele: 'td', text: new Date(file.mtime).toLocaleString() },
                            { ele: 'td', text: "" + file.size },
                            { ele: 'td', text: path },
                            {
                                ele: 'a', 
                                text: 'Preview',
                                attribs: { href: '#' }, 
                                evnts : {
                                    click: function () {
                                        let img = document.createElement('img')
                                        img.style = 'max-width: 100px; max-height: 100px'
                                        img.src = `/u/files/${files[0].name}/${file.name}`
                                        this.parentNode.appendChild(img)
                                        this.remove()
                                    }
                                }
                            }
                        ],
                        evnts : {
                            click: function () {
                                this.querySelector('input[type=checkbox]').checked = !this.querySelector('input[type=checkbox]').checked
                            }
                        }
                    }, () => 0, tab)
                })
            })
    }
    return new Promise((resolve, reject) => {
        let fp = render('inodes-file-picker', {
            ele: 'div',
            classList: 'container',
            children: [
                { ele: 'iframe', attribs: { name: 'hehe', style: 'display: none' } }, // to stop browser redirect after a post
                {ele: 'h4', text: 'Upload files'},
                {
                    ele: 'form',
                    classList: 'upload-form',
                    attribs: {
                        target: 'hehe'
                    },
                    children: [
                        {
                            ele: "input",
                            attribs: {
                                type: "file",
                                name: "file"
                            }
                        },
                        {
                            ele: "button",
                            text: "upload",
                            evnts: {
                                click: function () {
                                    let fd = new FormData(this.parentNode);
                                    postFile(`/files`, fd, {})
                                        .then(e => { self.file = e.response })
                                        .then(e => showSuccess('Uploaded !'))
                                        .catch(e => showError(e.message))
                                }
                            }
                        }
                    ]
                },
                {ele: 'h4', text: 'Your files'},
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
                            evnts : {
                                click: function () {
                                    let f = [], cbs = self.userFiles.getElementsByClassName('user-file-select-box');
                                    for (let i = 0; i < cbs.length; i++) {
                                        if(cbs[i].checked)
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
                            evnts : {
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