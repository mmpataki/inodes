<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test</title>
    <script type="text/javascript" src="common.js"></script>
</head>
<body>
    <input class="searchbox" type="text" value="!mmp"></input>
    <script>

(function() {
    let capitalizeFirstLetter = function(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }
    let getInput = function() {
        let xx = c('searchbox').value.split(/\s+/).filter(x => x[0] == '!')[0]
        if(!xx) return null;
        return xx.substring(1)
    }
    let inputUserId = getInput()
    let curUserId = getCurrentUser()
    inputUserId = inputUserId ? inputUserId : curUserId

    console.log(inputUserId, curUserId)

    let h = document.createElement('h3')
    h.innerText = inputUserId
    
    let d = document.createElement('table')

    let proms = [];
    if(inputUserId) proms.push(get(`/auth/user/${inputUserId}`))
    if(curUserId) proms.push(inputUserId == curUserId ? proms[0] : get(`/auth/user/${curUserId}`))

    Promise.all(proms).then(function(resps) {
        
        console.log(resps)

        let user, curUser;

        user = JSON.parse(resps[0].response)
        if(resps.length > 1) {
            curUser = JSON.parse(resps[1].response)
        }

        let ALL_ROLES = ["CREATE", "EDIT", "UPVOTE", "DOWNVOTE", "COMMENT", "DELETE", "ADMIN"]

        let canGiveThisPermission = function(u1, u2, perm) {
            if(!u1 || !u2) return false
            let rs1 = u1.basic.roles;
            if(!rs1.includes(perm)) return false;
            let r1 = rs1.split(",").sort();

            let rs2 = u2.basic.roles;
            let r2 = rs2.split(",");

            for(let i = 0; i < r2.length; i++) {
                if(!rs1.includes(r2[i])) {
                    return false;
                }
            }
            return true;
        }

        let getEditIcon = function() {
            return `<span style='border-left: solid 1px gray; float: right; cursor: pointer; padding: 3px;'
                        onclick='this.previousElementSibling.removeAttribute("readonly"); this.previousElementSibling.focus()'>&#x1F589;</span>`;
        }

        let valueMarshallers = {
            'roles' : function(key, roles) {
                return ALL_ROLES
                        .map(R => `<input type="checkbox" class="rolecbox" datakey='${R}' ${roles.includes(R) ? 'checked' : ''} ${!canGiveThisPermission(curUser, user, R) ? 'disabled' : ''}>${R}</input>`)
                        .join('<br/>')
            },
            '*' : function(key, v, editable, typ) {
                return `
                    <div style="border: solid 1px gray; border-radius: 3px">
                            <input type='${typ ? typ : 'text'}' style='border: none; padding: 5px; border-width: 0px; width: 310' datakey='${key}' value="${v}" readonly></input>
                            ${editable ? getEditIcon() : ''}
                    </div>`
            },
            'verified': function(key, v) { return `<input type="checkbox" ${v?'checked':''} style="pointer-events: none" readonly/>`},
            'password' : function(key, v) { return valueMarshallers['*']('password', '', true, 'password') }
        }

        let showToUserOnly = ['password'];
        let othersEditable = ['roles'];
        let readonly = ['userName'];

        let html = '';

        html = `<tr><td style="text-align: center; padding-right: 10px; width: 100px"><h3>Basic Info</h3></td></tr>`
        Object.keys(user.basic).forEach(function(k) {
            if((showToUserOnly.includes(k) && inputUserId != curUserId && !curUser.basic.roles.includes('ADMIN'))) {
                return;
            }
            let marshaller = valueMarshallers[k] ? valueMarshallers[k] : valueMarshallers['*']
            html += `
                <tr>
                    <td style="text-align: right; padding-right: 10px" data='basic' key='${k}'>${capitalizeFirstLetter(k)}</td>
                    <td>${marshaller(k, user.basic[k], (!readonly.includes(k) && (curUserId == inputUserId || curUser.basic.roles.includes('ADMIN') || othersEditable.includes(k))))}</td>
                </tr>`
        })
        html += `<tr><td/><td><button class='saveuserinfo-btn-stupid'>Save</button></td></tr><tr></tr><tr><td style="text-align: center; padding-right: 10px"><h3>Posts</h3></td></tr>`
        Object.keys(user.postsCount).forEach(function(k) {
          html += `
            <tr>
            <td style="width: 30px; text-align: right"><b>${user.postsCount[k]}</b></td>
            <td style="padding-left: 10px">
               <a href="?q=${ encodeURIComponent("~" + user.basic.userName + " %" + k) }">
                  ${k}
               </a>
            </td>
            </tr>
          `
        });

        d.innerHTML = html;
        d.querySelector('button.saveuserinfo-btn-stupid').addEventListener('click', () => {

            let user = {};

            let txts = d.querySelectorAll('input[type=text], input[type=password]');
            for (let i = 0; i < txts.length; i++) {
                let k = txts[i].getAttribute('datakey')
                if(k) {
                    if(k != 'password' || txts[i].value != '') {
                        user[k] = txts[i].value
                    }
                }
            }

            let rols = d.querySelectorAll('input[type=checkbox].rolecbox')
            console.log(rols)
            let role_list = []
            for (let i = 0; i < rols.length; i++) {
                if(rols[i].checked)
                    role_list.push(rols[i].getAttribute('datakey'))
            }
            user['roles'] = role_list.join(',')
            console.log(user)
            post(
                '/auth/user',
                user,
                {'Content-Type': 'application/json'}
            ).then( x => {
                alert('Done');
                inodes.refresh();
            })
        })
    })
    let pnode = document.currentScript.parentNode;
    pnode.appendChild(h);
    pnode.appendChild(d);
})();

    </script>
</body>
</html>