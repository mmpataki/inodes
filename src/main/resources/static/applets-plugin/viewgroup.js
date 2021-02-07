let A = getUApi()
let g = A.getInput()[0]
let tagStyle = 'display: inline-block; padding: 2px 8px; background-color: #eaeaea; border: solid 1px gray; color: black; margin: 2px; border-radius: 3px; cursor: pointer;'
let grp;
let gUsersDiv;
let aUsersDiv;
let addBtn;

let gName = A.makeH3({ style: "margin: 3px 0px" })
let gDesc = A.makeSpan({})
let gMail = A.makeSpan({})
let gTUrl = A.makeSpan({})
gUsersDiv = A.makeDiv({ style: 'display: block; padding:5px; border: solid 1px #f2f2f2; border-radius: 8px; margin-top: 5px' })
A.makeSpan({ innerText: 'All users', style: 'margin: 5px 0px; font-weight: bolder' })
allUsers = A.makeDiv({ style: 'display: block; padding:5px; border: solid 1px #f2f2f2; border-radius: 8px; margin-top: 5px' })

get(`/auth/groups/${g}`).then(x => {
    grp = JSON.parse(x.response)
    gName.innerText = grp.groupName
    gDesc.innerHTML = `<b>desc: </b>${grp.desc}<br/>`
    gMail.innerHTML = `<b>email: </b>${grp.email}<br/>`
    gTUrl.innerHTML = `<b>teams url: </b>${grp.teamsUrl}<br/>`
    grp.users.forEach(user => addToGList(user))
}).then(x => {
    get('/auth/users').then(x => {
        let users = JSON.parse(x.response)
        users.forEach(user => {
            addToAList(user)
        })
    })
})

function addToGList(user) {
    if (addBtn) {
        addBtn.remove()
    }
    let uspan = A.makeSpan({ innerText: user, style: tagStyle }, gUsersDiv)
    let urem = A.makeSpan({ innerText: 'x', style: 'padding: 0px 3px; font-size: 0.8em; border-radius: 50%; background-color: black; color: white; margin-left: 5px' }, uspan)
    urem.addEventListener('click', function () {
        post(`/auth/groups/${g}/delete?user=${user}`)
            .then(x => showSuccess(`Removed ${user} from ${g}`))
            .then(x => uspan.remove())
            .then(x => grp.users.splice(grp.users.indexOf(user), 1))
            .then(x => addToAList(user))
            .catch(x => showError(x.message))
    })
    if (addBtn) {
        gUsersDiv.appendChild(addBtn)
    }
}

let all__userToSpan = {}
function addToAList(user) {
    if(all__userToSpan[user]) {
        all__userToSpan[user].remove()
    }
    let uspan = A.makeSpan({ style: tagStyle, innerText: user }, allUsers);
    all__userToSpan[user] = uspan;
    if (!grp.users.includes(user)) {
        let uadd = A.makeSpan({ uid: user, style: 'padding: 0px 3px; font-size: 0.8em; border-radius: 50%; background-color: green; color: white; margin-left: 5px', innerHTML: '+' }, uspan);
        uadd.addEventListener('click', function () {
            post(`/auth/groups/${g}/add?user=${user}`)
                .then(x => showSuccess(`Added ${user} to ${g}`))
                .then(x => uadd.remove())
                .then(x => grp.users.push(user))
                .then(x => addToGList(user))
                .catch(x => showError(x.message))
        })
    }
}
