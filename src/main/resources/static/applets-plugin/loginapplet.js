(function () {
    let pnode = document.currentScript.parentNode;

    let uid = document.createElement('input');
    let password = document.createElement('input');
    let login = document.createElement('button');
    login.innerText = 'Login'

    let buts = [uid, password, login]
    let type = ['text', 'password', '']
    let labl = ['user id', 'password', '']

    for (let i = 0; i < buts.length; i++) {
        const e = buts[i];
        e.placeholder = labl[i]
        e.style.margin = "6px"
        e.style.display = 'block'
        e.type = type[i]
        pnode.appendChild(e)
    }
    
    login.addEventListener('click', function () {
        post(
            '/auth/login',
            {},
            {
                'Authorization': `Basic ${btoa(uid.value + ":" + password.value)}`
            }
        ).then((d) => {
            let h = d.headers;
            setCookie(USER_KEY, h['authinfo'].split(":")[0]);
            setCookie(TOK_KEY, h['authinfo'].split(":")[1]);
            inodes.showUserInfo()
            inodes.triggerSearch('')
        }).catch(() => {
            alert('authentication failed. check the username and password')
        })
    })
})();